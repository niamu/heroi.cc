(ns heroicc.config
  (:require
   [clojure.java.io :as io]
   [fern.easy :as fern]))

(def config
  (fern/file->environment (io/resource "datomic/ion-config.edn")))
