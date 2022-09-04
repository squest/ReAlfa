(ns app.core
  (:gen-class)
  (:require [app.sitesys.system :as system]
            [com.stuartsierra.component :as component]))

(defn -main [& x]
  (component/start-system (system/create-system :site)))
