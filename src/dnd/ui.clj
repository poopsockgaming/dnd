(ns dnd.ui
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [dnd.battle :as battle]
            [dnd.core :as core]))

(defn level [state]
  (read-string (slurp (io/resource (str "dnd/levels/" (:level state) ".edn")))))

(defn level-includes? [room level]
  (clojure.string/includes? level (str room)))

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

(defn delete-messages [state]
  (let [state (dissoc state :messages)]
    state))

(defn update [state]
  (core/clear-terminal)
  (if (:battle? state)
    (println (core/color-text "42" (str "Your HP: " (:hp state) "          " "Enemy HP: " (:enemy-hp state) "\n")))
    (do (println (core/color-text "1;42" (str "HP: " (:hp state))))
        (direction? state)))
  (doseq [message (:messages state)] (println message)))

(defn get-user-action [state]
  (let [action (read-line)]
    (assoc state :action action)))