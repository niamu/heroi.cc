(ns fume.client
  "Core client components for dynamic page content"
  (:require [fume.router :as router]))

(enable-console-print!)

(defn mount-route
  "Mount the React DOM Root corresponding with the current path"
  [path]
  (-> path
      router/path->name
      router/route->response))

(mount-route (.. js/window -location -pathname))
