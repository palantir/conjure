# How to publish TypeScript to npm

If you want to publish npm packages, you need to simulate the `npm login` command to ensure you have the necessary credentials to `npm publish`.  Add the following snippet to your `./your-project-api/build.gradle` to write the `$NPM_AUTH_TOKEN` environment variable to disk.  You should specify this as a secret variable on your CI server (e.g. CircleCI or TravisCI).

```diff
 apply plugin: 'com.palantir.conjure'

+project(':your-project-api:your-project-api-typescript') {
+    publishTypeScript.doFirst {
+        file('src/.npmrc') << "//registry.npmjs.org/:_authToken=${System.env.NPM_AUTH_TOKEN}"
+    }
+}
```
