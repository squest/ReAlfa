(ns app.views.template
  (:require
    [app.utils :as u]
    [re-frame.core :as re]
    [app.ajax :as server]
    [app.subs :as subs]))

(defn template-panel
  []
  (fn []
    (let [templates (re/subscribe [:subs-data-templates])]
      [:div.container
       [:hr]
       (into [:div.d-grid.gap-2]
             (for [{:keys [template-id filename]} @templates]
               [:button.btn.btn-outline-primary
                {:on-click #(do (server/get-problems template-id)
                                (re/dispatch
                                  [:event-set-template
                                   (subs filename 0 (- (count filename) 5))])
                                (js/console.log template-id))}
                (subs filename 0 (- (count filename) 5))]))])))
