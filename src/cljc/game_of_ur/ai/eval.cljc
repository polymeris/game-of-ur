(ns game-of-ur.ai.eval
  #?(:clj
      (:require
       [clojure.spec.alpha :as spec]
       [game-of-ur.game.board :as game])
      :cljs
      (:require
       [cljs.spec.alpha :as spec]
       [game-of-ur.game.board :as game])))

(defn dumb-evaluation-fn
  "sample board evaluation function"
  [board]
  (spec/assert ::game/board board)
  (if (game/player-won? board :black)
    10e6
    (- (+ (* 7 (get-in board [:goal :black])) (* 3 (game/stones-on-board board :black)))
       (+ (* 7 (get-in board [:goal :white])) (* 3 (game/stones-on-board board :white))))))
