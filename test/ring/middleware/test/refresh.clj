(ns ring.middleware.test.refresh
  (:require [clojure.java.io :as io]
            [clojure.test :refer [are deftest testing]]
            [ring.middleware.refresh :refer :all]))

(defn- create-test-handler [body]
  (-> (constantly {:status 200
                   :headers {"Content-Type" "text/html"}
                   :body body})
      wrap-refresh))

(defn- get-test-response-body [handler]
  (:body (handler {:request-method :get, :uri "/"})))

(def ^:private refresh-script
  (slurp (io/resource "ring/js/refresh.js")))

(deftest wrap-refresh-adds-script-after-<head>-test
  (testing "Without a <header> tag"
   (are [result-body body]
        (= result-body (get-test-response-body (create-test-handler body)))
        "<html><body>body</body></html>"
        "<html><body>body</body></html>"

        "<html><body>body head</body></html>"
        "<html><body>body head</body></html>"

        "<html><head/><body>body</body></html>"
        "<html><head/><body>body</body></html>"

        "<html><head /><body>body</body></html>"
        "<html><head /><body>body</body></html>"

        (str "<html><head>"
             "<script type=\"text/javascript\">" refresh-script "</script>"
             "</head><body>body</body></html>")
        (str "<html><head>"
             "</head><body>body</body></html>")

        (str "<html><head>"
             "<script type=\"text/javascript\">" refresh-script "</script>"
             "<title>title</title></head><body>body</body></html>")
        (str "<html><head>"
             "<title>title</title></head><body>body</body></html>")

        (str
         "<html><head attr=\"val\">"
         "<script type=\"text/javascript\">" refresh-script "</script>"
         "<title>title</title></head><body>body</body></html>")
        (str
         "<html><head attr=\"val\">"
         "<title>title</title></head><body>body</body></html>")))

  (testing "With a <header> tag"
   (are [result-body body]
        (= result-body (get-test-response-body (create-test-handler body)))
        "<html><body><header>head</header></body></html>"
        "<html><body><header>head</header></body></html>"

        "<html><head/><body><header>head</header></body></html>"
        "<html><head/><body><header>head</header></body></html>"

        "<html><head /><body><header>head</header></body></html>"
        "<html><head /><body><header>head</header></body></html>"

        (str "<html><head>"
             "<script type=\"text/javascript\">" refresh-script "</script>"
             "</head><body><header>head</header></body></html>")
        (str "<html><head>"
             "</head><body><header>head</header></body></html>")

        (str "<html><head>"
             "<script type=\"text/javascript\">" refresh-script "</script>"
             "<title>t</title></head><body><header>head</header></body></html>")
        (str "<html><head>"
             "<title>t</title></head><body><header>head</header></body></html>")

        (str
         "<html><head attr=\"val\">"
         "<script type=\"text/javascript\">" refresh-script "</script>"
         "<title>t</title></head><body><header>head</header></body></html>")
        (str
         "<html><head attr=\"val\">"
         "<title>t</title></head><body><header>head</header></body></html>"))))
