(ns game-of-ur.ai.ai
  #?(:clj
     (:require
      [clojure.spec :as spec]
      [game-of-ur.game.board :as game])
     :cljs
     (:require
      [cljs.spec.alpha :as spec]
      [game-of-ur.game.board :as game])))

(defn take-until
  "Returns every element before the first element that returns true for pred,
   including the element itself. (take-until odd? '(2 4 6 8 9 10 12 14)) => '(2 4 6 8 9)"
  [pred coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (if (pred (first s))
        (list (first s))
        (cons (first s) (take-until pred (rest s)))))))

(defn simulate-game
  "Returns a lazy-sequence like '([board-1 move-1] [board-2 move-2] ... [board-n move-n])
   where board-n is a finished game. `black-fn` and `white-fn` should be functions
   that take a board and a roll and return a valid move."
  [{:keys [black-fn white-fn]}]
  (letfn [(roll [] (rand-nth [0 1 1 1 1 2 2 2 2 2 2 3 3 3 3 4]))
          (decide-fn [board] (if (= :white (:turn board))
                               white-fn
                               black-fn))
          (next-board [[board _]]
            (let [move ((decide-fn board) board (roll))]
              [(game/child-board board move) move]))]      
    (->> [game/initial-board nil]
         (iterate next-board)
         (take-until (comp game/game-ended? first)))))
