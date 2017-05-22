(defproject threatgrid/ring-graphql-ui "0.1.0-SNAPSHOT"
  :description "GraphQL UI for Ring apps"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.7.1"]
                 [metosin/ring-http-response "0.9.0"]
                 [ring/ring-core "1.6.0"]
                 [threatgrid/ring-graphiql "0.1.0"]]
  :profiles {:test {:dependencies [[ring/ring-mock "0.3.0"]]}})
