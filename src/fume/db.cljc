(ns fume.db
  "Utilities for DB schema and operations"
  (:refer-clojure :exclude [import])
  (:require [fume.util :as util]
            [fume.steam :as steam]
            [datascript.core :as d]))

(def schema
  {:app/title         {:db/unique :db.unique/identity}
   :search/query      {:db/unique :db.unique/identity}
   :random/screenshot {:db/unique :db.unique/identity}
   :current/user      {:db/unique :db.unique/identity
                       :db/cardinality :db.cardinality/one
                       :db/valueType :db.type/ref}
   :player/steamid    {:db/unique :db.unique/identity}
   :player/friends    {:db/cardinality :db.cardinality/many
                       :db/valueType :db.type/ref}
   :player/games      {:db/cardinality :db.cardinality/many
                       :db/valueType :db.type/ref}
   :game/steam_appid  {:db/unique :db.unique/identity}
   :game/players      {:db/cardinality :db.cardinality/many
                       :db/valueType :db.type/ref}})

(defonce conn (d/create-conn schema))

(defn not-exists?
  "Returns boolean whether or not an entity exists in the DB"
  [entity]
  (nil? (d/entity (d/db conn) entity)))

(defn import-player
  "Given a map, import player data to the DB"
  [player]
  (doseq [p (:player/friends player)]
    (d/transact! conn [p]))
  (d/transact! conn
               [(assoc player
                       :player/friends
                       (map #(vector :player/steamid
                                     (:player/steamid %))
                            (:player/friends player)))]))

(defn import
  "Given a map of data, import that to the DB"
  [m]
  (doseq [[k v] m]
    (cond
      (every? #(util/in-namespace? % :player) v) (doall (map import-player v))
      (= :common/games k) (doseq [game v] (d/transact! conn [game]))
      (= :current/user k)
      (do (import-player v)
          (d/transact! conn
                       [{:current/user [:player/steamid (:player/steamid v)]}]))
      :else (d/transact! conn [{k v}]))))

#?(:clj
   (defn add-game
     "Add a game to the DB with relationship to the player/owner"
     [appid steamid]
     (let [game (steam/game appid)]
       (when (and (not-exists? [:game/steam_appid appid])
                  (not (nil? game)))
         (d/transact! conn [(-> game
                                (dissoc :website) ;; Because nil values fail
                                (util/namespace-map :game))]))
       (when-not (nil? game)
         (d/transact! conn [{:game/steam_appid appid
                             :game/players [:player/steamid steamid]}])))))

#?(:clj
   (defn add-player
     "Add a player and their games (optionally with friends) to the DB"
     ([steamid]
      (add-player steamid {:friends? false}))
     ([steamid {:keys [friends?] :or {:friends? false}}]
      (when (not-exists? [:player/steamid steamid])
        (let [player (-> (steam/player steamid)
                         (util/namespace-map :player))
              games (map :appid (steam/games-of steamid))
              actual-games (->> games
                                (map #(vector :game/steam_appid %))
                                (remove not-exists?))]
          (d/transact! conn [player])
          (doseq [appid games]
            (add-game appid steamid))
          (d/transact! conn [{:player/steamid steamid
                              :player/gamescount (count games)
                              :player/games actual-games}])))
      (when friends?
        (let [friends (steam/friends-of steamid)]
          (when (->> [:player/steamid steamid]
                     (d/entity (d/db conn))
                     :player/friends
                     empty?)
            (doseq [fid friends] (add-player fid)))
          (d/transact! conn [{:player/steamid steamid
                              :player/friends
                              (map #(vector :player/steamid %) friends)}]))))))

#?(:clj
   (defn init-db
     []
     (d/transact! conn [{:app/title "Heroicc"}])))
