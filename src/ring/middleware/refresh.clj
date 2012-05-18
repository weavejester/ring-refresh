(ns ring.middleware.refresh
  (:use [compojure.core :only (routes GET)]
        [watchtower.core :only (watcher rate on-change)]
        ring.middleware.params)
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import java.util.Date))

(defn- get-request? [request]
  (= (:request-method request) :get))

(defn- success? [response]
  (<= 200 (:status response) 299))

(defn- html-content? [response]
  (re-find
   #"text/html"
   (get-in response [:headers "Content-Type"])))

(def refresh-script
  (slurp (io/resource "ring/js/refresh.js")))

(defn add-script [body script]
  (str/replace
   body
   #"<head\s*[^>]*>"
   #(str % "<script type=\"text/javascript\">" script "</script>")))

(def last-modified
  (atom (Date.)))

(defn start-watch! []
  (watcher ["src" "resources"]
   (rate 100)
   (on-change
    (fn [_] (reset! last-modified (Date.))))))

(defn wrap-refresh
  [handler]
  (start-watch!)
  (routes
   (wrap-params
    (GET "/__source_changed" [since]
      (str (> (.getTime @last-modified)
              (Long. since)))))
   (fn [request]
     (let [response (handler request)]
       (if (and (get-request? request)
                (success? response)
                (html-content? response))
         (update-in response [:body] add-script refresh-script)
         response)))))
