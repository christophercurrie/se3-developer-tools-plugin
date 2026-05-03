# Developer tools plug-in for [Strange Eons](https://github.com/CGJennings/strange-eons)

This repository contains the source code for the developer tools plug-in. The plug-in includes tools to explore and edit settings, monitor memory use, view and clear caches, highlight regions on preview images, and more.

**Note:** If you simply want to *use* this plug-in, install it from the [plug-in catalogue](http://se3docs.cgjennings.ca/um-plugins-catalogue.html). A quick way to do that is to copy this text: `eonscat:6574` and then switch to the Strange Eons window.

## Building the plug-in

The plug-in compiles against `strange-eons-app` from the local Maven repository.
Build and install Strange Eons first:

```
cd ../strange-eons
mvn install -DskipTests
```

Then build the plug-in with the Gradle wrapper:

```
./gradlew build
```

The plug-in is written to `build/dist/DeveloperTools.seplugin`. If a Strange
Eons user folder exists (`%APPDATA%/StrangeEons3` on Windows, `~/.StrangeEons3`
otherwise, or `$STRANGE_EONS_USER_DIR` if set), the bundle is also copied into
its `plug-ins` subfolder.