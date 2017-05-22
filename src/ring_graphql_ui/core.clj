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
   conf-js-fn]
  (let [f (fn [{request-uri :uri :as req}]
            (let [;; Prefix path with servlet-context and compojure context
                  uri (join-paths (:context req) path)]
              ;; Check if requested uri is under swagger-ui path and what file is requested
              (when-let [req-path (get-path uri request-uri)]
                (condp = req-path
                  "" (http-response/found (join-paths request-uri "index.html"))
                  "conf.js" (http-response/content-type (http-response/ok (conf-js-fn options)) "application/javascript")
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

;;------- GraphiQL

(defn graphiql-conf-js
  "Generates GraphiQL js conf file as a string"
  [opts]
  (let [endpoint (:endpoint opts "graphql")
        conf (-> opts
                 (dissoc :root :path)
                 (assoc :endpoint endpoint))]
    (str "window.GRAPHIQL_CONF = " (json/generate-string conf) ";")))

(defn graphiql
  "Returns a Ring handler which can be used to serve GraphiQL"
  ([] (graphiql {}))
  ([options]
   {:pre [(map? options)]}
   (-> (serve (into {:path "/graphiql"
                     :root "graphiql"}
                    options)
              graphiql-conf-js)
       (content-type/wrap-content-type options)
       (not-modified/wrap-not-modified)
       (head/wrap-head))))

(defn wrap-graphiql
  "Middleware to serve GraphiQL."
  ([handler]
   (wrap-ui handler graphiql))
  ([handler options]
   (wrap-ui handler graphiql options)))
