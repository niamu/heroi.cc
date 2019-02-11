(ns heroicc.web.error
  (:require
   [domkm.silk :as silk]
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
       [:div.container
        [:h2 {:style {:padding "4em"
                      :text-align "center"}} "Error"]]))))
