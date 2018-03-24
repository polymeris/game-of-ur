(ns game-of-ur.views
  (:require [game-of-ur.game.board :as game-board]
            [game-of-ur.views.board :as board]
            [re-frame.core :as re-frame]
            [game-of-ur.config :as config]))

(defn main-panel []
  (let [roll        @(re-frame/subscribe [:roll])
        board-state @(re-frame/subscribe [:board-state])
        last-move   @(re-frame/subscribe [:last-move])]
    [:div
     [board/board board-state last-move]
     (when (and roll (game-board/must-pass? board-state roll))
       [:button.pass
        {:on-click #(re-frame/dispatch
                      [:make-move
                        (game-board/pass-move roll (get board-state :turn))])}
        "Pass"])  
                             
     ; Stuff for development purposes below
     (when config/debug?
       (->> (assoc board-state :last-move last-move)
            (map (fn [[title obj]] [:div [:h4 title] [:tt (str obj)]]))
            (into [:div#dev-helpers {:style {:border "1px solid grey"}}
                   (when roll
                     [:button
                      {:on-click #(re-frame/dispatch [:make-move (rand-nth (seq (game-board/valid-moves board-state roll)))])}
                      (str "Random move with roll " roll)])])))]))
