(ns heroicc.db.connection
  (:require
   [datomic.client.api :as d]))

(def client
  "This function will return a local implementation of the client
  interface when run on a Datomic compute node. If you want to call
  locally, fill in the correct values in the map."
  (memoize
   #(d/client {:server-type :ion
               :region "us-east-1"
               :system "heroicc"
               :endpoint "http://entry.heroicc.us-east-1.datomic.net:8182"
               :proxy-port 8182})))

(defn- anom-map
  [category msg]
  {:cognitect.anomalies/category (keyword "cognitect.anomalies" (name category))
   :cognitect.anomalies/message msg})

(defn- anomaly!
  ([name msg]
   (throw (ex-info msg (anom-map name msg))))
  ([name msg cause]
   (throw (ex-info msg (anom-map name msg) cause))))

(defn ensure-dataset
  "Ensure that a database named db-name exists, running setup-fn
  against a connection. Returns connection"
  [db-name setup-sym]
  (require (symbol (namespace setup-sym)))
  (let [setup-var (resolve setup-sym)
        client (client)]
    (when-not setup-var
      (anomaly! :not-found (str "Could not resolve " setup-sym)))
    (d/create-database client {:db-name db-name})
    (let [conn (d/connect client {:db-name db-name})]
      (setup-var conn)
      conn)))

(defn connection
  []
  (ensure-dataset "heroicc"
                  'heroicc.db.init/load-dataset))
