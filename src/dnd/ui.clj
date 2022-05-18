(ns dnd.ui
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [dnd.battle :as battle]
            [dnd.core :as core]))

(defn update [state]                                        ;; TODO - CRM: border with if statement
  (doseq [message (:messages state)] (println message)))

(defn get-user-action [state]
  (let [action (read-line)]
    (assoc state :action action)))