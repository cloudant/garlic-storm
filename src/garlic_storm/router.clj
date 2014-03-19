(ns garlic-storm.router
  (:require [garlic-storm.hashing :refer [make-ring add-node remove-node
                                          node-for]]))

(defn get-route [{:keys [ring ports replicas] :as routes} metric]
  {:pre [replicas]}
  (when (and ring ports)
    (if-let [server-instance (node-for ring metric)]
      [(first server-instance) (get ports server-instance)])))

(defn add-route [{:keys [ring ports replicas] :as routes} {:keys [service instance port]}]
  {:pre [service instance port replicas]}
  (if (contains? ports [service instance]) routes
      (merge routes {:ring (add-node ring [service instance] replicas)
                     :ports (merge ports {[service instance] port})})))

(defn routes [services replicas]
  (reduce (fn [acc [service instance port]] (add-route acc {:service service
                                                           :instance instance
                                                           :port port}))
          {:replicas replicas :ring (sorted-map)}
          services))
