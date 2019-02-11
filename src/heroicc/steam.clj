(ns heroicc.steam
  (:require
   [clojure.data.json :as json]
   [clojure.core.memoize :as memoize]
   [clojure.set :as set]
   [clojure.string :as string]
   [datomic.ion :as ion]
   [heroicc.config :refer [config]]
   [org.httpkit.client :as http]))

(def api-key
  (get (ion/get-params {:path "/datomic-shared/" (:app-name config) "/"})
       (str (:app-name config) "/steam-api-key")))

(def key-fn (comp keyword #(clojure.string/replace % \_ \-)))

(defn steam-api*
  [path params]
  (let [response @(http/get (str "http://api.steampowered.com" path)
                            {:query-params (merge {:key api-key
                                                   :format "json"}
                                                  params)})]
    (when (= 200 (:status response))
      (json/read-str (:body response) :key-fn key-fn))))

(defn steam-store*
  [path params]
  (let [response @(http/get (str "http://store.steampowered.com/api" path)
                            {:query-params params})]
    (when (= 200 (:status response))
      (get-in (json/read-str (:body response) :key-fn key-fn)
              [(keyword (str (:appids params))) :data]))))

(def steam-api
  (memoize/ttl steam-api* :ttl/threshold (* 12 60 60 1000)))

(def steam-store
  (memoize/ttl steam-store* :ttl/threshold (* 12 60 60 1000)))

(defn game
  [appid]
  (let [game (steam-store "/appdetails"
                          {:appids appid
                           :filters (string/join #"," ["basic"
                                                       "categories"
                                                       "platforms"])})]
    (if (= (:steam-appid game) appid)
      (-> game
          (select-keys [:steam-appid
                        :header-image
                        :categories
                        :name
                        :platforms])
          (update-in [:categories]
                     #(mapv (fn [{:keys [id description]}]
                              {:category/id id
                               :category/description description}) %))
          (update-in [:platforms]
                     (fn [platforms]
                       (->> (filter val platforms)
                            (map #(keyword "platform" (name (key %))))
                            set)))
          (set/rename-keys {:steam-appid  :steam/appid
                            :name         :steam/game-name
                            :header-image :steam/image
                            :categories   :steam/categories
                            :platforms    :steam/platforms}))
      {:steam/appid appid})))

(defn player-by-id
  [steamid]
  (some-> (steam-api "/ISteamUser/GetPlayerSummaries/v0002"
                     {:steamids steamid})
          :response :players first
          (select-keys [:steamid
                        :personaname
                        :avatarfull
                        :profileurl
                        :communityvisibilitystate])
          (update-in [:steamid] #(Long/parseLong  %))
          (update-in [:communityvisibilitystate] #(>= 3 %))
          (set/rename-keys {:steamid                   :steam/id
                            :personaname               :steam/player-name
                            :avatarfull                :steam/avatar
                            :profileurl                :steam/url
                            :communityvisibilitystate  :steam/public?})))

(defn vanity-url->steamid
  [url]
  (get-in (steam-api "/ISteamUser/ResolveVanityURL/v0001"
                     {:vanityurl url})
          [:response :steamid]))

(defn player
  [url]
  (let [[_ id steamid] (re-matches #".*\.com/(\w*)/(\w*)[/]?.*" url)]
    (cond-> steamid
      (= id "id") ((comp player-by-id vanity-url->steamid))
      (= id "profiles") player-by-id)))

(defn player-games
  [steamid]
  (some->> (get-in (steam-api "/IPlayerService/GetOwnedGames/v0001"
                              {:steamid steamid
                               :include_played_free_games 1})
                   [:response :games] [])
           (mapv (comp #(set/rename-keys % {:appid            :steam/appid
                                            :playtime-forever :steam/playtime})
                       #(select-keys % [:appid :playtime-forever])))))

(defn player-friends
  [steamid]
  (some->> (get-in (steam-api "/ISteamUser/GetFriendList/v0001"
                              {:steamid steamid
                               :relationship "friend"})
                   [:friendslist :friends])
           (mapv (fn [{:keys [steamid]}] [:steam/id (Long/parseLong steamid)]))))
