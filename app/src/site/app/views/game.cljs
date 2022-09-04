(ns app.views.game
  (:require
    [app.utils :as u]
    [re-frame.core :as re]
    [reagent.core :as rc]
    [app.subs :as subs]
    [app.ajax :as server]
    [app.views.components :as com]
    [cljs-time.core :as ct]))

;; TODO list
;; [x] Create "Udahan capek" menu in every soal
;; [x] Create action necessary for "udahan capek"
;; [x] Create a transition page from one soal to another

(defn main-menu
  []
  (let [total-problems (re/subscribe [:subs-total-problems])]
    (fn []
      [:div.container
       [:center
        [:p (str "Btw kalo error, refresh aja hehe, ohya total ada " @total-problems " soal di sini")]
        [:div.d-grid.gap-4
         [com/button-play :logic]
         [com/button-play :math]
         [com/button-play :english]
         [com/button-profile]
         [com/button-logout]]]])))

(defn panel-transition
  []
  (-> (fn []
        (re/dispatch [:event-set-main-panel :panel-game]))
      (js/setTimeout 200))
  (into [:div]
        (for [i (range 10)]
          [:p [:br]])))

(defn soal-section
  [soal-text]
  [:div [:h6 {:dangerouslySetInnerHTML {:__html soal-text}}]])

(def bclass (rc/atom "btn btn-outline-secondary"))

(defn soal-options
  [problem-data]
  (let [user (re/subscribe [:subs-user])
        username (:username @user)
        problem-no (re/subscribe [:subs-problem])]
    (fn [problem-data]
      (let [start (.now js/Date)]
        [:div
         [:center [:h6 (str "Soal no : " (inc @problem-no) " dari 8")] [:br]]
         [soal-section (get-in problem-data [:soal :soal-text])]
         (into [:div.d-grid.gap-4]
               (for [op (get-in problem-data [:soal :options])]
                 ^{:key op}
                 [:a
                  {:dangerouslySetInnerHTML {:__html (op 1)}
                   :class    @bclass
                   :href     "#"
                   :role     "button"
                   :on-click #(let [ans (u/jawab (op 2))
                                    kunjaw (get-in problem-data [:soal :jawaban])
                                    stop (.now js/Date)
                                    duration (int (/ (- stop start) 1000))]
                                (re/dispatch [:event-submit-answer
                                              (-> (assoc problem-data :correct? (= ans kunjaw))
                                                  (assoc :username username)
                                                  (assoc :duration duration)
                                                  (dissoc :soal :bahas))])
                                (-> (fn []
                                      (re/dispatch [:event-next-problem])
                                      (re/dispatch [:event-problem-reported false])
                                      (reset! bclass "btn btn-outline-secondary"))
                                    (js/setTimeout 300))
                                (if (= ans kunjaw)
                                  (reset! bclass "btn btn-outline-success")
                                  (reset! bclass "btn btn-outline-danger")))}]))]))))

(defn button-end-game
  []
  [:center
   [:br]
   [:button.btn.btn-primary
    {:on-click #(re/dispatch [:event-game-over])}
    "Udahan ah, capek"]])

(defn button-report-problem
  [problem-data]
  [:center
   [:br]
   [:button.btn.btn-warning
    {:on-click #(server/report-problem problem-data)}
    "Lapor boy! Soal salah nih"]])

(defn main-stage
  []
  (let [proset (re/subscribe [:subs-proset])
        problem (re/subscribe [:subs-problem])
        timer (re/subscribe [:subs-timer])
        problem-reported (re/subscribe [:subs-problem-reported])]
    (fn []
      (let [problem-data (@proset @problem)]
        (reset! bclass "btn btn-outline-secondary")
        [:div.container
         [:hr]
         [:div
          [:center
           [:h5 (str "Timer : " @timer)]]
          [soal-options problem-data]
          (when-not @problem-reported
            [button-report-problem (@proset @problem)])
          [button-end-game]
          [:br]]]))))





























