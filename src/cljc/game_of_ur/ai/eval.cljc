(ns game-of-ur.ai.eval
  #?(:clj
      (:require
       [clojure.spec.alpha :as spec]
       [game-of-ur.game.board :as game])
      :cljs
      (:require
       [cljs.spec.alpha :as spec]
       [game-of-ur.game.board :as game])))

(defn- ev-wrap [win-score f]
  (fn [board]
    (spec/assert ::game/board board)
    (cond (game/player-won? board :black) win-score
          (game/player-won? board :white) (- win-score)
          :else                           (f board))))

;; Decided to call evaluation functions by jazz album's/song's.

(def dumb-evaluation-fn
  (ev-wrap 10e6
   #(- (+ (* 7 (get-in % [:goal :black])) (* 3 (game/stones-on-board % :black)))
       (+ (* 7 (get-in % [:goal :white])) (* 3 (game/stones-on-board % :white))))))

(defn- stones-value [{:keys [stones goal]} player f]
  (let [p (game/paths player)]
    (->> (map-indexed vector p)
         (keep (fn [[n x]] (when (= player (stones x)) (f n))))
         (reduce +)
         (+ (* (get goal player) (f (dec (count p))))))))

;; Wins about 70% of games against dumb-evaluation-fn
(def inner-urge
  (ev-wrap 10e6
   #(- (stones-value % :black (partial * 0.5))
       (stones-value % :white (partial * 0.5)))))
