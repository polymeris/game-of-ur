(ns game-of-ur.events
  (:require [re-frame.core :as re-frame]
            [game-of-ur.db :as db]
            [game-of-ur.ai.minmax :as mm]
            [game-of-ur.game.board :as board]))

(re-frame/reg-fx
  :sound
  (fn [fn]
    (.play (js/Audio. fn))))

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

(defn roll-dice
  "Rolls the dice"
  [db]
  (assoc db :roll (rand-nth [0 4 1 1 1 1 3 3 3 3 2 2 2 2 2 2])))

(re-frame/reg-event-db
  :roll-dice
  (fn [db _]
    (roll-dice db)))

(re-frame/reg-event-fx
 :pass
 (fn [{{:keys [auto-roll]} :db} [_ roll turn]]
   (let [m [:make-move (board/pass-move roll turn)]]
     {:dispatch-n (if auto-roll [m [:roll-dice]] [m])})))

(re-frame/reg-event-fx
  :play-stone
  (fn [{{:keys [roll moves auto-roll]} :db} [_ coords]]
    (when roll
      (let [board-state (reduce board/child-board board/initial-board moves)
            move (board/full-move {:roll   roll
                                   :player (:turn board-state)
                                   :origin coords})]
        (when (board/valid-move? board-state move)
          {:sound      "sfx/clack.mp3"
           :dispatch-n (filter identity
                               (list [:make-move move]
                                     (when auto-roll [:roll-dice])))})))))

(defn moves->board
  "Given a seq of moves, returns the current state of the board"
  [moves]
  (reduce board/child-board board/initial-board moves))

(re-frame/reg-event-fx
  :play-best-move
  (fn [{{:keys [roll moves auto-roll]} :db} [_]]
    (when roll
      (let [board (moves->board moves)
            move (mm/best-move mm/dumb-evaluation-fn 3 board roll)]
        {:dispatch-n (if auto-roll [[:make-move move] [:roll-dice]] [:make-move move])}))))

(re-frame/reg-event-db
  :set-ai
  (fn [db [_ player ai]]
    (assoc-in db [:ai player] ai)))

(re-frame/reg-event-db
  :set-auto-roll
  (fn [db [_ auto-roll]]
    (assoc db :auto-roll auto-roll)))
