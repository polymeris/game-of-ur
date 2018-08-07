(ns game-of-ur.test.game.ai
  (:require [clojure.test :refer [deftest testing is]]
            [game-of-ur.game.ai :as ai]
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
  (is (= -18 (ai/dumb-evaluation-fn white-turn)))
  (is (= 17 (ai/dumb-evaluation-fn white-turn-2))))

(def endgame-black-turn
  {:home   {:white 1 :black 0}
   :turn   :black
   :stones {[4 1] :black}})

(deftest endgame-evaluation
  (is (= :goal (:destination (ai/best-move ai/dumb-evaluation-fn 3 endgame-black-turn 2))))
  (is (= [3 1] (:destination (ai/best-move ai/dumb-evaluation-fn 3 endgame-black-turn 1)))))

(deftest simulation-ends-in-finished-game
  (is (-> (ai/simulate-game ai/dumb-evaluation-fn ai/dumb-evaluation-fn 1)
          (last)
          (first)
          (b/game-ended?))))
                            
