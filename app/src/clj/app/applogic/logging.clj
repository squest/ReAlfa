(ns app.applogic.logging
  (:require
    [app.utils :refer :all]
    [app.schema :refer :all]
    [schema.core :as s]))

;; TODO list

(defn log-event!
  [db user-submission]
  (s/validate UserSubmission user-submission)
  (let [log-data (->> (merge user-submission {:timestamp (now)})
                      (s/validate UserLog))]
    (dosync (alter (:user-logs db) #(conj % log-data)))))

(defn store-log!
  [db]
  (let [current-user-logs (s/validate UserLogs @(:user-logs db))
        current-reports @(:reported-problems db)]
    (cspit (str (dumpsite) "user-logs-" (now) ".edn") current-user-logs)
    (cspit (str (dumpsite) "problem-reports-" (now) ".edn") current-reports)
    (dosync (ref-set (:user-logs db) []))))

(defn report-problem!
  [db problem-data]
  (let [templates (->> (-> db :content :templates)
                       vals (apply concat))
        mapi (zipmap (map :template-id templates)
                     (map :filename templates))]
    (dosync (alter (:reported-problems db)
                   #(conj % (assoc problem-data :filename (mapi (:template-id problem-data))))))))

(defn cronj-log
  [db]
  (loop []
    (Thread/sleep (* 60 60 1000))
    (store-log! db)
    (recur)))
