(ns heroicc.db.init
  (:require
   [clojure.core.memoize :as memoize]
   [datomic.client.api :as d]
   [heroicc.db.connection :as db]
   [heroicc.db.schema :as schema]
   [heroicc.steam :as steam])
  (:import [java.util Date]
           [java.time LocalDate]))

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

(defn load-player
  [current-user]
  ;; TODO: Refresh if last-updated is too old
  (when (empty? (d/q '[:find ?last-updated
                       :in $ ?steamid
                       :where
                       [?e :steam/id ?steamid]
                       [?e :app/last-updated ?last-updated]]
                     (d/db (db/connection))
                     current-user
                     [:app/last-updated]))
    ;; Add all games of the player and their friends
    (doseq [appid (->> (map second (steam/player-friends current-user))
                       (cons current-user)
                       (map (fn [steamid] (steam/player-games steamid)))
                       (apply concat)
                       (map (fn [{:keys [steam/appid]}] appid))
                       set
                       (remove
                        (set (flatten
                              (d/q '[:find ?appid
                                     :where [?e :steam/appid ?appid]]
                                   (d/db (db/connection)))))))]
      (when-let [game (steam/game appid)]
        (d/transact (db/connection) {:tx-data [game]})))
    ;; Add all players and their games
    (doseq [steamid (->> (map second (steam/player-friends current-user))
                         set
                         (remove
                          (set (flatten
                                (d/q '[:find ?steamid
                                       :where [_ :steam/id ?steamid]]
                                     (d/db (db/connection)))))))]
      (when-let [player (steam/player-by-id steamid)]
        (prn :player steamid)
        (d/transact (db/connection)
                    {:tx-data [(assoc player
                                      :steam/games
                                      (steam/player-games steamid))]})))
    ;; Add current-user
    (let [player (steam/player-by-id current-user)]
      (d/transact (db/connection)
                  {:tx-data [(-> player
                                 (assoc :steam/friends
                                        (steam/player-friends current-user))
                                 (assoc :steam/games
                                        (steam/player-games current-user))
                                 (assoc :app/last-updated (Date.)))]}))))
