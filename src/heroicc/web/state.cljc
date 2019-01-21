(ns heroicc.web.state
  (:refer-clojure :exclude [read])
  (:require
   [clojure.set :as set]
   [datomic.client.api :as d]
   [heroicc.db.connection :as db]
   [om.next :as om]))

(defmulti read om/dispatch)

(defmethod read :default
  [_ key _]
  (let [result (try (d/q '[:find ?x
                           :in $ ?key
                           :where [_ ?key ?x]]
                         (d/db (db/connection))
                         key)
                    (catch Exception e [["not-found"]]))]
    (if (empty? result)
      {:remote true}
      (if (= 1 (count result))
        {:value (ffirst result)}
        {:value result}))))

(defmethod read :app/host
  [{:keys [state]} _ _]
  #?(:clj {:value (str (name (get-in @state [:request :scheme]))
                       "://"
                       (get-in @state [:request :headers "host"]))}
     :cljs {:remote true}))

(defn current-user
  [state]
  #?(:clj (->> (get-in @state [:request :query-params "openid.identity"])
               (re-find #".*steamcommunity.com/openid/id/(\d+)")
               last
               (Long/parseLong))
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
  {:value (get (query-params state) "search")})

(defmethod read :app/categories
  [{:keys [state]} _ _]
  {:value (let [categories (get (query-params state) "category" [])]
            (if-not (coll? categories)
              [categories] categories))})

(defmethod read :app/players
  [{:keys [state query]} _ _]
  (let [steamids (->> (get (query-params state) "steamid")
                      #?(:clj (mapv #(Long/parseLong %))))
        result (d/q '[:find (pull ?e pattern)
                      :in $ [?steamids ...] pattern
                      :where
                      [?e :steam/id ?steamids]]
                    (d/db (db/connection))
                    steamids
                    query)]
    {:value (map (comp (fn [p] (update p :steam/id #(:db/id %))) first)
                 result)}))

(defmethod read :app/games
  [{:keys [state query]} _ _]
  (let [search (get (query-params state) "search" "")
        steamids (->> (get (query-params state) "steamid")
                      #?(:clj (mapv #(Long/parseLong %))))
        categories (->> (let [categories
                              (get (query-params state) "category" [])]
                          (if-not (coll? categories)
                            [categories] categories))
                        #?(:clj (mapv #(Integer/parseInt %))))
        appids (->> (d/q '[:find (pull ?p pattern)
                           :in $ [?steamids ...] pattern
                           :where
                           [?p :steam/id ?steamids]
                           [?c :category/id ?categories]]
                         (d/db (db/connection))
                         steamids
                         [{:steam/games [:steam/appid]}])
                    (map (comp set :steam/games first))
                    (apply set/intersection)
                    (map #(get-in % [:steam/appid :db/id])))
        result (->> (d/q '[:find (pull ?e query)
                           :in $ [?appids ...] ?search query
                           :where
                           [?e :steam/appid ?appids]
                           [?e :steam/game-name ?name]
                           [(.toLowerCase ^String ?name) ?lowercase]
                           [(.toLowerCase ^String ?search) ?s]
                           [(.contains ^String ?lowercase ?s)]]
                         (d/db (db/connection))
                         appids
                         search
                         query)
                    (map first)
                    (filter (fn [game]
                              (if-not (empty? categories)
                                (every? (fn [c]
                                          (-> (map (comp :db/id
                                                         :category/id)
                                                   (:steam/categories game))
                                              set
                                              (contains? c)))
                                        categories)
                                true))))]
    {:value (->> result
                 (map (fn [g] (update g :steam/appid #(:db/id %))))
                 (filter (fn [g] (:steam/game-name g))))}))

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
