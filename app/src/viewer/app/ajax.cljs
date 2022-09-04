(ns app.ajax
  (:require
    [re-frame.core :as re]
    [app.utils :as u :refer [info]]
    [ajax.core :as ajax :refer [GET POST ajax-request]]
    [ajax.edn :as edn]))

(defn get-templates
  "Update all content by sending a request for update to the server."
  []
  (->> {:uri           "/api/get-templates"
        :handler       (fn [[_ data]]
                         (re/dispatch [:event-set-templates data]))
        :error-handler (fn [[_ msg]] (set! (.-location js/window) "/"))}
       (merge (u/ajax-edn :get))
       ajax-request))

(defn get-problems
  "Update all content by sending a request for update to the server."
  [template-id]
  (->> {:uri           (str "/api/get-problems-by-id/" template-id)
        :handler       (fn [[_ data]]
                         (re/dispatch [:event-set-problems data]))
        :error-handler (fn [[_ msg]] (set! (.-location js/window) "/"))}
       (merge (u/ajax-edn :get))
       ajax-request))




