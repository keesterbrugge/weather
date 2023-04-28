(ns ^:figwheel-hooks learn-cljs.weather
  (:require
   [goog.dom :as gdom]
   [reagent.dom :as rdom]
   [reagent.core :as r]
   [ajax.core :as ajax]))

(defonce app-state (r/atom {:title "WhichWeather"          ;; <1>
                            :postal-code ""
                            :temperatures {:today {:label "Today"
                                                   :value nil}
                                           :tomorrow {:label "Tomorrow"
                                                      :value nil}}}))

(def api-key "f5ecb5faef74cda72839f9664ce85e0c")

(defn handle-response [resp]                               ;; <2>
  (let [today (get-in resp ["list" 0 "main" "temp"])
        tomorrow (get-in resp ["list" 8 "main" "temp"])]
    (swap! app-state
           update-in [:temperatures :today :value] (constantly today))
    (swap! app-state
           update-in [:temperatures :tomorrow :value] (constantly tomorrow))))

(defn get-forecast! []                                     ;; <3>
  (let [postal-code (:postal-code @app-state)]
    (ajax/GET "http://api.openweathermap.org/data/2.5/forecast"
      {:params {"q" (js/parseInt postal-code)
                "units" "metric" ;; alternatively, use "metric"
                "appid" api-key}
       :handler handle-response})))

(defn title []                                             ;; <4>
  [:h1 (:title @app-state)])

(defn temperature [temp]
  [:div {:class "temperature"}
   [:div {:class "value"}
    (:value temp)]
   [:h2 (:label temp)]])

(defn go-button []
  [:button {:on-click get-forecast!} "Go"])

(defn postal-code []
  [:div {:class "postal-code"}
   [:h3 "Enter your postal code"]
   [:input {:type "text"
            :placeholder "Postal Code"
            :value (:postal-code @app-state)
            :on-change #(swap! app-state assoc :postal-code (-> % .-target .-value))}]
   [go-button]])

(defn app []
  [:div {:class "app"}
   [title]
   [:div {:class "temperatures"}
    (for [temp (vals (:temperatures @app-state))]
      [temperature temp])]
   [postal-code]])

(defn mount-app-element []                                 ;; <5>
  (rdom/render [app] (gdom/getElement "app")))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))



;; (defn my-ajax-call [] 
;;   ((let [postal-code (:postal-code @app-state)]
;;      (ajax/GET "http://api.openweathermap.org/data/2.5/forecast"
;;        {:params {"q" postal-code
;;                  "units" "metric" ;; alternatively, use "metric"
;;                  "appid" api-key}
;;         :handler println}))))