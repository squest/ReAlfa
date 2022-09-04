(ns app.applogic.user
  (:require
    [app.utils :refer :all]
    [schema.core :as s]
    [taoensso.carmine :as car :refer [wcar]]
    [app.schema :refer :all]))

(defn init-user
  [{:keys [username password]}]
  (let [stats {:submission       10
               :viewed           0
               :right            1
               :wrong            0
               :score-one        0
               :average          0.0
               :duration         0
               :average-duration 0}]
    {:username username
     :password password
     :score    0
     :stats    {:math    stats
                :english stats
                :logic   stats}}))

(defn user-register!
  [{:keys [db redis]} {:keys [username] :as usermap}]
  (if (empty? (filter #{username} (keys @(:users db))))
    (do (dosync (->> #(assoc % username (init-user usermap))
                     (alter (:users db))))
        (wcar redis (car/set :users @(:users db)))
        {:status   true
         :message  "Sukses terdaftar"
         :username username})
    {:status   false
     :message  "Udah ada yang pake username ini, coba lagi deh"
     :username username}))

(defn check-username
  [db {:keys [username]}]
  (if (empty? (filter #{username} (keys @(:users db))))
    {:status false}
    {:status true}))

(defn user-login
  [db {:keys [username password]}]
  (if-let [user-data (get @(:users db) username)]
    (if (= password (:password user-data))
      {:status true :username username}
      {:status false :message "Password ngaco tuh"})
    {:status false :message "Username belom ada, check salah ngga tuh? atau daftar dulu"}))

(defn sort-user-rank!
  "Alter user-rank ref based on users ref"
  [db]
  (let [users-vals (vals @(:users db))
        total-users (count users-vals)
        [math logic eng] (for [t [:math :logic :english]]
                           (->> (sort-by #(get-in % [:stats t :score-one]) > users-vals)
                                (map-indexed #(do {:score    (get-in %2 [:stats t :score-one])
                                                   :average  (get-in %2 [:stats t :average])
                                                   :username (get %2 :username)
                                                   :rank     (inc %1)}))
                                vec))
        total (->> (sort-by :score > users-vals)
                   (map-indexed #(do {:score    (get %2 :score)
                                      :average  (* 100
                                                   (/ (reduce + (for [k [:math :logic :english]]
                                                                  (get-in %2 [:stats k :right])))
                                                      (reduce + (for [k [:math :logic :english]]
                                                                  (get-in %2 [:stats k :submission])))
                                                      1.0))
                                      :username (get %2 :username)
                                      :rank     (inc %1)}))
                   vec)]
    (dosync (ref-set (:user-rank db)
                     {:math    {:rank   math
                                :number total-users}
                      :logic   {:rank   logic
                                :number total-users}
                      :english {:rank   eng
                                :number total-users}
                      :total   {:rank   total
                                :number total-users}}))))

(defn cronj-sort-user-rank!
  [db]
  (loop []
    (Thread/sleep (* 15 60 1000))
    (sort-user-rank! db)
    (recur)))

(defn user-profile-rank
  [db {:keys [username] :as request-map}]
  (let [usermap (get @(:users db) username)
        [math logic english total]
        (for [t [:math :logic :english :total]]
          (->> (get-in @(:user-rank db) [t :rank])
               (filter #(= username (:username %)))
               first
               :rank))
        total-users (count @(:users db))]
    (merge (dissoc usermap :password)
           {:rank        {:math math :logic logic :english english :total total}
            :total-users total-users})))
