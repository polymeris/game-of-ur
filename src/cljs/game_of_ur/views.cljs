(ns game-of-ur.views
  (:require [game-of-ur.game.board :as game-board]
            [game-of-ur.views.board :as board]
            [re-frame.core :as re-frame]
            [game-of-ur.config :as config]))

(defn main-panel []
  (let [board-state @(re-frame/subscribe [:board-state])]
    [:div
     [board/board board-state]
     ; Stuff for development purposes below
     (when config/debug?
       (let [roll (rand-int 5)]
         (->> board-state
              (map (fn [[title obj]] [:div [:h4 title] [:tt (str obj)]]))
              (into [:div#dev-helpers {:style {:border "1px solid grey"}}
                     [:button
                      {:on-click #(re-frame/dispatch [:make-move (rand-nth (game-board/valid-moves board-state roll))])}
                      (str "Random move with roll " roll)]]))))]))
