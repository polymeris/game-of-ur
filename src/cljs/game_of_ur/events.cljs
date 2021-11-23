(ns game-of-ur.events
  (:require [re-frame.core :as re-frame]
            [game-of-ur.db :as db]
            [game-of-ur.ai.minmax :as mm]
            [game-of-ur.game.board :as board]
            [game-of-ur.ai.eval :as ev]))

(re-frame/reg-fx
  :sound
  (fn [fn]
    (.play (js/Audio. fn))))

(re-frame/reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(re-frame/reg-event-db
  :make-move'
  (fn [db [_ move]]
    (-> db
        (update :moves conj move)
        (dissoc :roll))))

(re-frame/reg-event-fx
 :make-move
 (fn [{{:keys [auto-roll]} :db} [_ move]]
   {:dispatch-n [[:make-move' move] (when auto-roll [:roll-dice]) [:ai-move]]}))

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
  (fn [{{:keys [roll moves ai auto-roll]} :db} [_ coords]]
    (when roll
      (let [board-state (reduce board/child-board board/initial-board moves)
            move (board/full-move {:roll   roll
                                   :player (:turn board-state)
                                   :origin coords})]
        (when (board/valid-move? board-state move)
          {:sound    "sfx/clack.mp3"
           :dispatch [:make-move move]})))))

(defn moves->board
  "Given a seq of moves, returns the current state of the board"
  [moves]
  (reduce board/child-board board/initial-board moves))

(re-frame/reg-event-fx
  :play-best-move
  (fn [{{:keys [roll moves auto-roll]} :db} [_]]
    (when roll
      (let [board (moves->board moves)
            move (case (:turn board)
                   :black (mm/best-move ev/dumb-evaluation-fn 3 board roll)
                   :white (mm/best-move ev/dumb-evaluation-fn 3 board roll))]
        {:dispatch-n (if auto-roll [[:make-move move] [:roll-dice]] [[:make-move move]])}))))

(re-frame/reg-event-fx
 :ai-move
 (fn [{{:keys [roll moves ai]} :db} [_]]
   (let [board-state (reduce board/child-board board/initial-board moves)]
     (when (and (get ai (:turn board-state)) (not (board/game-ended? board-state)))
       {:dispatch-n [(when-not roll [:roll-dice])]
        :dispatch-later {:ms 100 :dispatch [:play-best-move]}}))))

(re-frame/reg-event-db
  :set-ai
  (fn [db [_ player ai]]
    (assoc-in db [:ai player] ai)))

(re-frame/reg-event-db
  :set-auto-roll
  (fn [db [_ auto-roll]]
    (assoc db :auto-roll auto-roll)))
