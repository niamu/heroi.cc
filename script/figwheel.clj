(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra])

(ra/start-figwheel!
 {:figwheel-options {}
  :build-ids ["dev"]
  :all-builds
  [{:id "dev"
    :figwheel true
    :source-paths ["src"]
    :compiler {:main 'fume.client
               :asset-path "/js/out"
               :output-to "resources/public/js/fume.js"
               :output-dir "resources/public/js/out"
               :parallel-build true
               :verbose true}}]})

(ra/cljs-repl)
