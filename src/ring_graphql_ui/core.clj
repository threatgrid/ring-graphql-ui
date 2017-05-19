(ns ring-graphql-ui.core
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [ring.util.http-response :as http-response]
            [ring.middleware.content-type :as content-type]
            [ring.middleware.not-modified :as not-modified]
            [ring.middleware.head :as head]))

(defn get-path [root uri]
  (second (re-find (re-pattern (str "^" root "[/]?(.*)")) uri)))

(defn java-invoke
  "Invokes a Java object method via reflection "
  [class-name method-name object]
  (.invoke
   (.getMethod
    (Class/forName class-name)
    method-name
    (into-array Class []))
   object
   (object-array 0)))

(defn context
  "Context of a request. Defaults to \"\", but has the
   servlet-context in the legacy app-server environments."
  [{:keys [servlet-context]}]
  (if servlet-context
    (java-invoke "javax.servlet.ServletContext" "getContextPath" servlet-context)
    ""))

(defn join-paths
  "Join several paths together with \"/\". If path ends with a slash,
   another slash is not added."
  [& paths]
  (str/replace (str/replace (str/join "/" (remove nil? paths)) #"([/]+)" "/") #"/$" ""))

(defn- json-key [k]
  (name k))

(defn conf-js [req opts]
  (let [endpoint (join-paths (context req) (:endpoint opts "/graphql"))
        conf (-> opts
                 (assoc :endpoint endpoint))]
    (str "window.GRAPHIQL_CONF = " (json/generate-string conf {:key-fn json-key}) ";")))

(defn- serve [{:keys [path root] :or {path "/", root "graphiql"} :as options}]
  (let [f (fn [{request-uri :uri :as req}]
            (let [;; Prefix path with servlet-context and compojure context
                  uri (join-paths (:context req) path)]
              ;; Check if requested uri is under swagger-ui path and what file is requested
              (when-let [req-path (get-path uri request-uri)]
                (condp = req-path
                  "" (http-response/found (join-paths request-uri "index.html"))
                  "conf.js" (http-response/content-type (http-response/ok (conf-js req options)) "application/javascript")
                  (http-response/resource-response (str root "/" req-path))))))]
    (fn
      ([request]
       (f request))
      ([request respond _]
        (respond (f request))))))

(defn graphiql
  ([] (graphiql {}))
  ([options]
   {:pre [(map? options)]}
   (-> (serve options)
       (content-type/wrap-content-type options)
       (not-modified/wrap-not-modified)
       (head/wrap-head))))
