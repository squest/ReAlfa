(ns app.applogic.game
  (:require [schema.core :as s]
            [app.schema :refer :all]
            [app.utils :refer :all]
            [app.applogic.logging :as log]
            [clojure.edn :as edn]))

(defn update-template-stats!
  "Update a template-stats based on one submissions"
  [template-stats {:keys [topic template-id correct? duration]}]
  (dosync
    (alter template-stats
           (fn [x]
             (let [tmp (first (filter #(= template-id (:template-id %)) (topic x)))
                   real-dur (if correct? duration 0)
                   new-map (-> (update tmp (if correct? :right :wrong) inc)
                               (update :submission inc)
                               (update :duration #(+ % real-dur))
                               (assoc :rating-user (* 100 (/ (:wrong tmp) (:submission tmp) 1.0)))
                               (assoc :rating-template (/ (:submission tmp) (:right tmp) 1.0)))
                   mapi (assoc new-map :average-duration (int (* 100 (/ (:duration new-map) (:right new-map)))))
                   new-sorted (-> #(= (:template-id %) (:template-id tmp))
                                  (remove (topic x))
                                  (conj mapi))]
               (assoc x topic (vec new-sorted)))))))

(defn update-user-stats!
  "Update user-stats based on the submissions of a game"
  [users user-submissions]
  (let [usermap (get @users (:username (first user-submissions)))
        topic (:topic (first user-submissions))
        username (:username usermap)
        new-stats (-> (reduce (fn [x m]
                                (let [real-dur (if (:correct? m) (:duration m) 0)
                                      new-stat (update-in x
                                                          [(:topic m) (if (:correct? m) :right :wrong)]
                                                          inc)]
                                  (update-in new-stat
                                             [(:topic m) :duration]
                                             #(+ % real-dur))))
                              (:stats usermap)
                              user-submissions)
                      (update-in [topic :submission] #(+ 8 %)))
        new-stati (assoc-in new-stats
                            [topic :average-duration]
                            (try (int (/ (get-in new-stats [topic :duration])
                                         (get-in new-stats [topic :right])))
                              (catch Exception e 180)))
        ;; Regardless of the number of problems, submission always counted as 8
        recal-map (reduce (fn [m topic]
                            (assoc-in m [topic :average]
                                      (* 100
                                         (/ (get-in m [topic :right])
                                            (get-in m [topic :submission])
                                            1.0))))
                          new-stati
                          [:math :english :logic])]
    (dosync (alter users #(assoc % username (assoc usermap :stats recal-map))))))

(defn update-user-rating!
  "Update user rating based on the scores of new submissions"
  [users {:keys [math logic english total]} username]
  (let [usermap (get @users username)
        new-map (-> (merge-with + usermap {:score (int total)})
                    (update-in [:stats :math :score-one] #(int (+ % math)))
                    (update-in [:stats :logic :score-one] #(int (+ % logic)))
                    (update-in [:stats :english :score-one] #(int (+ % english))))
        new-score (int (Math/pow (->> [:math :logic :english]
                                      (map #(get-in new-map [:stats % :score-one]))
                                      (reduce *))
                                 1/3))
        avg (->> [:math :logic :english]
                 (map #(get-in new-map [:stats % :average]))
                 (reduce *))
        avg-dur (->> [:math :logic :english]
                     (map #(get-in new-map [:stats % :average-duration]))
                     (reduce *))
        new-avg (int (Math/pow (* avg avg) 1/6))
        total-score (int (Math/pow (* new-score new-avg 100000.0 (/ 1 avg-dur)) 1/3))]
    (dosync (alter users #(assoc % username (assoc new-map :score total-score))))))

(defn sort-templates!
  [db]
  (dosync (alter (:template-stats db)
                 (fn [x] (-> (update x :logic #(vec (sort-by :rating-user %)))
                             (update :math #(vec (sort-by :rating-user %)))
                             (update :english #(vec (sort-by :rating-user %))))))))

(defn user-submit!
  "After finishing a game, user submit the result.
  These are the actions to process the result:
  1. Update user stats with all the submissions
  2. Update template stats with every submission
  3. Update user score with all the submissions"
  [db user-submissions]
  (pres (s/validate UserSubmissions user-submissions))
  (update-user-stats! (:users db) user-submissions)
  (doseq [s user-submissions]
    (log/log-event! db s))
  (let [tmpstats @(:template-stats db)
        username (:username (first user-submissions))]
    (doseq [s user-submissions]
      (update-template-stats! (:template-stats db) s))
    (let [mp (/ (count user-submissions) 8.0)]
      ;; mp is the multiplier depending on the number of problems solved in one submission
      (loop [[s & ss] user-submissions mapi {:math 0 :english 0 :logic 0}]
        (if s
          (let [topic (:topic s)
                tmp-score (if (:correct? s)
                            (->> (topic tmpstats)
                                 (filter #(= (:template-id %) (:template-id s)))
                                 first
                                 :rating-template
                                 (* mp))
                            0)]
            (recur ss (merge-with + mapi {topic tmp-score})))
          (update-user-rating! (:users db)
                               (assoc mapi :total (reduce + (vals mapi)))
                               username))))
    (sort-templates! db)
    {:status  true
     :message "User profile and rank have been updated"}))

;; What this function do
;; 1. Get user stats for the topic
;; 2. Get 2 templates before and after according to user rating
;; 3. Get 4 problems from each template and shuffle to take only 8

(defn user-request
  "A user request a proset, and get the proset data"
  [db {:keys [topic username]}]
  (let [userdata (get @(:users db) username)
        {:keys [average score-one]} (topic (:stats userdata))
        topic-templates (topic @(:template-stats db))
        [easy diff] (-> (quot (count topic-templates) 2)
                        (split-at topic-templates))
        randth (rand-nth [true false])]
    (cond
      (>= score-one 500)
      (if randth
        (->> (map :template-id topic-templates)
             shuffle
             (take 8)
             (map #(rand-nth (get-in db [:content :problem-map %])))
             (mapv (get-in db [:content :problems])))
        (->> (map :template-id diff)
             shuffle
             (take 8)
             (map #(rand-nth (get-in db [:content :problem-map %])))
             (mapv (get-in db [:content :problems]))))
      (< 100 score-one 500)
      (->> (concat (take-last 20 easy) (take 20 diff))
           (map :template-id)
           shuffle
           (take 8)
           (map #(rand-nth (get-in db [:content :problem-map %])))
           (mapv (get-in db [:content :problems])))
      (<= score-one 100)
      (->> (map :template-id easy)
           shuffle
           (take 8)
           (map #(rand-nth (get-in db [:content :problem-map %])))
           (mapv (get-in db [:content :problems]))))))


