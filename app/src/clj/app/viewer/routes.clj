(ns app.viewer.routes
  (:require
    [compojure.core :refer [GET POST context routes]]
    [compojure.route :refer [resources files not-found]]
    [ring.util.response :as resp]
    [cheshire.core :as cc]
    [app.utils :refer :all]
    [app.viewer.pages :as page]
    [noir.response :as nresp]))

(declare front-routes api-routes other-routes)

(defn main-routes
  [producer]
  (routes (front-routes producer)
          (context "/api" req (api-routes producer))
          (other-routes)))

(defn other-routes
  []
  (routes
    (resources "/")
    (not-found "<center><h1>Nothing to see here</h1></center>")))

(defn front-routes
  [producer]
  (routes
    (GET "/" req (page/home))
    (GET "/viewer" req (page/viewer producer))))

(defn api-routes
  [producer]
  (routes
    (GET "/param/:mesakeh" req
      (do (pres req)
          (nresp/edn req)))
    (GET "/get-templates" req
      (nresp/edn (get-in producer [:templates])))
    (GET "/get-problems-by-id/:template-id" [template-id]
      (let [dor (get-in producer [:problem-map template-id])]
        (nresp/edn dor)))))

