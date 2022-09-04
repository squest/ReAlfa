(ns app.site.pages
  (:require
    [clojure.pprint :refer [pprint]]
    [selmer.parser :as selmer :refer [render-file]]
    [app.site.report :as rep]
    [app.utils :refer :all]))

(selmer/cache-off!)

(defn file
  [filename]
  (str "template/" filename ".html"))

(defn site
  [dbase]
  (render-file (file "site") dbase))

(defn user-report
  ([dbase]
   (render-file (file "user-report")
                (rep/report-users dbase)))
  ([dbase username]
   (render-file (file "user-report")
                (rep/report-users dbase username)))
  ([dbase topic which]
   (render-file (file "user-report")
                (rep/report-users dbase topic which))))

(defn template-report
  [dbase]
  (render-file (file "template-report")
               (rep/report-templates dbase)))
