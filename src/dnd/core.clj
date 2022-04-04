(ns dnd.core)

(declare -main)
(declare movement)

(defn dice-roll [sides]
  (inc (rand-int sides)))

(defn potion-roll []
  (+ (dice-roll 4) (dice-roll 4) 4))

(defn enemy-attack [hp ac enemy-damage]
  (let [enemy-roll (dice-roll 20)]
    (cond
      (= enemy-roll 20) (do (println "Enemy rolled:" enemy-roll "\nEnemy critical hit!") (- hp (* 2 (dice-roll enemy-damage))))
      (> enemy-roll ac) (do (println "Enemy rolled:" enemy-roll "\nEnemy hit!") (- hp (dice-roll enemy-damage)))
      :else (do (println "Enemy rolled:" enemy-roll "\nEnemy miss!") hp))))

(defn str-to-int [string-value]
  (Integer/parseInt string-value))

(defn player-starts-battle [location player hp ac damage potion enemy-hp enemy-ac enemy-damage]
  (loop [hp hp ac ac damage damage potion potion enemy-hp enemy-hp enemy-ac enemy-ac enemy-damage enemy-damage]
    (println "\nYour HP:" hp)
    (println "Enemy HP:" enemy-hp)
    (cond
      (<= enemy-hp 0) (do (println "You won!") (movement player location hp potion))
      (<= hp 0) (do (println "You died!") (System/exit 0))
      :else (do (println "What will you do? \nattack-(a)\ntake health potion-(t)\ninventory-(i)")
                (let [player-action (read-line) roll (dice-roll 20)]
                  (cond
                    (or (= player-action "attack") (= player-action "a")) (if (> (dice-roll 20) enemy-ac)
                                                                            (do (println "Hit!")
                                                                                (recur (enemy-attack hp ac enemy-damage) ac damage potion (- enemy-hp (dice-roll damage)) enemy-ac enemy-damage))
                                                                            (do (println "Miss")
                                                                                (recur (enemy-attack hp ac enemy-damage) ac damage potion enemy-hp enemy-ac enemy-damage)))
                    (or (= player-action "take health potion") (= player-action "t")) (if (> potion 0)
                                                                                        (do (println "Taking health potion") (recur (+ (potion-roll) (enemy-attack hp ac enemy-damage)) ac damage (- potion 1) enemy-hp enemy-ac enemy-damage))
                                                                                        (do (println "No health potion to take") (recur (enemy-attack hp ac enemy-damage) ac damage potion enemy-hp enemy-ac enemy-damage)))
                    (or (= player-action "inventory") (= player-action "i")) (do (println "Inventory:\n" potion "Potions") (recur hp ac damage potion enemy-hp enemy-ac enemy-damage))
                    :else (do (println "\nSELECT A VALID OPTION.") (recur hp ac damage potion enemy-hp enemy-ac enemy-damage))))))))

(defn enemy-starts-battle [location player hp ac damage potion enemy-hp enemy-ac enemy-damage]
  (loop [hp hp ac ac damage damage potion potion enemy-hp enemy-hp enemy-ac enemy-ac enemy-damage enemy-damage]
    (println "\nYour HP:" hp)
    (println "Enemy HP:" enemy-hp)
    (cond
      (<= hp 0) (do (println "You died!") (System/exit 0))
      (<= enemy-hp 0) (do (println "You won!") (movement player location hp potion))
      :else (do (println "What will you do? \nattack-(a)\ntake health potion-(t)\ninventory-(i)")
                (let [player-action (read-line)]
                  (cond
                    (or (= player-action "attack") (= player-action "a")) (if (> (dice-roll 20) enemy-ac)
                                                                            (do (println "Hit!")
                                                                                (recur (enemy-attack hp ac enemy-damage) ac damage potion (- enemy-hp (dice-roll damage)) enemy-ac enemy-damage))
                                                                            (do (println "Miss")
                                                                                (recur (enemy-attack hp ac enemy-damage) ac damage potion enemy-hp enemy-ac enemy-damage)))
                    (or (= player-action "take health potion") (= player-action "t")) (if (> potion 0)
                                                                                        (do (println "Taking health potion") (recur (+ (potion-roll) (enemy-attack hp ac enemy-damage)) ac damage (- potion 1) enemy-hp enemy-ac enemy-damage))
                                                                                        (do (println "No health potion to take") (recur (enemy-attack hp ac enemy-damage) ac damage potion enemy-hp enemy-ac enemy-damage)))
                    (or (= player-action "inventory") (= player-action "i")) (do (println "Inventory:\n" potion "Potions") (recur hp ac damage potion enemy-hp enemy-ac enemy-damage))
                    :else (do (println "\nSELECT A VALID OPTION.") (recur hp ac damage potion enemy-hp enemy-ac enemy-damage))))))))

