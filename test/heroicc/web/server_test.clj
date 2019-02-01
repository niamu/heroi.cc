(ns heroicc.web.server-test
  (:require [clojure.data.json :as json]
            [clojure.test :as t]
            [datomic.ion.lambda.api-gateway :as apigw]
            [heroicc.web.server :as server]))

(t/deftest login-request
  (t/testing "Login request results in 200 response"
    (t/is (= (-> (slurp "resources/test/aws-apigw/login.json")
                 apigw/gateway->edn
                 server/app-handler
                 (dissoc :body))
             {:status 200
              :headers {"Content-Type" "text/html; charset=utf-8"
                        "X-XSS-Protection" "1; mode=block"
                        "X-Frame-Options" "SAMEORIGIN"
                        "X-Content-Type-Options" "nosniff"}}))))

(t/deftest dashboard-login-redirect-request
  (t/testing "Dashboard request results in 302 redirect to login page."
    (t/is (= (-> (slurp "resources/test/aws-apigw/dashboard.json")
                 apigw/gateway->edn
                 server/app-handler
                 (dissoc :body))
             {:status 302
              :headers {"Location" "https://heroi.cc/login"
                        "Content-Type" "application/octet-stream"
                        "X-XSS-Protection" "1; mode=block"
                        "X-Frame-Options" "SAMEORIGIN"
                        "X-Content-Type-Options" "nosniff"}}))))

(t/deftest dashboard-request
  (t/testing "Dashboard"
    (t/is (= (-> (slurp "resources/test/aws-apigw/dashboard.json")
                 (json/read-str :key-fn keyword)
                 (assoc-in ["queryStringParameters"]
                           {"openid.identity"
                            "https://steamcommunity.com/openid/id/76561197997007156"})
                 json/write-str
                 apigw/gateway->edn
                 server/app-handler
                 (dissoc :body))
             {:status 200
              :headers {"Content-Type" "text/html; charset=utf-8"
                        "Set-Cookie" "steamid=76561197997007156;HttpOnly"
                        "X-XSS-Protection" "1; mode=block"
                        "X-Frame-Options" "SAMEORIGIN"
                        "X-Content-Type-Options" "nosniff"}}))))

(t/deftest games-request
  (t/testing "Games"
    (t/is (= (-> (slurp "resources/test/aws-apigw/dashboard.json")
                 (json/read-str :key-fn keyword)
                 (assoc-in [:multiValueQueryStringParameters]
                           {:steamid ["76561197997007156"
                                      "76561197981157470"
                                      "76561197984746117"
                                      "76561197982284209"]
                            :category ["1" "9"]})
                 (assoc-in [:queryStringParameters]
                           {:steamid "76561197982284209"
                            :search "dota"
                            :category "1"})
                 (assoc-in [:headers :Cookie] "steamid=76561197997007156")
                 (assoc :path "/games")
                 json/write-str
                 apigw/gateway->edn
                 server/app-handler
                 (dissoc :body))
             {:status 200
              :headers {"Content-Type" "text/html; charset=utf-8"
                        "X-XSS-Protection" "1; mode=block"
                        "X-Frame-Options" "SAMEORIGIN"
                        "X-Content-Type-Options" "nosniff"}}))))
