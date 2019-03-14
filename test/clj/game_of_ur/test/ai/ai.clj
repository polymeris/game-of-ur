(ns game-of-ur.test.ai.ai
  (:require [clojure.test :refer [deftest testing is]]
            [game-of-ur.ai.minmax :as mm]
            [game-of-ur.ai.ai :as ai]
            [game-of-ur.game.board :as b]))

(def white-turn
  {:home   {:white 3, :black 5}
   :turn   :white
   :stones {[-2 -1] :white, [-3 -1] :black, [-2 0] :black}})

(def white-turn-2
  {:home   {:white 3, :black 0}
   :turn   :white
   :stones {[-2 -1] :white, [-3 -1] :black, [-2 0] :black}})

(deftest evaluate-board-fn
  (is (= -18 (mm/dumb-evaluation-fn white-turn)))
  (is (= 17  (mm/dumb-evaluation-fn white-turn-2))))

(def endgame-black-turn
  {:home   {:white 1 :black 0}
   :turn   :black
   :stones {[4 1] :black}})

(deftest endgame-evaluation
  (is (= :goal (:destination (mm/best-move mm/dumb-evaluation-fn 3 endgame-black-turn 2))))
  (is (= [3 1] (:destination (mm/best-move mm/dumb-evaluation-fn 3 endgame-black-turn 1)))))

(deftest simulation-ends-in-finished-game
  (is (-> (ai/simulate-game {:black-fn (fn [b r] (mm/best-move mm/dumb-evaluation-fn 1 b r))
                             :white-fn (fn [b r] (mm/best-move mm/dumb-evaluation-fn 1 b r))})
          (last)
          (first)
          (b/game-ended?))))
                            
