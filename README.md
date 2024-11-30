[![Build Status](https://travis-ci.org/threatgrid/ring-graphql-ui.svg?branch=master)](https://travis-ci.org/threatgrid/ring-graphql-ui)

# Ring-Graphql-UI

GraphQL UI for Ring apps:

- [GraphiQL](https://github.com/shahankit/custom-graphiql/) based on [CustomGraphiQL]()
- [GraphQL Voyager](https://github.com/APIs-guru/graphql-voyager)
- [Graphql Playground](https://github.com/prisma-labs/graphql-playground)

## Usage

The `wrap-graphiql` middleware serves the GraphiQL UI, `wrap-voyager` serves the GraphQL Voyager UI and the `wrap-playground` serves the Graphql playground UI .

```clojure
(def app
 (-> handler
     (wrap-graphiql {:path "/graphiql"
                     :endpoint "/graphql"})
     (wrap-voyager {:path "/voyager"
                    :endpoint "/graphql"})
     (wrap-playground {:path "/playground"
                       :endpoint "/graphql"})))
```

They provides the following options:

- `path`:The path to the GraphiQL or GraphQL Voyager UI
- `endpoint`:The GraphQL endpoint URL

## License

### CustomGraphiQL

shahankit/custom-graphiql is licensed under the [MIT License](https://github.com/shahankit/custom-graphiql/blob/master/LICENSE)

### GraphQL Voyager

APIs-guru/graphql-voyager is licensed under the [MIT License](https://github.com/APIs-guru/graphql-voyager/blob/master/LICENSE)

### GraphQL Playground

prisma-labs/graphql-playground is licensed under the [MIT License](https://github.com/prisma-labs/graphql-playground/blob/master/LICENSE)

### Ring-Graphql-UI

Copyright © 2015-2016 Cisco Systems

Eclipse Public License v1.0

