(ns game-of-ur.game.ai
  #?(:clj
     (:require
       [clojure.spec :as spec]
       [game-of-ur.game.board :as game])
     :cljs
     (:require
       [cljs.spec.alpha :as spec]
       [game-of-ur.game.board :as game])))

(def roll-probability
  {0 0.0625
   1 0.25
   2 0.375
   3 0.25
   4 0.0625})

(defn dumb-evaluation-fn
  "sample board evaluation function"
  [board]
  (spec/assert ::game/board board)
  (if (game/player-won? board :black)
    10e6
    (- (+ (* 7 (:black (game/stones-in-goal board))) (* 3 (game/stones-on-board board :black)))
       (+ (* 7 (:white (game/stones-in-goal board))) (* 3 (game/stones-on-board board :white))))))


;; The following functions build a board search tree
;; The tree's root is the current board and branches into possible rolls, weighted by probability, these branches,
;; in turn branch into possible child boards for that roll.
;; I.e. the tree is layered into "board" nodes and "roll" nodes, for example, with depth=1:
;;
;;            board1                                     <- current board
;; .----------.----------.----------.----------.
;; roll0     roll1      roll2      roll3    roll4        <- rolls, weighted
;; .----.    .----.    .---.---.     .       .----.
;; B2   B3   B4   B5  B6  B7  B8    B9      B10  B11     <- child boards
;;
;; Each leaf child is evaluated with the provided function, and only the best one is considered.
;; The best boards scores for every board are added.

(declare expected-child-board-value)

(defn evaluate-board-branch
  "Recursively evaluates the provided board's score using the given function up to `depth` levels.
   Calls `expected-child-board-value` to determine the value of the best move for a given roll."
  [board-eval-fn depth board]
  (if (or (game/game-ended? board) (zero? depth))
    (board-eval-fn board)
    (->> roll-probability
         (map (fn [[roll prob]] (* prob (expected-child-board-value board-eval-fn (dec depth) board roll))))
         (reduce +))))

(defn expected-child-board-value
  "Recursively evaluates the value of the possible child boards for the given board and roll."
  [board-eval-fn depth board roll]
  (->> (game/valid-moves board roll)
       (map (comp (partial evaluate-board-branch board-eval-fn depth)
                  (partial game/child-board board)))
       (reduce max)))

(defn best-move
  "Using the provided function evaluates the board's possible child boards, and returns the best move.
   Calls the mutually recursive `evaluate-board-branch` and `expected-child-board-value`"
  [board-eval-fn depth board roll]
  (let [eval-fn (comp (if (= (:turn board) :white) - +) board-eval-fn)]
    (->> (game/valid-moves board roll)
         (map (comp (fn [[child-board move]] [(evaluate-board-branch eval-fn depth child-board) move])
                    (fn [move] [(game/child-board board move) move])))
         (reduce (partial max-key first))
         (second))))

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
   where board-n is a finished game. It uses best move and an evaluation function to decide
   the moves."
  [eval-fn-black eval-fn-white depth]
  (letfn [(roll [] (rand-nth [0 1 1 1 1 2 2 2 2 2 2 3 3 3 3 4]))
          (decide-fn [board] (if (= :white (:turn board))
                               eval-fn-white
                               eval-fn-black))
          (next-board [[board _]]
            (let [move (best-move (decide-fn board) depth board (roll))]
              [(game/child-board board move) move]))]      
    (->> [game/initial-board nil]
         (iterate next-board)
         (take-until (comp game/game-ended? first)))))
