(ns heroicc.web.error
  (:require
   [domkm.silk :as silk]
   [heroicc.web.common :as common]
   [heroicc.web.routes :as routes]
   [om.next :as om]
   [sablono.core :as sablono]))

(om/defui ErrorPage
  static om/IQuery
  (query [this]
    [:app/title])
  Object
  (render [this]
    (let [{:keys [app/title]} (om/props this)]
      (sablono/html
       [:div
        (common/header [:h1 {:data-title title}
                        [:a {:href (silk/depart routes/routes :dashboard)}
                         title]]
                       [:h2
                        [:span   "Reveal "]
                        [:strong "common games"]
                        [:span   " with "]
                        [:strong "Steam"]
                        [:span   " friends."]])
        [:div.container
         [:h2 {:style {:padding "4em"
                       :text-align "center"}} "Error"]]]))))
