# The Eurofurence Regsys (Classic)

In this version, what remains of the old regsys is its frontend pages and some
interface implementations.

- ✅ no longer has a database
- ✅ config is loaded from yaml at war deploy time 
- ❌ still implements dealers' den api, nosecounter api, security system api (to be moved to export service)

## Local Development Setup

### Install Java

You will need OpenJDK version 17 or newer.

On Linux or Mac, you can usually just install the package provided by your distribution.

For Windows, Microsoft provides long-term support builds for all operating systems, so you can safely work with LTS versions.

[download page (Windows)](https://learn.microsoft.com/en-us/java/openjdk/download)

Unpack the archive or run the installer.

Set `JAVA_HOME` and `JDK_HOME` to the base directory of the installation.

Add `$JAVA_HOME/bin` to your path.

### Install Apache 2.4

See the [apache setup readme](readme-apache.md) for this optional step that will give you a much nicer
local dev experience.

### Configure the registration system frontend

Copy `doc/config-template.yaml` to `config.yaml` next to the Makefile.

Also change the path in `HardcodedConfig.java` that points to `config.yaml`. 

Edit the configuration, mostly you may need to adapt the URLs to the various backend services, though for
ease of use you might wish to change their configuration to match the ports given in the default config file.

Note that you can add some JWT tokens to the config file for easy local log-in with various roles, but this
will only work if you configure a matching signing key in the configuration of each backend service.
**This is not a production feature!**

### Configure human-readable logging

This is a setting at the top of `build.gradle`. See the comments there.

### Build and deploy the webapp

`./gradlew runExecutableJar`, or the equivalent in your IDE's run configurations. 
