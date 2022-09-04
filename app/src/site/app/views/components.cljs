(ns app.views.components
  (:require
    [app.utils :as u :refer [info vinfo]]
    [re-frame.core :as re]
    [reagent.core :as rc]
    [app.subs :as subs]
    [app.ajax :as server]))

(defn button-logout
  []
  [:button.btn.btn-primary
   {:on-click #(re/dispatch [:event-user-logout])}
   "Logout"])

(defn button-profile
  []
  (let [user (re/subscribe [:subs-user])]
    (fn []
      [:button.btn.btn-primary
       {:on-click #(server/get-profile @user)}
       "Profile & Ranking"])))

(defn button-register
  []
  [:button.btn.btn-primary
   {:id       "register"
    :on-click #(do (u/info "Register clicked")
                   (re/dispatch [:event-set-main-panel :panel-register]))}
   "Register"])

(defn button-login
  []
  [:button.btn.btn-primary
   {:id       "login"
    :on-click #(do (u/info "Login clicked")
                   (re/dispatch [:event-set-main-panel :panel-login]))}
   "Login"])

(defn button-play
  [topic]
  (let [user (re/subscribe [:subs-user])]
    (fn []
      [:button.btn.btn-primary
       {:on-click #(server/get-proset {:topic topic :username (:username @user)})}
       (condp = topic
         :logic "Logic"
         :math "Mathematics"
         :english "English")])))

(defn button-main-menu
  []
  [:button.btn.btn-outline-success
   {:on-click #(re/dispatch [:event-set-main-panel :panel-main-menu])}
   "Back to main menu"])
