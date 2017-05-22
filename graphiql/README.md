# GraphiQL
Jar-packaged version of GraphiQL for ring-based clojure web-apps (and other JVM apps).

The GraphiQL integration is based on [CustomGraphiQL](https://github.com/shahankit/custom-graphiql/).

## Usage

Add the dependency to your `project.clj` file
and you have full GraphiQL ready in `/graphiql` on classpath.
You can override the `index.html`-page by putting a new page into your local `resources/graphiql`-directory.
The default URI for the GraphQL endpoint is `/graphql` but this can be changed by copying `resources/graphiql/conf.js` to your projects
resources dir and editing it.

## Packaging

### Initialize the submodule
```Shell
git submodule init
git submodule update
```

### New CustomGraphiQL version
```Shell
pushd ext/custom-graphiql
git fetch
git checkout <new tag>
npm install
npm run build
popd
git add ext/custom-graphiql # Update submodule to point into new custom-graphiql
vim project.clj README.md # Edit version
git add project.clj README.md
git commit -m "New version"
git tag -a "v2.y.z" -m "v2.y.z"
git push --tags origin master # Push new tags and master
lein do clean, install
```
