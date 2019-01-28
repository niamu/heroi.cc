# heroi.cc

[heroi.cc](https://heroi.cc) is a [Clojure](https://clojure.org)
([Datomic Ion](https://docs.datomic.com/cloud/ions/ions.html)) web app
that reveals common games amongst
[Steam](https://store.steampowered.com) friends. This is especially
useful when organizing LAN parties and game nights with friends.

## Usage

### Setup

#### Datomic Cloud

Follow the [Setting
Up](https://docs.datomic.com/cloud/setting-up.html) instructions for
Datomic Cloud.

#### Steam Web API Key

heroi.cc fetches data from the Steam Web API which requires an API Key
that can be obtained here: https://steamcommunity.com/dev/apikey.

Once your key has been obtained, use the `bin/put-steam-api-key` with
your key as the only argument to populate the [AWS Parameter
Store](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-paramstore.html).

### Pushing and Deploying

`bin/push` will use `./resources/datomic/ion-config.edn` to push a
specific revision of the app to be deployed. The successful completion
of this script will result in an
[EDN](https://github.com/edn-format/edn) response containing the
`:deploy-command` key. Execute the deploy command and wait for
successful deployment.

### API Gateway and Custom Domain Name

Optionally setup an [API
Gateway](https://docs.datomic.com/cloud/ions/ions-tutorial.html#sec-5-2)
and [Custom Domain
Name](https://docs.aws.amazon.com/apigateway/latest/developerguide/how-to-custom-domains.html).

## Development

### REPL

`bin/repl` creates an [nREPL](https://nrepl.org/nrepl/index.html)
connection that can be connected to with your favourite editor.

### SOCKS Proxy

Setup a [SOCKS
Proxy](https://docs.datomic.com/cloud/getting-started/connecting.html#socks-proxy)
to your EC2 instance for local development against your Datomic Cloud
instance.

## License

Copyright © 2019 Brendon Walsh.

Licensed under the EPL (see the file LICENSE).
