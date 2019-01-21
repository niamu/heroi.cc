(ns heroicc.web.login
  (:require
   [domkm.silk :as silk]
   [heroicc.web.common :as common]
   [heroicc.web.routes :as routes]
   [om.next :as om]
   [sablono.core :as sablono]))

(om/defui Login
  static om/IQuery
  (query [this]
    [:app/title :app/host])
  Object
  (render [this]
    (let [{:keys [app/title app/host]} (om/props this)]
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
         [:form.login.element {:action "https://steamcommunity.com/openid/login"
                               :method "post"}
          [:input {:type "hidden"
                   :name "openid.ns"
                   :value "http://specs.openid.net/auth/2.0"}]
          [:input {:type "hidden"
                   :name "openid.mode"
                   :value "checkid_setup"}]
          [:input {:type "hidden"
                   :name "openid.return_to"
                   :value (str host (silk/depart routes/routes :dashboard))}]
          [:input {:type "hidden"
                   :name "openid.realm"
                   :value (str host (silk/depart routes/routes :dashboard))}]
          [:input {:type "hidden"
                   :name "openid.identity"
                   :value "http://specs.openid.net/auth/2.0/identifier_select"}]
          [:input {:type "hidden"
                   :name "openid.claimed_id"
                   :value "http://specs.openid.net/auth/2.0/identifier_select"}]
          [:button.button "Login with Steam"]]]]))))
