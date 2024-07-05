(ns ring.middleware.refresh
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn- dir-last-modified-ts [dir]
  (when-let [files (file-seq (io/file dir))]
    (apply max (map #(.lastModified %) files))))

(defn- dirs-last-modified-ts [dir & dirs]
  (->> (cons dir dirs)
       (mapv dir-last-modified-ts)
       (reduce max)))

(defn- get-request? [request]
  (= (:request-method request) :get))

(defn- html-content? [response]
  (when-let [content-type (get-in response [:headers "Content-Type"])]
    (re-find #"text/html" content-type)))

(def ^:private refresh-script
  (slurp (io/resource "ring/js/refresh.js")))

(defprotocol AsString
  (as-str [x]))

(extend-protocol AsString
  String
  (as-str [s] s)
  java.io.File
  (as-str [f] (slurp f))
  java.io.InputStream
  (as-str [i] (slurp i))
  clojure.lang.ISeq
  (as-str [xs] (apply str xs))
  nil
  (as-str [_] nil))

(defn- add-script [body script]
  (when-let [body-str (as-str body)]
    (str/replace
     body-str
     #"</head>"
     #(str "\n<script type=\"text/javascript\" async>\n" script "</script>\n" %))))

(defn- source-changed-handler [_ dirs]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (str (apply dirs-last-modified-ts dirs))})

(defn- intercept-source-changed-route [handler dirs]
  (fn [{:keys [uri] :as request}]
  (if (= uri "/__source_changed")
      (source-changed-handler request dirs)
      (handler request))))

(defn- wrap-with-script [handler script]
  (fn [request]
    (let [response (handler request)]
      (if (and (get-request? request)
               (html-content? response))
        (-> response
            (update-in [:body] add-script script)
            (update-in [:headers] dissoc "Content-Length"))
        response))))

(defn wrap-refresh
  "Injects Javascript into HTML responses which automatically refreshes the
  browser when any file in the supplied directories is modified. Only
  responses from GET requests are affected. The default directories
  are 'src' and 'resources'."
  ([handler]
     (wrap-refresh handler ["src" "resources"]))
  ([handler dirs]
   (fn [request]
     (let [wrapped (-> handler (intercept-source-changed-route dirs) (wrap-with-script refresh-script))]
       (wrapped request)))))
