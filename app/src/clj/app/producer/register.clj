(ns app.producer.register
  (:require
    [app.generator.sbd.regis :as sbd]
    [app.generator.cania.regis :as cania]))

(defn soal-map
  "Register each folder"
  []
  (concat sbd/register cania/register))


