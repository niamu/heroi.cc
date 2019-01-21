(ns heroicc.web.server
  (:gen-class)
  (:require
   [datomic.ion.lambda.api-gateway :as apigw]
   [heroicc.web.router :as router]
   [org.httpkit.server :as server]
   [ring.middleware.not-modified :as not-modified]
   [ring.middleware.defaults :as defaults]))

(defonce ^:private server (atom nil))

(def app-handler
  (-> (fn [request]
        (router/route-handler
         (if-not (:scheme request)
           (assoc request
                  :scheme (-> request
                              (get-in [:headers "x-forwarded-proto"])
                              keyword))
           request)))
      (defaults/wrap-defaults (assoc-in defaults/site-defaults
                                        [:security :anti-forgery] false))
      not-modified/wrap-not-modified))

#_(-> (slurp "resources/api-test.json")
      apigw/gateway->edn
      app-handler)

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
