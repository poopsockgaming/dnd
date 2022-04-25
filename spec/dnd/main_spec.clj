(ns dnd.core-spec
  (:require [speclj.core :refer :all]
            [dnd.main :refer :all]
            [clojure.java.io :as io]
            [dnd.battle :as battle]
            [dnd.core :as core]))

(def state-level1 {:room [0 0] :player "test" :hp 10 :ac 12 :damage 5 :potion 0 :level 1 :battle 1 :enemy-hp 0 :enemy-ac 0 :enemy-damage 0 :initiative :player})
(def state-level2 {:room [0 1] :player "test" :hp 10 :ac 12 :damage 5 :potion 1 :level 2 :battle 1 :enemy-hp 0 :enemy-ac 0 :enemy-damage 0 :initiative :player})
(def state-level3 {:room [0 1] :player "test" :hp 10 :ac 12 :damage 0 :potion 1 :level 2 :battle 1 :enemy-hp 10 :enemy-ac 0 :enemy-damage 0 :initiative :player})
(describe "MISC"

  (around [it] (with-redefs [clear-terminal (fn [])] (it)))

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
        (should= {:room [0 0] :player "test" :hp 10 :ac 12 :damage 5 :potion 0 :level 1 :battle? false :enemy-hp 0 :enemy-ac 0 :enemy-damage 0}
                 (new-profile)))))

  (it "level selection"
    (should= 1 (:level (level-selection state-level1))))

  (it "goes north once in level 1"
    (let [result (process-turn state-level1 "n")]
      (should= [0 1] (:room result))
      (should= "Location: 0 1" (first (:messages result)))))

  (it "goes west once in level 2"
    (let [result (process-turn state-level2 "w")]
      (should= [-1 1] (:room result))
      (should= "Location: -1 1" (first (:messages result)))))

  (it "tries to go wrong direction"
    (let [result (process-turn state-level1 "s")]
      (should= [0 0] (:room result))
      (should= "\nCan't go that direction pick a new one\n" (first (:messages result)))))

  (it "handle move"
    (let [result (handle-move state-level1 "n")]
      (should= ["Location: 0 1"] (:messages result))
      (should= [0 1] (:room result)))
    (let [result (handle-move state-level2 "w")]
      (should= ["Location: -1 1"] (:messages result))
      (should= [-1 1] (:room result))))

  (it "conjv"
    (should= [1 2 3 4] (conjv [1 2 3] 4)))

  (it "potion room"
    (let [result (potion-room state-level2 -1 0)]
      (should= ["You found a potion!"] (:messages result))
      (should= 2 (:potion result))))

  (it "next dungeon"
    (let [result (next-dungeon state-level1)]
      (should= ["Next dungeon!"] (:messages result))
      (should= 2 (:level result))))

  (it "drop item movement"
    (let [result (drop-item state-level2)]
      (should= ["You dropped a potion."] (:messages result))
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
    (should= {:ac 12, :battle 1, :initiative :player, :level 1, :messages ["Where would you like to move?"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 0], :potion 0} (get-user-action-text state-level1)))

  (it "incorrect command text"
    (should= {:ac 12, :battle 1, :initiative :player, :level 1, :messages ["Incorrect command"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 0], :potion 0} (incorrect-command-text state-level1)))

  (it "directions?"                                         ;; TODO - CRM: write better test
    (should= () (direction? state-level1)))

  (it "player attacks?"
    (should= true (player-attack? "a"))
    (should= true (player-attack? "attack"))
    (should= false (player-attack? "w")))

  (it "player takes potion?"
    (should= true (player-take-potion? "q"))
    (should= true (player-take-potion? "quaff"))
    (should= false (player-take-potion? "w")))

  (it "player views inventory?"
    (should= true (view-inventory? "i"))
    (should= true (view-inventory? "inventory"))
    (should= false (view-inventory? "w")))

  (it "Checks dice roll"
    (should-contain (dice-roll 3) #{1 2 3}))

  (it "entering 'i' calls inventory function"
    (should= true (inventory? "i")))

  (it "inventory lists items correctly"
    (with-out-str (should-contain :potion (do-inventory state-level1))
                  (should= 0 (:potion state-level1))))

  (context "enemy-attack"

    (it "enemy miss with impossible AC"
      (with-redefs [core/dice-roll (fn [_] 19)]
        (let [state {:hp 10 :ac 100 :enemy-damage 1}
              result (enemy-attack2 state)]
          (should (map? result))
          (should-contain "Enemy rolled: 19" (:messages result))
          (should-contain "Enemy miss!" (:messages result))
          (should= 10 (:hp result))
          (should= :player (:initiative result)))))

    (it "enemy hit with a 19"
      (with-redefs [core/dice-roll (fn [_] 19)]
        (let [state {:hp 10 :ac 1 :enemy-damage 1}
              result (enemy-attack2 state)]
          (should (map? result))
          (should= 2 (count (:messages result)))
          (should-contain "Enemy rolled: 19" (:messages result))
          (should-contain "Enemy hit!" (:messages result))
          (should= 9 (:hp result)))))

    (it "enemy critical hit"
      (with-redefs [core/dice-roll (fn [_] 20)]
        (let [state {:hp 10 :ac 20 :enemy-damage 1}
              result (enemy-attack2 state)]
          (should (map? result))
          (should-contain "Enemy rolled: 20" (:messages result))
          (should-contain "Enemy critical hit!" (:messages result))
          (should= 8 (:hp result)))))
    )

  (it "start battle - player goes first"
    (with-redefs [battle/initiative-roll (fn [] "first")]
      (let [result (start-battle state-level1 {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4 :name "kobold"})]
        (should= true (:battle? result))
        (should= 5 (:enemy-hp result))
        (should= 12 (:enemy-ac result))
        (should= 4 (:enemy-damage result))
        (should= :player (:initiative result))
        (should-not-contain :name result)
        (should-contain "You are about to fight a kobold," (:messages result)))))

  (it "start battle - mob goes first"
    (with-redefs [battle/initiative-roll (fn [] "second")]
      (let [result (start-battle state-level1 {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4})]
        (should= true (:battle? result))
        (should= :enemy (:initiative result)))))

  (it "save!"
    (should= {:ac 12, :battle 1, :initiative :player, :level 1, :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 0], :potion 0} (save! state-level1)))

  (it "battle status"
    (should= {:ac 12, :battle 1, :initiative :player, :level 2, :messages ["test" "Your HP: 10          Enemy HP: 0\n"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (battle-status state-level2)))

  ;(it "player turn text"
  ;  (should= {:ac 12, :battle 1, :initiative :player, :level 2, :messages ["What will you do? \nattack-(a)\ntake health potion-(t)\ninventory-(i)"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (player-turn-text state-level2)))

  (it "player turn during battle"
    (with-in-str "1"
      (should= {:ac 12, :battle 1, :initiative :enemy, :level 2, :messages ["SELECT A VALID OPTION."], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (player-turn-during-battle state-level2))))

  (it "view inventory"
    (should= {:ac 12, :battle 1, :initiative :player, :level 2, :messages ["Inventory:\n1 Potions"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (view-inventory state-level2)))

  (it "player attacks miss"
    (with-redefs [core/dice-roll (fn [_] -1)]
      (should= {:ac 12, :battle 1, :initiative :player, :level 2, :messages ["Miss"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (player-attacks state-level2))))

  (it "player attacks hits"
    (with-redefs [core/dice-roll (fn [_] 1)]
      (should= {:ac 12, :battle 1, :initiative :player, :level 2, :messages ["Hit!"], :damage 0, :enemy-hp 9, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (player-attacks state-level3))))

  (it "player attacks critical hit"
    (with-redefs [core/dice-roll (fn [_] 20)]
      (should= {:ac 12, :battle 1, :initiative :player, :level 2, :messages ["Hit!"], :damage 0, :enemy-hp -10, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (player-attacks state-level3))))

  (it "win results"
    (should= {:ac 12, :battle 1, :battle? false, :initiative :player, :level 1, :messages ["You won!"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 0], :potion 0} (win-results state-level1)))

  (it "enemy attacks miss"
    (with-redefs [core/dice-roll (fn [_] -1)]
      (should= {:ac 12, :battle 1, :initiative :player, :level 2, :messages ["Enemy rolled: -1" "Enemy miss!"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (enemy-attack2 state-level2))))

  (it "enemy attack hits"
    (with-redefs [core/dice-roll (fn [_] 19)]
      (should= {:ac 12, :battle 1, :initiative :player, :level 2, :messages ["Enemy rolled: 19" "Enemy hit!"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (enemy-attack2 state-level2))))

  (it "enemy attacks critical hit"
    (with-redefs [core/dice-roll (fn [_] 20)]
      (should= {:ac 12, :battle 1, :initiative :player, :level 2, :messages ["Enemy rolled: 20" "Enemy critical hit!"], :damage 0, :enemy-hp 10, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (enemy-attack2 state-level3))))

  (it "add message"
    (should= {:ac 12, :battle 1, :initiative :player, :level 1, :messages ["hello"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 0], :potion 0} (add-message state-level1 "hello")))

  (it "potion roll"
    (should-contain (core/dice-roll 12) #{1 2 3 4 5 6 7 8 9 10 11 12}))

  (it "Checks dice roll"
    (should-contain (core/dice-roll 3) #{1 2 3}))
  )