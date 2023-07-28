# tomcat configuration files

These are adapted tomcat configuration files that we use in our Docker container.

- configuration for ECS json logging
- minimal http connector
- no user database/realm
- no JSP servlet
- no auto-unpacking of WARs

When updating tomcat, you should compare the dist files with what's provided here, and carry over any
necessary changes.
