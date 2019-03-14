(ns game-of-ur.ai.mcts
  (:require
    [game-of-ur.game.board :as b]))

(defn random-simulation [board]
  (if (b/game-ended? board)
    board
    (let [roll (reduce + (map (fn [_] (rand-int 2)) (range 4)))
          move (rand-nth (seq (b/valid-moves board roll)))]
      (recur (b/child-board board move)))))

(defn score-move
  "Given k in N-{0}, simulates k games and returns the ratio
   of winned games in perspective from the player to move"
  [{:keys [turn] :as board} move k]
  (->> (for [_ (range k)]
         (random-simulation board))
       (filter #(b/player-won? % turn))
       (count)
       (* (/ 1 k))))

(defn best-move
  "Does k random-simulations of games for every valid move,
   and then chosses the one that leads to more wins"
  [board roll k]
  (let [moves  (b/valid-moves board roll)
        scored (->> moves
                    (map (fn [m] [m (score-move board m k)]))
                    (group-by second))]
    (first (rand-nth (get scored (reduce max (map first scored)))))))
