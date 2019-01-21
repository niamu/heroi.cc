(ns heroicc.web.router
  (:require
   [domkm.silk :as silk]
   #?(:clj [domkm.silk.serve :as serve])
   [heroicc.db.init :as init]
   [heroicc.web.dashboard :as dashboard]
   [heroicc.web.error :as error]
   [heroicc.web.games :as games]
   [heroicc.web.login :as login]
   [heroicc.web.routes :as routes]
   [heroicc.web.state :as state]
   [heroicc.web.style :as style]
   [heroicc.web.wrap :as wrap]
   [om.dom :as dom]
   [om.next :as om]
   [ring.util.response :as ring]))

(defn react-root
  "Add a React UI component as a root. Client needs to target DOM node."
  [request root-component]
  (om/add-root! (state/reconciler request)
                root-component
                #?(:clj nil :cljs (.getElementById js/document "app"))))

(defn serve
  [route {:keys [status headers body]}]
  #?(:clj (fn [request]
            {:status (or status 200)
             :headers (or headers {"Content-Type" "text/html"})
             :body (wrap/wrap route
                              (if (fn? body)
                                (dom/render-to-str (react-root request body))
                                body))})
     :cljs body))

(defmulti response identity)

(defmethod response :default
  [route]
  (serve :error-404 {:status 404 :body error/ErrorPage}))

(defmethod response :login
  [route]
  (serve route {:body login/Login}))

(defmethod response :games
  [route]
  #?(:clj (fn [request]
            (if-let [steamid
                     (some->> (get-in request [:query-params "openid.identity"])
                              (re-find #".*steamcommunity.com/openid/id/(\d+)")
                              last
                              (Long/parseLong))]
              (do (init/load-player steamid)
                  {:status 200
                   :headers {"Content-Type" "text/html"}
                   :body (->> (react-root request games/Games)
                              dom/render-to-str
                              (wrap/wrap route))})
              (ring/redirect (silk/depart routes/routes :login))))
     :cljs (serve route {:body games/Games})))

(defmethod response :dashboard
  [route]
  #?(:clj (fn [request]
            (if-let [steamid
                     (some->> (get-in request [:query-params "openid.identity"])
                              (re-find #".*steamcommunity.com/openid/id/(\d+)")
                              last
                              (Long/parseLong))]
              (do (init/load-player steamid)
                  {:status 200
                   :headers {"Content-Type" "text/html"}
                   :body (->> (react-root request dashboard/Dashboard)
                              dom/render-to-str
                              (wrap/wrap route))})
              (ring/redirect (silk/depart routes/routes :login))))
     :cljs (serve route {:body dashboard/Dashboard})))

(defmethod response :stylesheet
  [route]
  (fn [request]
    {:status 200
     :headers {"Content-Type" "text/css"}
     :body (style/render)}))

(defn route->response
  [matched-route]
  (response matched-route))

#?(:clj
   (def route-handler
     (serve/ring-handler routes/routes route->response)))

#?(:cljs
   (defn path->name
     [url]
     (:domkm.silk/name (silk/arrive routes/routes url))))
