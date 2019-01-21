(ns heroicc.web.routes
  (:require
   [domkm.silk :as silk]))

(def routes
  ^{:doc "Route definitions. Some are server-side only."}
  (silk/routes (merge {:dashboard [[]]
                       :games [["games"]]
                       :login [["login"]]}
                      #?(:clj {:stylesheet [["css" "screen.css"]]}))))
