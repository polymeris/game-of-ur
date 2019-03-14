(ns game-of-ur.views.style)

(def light "#fbfdfe")

(def dark "#1a414a")

(def palette {:background        "#e1efef"
              :board             "#efdfc4"
              :cell              "#efebce"
              :cell-border       "#999684"
              :rosette-primary   "#84c0c6"
              :rosette-secondary "#46b1c9"
              :highlight         "#eac435"
              :stones            {:white light
                                  :black dark}})

(def main {:background-color (:background palette)})

(def button {:background-color (:board palette)
             :border (str "4px solid " (:rosette-primary palette))
             :font-size "200%"
             :font-weight :bold
             :height "2em"
             :margin "1em"
             :padding "0 2em"})