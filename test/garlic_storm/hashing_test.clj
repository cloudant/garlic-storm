(ns garlic-storm.hashing-test
  (:require [clojure.test :refer :all]
            [garlic-storm.hashing :refer :all]))

(deftest on-the-hash-function
  (testing "It behaves just like graphites"
    (is (= (md5-hash "('192.168.0.18', 2):1") 55736))
    (is (= (md5-hash "memory.active") 13347))
    (is (= (md5-hash "memory.free") 23055))))

(deftest on-hashing-nodes
  (testing "It behaves just like graphites"
    (let [node1 ["192.168.0.18" 1]
          node2 ["192.168.0.18" 2]]
      (is (= (first (hash-node node1 1)) [36935 [node1]]))
      (is (= (first (hash-node node2 1)) [23870 [node2]])))))

(deftest creating-rings
  (testing "Creating a ring with replicas <= 0 results in empty ring"
    (is (= {} (make-ring ["node1" "node2"] 0)))
    (is (= {} (make-ring ["node1" "node2"] -5))))

  (testing "Creating a ring with no nodes results in empty ring"
    (is (= {} (make-ring [] 5)))
    (is (= {} (make-ring nil 5))))

  (testing "A ring is a sorted-map where k1 < k2 < ..."
    (let [ring (make-ring [["192.168.0.18" 1 24001]
                           ["192.168.0.18" 2 24002]]
                          100)]
      (loop [entry1 (first ring) entry2 (second ring) ring (drop 2 ring)]
        (when (and entry1 entry2 ring)
          (is (< (first entry1) (first entry2)))
          (recur (first ring) (second ring) (drop 2 ring)))))))

(deftest adding-nodes

  (testing "Adding a node returns a ring with entries for it"
    (let [replicas 3
          foo-bar-ring (make-ring ["foo" "bar"] replicas)
          foo-ring (make-ring ["foo"] replicas)]
      (is (= foo-bar-ring (add-node foo-ring "bar" replicas))))))

(deftest just-like-graphites-hashing
  (testing "Should return the same node as graphite's hashing."
    ;;; NOTE: graphite relays use strings for the service instance
    (let [node1 ["192.168.0.18" "1"]
          node2 ["192.168.0.18" "2"]
          ring (make-ring [node1 node2] 100)]
      (is (= node1 (node-for ring "net.macbook.ulises.memory.inactive")))
      (is (= node1 (node-for ring "net.macbook.ulises.memory.free")))
      (is (= node2 (node-for ring "net.macbook.ulises.load.longterm")))
      (is (= node2 (node-for ring "net.macbook.ulises.df.root.df_complex.used")))))

  (testing "Should be able to hash [service-name instance]"
    (let [ring (make-ring [["service" 1] ["another-service" 1]] 5)]
      (is (= ["another-service" 1] (node-for ring "foo"))))))
