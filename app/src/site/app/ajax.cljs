(ns app.ajax
  (:require
    [re-frame.core :as re]
    [app.utils :as u :refer [info vinfo]]
    [ajax.core :as ajax :refer [GET POST ajax-request]]
    [ajax.edn :as edn]))

(defn user-register
  "Sending registration usermap"
  [usermap]
  (u/info "User register called")
  (u/info usermap)
  (->> {:uri           "/api/user-register"
        :params        usermap
        :handler       (fn [[_ data]]
                         (re/dispatch [:event-incoming-user-register data]))
        :error-handler (fn [[_ msg]] (set! (.-location js/window) "/"))}
       (merge (u/ajax-edn :post))
       ajax-request))

(defn get-total-problems
  "Getting the number of problems"
  []
  (->> {:uri           "/api/total-problems"
        :handler       (fn [[_ data]]
                         (re/dispatch [:event-incoming-get-total-problems data]))
        :error-handler (fn [[_ msg]] (set! (.-location js/window) "/"))}
       (merge (u/ajax-edn :get))
       ajax-request))

(defn user-login
  "Sending registration usermap"
  [usermap]
  (u/info "User login called")
  (u/info usermap)
  (->> {:uri           "/api/user-login"
        :params        usermap
        :handler       (fn [[_ data]]
                         (re/dispatch [:event-incoming-user-login data]))
        :error-handler (fn [[_ msg]] (set! (.-location js/window) "/"))}
       (merge (u/ajax-edn :post))
       ajax-request))

(defn get-proset
  "Get the problem set from the server"
  [proset-request-map]
  (->> {:uri           "/api/get-proset"
        :params        proset-request-map
        :handler       (fn [[_ data]]
                         (re/dispatch [:event-incoming-get-proset data]))
        :error-handler (fn [[_ msg]] (set! (.-location js/window) "/"))}
       (merge (u/ajax-edn :post))
       ajax-request))

(defn get-profile
  "Get the user profile and rank"
  [request-map]
  (->> {:uri           "/api/get-user-profile"
        :params        request-map
        :handler       (fn [[_ data]]
                         (re/dispatch [:event-incoming-get-profile data]))
        :error-handler (fn [[_ msg]] (set! (.-location js/window) "/"))}
       (merge (u/ajax-edn :post))
       ajax-request))

(defn game-submission
  "Submit the result of a game"
  [request-map]
  (->> {:uri           "/api/game-submissions"
        :params        request-map
        :handler       (fn [[_ data]]
                         (info "value from server : ")
                         (vinfo data))
        :error-handler (fn [[_ msg]] (set! (.-location js/window) "/"))}
       (merge (u/ajax-edn :post))
       ajax-request))

(defn report-problem
  "Report a problematic problem"
  [problem-data]
  (->> {:uri           "/api/report-problem"
        :params        (dissoc problem-data :soal :bahas)
        :handler       (fn [[_ data]]
                         (info "value from server : ")
                         (re/dispatch [:event-problem-reported true]))
        :error-handler (fn [[_ msg]] (set! (.-location js/window) "/"))}
       (merge (u/ajax-edn :post))
       ajax-request))

(defn check-username
  "check whether a user exist"
  [usermap]
  (->> {:uri           "/api/check-username"
        :params        usermap
        :handler       (fn [[_ data]]
                         (info data)
                         (if (:status data)
                           (do (re/dispatch [:event-incoming-set-user usermap])
                               (re/dispatch [:event-set-main-panel :panel-main-menu]))
                           (do (u/remove-storage "username")
                               (re/dispatch [:event-set-main-panel :panel-initiation]))))
        :error-handler (fn [[_ msg]] (set! (.-location js/window) "/"))}
       (merge (u/ajax-edn :post))
       ajax-request))






