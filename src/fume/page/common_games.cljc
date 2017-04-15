(ns fume.page.common-games
  (:require [fume.page :refer [hexagon-img header Game game Player]]
            [fume.routes :refer [routes]]
            [fume.util :as util]
            [fume.steam :as steam]
            [fume.db :as db]
            [clojure.string :as string]
            [domkm.silk :as silk]
            [datascript.core :as d]
            [om.next :as om #?(:clj :refer :cljs :refer-macros) [ui]]))

(defn CommonGames
  #?(:clj  [req]
     :cljs [])
  (ui
   static om/IQuery
   (query [this]
          (let [query-params (-> #?(:clj (-> req :query-params)
                                    :cljs (.. js/location -search))
                                 util/query->map
                                 (util/coll-keys :player :category))
                steamid #?(:clj (get-in req [:session
                                             :cemerick.friend/identity
                                             :current])
                           :cljs (.. (.getElementById js/document "steamid")
                                     -textContent))]
            [:app/title
             :search/search
             `({:common/games ~(om/get-query Game)} ~query-params)
             `({:players ~(om/get-query Player)} ~query-params)
             `({:current/user [:player/steamid
                               :player/personaname
                               :player/personastate
                               :player/avatarfull
                               {:player/friends 1}
                               :player/avatarmedium]}
               {:player/steamid ~steamid})]))
   Object
   (render [this]
           (let [{:keys [app/title
                         search/search
                         common/games
                         players
                         current/user]} (om/props this)
                 search (or search "")
                 games (filter (fn [game]
                                 (or (string/includes?
                                      (string/lower-case (:game/name game))
                                      (string/lower-case search))
                                     (some
                                      (fn [t]
                                        (string/includes?
                                         (string/lower-case (:description t))
                                         (string/lower-case search)))
                                      (:game/categories game))))
                               games)
                 friends (remove (fn [p] (= (:player/steamid user)
                                            (:player/steamid p)))
                                 players)]
             (-> [:div
                  (header [:nav
                           [:div.breadcrumbs
                            [:a {:href (silk/depart routes :dashboard)}
                             [:h1 {:data-title title} title]]
                            [:span "Common Games"]]
                           [:a {:href (silk/depart routes :logout)} "Logout"]]
                          (hexagon-img {:size 150
                                        :src (:player/avatarfull user)
                                        :alt (:player/personaname user)
                                        :border-color "#FFF"})
                          [:div.user_common
                           [:h2
                            [:strong (count games)]
                            [:span (util/plural (count games) " game")]
                            [:span " in common with "]
                            [:strong (count friends)]
                            [:span (util/plural (count friends) " friend")]]
                           [:ul.players
                            (map (fn [p]
                                   [:li.player
                                    (hexagon-img {:size 48
                                                  :src
                                                  (:player/avatarmedium p)
                                                  :alt (:player/personaname p)
                                                  :border-color
                                                  (condp =
                                                      (steam/player-state p)
                                                    :playing "#98BF66"
                                                    :offline "#6A6A6A"
                                                    :online "#77A6C8"
                                                    "#77A6C8")})])
                                 (sort-by :player/steamid friends))]])
                  [:form.common.container
                   {:action (silk/depart routes :common-games)
                    :method "get"}
                   [:input {:type "search"
                            :name "filter"
                            :autoComplete "off"
                            :autoCorrect "off"
                            :autoCapitalize "off"
                            :spellCheck "false"
                            :placeholder "Filter games by tag or name"
                            :value search
                            :onChange
                            #?(:cljs (fn [e]
                                       (d/transact db/conn
                                                   [{:search/query :games
                                                     :search/search
                                                     (.. e -target -value)}]))
                               :clj nil)}]
                   [:input {:type "hidden"
                            :name "player"
                            :value (:player/steamid user)}]
                   [:div
                    (map (fn [player]
                           [:input {:type "hidden"
                                    :name "player"
                                    :value (:player/steamid player)}])
                         friends)]
                   [:button.button "Filter games"]]
                  [:div.games.container
                   (map game
                        (->> games
                             (map #(assoc % :steamids-of/friends
                                          (set
                                           (conj (map :player/steamid
                                                      (:player/friends user))
                                                 (:player/steamid user)))))
                             (sort-by :games/steam_appid)))]]
                 util/dom)))))
