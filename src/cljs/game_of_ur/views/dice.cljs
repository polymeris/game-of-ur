(ns game-of-ur.views.dice
  (:require [game-of-ur.views.style :as style]
            [re-frame.core :as re-frame]))

(def die-contour "M  0      0.325
                  L  0.375 -0.325
                  L -0.375 -0.325
                  Z")

(def die-edges "M  0     -0.080
                L  0      0.325
                M  0     -0.080
                L  0.375 -0.325
                M  0     -0.080
                L -0.375 -0.325")

(def die-top-marker "M  0     -0.028
                     L  0.045 -0.108
                     L -0.045 -0.108
                     Z")

(def die-bottom-marker "M  0      0.325
                        L  0.038   0.26
                        L  0      0.245
                        L -0.038   0.26
                        Z")

(defn die [pos has-top-marker]
  (let [bottom-marker-pos (rand-int 4)]
    [:g
     {:filter "url(#inner-shadow)"}
     [:g {:transform (str "translate(" (* 0.55 pos) ") rotate(" (* 180 pos) ")")}
      [:path {:d            die-contour
              :fill         style/light
              :stroke       style/dark
              :stroke-width 0.001}]
      [:path {:transform (str "rotate(" (* 120 bottom-marker-pos) " 0 -0.108)")
              :d         die-bottom-marker
              :fill      style/dark}]
      (if has-top-marker
        [:path {:d    die-top-marker
                :fill style/dark}]
        [:path {:transform (str "rotate(" (* 120 (inc bottom-marker-pos)) " 0 -0.108)")
                :d         die-bottom-marker
                :fill      style/dark}])
      [:path {:d            die-edges
              :fill         :transparent
              :stroke       (style/palette :rosette-primary)
              :stroke-width 0.002}]]]))

(defn dice [value]
  (if value
    (let [markers (shuffle (concat (repeat value true) (repeat (- 4 value) false)))]
      [:g
       [die 0 (markers 0)]
       [die 1 (markers 1)]
       [die 2 (markers 2)]
       [die 3 (markers 3)]
       [:text
        {:y 0.1 :x 2.125 :text-anchor :middle :font-size 0.3 :fill (style/palette :rosette-primary)}
        value]])
    [:g {:on-click #(re-frame/dispatch [:roll-dice])}
     [die 0 false]
     [die 0.15 false]
     [die 0.30 false]
     [die 0.45 false]
     [:text
      {:y 0.1 :x 1.25 :text-anchor :middle :font-size 0.3 :fill (style/palette :rosette-primary)}
      "Roll!"]]))