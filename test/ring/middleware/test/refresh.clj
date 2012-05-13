(ns ring.middleware.test.refresh
  (:use clojure.test
        ring.middleware.refresh))

(deftest test-wrap-refresh
  (is (true? false)))