(defn initiative-roll []
  (let [player-initiative-roll (dice-roll 20) enemy-initiative-roll (dice-roll 20)]
    (println "Your initiative roll was" player-initiative-roll "and the enemies roll was" enemy-initiative-roll)
    (cond
      (< enemy-initiative-roll player-initiative-roll) "first"
      (> enemy-initiative-roll player-initiative-roll) "second"
      :else "first")))

(defn kobold-start [location player hp ac damage potion]
  (println "You are about to fight a kobold,")
  (if (= (initiative-roll) "first")
    (player-starts-battle location player hp ac damage potion 5 12 4)
    (enemy-starts-battle location player hp ac damage potion 5 12 4)))

(defn troll-start [location player hp ac damage potion]
  (println "You are about to fight a troll,")
  (if (= (initiative-roll) "first")
    (player-starts-battle location player hp ac damage potion 84 15 29)
    (enemy-starts-battle location player hp ac damage potion 84 15 29)))

(defn get-user-action []
  (println "Where would you like to move?
  \n(n) north\n(s) south\n(e) east\n(w) west")
  (read-line))

(def move-deltas {"e" [1 0]
                  "w" [-1 0]
                  "n" [0 1]
                  "s" [0 -1]})

(defn move [[x y] direction]
  (let [[dx dy] (get move-deltas direction)
        ;delta (get move-deltas direction) ;; [1 0]
        ;dx (first delta)
        ;dy (second delta)
        new-x (+ x dx)
        new-y (+ y dy)]
    (if (or (> new-x 10) (< new-x -10) (> new-y 10) (< new-y -10))
      (do (println "Can't go that direction pick a new one") [x y])
      [new-x new-y])))

(defn increase-potion [potion]
  (let [new-potion-num (+ potion 1)]
    new-potion-num))

(defn handle-move [state user-action potion]
  (let [new-room (move (:room state) user-action)
        [x y] new-room]
    (println "Location: " x y)
    (if (= 1 (dice-roll 20))
      (do (println "You found a potion!") (assoc state :room new-room :potion (increase-potion (:potion state))))
      (assoc state :room new-room))))

(defn movement [player location hp potion]
  (loop [state {:room location :player player :hp hp :ac 12 :damage 5 :potion potion}]
    (slurp player)
    ;; get, get-in assoc, assoc-in, update, update-in
    (let [user-action (get-user-action)]
      (if (or (= user-action "inventory") (= user-action "i"))
        (do (println "Inventory:\n" potion "Potions") (recur state))
        (cond
          ;(= 1 (dice-roll 20)) (troll-start (get state :room) (get state :player) (get state :hp) (get state :ac) (get state :damage) (get state :potion))
          (= 2 (dice-roll 20)) (kobold-start (get state :room) (get state :player) (get state :hp) (get state :ac) (get state :damage) (get state :potion))
          :else
          (let [new-state (handle-move state user-action potion)
                ]
            (spit player new-state)
            (recur new-state)))))))

(defn -main [& args]
  (println "Enter player (new) for new player: ")
  (let [name (read-line)]
    (if (.exists (clojure.java.io/file name))
      (let [state (slurp name)] (movement (get (read-string state) :player) (get (read-string state) :room) (get (read-string state) :hp) (get (read-string state) :potion)))
      (do (println "Enter new profile name:") (let [profile (read-line)] (spit profile {:room [0 0] :player profile :hp 10 :ac 12 :damage 5 :potion 0})
                                                                         (let [state (slurp profile)] (movement (get (read-string state) :player) (get (read-string state) :room) (get (read-string state) :hp) (get (read-string state) :potion))))))))


