(ns dnd.ui
(:require [clojure.java.io :as io]
  [clojure.java.shell :as shell]
  [dnd.battle :as battle]
  [dnd.core :as core]))

(defn update [state]
  (doseq [message (:messages state)] (println message)))

(defn get-user-action [state]
  (let [action (read-line)]
    (clojure.core/assoc state :action action)))