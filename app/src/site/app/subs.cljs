(ns app.subs
  (:require [re-frame.core :as re]))

(re/reg-sub :subs-view-main-panel (fn [db] (:view-main-panel db)))

(re/reg-sub :subs-user (fn [db] (:user db)))

(re/reg-sub :subs-data-templates (fn [db] (:data-templates db)))

(re/reg-sub :subs-message (fn [db] (:minor-message db)))

(re/reg-sub :subs-proset (fn [db] (:proset db)))

(re/reg-sub :subs-problem (fn [db] (:problem-no db)))

(re/reg-sub :subs-result (fn [db] (:game-result db)))

(re/reg-sub :subs-user-profile (fn [db] (:user-profile db)))

(re/reg-sub :subs-total-problems (fn [db] (:total-problems db)))

(re/reg-sub :subs-timer (fn [db] (:timer db)))

(re/reg-sub :subs-problem-reported (fn [db] (:problem-reported db)))







