(ns app.site.routes
  (:require
    [compojure.core :refer [GET POST context routes]]
    [compojure.route :refer [resources files not-found]]
    [app.applogic.user :as user]
    [app.applogic.game :as game]
    [app.applogic.logging :as logging]
    [app.utils :refer :all]
    [app.site.pages :as page]
    [noir.response :as nresp]
    [clojure.edn :as edn]))

(declare front-routes api-routes other-routes)

(defn main-routes
  [dbase]
  (routes (front-routes dbase)
          (context "/api" req (api-routes dbase))
          (other-routes)))

(defn other-routes
  []
  (routes
    (resources "/")
    (not-found "<center><h1>Nothing to see here</h1></center>")))

(defn front-routes
  [dbase]
  (routes
    (GET "/" req
      (page/site dbase))
    (GET "/report" req
      (page/user-report dbase))
    (GET "/report/users" req
      (page/user-report dbase))
    (GET "/report/users/:username" [username]
      (page/user-report dbase username))
    (GET "/report/users/:topic/:which" [topic which]
      (page/user-report dbase (keyword topic) (keyword which)))
    (GET "/report/templates" req
      (page/template-report dbase))))

(defn api-routes
  [dbase]
  (routes
    (GET "/total-problems" req
      (nresp/edn (let-pres {:total-problems (count (-> dbase :db :content :problems))})))
    (POST "/user-login" req
      (let [usermap (cslurp (:body req))]
        (nresp/edn (user/user-login (:db dbase) usermap))))
    (POST "/check-username" req
      (let [usermap (let-pres (cslurp (:body req)))]
        (nresp/edn (let-pres (user/check-username (:db dbase) usermap)))))
    (POST "/user-register" req
      (let [usermap (cslurp (:body req))]
        (nresp/edn (user/user-register! dbase usermap))))
    (POST "/get-proset" req
      (let [reqmap (cslurp (:body req))]
        (nresp/edn (game/user-request (:db dbase) reqmap))))
    (POST "/get-user-profile" req
      (let [reqmap (let-pres (cslurp (:body req)))]
        (nresp/edn (user/user-profile-rank (:db dbase) reqmap))))
    (POST "/game-submissions" req
      (let [reqmap (cslurp (:body req))]
        (nresp/edn (game/user-submit! (:db dbase) reqmap))))
    (POST "/report-problem" req
      (let [reqmap (cslurp (:body req))]
        (nresp/edn (logging/report-problem! (:db dbase) reqmap))))))

