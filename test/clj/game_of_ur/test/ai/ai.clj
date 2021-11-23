(ns game-of-ur.test.ai.ai
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.spec.alpha :as spec]
            [game-of-ur.ai.minmax :as mm]
            [game-of-ur.ai.eval :as ev]
            [game-of-ur.ai.ai :as ai]
            [game-of-ur.game.board :as b]))

(spec/check-asserts true)

(def white-turn
  {:home   {:white 3, :black 5}
   :goal   {:white (- (get-in b/initial-board [:home :black]) (+ 3 1)),
            :black (- (get-in b/initial-board [:home :black]) (+ 5 2))}
   :turn   :white
   :stones {[-2 -1] :white, [-3 -1] :black, [-2 0] :black}})

(def white-turn-2
  {:home   {:white 3, :black 0}
   :goal   {:white (- (get-in b/initial-board [:home :black]) (+ 3 1)),
            :black (- (get-in b/initial-board [:home :black]) (+ 0 2))}
   :turn   :white
   :stones {[-2 -1] :white, [-3 -1] :black, [-2 0] :black}})

(deftest evaluate-board-fn
  (is (= -18 (ev/dumb-evaluation-fn white-turn)))
  (is (= 17  (ev/dumb-evaluation-fn white-turn-2))))

(def endgame-black-turn
  {:home   {:white 1 :black 0}
   :goal   {:white (- (get-in b/initial-board [:home :black]) (+ 1 0)),
            :black (- (get-in b/initial-board [:home :black]) (+ 0 1))}
   :turn   :black
   :stones {[4 1] :black}})

(deftest endgame-evaluation
  (is (= :goal (:destination (mm/best-move ev/dumb-evaluation-fn 3 endgame-black-turn 2))))
  (is (= [3 1] (:destination (mm/best-move ev/dumb-evaluation-fn 3 endgame-black-turn 1)))))

(deftest simulation-ends-in-finished-game
  (is (-> (ai/simulate-game {:black (partial mm/best-move ev/dumb-evaluation-fn 1)
                             :white (partial mm/best-move ev/dumb-evaluation-fn 1)})
          (last)
          (first)
          (b/game-ended?))))
