(ns garlic-storm.hashing-test
  (:require [clojure.test :refer :all]
            [garlic-storm.hashing :refer :all]))

(deftest on-the-hash-function
  (testing "It behaves just like graphites"
    (is (= (md5-hash "('192.168.0.18', 2):1") 55736))))

(deftest on-hashing-nodes
  (testing "It behaves just like graphites"
    (is (= (first (hash-node ["192.168.0.18" 2] 1)) {23870 ["192.168.0.18" 2]}))))

(deftest creating-rings
  (testing "Creating a ring with replicas <= 0 results in empty ring"
    (is (= {} (make-ring ["node1" "node2"] 0)))
    (is (= {} (make-ring ["node1" "node2"] -5))))

  (testing "Creating a ring with no nodes results in empty ring"
    (is (= {} (make-ring [] 5)))
    (is (= {} (make-ring nil 5))))

  (testing "A ring is a sorted-map where k1 < k2 < ..."
    (let [ring (make-ring ["foo" "bar"] 3)]
      (loop [entry1 (first ring) entry2 (second ring) ring (drop 2 ring)]
        (when (and entry1 entry2 ring)
          (is (< (first entry1) (first entry2)))
          (recur (first ring) (second ring) (drop 2 ring)))))))

(deftest adding-removing-nodes
  (testing "Removing a node returns a ring with no entries for it"
    (let [replicas 3
          ring (make-ring ["foo"] replicas)
          ring-with-bar (add-node ring "bar" replicas)]
      (is (= ring (remove-node ring-with-bar "bar" replicas)))))

  (testing "Adding a node returns a ring with entries for it"
    (let [replicas 3
          foo-bar-ring (make-ring ["foo" "bar"] replicas)
          foo-ring (make-ring ["foo"] replicas)]
      (is (= foo-bar-ring (add-node foo-ring "bar" replicas))))))

(deftest just-like-graphites-hashing
  (testing "Should return the same node as graphite's hashing"
    (let [ring (make-ring ["foo" "bar"] 5)]
      (is (= "bar" (node-for ring "hi")))
      (is (= "foo" (node-for ring "some.service.com")))))

  (testing "Should be able to hash [service-name instance]"
    (let [ring (make-ring [["service" 1] ["another-service" 1]] 5)]
      (is (= ["another-service" 1] (node-for ring "foo"))))))
