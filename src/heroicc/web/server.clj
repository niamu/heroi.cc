(ns heroicc.web.server
  (:gen-class)
  (:require
   [datomic.ion.lambda.api-gateway :as apigw]
   [heroicc.web.router :as router]
   [org.httpkit.server :as server]
   [ring.middleware.not-modified :as not-modified]
   [ring.middleware.defaults :as defaults]))

(defonce ^:private server (atom nil))

(defn wrap-scheme
  [handler]
  (fn [request]
    (handler (assoc request :scheme
                    (keyword (get-in request [:headers "x-forwarded-proto"]
                                     "https"))))))

(def app-handler
  (-> router/route-handler
      (defaults/wrap-defaults (assoc-in defaults/site-defaults
                                        [:security :anti-forgery] false))
      not-modified/wrap-not-modified
      wrap-scheme))

(def app
  (apigw/ionize app-handler))

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
     (reset! server (server/run-server #'app-handler {:port port})))
   (println (str "Started server on port " port "."))))

(defn restart [] (stop) (start))

(defn -main
  [& args]
  (start))
