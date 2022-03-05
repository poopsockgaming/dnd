(ns dnd.core)

; loop
; asks for what to do
; if the roll is over the ac then hp - (roll for damage)
; if roll is less than ac then nothing happens
; if enemy roll over ac then roll for hp - (roll for damage)
; if roll is less than ac then nothing happens
; loop breaks when any hp is less than or equal to 0
; display winner

;(defn test-user-input []
;  (do (println "Hi there") (def x (read-line)))
;  (println "You said" x)
;  x)

(defn exit []
  (java.lang.System/exit 0))

(defn dice-roll [sides]
  (inc (rand-int sides)))

(defn dnd [hp ac]
  (loop [hp hp ac ac enemy-hp 10 enemy-ac 12]
    (println "Health:" hp)
    (println "Enemy Health:" enemy-hp)
    (if (or (<= hp 0) (<= enemy-hp 0))
      (cond
        (and (<= hp 0) (<= enemy-hp 0)) (do (println "You both died") (exit))
        (<= hp 0) (do (println "You died") (exit))
        (<= enemy-hp 0) (do (println "You won") (exit))
        )
      (do (println "What will you do? (attack or a)") (def x (read-line))))
    (if (or (= "attack" x) (= "a" x))
      (cond
        (and (< enemy-ac (dice-roll 20)) (< ac (dice-roll 20))) (recur (- hp (dice-roll 4)) ac (- enemy-hp (dice-roll 5)) enemy-ac)
        (< enemy-ac (dice-roll 20)) (recur hp ac (- enemy-hp (dice-roll 5)) enemy-ac)
        (< ac (dice-roll 20)) (recur (- hp (dice-roll 4)) ac enemy-hp enemy-ac)
        :else (recur hp ac enemy-hp enemy-ac)


        )
      (recur hp ac enemy-hp enemy-ac))
    )
  )