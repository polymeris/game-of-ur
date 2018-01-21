(ns game-of-ur.views
  (:require [game-of-ur.game.board :as game-board]
            [game-of-ur.views.board :as board]
            [re-frame.core :as re-frame]
            [game-of-ur.config :as config]))

(defn main-panel []
  (let [board-state @(re-frame/subscribe [:board-state])
        last-move @(re-frame/subscribe [:last-move])]
    [:div
     [board/board board-state last-move]
     ; Stuff for development purposes below
     (when config/debug?
       (let [roll (rand-nth [0 4 1 1 1 1 3 3 3 3 2 2 2 2 2 2])]
         (->> (assoc board-state :last-move last-move)
              (map (fn [[title obj]] [:div [:h4 title] [:tt (str obj)]]))
              (into [:div#dev-helpers {:style {:border "1px solid grey"}}
                     [:button
                      {:on-click #(re-frame/dispatch [:make-move (rand-nth (seq (game-board/valid-moves board-state roll)))])}
                      (str "Random move with roll " roll)]]))))]))
