(ns dnd.battle-spec
  (:require [speclj.core :refer :all]
            [dnd.battle :as battle]
            [dnd.core :as core]))

(def state-level1 {:room [0 0] :player "test" :hp 10 :ac 0 :damage 5 :potion 0 :key 0 :level 1 :battle 1 :enemy-hp 0 :enemy-ac 0 :enemy-damage 0 :initiative :player})
(def state-level2 {:room [0 1] :player "test" :hp 10 :ac 12 :damage 5 :potion 1 :key 0 :level 2 :battle 1 :enemy-hp 0 :enemy-ac 0 :enemy-damage 0 :initiative :player})
(def state-level3 {:room [0 1] :player "test" :hp 10 :ac 12 :damage 20 :potion 1 :key 0 :level 2 :battle 1 :enemy-hp 10 :enemy-ac 0 :enemy-damage 0 :initiative :player})
(def state-level4 {:room [0 1] :player "test" :hp 10 :ac 0 :damage 20 :potion 1 :key 0 :level 2 :battle 1 :enemy-hp 10 :enemy-ac 0 :enemy-damage 20 :initiative :enemy})

(def state :undefined)

(describe "battle"

  (around [it] (with-redefs [core/clear-terminal (fn [])
                             core/color-text (fn [_ text] text)]
                 (it)))

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
        (should-not-contain (get-in state-level1 [:player :name]) result)
        (should-contain "You are about to fight a kobold," (:messages result)))))

  (it "start battle - mob goes first"
    (with-redefs [battle/initiative-roll (fn [] "second")]
      (let [result (battle/start-battle state-level1 {:enemy-hp 5 :enemy-ac 12 :enemy-damage 4})]
        (should= true (:battle? result))
        (should= :enemy (:initiative result)))))

  (it "player attacks?"
    (should= true (battle/player-attack? "a"))
    (should= true (battle/player-attack? "attack"))
    (should= false (battle/player-attack? "w")))

  (it "player takes potion?"
    (should= true (battle/player-take-potion? "q"))
    (should= true (battle/player-take-potion? "quaff"))
    (should= false (battle/player-take-potion? "w")))

  #_(it "player views inventory?"
      (should= true (battle/view-inventory? "i"))
      (should= true (battle/view-inventory? "inventory"))
      (should= false (battle/view-inventory? "w")))

  (it "battle status"
    (should= ["Your HP: 10          Enemy HP: 0\n"] (:messages (battle/battle-status state-level2))))

  ;(it "player turn text"
  ;  (should= {:ac 12, :battle 1, :initiative :player, :level 2, :messages ["What will you do? \nattack-(a)\ntake health potion-(t)\ninventory-(i)"], :damage 5, :enemy-hp 0, :enemy-damage 0, :player "test", :enemy-ac 0, :hp 10, :room [0 1], :potion 1} (player-turn-text state-level2)))

  (it "player turn during battle"
    (with-in-str "1"
      (should= ["SELECT A VALID OPTION."] (:messages (battle/player-turn-during-battle state-level2)))))

  (it "view inventory"
    (should= ["Inventory:\n1 Potions\n0 Keys"] (:messages (core/do-inventory state-level2))))

  (it "player attacks miss"
    (with-redefs [core/dice-roll (fn [_] -1)]
      (should= ["Roll: -1\nMiss"] (:messages (battle/player-attacks state-level2)))))

  (it "player attacks hits"
    (with-redefs [core/dice-roll (fn [_] 1)]
      (should= ["You scratch the enemy for 1 damage!"] (:messages (battle/player-attacks state-level3)))))

  (it "player attacks critical hit"
    (with-redefs [core/dice-roll (fn [_] 20)]
      (should= ["You eviscerate the enemy for 40 damage!"] (:messages (battle/player-attacks state-level3)))))

  (it "win results"
    (should= ["You won!"] (:messages (battle/win-results state-level1))))

  (it "enemy attacks miss"
    (with-redefs [core/dice-roll (fn [_] -1)]
      (should= ["Enemy rolled: -1" "Miss"] (:messages (battle/enemy-attack state-level2)))))

  (it "enemy attack hits"
    (with-redefs [core/dice-roll (fn [_] 19)]
      (should= ["Your HP: -9          Enemy HP: 0\n" "Enemy rolled: 19" "The enemy eviscerates you for 19 damage"] (:messages (battle/enemy-attack state-level2)))))

  (it "enemy attacks critical hit"
    (with-redefs [core/dice-roll (fn [_] 20)]
      (should= ["Your HP: -30          Enemy HP: 10\n" "Enemy rolled: 20" "The enemy eviscerates you for 40 damage"] (:messages (battle/enemy-attack state-level3)))))

  (context "Colorful attack"
    (it "1-2"
      (with-redefs [core/dice-roll (fn [_] 1)]
        (should= "You scratch the enemy for 1 damage!"
                 (first (:messages (battle/player-attacks state-level1))))))

    (it "3-4"
      (with-redefs [core/dice-roll (fn [_] 3)]
        (should= "You bruise the enemy for 3 damage!"
                 (first (:messages (battle/player-attacks state-level1))))))

    (it "5-6"
      (with-redefs [core/dice-roll (fn [_] 5)]
        (should= "You wound the enemy for 5 damage!"
                 (first (:messages (battle/player-attacks state-level1))))))

    (it "7-8"
      (with-redefs [core/dice-roll (fn [_] 7)]
        (should= "You pummel the enemy for 7 damage!"
                 (first (:messages (battle/player-attacks state-level1))))
        #_(should= [:damage 7]
                   (first (:events (battle/player-attacks state-level1))))
        ))

    (it "9-10"
      (with-redefs [core/dice-roll (fn [_] 9)]
        (should= "You destroy the enemy for 9 damage!"
                 (first (:messages (battle/player-attacks state-level1))))))

    (it "11+"
      (with-redefs [core/dice-roll (fn [_] 11)]
        (should= "You eviscerate the enemy for 11 damage!"
                 (first (:messages (battle/player-attacks state-level1))))))

    (it "player attacks critical hit"
      (with-redefs [core/dice-roll (fn [_] 20)]
        (should= "You eviscerate the enemy for 40 damage!" (first (:messages (battle/player-attacks state-level1))))))


    (it "colorful-attack changes enemy hp"
      (should= 5 (:enemy-hp (battle/colorful-attack state-level3 "wound" 5))))

    (it "enemy 1-2"
      (with-redefs [core/dice-roll (fn [_] 1)]
        (should= ["Your HP: 9          Enemy HP: 0\n" "Enemy rolled: 1" "The enemy scratches you for 1 damage"]
                 (:messages (battle/enemy-attack state-level1)))))

    (it "enemy 3-4"
      (with-redefs [core/dice-roll (fn [_] 3)]
        (should= ["Your HP: 7          Enemy HP: 0\n" "Enemy rolled: 3" "The enemy bruises you for 3 damage"]
                 (:messages (battle/enemy-attack state-level1)))))

    (it "enemy 5-6"
      (with-redefs [core/dice-roll (fn [_] 5)]
        (should= ["Your HP: 5          Enemy HP: 0\n" "Enemy rolled: 5" "The enemy wounds you for 5 damage"]
                 (:messages (battle/enemy-attack state-level1)))))

    (it "enemy 7-8"
      (with-redefs [core/dice-roll (fn [_] 7)]
        (should= ["Your HP: 3          Enemy HP: 0\n" "Enemy rolled: 7" "The enemy pummels you for 7 damage"]
                 (:messages (battle/enemy-attack state-level1)))
        #_(should= [:damage 7]
                   (first (:events (battle/player-attacks state-level1))))
        ))

    (it "enemy 9-10"
      (with-redefs [core/dice-roll (fn [_] 9)]
        (should= ["Your HP: 1          Enemy HP: 0\n" "Enemy rolled: 9" "The enemy destroys you for 9 damage"]
                 (:messages (battle/enemy-attack state-level1)))))

    (it "enemy 11+"
      (with-redefs [core/dice-roll (fn [_] 11)]
        (should= ["Your HP: -1          Enemy HP: 0\n" "Enemy rolled: 11" "The enemy eviscerates you for 11 damage"]
                 (:messages (battle/enemy-attack state-level1)))))

    )

  (context "initiative"

    (it "roll initiative for a creature"
      (let [result (battle/roll-initiative {:name "kobold"})]
        (should-contain :initiative result)
        (should< 0 (:initiative result))
        (should>= 20 (:initiative result))))

    (it "one mob"
      (let [state {:mobs   [{:name "kobold"}]
                   :player {:name "Sven"}}
            result (battle/roll-initiatives state)]
        (should-contain :initiative (get-in result [:mobs 0]))
        (should-contain :initiative (:player result))))

    (it "multiple mobs"
      (let [state {:mobs   [{:name "kobold"}
                            {:name "kobold"}]
                   :player {:name "Sven"}}
            result (battle/roll-initiatives state)]
        (should-contain :initiative (get-in result [:mobs 0]))
        (should-contain :initiative (get-in result [:mobs 1]))
        (should-contain :initiative (:player result))))

    (it "turn order"
      (let [state {:mobs   [{:name "kobold1" :initiative 1}
                            {:name "kobold2" :initiative 2}
                            {:name "kobold3" :initiative 3}]
                   :player {:name "Sven" :initiative 4}}
            result (battle/turn-order state)]
        (should= [{:name "Sven" :initiative 4}
                  {:name "kobold3" :initiative 3}
                  {:name "kobold2" :initiative 2}
                  {:name "kobold1" :initiative 1}]
                 result))
      )
    )

  (context "make-attack"

    (with state {:mobs [{:name "kobold" :ac 10 :hp 10 :damage 10}]
                 :player {:name "smurf" :ac 10 :hp 10 :damage 10}})

    (it "miss"
      (with-redefs [battle/attack-roll (fn [] 1)]
        (let [result (battle/make-attack @state [:mobs 0] [:player])]
          (should= 10 (get-in result [:player :hp])))))

    (it "hit"
      (with-redefs [battle/attack-roll (fn [] 11)
                    battle/damage-roll (fn [max] 5)]
        (let [result (battle/make-attack @state [:mobs 0] [:player])]
          (should= 5 (get-in result [:player :hp])))))

    (it "miss messages"
      (with-redefs [battle/attack-roll (fn [] 1)]
        (let [result (battle/make-attack @state [:mobs 0] [:player])]
          (should-contain (str "kobold misses " (core/blue-text "1"))
                          (:messages result)))))

    (it "hit player message"
      (with-redefs [battle/attack-roll (fn [] 11)
                    battle/damage-roll (fn [max] 5)]
        (let [result (battle/make-attack @state [:mobs 0] [:player])]
          (should-contain (core/red-text "kobold wounds smurf for 5 damage")
                          (:messages result)))))

    (it "hit mob message"
      (with-redefs [battle/attack-roll (fn [] 11)
                    battle/damage-roll (fn [max] 5)]
        (let [result (battle/make-attack @state [:player] [:mobs 0])]
          (should-contain (core/green-text "smurf wounds kobold for 5 damage")
                          (:messages result)))))

    (it "damage adjectives"
      (should= "scratches" (battle/damage-adjective 1))
      (should= "scratches" (battle/damage-adjective 2))
      (should= "bruises" (battle/damage-adjective 3))
      (should= "bruises" (battle/damage-adjective 4))
      (should= "wounds" (battle/damage-adjective 5))
      (should= "wounds" (battle/damage-adjective 6))
      (should= "pummels" (battle/damage-adjective 7))
      (should= "pummels" (battle/damage-adjective 8))
      (should= "destroys" (battle/damage-adjective 9))
      (should= "destroys" (battle/damage-adjective 10))
      (should= "eviscerates" (battle/damage-adjective 11)))
    )

  (it "both sides attack"
    (with-redefs [core/dice-roll (fn [_] 1)]
      (should= "You scratch the enemy for 1 damage!" (first (:messages (battle/attack state-level3))))
      (should= "Enemy rolled: 1" (second (:messages (battle/attack state-level4))))))

  #_(context "enemy-attack"

      (it "enemy miss with impossible AC"
        (with-redefs [core/dice-roll (fn [_] 19)]
          (let [state {:hp 10 :ac 100 :enemy-damage 1}
                result (battle/enemy-attack state)]
            (should (map? result))
            (should-contain "Enemy rolled: 19" (:messages result))
            (should-contain ["Enemy rolled: 19" "Miss"] (:messages result))
            (should= 10 (:hp result))
            (should= :player (:initiative result)))))

      (it "enemy hit with a 19"
        (with-redefs [core/dice-roll (fn [_] 19)]
          (let [state {:hp 10 :ac 1 :enemy-damage 1}
                result (battle/enemy-attack state)]
            (should (map? result))
            (should= 2 (count (:messages result)))
            (should-contain "Enemy rolled: 19" (:messages result))
            (should-contain ["Enemy rolled: 19" "The enemy eviscerates you for 19 damage"] (:messages result))
            (should= 9 (:hp result)))))

      (it "enemy critical hit"
        (with-redefs [core/dice-roll (fn [_] 20)]
          (let [state {:hp 10 :ac 20 :enemy-damage 1}
                result (battle/enemy-attack state)]
            (should (map? result))
            (should-contain "Enemy rolled: 20" (:messages result))
            (should-contain ["Enemy rolled: 20" "The enemy eviscerates you for 40 damage"] (:messages result))
            (should= 8 (:hp result)))))
      )
  )