# About this repository

|  Branch | Build Status(Jenkins) | Build Status(TravisCI) |
| :------------ |:------------- |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/platform-builds/job/carbon-apimgt/badge/icon)](https://wso2.org/jenkins/job/platform-builds/job/carbon-apimgt/) | [![Build Status](https://api.travis-ci.org/wso2/carbon-apimgt.svg?branch=master)](https://travis-ci.org/wso2/carbon-apimgt) |

 This repository contains major components which are used to build the API Manager product.

## Building from the source

If you want to build carbon-apimgt from the source code:

1. Install Java 8 (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
1. Install Apache Maven 3.x.x (https://maven.apache.org/download.cgi#)
1. Get a clone or download the source from this repository (https://github.com/wso2/carbon-apimgt.git).
1. Run the Maven command ``mvn clean install`` from the ``carbon-apimgt`` directory.


## Running Integration tests in docker containers(Optional)


1. Install docker
1. Go inside to the carbon-apimgt directory
1. Run integration test by giving following commands

    * mvn clean install -P local-h2
    * mvn clean install -P local-mysql
    * mvn clean install -P local-postgres
    * mvn clean install -P local-mssql
    * mvn clean install -P local-oracle

## Start integration tests in debug mode

 ### For docker based tests:

    * mvn -P local-mysql -Dmaven.failsafe.debug verify
      Note: local-mysql is the profile. Use other profiles accordingly.

 ### For unit tests:

    * mvn -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE" test

## How to create a new REST API in C5:

    1. Build following repositories.

        * https://github.com/swagger-api/swagger-codegen
        * https://github.com/sanjeewa-malalgoda/swagger2MSF4J

    2. If you going to add or modify the publisher REST API then add your API definition changes into the publisher-api.yaml
       Likewise, if you are going to add or modify the store REST API then add your API definition changes into the store-api.yaml


    3 Build the org.wso2.carbon.apimgt.rest.api.publisher or org.wso2.carbon.apimgt.rest.api.store REST API components with following command.
       * mvn swagger2msf4j:generate

        NOTE: This command will erase some existing classes. Please filter relevant changes which regard to your modification.


    4. Increase the service count by number of APIs that you create(This requires only if you add new API).
        <carbon.component>
           osgi.service; objectClass="org.wso2.msf4j.Microservice"; serviceCount="5"
        </carbon.component>

