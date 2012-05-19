(ns ring.middleware.refresh
  (:use [compojure.core :only (routes GET)]
        [watchtower.core :only (watcher rate on-change)]
        ring.middleware.params)
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.util Date UUID]))

(defn- get-request? [request]
  (= (:request-method request) :get))

(defn- success? [response]
  (<= 200 (:status response) 299))

(defn- html-content? [response]
  (re-find
   #"text/html"
   (get-in response [:headers "Content-Type"])))

(def ^:private refresh-script
  (slurp (io/resource "ring/js/refresh.js")))

(defn- add-script [body script]
  (str/replace
   body
   #"<head\s*[^>]*>"
   #(str % "<script type=\"text/javascript\">" script "</script>")))

(def ^:private last-modified
  (atom (Date.)))

(defn- watch-dirs! [dirs]
  (watcher dirs
   (rate 100)
   (on-change
    (fn [_] (reset! last-modified (Date.))))))

(defn- random-uuid []
  (str (UUID/randomUUID)))

(defn- watch-until [reference pred timeout-ms]
  (let [result    (promise)
        watch-key (random-uuid)]
    (try
      (add-watch reference
                 watch-key
                 (fn [_ _ _ value]
                   (when (pred value)
                     (deliver result true))))
      (or (pred @reference)
          (deref result timeout-ms false))
      (finally
       (remove-watch reference watch-key)))))

(def ^:private source-changed-route
  (GET "/__source_changed" [since]
    (let [timestamp (Long. since)]
      (str (watch-until
            last-modified
            #(> (.getTime %) timestamp)
            60000)))))

(defn- wrap-with-script [handler script]
  (fn [request]
    (let [response (handler request)]
      (if (and (get-request? request)
               (success? response)
               (html-content? response))
        (update-in response [:body] add-script script)
        response))))

(defn wrap-refresh
  "Injects Javascript into HTML responses which automatically refreshes the
  browser when any file in the supplied directories is modified. Only successful
  responses from GET requests are affected. The default directories are 'src'
  and 'resources'."
  ([handler]
     (wrap-refresh handler ["src" "resources"]))
  ([handler dirs]
     (watch-dirs! dirs)
     (routes
      (wrap-params source-changed-route)
      (wrap-with-script handler refresh-script))))
