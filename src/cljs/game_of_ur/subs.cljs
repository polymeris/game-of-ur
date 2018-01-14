(ns game-of-ur.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [game-of-ur.game.board :as board]))

(re-frame/reg-sub
  :board-state
  (fn [db]
    (reduce board/child-board board/initial-board (:moves db))))

(re-frame/reg-sub
  :last-move
  (fn [db]
    (last (:moves db))))
