# The Eurofurence Regsys (Classic)

In this version, what remains of the old regsys is its frontend pages and some
interface implementations.

- ✅ no longer has a database
- ✅ config is loaded from yaml at war deploy time 
- ❌ still implements dealers' den api, nosecounter api, security system api (to be moved to export service)

## Local Setup

### Install Java

You will need OpenJDK.

On Linux or Mac, you can usually just install the package provided by your distribution.

For Windows, Microsoft provides long-term support builds for all operating systems, so you can safely work with LTS versions.

[download page (Windows)](https://learn.microsoft.com/en-us/java/openjdk/download)

On the server we run on a LTS Linux distribution which is currently on JDK 11, so if you are going to be 
making code changes, it is best to work with JDK version 11 to ensure you are not accidentally breaking the web 
application.

Unpack the archive or run the installer.

Set `JAVA_HOME` and `JDK_HOME` to the base directory of the installation.

Add `$JAVA_HOME/bin` to your path.

### Install Tomcat 8.5

You will need a current Tomcat 8.5.

On Linux or Mac, you can usually just install the package provided by your distribution. For Windows:

[download page](https://tomcat.apache.org/download-80.cgi)

Unpack the archive.

Vanilla tomcat comes with some (potentially security issue ridden) stuff you won't need. Go to `webapps` inside
the installation directory and delete everything except `ROOT`.

Starting tomcat is as simple as
```
cd /path/to/tomcat/bin
catalina run
```

This will run tomcat, logging to the console.

Or you can run tomcat from within IntelliJ idea. It usually knows what to do with this project.

### Install Apache 2.4

Strictly speaking, this step is optional, but you will have a much nicer dev experience if you use
our supplied local apache configuration, so everything resides under http://localhost:10000/ under the correct paths.

On Linux or Mac, you can usually just install the package provided by your distribution.

[download page (Windows)](https://www.apachehaus.com/cgi-bin/download.plx)

Download a current Apache 2.4 package and install.

### Configure the registration system frontend

Copy `doc/config-template.yaml` to `config.yaml` next to the Makefile.

Also change the path in `HardcodedConfig.java` that points to `config.yaml`. 

Edit the configuration, mostly you may need to adapt the URLs to the various backend services, though for
ease of use you might wish to change their configuration to match the ports given in the default config file.

Note that you can add some JWT tokens to the config file for easy local log-in with various roles, but this
will only work if you configure a matching signing key in the configuration of each backend service.
**This is not a production feature!**

### Build and deploy the webapp

Set `INSTBASE` to point to the `webapps` directory in your tomcat.

Set `WARNAME` to `regsys.war`.

In this directory do

```
make deploy 
```

This will build the registration system WAR and deploy it to tomcat. You should see the output.

If you don't have the `make` utility, the Makefile has the individual commands that build and copy the WAR.

Or build and deploy the tomcat application from within IntelliJ idea.

### Run local apache to achieve a server-like setup

Back up the original `httpd.conf` in your apache httpd installation.

Add `httpd.conf` to the end of it. 

You will probably need to change the SERVERROOT path at the beginning.

Also enable mod_proxy, mod_proxy_http and mod_proxy_http2. 

Set the only Listen directive to `Listen localhost:10000`.

Test the configuration validity with `httpd -t`.

Start apache with just `httpd`, stop it with Ctrl-C.

Logs are written in the configured log directory, should you need them.
