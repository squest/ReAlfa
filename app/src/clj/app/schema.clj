(ns app.schema
  (:require [schema.core :as s]))

(def topics #{:english :math :logic :total})

;; These are static content

(def TemplateIds [s/Str])
;; Available on redis

(def Template
  "Map for each template"
  {:template-id s/Str
   :edn-file    s/Str
   :filename    s/Str
   :topic       (s/pred topics)})

(def Templates
  "A category map, with topics as defined keys,
  and templates for that topics as values"
  {(s/pred topics) [Template]})
;; Available on redis

(def ProblemMap
  "Big map with template-id as key
  and problem-ids for that template as values"
  {s/Str [s/Str]})
;; Available on redis

(def Problem
  "Map of each problem"
  {:soal        {:soal-text s/Str
                 :options   [s/Any]
                 :jawaban   s/Str}
   :bahas       s/Str
   :template-id s/Str :problem-id s/Str :topic (s/pred topics)})

(def Problems
  "A Map for all problems, key problem-id and value problem data"
  {s/Str Problem})
;; Available on redis

;; These are dynamic content

(def TemplateStat
  "Sorted templates based on their stats"
  {:template-id s/Str :topic s/Keyword
   :submission  s/Int :viewed s/Int
   :right       s/Int :wrong s/Int
   :rating-user s/Num :rating-template s/Num
   :duration    s/Int :average-duration s/Int})

(def TemplateStats
  "A map with topics as keys
  and ordered list of template-stats as values"
  {(s/pred topics) [TemplateStat]})
;; Available on redis

;; These are user

(def UserTopicStats
  "A map of user-stats for one topic"
  {:submission s/Int :viewed s/Int
   :right      s/Int :wrong s/Int
   :average    s/Num :score-one s/Int
   :duration   s/Int :average-duration s/Int})

(def User
  "A map of user profile with stats included"
  {:username s/Str
   :password s/Str
   :score    s/Int
   :stats    {(s/pred topics) UserTopicStats}})

(def Users {s/Str User})
;; Available on redis

(def UserLog
  "One map to represent each time a user worked on a problem"
  {:username s/Str :timestamp s/Str :correct? s/Bool :duration s/Int
   :topic    (s/pred topics) :template-id s/Str :problem-id s/Str})

(def UserLogs [UserLog])

(def UserRank
  {(s/pred topics) {:rank   [{:username s/Str
                              :average  s/Num
                              :score    s/Int
                              :rank     s/Int}]
                    :number s/Int}})

(def UserSubmission
  (dissoc UserLog :timestamp))

(def UserSubmissions
  "Submissions of each quiz by a user"
  [UserSubmission])






























