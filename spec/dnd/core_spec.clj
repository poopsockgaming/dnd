(ns dnd.core-spec
  (:require [speclj.core :refer :all]
            [dnd.core :as core]))
(describe "core"
(it "Checks dice roll"
  (should-contain (core/dice-roll 3) #{1 2 3}))

(it "conjv"
  (should= [1 2 3 4] (core/conjv [1 2 3] 4)))


(it "add message"
  (should= ["hello"] (:messages (core/add-message {} "hello"))))
)