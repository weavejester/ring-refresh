# Ring-Refresh

Ring-Refresh is a middleware library for Ring that automatically
triggers a browser refresh when your source files change.

It achieves this by injecting a small Javascript script into any HTML
response triggered by a `GET` route.

This library is designed for use only in development environments.

## Installation

Add the following development dependency to your `project.clj` file:

    [ring-refresh "0.1.3"]

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

## License

Copyright © 2022 James Reeves

Distributed under the Eclipse Public License, the same as Clojure.
