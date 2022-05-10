(ns dnd.main-spec
  (:require [speclj.core :refer :all]
            [dnd.main :refer :all]
            [clojure.java.io :as io]
            [dnd.battle :as battle]
            [dnd.core :as core]))

(def state-level1 {:room [0 0] :player "test" :hp 10 :ac 12 :damage 5 :potion 0 :key 0 :level 1 :battle 1 :enemy-hp 0 :enemy-ac 0 :enemy-damage 0 :initiative :player})
(def state-level2 {:room [0 1] :player "test" :hp 10 :ac 12 :damage 5 :potion 1 :key 0 :level 2 :battle 1 :enemy-hp 0 :enemy-ac 0 :enemy-damage 0 :initiative :player})
(def state-level3 {:room [0 1] :player "test" :hp 10 :ac 12 :damage 0 :potion 1 :key 0 :level 3 :battle 1 :enemy-hp 10 :enemy-ac 0 :enemy-damage 0 :initiative :player})
(describe "main"

  (around [it] (with-redefs [core/clear-terminal (fn [])
                             core/color-text (fn [_ text] text)]
                 (it)))

  (it "file exists?"
    (should= true (file-exists? "caleb"))
    (should= false (file-exists? "abc")))

  (it "file path creation from :level"
    (should= "dnd/levels/1.edn" (file-path {:level 1})))

  (it "open file"
    (should= {
              [0, 0] {:start? true}
              [0, 1] {}
              [0, 2] {:exit? true}
              } (open-level "dnd/levels/1.edn")))

  (it "new profile text"
    (should-contain "Enter new profile name:" (with-out-str (new-profile-text))))

  (it "menu text"
    (should-contain "Enter player (new) for new player:" (with-out-str (menu-text))))

  (it "new profile data"
    (with-in-str "test"
      (with-out-str
        (should= {:key 0, :ac 12, :battle? false, :level 1, :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 0], :potion 0}
                 (new-profile)))))

  (it "level selection"
    (should= 1 (:level (level-selection state-level1))))

  (it "goes north once in level 1"
    (let [result (process-turn state-level1 "n")]
      (should= [0 1] (:room result))
      (should= "Location: 0 1" (second (:messages result)))))

  (it "goes west once in level 2"
    (let [result (process-turn state-level2 "w")]
      (should= [-1 1] (:room result))
      (should= "Location: -1 1" (second (:messages result)))))

  (it "tries to go wrong direction"
    (let [result (process-turn state-level1 "s")]
      (should= [0 0] (:room result))
      (should= "HP: 10" (first (:messages result)))
      (should= "Can't go that direction pick a new one" (second (:messages result)))))

  (it "handle move"
    (let [result (handle-move state-level1 "n")]
      (should= "HP: 10" (first (:messages result)))
      (should= "Location: 0 1" (second (:messages result)))
      (should= [0 1] (:room result)))
    (let [result (handle-move state-level2 "w")]
      (should= "Location: -1 1" (second (:messages result)))
      (should= [-1 1] (:room result))))

  (it "potion room"
    (let [result (potion-room state-level2 -1 0)]
      (should= "You found a potion!" (second (:messages result)))
      (should= 2 (:potion result))))

  (it "key room"
    (let [result (key-room state-level3 -1 0)]
      (should= "You found a key!" (second (:messages result)))
      (should= 1 (:potion result))))

  (it "next dungeon"
    (let [result (next-dungeon state-level1)]
      (should= ["HP: 10" "Next dungeon!"] (:messages result))
      (should= 2 (:level result))))

  (it "drop item movement"
    (let [result (drop-item state-level2)]
      (should= "HP: 10" (first (:messages result)))
      (should= "You dropped a potion." (second (:messages result)))
      (should= 0 (:potion result))))

  (it "drop item?"
    (should= true (drop-item? "drop"))
    (should= true (drop-item? "d"))
    (should= false (drop-item? "not")))

  (it "progresses to next dungeon?"
    (should= true (next-dungeon? [0 2] (read-string (slurp (io/resource (str "dnd/levels/1.edn"))))))
    (should= false (next-dungeon? [0 1] (read-string (slurp (io/resource (str "dnd/levels/1.edn")))))))

  (it "potion room?"
    (should= true (potion-room? [-1 0] (read-string (slurp (io/resource (str "dnd/levels/2.edn"))))))
    (should= false (potion-room? [0 1] (read-string (slurp (io/resource (str "dnd/levels/2.edn")))))))

  (it "key room?"
    (should= true (key-room? [-1 0] (read-string (slurp (io/resource (str "dnd/levels/3.edn"))))))
    (should= false (key-room? [0 1] (read-string (slurp (io/resource (str "dnd/levels/3.edn")))))))

  (it "kobold room?"
    (should= true (kobold-room? [-1 2] (read-string (slurp (io/resource (str "dnd/levels/2.edn"))))))
    (should= false (kobold-room? [0 1] (read-string (slurp (io/resource (str "dnd/levels/2.edn")))))))

  (it "room exists?"
    (should= true (level-includes? [-1 1] (read-string (slurp (io/resource (str "dnd/levels/2.edn"))))))
    (should= false (level-includes? [5 5] (read-string (slurp (io/resource (str "dnd/levels/2.edn")))))))

  (it "move deltas"
    (should= [1 0] (move-deltas "e"))
    (should= [-1 0] (move-deltas "w"))
    (should= [0 1] (move-deltas "n"))
    (should= [0 -1] (move-deltas "s")))

  (it "get user action"
    (with-in-str "n"
      (with-out-str (should= "n" (get-user-action state-level1)))))

  (it "get user action text"
    (should= ["Where would you like to move?"] (:messages (get-user-action-text state-level1))))

  (it "incorrect command text"
    (should= ["Incorrect command"] (:messages (incorrect-command-text state-level1))))

  (it "directions?"                                         ;; TODO - CRM: write better test
    (should= () (direction? state-level1)))

  (it "entering 'i' calls inventory function"
    (should= true (inventory? "i")))

  (it "inventory lists items correctly"
    (with-out-str (should-contain :potion (do-inventory state-level1))
                  (should= 0 (:potion state-level1))
                  (should= 0 (:key state-level1))
                  (should= ["HP: 10" "Inventory:\n0 Potions\n0 Keys"] (:messages (do-inventory state-level1)))))

  (it "save!"
    (should= {:key 0, :ac 12, :battle 1, :initiative :player, :level 1, :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 0], :potion 0} (core/save! state-level1)))

  (it "potion roll"
    (should-contain (core/dice-roll 12) #{1 2 3 4 5 6 7 8 9 10 11 12}))

  (it "Checks dice roll"
    (should-contain (core/dice-roll 3) #{1 2 3}))

  #_(it "double kobold"
      (should= true (kobold-room? [-1 2] (read-string (slurp (io/resource (str "dnd/levels/3.edn")))))))

  (it "2 kobolds"
    (should= true (two-kobolds? [-1 2] (read-string (slurp (io/resource (str "dnd/levels/3.edn")))))))

  (it "3 kobolds"
    (should= true (three-kobolds? [-1 1] (read-string (slurp (io/resource (str "dnd/levels/3.edn")))))))

  (it "2 trolls"
    (should= true (two-trolls? [-1 3] (read-string (slurp (io/resource (str "dnd/levels/3.edn")))))))

  (it "enemy stats"
    (should= {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4 :name "kobold"} kobold)
    (should= {:enemy-hp 84 :enemy-ac 15 :enemy-damage 29 :name "troll"} troll)
    (should= {:enemy-hp 10 :enemy-ac 12 :enemy-damage 8 :name "kobold pair"} kobold-pair)
    (should= {:enemy-hp 15 :enemy-ac 12 :enemy-damage 12 :name "kobold group of 3"} kobold-group-three)
    (should= {:enemy-hp 168 :enemy-ac 15 :enemy-damage 58 :name "troll pair"} troll-pair))

  )