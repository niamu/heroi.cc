(ns heroicc.db.init
  (:require
   [clojure.core.memoize :as memoize]
   [datomic.client.api :as d]
   [heroicc.db.connection :as db]
   [heroicc.db.schema :as schema]
   [heroicc.steam :as steam]))

(defn- has-ident?
  [db ident]
  (contains? (d/pull db {:eid ident :selector [:db/ident]})
             :db/ident))

(defn- data-loaded?
  [db]
  (has-ident? db :app/title))

(defn load-dataset
  [conn]
  (if (data-loaded? (d/db conn))
    :already-loaded
    (doseq [datom [schema/app-title
                   [{:app/title "Heroicc"}]
                   schema/player
                   schema/game
                   schema/categories]]
      (d/transact conn {:tx-data datom}))))

(defn load-player*
  [current-user]
  ;; Add all players and their games
  (doseq [steamid (->> (map second (steam/player-friends current-user))
                       (cons current-user)
                       set
                       (remove (set (flatten
                                     (d/q '[:find ?steamid
                                            :where [_ :steam/id ?steamid]]
                                          (d/db (db/connection)))))))]
    (when-let [player (steam/player-by-id steamid)]
      (d/transact (db/connection)
                  {:tx-data [(cond-> player
                               (= steamid current-user)
                               (assoc :steam/friends
                                      (steam/player-friends steamid))
                               true
                               (assoc :steam/games
                                      (steam/player-games steamid)))]})))
  ;; Add all games of the player and their friends
  (doseq [appid (->> (map second (steam/player-friends current-user))
                     (cons current-user)
                     (map (fn [steamid] (steam/player-games steamid)))
                     (apply concat)
                     (map (fn [{:keys [steam/appid]}] appid))
                     set
                     (remove (set (flatten
                                   (d/q '[:find ?appid
                                          :where [_ :steam/appid ?appid]]
                                        (d/db (db/connection)))))))]
    (when-let [game (steam/game appid)]
      (d/transact (db/connection)
                  {:tx-data [game]}))))

(def load-player
  (memoize/ttl load-player* :ttl/threshold (* 60 60 1000)))
