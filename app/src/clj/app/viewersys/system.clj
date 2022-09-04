(ns app.viewersys.system
  (:require
    [com.stuartsierra.component :as component]
    [app.viewersys.config :refer [config]]
    [clojure.tools.namespace.repl :refer [refresh]]
    [app.producer.component :as cont]
    [app.utils :refer :all]
    [app.viewersys.server :as immut]
    [app.viewersys.handler :as http]))

(defn create-system
  "It creates a system, and return the system, but not started yet"
  [which-system]
  (let [{:keys [server content]}
        (config)]
    (component/system-map
      :producer (cont/make content)
      :handler (component/using (http/make) [:producer])
      :server (component/using (immut/make server) [:handler])
      :which-system which-system)))