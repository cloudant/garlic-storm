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
    (let [routes (routes [["service" "1" 12345]] 5)]
      (is (= ["service" 12345] (get-route routes "metric")))))

  (testing "Getting route for metric matches the returns value from graphite's implementation"
    (let [node1 ["192.168.0.18" "1" 24001]
          node2 ["192.168.0.18" "2" 24002]
          router (routes [node1 node2] 100)]

      ;;; node1 assertions
      (is (= ["192.168.0.18" 24001] (get-route router "carbon.relays.graphite-1.cpuUsage")))

      (is (= ["192.168.0.18" 24001] (get-route router "net.macbook.ulises.load.midterm")))
      (is (= ["192.168.0.18" 24001] (get-route router "net.macbook.ulises.load.shortterm")))

      (is (= ["192.168.0.18" 24001] (get-route router "net.macbook.ulises.memory.free")))
      (is (= ["192.168.0.18" 24001] (get-route router "net.macbook.ulises.memory.inactive")))

      (is (= ["192.168.0.18" 24001] (get-route router "net.macbook.ulises.df.root.df_complex.reserved")))
      (is (= ["192.168.0.18" 24001] (get-route router "net.macbook.ulises.df.root.df_complex.free")))

      ;;; node2 assertions
      (is (= ["192.168.0.18" 24002] (get-route router "carbon.relays.graphite-1.memUsage")))

      (is (= ["192.168.0.18" 24002] (get-route router "net.macbook.ulises.load.longterm")))

      (is (= ["192.168.0.18" 24002] (get-route router "net.macbook.ulises.df.root.df_complex.used")))
      (is (= ["192.168.0.18" 24002] (get-route router "net.macbook.ulises.load.longterm")))

      (is (= ["192.168.0.18" 24002] (get-route router "net.macbook.ulises.memory.active")))
      (is (= ["192.168.0.18" 24002] (get-route router "net.macbook.ulises.memory.wired"))))))

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
      (is (thrown? AssertionError (add-route nil s)))))

  (testing "Re-adding the same route is a noop"
    (let [service {:service "s" :port 9999 :instance "1"}
          route (add-route {:replicas 1 :ring (sorted-map) :ports {}}
                           service)]
      (is (= route (add-route route service))))))
