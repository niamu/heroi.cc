(ns heroicc.web.games
  (:require
   [clojure.set :as set]
   [domkm.silk :as silk]
   [heroicc.web.common :as common]
   [heroicc.web.routes :as routes]
   [heroicc.web.style :as style]
   [om.next :as om]
   [sablono.core :as sablono]))

(om/defui Games
  static om/IQuery
  (query [_]
    [:app/title
     :app/current-user
     :app/search
     :app/categories
     {:app/players [:steam/id
                    :steam/player-name
                    :steam/avatar
                    :steam/url]}
     `{:app/games ~(om/get-query common/Game)}
     `({:steam/player ~(om/get-query common/Player)}
       {:steam/id :app/current-user})])
  Object
  (render [this]
    (let [{:keys [app/title
                  app/current-user
                  app/search
                  app/categories
                  app/players
                  app/games
                  steam/player] :as props} (om/props this)]
      (sablono/html
       [:div
        (common/header
         [:h1 {:data-title title}
          [:a {:href (silk/depart routes/routes :dashboard)}
           title]]
         [:h2
          [:span   "Reveal "]
          [:strong "common games"]
          [:span   " with "]
          [:strong "Steam"]
          [:span   " friends."]])
        [:div.container
         [:div.user
          [:a {:href (:steam/url player)}
           (common/hexagon-img {:size 128
                                :src (:steam/avatar player)
                                :alt (:steam/player-name player)
                                :border-color (style/theme 2)})]
          [:h2 [:a {:href (:steam/url player)} (:steam/player-name player)]]]
         [:div.common-players
          (map common/player (remove #(= (:steam/id %) current-user) players))]
         [:form.games.container
          {:action (silk/depart routes/routes :games)
           :method "get"}
          [:h2 "Filter Games"]
          [:p "Find common games with selected friends and yourself."]
          [:input {:type "text"
                   :name "search"
                   :value (or search "")
                   :placeholder "Search game titles..."}]
          [:ul.tags (map (fn [{:keys [category/description category/id]}]
                           [:li.tag
                            (common/checkbox
                             {:name "category"
                              :value id
                              :checked? (contains? (set categories) (str id))
                              :component [:span description]})])
                         (->> (map (comp set :steam/categories) games)
                              (apply set/union)
                              (map #(assoc % :category/id
                                           (get-in % [:category/id :db/id])))
                              (sort-by :category/description)))]
          (map (fn [p]
                 [:input {:type "hidden"
                          :name "steamid"
                          :value (:steam/id p)}])
               players)
          [:button.button {:type "submit"} "Filter Games"]]
         [:div.games (map common/game games)]]
        [:script#current-user {:type "text/plain"} current-user]]))))
