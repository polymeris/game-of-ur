(ns game-of-ur.test.game.board
  (:require [cljs.test :refer [deftest testing is]]
            [game-of-ur.game.board :as b]))

(deftest moves
  (testing "move destinations"
    (is (= :home (:destination (b/move-destination {:roll 0, :player :white, :origin :home}))))))