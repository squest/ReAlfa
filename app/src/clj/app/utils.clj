(ns app.utils
  (:require
    [clj-uuid :as uuid]
    [clojure.edn :as edn]
    [cheshire.core :refer [parse-string]]
    [clojure.string :as cs]
    [clojure.pprint :refer [pprint]]
    [me.raynes.fs :as fs]
    [java-time :as t]))

(defn now
  []
  (subs (str (t/local-date-time)) 0 19))

(defn num->per
  "Convert decimals into percentage string"
  [num]
  (str (subs (str (* 100 num)) 0 5) "%"))

(defn dumpsite
  []
  (-> (->> (-> (str fs/*cwd*)
               (cs/split #"/"))
           (drop-last 3)
           (cs/join "/"))
      (str "/dumpsite/")))

(defn info [& body]
  (apply println "INFO :" body))

(defn error [& body]
  (apply println "ERROR :" body))

(defn warn [& body]
  (apply println "WARNING :" body))

(defn conpath
  "Content directory path"
  [dir]
  (->> (cs/split (str dir) #"/")
       (drop 6)
       (interpose "/")
       (cons "/")
       (apply str)))

(def pres clojure.pprint/pprint)

(defn let-pres
  [exprs]
  (pres exprs)
  exprs)

(defmacro pro-catch
  "Macro to report problem error"
  [message coder exprs]
  `(try ~exprs
        (catch Exception ~(gensym)
          (error ~message ~coder)
          (throw (Exception. ~message)))))

(defmacro no-throw
  "Macro to report error without exception"
  [message some-var exprs]
  `(try ~exprs
        (catch Exception ~(gensym)
          (error ~message ~some-var))))

(defmacro silent-try
  "Macro to report error without exception"
  [exprs]
  `(try ~exprs
        (catch Exception ~(gensym))))

(defn pro-rep
  "Reporting error"
  [message coder]
  (error message coder)
  (throw (Exception. message)))

(defn get-os
  []
  (let [res (System/getProperty "os.name")]
    (if (cs/includes? res "Win") :win :posix)))

(defn create-path
  "Given one argument it returns the dir path from a vector of dir path."
  ([vec-path os]
   (if (= :win os)
     (->> vec-path
          (interpose "\\")
          (cons "\\")
          (apply str))
     (->> vec-path
          (interpose "/")
          (cons "/")
          (apply str))))
  ([vec-path]
   (create-path vec-path (get-os))))

(defn path
  "Create a string path of string dir to string child"
  [dir child]
  (create-path (concat (fs/split dir) [child])))

(defn main-path
  "Function to return the main path of the zenpres-school"
  ([vec?] (->> (fs/split fs/*cwd*)
               rest
               (drop-last 2)))
  ([] (->> (fs/split fs/*cwd*)
           rest
           (drop-last 2)
           create-path)))

(defn cslurp
  "Helper function to easily slurp and read-string a file"
  [fname]
  ((comp edn/read-string slurp) fname))

(defn cspit
  "Helper function to beautifully print clojure to file"
  [fname data]
  (->> data pprint with-out-str (spit fname)))

(defn cstr
  [data]
  (with-out-str (pprint data)))

(defn uuid
  "When given zero argument, it returns a uuid/v1, given one arguments, it returns
  a list of n uuids."
  ([]
   (cs/replace (str (uuid/v1)) #"-" ""))
  ([n]
   (repeatedly n uuid)))




