(ns app.sitesys.system
  (:require
    [com.stuartsierra.component :as component]
    [app.sitesys.config :refer [config]]
    [clojure.tools.namespace.repl :refer [refresh]]
    [app.utils :refer :all]
    [app.sitesys.server :as immut]
    [app.sitesys.handler :as http]))

(defn create-system
  "It creates a system, and return the system, but not started yet"
  [which-system]
  (let [{:keys [server]}
        (config)]
    (component/system-map
      :handler (http/make)
      :server (component/using (immut/make server) [:handler])
      :which-system which-system)))




