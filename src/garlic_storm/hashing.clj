(ns garlic-storm.hashing
  (:require [clojure.string :as string])
  (:import (java.security MessageDigest)))

;;; Taken mostly from
;;; http://nakkaya.com/2010/05/05/consistent-hashing-with-clojure/
;;; with the exception of the hash function which behaves like the one
;;; used in
;;; https://github.com/graphite-project/carbon/blob/master/lib/carbon/hashing.py#L16

(defn- zero-pad [digest]
  (string/join (reverse (into (repeat (- 32 (count digest)) "0") digest))))

(defn md5-hash
  "Calculate hash for the given object. This implementation follows closely
   that of graphite's compute_ring_position() in hashing.py"
  [o]
  (let [bytes (.getBytes (with-out-str (pr o)))
        digest (.digest (MessageDigest/getInstance "MD5")
                        (.getBytes o))
        hexdigest (.toString (BigInteger. 1 digest) 16)]
    (Integer/parseInt (subs (zero-pad hexdigest) 0 4) 16)))

(defn pythonise [node]
  (-> (str node)
      (string/replace "\"" "'")
      (string/replace "]" ")")
      (string/replace "[" "(")
      (string/replace " " ", ")))

(defn hash-node [node replicas]
  (let [str-node (pythonise node)]
    (map #(sorted-map (md5-hash (str str-node ":" %)) node) (range replicas))))

(defn add-node [ring node replicas]
  (apply merge ring (hash-node node replicas)))

(defn remove-node [ring node replicas]
  (apply dissoc ring (map first (map keys (hash-node node replicas)))))

(defn make-ring [nodes replicas]
  (reduce (fn [ring node] (add-node ring node replicas)) (sorted-map) nodes))

(defn- tail-map [ring hash]
  (filter #(< 0 (compare (key %) hash)) ring))

(defn node-for [ring o]
  (let [hash (md5-hash o)
        tmap (tail-map ring hash)]
    (if (empty? tmap)
      (val (first ring))
      (val (first tmap)))))
