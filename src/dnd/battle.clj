(ns dnd.battle
  (:require [dnd.core :as core]))

(defn initiative-roll []
  (let [player-initiative-roll (core/dice-roll 20) enemy-initiative-roll (core/dice-roll 20)]
    (println "Your initiative roll was" player-initiative-roll "and the enemies roll was" enemy-initiative-roll)
    (cond
      (< enemy-initiative-roll player-initiative-roll) "first"
      (> enemy-initiative-roll player-initiative-roll) "second"
      :else "first")))

(defn start-battle [state mob]
  (let [state (-> (assoc state :battle? true)
                  (merge (dissoc mob :name))
                  (core/add-message (str "You are about to fight a " (:name mob) ",")))]
    (if (= (initiative-roll) "first")
      (let [state (assoc state :initiative :player)] state)
      (let [state (assoc state :initiative :enemy)] state))))