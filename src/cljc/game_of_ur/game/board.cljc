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

(def stone-colors #{:white :black})
(spec/def ::position (spec/tuple int? int?))
(spec/def ::stone stone-colors)
(spec/def ::stones (spec/map-of ::position (spec/nilable ::stone)))
(spec/def ::player stone-colors)
(spec/def ::turn stone-colors)
(spec/def ::white pos-int?)
(spec/def ::black pos-int?)
(spec/def ::roll (spec/and int? #(<= 0 % 4)))
(def origin? #(or (= :home %) (spec/valid? ::position %)))
(spec/def ::origin origin?)
(spec/def ::home (spec/keys :req-un [::white ::black]))
(spec/def ::board (spec/keys :req-un [::home ::turn ::stones]))
(spec/def ::move (spec/keys :req-un [::roll ::origin]))
(spec/def ::full-move (spec/keys :req-un [::roll ::origin ::destination ::player]))

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

(defn move-destination [{:keys [roll player origin] :as move}]
  (spec/assert ::move move)
  (let [path (get paths player)
        destination (get path (+ roll (.indexOf path origin)))]
    (assoc move :destination destination)))

(defn valid-move? [{:keys [home turn stones] :as board}
                   {:keys [roll player origin destination] :as move}]
  (spec/assert ::board board)
  (spec/assert ::full-move move)
  (and destination
       (or (not= origin :home) (> (home player) 0))
       (or (nil? (get stones destination))
           (and (= (opponent turn) (get stones destination))
                (not (rosettes destination))))))

(defn move-stone [{:keys [stones] :as board}
                  {:keys [player origin destination] :as move}]
  (spec/assert ::board board)
  (spec/assert ::full-move move)
  (let [opponent-color (opponent player)]
    (cond-> board
            true (assoc-in [:stones origin] nil)
            (= opponent-color (get stones destination)) (update-in [:home opponent-color] inc)
            (not= :goal destination) (assoc-in [:stones destination] player))))

(defn child-board [{:keys [turn] :as board} {:keys [roll] :as move}]
  (spec/assert ::board board)
  (spec/assert ::move move)
  (when-not (game-ended? board)
    (let [full-move (move-destination (assoc move :player turn))
          destination (:destination full-move)
          next-turn (if (and (not= 0 roll) (rosettes destination))
                      turn
                      (opponent turn))
          next-board (move-stone board full-move)]
      (cond (= 0 roll) (assoc board :turn next-turn)
            (valid-move? board full-move) (assoc next-board :turn next-turn)))))