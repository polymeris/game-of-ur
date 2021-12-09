(ns game-of-ur.ai.ai
  #?(:clj
     (:require
      [clojure.spec.alpha :as spec]
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
  [fns]
  (letfn [(roll [] (rand-nth [0 1 1 1 1 2 2 2 2 2 2 3 3 3 3 4]))
          (decide-fn [board] (fns (:turn board)))
          (next-board [[board _]]
            (let [move ((decide-fn board) board (roll))]
              [(game/unsafe-child-board board move) move]))]
    (->> [game/initial-board nil]
         (iterate next-board)
         (take-until (comp game/game-ended? first)))))

(defn ranked-moves
  "Evaluates which is/are the best move/s for a given player, board, and roll. Given
  the criteria of `rank-fn`. We assume that the greater the number, the better the
  positions is for black."
  [rank-fn board roll]
  (map (fn [m] [m (rank-fn board m)])
       (game/valid-moves board roll)))

(defn best-moves
  [rank-fn board roll]
  (let [options    (ranked-moves rank-fn board roll)
        best-score (reduce (if (= :black (:turn board)) max min) (map second options))]
    (keep (fn [[move score]] (when (= score best-score) move))
          options)))

(defn best-move [rank-fn board roll]
  (when-not (game/game-ended? board)
    (rand-nth (best-moves rank-fn board roll))))
