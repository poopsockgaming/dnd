(ns dnd.core-spec
  (:require [speclj.core :refer :all]
            [dnd.core :refer :all]))

(describe "dnd simulator"
  ;(it "test"
  ;  (should= "hello" (test-user-input)))
  (it "dnd simulator"
    (should= "You won" (dnd 10 10)))
  )
