(ns app.views.result
  (:require
    [app.utils :as u]
    [re-frame.core :as re]
    [reagent.core :as rc]
    [app.subs :as subs]
    [app.views.components :as com]
    [app.ajax :as server]))

(defn button-next-proset
  [topic username]
  [:button.btn.btn-outline-secondary
   {:on-click #(server/get-proset {:topic topic :username username})}
   (str "Next " (name topic) " game")])

(defn button-bahas
  [caption]
  [:button.btn.btn-outline-primary
   {:on-click #(re/dispatch [:event-set-main-panel :panel-bahas])}
   caption])

(defn bahas-options
  [options]
  (into [:div]
        (for [a options]
          (if (first a)
            [:li [:b (str (u/jawab (a 2)) "." (a 1))]]
            [:li (str (u/jawab (a 2)) "." (a 1))]))))

(defn bahas-soal
  [problem submit nomer]
  (let [{:keys [soal bahas]} problem
        {:keys [hasil]} submit
        {:keys [jawaban options soal-text]} soal]
    [:div [:h6 (str "Soal nomer " nomer " : Jawaban => " hasil)]
     [:div [:h6 {:dangerouslySetInnerHTML {:__html soal-text}}]]
     [bahas-options options]
     [:br]
     [:h6 (str "Pembahasan kunjaw : " jawaban)]
     [:div [:p {:dangerouslySetInnerHTML {:__html bahas}}] [:br]]
     [:hr]]))

(defn game-bahas
  []
  (let [proset (re/subscribe [:subs-proset])
        result (re/subscribe [:subs-result])
        user (re/subscribe [:subs-user])]
    (fn []
      (let [submits (@result :submissions)
            {:keys [username]} @user]
        [:div.container
         [:center [:h3 "Pembahasan"] [:hr]]
         (when-not (empty? submits)
           (into [:div]
                 (for [i (range (count submits))]
                   ^{:key i}
                   [bahas-soal (@proset i) (submits i) (inc i)])))
         [:hr]
         [button-next-proset (-> @proset first :topic) username]
         [com/button-main-menu]]))))

(defn game-result
  []
  (let [proset (re/subscribe [:subs-proset])
        result (re/subscribe [:subs-result])
        user (re/subscribe [:subs-user])]
    (fn []
      (let [topic (-> @proset first :topic)]
        [:div.container
         [:h5 (str "Yeay, elo bener " (:bener @result) " dari " (:total @result))]
         [:br]
         [:div.d-grid.gap-4
          [button-bahas "Liat pembahasan"]
          [button-next-proset topic (:username @user)]
          [com/button-main-menu]]]))))
