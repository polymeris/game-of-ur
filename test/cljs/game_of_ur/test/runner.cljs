(ns game-of-ur.test.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [game-of-ur.test.game.board]))

(doo-tests 'game-of-ur.test.game.board)