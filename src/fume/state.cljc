(ns fume.state
  "Utilities for state parsing and sync with server"
  (:refer-clojure :exclude [read])
  (:require [fume.db :as db :refer [conn]]
            [fume.util :refer [coll-keys]]
            [clojure.string :as string]
            [cognitect.transit :as t]
            [datascript.core :as d]
            [clojure.set :refer [intersection]]
            [om.next :as om]
            [#?(:clj clojure.edn :cljs cljs.reader) :as edn])
  #?(:cljs (:import [goog.net XhrIo])))

#?(:cljs
   (defn transit-post
     "Send queries to remote and await process returned data with db/import"
     [url]
     (fn [{:keys [remote] :as env} _]
       (.send XhrIo url
              (fn [e]
                (this-as this
                  (db/import (t/read (t/reader :json)
                                     (.getResponseText this)))))
              "POST" (t/write (t/writer :json) remote)
              #js {"Content-Type" "application/transit+json"}))))

(defn pull-query
  "Return Datascript result of query with pull syntax"
  ([state key query]
   (pull-query state key query nil))
  ([state key query params]
   (d/q [:find '[(pull ?e ?selector) ...]
         :in '$ '?selector
         :where ['?e key]
         (when params
           ['?e (first (keys params)) (first (vals params))])]
        (d/db state) query)))

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state query]} key _]
  (let [result (pull-query state key (or query [key]))]
    (if (empty? result)
      {:remote true}
      (if (= 1 (count result))
        {:value (get (first result) key :not-found)}
        {:value result}))))

(defmethod read :players
  [{:keys [state query]} _ params]
  (let [result (d/q [:find '[(pull ?e ?selector) ...]
                     :in '$ '?selector '?filter-pred
                     :where ['?e :player/steamid]
                     ['(?filter-pred ?e)]]
                    (d/db conn) query
                    (when params
                      #(contains? (set (:player params))
                                  (:player/steamid (d/entity (d/db conn) %)))))]
    #?(:cljs (if (empty? result)
               {:remote true}
               {:value (sort-by (first query) result)})
       :clj {:value (sort-by (first query) result)})))

(defmethod read :common/games
  [{:keys [state query] :as opts} _ params]
  (let [params (coll-keys params :player :filter)
        result (d/q [:find '[(pull ?e ?selector) ...]
                     :in '$ '?selector '?filter-player '?filter-category
                     :where ['?e :game/players]
                     ['(?filter-player ?e)]
                     ['(?filter-category ?e)]]
                    (d/db conn)
                    query
                    #(every? (fn [player]
                               (contains? (set (map :player/steamid
                                                    (-> (d/db conn)
                                                        (d/entity %)
                                                        :game/players)))
                                          player))
                             (:player params))
                    #(every? (fn [search]
                               (or (string/includes?
                                    (string/lower-case (-> (d/db conn)
                                                           (d/entity %)
                                                           :game/name))
                                    (string/lower-case search))
                                   (some
                                    (fn [t]
                                      (string/includes?
                                       (string/lower-case (:description t))
                                       (string/lower-case search)))
                                    (-> (d/db conn)
                                        (d/entity %)
                                        :game/categories))))
                             (:filter params)))]
    #?(:cljs (if (empty? result)
               {:remote true}
               {:value (sort-by (first query) result)})
       :clj {:value (sort-by (first query) result)})))

(defmethod read :current/user
  [{:keys [state query] :as env} _ params]
  #?(:clj (let [result (if params
                         (pull-query state :player/steamid query params)
                         (pull-query state :current/user
                                     [{:current/user query}]))]
            (if (= 1 (count result))
              {:value (if params
                        (first result)
                        (-> (get (first result) :current/user :not-found)))}
              {:value result}))
     :cljs (let [result (pull-query state :current/user
                                    [{:current/user query}])]
             (if (empty? result)
               {:remote true}
               {:value (-> (get (first result) :current/user :not-found))}))))

(defmethod read :games/categories
  [{:keys [state query]} key _]
  (let [result #?(:clj (->> (d/q [:find '[(pull ?e ?selector) ...]
                                  :in '$ '?selector
                                  :where ['?e :game/steam_appid]]
                                 (d/db conn) [:game/categories])
                            (reduce (fn [accl game]
                                      (apply conj accl (:game/categories game)))
                                    #{})
                            (sort-by :id)
                            vec)
                  :cljs (-> (pull-query state :games/categories
                                        [:games/categories])
                            first
                            :games/categories))]
    (if (empty? result)
      {:remote true}
      {:value result})))

(def parser
  (om/parser {:read read}))

(def reconciler
  (om/reconciler (merge {:state conn
                         :parser parser}
                        #?(:cljs {:send (transit-post "/api")}))))
