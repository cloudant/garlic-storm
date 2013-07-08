(defproject garlic-storm "0.0.1-SNAPSHOT"
  :description "An implementation of graphite relays"

  :url "http://github.com/ulises/garlic-storm"
  :license {:name "Apache License Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.txt"
            :distribution :repo}

  :dependencies [[org.clojure/clojure "1.5.1"]]

  :global-vars {*warn-on-reflection* true}

  :deploy-branches ["master"]

  :repositories [["releases" {:url "https://clojars.org/repo"
                              :creds :gpg}]
                 ["snapshots" {:url "https://clojars.org/repo"
                               :creds :gpg}]]

  :aot :all)
