(ns fume.router
  "Shared routing and response utilities between server and client"
  (:require [fume.routes :refer [routes]]
            [fume.page :as page]
            [fume.page.login :refer [Login]]
            [fume.page.dashboard :refer [Dashboard]]
            [fume.page.common-games :refer [CommonGames]]
            [fume.db :as db]
            [fume.state :as state]
            [domkm.silk :as silk]
            #?@(:clj
                [[domkm.silk.serve :refer [ring-handler]]
                 [datascript.core :as d]
                 [cemerick.friend :as friend]
                 [ring.util.response :refer [redirect]]])))

#?(:clj
   (defmacro authenticated
     "If user is authenticated, return the requested page.
     Otherwise redirect to the login page"
     [page]
     `(if ((comp boolean friend/identity) ~'req) ~page (redirect "/login"))))

(defn serve
  "Serve pages in a map as expected by Ring"
  ([route {:keys [status headers body] :as request}]
   (serve route request false))
  ([route {:keys [status headers body] :as request} auth-required?]
   #?(:clj (fn [req]
             (cond-> {:status (or status 200)
                      :headers (or headers {"Content-Type" "text/html"})
                      :body (page/wrap req route (page/react-root body req))}
               auth-required? authenticated))
      :cljs (page/react-root body))))

(defmulti response identity)

(defmethod response :api
  [route]
  (fn [req]
    {:status 200
     :headers {"Content-Type" "application/transit+json"}
     :body (state/parser {:state db/conn} (:transit-params req))}))

(defmethod response :login
  [route]
  (serve route {:body #'Login}))

(defmethod response :logout
  [route]
  #?(:clj (fn [req] (-> (silk/depart routes :login) redirect friend/logout*))
     :cljs (serve route {:body #'Login})))

(defmethod response :dashboard
  [route]
  (serve route {:body #'Dashboard} true))

(defmethod response :common-games
  [route]
  (serve route {:body #'CommonGames} true))

(defmethod response :default
  [route]
  (serve route {:body #'Login :status 404}))

(defn route->response
  [matched-route]
  (response matched-route))

#?(:clj
   (def route-handler
     (ring-handler routes route->response)))

#?(:cljs
   (defn path->name
     [url]
     (:domkm.silk/name (silk/arrive routes url))))
