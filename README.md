[![Build Status](https://travis-ci.org/threatgrid/ring-graphql-ui.svg?branch=master)](https://travis-ci.org/threatgrid/ring-graphql-ui)

# Ring-Graphql-UI

Provides GraphiQL UI for Ring apps.

## Usage

The `wrap-graphiql` middleware will serve the GraphiQL UI

```clojure
(def app
 (-> handler
     (wrap-graphiql {:path "/graphiql"
                     :endpoint "ctia/graphql"})))
```

It provides the following options:

- `path`:The path for the GraphiQL UI
- `endpoint`:The relative url of the GraphQL endpoint

## License

Copyright © 2015-2016 Cisco Systems

Eclipse Public License v1.0

