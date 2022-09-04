(ns app.views.profile
  (:require
    [app.utils :as u]
    [re-frame.core :as re]
    [reagent.core :as rc]
    [app.subs :as subs]
    [app.ajax :as server]
    [app.views.components :as com]))

;; TODO
;; [] Create a profile page
;; [] Create a panel for showing user rank in every topic
;; [] Create a panel showing percentage true for every topic
;; [] Create a button to go to main menu

(defn rank-score
  [user-profile topic]
  (if (= :total topic)
    [:div
     [:br]
     [:h6 (str "Score total : " (:score user-profile))]
     [:h6 (str "Rank total : " (-> user-profile :rank :total) " dari " (:total-users user-profile))]]
    [:div
     [:br]
     [:h6 (str "Score " (name topic) " : "
               (-> user-profile :stats topic :score-one))]
     [:h6 (str "Rank " (name topic)
               " : " (-> user-profile :rank topic)
               " dari " (:total-users user-profile))]
     [:h6 (str "Persen bener : " (subs (str (-> user-profile :stats topic :average)) 0 5) "%")]
     [:h6 (str "Average speed : " (-> user-profile :stats topic :average-duration) "s")]]))

(defn profile
  []
  (let [user-profile (re/subscribe [:subs-user-profile])]
    (fn []
      [:div.container
       [:h4 "User profile & ranks"]
       [:div
        [:h6 "Username : " (:username @user-profile)]
        [rank-score @user-profile :total]
        [rank-score @user-profile :math]
        [rank-score @user-profile :logic]
        [rank-score @user-profile :english]]
       [:hr]
       [com/button-main-menu]])))
