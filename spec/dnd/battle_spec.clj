(ns dnd.battle-spec
  (:require [speclj.core :refer :all]
            [dnd.battle :as battle]
            [dnd.core :as core]))

(def state-level1 {:room [0 0] :player "test" :hp 10 :ac 12 :damage 5 :potion 0 :level 1 :battle 1 :enemy-hp 0 :enemy-ac 0 :enemy-damage 0 :initiative :player})


(describe "battle"
  (it "Checks initiative roll for both rolling 20"
    (with-redefs [core/dice-roll (fn [_] 20)]
      (with-out-str (should= "first" (battle/initiative-roll)))))

  (it "start battle - player goes first"
    (with-redefs [battle/initiative-roll (fn [] "first")]
      (let [result (battle/start-battle state-level1 {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4 :name "kobold"})]
        (should= true (:battle? result))
        (should= 5 (:enemy-hp result))
        (should= 12 (:enemy-ac result))
        (should= 4 (:enemy-damage result))
        (should= :player (:initiative result))
        (should-not-contain :name result)
        (should-contain "You are about to fight a kobold," (:messages result)))))

  (it "start battle - mob goes first"
    (with-redefs [battle/initiative-roll (fn [] "second")]
      (let [result (battle/start-battle state-level1 {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4})]
        (should= true (:battle? result))
        (should= :enemy (:initiative result)))))
  )