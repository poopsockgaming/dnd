(ns dnd.ui-spec
  (:require [speclj.core :refer :all]
            [dnd.main :as main]
            [clojure.java.io :as io]
            [dnd.battle :as battle]
            [dnd.core :as core]
            [dnd.ui :as ui]))

(def hello-world {:messages ["hello world"]})
(describe "ui"

  (it "update function"
    (should= "hello world\n" (with-out-str (ui/update hello-world))))

  (it "gets user action"
    (with-in-str "n"
    (should= {:action "n"} (ui/get-user-action {:action "e"}))))

  (it "test"
    (should= "0\n" (with-out-str (ui/update {:messages ["0"]}))))




  )
