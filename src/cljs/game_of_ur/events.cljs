(ns game-of-ur.events
  (:require [re-frame.core :as re-frame]
            [game-of-ur.db :as db]))

(re-frame/reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))
