(ns dnd.battle
  (:require [dnd.core :as core]))

(defn battle-status [state]
  ;(-> (core/add-message state (str (:player state)))
  (core/add-message state (core/color-text "42" (str "Your HP: " (:hp state) "          " "Enemy HP: " (:enemy-hp state) "\n"))))

(defn enter-to-continue []
  (println (core/yellow-text "Press enter to continue"))
  (let [continue (read-line)]
    (if (= "\n" continue)
      (core/clear-terminal)
      (core/clear-terminal))))

(defn potion-roll []
  (+ (core/dice-roll 4) (core/dice-roll 4) 4))

(defn enemy-colorful-attack [state word attack-roll enemy-roll]
  (core/clear-terminal)
  (-> (update state :hp - attack-roll)
      (battle-status)
      (core/add-message (str "Enemy rolled: " (core/blue-text enemy-roll)))
      (core/add-message (core/red-text (str "The enemy " word " you for " attack-roll " damage")))
      #_(core/add-event state [:damage attack-roll])
      ))

(defn enemy-attack [state]
  (let [enemy-roll (core/dice-roll 20)
        state (assoc state :initiative :player)
        enemy-damage (core/dice-roll (:enemy-damage state))]

    (cond
      (= enemy-roll 20) (let [attack-roll (* 2 enemy-damage)]
                          (cond
                            (or (= (* 2 attack-roll) 1) (= (* 2 attack-roll) 2)) (enemy-colorful-attack state "scratches" (* 2 attack-roll) enemy-roll)
                            (or (= (* 2 attack-roll) 3) (= (* 2 attack-roll) 4)) (enemy-colorful-attack state "bruises" (* 2 attack-roll) enemy-roll)
                            (or (= (* 2 attack-roll) 5) (= (* 2 attack-roll) 6)) (enemy-colorful-attack state "wounds" (* 2 attack-roll) enemy-roll)
                            (or (= (* 2 attack-roll) 7) (= (* 2 attack-roll) 8)) (enemy-colorful-attack state "pummels" (* 2 attack-roll) enemy-roll)
                            (or (= (* 2 attack-roll) 9) (= (* 2 attack-roll) 10)) (enemy-colorful-attack state "destroys" (* 2 attack-roll) enemy-roll)
                            :else (enemy-colorful-attack state "eviscerates" attack-roll enemy-roll)))
      (> enemy-roll (:ac state)) (cond
                                   (or (= enemy-damage 1) (= enemy-damage 2)) (enemy-colorful-attack state "scratches" enemy-damage enemy-roll)
                                   (or (= enemy-damage 3) (= enemy-damage 4)) (enemy-colorful-attack state "bruises" enemy-damage enemy-roll)
                                   (or (= enemy-damage 5) (= enemy-damage 6)) (enemy-colorful-attack state "wounds" enemy-damage enemy-roll)
                                   (or (= enemy-damage 7) (= enemy-damage 8)) (enemy-colorful-attack state "pummels" enemy-damage enemy-roll)
                                   (or (= enemy-damage 9) (= enemy-damage 10)) (enemy-colorful-attack state "destroys" enemy-damage enemy-roll)
                                   :else (enemy-colorful-attack state "eviscerates" enemy-damage enemy-roll))
      :else (-> #_(battle-status state)
                (core/add-message state (str "Enemy rolled: " (core/blue-text enemy-roll)))
                (core/add-message "Miss")))))
#_(cond
    (= enemy-roll 20) (-> (core/add-message state (core/red-text "Enemy critical hit!"))
                          (update :hp - (* 2 (:enemy-damage state))))
    (> enemy-roll (:ac state)) (-> (core/add-message state (core/red-text "Enemy hit!"))
                                   (update :hp - (:enemy-damage state)))
    :else (core/add-message state "Enemy miss!"))

(defn win-results [state]
  (core/clear-terminal)
  (-> #_(battle-status state)
      (core/add-message state (core/green-text "You won!"))
      (assoc :battle? false)))

(defn loss-results [state]
  (core/clear-terminal)
  (println (core/color-text "42" (str "Your HP: 0          Enemy HP: " (:enemy-hp state))))
  (println "You died!")
  (System/exit 0))


(defn player-attack? [player-action]
  (or (= player-action "attack") (= player-action "a")))

(defn player-take-potion? [player-action]
  (or (= player-action "quaff") (= player-action "q")))

(defn view-inventory? [player-action]
  (or (= player-action "inventory") (= player-action "i")))

