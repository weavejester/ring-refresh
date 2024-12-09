# Ring-Refresh [![Build Status](https://github.com/weavejester/ring-refresh/actions/workflows/test.yml/badge.svg)](https://github.com/weavejester/ring-refresh/actions/workflows/test.yml)

Ring-Refresh is a middleware library for Ring that automatically
triggers a browser refresh when your source files change.

It achieves this by injecting a small Javascript script into any HTML
response triggered by a `GET` route, which then periodically calls back
to a route which it also adds to the server, which tells it whether any source
files have changed and as such, whether it should then reload the page.

This library is designed for use only in development environments.

Note that, in respect of clojure source files already running in a jvm, 
you may also wish to use the ring middleware wrap-reload, which also
detects source file changes but in its case, injects changed namespaces
into the running jvm.

Ring-refresh itself will reload static files only and will not touch any running JVM.

## Compatibility with Retit

Currently there is a question mark over whether this library is compatible
with the reitit router. (however wrap-reload seems to work fine).

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

## License

Copyright © 2024 James Reeves

Distributed under the Eclipse Public License, the same as Clojure.
