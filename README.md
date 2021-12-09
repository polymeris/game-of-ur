# game-of-ur
[![CircleCI](https://circleci.com/gh/polymeris/game-of-ur.svg?style=svg)](https://circleci.com/gh/polymeris/game-of-ur)

[Live Demo](https://polymeris.github.io/game-of-ur/)

## How to improve?

There are two main things to improve. The first one is simple, and is to improve
user interface/experience by getting the UI to work better. The second and more
interesting one is to improve the AI. For this we've thought about two
approaches. One is to improve the search function which as for now is a
simple _expectiminimax_, either by implementing some pruning or by
memoizing some redundant information somehow. The other and again more
interesting, is to either create better evaluation functions at
`cljc/game_of_ur/ai/eval.cljc`, or to make them evolve using some kind of
genetic algorithm.

## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

We also encourage to set `(spec/check-asserts true)`
to `false` in `cljc/game_of_ur/game/board.cljc` when working on the AI, since it
makes the program run considerably faster.


## Production Build


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```
