(ns dnd.core
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [dnd.battle :as battle]))

(declare -main)
(declare movement)
(declare level-selection)
(declare conjv)
(declare level-includes?)
(declare level)
(declare player-turn-during-battle)

(defn dice-roll [sides]
  (inc (rand-int sides)))

(defn potion-roll []
  (+ (dice-roll 4) (dice-roll 4) 4))

(defn add-message [state message] (update state :messages conjv message))

(defn enemy-attack2 [state]
  (let [enemy-roll (dice-roll 20)
        state (add-message state (str "Enemy rolled: " enemy-roll))
        state (assoc state :initiative :player)]
    (cond
      (= enemy-roll 20) (-> (add-message state "Enemy critical hit!")
                            (update :hp - (* 2 (:enemy-damage state))))
      (> enemy-roll (:ac state)) (-> (add-message state "Enemy hit!")
                                     (update :hp - (:enemy-damage state)))
      :else (add-message state "Enemy miss!"))))

(defn win-results [state]
  (-> (add-message state "You won!")
  (assoc :battle? false)))

(defn loss-results []
  (do (println "You died!") (System/exit 0)))

(defn player-attack? [player-action]
  (or (= player-action "attack") (= player-action "a")))

(defn player-take-potion? [player-action]
  (or (= player-action "quaff") (= player-action "q")))

(defn view-inventory? [player-action]
  (or (= player-action "inventory") (= player-action "i")))

(defn player-attacks [{:keys [damage enemy-ac] :as state}]
  (if (> (dice-roll 20) enemy-ac)
    (-> (add-message state "Hit!")
        (update :enemy-hp - (dice-roll damage)))
    (add-message state "Miss")))

(defn player-takes-potion [{:keys [potion] :as state}]
  (if (> potion 0)
    (-> (add-message state "Taking health potion!")
        (update :hp + (potion-roll))
        (update :potion dec))
    (add-message state "No health potion to take")))

(defn view-inventory [{:keys [potion] :as state}]           ;; TODO - CRM: "Dont let enemy attack after viewing inventory"
  (-> (add-message state (str "Inventory:\n" potion " Potions"))))

(defn player-turn-text []
  (println "What will you do? \nattack-(a)\ntake health potion-(t)\ninventory-(i)"))

(defn clear-terminal []
  (->> (shell/sh "/bin/sh" "-c" "clear <  /dev/null") :out (print "")))

(defn player-turn-during-battle [state]
  (player-turn-text)
  (let [player-action (read-line)
        state (cond
                (player-attack? player-action) (player-attacks state)
                (player-take-potion? player-action) (player-takes-potion state)
                (view-inventory? player-action) (view-inventory state)
                :else (add-message state "SELECT A VALID OPTION."))]
    (clear-terminal)
    (assoc state :initiative :enemy)))

(defn battle-status [state]
  (-> (add-message state (str (:player state)))
      (add-message (str "Your HP: " (:hp state) "          " "Enemy HP: " (:enemy-hp state) "\n"))))

(defn save! [state]
  (spit (:player state) state)
  state)

(defn player-starts-battle [{:keys [hp enemy-hp] :as state}]
  (let [state (save! state)
        state (battle-status state)]
    (cond
      (<= enemy-hp 0) (win-results state)
      (<= hp 0) (loss-results)
      (= :enemy (:initiative state)) (enemy-attack2 state)
      :else (player-turn-during-battle state))))

(defn start-battle [state mob]
  (let [state (-> (assoc state :battle? true)
                  (merge (dissoc mob :name))
                  (add-message (str "You are about to fight a " (:name mob) ",")))]
    (if (= (battle/initiative-roll) "first")
      (let [state (assoc state :initiative :player)] state)
      (let [state (assoc state :initiative :enemy)] state))))

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
  (add-message state "Where would you like to move?"))

(defn incorrect-command-text [state]
  (add-message state "Incorrect command"))

