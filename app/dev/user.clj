(ns user
  (:require
    [app.sitesys.system :as sitesys]
    [com.stuartsierra.component :as component]
    [clojure.set :as cset]
    [app.utils :refer :all]))

(defonce dev-system (atom nil))

;;===== SYSTEM RELATED FUNCTIONS ======

(defn start
  "Starting the viewer"
  [which-system]
  (->> (sitesys/create-system which-system)
       (component/start-system)
       (reset! dev-system)))

(defn stop []
  (swap! dev-system component/stop-system))

(defn restart
  []
  (let [which-system (:which-system @dev-system)]
    (do (stop)
        (print "Restarting the system in 2 seconds... ")
        (Thread/sleep 100)
        (println "plus/minus 5 minutes.")
        (Thread/sleep 100)
        (start which-system))))

(defn stat
  []
  (require '[stats])
  (in-ns 'stats))

(defn db-updates
  []
  (require '[db-update])
  (in-ns 'db-update))

(defn adhoc-update
  []
  (require '[adhoc])
  (in-ns 'adhoc))































