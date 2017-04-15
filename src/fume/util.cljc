(ns fume.util
  "Helper functions"
  (:require [clojure.set :refer [rename-keys]]
            [clojure.string :as string]
            [datascript.core :as d]
            [om.dom :as dom]
            [om.next.protocols :as p]
            #?@(:clj  [[clojure.edn :as reader]
                       [sablono.normalize :as normalize]
                       [sablono.util :refer [element?]]
                       [cemerick.friend :as friend]]
                :cljs [[cljs.reader :as reader]
                       [sablono.core :as html :refer-macros [html]]])))

#?(:clj
   (defn logged-in?
     "Return boolean based on current authentication status"
     [req]
     ((comp boolean friend/identity) req)))

(defn query->map
  "Return a map of URL query parameters"
  [s]
  (let [qmap (if (map? s)
               s
               (reduce (fn [accl x]
                         (let [[k v] (string/split x #"=")]
                           (if (accl k)
                             (assoc accl k
                                    (vec (flatten (vector (accl k) v))))
                             (assoc accl k v))))
                       {}
                       (-> s (subs 1) (string/split #"&"))))]
    (rename-keys qmap
                 (reduce #(assoc %1 %2 (keyword %2))
                         {}
                         (keys qmap)))))

(defn coll-keys
  [coll & keys]
  (reduce (fn [accl k]
            (assoc accl k
                   (cond-> (k accl)
                     (and (not (coll? (k accl)))
                          (not (nil? (k accl)))) vector)))
          coll
          keys))

#?(:clj
   (defn- compile-attrs
     "Conform attributes to a format expected by Om DOM utils"
     [attrs]
     (when-not (or (nil? attrs) (empty? attrs))
       (cond-> (rename-keys attrs {:class :className
                                   :for :htmlFor})
         (:class attrs) (update-in [:className] #(string/join #" " %))))))

#?(:clj
   (defn- compile-element
     "Given Hiccup structure, recursively conform the structure to one
     that Om DOM utils expect"
     [element]
     (cond
       (element? element)
       (let [[tag attrs children] (normalize/element element)]
         (dom/element {:tag tag
                       :attrs (compile-attrs attrs)
                       :children (vector (map compile-element children))}))
       (satisfies? p/IReactComponent element) element
       :else (dom/text-node (str element)))))

(defn dom
  "Convert Hiccup syntax to Om DOM snytax."
  [markup]
  #?(:clj (compile-element markup)
     :cljs (html markup)))

(defn namespace-map
  "Given a map and a prefix as args, apply the prefix to all keys of the map"
  [m prefix]
  (reduce (fn [acc item]
            (assoc acc
                   (->> (key item) name (str (name prefix) "/") keyword)
                   (val item)))
          {}
          m))

(defn in-namespace?
  "Given a map and namespace as arguments, determines if that map matches
  the expected namespace."
  [m ns]
  (when (map? m)
    (contains? (->> m keys (map namespace) set) (name ns))))

(defn plural
  [qualifier base]
  (when (number? qualifier)
    (cond-> base
      (or (> qualifier 1) (= qualifier 0)) (str "s"))))
