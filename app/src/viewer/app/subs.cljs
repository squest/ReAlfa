(ns app.subs
  (:require [re-frame.core :as re]))

(re/reg-sub
  :subs-view-main-panel
  (fn [db]
    (:view-main-panel db)))

(re/reg-sub
  :subs-data-templates
  (fn [db]
    (:data-templates db)))

(re/reg-sub
  :subs-data-problems
  (fn [db]
    (:data-problems db)))

(re/reg-sub
  :subs-template-name
  (fn [db]
    (:template-name db)))




