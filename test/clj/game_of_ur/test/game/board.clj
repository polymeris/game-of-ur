(ns game-of-ur.test.game.board
  (:import  [clojure.lang ExceptionInfo])
  (:require [clojure.test :refer [deftest testing is]]
            [game-of-ur.game.board :as b]))

(def move-white-home-0 {:roll 0, :origin :home, :player :white})
(def move-black-home-0 {:roll 0, :origin :home, :player :black})

(def move-white-home-3 {:roll 3, :origin :home, :player :white})
(def move-white-0 {:roll 0, :origin [0   0], :player :white})
(def move-white-1 {:roll 1, :origin [-3 -1], :player :white})
(def move-white-2 {:roll 3, :origin [-2 -1], :player :white})

(def move-black-1 {:roll 1, :origin [-2 0], :player :black})
(def move-black-2 {:roll 2, :origin [-2 0], :player :black})

(def invalid-move-black-goal {:roll 1, :origin :goal, :player :black})

(def pass-move {:roll 3, :origin :pass, :player :black})
(def pass-move-0 {:roll 0, :origin :pass, :player :black})

(deftest move-destination
  (is (= [-3 0] (:destination (b/full-move move-white-1))))
  (is (= [-2 0] (:destination (b/full-move move-white-2))))
  (is (= [-1 0] (:destination (b/full-move move-black-1))))
  (is (thrown? ExceptionInfo (= :home (:destination (b/full-move invalid-move-black-goal))))))

(def white-turn
  {:home   {:white 3, :black 5}
   :turn   :white
   :stones {[-2 -1] :white, [-3 -1] :black, [-2 0] :black}})

(def black-turn-1
  {:home   {:white 4, :black 4}
   :turn   :black
   :stones {[-3 -1] :white, [3 0] :black, [-3 1] :white}})

(def black-turn-2
  {:home   {:white 3, :black 0},
   :turn   :black
   :stones {[-2 0] :black, [0 0] :black}})

(def black-turn-3
  {:home   {:white 3, :black 0},
   :turn   :black
   :stones {[-2 0] :black}})

(def no-valid-move-for-black
  {:home   {:white 0, :black 0},
   :turn   :black
   :stones {[-3 0] :black, [0 0] :white, [4, 1] :black}})


(deftest valid-move?
  (is (not (b/valid-move? b/initial-board (b/full-move move-white-home-0))))
  (is (b/valid-move? b/initial-board (b/full-move move-white-home-3)))
  (is (not (b/valid-move? b/initial-board (b/full-move move-black-home-0)))) ; white turn
  (is (not (b/valid-move? b/initial-board (b/full-move move-white-0)))) ; can't move when rolling 0
  (is (not (b/valid-move? b/initial-board (b/full-move move-white-1)))) ; no stone at [-3 -1] origin
  (is (not (b/valid-move? white-turn (b/full-move move-white-home-0))))
  (is (not (b/valid-move? white-turn (b/full-move move-white-1)))) ; stone at [-3 -1] is black
  (is (b/valid-move? white-turn (b/full-move move-white-2)))
  (is (not (b/valid-move? black-turn-1 (b/full-move move-black-home-0))))
  (is (not (b/valid-move? black-turn-1 (b/full-move move-white-1)))) ; black turn
  (is (not (b/valid-move? black-turn-1 (b/full-move move-black-1)))) ; no stone at [2 0] origin
  (is (not (b/valid-move? black-turn-2 (b/full-move move-black-home-0)))) ; no stone at home
  (is (b/valid-move? black-turn-2 (b/full-move move-black-1)))
  (is (not (b/valid-move? black-turn-2 (b/full-move move-black-2)))) ; there is already a black stone at [0 0]
  (is (b/valid-move? black-turn-3 (b/full-move move-black-2)))
  (is (not (b/valid-move? black-turn-1 (b/full-move pass-move)))) ; non of these
  (is (not (b/valid-move? black-turn-2 (b/full-move pass-move)))) ; are valid 
  (is (not (b/valid-move? black-turn-3 (b/full-move pass-move)))) ; passing positions
  (is (b/valid-move? no-valid-move-for-black (b/full-move pass-move)))) ; :pass is valid iff there are no other valid-moves

(defn clear-nil-stones [board]
  (when board (update board :stones #(into {} (filter second %)))))

(deftest child-board
  (is (= (clear-nil-stones (assoc b/initial-board :turn :black))
         (clear-nil-stones (b/child-board b/initial-board (b/full-move pass-move-0)))))
  (is (= {:home {:black 7, :white 6}, :turn :black, :stones {[-2 -1] :white}}
         (clear-nil-stones (b/child-board b/initial-board (b/full-move move-white-home-3)))))
  (is (= (clear-nil-stones (assoc white-turn :turn :black))
         (clear-nil-stones (b/child-board white-turn (b/full-move pass-move-0)))))
  (is (= {:home {:white 3, :black 6}, :turn :black, :stones {[-3 -1] :black, [-2 0] :white}} ; black captured
         (clear-nil-stones (b/child-board white-turn (b/full-move move-white-2)))))
  (is (= (clear-nil-stones (assoc black-turn-1 :turn :white))
         (clear-nil-stones (b/child-board black-turn-1 (b/full-move move-black-home-0)))))
  (is (= {:home {:white 3, :black 0}, :turn :white, :stones {[0 0] :black, [-1 0] :black}}
         (clear-nil-stones (b/child-board black-turn-2 (b/full-move move-black-1)))))
  (is (= {:home {:white 3, :black 0}, :turn :black, :stones {[0 0] :black}} ; lands on rosette and plays again
         (clear-nil-stones (b/child-board black-turn-3 (b/full-move move-black-2)))))
  (is (= {:home {:white 0 :black 0}, :turn :white, :stones {[-3 0] :black, [0 0] :white, [4, 1] :black}} ; If there is no possible valid move
         (b/child-board no-valid-move-for-black (b/full-move pass-move))))) ; should only change :turn
