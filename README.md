# carbon-apimgt

# What is this branch ?

We are planing to move the Admin portal implementation from Jaggery to React in next major release.Hence , this is a feature branch created targeting the APIM 3.2.0 release for the API Manager Admin portal revamping effort.


## About this repository

|  Branch | Build Status(Jenkins) | Build Status(TravisCI) |
| :------------ |:------------- |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/platform-builds/job/carbon-apimgt/badge/icon)](https://wso2.org/jenkins/view/platform/job/platform-builds/job/carbon-apimgt/) | [![Build Status](https://api.travis-ci.org/wso2/carbon-apimgt.svg?branch=master)](https://travis-ci.org/wso2/carbon-apimgt) |

## Building from the source

If you want to build carbon-apimgt from the source code:

1. Install Java 7 or 8 (http://www.oracle.com/technetwork/java/javase/downloads/)
1. Install Apache Maven 3.x.x (https://maven.apache.org/download.cgi#)
1. Get a clone or download the source from this repository (https://github.com/wso2/carbon-apimgt.git).
1. Check out branch master as follows:\
``git checkout master``
1. Navigate to the ``carbon-apimgt`` directory and run the following Maven command.\
 ``mvn clean install``
