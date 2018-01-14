(ns game-of-ur.views
  (:require [game-of-ur.game.board :as game-board]
            [game-of-ur.views.board :as board]))

(defn main-panel []
  [board/board game-board/initial-board])