(defn colorful-attack [state word attack-roll]
  (core/clear-terminal)
  (-> (update state :enemy-hp - attack-roll)
      #_(battle-status)
      (core/add-message (core/green-text (str "You " word " the enemy for " attack-roll " damage!")))
      #_(core/add-event state [:damage attack-roll])
      #_(core/add-message (core/yellow-text "Press enter to continue")))
  )

(defn player-attacks [{:keys [damage enemy-ac] :as state}]
  (let [attack-roll (core/dice-roll damage)
        ac-roll (core/dice-roll 20)]
    (cond
      (= ac-roll 20) (let [attack-roll (* 2 attack-roll)]
                       (cond
                         (or (= (* 2 attack-roll) 1) (= (* 2 attack-roll) 2)) (colorful-attack state "scratch" (* 2 attack-roll)) ;; TODO - CRM: have tests for all outcomes
                         (or (= (* 2 attack-roll) 3) (= (* 2 attack-roll) 4)) (colorful-attack state "bruise" (* 2 attack-roll))
                         (or (= (* 2 attack-roll) 5) (= (* 2 attack-roll) 6)) (colorful-attack state "wound" (* 2 attack-roll))
                         (or (= (* 2 attack-roll) 7) (= (* 2 attack-roll) 8)) (colorful-attack state "pummel" (* 2 attack-roll))
                         (or (= (* 2 attack-roll) 9) (= (* 2 attack-roll) 10)) (colorful-attack state "destroy" (* 2 attack-roll))
                         :else (colorful-attack state "eviscerate" attack-roll)))
      (> ac-roll enemy-ac) (cond
                             (or (= attack-roll 1) (= attack-roll 2)) (colorful-attack state "scratch" attack-roll)
                             (or (= attack-roll 3) (= attack-roll 4)) (colorful-attack state "bruise" attack-roll)
                             (or (= attack-roll 5) (= attack-roll 6)) (colorful-attack state "wound" attack-roll)
                             (or (= attack-roll 7) (= attack-roll 8)) (colorful-attack state "pummel" attack-roll)
                             (or (= attack-roll 9) (= attack-roll 10)) (colorful-attack state "destroy" attack-roll)
                             :else (colorful-attack state "eviscerate" attack-roll))
      :else (-> #_(battle-status state)
                (core/add-message state (str "Roll: " (core/blue-text (str ac-roll)) "\nMiss"))))))

(defn player-takes-potion [{:keys [potion] :as state}]
  (if (> potion 0)
    (-> (update state :hp + (potion-roll))
        (update :potion dec)
        #_(battle-status)
        (core/add-message (core/green-text "Taking health potion!")))
    (core/add-message state (core/red-text "No health potion to take"))))

(defn view-inventory [state]           ;; TODO - CRM: "Dont let enemy attack after viewing inventory"
  ;(core/clear-terminal)
  (-> #_(battle-status state)
      (core/add-message state (core/blue-text (str "Inventory:\n" (:potion state) " Potions\n" (:key state) " Keys")))))

(defn player-turn-text [state]
  ;(println (core/color-text "42" (str "Your HP: " (:hp state) "          " "Enemy HP: " (:enemy-hp state) "\n")))
  (println (core/yellow-text "What will you do? \nattack-(a)\ntake health potion-(q)\ninventory-(i)")))

(defn player-turn-during-battle [state]
  (player-turn-text state)
  (let [player-action (read-line)
        _ (core/clear-terminal)
        state (cond
                (player-attack? player-action) (player-attacks state)
                (player-take-potion? player-action) (player-takes-potion state)
                (view-inventory? player-action) (view-inventory state)
                :else (core/add-message state (core/red-text "SELECT A VALID OPTION.")))]
    (assoc state :initiative :enemy)))

(defn player-starts-battle [{:keys [hp enemy-hp] :as state}]
  (let [state (core/save! state)                            ;; TODO - CRM: Don't think I should be here
        #_state #_(battle-status state)]
    ;(enter-to-continue)
    (cond
      (<= enemy-hp 0) (win-results state)
      (<= hp 0) (loss-results state)
      (= :enemy (:initiative state)) (enemy-attack state)
      :else (player-turn-during-battle state #_(battle-status state)))))

(defn initiative-roll []
  (let [player-initiative-roll (core/dice-roll 20) enemy-initiative-roll (core/dice-roll 20)]
    (println (core/blue-text (str "Your initiative roll was " player-initiative-roll " and the enemies roll was " enemy-initiative-roll)))
    (cond
      (< enemy-initiative-roll player-initiative-roll) "first"
      (> enemy-initiative-roll player-initiative-roll) "second"
      :else "first")))

(defn start-battle [state mob]
  (let [state (-> (assoc state :battle? true)
                  (merge (dissoc mob :name))
                  #_(battle-status)
                  (core/add-message (str "You are about to fight a " (:name mob) ",")))]
    (if (= (initiative-roll) "first")
      (let [state (assoc state :initiative :player)] state)
      (let [state (assoc state :initiative :enemy)] state))))