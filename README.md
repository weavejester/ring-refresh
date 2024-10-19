# Ring-Refresh [![Build Status](https://github.com/weavejester/ring-refresh/actions/workflows/test.yml/badge.svg)](https://github.com/weavejester/ring-refresh/actions/workflows/test.yml)

Ring-Refresh is a middleware library for Ring that automatically
triggers a browser refresh when your source files change.

It achieves this by injecting a small Javascript script into any HTML
response triggered by a `GET` route.

This library is designed for use only in development environments.

## Installation

Add the following development dependency to your `project.clj` file:

    [ring-refresh "0.2.0"]

## Usage

By default, the middleware monitors the `src` and `resources` directories:

```clojure
(use 'ring.middleware.refresh)

(def app
  (wrap-refresh your-handler))
```

But it can be customized to include other directories:

```clojure
(def app
  (wrap-refresh
   your-handler
   ["src" "resources" "checkouts/foo/src"]))
```
### Caveats

Do note this library will only refresh the browser and nothing else. This means updates on the code will trigger a browser refresh, but the code itself won't be reloaded. 

This is good when the assets you are modifying reside in `resources/` such as html, css and js files. However, sometimes you might reach for tools such as [hiccup](https://github.com/weavejester/hiccup) for HTML templating. Under these scenarios, `ring-refresh` needs something else to reload the code for it.

The following is an example using `ring.middleware.reload` to reload the code when you modify the `hiccup` template. You can alternatively reload with the REPL.

```clojure
(ns test.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.refresh :refer [wrap-refresh]]
            [hiccup.page :refer [html5]]))

(defn home-page []
  (html5
   [:head
    [:title "Ring Refresh Example"]]
   [:body
    [:p "This page will auto-refresh when you change the source code."]
    [:p "Try changing something and saving the file, and you'll see the update automatically."]]))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (home-page)})

(def app
  (-> handler
      wrap-refresh))

(defn -main []
  (run-jetty (wrap-reload #'app) {:port 3000}))
```

## License

Copyright © 2024 James Reeves

Distributed under the Eclipse Public License, the same as Clojure.
