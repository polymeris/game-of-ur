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

(spec/def ::origin (spec/or :h ::home-position :p ::position))
(spec/def ::destination (spec/or :h ::home-position :p ::position :g ::goal-position))
(spec/def ::home (spec/keys :req-un [::white ::black]))
(spec/def ::board (spec/keys :req-un [::home ::turn ::stones]))
(spec/def ::move (spec/keys :req-un [::roll ::origin]))
(spec/def ::full-move (spec/keys :req-un [::roll ::origin ::destination ::player]))

(defn stones-in-play [{:keys [home stones] :as board} player]
  (spec/assert ::board board)
  (spec/assert ::player player)
  (->> (vals stones)
       (filter #(= % player))
       (count)
       (+ (get home player))))

(defn player-won? [board player]
  (spec/assert ::board board)
  (spec/assert ::player player)
  (= 0 (stones-in-play board player)))

(defn game-ended? [board]
  (spec/assert ::board board)
  (or (player-won? board :white)
      (player-won? board :black)))

(defn full-move [{:keys [roll player origin] :as move}]
  (spec/assert ::move move)
  (let [path (get paths player)
        destination (get path (+ roll (.indexOf path origin)))]
    (assoc move :destination destination)))

(defn valid-move? [{:keys [home turn stones] :as board}
                   {:keys [player origin destination] :as move}]
  (spec/assert ::board board)
  (spec/assert ::move move)
  (and destination
       (= turn player)
       (or (not= origin :home) (> (home player) 0))
       (or (= origin :home) (= player (get stones origin)))
       (or (nil? (get stones destination))
           (and (= (opponent turn) (get stones destination))
                (not (rosettes destination))))))

(defn move-stone [{:keys [stones] :as board}
                  {:keys [player origin destination] :as move}]
  (spec/assert ::board board)
  (spec/assert ::full-move move)
  (let [opponent-color (opponent player)]
    (cond-> board
            (not= :home origin) (assoc-in [:stones origin] nil)
            (= opponent-color (get stones destination)) (update-in [:home opponent-color] inc)
            (= :home origin) (update-in [:home player] dec)
            (not= :goal destination) (assoc-in [:stones destination] player))))

(defn child-board [{:keys [turn] :as board} {:keys [roll] :as move}]
  (spec/assert ::board board)
  (spec/assert ::move move)
  (when-not (game-ended? board)
    (let [full-move (full-move (assoc move :player turn))
          destination (:destination full-move)
          next-turn (if (and (not= 0 roll) (rosettes destination))
                      turn
                      (opponent turn))
          next-board (move-stone board full-move)]
      (cond (= 0 roll) (assoc board :turn next-turn)
            (valid-move? board full-move) (assoc next-board :turn next-turn)))))

(defn all-moves [{:keys [turn] :as board} roll]
  (spec/assert ::board board)
  (spec/assert ::roll roll)
  (->> (valid-origins turn)
       (map (fn [origin] (full-move {:roll roll :origin origin :player turn})))))

(defn valid-moves [board roll]
  (spec/assert ::board board)
  (spec/assert ::roll roll)
  (->> (all-moves board roll)
       (filter (partial valid-move? board))))