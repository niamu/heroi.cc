(ns heroicc.web.dashboard
  (:require
   [domkm.silk :as silk]
   [heroicc.web.common :as common]
   [heroicc.web.routes :as routes]
   [heroicc.web.style :as style]
   [om.next :as om]
   [sablono.core :as sablono]))

(om/defui Dashboard
  static om/IQuery
  (query [_]
    [:app/title
     :app/current-user
     `({:steam/player ~(om/get-query common/Player)}
       {:steam/id :app/current-user})])
  Object
  (render [this]
    (let [{:keys [app/title
                  app/current-user
                  steam/player] :as props} (om/props this)]
      (sablono/html
       [:div
        (common/header
         [:h1 {:data-title title}
          [:a {:href (str (silk/depart routes/routes :dashboard)
                          "?"
                          (silk/encode-query
                           {"openid.identity"
                            (str "https://steamcommunity.com/openid/id/"
                                 current-user)}))}
           title]]
         [:h2
          [:span   "Reveal "]
          [:strong "common games"]
          [:span   " with "]
          [:strong "Steam"]
          [:span   " friends."]])
        [:div.container
         [:div.user
          (common/hexagon-img {:size 128
                               :src (:steam/avatar player)
                               :alt (:steam/player-name player)
                               :border-color (style/theme 2)})
          [:h2 (:steam/player-name player)]]
         [:form.friends.container
          {:action (silk/depart routes/routes :games)
           :method "get"}
          [:h2 "Select Friends"]
          [:p "Find common games with selected friends and yourself."]
          [:div.players
           (map (fn [{:keys [steam/public? steam/id] :as p}]
                  (common/checkbox {:name "steamid"
                                    :disabled? (not public?)
                                    :value id
                                    :component (common/player p)}))
                (map #(assoc % :steam/id (get-in % [:steam/id :db/id]))
                     (:steam/friends player)))]
          [:input {:type "hidden"
                   :name "steamid"
                   :value current-user}]
          [:input {:type "hidden"
                   :name "openid.identity"
                   :value (str "https://steamcommunity.com/openid/id/"
                               current-user)}]
          [:button.button {:type "submit"} "Find Common Games"]]]
        [:script#current-user {:type "text/plain"} current-user]]))))
