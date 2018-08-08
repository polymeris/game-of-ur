(ns game-of-ur.ascii
  #?(:clj
     (:require
      [game-of-ur.game.board :as board]
      [game-of-ur.game.ai :as ai]
      [clojure.string :as cs])
     :cljs
     (:require
      [game-of-ur.game.board :as board]
      [game-of-ur.game.ai :as ai]
      [clojure.string :as cs])))

;; rectangle board coordinates
(def coords [[[-3 -1] [-2 -1] [-1 -1] [0 -1] [1 -1] [2 -1] [3 -1] [4 -1]]
             [[-3  0] [-2  0] [-1  0] [0  0] [1  0] [2  0] [3  0] [4  0]]
             [[-3  1] [-2  1] [-1  1] [0  1] [1  1] [2  1] [3  1] [4  1]]])

(defn get-rosettes
  "Assigns '[~]' to rosettes coordinates that do not have a stone on top"
  [stones]
  (->> board/rosettes
       (filter (comp nil? stones))
       (mapcat (fn [r] [r "[~]"]))
       (apply hash-map)))

(defn stones->string
  "It translates the :stones key of a board to a vector of strings"
  [stones]
  (let [key->string {:black "[o]" :white "[x]" nil "[ ]"}
        gaps        [[2 -1] [2 1] [1 -1] [1 1]]] ; Coords of the rectangle that aren't part of the board
    (as-> stones $
         (map (juxt first (comp key->string second)) $)
         (concat (map vector gaps (repeat "   ")) $)
         (into {} $)
         (reduce conj $ (get-rosettes stones))
         (map #(apply str (map $ %)) coords))))

(defn print-game-state
  "Prints a game-state given a board, player, roll, and destination.
   Example:

          -3 -2 -1  0  1  2  3  4 
        --------------------------
        | x                 xx
      -1| [~][ ][x][ ]      [~][ ]
       0| [x][x][ ][o][ ][ ][ ][ ]
       1| [o][ ][o][o]      [~][x]
        | o                 oo

      black rolled 2 from :home to [-1 1]
  "
  [{:keys [home stones] :as board} player roll origin destination]
  (let [in-goal    (board/stones-in-goal board)
        white-goal (apply str (concat (repeat (- 18 (:white home)) " ")
                                      (repeat (:white in-goal) \x)))
        black-goal (apply str (concat (repeat (- 18 (:black home)) " ")
                                      (repeat (:black in-goal) \o)))
        board-str  (stones->string stones)
        board-str' (map #(cons (format "%2d| " %1) %2) (range -1 2) board-str)]
    (do
      (println "\n    -3 -2 -1  0  1  2  3  4 ")
      (println "  --------------------------")
      (println (apply str (concat (cons "  | " (repeat (:white home) "x")) white-goal)))
      (doall (map (comp println (partial apply str)) board-str'))
      (println (apply str (concat (cons "  | " (repeat (:black home) "o")) black-goal)))
      (print (str "\n" (apply str (rest (str player))) " rolled " roll))
      (println (str " from " origin " to " destination "\n")))))

(defn graphic-simulation
  "Prints every step of an AI vs AI game simulation, until the game is ended.
   see: ai/simulate-game"
  [eval-fn-black eval-fn-white depth]
  (let [game (ai/simulate-game eval-fn-black eval-fn-white depth)
        show (fn [[board {:keys [origin destination player roll]}]]
               (print-game-state board player roll origin destination))]
    (doall (map show game))))

(defn player-move
  "Asks the player for a coordinate. It will check if the coordinates is a correct
   stone to move, but it will not check if the coordinate is correctly written, or if
   it's an unexpected value.  Expected values are 'home', 'pass', or a valid coordinate
   that is two numbers separated by spaces like '3 -1'. If the coordinate is out of
   range the specs will throw an error too"
  [board valid-moves roll]
  (do (println (str "\nyour roll is " roll ". Enter coordinates:"))
      (let [entry  (read-line)
            origin (cond (= entry "home") :home
                         (= entry "pass") :pass
                         :else            (mapv read-string (cs/split entry #" ")))]
        (if (contains? (board/valid-moves board roll)
              (board/full-move {:roll roll :origin origin :player (:turn board)}))
          (board/full-move {:roll roll :origin origin :player (:turn board)})
          (recur board valid-moves roll)))))

(defn game-loop
  "Given a starting board, a color chosen by the player and a depth for the AI,
   will simulate a game until the player gets bured and closes the repl, or until
   the game ends"
  [board evaluation-fn color depth]
  (loop [board board, player nil, roll nil, origin nil, destination nil]
    (do (print-game-state board player roll origin destination)
      (when-not (board/game-ended? board)
        (let [roll   (rand-nth [0 1 1 1 1 2 2 2 2 2 2 3 3 3 3 4])
              move   (if (= (:turn board) color)
                       (player-move board (board/valid-moves board roll) roll)
                       (ai/best-move evaluation-fn depth board roll))
              nboard (board/child-board board move)]
          (recur nboard (:turn board) roll (:origin move) (:destination move)))))))
