(ns ring-graphql-ui.core
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [ring.util.http-response :as http-response]
            [ring.middleware.content-type :as content-type]
            [ring.middleware.not-modified :as not-modified]
            [ring.middleware.head :as head]))

(defn get-path [root uri]
  (let [pattern (re-pattern (str "^" root "[/]?(.*)"))
        [_ path] (re-find pattern uri)]
    path))

(defn join-paths
  "Join several paths together with \"/\". If path ends with a slash,
   another slash is not added."
  [& paths]
  (-> (str/join "/" (remove nil? paths))
      (str/replace #"([/]+)" "/") ;; remove duplicates slash
      (str/replace #"/$" ""))) ;; remove trailing slash

(defn- serve
  "Serves GraphQL UI conf and static files.
   path: The GraphQL UI endpoint
   root: The root directory in the resource folder containing static files
   conf-js-fn: Fn that generates the js configuration file as a string"
  [{:keys [path root] :as options}
   conf-js]
  (let [f (fn [{request-uri :uri :as req}]
            (let [;; Prefix path with servlet-context and compojure context
                  uri (join-paths (:context req) path)]
              ;; Check if requested uri is under swagger-ui path and what file is requested
              (when-let [req-path (get-path uri request-uri)]
                (condp = req-path
                  "" (http-response/found (join-paths request-uri "index.html"))
                  "conf.js" (http-response/content-type (http-response/ok conf-js) "application/javascript")
                  (http-response/resource-response (str root "/" req-path))))))]
    (fn
      ([request]
       (f request))
      ([request respond _]
        (respond (f request))))))

(defn- wrap-ui
  "Middleware to serve GraphQL UI."
  ([handler ui-fn]
   (wrap-ui handler ui-fn {}))
  ([handler ui-fn options]
   (let [ui (ui-fn options)]
     (fn
       ([request]
        ((some-fn ui handler) request))))))

(defn conf-js
  "Generates GraphiQL js conf file as a string"
  [opts js-prop]
  (let [endpoint (:endpoint opts "graphql")
        conf (-> opts
                 (dissoc :root :path)
                 (assoc :endpoint endpoint))]
    (str "window."
         js-prop
         " = " (json/generate-string conf) ";")))

(defn- serve-ui
  [default-options options js-prop]
  (let [opts (into default-options
                   options)]
    (-> (serve opts
               (conf-js opts js-prop))
        (content-type/wrap-content-type options)
        (not-modified/wrap-not-modified)
        (head/wrap-head))))

;;------- GraphiQL

(defn graphiql
  "Returns a Ring handler which can be used to serve GraphiQL"
  ([] (graphiql {}))
  ([options]
   (serve-ui {:path "/graphiql"
              :root "graphiql"}
             options
             "GRAPHIQL_CONF")))

(defn wrap-graphiql
  "Middleware to serve GraphiQL."
  ([handler]
   (wrap-ui handler graphiql))
  ([handler options]
   (wrap-ui handler graphiql options)))

;;-------- GraphQL Voyager

(defn voyager
  "Returns a Ring handler which can be used to serve GraphQL Voyager"
  ([] (voyager {}))
  ([options]
   (serve-ui {:path "/voyager"
              :root "graphql-voyager"}
             options
             "GRAPHQL_VOYAGER_CONF")))

(defn wrap-voyager
  "Middleware to serve GraphQL Voyager."
  ([handler]
   (wrap-ui handler voyager))
  ([handler options]
   (wrap-ui handler voyager options)))

;;-------- GraphQL Playground

(defn playground
  "Returns a Ring handler which can be used to serve GraphQL Playground https://github.com/prisma-labs/graphql-playground#as-html-page"
  ([] (playground {}))
  ([options]
   (serve-ui {:path "/playground"
              :root "graphql-playground"}
             options
             "GRAPHQL_PLAYGROUND_CONF")))

(defn wrap-playground
  "Middleware to serve GraphQL Playground."
  ([handler]
   (wrap-ui handler playground))
  ([handler options]
   (wrap-ui handler playground options)))
