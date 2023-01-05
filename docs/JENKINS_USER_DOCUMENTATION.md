<!--

    Proodos BV Open Source Version
    Copyright (c) 2022-present Proodos BV
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    All trademarks are the property of their respective owners.

-->
## Jenkins Repositories

### Introduction

[Jenkins Update site](https://updates.jenkins.io/) is the default update site for Jenkins. It is used for Jenkins to check for updates and hosts the plugins for Jenkins.

### Proxying Jenkins plugin repository

You can create a proxy repository in Nexus Repository Manager (NXRM) that will cache artifacts from a remote Jenkins update site repository such as
https://updates.jenkins.io/. To make `Jenkins` use your NXRM Proxy, you will need to configure it in the Jenkins administration console. 
 
To proxy a Jenkins repository, you simply create a new 'Jenkins (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management). 

Minimal configuration steps are:
- Define 'Name' - e.g. `jenkins-proxy`
- Define URL for 'Remote storage' - e.g. [https://updates.jenkins.io/download/](https://updates.jenkins.io/)
- Select a `Blob store` for `Storage`

If you haven't already, update the Jenkins instance configuration to use your jenkins-proxy (i.e. add `http://localhost:8081/repository/jenkins-proxy/`).

From this time on, Jenkins will connect to this Nexus proxy to download his plugins, saving you bandwith if you have several instances of Jenkins running inside your network.
