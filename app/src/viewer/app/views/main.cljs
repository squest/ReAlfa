(ns app.views.main
  (:require
    [app.utils :as u]
    [re-frame.core :as re]
    [app.views.template :as template]
    [app.views.problem :as problem]
    [app.subs :as subs]))

(defn waiter-panel
  "Just a waiting progress bar."
  []
  (fn []
    (->> [[:center
           [:h2 "Please be patient, the content you're requesting may or may not appear"]
           [:br] [:br]]]
         (concat [:div.container]
                 (repeat 2 [:br]))
         (vec))))

(defn show-main-panel
  "This is the main panel slot for all other parts of the app after app-bar part."
  [main-panel]
  (fn [main-panel]
    (condp = main-panel
      :panel-waiter [waiter-panel]
      :panel-templates [template/template-panel]
      :panel-problems [problem/problem-panel]
      [waiter-panel])))

(defn footer []
  [:div.container
   [:br]
   [:hr]
   [:center [:h5 "Copyright PT Zenius Education"]]])

(defn body-controller
  []
  (let [main-panel (re/subscribe [:subs-view-main-panel])]
    (fn []
      [show-main-panel @main-panel])))

(defn header
  []
  (let [title (re/subscribe [:subs-view-main-panel])]
    (fn []
      [:center [:h2 (condp = @title
                      :panel-templates "Available templates"
                      :panel-problems "Available problems for this template"
                      "Great problem generator")]])))

(defn main-page
  []
  [:div
   [header]
   [body-controller]
   [footer]])

