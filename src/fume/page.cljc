(ns fume.page
  "Page assets shared by server and client."
  (:require [binnacle.core :as binnacle]
            [fume.style :as style]
            [fume.steam :as steam]
            [fume.state :as state]
            [fume.util :as util]
            [clojure.string :as string]
            [garden.color :as color]
            [om.next :as om #?(:clj :refer :cljs :refer-macros) [defui]]
            [om.dom :as dom :refer [render-to-str]]
            #?@(:clj [[hiccup.page :as h]
                      [clj-time.core :as t]]
                :cljs [[cljs-time.core :as t]])))

(defn react-root
  "Add a React UI component as a root. Client needs to target DOM node."
  #?(:clj  [root-component req]
     :cljs [root-component])
  (om/add-root! state/reconciler
                (root-component #?(:clj req))
                #?(:clj  nil
                   :cljs (.getElementById js/document "app"))))

(defn hexagon-path
  [x y radius]
  (let [pi #?(:clj (Math/PI)
              :cljs (.-PI js/Math))
        pi6th (/ pi 6)
        cos #?(:clj #(Math/cos %)
               :cljs #(.cos js/Math %))
        sin #?(:clj #(Math/sin %)
               :cljs #(.sin js/Math %))
        c (* (cos pi6th) radius)
        s (* (sin pi6th) radius)]
    (str "M" (int x) " " (- y radius)
         " L" (int (+ x c)) " " (int (- y s))
         " L" (int (+ x c)) " " (int (+ y s))
         " L" (int x) " " (+ y radius)
         " L" (int (- x c)) " " (int (+ y s))
         " L" (int (- x c)) " " (int (- y s)) "z")))

