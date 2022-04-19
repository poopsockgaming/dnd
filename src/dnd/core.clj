(ns dnd.core
  (:require [clojure.java.io :as io]))

(declare -main)
(declare movement)
(declare level-selection)
(declare conjv)

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

;(defn enemy-attack [hp ac enemy-damage]
;  (let [enemy-roll (dice-roll 20)]
;    (cond
;      (= enemy-roll 20) (do (println "Enemy rolled:" enemy-roll "\nEnemy critical hit!") (- hp (* 2 (dice-roll enemy-damage))))
;      (> enemy-roll ac) (do (println "Enemy rolled:" enemy-roll "\nEnemy hit!") (- hp (dice-roll enemy-damage)))
;      :else (do (println "Enemy rolled:" enemy-roll "\nEnemy miss!") hp))))

(defn win-results [state]
  (println "You won!")
  (assoc state :battle? false))

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

(defn view-inventory [{:keys [potion] :as state}]
  (-> (add-message state (str "Inventory:\n" potion " Potions"))))

(defn player-turn-during-battle [state]
  (println "What will you do? \nattack-(a)\ntake health potion-(t)\ninventory-(i)")
  (let [player-action (read-line)
        state (cond
                (player-attack? player-action) (player-attacks state)
                (player-take-potion? player-action) (player-takes-potion state)
                (view-inventory? player-action) (view-inventory state)
                :else (add-message state "SELECT A VALID OPTION."))]
    (assoc state :initiative :enemy)))

(defn battle-status [state]
  (-> (add-message state (str "Your HP: " (:hp state)))
      (add-message (str "Enemy HP: " (:enemy-hp state)))))

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

;(defn enemy-starts-battle [state]
;  (loop [hp (:hp state) ac (:ac state) damage (:damage state) potion (:potion state) enemy-hp (:enemy-hp state) enemy-ac (:enemy-ac state) enemy-damage (:enemy-damage state)]
;    (spit (:player state) {:room (:room state) :player (:player state) :hp hp :ac ac :damage damage :potion potion :level (:level state) :battle (:battle state) :enemy-hp enemy-hp :enemy-ac enemy-ac :enemy-damage enemy-damage})
;    (println "\nYour HP:" hp)
;    (println "Enemy HP:" enemy-hp)
;    (cond
;      (<= hp 0) (loss-results)
;      (<= enemy-hp 0) (win-results state)
;      :else (do (println "What will you do? \nattack-(a)\ntake health potion-(t)\ninventory-(i)")
;                (let [player-action (read-line)]
;                  (cond
;                    (player-attack? player-action) (if (> (dice-roll 20) enemy-ac)
;                                                     (do (println "Hit!")
;                                                         (recur (enemy-attack hp ac enemy-damage) ac damage potion (- enemy-hp (dice-roll damage)) enemy-ac enemy-damage))
;                                                     (do (println "Miss")
;                                                         (recur (enemy-attack hp ac enemy-damage) ac damage potion enemy-hp enemy-ac enemy-damage)))
;                    (player-take-potion? player-action) (if (> potion 0)
;                                                          (do (println "Taking health potion") (recur (+ (potion-roll) (enemy-attack hp ac enemy-damage)) ac damage (- potion 1) enemy-hp enemy-ac enemy-damage))
;                                                          (do (println "No health potion to take") (recur (enemy-attack hp ac enemy-damage) ac damage potion enemy-hp enemy-ac enemy-damage)))
;                    (view-inventory? player-action) (do (println "Inventory:\n" potion "Potions") (recur hp ac damage potion enemy-hp enemy-ac enemy-damage))
;                    :else (do (println "\nSELECT A VALID OPTION.") (recur hp ac damage potion enemy-hp enemy-ac enemy-damage))))))))

(defn initiative-roll []
  (let [player-initiative-roll (dice-roll 20) enemy-initiative-roll (dice-roll 20)]
    (println "Your initiative roll was" player-initiative-roll "and the enemies roll was" enemy-initiative-roll)
    (cond
      (< enemy-initiative-roll player-initiative-roll) "first"
      (> enemy-initiative-roll player-initiative-roll) "second"
      :else "first")))

