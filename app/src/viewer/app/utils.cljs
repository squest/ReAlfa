(ns app.utils
  (:require
    [ajax.edn :as edn]
    [cljs.pprint :as pp]))

(defn by-id
  "Get element by id"
  [id]
  (.getElementById js/document id))

(defn by-class
  "Get element by class, result will be in js array"
  [class]
  (.getElementsByClassName js/document class))

(defn info [body]
  "Console log"
  (pp/pprint body))

(defn set-storage
  "Set something from local storage"
  [k v]
  (.setItem js/localStorage k v))

(defn get-storage
  "Get something from local storage"
  [k]
  (.getItem js/localStorage k))

(defn ajax-edn
  [method]
  {:format          (edn/edn-request-format)
   :response-format (edn/edn-response-format)
   :method          method})

(comment
  (defn re-render-mathjax []
    (js/MathJax.Hub.Queue (array "Typeset" js/MathJax.Hub))))
