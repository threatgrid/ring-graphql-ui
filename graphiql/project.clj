(defproject threatgrid/graphiql "0.1.0-SNAPSHOT"
  :description "GraphiQL UI"
  :url "https://github.com/threatgrid/ring-graphiql"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :resource {:resource-paths ["ext/custom-graphiql" "resources/graphiql"]
             :target-path "target/resources/graphiql"
             :skip-stencil [ #".*" ]
             :includes [#"ext/custom-graphiql/custom-graphiql\.css"
                        #"ext/custom-graphiql/custom-graphiql\.min.js"
                        #".*react\.min\.js"
                        #".*react-dom\.min.js"
                        #"resources/graphiql/index\.html"]}
  :resource-paths ["target/resources"]
  :hooks [leiningen.resource]
  :profiles {:dev {:plugins [[lein-resource "16.9.1"]]}})
