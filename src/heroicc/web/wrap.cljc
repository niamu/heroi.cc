(ns heroicc.web.wrap
  (:require
   [binnacle.core :as binnacle]
   [clojure.string :as string]
   [domkm.silk :as silk]
   [heroicc.web.common :as common]
   [heroicc.web.routes :as routes]
   [heroicc.web.style :as style]
   [hiccup.page :as h])
  #?(:clj (:import [java.time LocalDate])))

#?(:clj
   (defn wrap
     "Server-side wrapping of a React Root in HTML markup"
     [route body]
     (h/html5
      [:head
       [:title (common/title (->> (-> route name (string/split #"-"))
                                  (map string/capitalize)
                                  (string/join " ")))]
       [:link {:rel "apple-touch-icon" :sizes "180x180"
               :href "/apple-touch-icon.png"}]
       [:link {:rel "icon" :type "image/png" :sizes "32x32"
               :href "/favicon-32x32.png"}]
       [:link {:rel "icon" :type "image/png" :sizes "16x16"
               :href "/favicon-16x16.png"}]
       [:link {:rel "mask-icon" :color (style/theme 1)
               :href "/safari-pinned-tab.svg"}]
       [:meta {:name "apple-mobile-web-app-capable" :content "yes"}]
       [:meta {:name "apple-mobile-web-app-status-bar-style"
               :content "black-translucent"}]
       [:meta {:name "apple-mobile-web-app-title" :content "Heroicc"}]
       [:meta {:name "application-name" :content "Heroicc"}]
       [:meta {:name "theme-color"
               :content (style/theme 1)}]
       [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
       (h/include-css (silk/depart routes/routes :stylesheet))]
      [:body
       [:div#app body]
       [:hr]
       [:footer.container
        [:ul.element
         [:li.brendonwalsh
          [:a {:href "http://brendonwalsh.me"
               :title "Brendon Walsh"}
           [:img {:src "/images/brendonwalsh.jpg"
                  :alt "Brendon Walsh"}]]]
         [:li.steam
          [:a {:href "http://steampowered.com"
               :title "Powered by Steam"}
           (get-in style/assets [:assets
                                 :images
                                 :steam.svg])]]]
        [:small
         [:span (str "&copy; " (.getYear (LocalDate/now)))]]]
       #_(when-not (= route :login)
           (h/include-js "/js/heroicc.js"))])))