(defn get-user-action [state]
  (get-user-action-text state)
    (direction? state)
  (let [user-action (read-line)]
    (if (or (= user-action "n") (= user-action "s") (= user-action "e") (= user-action "w") (= user-action "i") (= user-action "d"))
      user-action
      (do (incorrect-command-text state) (get-user-action state)))))

(def move-deltas {"e" [1 0]
                  "w" [-1 0]
                  "n" [0 1]
                  "s" [0 -1]})

(defn next-dungeon? [room level]
  (= room (vec (last (keys level)))))

(defn next-dungeon [state]
  (-> (update state :level inc) (add-message "Next dungeon!")))

(defn potion-room? [room level]
  (let [room-info (get level room)
        items (:items room-info)]
    (contains? (set items) :potion)))

(defn potion-room [state new-x new-y]
  (-> (update state :potion inc) (assoc :room [new-x new-y]) (add-message "You found a potion!")))

(defn kobold-room? [room level]
  (let [room-info (get level room)
        items (:mobs room-info)]
    (contains? (set items) :kobold)))

(defn troll-room? [room level]
  (let [room-info (get level room)
        items (:mobs room-info)]
    (contains? (set items) :troll)))

(defn level [state]
  (read-string (slurp (io/resource (str "dnd/levels/" (:level state) ".edn")))))

(defn level-includes? [room level]
  (clojure.string/includes? level (str room)))

(def kobold {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4 :name "kobold"})
(def troll {:enemy-hp 84 :enemy-ac 15 :enemy-damage 29 :name "troll"})

(defn move [{:keys [room] :as state} direction]
  (clear-terminal)
  ;(flush)
  (let [[x y] room
        [dx dy] (get move-deltas direction)
        new-x (+ x dx)
        new-y (+ y dy)
        level (level state)]
    (cond
      (next-dungeon? [new-x new-y] level) (level-selection (next-dungeon state))
      (potion-room? [new-x new-y] level) (potion-room state new-x new-y)
      (kobold-room? [new-x new-y] level) (start-battle state kobold)
      (troll-room? [new-x new-y] level) (start-battle state troll)
      (level-includes? [new-x new-y] level) (assoc state :room [new-x new-y])
      :else (add-message state "\nCan't go that direction pick a new one\n"))))

(defn conjv [col value] (conj (vec col) value))

(defn handle-move [state user-action]
  (let [state (move state user-action)
        [x y] (:room state)]
    (add-message state (str "Location: " x " " y))))

(defn inventory? [user-action] (or (= user-action "inventory") (= user-action "i")))

(defn do-inventory [state] (println "Inventory:\n" (:potion state) "Potions") state)

(defn drop-item? [user-action]
  (or (= user-action "drop") (= user-action "d")))

(defn drop-item [state]
  (-> (add-message state "You dropped a potion.")
      (update :potion dec)))

(defn process-turn [state user-action]
  (cond
    (inventory? user-action) (do-inventory state)
    (drop-item? user-action) (drop-item state)
    :else (handle-move state user-action)))

(defn movement [state]
  (loop [state state]
    (doseq [message (:messages state)] (println message))
    (let [state (dissoc state :messages)]
      (if (:battle? state)
        (recur (player-starts-battle state))
        (let [user-action (get-user-action state)
              new-state (process-turn state user-action)]
          (save! new-state)
          (recur new-state))))))

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
  (println "Enter new profile name:"))

(defn new-profile []
  (do (new-profile-text)
      (let [profile (read-line)]
        (spit profile {:room [0 0] :player profile :hp 10 :ac 12 :damage 5 :potion 0 :level 1 :battle? false :enemy-hp 0 :enemy-ac 0 :enemy-damage 0})
        (let [state (read-string (slurp profile))]
          state
          #_(level-selection state)))))

(defn file-exists? [name]
  (.exists (io/file name)))

(defn menu-text []
  (println "Enter player (new) for new player:"))

(defn -main [& _args]
  (menu-text)
  (let [name (read-line)]
    (let [state (if (file-exists? name) (read-string (slurp name)) (new-profile))
          state (if (:battle? state) state (level-selection state))]
      (movement state))))
