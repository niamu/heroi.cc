(ns heroicc.web.server
  (:gen-class)
  (:require
   [datomic.ion.lambda.api-gateway :as apigw]
   [heroicc.web.router :as router]
   [org.httpkit.server :as server]
   [ring.middleware.cookies :as cookies]
   [ring.middleware.defaults :as defaults]
   [ring.middleware.not-modified :as not-modified]))

(defonce ^:private server (atom nil))

(defn wrap-scheme
  [handler]
  (fn [request]
    (handler (assoc request :scheme
                    (keyword (get-in request [:headers "x-forwarded-proto"]
                                     (:scheme request)))))))

(def app-handler
  (-> router/route-handler
      (defaults/wrap-defaults (assoc-in defaults/site-defaults
                                        [:security :anti-forgery] false))
      not-modified/wrap-not-modified
      cookies/wrap-cookies
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
