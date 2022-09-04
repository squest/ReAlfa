(ns app.site.report
  (:require
    [app.utils :refer :all]
    [app.schema :refer :all]
    [schema.core :as s]))

(comment
  {:total-users  usercount
   :top-users    (->> (take 10 sorted)
                      (map-indexed #(assoc %2 :rank (inc %1))))
   :bottom-users (->> (take-last 10 sorted)
                      (map-indexed #(assoc %2 :rank (inc %1))))
   :mid-users    (->> (split-at (quot (count sorted) 2) sorted)
                      second
                      (take-last 10)
                      (map-indexed #(assoc %2 :rank (inc %1))))})

(defn report-users
  ([dbase]
   (let [users @(:users (:db dbase))
         usercount (count users)
         sorted (->> (vals users)
                     (sort-by :score >)
                     (map-indexed #(assoc %2 :rank (inc %1))))]

     {:total usercount
      :what  "total score"
      :users sorted}))
  ([dbase username]
   (let [users @(:users (:db dbase))
         usercount (count users)
         sorted (->> (vals users)
                     (sort-by :username)
                     (map-indexed #(assoc %2 :rank (inc %1))))]

     {:total usercount
      :what  "username"
      :users sorted}))
  ([dbase topic which]
   (let [users @(:users (:db dbase))
         usercount (count users)
         sorted (->> (vals users)
                     (sort-by (comp which topic :stats) >)
                     (map-indexed #(assoc %2 :rank (inc %1))))]
     {:total usercount
      :what  (str (name topic) " " (name which))
      :users sorted})))

(defn report-templates
  [{:keys [db]}]
  (let [tmp-stats @(:template-stats db)
        mapi (let [data (->> (:content db) :templates vals (apply concat))]
               (zipmap (map :template-id data)
                       (map :filename data)))
        tmp-error (->> @(:reported-problems db)
                       (mapv :template-id)
                       (mapv mapi)
                       frequencies
                       (into []))]
    (merge (zipmap (keys tmp-stats)
                   (->> (vals tmp-stats)
                        (mapv #(map-indexed
                                 (fn [i x]
                                   (assoc x :rank (inc i)
                                            :template-name (mapi (:template-id x)))) %))))
           {:total             (count (apply concat (vals tmp-stats)))
            :reported-problems (map #(do {:filename (key %) :freq (val %)}) tmp-error)})))
