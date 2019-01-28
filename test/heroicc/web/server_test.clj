(ns heroicc.web.server-test
  (:require [datomic.ion.lambda.api-gateway :as apigw]
            [heroicc.web.server :as server]
            [clojure.test :as t]))

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
                 apigw/gateway->edn
                 (assoc :query-string
                        (str "openid.identity=https%3A%2F%2Fsteamcommunity.com"
                             "%2Fopenid%2Fid%2F76561197997007156"))
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
                 apigw/gateway->edn
                 (assoc :uri "/games")
                 (assoc :query-string
                        (str "openid.identity=https%3A%2F%2Fsteamcommunity.com"
                             "%2Fopenid%2Fid%2F76561197997007156"
                             "&steamid=76561198073236340"
                             "&steamid=76561197997007156"))
                 server/app-handler
                 (dissoc :body))
             {:status 200
              :headers {"Content-Type" "text/html; charset=utf-8"
                        "X-XSS-Protection" "1; mode=block"
                        "X-Frame-Options" "SAMEORIGIN"
                        "X-Content-Type-Options" "nosniff"}}))))
