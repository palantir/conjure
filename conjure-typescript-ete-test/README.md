typescript-ete-test
============
A template for a web app using React.

##Getting Started
###Run the server
1. `./gradlew run` - runs the server.
2. Navigate to `https://localhost:8443/typescript-ete-test/` to view the web app.

###Run the server in Dev mode
1. `./gradlew runDev` - runs the server.
2. Navigate to `https://localhost:8443/typescript-ete-test/` to view the web app.
3. Navigate to `https://localhost:8443/typescript-ete-test/api/hello`, or run `curl -w "\n" https://localhost:8443/typescript-ete-test/api/hello --insecure` in a terminal to view the API response.

###Gradle Tasks
`./gradlew tasks` - to get the list of gradle tasks

###Common Scripts
The project comes with standardized scripts to accomplish common tasks. In particular:

- `scripts/setup.sh` - sets up the development environment.
- `scripts/verify.sh` - verifies the project builds correctly. Useful for running in CI servers.

##Understanding this App

###Folder Structure
The project contains the following main folders:

- /*-api

	- The application's API code.

- /*-server

	- The application's server code. The server is a Java application, backed by Dropwizard as its primary technology. Tests are run using JUnit. The build system uses Gradle.
	- The configuration files for your service are in directories matching the [SLSv2](https://rtfm.yojoe.local/docs/skylab/en/latest/layoutSpec/pages/required.html) directory layout specification.

- /*-app

	- This folder contains an React web UI.

API and Server are generated in two separate folders.

- it reduces the number of dependencies exposed by the public JAX-RS API library
- it allows for independent versioning of the API and its implementation, thus enabling long-lived, stable APIs with frequently changing implementations

##Ecosystem

###Build Tools
| Tool              | Links |
| ---------         | ------------- |
| Java              | https://wiki.yojoe.local/display/BDTC/Java  |
| Gradle            | https://gradle.org  |
| Baseline          | https://rtfm.yojoe.local/docs/baseline/en/latest/ |
| Nebula Publishing | https://github.com/nebula-plugins/nebula-publishing-plugin |
| Nebula Info       | https://github.com/nebula-plugins/gradle-info-plugin |
| Java Distribution | https://github.com/palantir/gradle-java-distribution |
| Git Version       | https://github.com/palantir/gradle-git-version |

###Back-End
| Tool                | Links |
| ---------           | ------------- |
| Dropwizard          | http://www.dropwizard.io/ |
| Jersey              | https://jersey.java.net/ |
| Jackson             | http://wiki.fasterxml.com/JacksonHome |
| Jetty               | https://www.eclipse.org/jetty |
| Multipass           | https://rtfm.yojoe.local/docs/multipass/en/latest/ |
| HTTP Remoting       | https://github.com/palantir/http-remoting |
| Configurable Assets | https://github.com/bazaarvoice/dropwizard-configurable-assets-bundle |
| Index Page          | https://github.com/palantir/dropwizard-index-page |
| Web Security        | https://github.com/palantir/dropwizard-web-security |
| JUnit               | http://junit.org |
| Mockito             | http://mockito.org |

###Front-End
| Tool        | Helpful Links |
|-------------|-------------- |
| React       | https://facebook.github.io/react/ |
| Blueprint   | https://blueprint.yojoe.local/ |
| LESS        | http://lesscss.org |
| NPM         | https://npmjs.com |
| Typescript  | http://www.typescriptlang.org |
| TSLint      | https://github.com/palantir/tslint |
| Karma       | https://karma-runner.github.io |
| WebdriverIO | http://www.webdriver.io/ |
| Webpack     | https://webpack.github.io/ |

##Packaging and Deploying

###Deploy on InstantLaunch

####Create an InstantLaunch Cluster

Create an InstantLaunch cluster.

1. Navigate to `InstantLaunch <https://instant.yojoe.local>`_ in your favorite browser.
2. Log in.
3. Click **CentOS 6.6 - Tiny** under **Prebuilt VMs** to create an instance.

Click the **My Clusters** tab to see your new instance.

- The instance's name appears in **Active Clusters**.
- The password for the ``palantir`` user on the instance can be found by clicking **More Info** in the **Instance Info** column.

####Create, Upload, and Run a Distribution

```shell

// Create a distribution for an application
$ ./gradlew distTar
// the distribution is located at ./<project-name>/<project-name>-server>build/distributions/<project-name-and-version>.tgz

// Upload the application distribution to InstantLaunch
$ scp <path_to_tarball> palantir@<instant_launch_hostname>:~/
<enter password when prompted>

// SSH into the InstantLaunch VM
$ ssh palantir@<instant_launch_hostname>
<enter password when prompted>

// Extract the app distribution tarball, navigate to the root of the app
$ tar xzf <tarball_name>.tgz && cd <tarball_name>

// Start the application
$ ./service/bin/init.sh start

```

View the application in a browser:

```shell

$ https://<instant_launch_hostname>:<port>/<project-name>

```

#####Example

```shell

// Create a distribution for an application
$ ./gradlew distTar
:nodeSetup UP-TO-DATE
:extractNodeModules UP-TO-DATE
...
Distribution tarball is ready at nyan-cat/nyan-cat-server/build/distributions/nyan-cat-0.4.0-1-g1101e2c3dcf0.tgz
BUILD SUCCESSFUL
...

// Upload the application distribution to InstantLaunch
$ scp nyan-cat/nyan-cat-server/build/distributions/nyan-cat-0.4.0-1-g1101e2c3dcf0.tgz palantir@il-pg-alpha-32376.usw1.ptrtech.net:~/
<enter password when prompted>

// SSH into the InstantLaunch VM
$ ssh palantir@il-pg-alpha-32376.usw1.ptrtech.net
<enter password when prompted>

// Extract the distribution tarball, navigate to the root of the app
$ tar xzf nyan-cat-0.4.0-1-g1101e2c3dcf0.tgz && cd nyan-cat-0.4.0-1-g1101e2c3dcf0

// Start the application
$ ./service/bin/init.sh start

```

View the application in a browser:

```shell

$ http://il-pg-alpha-32376.usw1.ptrtech.net:8000/nyan-cat

```

####Debugging on InstantLaunch

The server logs can be found at:

```shell

$ <project-name>/var/log/<project-name>.log

```

####Editing Configuration in Deployment

**configuration files** are located at ``<tarball_name>/var/conf``.

- *Example:* ``nyan-cat-0.4.0-1-g1101e2c3dcf0/var/conf/nyan-cat.yml``

**static assets and web resources** are located at ``<tarball_name>/service/web``.

- *Example:* ``nyan-cat-0.4.0-1-g1101e2c3dcf0/service/web/index.html``
