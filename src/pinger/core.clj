(ns pinger.core
  (:require [clj-http.client :as http]

            [clojure.string :as str]
            [clojure.spec.alpha :as s]))

(defn send-notification
  ([channel message] (send-notification channel message {}))
  ([channel message
    {:keys [server priority tags title]
     :or   {server   "ntfy.sh"
            priority :low
            tags     []}}]
   (let [uri (format "https://%s/%s" server channel)]
     (http/post uri
                {
                 :body message
                 :headers {
                           :Title (or title message)
                           :Priority (name priority)
                           :Tags (str/join "," tags)
                           }
                 }))))

(defprotocol INtfyChan
  (send! [self title msg])
  (alert! [self title msg]))

(defrecord NtfyChan [server channel]
  INtfyChan
  (send! [_ title msg]
    (send-notification channel msg {:server server :title title :priority :low}))
  (alert! [_ title msg]
    (send-notification channel msg {:server server :title title :priority :max})))

(defn open-channel
  ([chan] (open-channel "ntfy.sh" chan))
  ([srv chan] (NtfyChan. srv chan)))