(defn start-battle [state mob]
  (let [state (-> (assoc state :battle? true)
                  (merge (dissoc mob :name))
                  (add-message (str "You are about to fight a " (:name mob) ",")))]
    (if (= (initiative-roll) "first")
      (let [state (assoc state :initiative :player)] state)
      (let [state (assoc state :initiative :enemy)] state))))

;(defn kobold-start [state]
;  (println "You are about to fight a kobold,")
;  (let [state (assoc state :battle 2 :enemy-hp 5 :enemy-ac 12 :enemy-damage 4)]
;    (if (= (initiative-roll) "first")
;      (player-starts-battle state)
;      (enemy-starts-battle state))))
;
;(defn troll-start [state]
;  (println "You are about to fight a troll,")
;  (let [state (assoc state :battle 2 :enemy-hp 84 :enemy-ac 15 :enemy-damage 29)]
;    (if (= (initiative-roll) "first")
;      (player-starts-battle state)
;      (enemy-starts-battle state))))

(defn get-user-action []
  (println "Where would you like to move?
  \n(n) north\n(s) south\n(e) east\n(w) west")
  (read-line))

(def move-deltas {"e" [1 0]
                  "w" [-1 0]
                  "n" [0 1]
                  "s" [0 -1]})

(defn next-dungeon? [room level]
  (= room (vec (last (keys level)))))

(defn potion-room? [room level]
  (let [room-info (get level room)
        items (:items room-info)]
    (contains? (set items) :potion)))

(defn kobold-room? [room level]
  (let [room-info (get level room)
        items (:mobs room-info)]
    (contains? (set items) :kobold)))

(defn troll-room? [room level]
  (let [room-info (get level room)
        items (:mobs room-info)]
    (contains? (set items) :troll)))

(defn level-includes? [room level]
  (clojure.string/includes? level (str room)))

(def kobold {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4 :name "kobold"})
(def troll {:enemy-hp 84 :enemy-ac 15 :enemy-damage 29 :name "troll"})

(defn move [{:keys [room] :as state} direction]
  (let [[x y] room
        [dx dy] (get move-deltas direction)
        new-x (+ x dx)
        new-y (+ y dy)
        level (read-string (slurp (io/resource (str "dnd/levels/" (:level state) ".edn"))))]
    (cond
      (next-dungeon? [new-x new-y] level) (do (println "Next dungeon!") (level-selection (update state :level inc)))
      (potion-room? [new-x new-y] level) (do (println "You found a potion!") (-> (update state :potion inc) (assoc :room [new-x new-y])))
      (kobold-room? [new-x new-y] level) (start-battle state kobold)
      (troll-room? [new-x new-y] level) (start-battle state troll)
      (level-includes? [new-x new-y] level) (assoc state :room [new-x new-y])
      :else (do (println "Can't go that direction pick a new one") state))))

(defn conjv [col value] (conj (vec col) value))

(defn handle-move [state user-action]
  (let [state (move state user-action)
        [x y] (:room state)]
    (add-message state (str "Location: " x " " y))))

(defn inventory? [user-action] (or (= user-action "inventory") (= user-action "i")))

(defn do-inventory [state] (println "Inventory:\n" (:potion state) "Potions") state)

(defn process-turn [state user-action]
  (cond
    (inventory? user-action) (do-inventory state)
    :else (handle-move state user-action)))

(defn movement [state]
  (loop [state state]
    (doseq [message (:messages state)] (println message))
    (let [state (dissoc state :messages)]
      (if (:battle? state)
        (recur (player-starts-battle state))
        (let [user-action (get-user-action)
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

(defn new-profile []
  (do (println "Enter new profile name:")
      (let [profile (read-line)]
        (spit profile {:room [0 0] :player profile :hp 10 :ac 12 :damage 5 :potion 0 :level 1 :battle? false :enemy-hp 0 :enemy-ac 0 :enemy-damage 0})
        (let [state (read-string (slurp profile))]
          state
          #_(level-selection state)))))

(defn file-exists? [name]
  (.exists (io/file name)))

(defn -main [& args]
  (println "Enter player (new) for new player:")
  (let [name (read-line)]
    (let [state (if (file-exists? name) (read-string (slurp name)) (new-profile))
          state (if (:battle? state) state (level-selection state))]
      (movement state))))
;(if (= (:battle state) 1)
;  (level-selection state)
;  (player-starts-battle state)))))

