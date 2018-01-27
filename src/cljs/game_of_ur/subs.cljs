(ns game-of-ur.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [game-of-ur.game.board :as board]
            [cljs.spec.alpha :as spec]))

(re-frame/reg-sub
  :board-state
  (fn [db]
    (spec/assert ::board/board (reduce board/child-board board/initial-board (:moves db)))))

(re-frame/reg-sub
  :last-move
  (fn [db]
    (when-not (empty? (:moves db))
      (spec/assert ::board/full-move (last (:moves db))))))

(re-frame/reg-sub
  :roll
  (fn [db]
    (:roll db)))