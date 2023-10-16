# The Eurofurence Regsys (Classic)

This page explains how to set up a local apache web server for ease of local development.

Strictly speaking, this step is optional, but you will have a much nicer dev experience if you use
our supplied local apache configuration, so everything resides under http://localhost:10000/ under the correct paths.

## Install Apache 2.4

On Linux or Mac, you can usually just install the package provided by your distribution.

[download page (Windows)](https://www.apachehaus.com/cgi-bin/download.plx)

Download a current Apache 2.4 package and install.

## Run local apache to achieve a server-like setup

Back up the original `httpd.conf` in your apache httpd installation.

Add `httpd.regsys_proxy.conf` to the end of it (or include the file).

You will probably need to change the SERVERROOT path at the beginning.

Also enable mod_proxy, mod_proxy_http and mod_proxy_http2.

Set the only Listen directive to `Listen localhost:10000`.

Test the configuration validity with `httpd -t`.

Start apache with just `httpd`, stop it with Ctrl-C.

Logs are written in the configured log directory, should you need them.
