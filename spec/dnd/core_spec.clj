(ns dnd.core-spec
  (:require [speclj.core :refer :all]
            [dnd.core :refer :all]
            [clojure.java.io :as io]))

(def state-level1 {:room [0 0] :player "test" :hp 10 :ac 12 :damage 5 :potion 0 :level 1 :battle 1 :enemy-hp 0 :enemy-ac 0 :enemy-damage 0})
(def state-level2 {:room [0 1] :player "test" :hp 10 :ac 12 :damage 5 :potion 1 :level 2 :battle 1 :enemy-hp 0 :enemy-ac 0 :enemy-damage 0})

(describe "MISC"
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

  (it "goes north once in level 1"
    (let [result (process-turn state-level1 "n")]
      (should= [0 1] (:room result))
      (should= "Location: 0 1" (first (:messages result)))))

  (it "goes west once in level 2"
    (let [result (process-turn state-level2 "w")]
      (should= [-1 1] (:room result))
      (should= "Location: -1 1" (first (:messages result)))))

  (it "tries to go wrong direction"
    (should= "Can't go that direction pick a new one\n" (with-out-str (move state-level1 "s"))))

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

  (it "Checks initiative roll for both rolling 20"
    (with-redefs [dice-roll (fn [_] 20)]
      (with-out-str (should= "first" (initiative-roll)))))

  (it "Checks dice roll"
    (should-contain (dice-roll 3) #{1 2 3}))

  (it "entering 'i' calls inventory function"
    (should= true (inventory? "i")))

  (it "inventory lists items correctly"
    (with-out-str (should-contain :potion (do-inventory state-level1))
                  (should= 0 (:potion state-level1))))

  (context "enemy-attack"

    (it "enemy miss with impossible AC"
      (with-redefs [dice-roll (fn [_] 19)]
        (let [state {:hp 10 :ac 100 :enemy-damage 1}
              result (enemy-attack2 state)]
          (should (map? result))
          (should-contain "Enemy rolled: 19" (:messages result))
          (should-contain "Enemy miss!" (:messages result))
          (should= 10 (:hp result))
          (should= :player (:initiative result)))))

    (it "enemy hit with a 19"
      (with-redefs [dice-roll (fn [_] 19)]
        (let [state {:hp 10 :ac 1 :enemy-damage 1}
              result (enemy-attack2 state)]
          (should (map? result))
          (should= 2 (count (:messages result)))
          (should-contain "Enemy rolled: 19" (:messages result))
          (should-contain "Enemy hit!" (:messages result))
          (should= 9 (:hp result)))))

    (it "enemy critical hit"
      (with-redefs [dice-roll (fn [_] 20)]
        (let [state {:hp 10 :ac 20 :enemy-damage 1}
              result (enemy-attack2 state)]
          (should (map? result))
          (should-contain "Enemy rolled: 20" (:messages result))
          (should-contain "Enemy critical hit!" (:messages result))
          (should= 8 (:hp result)))))
    )

  (it "start battle - player goes first"
    (with-redefs [initiative-roll (fn [] "first")]
      (let [result (start-battle state-level1 {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4 :name "kobold"})]
        (should= true (:battle? result))
        (should= 5 (:enemy-hp result))
        (should= 12 (:enemy-ac result))
        (should= 4 (:enemy-damage result))
        (should= :player (:initiative result))
        (should-not-contain :name result)
        (should-contain "You are about to fight a kobold," (:messages result)))))

  (it "start battle - mob goes first"
    (with-redefs [initiative-roll (fn [] "second")]
      (let [result (start-battle state-level1 {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4})]
        (should= true (:battle? result))
        (should= :enemy (:initiative result)))))
  )