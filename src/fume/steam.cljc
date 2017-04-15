(ns fume.steam
  "Utilities for querying Steam."
  (:require #?@(:clj [[fume.config :refer [config]]
                      [org.httpkit.client :as http]
                      [clojure.data.json :as json]
                      [clojure.core.memoize :as memo]])
            [clojure.set :refer [difference intersection]]))

(defn url
  [domain & [paths]]
  (str "http://" domain "/"
       (apply str (interpose "/" paths))))

(defn steam
  [& paths]
  (url "api.steampowered.com" paths))

(defn steam-store
  [& paths]
  (url "store.steampowered.com/api" paths))

#?(:clj
   (defn- request
     [url params]
     (prn url params)
     (let [response @(http/get url {:query-params params})]
       (when (= 200 (:status response))
         (-> response
             :body
             (json/read-str :key-fn keyword))))))

#?(:clj
   (defn game*
     "Given appid of game, return game data"
     [appid]
     (let [game (-> (request (steam-store "appdetails")
                             {:appids appid
                              :filters (clojure.string/join #","
                                                            ["basic"
                                                             "categories"
                                                             "platforms"])})
                    first second :data)]
       (when (:steam_appid game) game))))

#?(:clj
   (defn games-of*
     "Return all owned games of player with given steamid"
     [steamid]
     (-> (request (steam "IPlayerService" "GetOwnedGames" "v0001")
                  {:key (:steam-apikey config)
                   :steamid steamid
                   :include_played_free_games 1
                   :format "json"})
         :response :games)))

#?(:clj
   (defn wishlist-of*
     "Return all games on the wishlist of player with given steamid"
     [steamid]
     (->> (http/get (url "steamcommunity.com"
                         ["profiles" steamid "wishlist"]))
          deref str
          (re-seq #"game_([0-9]+)")
          (map (fn [game] (second game))))))

#?(:clj
   (defn friends-of*
     "Return all friends of player with given steamid"
     [steamid]
     (->> (request (steam "ISteamUser" "GetFriendList" "v0001")
                   {:key (:steam-apikey config)
                    :steamid steamid
                    :relationship "friend"
                    :format "json"})
          :friendslist :friends
          (map :steamid))))

#?(:clj
   (defn player*
     "Return player data of given steamid"
     [steamid]
     (-> (request (steam "ISteamUser" "GetPlayerSummaries" "v0002")
                  {:key (:steam-apikey config)
                   :steamids steamid
                   :format "json"})
         :response :players first)))

#?(:clj
   (defmacro defcache
     [query-fn]
     `(def ~query-fn
        (memo/memo ~(symbol (str query-fn "*"))))))

#?(:clj
   (defmacro defttl
     [query-fn ttl]
     `(def ~query-fn
        (memo/ttl ~(symbol (str query-fn "*"))
                  :ttl/threshold (* 1 (* 60 1000))))))

;; Caching wrappers
#?(:clj (defttl player 1))
#?(:clj (defcache game))
#?(:clj (defcache games-of))
#?(:clj (defcache wishlist-of))
#?(:clj (defcache friends-of))

(defn player-state
  "Given a player map, return the current state as a keyword"
  [{:keys [player/gameid
           player/personastate
           player/communityvisibilitystate] :as player}]
  (cond
    gameid :playing
    (= 1 communityvisibilitystate) :private
    :else (condp = personastate
            0 :offline
            1 :online
            2 :busy
            3 :away
            4 :snooze
            5 :looking-to-trade
            6 :looking-to-play
            :error)))

#_(defn common-games-not-owned
    "Common games owned by everyone but me"
    [me friends]
    (when (not-empty friends)
      (difference (common-games-between friends)
                  (into #{} (map :appid (games-of me))))))
