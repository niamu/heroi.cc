# Fume

Fume is the Clojure(Script) source code for the web app [Heroi.cc](http://heroi.cc).

Fume allows users to login to [Steam](http://store.steampowered.com) via OpenID to reveal all common games between selected friends.


## Documentation

Documentation is currently lacking. There are minimal docstrings throughout the source code. With time, the flow of the application will be documented in greater detail.

Be sure to rename the "resources/sample_config.edn" file to "resources/config.edn" and add your own Steam Developer API key.

The basic application flow is that when a user logs in, their user information, games, friends, and friends' games all get fetched and stored into a DataScript database.

Each page is rendered on the server as well as renderable on the client for maximum performance. Special care was taken to ensure that the same functionality is available in the app with or without JavaScript enabled on the client's browser.


## Deploying

The Dockerfile produces an uberjar and exposes the web server on port 8080.

There's also an nREPL server that starts with the web server on the default port 7888. This port is intentionally not exposed from the Docker container for security purposes.


## Development

[Figwheel](https://github.com/bhauman/lein-figwheel) is used for hot code reloading and can be started with the following script:

```BASH
lein run -m clojure.main script/figwheel.clj
```
