(ns garlic-storm.router-test
  (:require [clojure.test :refer :all]
            [garlic-storm.router :refer :all]))

(deftest on-empty-routes
  (testing "Getting route for metric from empty routes returns nothing"
    (is (nil? (get-route {:replicas 1} "some.service.com"))))

  (testing "Getting route for metrics from nil/empty routes throws"
    (is (thrown? AssertionError (get-route nil "service")))
    (is (thrown? AssertionError (get-route {} "service")))))

(deftest on-getting-routes
  (testing "Getting route for metric returns a [service port] pair"
    (let [routes (routes [["service" 1 12345]] 5)]
      (is (= ["service" 12345] (get-route routes "metric")))))

  (testing "Getting route for metric matches the returns value from graphite's implementation"
    (let [routes (routes [["service" 1 12345] ["another" 1 9999]] 5)]
      (is (= ["another" 9999] (get-route routes "metric"))))

    (let [routes (routes [["192.168.0.18" 1 24001] ["192.168.0.18" 2 24002]] 1)]
      (is (= ["192.168.0.18" 24001] (get-route routes "load.shortterm")))
      (is (= ["192.168.0.18" 24002] (get-route routes "df.root.df_complex.free"))))))

(deftest on-adding-routes
  (testing "Adding a route with a nil port throws"
    (is (thrown? AssertionError (add-route {:replicas 1} {:service "s" :instance 1}))))

  (testing "Adding a route with no service throws"
    (is (thrown? AssertionError (add-route {:replicas 1} {:instance 1 :port 9999}))))

  (testing "Adding a route with no instance throws"
    (is (thrown? AssertionError (add-route {:replicas 1} {:service "s" :port 9999}))))

  (testing "Adding a route to an invalid routes-map throws"
    (let [s {:service "s" :instance 1 :port 9999}]
      (is (thrown? AssertionError (add-route {} s)))
      (is (thrown? AssertionError (add-route nil s))))))
