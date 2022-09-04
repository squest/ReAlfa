(ns app.sitesys.handler
  (:require
    [compojure.core :refer [GET POST context routes]]
    [compojure.route :refer [resources files not-found]]
    [com.stuartsierra.component :as component]
    [app.utils :refer :all]
    [app.site.routes :refer :all]))

(defrecord Handler [dbase]
  component/Lifecycle
  (start [this]
    (assoc this :routes (main-routes dbase)))
  (stop [this]
    this))

(defn make []
  (map->Handler {}))
