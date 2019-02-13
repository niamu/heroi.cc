(ns heroicc.web.style
  (:require
   [binnacle.core :as binnacle]
   [clojure.string :as string]
   [garden.color :as color]
   [garden.core :as garden]
   [garden.stylesheet :as stylesheet]
   [garden.selectors :as selectors]
   [garden.units :as units]
   [heroicc.web.assets :as assets]
   [normalize.core :as normalize])
  #?(:cljs (:require-macros
            [heroicc.web.style :refer [defbreakpoint]])))

#?(:clj (def assets assets/assets))

(def theme
  "Teal and Orange"
  ["#FFFFFF" "#FABD4A" "#2A5769" "#002E40" "#FA9600"])

#?(:clj
   (defmacro defbreakpoint [name media-params]
     `(defn ~name [& rules#]
        (stylesheet/at-media ~media-params [:& rules#]))))

#?(:clj
   (defbreakpoint mobile-screen
     {:screen true
      :max-width (units/px 480)}))

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
          :src (url (binnacle/data-url assets [:assets
                                               :fonts
                                               :franchise-bold.woff]))
          :font-weight :normal
          :font-style :normal}
         stylesheet/at-font-face)))

#?(:clj
   (def typography
     "Basic rules for type"
     [[:body {:font [["13px/16px" "Helvetica"] "Arial" "sans-serif"]}]
      [:h1 :h2 :h3 {:line-height (units/em 1)
                    :margin 0}]
      [:h2 :h3 {:font-weight 300
                :margin [[0 0 (units/em 0.5)]]}]
      [:h1 {:position :relative
            :font-family "Franchise"
            :font-weight 300
            :opacity 0.75
            :text-transform :uppercase
            :line-height (units/em 1.2)
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
      {:width (units/percent 100)
       :border [[(units/px 1) :solid (-> (theme 0)
                                         (color/darken 10)
                                         color/as-hex)]]
       :padding (units/em 0.5)}]))

#?(:clj
   (def button
     "Basic rules for buttons"
     [:.button
      {:outline :none
       :display :table
       :text-align :center
       :margin [[0 :auto]]
       :border :none
       :color (theme 0)
       :background (radial-gradient (theme 4) (theme 1))
       :padding [[(units/px 20) (units/px 25)]]
       :text-transform :uppercase
       :border-radius (units/px 5)
       :font-weight 600
       :font-size (units/px 16)
       :box-shadow [[0 (units/px 2) (units/px 10) (-> (theme 2)
                                                      color/as-rgb
                                                      (assoc :alpha 0.3))]]
       :cursor :pointer
       :transition [[:transform :ease-in-out (units/s 0.05)]
                    [:box-shadow :ease-in-out (units/s 0.05)]
                    [:background-color :ease-in-out (units/s 0.05)]]}
      [:&:focus
       {:box-shadow [[0 0 (units/px 1) (units/px 2) (theme 1)]
                     [0 (units/px 2) (units/px 15) (-> (theme 2)
                                                       color/as-rgb
                                                       (assoc :alpha 0.2))]]}]
      [:&:hover
       {:transform (scale 1.015)
        :box-shadow [[0 (units/px 2) (units/px 15) (-> (theme 2)
                                                       color/as-rgb
                                                       (assoc :alpha 0.2))]]
        :background (radial-gradient (color/lighten (theme 4) 10)
                                     (color/lighten (theme 1) 10))}]
      [:&:active
       {:background (radial-gradient (color/darken (theme 4) 10)
                                     (color/darken (theme 1) 10))
        :transform (scale 0.985)}]
      (mobile-screen
       [:& {:width (units/percent 100)}])]))

#?(:clj
   (def header
     "Rules for the header look and feel"
     [[:header
       {:position :relative
        :padding (units/em 0.75)
        :z-index 1
        :text-align :center
        :color (theme 0)
        :background-position [[(units/percent 50) (units/percent 25)]]
        :background-size :cover}
       [:&:after :&:before
        {:position :absolute
         :top 0
         :left 0
         :display :block
         :content (attr :nil)
         :height (units/percent 100)
         :width (units/percent 100)}]
       [:&:before
        {:filter (grayscale 0.75)
         :background-position :inherit
         :background-size :inherit
         :background-image :inherit
         :z-index -1}]
       [:&:after
        {:opacity 0.85
         :z-index -1
         :background (radial-gradient (theme 2) (theme 1))
         :background-size (units/percent 150)}]
       [:&.login
        {:padding (units/em 2)
         :background-position [[(units/percent 50) (units/percent 15)]]}
        [:h1 {:font-size (units/vmin 32)}]]
       [:&>
        [:h1
         {:display :inline-block
          :font-size (units/vmin 15)
          :color (theme 1)
          :transition [[(units/s 0.15) :linear :color]]}
         [:&:before
          {:position :absolute
           :z-index -1
           :content (attr :data-title)
           :top 0
           :left 0
           :width (units/percent 100)
           :color (theme 2)
           :letter-spacing (units/em -0.01)
           :text-shadow [[0 0 (units/vw 0.5) (theme 2)]]
           :transition :inherit}]
         [:&:hover:before
          {:color (-> (theme 2)
                      (color/darken 10))}]
         [:a {:color :inherit
              :transition :inherit
              :outline :none
              :padding [[0 (units/em 0.1)]]
              :border-radius (units/px 1)}
          [:&:focus
           {:box-shadow [[0 0 (units/px 1) (units/px 2)]]}]
          [:&:hover {:color (-> (theme 1)
                                (color/lighten 3))}]]]
        [:h2 {:font-size (units/vmin 3.6)
              :text-shadow [[0 0 (units/px 3) (theme 2)]]}]]
       [:nav {:display :flex
              :text-align :left
              :text-transform :uppercase
              :font-weight 600
              :padding-bottom (units/em 1)
              :margin-bottom (units/em 2)
              :border-bottom [[(units/px 2) :solid (-> (theme 3)
                                                       color/as-rgb
                                                       (assoc :alpha 0.1))]]}
        [:div.breadcrumbs {:flex-grow 1}]
        [(selectors/- :div.breadcrumbs :a) {:align-self :center}]
        [:h1 {:font-size (units/em 3)
              :vertical-align :baseline
              :line-height (units/em 1)
              :display :inline}]
        [:span {:opacity 0.75
                :font-weight 400
                :text-transform :uppercase}]]]
      [(selectors/+ :header :div)
       {:box-shadow [[0 (units/px -5) (units/px 5) (-> (color/rgb 0 0 0)
                                                       (assoc :alpha 0.3))]]
        :position :relative
        :z-index 1}]]))

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
             :margin [[0 (units/px 5)]]}
        [:&.steam
         [:svg
          [:path
           [:&:first-child
            {:fill (theme 3)}]
           [:&:last-child
            {:fill (theme 0)}]]]]
        [:a
         {:outline :none}
         [:&:focus
          [:img :svg
           {:box-shadow [[0 0 (units/px 1) (units/px 2) (theme 1)]]}]]
         [:img :svg {:width (units/px 32)
                     :height (units/px 32)
                     :border-radius (units/percent 50)}]]]]]))

#?(:clj
   (def user
     [[:div.user
       {:display :flex
        :justify-content :center
        :margin [[0 :auto]]
        :max-width (units/px 400)
        :width (units/percent 100)
        :margin-top (units/em -3)}
       [:svg {:flex-shrink 0}]
       [:h2
        {:font-size (units/em 3.5)
         :margin [[(units/px 35) 0 0 (units/em 0.5)]]}
        [:a {:color :inherit}]]]
      [:div.user_common
       [:h2 {:margin [[(units/em 0.5) 0 (units/em 1)]]}]]]))

#?(:clj
   (def login-form
     "Login form style rules"
     [:form.login
      {:padding [[(units/vh 10) 0]]}
      [:button
       {:position :relative
        :overflow :hidden
        :padding [[(units/px 20) (units/px 20) (units/px 20) (units/px 75)]]}
       [:&:after
        {:content (attr :data-background)
         :display :block
         :position :absolute
         :background-image
         (url (-> (binnacle/data-url assets [:assets
                                             :images
                                             :steam.svg])
                  (string/replace "%23fff" (theme 0))
                  (string/replace "%23000" "transparent")
                  (string/replace "#" "%23")))
         :width (units/percent 100)
         :height (units/percent 100)
         :top 0
         :left 0
         :background-size (units/px 80)
         :background-repeat :no-repeat
         :background-position [[(units/px -10) (units/px -12)]]}]]]))

#?(:clj
   (def checkbox
     [[:div.checkbox-wrapper
       {:position :relative}
       [:input {:position :absolute
                :outline :none
                :opacity 0
                :width (units/percent 100)
                :height (units/percent 100)}]
       [:label {:display :block
                :cursor :pointer}]
       [(selectors/> (selectors/+ :input :label) :*)
        {:position :relative}]
       [(selectors/> (selectors/+ :input:disabled :label) :*)
        {:background (-> (theme 0)
                         (color/darken 10))}]
       [(selectors/> (selectors/+ :input :label) :*:after)
        {:display :block
         :width (units/px 10)
         :height (units/px 20)
         :content (attr :nil)
         :transform [[(scaleX -1) (rotate (units/deg 135))]]
         :margin [[(units/em -1) (units/em 1) 0 (units/em 2)]]
         :border-right [[(units/px 4) :solid (-> (theme 0)
                                                 (color/darken 10))]]
         :border-top [[(units/px 4) :solid (-> (theme 0)
                                               (color/darken 10))]]
         :transition [[(units/s 0.15) :linear :all]]}]
       [(selectors/> (selectors/+ :input:checked :label) :*:after)
        {:border-color (-> (theme 1)
                           color/as-hex)}]
       [(selectors/+ :input:checked :label)
        [:div.player.element [:rect.checked {:display :block}]]]]
      [:li.tag
       [:div.checkbox-wrapper
        [(selectors/+ :input:focus :label)
         {:text-decoration :underline}]
        [(selectors/> (selectors/+ :input :label) :*)
         {:vertical-align :text-bottom}
         [:&:after
          {:display :inline-block
           :width (units/px 8)
           :height (units/px 15)
           :margin [[(units/em -0.2) (units/em 0.3) 0(units/em 0.7)]]}]]]]]))

#?(:clj
   (def tag
     [[:ul.tags
       [:li
        {:background (theme 2)
         :color (theme 0)
         :margin (units/em 0.15)
         :padding [[(units/em 0.15) (units/em 0.5)]]
         :border-radius (units/px 3)
         :font-weight 300}]]
      [:form [:ul.tags
              {:margin [[(units/em 1) 0]]}]]]))

#?(:clj
   (def game
     [(mobile-screen
       [[:div.games
         [:div.checkbox-wrapper
          {:padding 0}]
         [:div.game
          {:margin [[(units/em 0.5) 0]]
           :max-width (units/percent 100)}]]
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
        :max-width (units/px 300)
        :margin [[(units/em 0.5)]]
        :box-sizing :border-box
        :background (theme 0)
        :border [[(units/px 1) :solid (-> (theme 0)
                                          (color/darken 10)
                                          color/as-hex)]]}
       [:img
        {:max-width (calc ["100%" "+" "2em"])
         :margin [[(units/em -1) (units/em -1) (units/em 1)]]}]
       [:h4
        {:position :absolute
         :top 0
         :z-index -1}]
       [:ul.platforms
        {:padding 0
         :margin [[0 0 (units/em 1)]]}
        [:li
         {:position :relative
          :display :inline-block
          :text-transform :uppercase
          :font-size (units/em 0.8)}]
        [(selectors/+ :li :li)
         {:margin-left (units/em 0.75)}]]
       [:ul.players
        {:margin-bottom (units/em 1)}]]]))

#?(:clj
   (def player
     [[:div.players
       {:margin-bottom (units/em 1)}
       [:div.player
        {:border-bottom :none}]]
      [(selectors/> :div.players :div)
       {:width (units/percent 100)
        :box-sizing :border-box}]
      [(selectors/> :div.players :div:last-child)
       [:div.player
        {:border-bottom [[(units/px 1) :solid (-> (theme 0)
                                                  (color/darken 10)
                                                  color/as-hex)]]}]]
      [(selectors/> :div.players :div.player:last-child)
       {:border-bottom [[(units/px 1) :solid (-> (theme 0)
                                                 (color/darken 10)
                                                 color/as-hex)]]}]
      [:div.player
       {:display :flex
        :flex-direction :row
        :align-items :center
        :background (theme 0)
        :border [[(units/px 1) :solid (-> (theme 0)
                                          (color/darken 10)
                                          color/as-hex)]]}
       [:svg {:flex-shrink 0
              :margin-right (units/em 1)}
        [:rect.checked {:display :none}]]
       [:div.info
        {:flex-grow 1}
        [:h3 [:span.realname
              {:font-size (units/em 0.75)
               :text-transform :uppercase
               :font-weight 600
               :margin-left (units/em 0.25)}]]]]
      [(selectors/+ :input:focus :label)
       [:div.player
        {:position :relative
         :box-shadow [[0 0 (units/px 1) (units/px 2)]]
         :border-radius (units/px 1)
         :z-index 1}]]
      [(selectors/+ :input:hover :label)
       [:div.player
        {:background-color (-> (color/as-rgb (theme 2))
                               (assoc :alpha 0.025))}]]
      [:div.common-players
       {:display :flex
        :flex-wrap :wrap
        :justify-content :center}
       [:div.player
        {:background :none
         :border :none
         :display :block
         :text-align :center}
        [:svg {:margin 0}]]]
      [:ul.players :ul.tags
       {:margin 0
        :padding 0
        :list-style :none}
       [:li.player :li.tag
        {:display :inline-block}]]]))

#?(:clj
   (def stylesheet
     [normalize/normalize
      [:* {:box-sizing :border-box}]
      [:body {:margin 0
              :color (theme 2)
              :background (-> (theme 0)
                              (color/darken 2)
                              color/as-hex)}]
      [:.element :.container {:padding (units/em 1)}
       [:&.full-width {:margin [[0 (units/em -1)]]}]]
      [:hr
       {:border :none
        :margin [[0 (units/em 1)]]}
       {:border-top [[(units/px 2) :solid (-> (theme 3)
                                              color/as-rgb
                                              (assoc :alpha 0.1))]]}]
      [:form.common
       [:button {:margin-top (units/em 1)}]]
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

#?(:clj (defn render [] (garden/css stylesheet)))
