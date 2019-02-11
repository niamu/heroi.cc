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
    [:app/current-user
     `({:steam/player ~(om/get-query common/Player)}
       {:steam/id :app/current-user})])
  Object
  (render [this]
    (let [{:keys [app/current-user steam/player] :as props} (om/props this)]
      (sablono/html
       [:div
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
                (->> (:steam/friends player)
                     (map #(assoc % :steam/id (get-in % [:steam/id :db/id])))
                     (sort-by :steam/id)))]
          [:input {:type "hidden"
                   :name "steamid"
                   :value current-user}]
          [:button.button {:type "submit"} "Find Common Games"]]]
        [:script#current-user {:type "text/plain"} current-user]]))))
