(ns game-of-ur.events
  (:require [re-frame.core :as re-frame]
            [game-of-ur.db :as db]
            [game-of-ur.game.board :as board]))

(re-frame/reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(re-frame/reg-event-db
  :make-move
  (fn [db [_ move]]
    (-> db
        (update :moves conj move)
        (dissoc :roll))))

(re-frame/reg-event-db
  :roll-dice
  (fn [db _]
    (assoc db :roll (rand-nth [0 4 1 1 1 1 3 3 3 3 2 2 2 2 2 2]))))

(re-frame/reg-event-db
  :play-stone
  (fn [db [_ coords]]
    (if-let [roll (:roll db)]
      (let [board-state (reduce board/child-board board/initial-board (:moves db))
            move (board/full-move {:roll   roll
                                   :player (:turn board-state)
                                   :origin coords})]
        (if (board/valid-move? board-state move)
          (-> db
              (update :moves conj move)
              (dissoc :roll))
          db))
      db)))