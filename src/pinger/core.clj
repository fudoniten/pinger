(ns pinger.core
  (:require [clj-http.client :as http]

            [clojure.string :as str]
            [clojure.spec.alpha :as s]))

(defn send-notification
  "POST `message` to `channel` on an ntfy server.

  `token`, when given, is an ntfy access token (`tk_...`) sent as a Bearer
  token, for servers that don't allow anonymous publishing (such as
  ntfy.fudo.org). Without one the request is anonymous, as ntfy.sh allows."
  ([channel message] (send-notification channel message {}))
  ([channel message
    {:keys [server priority tags title token]
     :or   {server   "ntfy.sh"
            priority :low
            tags     []}}]
   (let [uri (format "https://%s/%s" server channel)]
     (http/post uri
                {
                 :body message
                 :headers (cond-> {
                                   :Title (or title message)
                                   :Priority (name priority)
                                   :Tags (str/join "," tags)
                                   }
                            (not (str/blank? token))
                            (assoc :Authorization (str "Bearer " token)))
                 }))))

(defprotocol INtfyChan
  (send! [self title msg])
  (alert! [self title msg]))

(defrecord NtfyChan [server channel token]
  INtfyChan
  (send! [_ title msg]
    (send-notification channel msg {:server server :title title :priority :low :token token}))
  (alert! [_ title msg]
    (send-notification channel msg {:server server :title title :priority :max :token token})))

(defn open-channel
  ([chan]           (open-channel "ntfy.sh" chan))
  ([srv chan]       (open-channel srv chan nil))
  ([srv chan token] (NtfyChan. srv chan token)))
