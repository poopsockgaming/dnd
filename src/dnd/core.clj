(ns dnd.core
  (:require [clojure.java.shell :as shell]))

(defn clear-text [text]
  (str "\033[0m" text))


(defn color-text [style-code text]
  (str "\u001b[" style-code "m" text "\u001b[0m"))

(defn grey-text [text] (color-text 90 text))
(defn red-text [text] (color-text 31 text))
(defn green-text [text] (color-text 32 text))
(defn blue-text [text] (color-text 36 text))
(defn yellow-text [text] (color-text 33 text))

(defn clear-terminal []
  (->> (shell/sh "/bin/sh" "-c" "clear <  /dev/null") :out (print "")))

(defn save! [state]
  (spit (:player state) state)
  state)

(defn dice-roll [sides]
  (inc (rand-int sides)))

(defn conjv [col value] (conj (vec col) value))

(defn add-message [state message] (update state :messages conjv message))

(defn inventory? [user-action] (or (= user-action "inventory") (= user-action "i")))

(defn do-inventory [state] (do (clear-terminal)
                               (add-message state (blue-text (str "Inventory:\n" (:potion state) " Potions\n" (:key state) " Keys")))))
