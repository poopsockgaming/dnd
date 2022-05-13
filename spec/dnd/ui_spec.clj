(ns dnd.ui_spec
  (:require [speclj.core :refer :all]
            [dnd.main :as main]
            [clojure.java.io :as io]
            [dnd.battle :as battle]
            [dnd.core :as core]
            [dnd.ui :as ui]))

(def hello-world {:messages ["hello world"]})
(def state-level1 {:room [0 0] :player "test" :hp 10 :ac 12 :damage 5 :potion 0 :key 0 :level 1 :battle 1 :enemy-hp 0 :enemy-ac 0 :enemy-damage 0 :initiative :player :action ""})
(describe "ui"

  (it "update function"
    (should= "hello world\n"
                    (with-out-str (ui/update hello-world))))

  (it "gets user action"
    (with-in-str "n"
    (should= "n" (:action (ui/get-user-action state-level1)))))





  )