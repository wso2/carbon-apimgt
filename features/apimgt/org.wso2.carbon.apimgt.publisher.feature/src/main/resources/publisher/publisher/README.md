### Note
 This is meant to be used **ONLY** in development time to server the react app.
 This `index.html` file will be used when running `npm start` with `webpack-dev-server`.
 We need to have a separate index.html file because
 - This page will be served via webpack dev server so `.jag` files won't work
 - As a [common practice](https://github.com/webpack/webpack-dev-server/issues/377#issuecomment-241258405) don't use content hashing so no hash values will be added to the file name