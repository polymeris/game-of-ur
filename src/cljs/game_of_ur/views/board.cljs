(ns game-of-ur.views.board
  (:require [game-of-ur.config :as config]
            [game-of-ur.game.board :as game-board]
            [game-of-ur.views.style :as style]
            [game-of-ur.views.dice :as dice]
            [re-frame.core :as re-frame]))

(def rosette-path "M 0 0
                   Q 0.1,0.3 0,0.4 Q -.1,0.3 0,0
                   Q 0.3,0.1 0.4,0 Q 0.3,-.1 0,0
                   Q 0.1,-.3 0,-.4 Q -.1,-.3 0,0
                   Q -.3,0.1 -.4,0 Q -.3,-.1 0,0
                   Z")

(defn board-background []
  [:path {:id           "board-background"
          :stroke       (:board style/palette)
          :fill         (:board style/palette)
          :stroke-width 0.2
          :filter       "url(#drop-shadow)"
          :d            "M -3.5 0
                         V -1.5 H 0.5 V -0.5 H 2.5 V -1.5 H 4.5
                         V 1.5 H 2.5 V 0.5 H 0.5 V 1.5 H -3.5
                         V 0"}])

(defn- cell [[x y]]
  [:g {:transform (str "translate(" x "," y ")")}
   [:rect {:x -0.5 :y -0.5 :width 1 :height 1}]
   (when (get game-board/rosettes [x y])
     [:g {:stroke-width 0}
      [:path {:fill      (:rosette-secondary style/palette)
              :transform "rotate(22.5)"
              :d         rosette-path}]
      [:path {:fill      (:rosette-primary style/palette)
              :transform "rotate(-22.5)"
              :d         rosette-path}]
      [:circle {:stroke-width 0.02
                :stroke       (:cell style/palette)
                :fill         (:rosette-primary style/palette)
                :r            0.1}]])
   (when config/debug?
     [:text {:x 0 :y 0.075 :text-anchor :middle :font-size 0.15} (str "[" x ", " y "]")])])

(defn- cells []
  (->>
    game-board/valid-positions
    (map cell)
    (into [:g {:stroke       (:cell-border style/palette)
               :stroke-width 0.015
               :fill         (:cell style/palette)}])))

(def player-home
  {:white [0 -2.25]
   :black [0 2.25]})

(def player-goal
  {:white [4.79 -2]
   :black [4.79 2]})

(defn move-path [{:keys [roll player origin destination]}]
  (when-not (or (= :pass origin) (keyword? destination))
    (let [from-home? (= :home origin)
          path (get game-board/paths player)
          start-index (if from-home? 0 (.indexOf path origin))
          end-index (min (count path) (inc (+ start-index roll)))
          origin (if from-home? (player-home player) origin)
          steps (subvec path start-index end-index)]
      [:path {:stroke          (str (get-in style/palette [:stones player]) "7f")
              :fill            :transparent
              :stroke-width    0.2
              :stroke-linecap  :round
              :stroke-linejoin :round
              :d               (->> (rest steps)
                                    (map (fn [[x y]] (str " L " x " " y)))
                                    (apply str "M " (first origin) " " (second origin)))}])))

(defn- stone [[[x y] color]]
  (when color
    [:circle {:fill (get-in style/palette [:stones color]) :cx x :cy (- y 0.015) :r 0.3 :filter "url(#drop-shadow)"}]))

(defn- in-play-stones [stones]
  (->> stones
       (map (fn [[coords color]] [:g {:on-click #(re-frame/dispatch [:play-stone coords])} (stone [coords color])]))
       (into [:g.in-play-stones])))

(defn- off-board-stones [stones location]
  (letfn [(place [[color count]]
            (map (fn [i] [(update (location color) 0 #(- % (/ i 2))) color])
                 (range count)))]
    (->> stones
         (mapcat place)
         (map stone)
         (into [:g.home-stones {:on-click #(re-frame/dispatch [:play-stone :home])}]))))

(defn board []
  (let [{:keys [home stones turn] :as current-board} @(re-frame/subscribe [:board-state])
        last-move @(re-frame/subscribe [:last-move])
        roll @(re-frame/subscribe [:roll])]
    [:svg {:width    "100%"
           :height   "100%"
           :view-box "-4 -2.75 9 5.5"}
     [:defs
      [:filter {:id "drop-shadow" :height "150%"}
       [:feOffset {:in "SourceAlpha" :dy 0.015}]
       [:feGaussianBlur {:std-deviation 0.002}]
       [:feBlend {:in "SourceGraphic"}]]
      [:filter {:id "inner-shadow" :height "120%"}
       [:feOffset {:dy -0.08}]
       [:feGaussianBlur {:std-deviation 0.06 :result :offset-blur}]
       [:feComposite {:operator :out :in "SourceGraphic" :in2 :offset-blur :result :inverse}]
       [:feFlood {:flood-color :black :flood-opacity 0.15 :result :color}]
       [:feComposite {:operator :in :in :color :in2 :inverse :result :shadow}]
       [:feComposite {:operator :over :in :shadow :in2 "SourceGraphic"}]]]
     [:g
      [board-background]
      [cells]
      (when last-move [move-path last-move])
      [off-board-stones home player-home]
      [:g {:transform "scale(0.45)"}
       [off-board-stones (game-board/stones-in-goal current-board) player-goal]]
      [in-play-stones stones]
      [:g {:transform (str "translate(2.25, " (second (player-home turn)) ")")}
       [dice/dice roll]]]]))
