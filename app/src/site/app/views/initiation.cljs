(ns app.views.initiation
  (:require
    [app.utils :as u]
    [re-frame.core :as re]
    [reagent.core :as rc]
    [app.subs :as subs]
    [app.views.components :as com]
    [app.ajax :as server]))

(defn init-panel
  []
  (fn []
    [:div.container
     [:hr]
     [:center
      [:br] [:br] [:br]
      [:div {:class "d-grid gap-4"}
       [com/button-register]
       [com/button-login]]]]))

(defn username-input
  [username]
  [:input {:type      "text"
           :value     @username
           :on-change #(reset! username (-> % .-target .-value))}])

(defn password-input
  [password]
  [:input {:type      "password"
           :value     @password
           :on-change #(reset! password (-> % .-target .-value))}])

(defn password2-input
  [password password2]
  (let [message (rc/atom "")]
    (fn []
      [:div
       [:input {:type      "password"
                :value     @password2
                :on-change #(do (reset! password2 (-> % .-target .-value))
                                (if (= @password @password2)
                                  (reset! message "Udah sama")
                                  (reset! message "Password belom sama")))}]
       [:p @message]])))

(defn message
  []
  (let [message (re/subscribe [:subs-message])]
    (fn []
      [:h5 @message])))

(defn login-panel
  []
  (let [username (rc/atom "")
        password (rc/atom "")]
    (re/dispatch [:event-set-message ""])
    [:div
     [:center
      [:br] [:br] [:br]
      [:p "Username"
       [:br]
       [username-input username]]
      [:p "Password"
       [:br]
       [password-input password]]
      [:br]
      [message]
      [:br]
      [:button.btn.btn-primary
       {:on-click #(server/user-login {:username @username
                                       :password @password})}
       "Login"]
      [:br] [:br]
      [com/button-register]]]))

(defn register-panel
  []
  (let [username (rc/atom "")
        password (rc/atom "")
        password2 (rc/atom "")]
    (re/dispatch [:event-set-message ""])
    (fn []
      [:div
       [:center
        [:br] [:br] [:br]
        [:p "Username"
         [:br]
         [username-input username]]
        [:p "Password"
         [:br]
         [password-input password]
         [:br]]
        [:p "Konfirmasi Password"
         [:br]
         [password2-input password password2]
         [:br]
         [message]
         [:br]]
        [:button.btn.btn-primary
         {:on-click #(if (= @password @password2)
                       (server/user-register {:username @username
                                              :password @password})
                       (js/alert "Password sama konfirmasi belom sama tuh"))}
         "Register"]]])))
