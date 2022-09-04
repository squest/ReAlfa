(ns app.views.main
  (:require
    [app.utils :as u]
    [re-frame.core :as re]
    [app.views.game :as game]
    [app.views.initiation :as init]
    [app.views.result :as res]
    [app.views.profile :as pro]
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
      :panel-initiation [init/init-panel]
      :panel-login [init/login-panel]
      :panel-register [init/register-panel]
      :panel-main-menu [game/main-menu]
      :panel-game [game/main-stage]
      :panel-transition [game/panel-transition]
      :panel-result [res/game-result]
      :panel-bahas [res/game-bahas]
      :panel-profile [pro/profile]
      [waiter-panel])))

(defn footer []
  [:div
   [:br]
   [:hr]
   [:center [:h6 "Alpha testing by Zenius"]]])

(defn body-controller
  []
  (let [main-panel (re/subscribe [:subs-view-main-panel])]
    (fn []
      [show-main-panel @main-panel])))

(defn header
  []
  (let [title (re/subscribe [:subs-view-main-panel])]
    (fn []
      [:div
       [:div
        {:class "shadow p-3 mb-5 bg-light"}
        [:center
         [:br]
         [:h3 "===== ZenPractice ====="]
         [:h5 (condp = @title
                :panel-initiation "Daftar atau login dulu"
                :panel-register "Register"
                :panel-login "Login"
                "Perfect practice makes perfect")]]]
       [:hr]])))

(defn main-page
  []
  [:div.container
   [header]
   [body-controller]
   [footer]])

