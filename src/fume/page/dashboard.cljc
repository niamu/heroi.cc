(ns fume.page.dashboard
  (:require [fume.page :refer [hexagon-img header player checkbox]]
            [fume.routes :refer [routes]]
            [fume.util :as util]
            [fume.steam :as steam]
            [domkm.silk :as silk]
            [om.next :as om #?(:clj :refer :cljs :refer-macros) [ui]]))

(defn Dashboard
  #?(:clj  [req]
     :cljs [])
  (ui
   static om/IQuery
   (query [this]
          [:app/title
           `({:current/user [:player/steamid
                             :player/personaname
                             :player/realname
                             :player/personastate
                             :player/avatarfull
                             :player/avatarmedium
                             :player/communityvisibilitystate
                             :player/gamescount
                             {:player/friends 1}]}
             {:player/steamid
              ~#?(:clj (get-in req [:session
                                    :cemerick.friend/identity
                                    :current])
                  :cljs (.. (.getElementById js/document "steamid")
                            -textContent))})])
   Object
   (render [this]
           (let [{:keys [app/title
                         current/user]} (om/props this)]
             (-> [:div.content
                  (header [:nav
                           [:div.breadcrumbs
                            [:a {:href (silk/depart routes :dashboard)}
                             [:h1 {:data-title title} title]]
                            [:span "Dashboard"]]
                           [:a {:href (silk/depart routes :logout)} "Logout"]]
                          [:div.user
                           (hexagon-img {:size 150
                                         :src (:player/avatarfull user)
                                         :alt (:player/personaname user)
                                         :border-color "#FFF"})
                           [:div.meta
                            [:h2 (:player/personaname user)]
                            (when (:player/realname user)
                              [:h3 (:player/realname user)])
                            [:ul
                             [:li
                              [:strong (:player/gamescount user)]
                              [:span (util/plural (:player/gamescount user)
                                                  " game")]]
                             [:li
                              [:strong (count (:player/friends user))]
                              [:span (util/plural (count (:player/friends user))
                                                  " friend")]]]]])
                  [:form.friends.container
                   {:action (silk/depart routes :common-games)
                    :method "get"}
                   [:h2 "Select Friends"]
                   [:p "Find common games with selected friends and yourself."]
                   [:div.players
                    (map (fn [p]
                           (checkbox {:name "player"
                                      :disabled? (= :private
                                                    (steam/player-state p))
                                      :value (:player/steamid p)
                                      :component (player p)}))
                         (sort-by #((into {} (map-indexed (fn [i e] [e i])
                                                          [:playing
                                                           :looking-to-play
                                                           :looking-to-trade
                                                           :online
                                                           :away
                                                           :busy
                                                           :snooze
                                                           :offline
                                                           :private]))
                                    (steam/player-state %))
                                  (:player/friends user)))]
                   [:input {:type "hidden"
                            :name "player"
                            :value (:player/steamid user)}]
                   [:button.button {:type "submit"} "Find Common Games"]]]
                 util/dom)))))
