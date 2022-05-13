(ns dnd.main
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [dnd.battle :as battle]
            [dnd.core :as core]
            [dnd.ui :as ui]))

(declare -main)
(declare movement)
(declare level-selection)
(declare level-includes?)
(declare level)
(declare player-turn-during-battle)


(defn direction? [state]
  (let [[x y] (:room state)]
    (if (level-includes? [x (inc y)] (level state))
      (println "north(n)")
      ())
    (if (level-includes? [x (dec y)] (level state))
      (println "south(s)")
      ())
    (if (level-includes? [(inc x) y] (level state))
      (println "east(e)")
      ())
    (if (level-includes? [(dec x) y] (level state))
      (println "west(w)")
      ())))

(defn get-user-action-text [state]
  (core/add-message state "Where would you like to move?"))

(defn incorrect-command-text [state]
  (core/add-message state (core/red-text "Incorrect command")))

(defn get-user-action [state]
  (get-user-action-text state)
  (direction? state)
  (let [user-action (read-line)]
    (if (or (= user-action "n") (= user-action "s") (= user-action "e") (= user-action "w") (= user-action "i") (= user-action "d"))
      user-action
      (do (core/clear-terminal) (-> (incorrect-command-text state)
                                    (get-user-action))))))

(def move-deltas {"e" [1 0]
                  "w" [-1 0]
                  "n" [0 1]
                  "s" [0 -1]})

(def kobold {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4 :name "kobold"})
(def troll {:enemy-hp 84 :enemy-ac 15 :enemy-damage 29 :name "troll"})
(def kobold-pair {:enemy-hp 10 :enemy-ac 12 :enemy-damage 8 :name "kobold pair"})
(def kobold-group-three {:enemy-hp 15 :enemy-ac 12 :enemy-damage 12 :name "kobold group of 3"})
(def troll-pair {:enemy-hp 168 :enemy-ac 15 :enemy-damage 58 :name "troll pair"})

(defn next-dungeon? [room level]
  (= room (vec (last (keys level)))))

(defn next-dungeon [state]
  (-> (core/add-message state (core/color-text "1;42" (str "HP: " (:hp state))))
      (update :level inc)
      (core/add-message (core/blue-text "Next dungeon!"))))

(defn key-room? [room level]
  (let [room-info (get level room)
        items (:items room-info)]
    (contains? (set items) :key)))

(defn potion-room? [room level]
  (let [room-info (get level room)
        items (:items room-info)]
    (contains? (set items) :potion)))

(defn potion-room [state new-x new-y]                       ;; TODO - CRM: getting issue when going into potion room, exiting program, and then going back in
  (-> (core/add-message state (core/color-text "1;42" (str "HP: " (:hp state))))
      (update :potion inc)
      (assoc :room [new-x new-y])
      (core/add-message (core/blue-text "You found a potion!"))))

(defn key-room [state new-x new-y]
  (-> (core/add-message state (core/color-text "1;42" (str "HP: " (:hp state))))
      (update :key inc)
      (assoc :room [new-x new-y])
      (core/add-message (core/blue-text "You found a key!"))))

(defn kobold-room? [room level]
  (let [room-info (get level room)
        items (:mobs room-info)]
    (contains? (set items) :kobold)))

(defn troll-room? [room level]
  (let [room-info (get level room)
        items (:mobs room-info)]
    (contains? (set items) :troll)))

(defn two-kobolds? [room level]
  (let [room-info (get level room)
        items (:2 room-info)]
    (contains? (set items) :kobold)))

(defn three-kobolds? [room level]
  (let [room-info (get level room)
        items (:3 room-info)]
    (contains? (set items) :kobold)))

(defn two-trolls? [room level]
  (let [room-info (get level room)
        items (:2 room-info)]
    (contains? (set items) :troll)))

#_(defn kobold-count [room level state]
    (let [room-info (get level room)
          items (:2 room-info)]
      (cond
        (contains? (set items) :2) (battle/start-battle state {:enemy-hp 10 :enemy-ac 12 :enemy-damage 8 :name "kobold group"})
        (contains? (set items) :3) (battle/start-battle state {:enemy-hp 15 :enemy-ac 12 :enemy-damage 12 :name "kobold group"})
        :else (battle/start-battle state {:enemy-hp 20 :enemy-ac 12 :enemy-damage 16 :name "kobold group"})))

    )

(defn level [state]
  (read-string (slurp (io/resource (str "dnd/levels/" (:level state) ".edn")))))

(defn level-includes? [room level]
  (clojure.string/includes? level (str room)))


