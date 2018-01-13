(ns game-of-ur.views
  (:require [game-of-ur.game.board :as game-board]))

(def palette {:board             "#efdfc4"
              :cell              "#efebce"
              :cell-border       "#999684"
              :rosette-primary   "#84c0c6"
              :rosette-secondary "#46b1c9"
              :highlight         "#eac435"
              :stones            {:white "#fbfdfe"
                                  :black "#1a414a"}})

(def rosette-path "M 0 0
                   Q 0.1,0.3 0,0.4 Q -.1,0.3 0,0
                   Q 0.3,0.1 0.4,0 Q 0.3,-.1 0,0
                   Q 0.1,-.3 0,-.4 Q -.1,-.3 0,0
                   Q -.3,0.1 -.4,0 Q -.3,-.1 0,0
                   Z")

(defn board-background []
  [:path {:id           "board-background"
          :stroke       (:board palette)
          :fill         (:board palette)
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
      [:path {:fill      (:rosette-secondary palette)
              :transform "rotate(22.5)"
              :d         rosette-path}]
      [:path {:fill      (:rosette-primary palette)
              :transform "rotate(-22.5)"
              :d         rosette-path}]
      [:circle {:stroke-width 0.02
                :stroke       (:cell palette)
                :fill         (:rosette-primary palette)
                :r            0.1}]])])

(defn- cells []
  (->>
    game-board/valid-positions
    (map cell)
    (into [:g {:stroke       (:cell-border palette)
               :stroke-width 0.015
               :fill         (:cell palette)}])))

(defn move-path [{:keys [roll player origin]}]
  (let [path (get game-board/paths player)
        ix (.indexOf path origin)
        steps (subvec path ix (+ ix roll))]
    [:path {:stroke          (str (get-in palette [:stones player]) "7f")
            :fill            :transparent
            :stroke-width    0.2
            :stroke-linecap  :round
            :stroke-linejoin :round
            :d               (->> (rest steps)
                                  (map (fn [[x y]] (str " L " x " " y)))
                                  (apply str "M " (first origin) " " (second origin)))}]))

(defn- stone [{:keys [x y color]}]
  [:circle {:fill (get-in palette [:stones color]) :cx x :cy (- y 0.015) :r 0.3 :filter "url(#drop-shadow)"}])

(defn- stones []
  (->> [{:x 1 :y 0 :color :black}
        {:x -3 :y 0 :color :white}]
       (map stone)
       (into [:g])))

(defn- board []
  [:svg {:width    "100%"
         :height   "100%"
         :view-box "-4 -1.725 9 3.5"}
   [:defs
    [:filter {:id "drop-shadow" :height "150%"}
     [:feOffset {:in "SourceAlpha" :dy 0.015}]
     [:feGaussianBlur {:std-deviation 0.002}]
     [:feBlend {:in "SourceGraphic"}]]]
   [:g
    [board-background]
    [cells]
    [move-path {:roll 4 :player :white :origin [-1 -1]}]
    [stones]]])

(defn main-panel []
  [board])
