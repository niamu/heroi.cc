(ns fume.page.login
  (:require [fume.page :refer [header]]
            [fume.util :as util]
            [om.next :as om #?(:clj :refer :cljs :refer-macros) [ui]]))

(defn Login
  #?(:clj  [req]
     :cljs [])
  (ui
   static om/IQuery
   (query [this]
          [:app/title])
   Object
   (render [this]
           (let [{:keys [app/title]} (om/props this)]
             (-> [:div
                  (header [:h1 {:data-title title} title]
                          [:h2
                           [:span   "Reveal "]
                           [:strong "common games"]
                           [:span   " with "]
                           [:strong "Steam"]
                           [:span   " friends."]])
                  [:div.container
                   [:form.login.element {:action "/openid" :method "post"}
                    [:input {:type "hidden"
                             :name "identifier"
                             :value "http://steamcommunity.com/openid"}]
                    [:button.button "Login with Steam"]]]]
                 util/dom)))))
