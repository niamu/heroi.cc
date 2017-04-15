(ns fume.util-test
  (:require [fume.util :as util]
            [om.dom :as dom]
            #?(:cljs [cljs.test :as t :refer-macros [is are deftest testing]]
               :clj  [clojure.test :as t :refer [is are deftest testing]])))

(deftest dom
  (testing "single node"
    (are [x y] (= x y)
      (util/dom [:div])
      (dom/div nil nil)

      (util/dom [:span "test"])
      (dom/span nil "test")

      (util/dom [:input {:type "text"}])
      (dom/input {:type "text"})))

  (testing "children nodes"
    (are [x y] (= x y)
      (util/dom [:div [:div]])
      (dom/div nil (dom/div nil nil))

      (util/dom [:div [:span "test"]])
      (dom/div nil (dom/span nil "test"))

      (util/dom [:div {:data-test "test"}
                 [:h1 "heading test"]
                 [:ul
                  [:li "point 1"]
                  [:li "point 2"]]])
      (dom/div {:data-test "test"}
               (dom/h1 nil "heading test")
               (dom/ul nil
                       (dom/li nil "point 1")
                       (dom/li nil "point 2")))))

  (testing "IDs and Classes"
    (are [x y] (= x y)
      (util/dom [:div.test])
      (dom/div {:className "test"})

      (util/dom [:div#test])
      (dom/div {:id "test"})

      (util/dom [:div#test.testing])
      (dom/div {:id "test" :className "testing"})

      (util/dom [:div#test {:class "testing"}])
      (dom/div {:id "test" :className "testing"})

      (util/dom [:div {:id "test" :class "testing"}])
      (dom/div {:id "test" :className "testing"}))))

(t/run-all-tests)
