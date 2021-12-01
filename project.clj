(defproject game-of-ur "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.893"]
                 [reagent "1.1.0"]
                 [re-frame "1.2.0"]
                 [re-frisk "1.5.2"]
                 [cljsjs/react "17.0.2-0"]
                 [cljsjs/react-dom "17.0.2-0"]]
  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-ancient "1.0.0-RC3"]]
  :min-lein-version "2.5.3"
  :source-paths ["src/clj" "src/cljc"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/css"
                                    "target"]

  ;; PROFILES
  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.4"]
                                  [figwheel-sidecar "0.5.20"]
                                  [cider/piggieback "0.5.3"]
                                  [org.clojure/test.check "1.1.0"]]
                   :plugins      [[lein-figwheel "0.5.20"]]}}

  ;; DEV
  :figwheel {:css-dirs ["resources/public/css"]}
  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  ;; TEST
  :test-paths ["test/clj"]

  ;; BUILDS
  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src/cljs" "src/cljc"]
                        :figwheel     {:on-jsload "game-of-ur.core/mount-root"}
                        :compiler     {:main                 game-of-ur.core
                                       :output-to            "resources/public/js/compiled/app.js"
                                       :output-dir           "resources/public/js/compiled/out"
                                       :asset-path           "js/compiled/out"
                                       :source-map-timestamp true
                                       :preloads             [devtools.preload]
                                       :external-config      {:devtools/config {:features-to-install :all}}}}
                       {:id           "min"
                        :source-paths ["src/cljs" "src/cljc"]
                        :compiler     {:main            game-of-ur.core
                                       :output-to       "resources/public/js/compiled/app.js"
                                       :optimizations   :advanced
                                       :closure-defines {goog.DEBUG false}
                                       :pretty-print    false}}]})