(defn move [{:keys [room] :as state} direction]
  (core/clear-terminal)
  (let [[x y] room
        [dx dy] (get move-deltas direction)
        new-x (+ x dx)
        new-y (+ y dy)
        level (level state)]
    (cond
      (next-dungeon? [new-x new-y] level) (level-selection (next-dungeon state))
      (potion-room? [new-x new-y] level) (potion-room state new-x new-y)
      (key-room? [new-x new-y] level) (key-room state new-x new-y)
      (kobold-room? [new-x new-y] level) (battle/start-battle state kobold)
      (troll-room? [new-x new-y] level) (battle/start-battle state troll)

      (two-kobolds? [new-x new-y] level) (battle/start-battle state kobold-pair)
      (three-kobolds? [new-x new-y] level) (battle/start-battle state kobold-group-three)
      (two-trolls? [new-x new-y] level) (battle/start-battle state troll-pair)

      (level-includes? [new-x new-y] level) (assoc state :room [new-x new-y])
      :else (-> (core/add-message state (core/color-text "1;42" (str "HP: " (:hp state))))
                (core/add-message "Can't go that direction pick a new one")))))

(defn handle-move [state user-action]
  (let [state (move state user-action)
        [x y] (:room state)]
    (core/clear-terminal)
    (-> (core/add-message state (core/color-text "1;42" (str "HP: " (:hp state))))
        (core/add-message (core/grey-text (str "Location: " x " " y))))))

(defn inventory? [user-action] (or (= user-action "inventory") (= user-action "i")))

(defn do-inventory [state] (do (core/clear-terminal)
                               (-> (core/add-message state (core/color-text "1;42" (str "HP: " (:hp state))))
                                   (core/add-message (core/blue-text (str "Inventory:\n" (:potion state) " Potions\n" (:key state) " Keys"))))))

(defn drop-item? [user-action]
  (or (= user-action "drop") (= user-action "d")))

(defn drop-item [state]
  (core/clear-terminal)
  (-> (core/add-message state (core/color-text "1;42" (str "HP: " (:hp state))))
      (core/add-message (core/blue-text "You dropped a potion."))
      (update :potion dec)))

(defn process-turn [state user-action]
  (cond
    (inventory? user-action) (do-inventory state)
    (drop-item? user-action) (drop-item state)
    :else (handle-move state user-action)))

(defn movement [state]
  (loop [state state]
    ;(core/save! state)
    ;; ui/update
    ;(clean-terminal)
    (when (:battle? state) (println (core/color-text "42" (str "Your HP: " (:hp state) "          " "Enemy HP: " (:enemy-hp state) "\n"))))
    (doseq [message (:messages state)] (println message))
    ;; print user prompt
    ; ---
    (let [state (dissoc state :messages)]
      (if (:battle? state)
        (recur (battle/player-starts-battle state))
        (let [user-action (get-user-action state)
              new-state (process-turn state user-action)]
          (core/save! new-state)                            ;; TODO - CRM: don't think I should be here
          (recur new-state))))))

(defn process-action [state]
  (if (:battle? state)
    (cond
      (battle/player-attack? (:action state)) (battle/player-attacks state)
      (battle/player-take-potion? (:action state)) (battle/player-takes-potion state)
      (battle/view-inventory? (:action state)) (do-inventory state)
      :else (core/add-message state (core/red-text "SELECT A VALID OPTION.")))
    (process-turn state (:action state))))

;(defn tick [state]
;  (ui/update state)                                         ;; This is the one and only place where the terminal updates
;  (let [action (ui/get-user-action state)                   ;; this is the one and only place where user input is accepted
;        new-state (process-action state action)]
;    (core/save! new-state)))
;
;(defn run [state]
;  (loop [state state]
;    (when-not (:game-over? state)
;      (recur (tick state)))))

(defn open-level [file-level]
  (read-string (slurp (io/resource file-level))))

(defn file-path [state]
  (str "dnd/levels/" (get state :level) ".edn"))

(defn level-selection [state]
  (let [file-level (file-path state)
        level-map (open-level file-level)
        start-position (vec (first (keys level-map)))]      ;; TODO - CRM: search for :start? true
    (assoc state :room start-position)
    #_(movement state)))

(defn new-profile-text []
  (println (core/yellow-text "Enter new profile name:")))

(defn new-profile []
  (do (new-profile-text)
      (let [profile (read-line)]
        (spit profile {:room [0 0] :player profile :hp 10 :ac 12 :damage 5 :potion 0 :key 0 :level 1 :battle? false :enemy-hp 0 :enemy-ac 0 :enemy-damage 0})
        (let [state (read-string (slurp profile))]
          state
          #_(level-selection state)))))

(defn file-exists? [name]
  (.exists (io/file name)))

(defn menu-text []
  (println (core/yellow-text "Enter player (new) for new player:")))

(defn -main [& _args]
  ;(println (core/color-text "1" "test"))
  (menu-text)
  (let [name (read-line)]
    (let [state (if (file-exists? name) (read-string (slurp name)) (new-profile))
          state (if (:battle? state) state (level-selection state))]
      (movement state))))
