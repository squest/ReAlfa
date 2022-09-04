(ns app.viewersys.handler
  (:require
    [compojure.core :refer [GET POST context routes]]
    [compojure.route :refer [resources files not-found]]
    [com.stuartsierra.component :as component]
    [ring.util.response :as resp]
    [app.utils :refer :all]
    [app.viewer.routes :refer :all]
    [me.raynes.fs :as fs]))

(defrecord Handler [producer]
  component/Lifecycle
  (start [this]
    (assoc this :routes (main-routes producer)))
  (stop [this]
    this))

(defn make []
  (map->Handler {}))
