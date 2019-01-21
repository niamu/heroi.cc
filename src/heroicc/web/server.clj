(ns heroicc.web.server
  (:require
   [datomic.ion.lambda.api-gateway :as apigw]
   [heroicc.web.router :as router]
   [org.httpkit.server :as server]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defonce ^:private server (atom nil))

(def app*
  (-> #'router/route-handler
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      wrap-not-modified))

(def app (apigw/ionize app*))

(defn stop
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil))
  (println "Stopped server."))

(defn start
  ([]
   (start 8080))
  ([port]
   (when (nil? @server)
     (reset! server (server/run-server app* {:port port})))
   (println (str "Started server on port " port "."))))

(defn restart [] (stop) (start))
