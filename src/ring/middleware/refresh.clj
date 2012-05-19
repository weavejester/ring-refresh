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

(def ^:private refresh-script
  (slurp (io/resource "ring/js/refresh.js")))

(defn add-script [body script]
  (str/replace
   body
   #"<head\s*[^>]*>"
   #(str % "<script type=\"text/javascript\">" script "</script>")))

(def ^:private last-modified
  (atom (Date.)))

(defn start-watch! [dirs]
  (watcher dirs
   (rate 100)
   (on-change
    (fn [_] (reset! last-modified (Date.))))))

(def ^:private source-changed-route
  (wrap-params
   (GET "/__source_changed" [since]
     (str (> (.getTime @last-modified)
             (Long. since))))))

(defn wrap-refresh
  ([handler]
     (wrap-refresh handler ["src" "resources"]))
  ([handler dirs]
     (start-watch! dirs)
     (routes
      source-changed-route
      (fn [request]
        (let [response (handler request)]
          (if (and (get-request? request)
                   (success? response)
                   (html-content? response))
            (update-in response [:body] add-script refresh-script)
            response))))))
