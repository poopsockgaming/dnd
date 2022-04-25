(ns dnd.core)

(defn dice-roll [sides]
  (inc (rand-int sides)))

(defn conjv [col value] (conj (vec col) value))

(defn add-message [state message] (update state :messages conjv message))
