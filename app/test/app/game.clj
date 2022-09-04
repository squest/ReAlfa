(ns app.game
  (:require [clojure.test :refer :all]
            [app.applogic.game :refer :all]
            [app.applogic.user :refer :all]
            [app.applogic.dbase :as dbase]
            [app.utils :refer :all]
            [app.schema :refer :all]
            [schema.core :as s]))

(def db
  (do (comment (user/init-db-once))
      (:db (dbase/db->ref {:pool {} :spec {}}))))

(deftest user-test
  (testing "user's basic behaviour"
    (let [new-user {:username "jojolipet" :password "mizone"}]
      (is (= {:status   true
              :message  "Sukses terdaftar"
              :username "jojolipet"}
             (user-register! db new-user)))
      (is (= false (:status (user-register! db new-user))))
      (is (= true (:status (user-login db new-user))))
      (is (= false (:status (user-login db {:username "jojo" :password "mijo"}))))
      (is (= false (:status (user-login db (assoc new-user :password "mie ayam")))))
      (is (= (init-user new-user)
             (get @(:users db) "jojolipet"))))))

(deftest game-test
  (testing "Game play"
    (let [res (user-request db {:username "jojolipet" :topic :logic})
          user (get @(:users db) "jojolipet")]
      (is (= res (s/validate [Problem] res)))
      (is (= 8 (count res)))
      (let [user-submissions (-> #(-> (dissoc % :soal :bahas)
                                      (merge {:username (:username user)
                                              :correct? (rand-nth [true false])}))
                                 (mapv res))]
        (is (= user-submissions (s/validate UserSubmissions user-submissions)))

        (user-submit! db user-submissions)

        (is (= @(:user-logs db) (s/validate UserLogs @(:user-logs db))))

        (let [user-1 (get @(:users db) "jojolipet")]
          (is (= user-1 (s/validate User user-1)))

          (doseq [many [:logic :math :english :math :english :math :logic :math :logic]]
            (let [d (user-request db {:username "jojolipet" :topic many})]
              (user-submit! db
                            (-> #(-> (dissoc % :soal :bahas)
                                     (merge {:username "jojolipet"
                                             :correct? (rand-nth [true false])}))
                                (mapv d)))
              (is (= 8 (count d)))))

          (let [user-2 (get @(:users db) "jojolipet")
                userlogs @(:user-logs db)]
            (is (not= user-1 user-2))
            (is (= (dissoc user-1 :stats :score)
                   (dissoc user-2 :stats :score)))
            (is (= 17 (get-in user-2 [:stats :english :submission])))
            (is (= 33 (get-in user-2 [:stats :logic :submission])))
            (is (= 33 (get-in user-2 [:stats :math :submission])))
            (is (= (repeat 80 "jojolipet")
                   (mapv :username userlogs)))

            (is (= (* 10 8) (count userlogs)))

            (user-register! db {:username "jojon" :password "jojoli"})
            (doseq [many [:logic :math :english :math :logic :math]]
              (let [d (user-request db {:username "jojon" :topic many})]
                (user-submit! db
                              (-> #(-> (dissoc % :soal :bahas)
                                       (merge {:username "jojon"
                                               :correct? (rand-nth [true false false])}))
                                  (mapv d)))
                (is (= 8 (count d)))))

            (let [sorted (do (sort-user-rank! db)
                             @(:user-rank db))]
              (is (= sorted (s/validate UserRank sorted)))
              (is (= (get-in sorted [:math :number]) (count (get-in sorted [:math :rank]))))
              (is (= ["jojolipet" "jojon"]
                     (->> (get-in sorted [:math :rank])
                          (mapv :username)))))

            (let [user-3 (get @(:users db) "jojon")
                  user-profile (user-profile-rank db "jojon")]
              (comment (pres user-3)
                       (pres @(:template-stats db))
                       (pres @(:user-rank db)))
              (is (= #{:username :password :rank :stats :score :total-users}
                     (set (keys user-profile))))
              (is (= (get-in user-profile [:stats :math])
                     (s/validate UserTopicStats (get-in user-profile [:stats :math]))))
              (is (= #{:math :logic :english :total}
                     (set (keys (:rank user-profile)))))
              (is (= true (every? int? (vals (:rank user-profile))))))))))))
