(ns app.applogic.dbase
  (:require
    [com.stuartsierra.component :as component]
    [taoensso.carmine :as car :refer [wcar]]
    [app.applogic.logging :as log]
    [app.applogic.user :as user]
    [app.applogic.game :as game]
    [schema.core :as s]
    [app.schema :refer :all]
    [app.utils :refer :all]))

(declare grab-content init-db cronj-db load-db-content)

(def DBSchema
  "Structure DBase.db"
  {:content        {:template-ids []
                    :templates    {}
                    :problem-map  {}
                    :problems     {}}
   :template-stats (ref {:math    []
                         :logic   []
                         :english []})
   :users          (ref {})
   :user-rank      (ref {:math    {}
                         :logic   {}
                         :english {}
                         :total   {}})
   :user-logs      (ref [])})

(defn db->ref
  [redis]
  {:db {:content           (load-db-content redis)
        :template-stats    (ref (->> (wcar redis (car/get :template-stats))
                                     (s/validate TemplateStats)))
        :users             (ref (->> (wcar redis (car/get :users))
                                     (s/validate Users)))
        :user-rank         (ref (->> (wcar redis (car/get :user-rank))
                                     (s/validate UserRank)))
        :user-logs         (ref [])
        :reported-problems (ref [])}})

(defrecord Dbase [source redis]
  component/Lifecycle
  (start [this]
    (let [db (db->ref redis)
          log-cronj (do (println "Starting the cronjob for logging")
                        (future (log/cronj-log (:db db))))
          db-cronj (do (println "Starting the ref to db cronjob")
                       (future (cronj-db (merge db {:redis redis}))))
          user-cronj (do (println "Starting the user sorting cronjob")
                         (future (user/cronj-sort-user-rank! (:db db))))]
      (user/sort-user-rank! (:db db))
      (game/sort-templates! (:db db))
      (println "Dbase component started")
      (merge this db {:log-cronj  log-cronj
                      :db-cronj   db-cronj
                      :user-cronj user-cronj})))
  (stop [this]
    (when-let [log-cronj (:log-cronj this)]
      (future-cancel log-cronj))
    (when-let [db-cronj (:db-cronj this)]
      (future-cancel db-cronj))
    (when-let [user-cronj (:user-cronj this)]
      (future-cancel user-cronj))
    this))

(defn make [db-config]
  (map->Dbase db-config))

(defn load-db-content
  "Load content data from existing db"
  [redis]
  {:template-ids (->> (wcar redis (car/get :template-ids))
                      (s/validate TemplateIds))
   :templates    (->> (wcar redis (car/get :templates))
                      (s/validate Templates))
   :problem-map  (->> (wcar redis (car/get :problem-map))
                      (s/validate ProblemMap))
   :problems     (->> (wcar redis (car/get :problems))
                      (s/validate Problems))})

(defn ref->db
  "Store the refs to the db"
  [{:keys [db redis]}]
  (wcar redis
        (car/set :template-stats @(:template-stats db))
        (car/set :user-rank @(:user-rank db))
        (car/set :users @(:users db))))

(defn cronj-db
  "Cronj job to store refs to redis"
  [dbase]
  (loop []
    (Thread/sleep (* 2 60 1000))
    (ref->db dbase)
    (recur)))






