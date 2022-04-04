(ns dnd.core-spec
  (:require [speclj.core :refer :all]
            [dnd.core :refer :all]))

(describe "MAP"

  (it "going in 1 direction until you hit a wall"
    (with-redefs [dice-roll (fn [_] 20)]
      (with-in-str "e\ne\ne\ne\ne\ne\ne\ne\ne\ne\ne"
        (should-contain "Can't go that direction pick a new one" (movement "caleb" [0 0] 10 0)))))

  )

(describe "ATTACK"
  (it "19 is a hit"
    (with-redefs [dice-roll (fn [_] 19)]
      (with-in-str "a"
        (should-contain "You rolled: 19 \nHit!" (with-out-str (player-starts-battle [1 1] "caleb" 0 10 100 1000 5 12 4))))))

  (it "20 is a critical hit"
    (with-redefs [dice-roll (fn [_] 20)]
      (with-in-str "a"
        (should-contain "You rolled: 20 \nCritical hit!" (with-out-str (player-starts-battle [1 1] "caleb" 0 10 100 1000 5 12 4))))))

  (it "enemy miss with impossible AC"
    (with-redefs [dice-roll (fn [_] 19)]
      (should-contain "Enemy miss!" (with-out-str (enemy-attack 1 100 1)))))

  (it "enemy hit with a 19"
    (with-redefs [dice-roll (fn [_] 19)]
      (should-contain "Enemy hit!" (with-out-str (enemy-attack 10 1 1)))))

  (it "enemy critical hit"
    (with-redefs [dice-roll (fn [_] 20)]
      (should-contain "Enemy rolled: 20 \nEnemy critical hit!" (with-out-str(enemy-attack 10 1 1)))))

  )

(describe "MISC"
  (it "Checks printed initiative roll"
    (with-redefs [dice-roll (fn [_] 20)]
      (should-contain "Your initiative roll was 20 and the enemies roll was 20\n" (with-out-str (initiative-roll)))))

  (it "Checks initiative roll for both rolling 20"
    (with-redefs [dice-roll (fn [_] 20)]
      (should= "first" (initiative-roll))))

  (it "turns strings to integers"
    (should= 10 (str-to-int "10")))

  (it "Checks dice roll"
    (should-contain (dice-roll 3) #{1 2 3}))

  (it "Checks that program prompts user for player name"
    (should-contain "Enter player (new) for new player: " (-main)))

  (it "Checks that when entering 'new' in the player selection it prompts for new profile name"
    (with-in-str "new"
      (should-contain "Enter new profile name:" (with-out-str (-main)))))
  )

(describe "Checks win/loss outcomes"
  (it "Checks kobold battle win (player start)"
    (with-redefs [dice-roll (fn [_] 20)]
      (with-in-str "a\na\n"
        (should-contain "You won!" (with-out-str (player-starts-battle [0 0] "caleb" 10 100 100 100 5 12 4))))))

  (it "Checks troll battle win (player start)"
    (with-redefs [dice-roll (fn [_] 20)]
      (with-in-str "a\na\na"
        (should-contain "You won!" (with-out-str (player-starts-battle [0 0] "caleb" 0 100 100 100 84 15 29))))))

  (it "Checks kobold battle loss (player start)"
    (with-redefs [dice-roll (fn [_] 19)]
      (with-in-str "a\n"
        (should-contain "You died!" (with-out-str (player-starts-battle [0 0] "caleb" 0 0 0 0 5 12 4))))))

  (it "Checks troll battle loss (player start)"
    (with-redefs [dice-roll (fn [_] 19)]
      (with-in-str "a\n"
        (should-contain "You died!" (with-out-str (player-starts-battle [0 0] "caleb" 0 0 0 0 84 15 29))))))

  (it "Checks kobold battle win (enemy start)"
    (with-redefs [dice-roll (fn [_] 20)]
      (with-in-str "a\na\n"
        (should-contain "You won!" (with-out-str (enemy-starts-battle [0 0] "caleb" 0 100 100 100 5 12 4))))))

  (it "Checks troll battle win (enemy start)"
    (with-redefs [dice-roll (fn [_] 20)]
      (with-in-str "a\na\na\na\na"
        (should-contain "You won!" (with-out-str (enemy-starts-battle [0 0] "caleb" 0 1000 100 100 84 15 29))))))

  (it "Checks kobold battle loss (enemy start)"
    (with-redefs [dice-roll (fn [_] 19)]
      (with-in-str "a\n"
        (should-contain "You died!" (with-out-str (enemy-starts-battle [0 0] "caleb" 0 0 0 0 5 12 4))))))

  (it "Checks troll battle loss (enemy start)"
    (with-redefs [dice-roll (fn [_] 19)]
      (with-in-str "a\n"
        (should-contain "You died!" (with-out-str (enemy-starts-battle [0 0] "caleb" 0 0 0 0 84 15 29))))))
  )
