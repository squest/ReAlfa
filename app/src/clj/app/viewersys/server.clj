(ns app.viewersys.server
  (:require
    [immutant.web :as web]
    [com.stuartsierra.component :as component]
    [ring.middleware.defaults :refer :all]
    [app.utils :refer :all]
    [noir.cookies :as cook]
    [noir.session :as sess]
    [immutant.util :as log]))

(defrecord Server [port handler path host]
  component/Lifecycle
  (start [this]
    (println "")
    (info "Starting the server... A Jew, an Arab, and Donald Trump walk into a bar...")
    (let [site-config (assoc-in site-defaults [:security :anti-forgery] false)
          site (-> (:routes handler)
                   cook/wrap-noir-cookies
                   sess/wrap-noir-session
                   (wrap-defaults site-config))]
      (log/set-log-level! :OFF)
      (do (println "")
          (info "Server has started")
          (info "Now you can open your browser on http://localhost:4000\n"))
      (assoc this :stop-fn (web/run site {:port port :path path :host host}))))
  (stop [this]
    (info "Stopping the server... ")
    (web/stop (:stop-fn this))
    (dissoc this :stop-fn)))

(defn make [server-config]
  (map->Server server-config))
