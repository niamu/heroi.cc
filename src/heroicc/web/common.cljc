(ns heroicc.web.common
  (:require [clojure.string :as string]
            [garden.color :as color]
            [heroicc.web.style :as style]
            [om.next :as om]
            [sablono.core :as sablono])
  #?(:clj (:import [java.time LocalDate])))

(defn title
  "Returns a formatted title
  i.e. \"Login - Heroicc\""
  [subtitle]
  (cond->> "Heroicc" (not-empty subtitle) (str subtitle " - ")))

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
  [{:keys [size src alt border-color border-color-selected]
    :or {border-color "#FFF"}}]
  (let [border? (not (false? border-color))
        border-width (int (/ size 10))
        uniq (gensym size)]
    [:svg
     {:xmlns "http://www.w3.org/2000/svg"
      :version "1.1"
      :role "img"
      :width size
      :height size
      :style (when border?
               {:filter (str "drop-shadow( 1px 1px " (/ border-width 2) "px "
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
      (when border-color-selected
        [:linearGradient {:id (str "gradient_selected_" uniq)
                          :x1 0
                          :x2 1
                          :y1 0
                          :y2 1}
         [:stop {:offset "0%" :stopColor border-color-selected}]
         [:stop {:offset "100%" :stopColor (-> border-color-selected
                                               (color/darken 10)
                                               color/as-hex)}]])
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
     (when border-color-selected
       [:rect.checked
        {:x 0
         :y 0
         :width size
         :height size
         :fill (str "url(#gradient_selected_" uniq ")")
         :clipPath (str "url(#border_" uniq ")")}])
     (when src
       [:image {:xlinkHref src
                :x (cond-> 0 border? (+ border-width))
                :y (cond-> 0 border? (+ border-width))
                :width (str (cond-> size border? (- (* border-width 2))) "px")
                :height (str (cond-> size border? (- (* border-width 2))) "px")
                :clipPath (str "url(#clip_" uniq ")")}])]))

(om/defui Checkbox
  Object
  (render [this]
    (let [{:keys [component disabled? checked? name value]} (om/props this)
          id (hash (str name value))]
      (sablono/html
       [:div.checkbox-wrapper
        [:input {:type "checkbox"
                 :disabled disabled?
                 :checked checked?
                 :name name
                 :id (str "checkbox_" id)
                 :value value}]
        [:label {:for (str "checkbox_" id)} component]]))))

(def checkbox (om/factory Checkbox))

(om/defui Player
  static om/IQuery
  (query [_]
    [:steam/id
     :steam/player-name
     :steam/avatar
     :steam/url
     :steam/public?
     {:steam/friends [:steam/id
                      :steam/player-name
                      :steam/avatar
                      :steam/url
                      :steam/public?]}])
  Object
  (render [this]
    (let [{:keys [steam/id
                  steam/player-name
                  steam/avatar
                  steam/url
                  steam/public?
                  steam/friends]} (om/props this)]
      (sablono/html
       [:div.player.element
        [:a {:href url}
         (hexagon-img {:size 64
                       :src avatar
                       :alt player-name
                       :border-color (style/theme 2)
                       :border-color-selected (style/theme 1)})]
        [:div.info
         [:h3 [:span [:a {:href url} player-name]]]]]))))

(def player (om/factory Player))

(om/defui Game
  static om/IQuery
  (query [_]
    [:steam/appid
     :steam/game-name
     :steam/image
     {:steam/categories [:category/id :category/description]}
     :steam/platforms])
  Object
  (render [this]
    (let [{:keys [steam/appid
                  steam/game-name
                  steam/image
                  steam/categories
                  steam/platforms]} (om/props this)]
      (sablono/html
       [:div.game.element
        [:a {:href (str "http://steampowered.com/app/" appid)}
         [:img {:src image :alt game-name}]]
        [:h4 game-name]
        [:ul.platforms
         (map (fn [platform]
                [:li platform])
              (map (comp string/capitalize name) platforms))]
        [:ul.tags
         (map (fn [c] [:li.tag (:category/description c)]) categories)]]))))

(def game (om/factory Game))
