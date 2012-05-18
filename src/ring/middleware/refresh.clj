(ns ring.middleware.refresh
  (:use [compojure.core :only (routes GET)]
        [ns-tracker.core :only (ns-tracker)])
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

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

(def changed-files
  (ns-tracker ["src"]))

(defn wrap-refresh
  [handler]
  (routes
   (GET "/__source_changed" []
     (if (empty? (changed-files))
       "false"
       "true"))
   (fn [request]
     (let [response (handler request)]
       (if (and (get-request? request)
                (success? response)
                (html-content? response))
         (update-in response [:body] add-script refresh-script)
         response)))))
