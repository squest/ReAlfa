(ns app.events
  (:require
    [re-frame.core :as re]
    [app.db :as db]
    [app.utils :as u :refer [info vinfo]]
    [ajax.core :as ajax :refer [GET POST ajax-request]]
    [ajax.edn :as edn]
    [app.ajax :as server]))

(re/reg-event-db
  :event-initialize-db
  (fn [_ _]
    db/default-db))

;; Checking the "cookies"

(def get-storage
  (fn [db _]
    (if-let [username (u/get-storage "username")]
      (do (info (str "This is username : " username))
          (server/check-username {:username username})
          db)
      (do (re/dispatch [:event-set-main-panel :panel-initiation])
          db))))

(re/reg-event-db :event-get-storage get-storage)

(def set-user
  (fn [db [_ data]]
    (assoc db :user data)))

(re/reg-event-db :event-incoming-set-user set-user)

(def set-total-problems
  (fn [db [_ data]]
    (assoc db :total-problems (:total-problems data))))

(re/reg-event-db :event-incoming-get-total-problems set-total-problems)

;; Main view events

(def set-main-panel
  (fn [db [_ main-panel]]
    (assoc db :view-main-panel main-panel)))

(re/reg-event-db :event-set-main-panel set-main-panel)

;; User init, registration, and login events

(def set-message
  (fn [db [_ message]]
    (assoc db :minor-message message)))

(re/reg-event-db :event-set-message set-message)

(def set-register-action
  (fn [db [_ {:keys [status message username]}]]
    (if status
      (do (u/set-storage "username" username)
          (re/dispatch [:event-set-main-panel :panel-main-menu])
          (assoc db :user {:username username}))
      (do (re/dispatch [:event-set-message message])
          db))))

(re/reg-event-db :event-incoming-user-register set-register-action)

(def set-login-action
  (fn [db [_ {:keys [status message username]}]]
    (if status
      (do (u/set-storage "username" username)
          (re/dispatch [:event-set-main-panel :panel-main-menu])
          (assoc db :user {:username username}))
      (do (re/dispatch [:event-set-message message])
          (u/info (str "this is message from server : " message))
          db))))

(re/reg-event-db :event-incoming-user-login set-login-action)

(def set-logout-action
  (fn [db _]
    (do (u/remove-storage "username")
        (re/dispatch [:event-set-main-panel :panel-initiation])
        (dissoc db :user))))

(re/reg-event-db :event-user-logout set-logout-action)

(def set-incoming-proset
  (fn [db [_ data]]
    (u/info (count data))
    (do (re/dispatch [:event-set-main-panel :panel-game])
        (re/dispatch [:event-set-timer :start])
        (re/dispatch [:event-set-interval])
        (-> (assoc db :proset data)
            (assoc :problem-no 0)
            (assoc :submissions [])))))

(re/reg-event-db :event-incoming-get-proset set-incoming-proset)

(def set-interval
  (fn [db _]
    (let [interval (atom nil)]
      (->> (-> (fn [] (re/dispatch [:event-set-timer :next]))
               (js/setInterval 1000))
           (reset! interval))
      (assoc db :interval interval))))

(re/reg-event-db :event-set-interval set-interval)

(def set-timer
  (fn [db [_ data]]
    (condp = data
      :start (assoc db :timer 120)
      :stop (do (swap! (:interval db) js/clearInterval)
                db)
      :next (if (zero? (:timer db))
              (do (swap! (:interval db) js/clearInterval)
                  (re/dispatch [:event-game-over])
                  db)
              (update db :timer dec)))))

(re/reg-event-db :event-set-timer set-timer)

(def submit-answer
  (fn [db [_ data]]
    (update db :submissions #(conj % data))))

(re/reg-event-db :event-submit-answer submit-answer)

(def next-problem
  (fn [db _]
    (u/info (:problem-no db))
    (u/info (count (:proset db)))
    (if (< (:problem-no db) 7)
      (update db :problem-no inc)
      (do (re/dispatch [:event-game-over])
          (assoc db :problem-no 0)))))

(re/reg-event-db :event-next-problem next-problem)

(def game-over
  (fn [db _]
    (let [submissions (:submissions db)
          result {:bener       (count (filterv :correct? submissions))
                  :total       (count submissions)
                  :submissions (-> #(assoc % :hasil (if (:correct? %) "Bener" "Salah"))
                                   (mapv submissions))}]
      (when-not (empty? submissions)
        (server/game-submission submissions))
      (re/dispatch [:event-set-main-panel :panel-result])
      (re/dispatch [:event-set-timer :stop])
      (-> (assoc db :game-result result)
          (dissoc :submissions)))))

(re/reg-event-db :event-game-over game-over)

(def set-profile
  (fn [db [_ data]]
    (info data)
    (re/dispatch [:event-set-main-panel :panel-profile])
    (assoc db :user-profile data)))

(re/reg-event-db :event-incoming-get-profile set-profile)

(def problem-reported
  (fn [db [_ data]]
    (assoc db :problem-reported data)))

(re/reg-event-db :event-problem-reported problem-reported)

















