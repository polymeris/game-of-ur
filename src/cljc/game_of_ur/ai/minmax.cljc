(ns game-of-ur.ai.minmax
  #?(:clj
     (:require
       [clojure.spec.alpha :as spec]
       [game-of-ur.game.board :as game])
     :cljs
     (:require
       [cljs.spec.alpha :as spec]
       [game-of-ur.game.board :as game])))


;;;;; BASIC MINIMAX

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

(defn- evaluate-board-branch
  "Recursively evaluates the provided board's score using the given function up to `depth` levels.
   Calls `expected-child-board-value` to determine the value of the best move for a given roll."
  [board-eval-fn depth board]
  (if (or (zero? depth) (game/game-ended? board))
    (board-eval-fn board)
    #(->> game/roll-probability
          (map (fn [[roll prob]] (* prob (expected-child-board-value board-eval-fn (dec depth) board roll))))
          (reduce +))))

(defn- expected-child-board-value
  "Recursively evaluates the value of the possible child boards for the given board and roll."
  [board-eval-fn depth board roll]
  (->> (game/valid-moves board roll)
       (map (fn [move] (trampoline evaluate-board-branch board-eval-fn depth
                                   (game/unsafe-child-board board move))))
       (reduce (if (= :black (:turn board)) max min))))

(defn minimax-rank-move [eval-fn depth board move]
  (trampoline evaluate-board-branch eval-fn depth
              (game/unsafe-child-board board move)))


;;;; MINMAX WITH ALPHA BETA PRUNING

;; This is semantically equivalent to minimax, but somewhat more efficient due to
;; pruning of the search tree. Thus, `search` and `prune` are the analogous to
;; `evaluate-board-branch` and `expected-child-board-balue`.
;; TODO: change names, find out if trampoline would make any difference.

(def +INF #?(:clj Double/POSITIVE_INFINITY :cljs js/Infinity))
(def -INF #?(:clj Double/NEGATIVE_INFINITY :cljs (- js/Infinity)))

(declare prune)

(defn- search [eval-fn board depth α β]
  (if (or (zero? depth) (game/game-ended? board))
    (eval-fn board)
    (-> (fn [k [roll p]]
          (as-> (game/valid-moves board roll) $
            (map (partial game/unsafe-child-board board) $)
            (prune eval-fn α β (dec depth) (= :black (:turn board)) $)
            (+ k (* p $))))
        (reduce 0 game/roll-probability))))

(defn- prune [eval-fn α β depth m? childs]
  (loop [[c & cs] childs, α α, β β, v (if m? -INF +INF)]
    (if (nil? c)
      v
      (let [v' ((if m? max min) v (search eval-fn c depth α β))
            α' (max α v')
            β' (min β v')]
        (if m?
          (if (>= α' β) v' (recur cs α' β v'))
          (if (>= α β') v' (recur cs α β' v')))))))

(defn alpha-beta-rank-move [eval-fn depth board move]
  (search eval-fn (game/unsafe-child-board board move) depth -INF +INF))
