(ns fume.server
  "Core server components to serve web pages"
  (:gen-class)
  (:require [fume
             [db :as db]
             [style :as style]
             [middleware :refer [wrap-transit-response wrap-transit-params]]
             [router :refer [route-handler]]]
            [clojure.tools.nrepl.server :as repl]
            [org.httpkit.server :as http]
            [cemerick.friend :as friend]
            [cemerick.friend.openid :as openid]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defonce server (atom nil))
(defonce nrepl (atom nil))

(defn parse-identity
  "Find the user's Steam ID"
  [auth]
  (when auth
    (let [steamid (->> (:identity auth)
                       (re-find #"http://steamcommunity.com/openid/id/(\d+)")
                       last)]
      (db/add-player steamid {:friends? true})
      {:identity steamid})))

(def app
  "Application route handling, authentication and middleware"
  (-> #'route-handler
      (friend/authenticate
       {:allow-anon? true
        :unathorized-handler (constantly {:status 401})
        :unauthenticated-handler (constantly {:status 401})
        :workflows [(openid/workflow :credential-fn parse-identity)]})
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      wrap-transit-params
      wrap-transit-response
      (wrap-not-modified)))

(defn stop
  "Stop the Web server"
  []
  (when-not (nil? @nrepl)
    (repl/stop-server nrepl))
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil))
  (println "Stopped."))

(defn start
  "Start Web server and initialize stylsheet and DB"
  []
  (db/init-db)
  (when (nil? @nrepl)
    (reset! nrepl (repl/start-server :port 7888 :bind "localhost")))
  (when (nil? @server)
    (reset! server (http/run-server #'app {:port 8080})))
  (println "Fuming..."))

(defn restart [] (stop) (start))

(defn -main [] (start))

(style/render)
