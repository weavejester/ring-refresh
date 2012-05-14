(ns ring.middleware.refresh
  (:require [clojure.string :as str]))

(defn- get-request? [request]
  (= (:request-method request) :get))

(defn- success? [response]
  (<= 200 (:status response) 299))

(defn- html-content? [response]
  (re-find
   #"text/html"
   (get-in response [:headers "Content-Type"])))

(defn add-refresh-script [body]
  (str/replace
   body
   #"<head\s*[^>]*>"
   #(str % "<script></script>")))

(defn wrap-refresh
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (and (get-request? request)
               (success? response)
               (html-content? response))
        (update-in response [:body] add-refresh-script)
        response))))
