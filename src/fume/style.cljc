(ns fume.style
  "Utilities to generate CSS"
  (:require [binnacle.core :as binnacle]
            [garden.color :as color]
            [garden.core :refer [style css]]
            [garden.stylesheet :as stylesheet]
            [garden.selectors :as selectors]
            [garden.units :refer [s percent px em vw vh vmin deg]]
            [normalize.core :refer [normalize]]
            [clojure.string :as string])
  #?(:cljs (:require-macros
            [fume.style :refer [assets* defbreakpoint]])))

#?(:clj
   (defmacro assets*
     [path]
     (binnacle/assets path)))

#?(:clj
   (def assets (assets* "resources/assets")))

(def theme
  "Teal and Orange"
  ["#FFFFFF" "#FABD4A" "#2A5769" "#002E40" "#FA9600"])

#?(:clj
   (defmacro defbreakpoint [name media-params]
     `(defn ~name [& rules#]
        (stylesheet/at-media ~media-params
                             [:& rules#]))))

#?(:clj
   (defbreakpoint mobile-screen
     {:screen true
      :max-width (px 480)}))

#?(:clj (defn radial-gradient [c1 c2] (apply (stylesheet/cssfn :radial-gradient)
                                             [[:circle :at 0 0] c1 c2])))
#?(:clj (def linear-gradient (stylesheet/cssfn :linear-gradient)))
#?(:clj (def url (stylesheet/cssfn :url)))
#?(:clj (def attr (stylesheet/cssfn :attr)))
#?(:clj (def calc (stylesheet/cssfn :calc)))
#?(:clj (def rotate (stylesheet/cssfn :rotate)))
#?(:clj (def scale (stylesheet/cssfn :scale)))
#?(:clj (def scaleX (stylesheet/cssfn :scaleX)))
#?(:clj (def grayscale (stylesheet/cssfn :grayscale)))

#?(:clj
   (def fonts
     ^{:doc "Web fonts to embed into CSS"}
     (-> {:font-family "Franchise"
          :src (url (binnacle/data-url assets [:resources
                                               :assets
                                               :fonts
                                               :franchise-bold.woff]))
          :font-weight :normal
          :font-style :normal}
         stylesheet/at-font-face)))

#?(:clj
   (def typography
     "Basic rules for type"
     [[:body {:font [["13px/16px" "Helvetica"] "Arial" "sans-serif"]}]
      [:h1 :h2 :h3 {:line-height (em 1)
                    :margin 0}]
      [:h2 :h3 {:font-weight 300
                :margin [[0 0 (em 0.5)]]}]
      [:h1 {:position :relative
            :font-family "Franchise"
            :font-weight 300
            :opacity 0.75
            :text-transform :uppercase
            :line-height (em 1.2)
            :color (theme 0)}]
      [:h2 {}
       [:strong {:font-weight 600}]]
      [:a {:text-decoration :none
           :color (theme 3)}
       [:&:hover {:color (theme 3)}]]]))

#?(:clj
   (def input
     [(selectors/input (selectors/attr= :type :search))
      (selectors/input (selectors/attr= :type :text))
      {:width (percent 100)
       :border [[(px 1) :solid (-> (theme 0)
                                   (color/darken 10)
                                   color/as-hex)]]
       :padding (em 0.5)}]))

#?(:clj
   (def button
     "Basic rules for buttons"
     [:.button
      {:display :table
       :text-align :center
       :margin [[0 :auto]]
       :border :none
       :color (theme 0)
       :background (radial-gradient (theme 4) (theme 1))
       :padding [[(px 20) (px 25)]]
       :text-transform :uppercase
       :border-radius (px 5)
       :font-weight 600
       :font-size (px 16)
       :box-shadow [[0 (px 2) (px 10) (-> (theme 2)
                                          color/as-rgb
                                          (assoc :alpha 0.3))]]
       :cursor :pointer
       :transition [[:transform :ease-in-out (s 0.05)]
                    [:box-shadow :ease-in-out (s 0.05)]
                    [:background-color :ease-in-out (s 0.05)]]}
      [:&:hover
       {:transform (scale 1.015)
        :box-shadow [[0 (px 2) (px 15) (-> (theme 2)
                                           color/as-rgb
                                           (assoc :alpha 0.2))]]
        :background (radial-gradient (color/lighten (theme 4) 10)
                                     (color/lighten (theme 1) 10))}]
      [:&:active
       {:background (radial-gradient (color/darken (theme 4) 10)
                                     (color/darken (theme 1) 10))
        :transform (scale 0.985)}]
      (mobile-screen
       [:& {:width (percent 100)}])]))

#?(:clj
   (def header
     "Rules for the header look and feel"
     [:header
      {:position :relative
       :padding (em 2)
       :z-index 1
       :text-align :center
       :color (theme 0)
       :background-position [[(percent 50) (percent 15)]]
       :background-size :cover}
      [:&:after :&:before
       {:position :absolute
        :top 0
        :left 0
        :display :block
        :content (attr :nil)
        :height (percent 100)
        :width (percent 100)}]
      [:&:before
       {:filter (grayscale 0.75)
        :background-position :inherit
        :background-size :inherit
        :background-image :inherit
        :z-index -1}]
      [:&:after
       {:opacity 0.85
        :z-index -1
        :background (radial-gradient (theme 2) (theme 1))}]
      [:&>
       [:h1 {:font-size (vmin 32)
             :color (theme 1)}
        [:&:before
         {:position :absolute
          :z-index -1
          :content (attr :data-title)
          :top 0
          :left 0
          :width (percent 100)
          :color (theme 2)
          :letter-spacing (em -0.01)
          :text-shadow [[0 0 (vw 0.5) (theme 2)]]}]]
       [:h2 {:font-size (vmin 3.6)
             :text-shadow [[0 0 (px 3) (theme 2)]]}]]
      [:nav {:display :flex
             :text-align :left
             :text-transform :uppercase
             :font-weight 600
             :padding-bottom (em 1)
             :margin-bottom (em 2)
             :border-bottom [[(px 2) :solid (-> (theme 3)
                                                color/as-rgb
                                                (assoc :alpha 0.1))]]}
       [:div.breadcrumbs {:flex-grow 1}]
       [(selectors/- :div.breadcrumbs :a) {:align-self :center}]
       [:h1 {:font-size (em 3)
             :vertical-align :baseline
             :line-height (em 1)
             :display :inline}]
       [:span {:opacity 0.75
               :font-weight 400
               :text-transform :uppercase}]]]))

#?(:clj
   (def footer
     "Rules for the footer look and feel"
     [:footer
      {:color (theme 3)
       :text-align :center}
      [:ul {:list-style :none
            :margin 0
            :padding 0}
       [:li {:display :inline-block
             :margin [[0 (px 5)]]}
        [:&.steam
         [:svg
          [:path
           [:&:first-child
            {:fill (theme 3)}]
           [:&:last-child
            {:fill (theme 0)}]]]]
        [:img :svg {:width (px 32)
                    :height (px 32)
                    :border-radius (percent 50)}]]]]))

#?(:clj
   (def user
     [[:div.user
       {:display :flex
        :justify-content :center}
       [:svg {:flex-shrink 0}]
       [:div.meta
        {:text-align :left}
        [:h2 {:font-size (em 3.5)
              :font-weight 400
              :margin 0}]
        [:h3 {:text-transform :uppercase}]
        [:ul
         {:list-style :none
          :padding 0}
         [:li
          {:line-height 1
           :text-transform :uppercase}
          [:strong {:font-size (em 2.5)
                    :opacity 0.75}]]]]]
      [:div.user_common
       [:h2 {:margin [[(em 0.5) 0 (em 1)]]}]]]))

#?(:clj
   (def login-form
     "Login form style rules"
     [:form.login
      {:padding [[(vh 10) 0]]}
      [:button
       {:position :relative
        :overflow :hidden
        :padding [[(px 20) (px 20) (px 20) (px 75)]]}
       [:&:after
        {:content (attr :data-background)
         :display :block
         :position :absolute
         :background-image
         (url (str "'"
                   (-> (binnacle/data-url assets [:resources
                                                  :assets
                                                  :images
                                                  :steam.svg])
                       (string/replace "#fff" (theme 0))
                       (string/replace "#000" "transparent")
                       (string/replace "#" "%23"))
                   "'"))
         :width (percent 100)
         :height (percent 100)
         :top 0
         :left 0
         :background-size (px 80)
         :background-repeat :no-repeat
         :background-position [[(px -10) (px -12)]]}]]]))

#?(:clj
   (def checkbox
     [:div.checkbox-wrapper
      [:input {:display :none}]
      [:label {:display :block
               :cursor :pointer}]
      [(selectors/> (selectors/+ :input :label)
                    :div)
       {:position :relative}]
      [(selectors/> (selectors/+ :input:disabled :label)
                    :div)
       {:background (-> (theme 0)
                        (color/darken 10))}]
      [(selectors/> (selectors/+ :input :label)
                    :div:after)
       {:display :block
        :width (px 10)
        :height (px 20)
        :content (attr :nil)
        :transform [[(scaleX -1) (rotate (deg 135))]]
        :margin [[(em -1) (em 1) 0 (em 2)]]
        :border-right [[(px 4) :solid (-> (theme 0)
                                          (color/darken 10))]]
        :border-top [[(px 4) :solid (-> (theme 0)
                                        (color/darken 10))]]
        :transition [[(s 0.15) :linear :all]]}]
      [(selectors/> (selectors/+ :input:checked :label)
                    :div:after)
       {:border-color (-> (theme 1)
                          color/as-hex)}]]))

#?(:clj
   (def tag
     [[:ul.tags
       [:li
        {:background (theme 2)
         :color (theme 0)
         :margin (em 0.15)
         :padding [[(em 0.15) (em 0.5)]]
         :border-radius (px 3)
         :font-weight 300}]]]))

#?(:clj
   (def game
     [(mobile-screen
       [[:div.games
         [:div.checkbox-wrapper
          {:padding 0}]
         [:div.game
          {:margin [[(em 0.5) 0]]}]]
        [(selectors/> :div.game :div)
         {:flex-grow 1}]])
      [:div.games
       {:display :flex
        :flex-flow [[:row :wrap]]
        :justify-content :center}]
      [:div.game
       {:display :flex
        :flex-direction :column
        :position :relative
        :max-width (px 460)
        :margin [[(em 0.5)]]
        :box-sizing :border-box
        :background (theme 0)
        :border [[(px 1) :solid (-> (theme 0)
                                    (color/darken 10)
                                    color/as-hex)]]}
       [:img
        {:max-width (calc ["100%" "+" "2em"])
         :margin [[(em -1) (em -1) (em 1)]]}]
       [:h4
        {:position :absolute
         :top 0
         :z-index -1}]
       [:ul.platforms
        {:padding 0
         :margin [[0 0 (em 1)]]}
        [:li
         {:position :relative
          :display :inline-block
          :text-transform :uppercase
          :font-size (em 0.8)}]
        [(selectors/+ :li :li)
         {:margin-left (em 0.75)}]]
       [:ul.players
        {:margin-bottom (em 1)}]]]))

#?(:clj
   (def player
     [[:div.players
       {:margin-bottom (em 1)}
       [:div.player
        {:border-bottom :none}]]
      [(selectors/> :div.players :div)
       {:width (percent 100)
        :box-sizing :border-box}]
      [(selectors/> :div.players :div:last-child)
       [:div.player
        {:border-bottom [[(px 1) :solid (-> (theme 0)
                                            (color/darken 10)
                                            color/as-hex)]]}]]
      [(selectors/> :div.players :div.player:last-child)
       {:border-bottom [[(px 1) :solid (-> (theme 0)
                                           (color/darken 10)
                                           color/as-hex)]]}]
      [:div.player
       {:display :flex
        :flex-direction :row
        :align-items :center
        :background (theme 0)
        :border [[(px 1) :solid (-> (theme 0)
                                    (color/darken 10)
                                    color/as-hex)]]}
       [:svg {:flex-shrink 0
              :margin-right (em 1)}]
       [:div.info
        {:flex-grow 1}
        [:h3 [:span.realname
              {:font-size (em 0.75)
               :text-transform :uppercase
               :font-weight 600
               :margin-left (em 0.25)}]]
        [:div.meta
         {:display :flex
          :flex-direction :row}
         [:span {:flex-grow 1}]
         [:span.games {:flex-grow 0}]]]]
      [:ul.players :ul.tags
       {:margin 0
        :padding 0
        :list-style :none}
       [:li.player :li.tag
        {:display :inline-block}]]]))

#?(:clj
   (def stylesheet
     "Combined stylesheet rules. Indexed map of swatches as an argument"
     [normalize
      [:html :body {:min-height (percent 100)}]
      [:body {:color (theme 2)
              :background (-> (theme 0)
                              (color/darken 2)
                              color/as-hex)}]
      [:.element :.container {:padding (em 1)}
       [:&.full-width {:margin [[0 (em -1)]]}]]
      [:hr
       {:border :none
        :margin [[0 (em 1)]]}
       {:border-top [[(px 2) :solid (-> (theme 3)
                                        color/as-rgb
                                        (assoc :alpha 0.1))]]}]
      [:form.common
       [:button {:margin-top (em 1)}]]
      fonts
      typography
      input
      button
      header
      user
      footer
      login-form
      player
      game
      tag
      checkbox]))

#?(:clj
   (defn render
     "Output stylesheet to external file"
     []
     (css {:output-to "resources/public/css/screen.css"} stylesheet)))
