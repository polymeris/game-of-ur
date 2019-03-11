(ns game-of-ur.game.board
  #?(:clj
     (:require
       [clojure.spec :as spec]
       [clojure.set :as set])
     :cljs
     (:require
       [cljs.spec.alpha :as spec]
       [clojure.set :as set])))

(spec/check-asserts true)

(def paths
  {:white [:home
           [0 -1] [-1 -1] [-2 -1] [-3 -1]
           [-3 0] [-2 0] [-1 0] [0 0] [1 0] [2 0] [3 0]
           [3 1] [4 1] [4 0] [4 -1] [3 -1]
           :goal]
   :black [:home
           [0 1] [-1 1] [-2 1] [-3 1]
           [-3 0] [-2 0] [-1 0] [0 0] [1 0] [2 0] [3 0]
           [3 -1] [4 -1] [4 0] [4 1] [3 1]
           :goal]})

(def rosettes
  #{[-3 -1] [-3 1] [0 0] [3 -1] [3 1]})

(defn valid-origins [player]
  (-> (set (paths player))
      (disj :goal)))

(def valid-positions
  (-> (set/union (set (:white paths)) (set (:black paths)))
      (disj :home :goal)))

(def initial-board
  {:home   {:black 7, :white 7}
   :turn   :white
   :stones (into {} (map (fn [k] [k nil]) valid-positions))})

(def opponent
  {:white :black
   :black :white})

(def stone-colors #{:white :black})
(spec/def ::position (spec/with-gen #(contains? valid-positions %)
                                    #(spec/gen valid-positions)))
(spec/def ::home-position (spec/with-gen #(= :home %)
                                         #(spec/gen #{:home})))
(spec/def ::goal-position (spec/with-gen #(= :goal %)
                                         #(spec/gen #{:goal})))
(spec/def ::stone stone-colors)
(spec/def ::stones (spec/map-of ::position (spec/nilable ::stone)))
(spec/def ::player stone-colors)
(spec/def ::turn stone-colors)
(spec/def ::white (spec/and int? #(<= 0 % 7)))
(spec/def ::black (spec/and int? #(<= 0 % 7)))
(spec/def ::roll (spec/and int? #(<= 0 % 4)))
(spec/def ::pass-position (spec/with-gen #(= :pass %)
                                         #(spec/gen #{:pass})))

(spec/def ::origin (spec/or :h ::home-position :p ::position :pass ::pass-position))
(spec/def ::destination (spec/or :h ::home-position :p ::position :g ::goal-position
                                 :pass ::pass-position))
(spec/def ::home (spec/keys :req-un [::white ::black]))
(spec/def ::board (spec/keys :req-un [::home ::turn ::stones]))
(spec/def ::move (spec/keys :req-un [::roll ::origin]))
(spec/def ::full-move (spec/keys :req-un [::roll ::origin ::destination ::player]))

(defn stones-in-play
  "Counts the amount of stones that aren't
   in :goal, of a given player and a given board."
  [{:keys [home stones] :as board} player]
  (spec/assert ::board board)
  (spec/assert ::player player)
  (->> (vals stones)
       (filter #(= % player))
       (count)
       (+ (get home player))))

(defn stones-in-goal
  "Returns number of stones in goal for the given board and player"
  [board]
  (spec/assert ::board board)
  {:white (- (get-in initial-board [:home :white])
             (stones-in-play board :white))
   :black (- (get-in initial-board [:home :black])
             (stones-in-play board :black))})

(defn stones-on-board
  "Return the number of stones not in goal or home for the given player"
  [{:keys [stones]} player]
  (spec/assert ::stones stones)
  (spec/assert ::player player)
  (->> stones
       (vals)
       (filter #(= % player))
       (count)))

(defn player-won?
  "Returns true iff the given player
   has no stones left to play."
  [board player]
  (spec/assert ::board board)
  (spec/assert ::player player)
  (= 0 (stones-in-play board player)))

(defn game-ended?
  "Returns true iff either white or black wins"
  [board]
  (spec/assert ::board board)
  (or (player-won? board :white)
      (player-won? board :black)))

(defn pass-move [roll turn]
  {:origin :pass, :roll roll, :destination :pass, :player turn})

(defn full-move
  "Takes a roll and an origin coordinate, and adds the
   destination coordinate of the move"
  [{:keys [roll player origin] :as move}]
  (spec/assert ::move move)
  (let [path (get paths player)
        destination (if (or (zero? roll) (= :pass origin))
                      :pass
                      (get path (+ roll (.indexOf path origin))))]
    (assoc move :destination destination)))

(defn- valid-non-pass-move?
  "Should return true iff the given full-move is
   valid in the current board and state, which will
   only happen if:
     - If the move is taking a stone from home, home cannot be empty
     - If the origin is in the board, there must be a right colored stone on it
     - Either the destination is empty, or there is an oppnent stone on it and
       the square is not a member of rosettes."
  [{:keys [home turn stones] :as board}
   {:keys [player origin destination roll] :as move}]
  (spec/assert ::board board)
  (spec/assert ::move move)
  (let [in-destination (get stones destination)]
    (and destination
         (not= 0 roll)
         (= turn player)
         (or (not= origin :home) (> (home player) 0))
         (or (= origin :home) (= player (get stones origin)))
         (or (nil? in-destination) (= (opponent turn) in-destination))
         (or (nil? in-destination) (not (rosettes destination))))))

(declare valid-moves)

(defn valid-move?
  "Same that valid-non-pass-move?
   but taking in count the :pass option"
  [board {:keys [roll] :as move}]
  (spec/assert ::move move)
  (spec/assert ::board board)
  (contains? (valid-moves board roll) move))

(defn move-stone
  "- If the origin is in the board must be replaced with nil.
   - If the destination content is an opponent stone, the amount of stones
     in opponent's :home must be incremented.
   - If the origin is home, the amount of stones in :home decreases.
   - If the destination is not :goal, the stone must be placed on it."
  [{:keys [stones] :as board}
   {:keys [player origin destination] :as move}]
  (spec/assert ::board board)
  (spec/assert ::full-move move)
  (let [opponent-color (opponent player)]
    (cond-> board
            (not (#{:home :pass} origin)) (assoc-in [:stones origin] nil)
            (not (#{:home :goal :pass} destination)) (assoc-in [:stones destination] player)
            (= opponent-color (get stones destination)) (update-in [:home opponent-color] inc)
            (= :home origin) (update-in [:home player] dec))))

(defn child-board
  "It takes a move and a board (see specs), and returns
   a new board in which 'move' was applied, that might
   or might not be the same as before."
  [{:keys [turn] :as board} {:keys [roll] :as move}]
  (spec/assert ::board board)
  (spec/assert ::move move)
  (when-not (game-ended? board)
    (let [full-move (full-move (assoc move :player turn))
          destination (:destination full-move)
          next-turn (if (and (not= 0 roll) (rosettes destination))
                      turn
                      (opponent turn))
          next-board (move-stone board full-move)]
      (cond (= :pass destination) (assoc board :turn next-turn)
            (= 0 roll) (assoc board :turn next-turn)
            (valid-move? board full-move) (assoc next-board :turn next-turn)))))

(defn all-moves
  "returns all possible moves with a given roll,
   not taking in count if they are valid or not."
  [{:keys [turn] :as board} roll]
  (spec/assert ::board board)
  (spec/assert ::roll roll)
  (->> (valid-origins turn)
       (map (fn [origin] (full-move {:roll roll, :origin origin, :player turn})))))

(defn valid-moves
  "returns all valid moves with a given roll"
  [{:keys [turn] :as board} roll]
  (spec/assert ::board board)
  (spec/assert ::roll roll)
  (let [moves (->> (all-moves board roll)
                   (filter (partial valid-non-pass-move? board)))]
    (if-not (empty? moves)
      (set moves)
      #{(pass-move roll turn)})))

(defn must-pass?
  "returns true if pass is a valid move"
  [{:keys [turn] :as board} roll]
  (or (zero? roll)
      (valid-move?
        board
        (pass-move roll turn))))