(defn hexagon-img
  [{:keys [size src alt border-color]
    :or {border-color "#FFF"}}]
  (let [border? (not (false? border-color))
        border-width (int (/ (/ size 14) 2))
        uniq #?(:clj size :cljs (gensym size))]
    [:svg
     {:xmlns "http://www.w3.org/2000/svg"
      :version "1.1"
      :role "img"
      :width size
      :height size
      :style (when border?
               {:filter (str "drop-shadow( 1px 1px " (+ border-width 2) "px "
                             "rgba(0, 0, 0, 0.5) )")})}
     [:title alt]
     [:defs
      [:linearGradient {:id (str "gradient_" uniq)
                        :x1 0
                        :x2 1
                        :y1 0
                        :y2 1}
       [:stop {:offset "0%" :stopColor border-color}]
       [:stop {:offset "100%" :stopColor (-> border-color
                                             (color/darken 10)
                                             color/as-hex)}]]
      [:clipPath {:id (str "clip_" uniq)}
       [:path {:d (hexagon-path (/ size 2)
                                (/ size 2)
                                (cond-> (/ size 2)
                                  border? (- border-width)))}]]
      [:clipPath {:id (str "border_" uniq)}
       [:path {:d (hexagon-path (/ size 2)
                                (/ size 2)
                                (/ size 2))}]]]
     [:rect
      {:x 0
       :y 0
       :width size
       :height size
       :fill (if border-color (str "url(#gradient_" uniq ")")
                 "#FFF")
       :clipPath (str "url(#border_" uniq ")")}]
     (when src
       [:image {:xlinkHref src
                :x (cond-> 0 border? (+ border-width))
                :y (cond-> 0 border? (+ border-width))
                :width (str (cond-> size border? (- (* border-width 2))) "px")
                :height (str (cond-> size border? (- (* border-width 2))) "px")
                :clipPath (str "url(#clip_" uniq ")")}])]))

(defn title
  "Returns a formatted title
  i.e. \"Login - Heroi.cc\""
  [subtitle]
  (cond->> "Heroi.cc" (not-empty subtitle) (str subtitle " - ")))

(defn header
  "Returns markup for the header with screenshot and optional children"
  [& children]
  [:header
   {:style {:background-image (str "url(/images/background/"
                                   (t/day (t/today-at-midnight))
                                   ".jpg)")}}
   children])

#?(:clj
   (defn wrap
     "Server-side wrapping of a React Root in HTML markup"
     [req route react-root]
     (h/html5
      [:head
       [:title (title (->> (-> route name (string/split #"-"))
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
       (h/include-css "/css/screen.css")]
      [:body
       [:div#app (render-to-str react-root)]
       [:hr]
       [:footer.container
        [:ul.element
         [:li.brendonwalsh
          [:a {:href "http://brendonwalsh.me"
               :title "Brendon Walsh"}
           [:img {:src (binnacle/data-url style/assets
                                          [:resources
                                           :assets
                                           :images
                                           :brendonwalsh.jpg])
                  :alt "Brendon Walsh"}]]]
         [:li.steam
          [:a {:href "http://steampowered.com"
               :title "Powered by Steam"}
           (get-in style/assets [:resources
                                 :assets
                                 :images
                                 :steam.svg])]]]
        [:small
         [:span (str "&copy; " (t/year (t/today-at-midnight)))]]]
       (when (util/logged-in? req)
         [:script#steamid {:type "text/plain"}
          (str (get-in req [:session
                            :cemerick.friend/identity
                            :current]))])
       (when (util/logged-in? req)
         (h/include-js "/js/fume.js"))])))

(defui Checkbox
  Object
  (render [this]
    (let [{:keys [component disabled? name value]} (om/props this)
          id (hash (str name value))]
      (-> [:div.checkbox-wrapper
           [:input {:type "checkbox"
                    :disabled disabled?
                    :name name
                    :id (str "checkbox_" id)
                    :value value}]
           [:label {:for (str "checkbox_" id)} component]]
          util/dom))))

(def checkbox (om/factory Checkbox))

(declare Player)

(defui Game
  static om/IQuery
  (query [this]
    [:game/steam_appid
     :game/name
     :game/header_image
     :game/categories
     :game/platforms
     `{:game/players ~(om/get-query Player)}])
  Object
  (render [this]
    (let [{:keys [game/steam_appid
                  game/name
                  game/header_image
                  game/categories
                  game/players
                  game/platforms
                  steamids-of/friends]} (om/props this)
          name (when name (string/replace name "'" "’"))]
      (-> [:div.game.element
           [:img {:src header_image :alt name}]
           [:h4 name]
           [:ul.platforms
            (map (fn [platform]
                   [:li platform])
                 (->> platforms
                      (filter (fn [[_ supported?]] supported?))
                      keys
                      (map clojure.core/name)
                      (map string/capitalize)))]
           [:ul.players
            (map (fn [player]
                   [:li.player
                    (hexagon-img {:size 32
                                  :src (:player/avatarmedium player)
                                  :alt (:player/personaname player)
                                  :border-color (condp =
                                                    (steam/player-state player)
                                                  :playing "#98BF66"
                                                  :offline "#6A6A6A"
                                                  :online "#77A6C8"
                                                  "#77A6C8")})])
                 (->> players
                      (filter (fn [player]
                                (contains? friends (:player/steamid player))))
                      (sort-by :player/steamid)))]
           [:ul.tags
            (map (fn [c] [:li.tag (:description c)])
                 categories)]]
          util/dom))))

(def game (om/factory Game))

(defui Player
  static om/IQuery
  (query [this]
    [:player/steamid
     :player/personaname
     :player/realname
     :player/personastate
     :player/avatarmedium
     :player/communityvisibilitystate
     :player/gamescount])
  Object
  (render [this]
    (let [{:keys [player/steamid
                  player/personaname
                  player/realname
                  player/gamescount
                  player/personastate
                  player/communityvisibilitystate
                  player/avatarmedium]
           :as player} (om/props this)]
      (-> [:div.player.element
           (hexagon-img {:size 64
                         :src avatarmedium
                         :alt personaname
                         :border-color (condp = (steam/player-state player)
                                         :playing "#98BF66"
                                         :offline "#6A6A6A"
                                         :private "#6A6A6A"
                                         :online "#77A6C8"
                                         "#77A6C8")})
           [:div.info
            [:h3
             [:span personaname]
             (when realname
               [:span.realname (str " (" realname ")")])]
            [:div.meta
             [:span (name (steam/player-state player))]
             [:span.games
              [:strong (if (= :private (steam/player-state player))
                         "?"
                         gamescount)]
              [:span (util/plural gamescount " game")]]]]]
          util/dom))))

(def player (om/factory Player))
