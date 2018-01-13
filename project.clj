(defproject game-of-ur "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.542"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.2"]
                 [re-frisk "0.3.2"]]
  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-sassc "0.10.4"]]
  :min-lein-version "2.5.3"
  :source-paths ["src/clj" "src/cljc"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/css"
                                    "target"]

  ;; PROFILES
  :profiles {:dev {:dependencies [[binaryage/devtools "0.8.2"]
                                  [figwheel-sidecar "0.5.8"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/test.check "0.9.0"]]
                   :plugins      [[lein-figwheel "0.5.9"]
                                  [lein-doo "0.1.7"]]}}
  ;; DEV
  :figwheel {:css-dirs ["resources/public/css"]}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  ;; TEST
  :aliases {"test" ["doo" "once"]}
  :doo {:build "test"
        :alias {:default [:phantom]}}

  ;; RESOURCES
  :sassc [{:src       "resources/sass/screen.sass"
           :output-to "resources/public/css/screen.css"
           :style     "compressed"}]

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
                                       :pretty-print    false}}
                       {:id           "test"
                        :source-paths ["src/cljs" "src/cljc" "test/cljs"]
                        :compiler     {:main          game-of-ur.test.runner
                                       :output-to     "resources/public/js/compiled/test.js"
                                       :output-dir    "resources/public/js/compiled/test"
                                       :optimizations :simple}}]})

