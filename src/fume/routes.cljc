(ns fume.routes
  (:require [domkm.silk :as silk]))

(def routes
  ^{:doc "Route definitions. Only server needs to know of the /api endpoint"}
  (silk/routes (merge {:dashboard    [[]]
                       :login        [["login"]]
                       :logout       [["logout"]]
                       :common-games [["games" "common"]]}
                      #?(:clj  {:api [["api"]]}))))
