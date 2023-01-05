<!--

    Proodos BV Open Source Version
    Copyright (c) 2022-present Proodos BV
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    All trademarks are the property of their respective owners.

-->

# Nexus Repository Jenkins plugin Format

# Table Of Contents

- [Developing](#developing)
  - [Requirements](#requirements)
  - [Download](#download)
  - [Building](#building)
- [Using Jenkins with Nexus Repository Manager 3](#using-Jenkins-with-nexus-repository-manager-3)
- [Compatibility with Nexus Repository Manager 3 Versions](#compatibility-with-nexus-repository-manager-3-versions)
- [Installing the plugin](#installing-the-plugin)
  - [Easiest Install](#permanent-install)
  - [Temporary Install](#temporary-install)
  - [Other Permanent Install Options](#other-permanent-install-options)
- [The Fine Print](#the-fine-print)
- [Getting Help](#getting-help)

## Developing

### Requirements

- [Apache Maven 3.3.3+](https://maven.apache.org/install.html)
- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)


### Download

Find pre-compiled files [here](https://search.maven.org/search?q=a:%22nexus-repository-jenkins%22).

### Building

To build the project and generate the bundle use Maven

    mvn clean package -PbuildKar

If everything checks out, the bundle for Jenkins should be available in the `target` folder

#### Build with Docker

`docker build -t nexus-repository-jenkins .`

#### Run as a Docker container

`docker run -d -p 8081:8081 --name nexus-repository-jenkins nexus-repository-jenkins`

When you want to remotely debug the nexus instance, you can use something like the following:

`docker run --rm -d -p 8081:8081 -p 5005:5005 INSTALL4J_ADD_VM_PARAMS="-Xms2703m -Xmx2703m -XX:MaxDirectMemorySize=2703m -Djava.util.prefs.userRoot=/nexus-data/javaprefs -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" --name nexus-repository-jenkins nexus-repository-jenkins `


**The application will now be available from your browser at http://localhost:8081

After allowing some time to spin up, the application will be available from your browser at http://localhost:8081.

To read the generated admin password for your first login to the web UI, you can use the command below against the running docker container:

    docker exec -it nexus-repository-jenkins cat /nexus-data/admin.password && echo

## Using Jenkins With Nexus Repository Manager 3

[We have detailed instructions on how to get started here!](docs/Jenkins_USER_DOCUMENTATION.md)

## Compatibility with Nexus Repository Manager 3 Versions

The table below outlines what version of Nexus Repository the plugin was built against**

| Plugin Version | Nexus Repository Version |
|----------------|--------------------------|
| v0.0.1         | 3.41.0-01                |

If a new version of Nexus Repository is released and the plugin needs changes, a new release will be made, and this
table will be updated to indicate which version of Nexus Repository it will function against. This is done on a time
available basis, as this is community supported. If you see a new version of Nexus Repository, go ahead and update the
plugin and send us a PR after testing it out!


## Features Implemented In This Plugin

| Feature | Implemented        |
| ------- | ------------------ |
| Proxy   | :heavy_check_mark: |
| Hosted  |                    |
| Group   |                    |

## Installing the plugin

There are a range of options for installing the Jenkins plugin. You'll need to build it first, and
then install the plugin with the options shown below:

### Permanent Install

Thanks to some upstream work in Nexus Repository, it's become a LOT easier to install a plugin. To install the `Jenkins` plugin, follow these steps:

- Build the plugin with `mvn clean package -PbuildKar`
- Copy the `nexus-repository-jenkins-0.0.1-bundle.kar` file from your `target` folder to the `deploy` folder for your Nexus Repository installation.

Once you've done this, go ahead and either restart Nexus Repo, or go ahead and start it if it wasn't running to begin with.

You should see `Jenkins (proxy)` in the available Repository Recipes to use, if all has gone according to plan :)

### Temporary Install

Installations done via the Karaf console will be wiped out with every restart of Nexus Repository. This is a
good installation path if you are just testing or doing development on the plugin.

- Enable Nexus Repo console: edit `<nexus_dir>/bin/nexus.vmoptions` and change `karaf.startLocalConsole` to `true`.


- Run Nexus Repo console:
  ```shell
  # sudo su - nexus
  $ cd <nexus_dir>/bin
  $ ./nexus run
  > bundle:install file:///tmp/nexus-repository-jenkins-0.0.1.jar
  > bundle:list
  ```
  ```
  > bundle:start <org.proodos.nexus.plugins:nexus-repository-jenkins ID>
  ```

### Other Permanent Install Options


## The Fine Print

It is worth noting that this is **NOT SUPPORTED** by Proodos, and is a contribution of ours
to the open source community (read: you!)

Remember:

- Use this contribution at the risk tolerance that you have
- DO file issues here on GitHub, so that the community can pitch in

