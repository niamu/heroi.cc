(defproject fume "0.1.2"
  :description "Fume: A Steam web client"
  :url "http://heroi.cc"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure         "1.8.0"]
                 [org.clojure/clojurescript   "1.9.229"]
                 [org.clojure/tools.nrepl     "0.2.12"]
                 [org.clojure/core.memoize    "0.5.8"]
                 [org.clojure/test.check      "0.9.0"]
                 [org.clojure/data.json       "0.2.6"]
                 [com.cognitect/transit-clj   "0.8.285"]
                 [com.cognitect/transit-cljs  "0.8.239"]

                 [org.omcljs/om               "1.0.0-alpha47"]

                 [clj-time                    "0.13.0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]

                 [sablono                     "0.7.3"]
                 [hiccup                      "1.0.5"]
                 [garden                      "1.3.2"]

                 [http-kit                    "2.1.18"]
                 [ring/ring-core              "1.4.0"]
                 [ring/ring-defaults          "0.2.1"]

                 [com.cemerick/friend         "0.2.1"
                  :exclusions [org.clojure/core.cache]]
                 [javax.servlet/servlet-api   "2.5"]

                 [datascript                  "0.15.0"]
                 [com.domkm/silk              "0.1.2"]

                 [com.powernoodle/binnacle    "0.3.3"]
                 [com.powernoodle/normalize   "0.1.3"]]
  :source-paths ["src" "test"]
  :main fume.server
  :profiles {:uberjar {:aot :all
                       :uberjar-name "fume.jar"
                       :prep-tasks ["compile" ["cljsbuild" "once"]]}
             :dev {:dependencies [[figwheel-sidecar "0.5.7"]]
                   :plugins [[lein-cljsbuild "1.1.4"]]}}
  :clean-targets ^{:protect false} ["resources/public/js" "target"]
  :cljsbuild {:builds [{:source-paths ["src"]
                        :compiler {:main fume.client
                                   :asset-path "/js/out"
                                   :output-to "resources/public/js/fume.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :advanced
                                   :parallel-build true
                                   :source-map-timestamp true}}]})
