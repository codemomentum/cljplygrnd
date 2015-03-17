(require
  '[ring.util.response :as resp]
  '[compojure.route :as route]
  '[ring.adapter.jetty :as jetty])
(use
  '[ring.middleware.multipart-params :only [wrap-multipart-params]]
  '[ring.util.response :only [header]]
  '[compojure.core :only [context ANY routes defroutes]]
  '[compojure.handler :only [api]])

(defn assemble-routes []
  (->
    (routes
      (ANY "/" [] (resp/redirect "/index.html"))
      (route/resources "/"))))

(def handler
  (-> (assemble-routes)))

(defn start [options]
  (jetty/run-jetty #'handler (assoc options :join? false)))

(defn -main
  ([port]
   (start {:port (Integer/parseInt port)}))
  ([]
   (-main "3000")))