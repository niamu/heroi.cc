(ns heroicc.web.state
  (:refer-clojure :exclude [read])
  (:require
   [clojure.set :as set]
   [datomic.client.api :as d]
   [heroicc.db.connection :as db]
   [om.next :as om]))

(defn steamid-from-request
  [request]
  (or (some->> (get-in request [:query-params "openid.identity"])
               vector flatten first
               (re-find #".*steamcommunity.com/openid/id/(\d+)")
               last
               (Long/parseLong))
      (some->> (get-in request [:cookies "steamid" :value])
               (Long/parseLong))))

(defmulti read om/dispatch)

(defmethod read :default
  [_ _ _]
  {:value "not-found"})

(defmethod read :app/host
  [{:keys [state]} _ _]
  #?(:clj {:value (str (name (get-in @state [:request :scheme]))
                       "://"
                       (get-in @state [:request :headers "host"]))}
     :cljs {:remote true}))

(defn current-user
  [state]
  #?(:clj (steamid-from-request (:request @state))
     :cljs (.. (getElementById js/document "current-user")
               -textContent)))

(defmethod read :app/current-user
  [{:keys [state]} _ _]
  {:value (current-user state)})

(defn query-params
  [state]
  #?(:clj (get-in @state [:request :query-params])
     :cljs (reduce (fn [accl param]
                     (let [[k v] (string/split param #"=")]
                       (assoc accl k v)))
                   {}
                   (-> (string/split (.. js/window
                                         -location
                                         -search) #"\?")
                       last
                       (string/split #"&")))))

(defmethod read :app/search
  [{:keys [state]} _ _]
  {:value (get (query-params state) "search" "")})

(defmethod read :app/categories
  [{:keys [state]} _ _]
  {:value (-> (get (query-params state) "category" [])
              vector flatten)})

(defmethod read :app/players
  [{:keys [state query]} _ _]
  {:value (pmap (comp (fn [p] (update p :steam/id #(:db/id %))) first)
                (d/q '[:find (pull ?e pattern)
                       :in $ [?steamids ...] pattern
                       :where
                       [?e :steam/id ?steamids]]
                     (d/db (db/connection))
                     (->> (get (query-params state) "steamid" [])
                          vector flatten
                          #?(:clj (pmap #(Long/parseLong %))))
                     query))})

(defmethod read :app/games
  [{:keys [state query]} _ _]
  (if (->> (get (query-params state) "steamid" [])
           vector flatten count zero?)
    {:value []}
    (let [categories (->> (get (query-params state) "category" [])
                          vector flatten
                          #?(:clj (pmap #(Integer/parseInt %))))
          appids (->> (d/q '[:find (pull ?p pattern)
                             :in $ [?steamids ...] pattern
                             :where
                             [?p :steam/id ?steamids]]
                           (d/db (db/connection))
                           (->> (get (query-params state) "steamid" [])
                                vector flatten
                                #?(:clj (pmap #(Long/parseLong %))))
                           [{:steam/games [:steam/appid]}])
                      (pmap (comp set :steam/games first))
                      (apply set/intersection)
                      (pmap #(get-in % [:steam/appid :db/id])))
          result (->> (d/q '[:find (pull ?e query)
                             :in $ [?appids ...] query
                             :where
                             [?e :steam/appid ?appids]]
                           (d/db (db/connection))
                           appids
                           query)
                      (pmap first)
                      (filter (fn [game]
                                (if-not (empty? categories)
                                  (every? (fn [c]
                                            (-> (pmap (comp :db/id
                                                            :category/id)
                                                      (:steam/categories game))
                                                set
                                                (contains? c)))
                                          categories)
                                  true))))]
      {:value (->> result
                   (pmap (fn [g] (update g :steam/appid #(:db/id %))))
                   (filter (fn [g] (:steam/game-name g))))})))

(defmethod read :steam/player
  [{:keys [state query]} _ params]
  (let [result (try (d/q '[:find (pull ?e pattern)
                           :in $ ?steamid pattern
                           :where [?e :steam/id ?steamid]]
                         (d/db (db/connection))
                         (if (= (:steam/id params) :app/current-user)
                           (current-user state)
                           (:steam/id params))
                         query)
                    (catch Exception e [["not-found"]]))]
    (if (empty? result)
      {:remote true}
      (if (= 1 (count result))
        {:value (ffirst result)}
        {:value result}))))

(defmethod read :steam/game
  [{:keys [state query]} _ params]
  (let [result (try (d/q '[:find (pull ?e pattern)
                           :in $ ?appid pattern
                           :where [?e :steam/appid ?appid]]
                         (d/db (db/connection))
                         (:steam/appid params)
                         query)
                    (catch Exception e [["not-found"]]))]
    (if (empty? result)
      {:remote true}
      (if (= 1 (count result))
        {:value (ffirst result)}
        {:value result}))))

(def parser
  (om/parser {:read read}))

(defn reconciler
  [request]
  (om/reconciler {:state {:request request}
                  :parser parser}))
