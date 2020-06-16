(ns ring-graphql-ui.core-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is testing]]
            [ring-graphql-ui.core :as sut]
            [ring.mock.request :as mock]
            [ring.util.response :only [response content-type]]))

(deftest get-path-test
  (is (= "a/b"
         (sut/get-path "" "a/b")))
  (is (= "b"
         (sut/get-path "" "b")))
  (is (= "b/c"
         (sut/get-path "a" "a/b/c")))
  (is (= "c"
         (sut/get-path "a/b" "a/b/c"))))

(deftest join-paths-test
  (is (= "a/b"
         (sut/join-paths "a" "b")))
  (is (= "a/b"
         (sut/join-paths "a/" "/b/")))
  (is (= "a/b/c"
         (sut/join-paths "a/b" "c/"))))

(defn- strip-js [s conf-property]
  (let [pattern (re-pattern (str "window\\."
                                 conf-property
                                 " = (.*);"))
        [_ conf] (re-find pattern s)]
    conf))

(defn- read-js [s conf-property]
  (-> s
      (strip-js conf-property)
      (json/parse-string true)))

(deftest conf-js-test
  (is (= {:endpoint "ctia/graphql"}
         (read-js
          (sut/conf-js
           {:endpoint "ctia/graphql"}
           "GRAPHIQL_CONF")
          "GRAPHIQL_CONF"))))

(defn http-get
  [app uri]
  (app (mock/request :get uri)))

(defn status? [{:keys [status]} expected]
  (= status expected))

(defn redirect? [uri]
  (fn [{{location "Location"} :headers :as res}]
    (and (status? res 302) (= location uri))))

(defn html? [{{content-type "Content-Type"} :headers :as res}]
  (and (status? res 200) (= content-type "text/html")))

(defn javascript? [{{content-type "Content-Type"} :headers :as res}]
  (and (status? res 200) (= content-type "application/javascript")))

(deftest graphiql-test
  (let [handler (sut/graphiql {:path "/graphiql"
                               :endpoint "ctia/graphql"})]
    (testing "index.html"
      (is (redirect? (http-get handler "/graphiql")))
      (is (html? (http-get handler "/graphiql/index.html"))))
    (testing "conf.js"
      (let [conf-response (http-get handler "/graphiql/conf.js")]
        (is (javascript? conf-response))
        (is (= {:endpoint "ctia/graphql"}
               (read-js (:body conf-response)
                        "GRAPHIQL_CONF")))))))

(deftest wrap-graphiql-test
  (let [handler (-> (constantly nil)
                    (sut/wrap-graphiql {:path "/graphiql"
                                        :endpoint "ctia/graphql"}))]
    (is (html? (http-get handler "/graphiql/index.html")))
    (is (nil? (http-get handler "unknown")))))

(deftest voyager-test
  (let [handler (sut/voyager {:path "/voyager"
                              :endpoint "ctia/graphql"})]
    (testing "index.html"
      (is (redirect? (http-get handler "/voyager")))
      (is (html? (http-get handler "/voyager/index.html"))))
    (testing "conf.js"
      (let [conf-response (http-get handler "/voyager/conf.js")]
        (is (javascript? conf-response))
        (is (= {:endpoint "ctia/graphql"}
               (read-js (:body conf-response)
                        "GRAPHQL_VOYAGER_CONF")))))))

(deftest wrap-voyager-test
  (let [handler (-> (constantly nil)
                    (sut/wrap-graphiql {:path "/voyager"
                                        :endpoint "ctia/graphql"}))]
    (is (html? (http-get handler "/voyager/index.html")))
    (is (nil? (http-get handler "unknown")))))

(deftest playground-test
  (let [handler (sut/playground {:path "/playground"
                                 :endpoint "ctia/graphql"})]
    (testing "index.html"
      (is (redirect? (http-get handler "/playground")))
      (is (html? (http-get handler "/playground/index.html"))))
    (testing "conf.js"
      (let [conf-response (http-get handler "/playground/conf.js")]
        (is (javascript? conf-response))
        (is (= {:endpoint "ctia/graphql"}
               (read-js (:body conf-response)
                        "GRAPHQL_PLAYGROUND_CONF")))))))

(deftest wrap-playground-test
  (let [handler (-> (constantly nil)
                    (sut/wrap-graphiql {:path "/playground"
                                        :endpoint "ctia/graphql"}))]
    (is (html? (http-get handler "/playground/index.html")))
    (is (nil? (http-get handler "unknown")))))
