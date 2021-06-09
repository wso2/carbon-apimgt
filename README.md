# carbon-apimgt

## About this repository

|  Branch | Build Status(Jenkins) | Build Status(TravisCI) |
| :------------ |:------------- |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/platform-builds/job/carbon-apimgt/badge/icon)](https://wso2.org/jenkins/view/platform/job/platform-builds/job/carbon-apimgt/) | [![Build Status](https://api.travis-ci.org/wso2/carbon-apimgt.svg?branch=master)](https://travis-ci.org/wso2/carbon-apimgt) |

## Building from the source

If you want to build carbon-apimgt from the source code:

<<<<<<< HEAD
1. Install Java 7 or 8 (http://www.oracle.com/technetwork/java/javase/downloads)
1. Install Apache Maven 3.x.x (https://maven.apache.org/download.cgi#)
1. Get a clone or download the source from this repository (https://github.com/wso2/carbon-apimgt.git).
1. Check out branch master as follows:\
``git checkout master``
1. Navigate to the ``carbon-apimgt`` directory and run the following Maven command.\
 ``mvn clean install``
=======
    We need to build below two additional Repos before building support-6.1.66 branch:

    1. carbon-governance : support-4.7.0
https://github.com/wso2-support/carbon-governance/tree/support-4.7.0

    2. wso2-wsdl4j : support-1.6.2-wso2v4
https://github.com/wso2-support/wso2-wsdl4j/tree/support-1.6.2-wso2v4

- [Support-6.2.201](https://github.com/wso2-support/carbon-apimgt/tree/support-6.2.201) - APIM 2.2.0
- [Support-6.3.95](https://github.com/wso2-support/carbon-apimgt/tree/support-6.3.95) - APIM 2.5.0

    We need to build following additional Repo before building support-6.3.95 branch:

    1. identity-inbound-auth-oauth - support-6.0.14
https://github.com/wso2-support/identity-inbound-auth-oauth/tree/support-6.0.14

- [Support-6.4.50](https://github.com/wso2-support/carbon-apimgt/tree/support-6.4.50) - APIM 2.6.0

    We need to build following additional Repo before building support-6.4.50 branch:
    
    1. orbit - master
https://github.com/wso2-support/orbit
    2. carbon-kernel - support-4.4.35
https://github.com/wso2-support/carbon-kernel/tree/support-4.4.35
    3. carbon-consent-management - support-2.0.18
https://github.com/wso2-support/carbon-consent-management/tree/support-2.0.18
    4. carbon-identity-framework - support-5.12.153
https://github.com/wso2-support/carbon-identity-framework/tree/support-5.12.153
    5. wso2-axis2 - support-1.6.1-wso2v28
https://github.com/wso2-support/wso2-axis2/tree/support-1.6.1-wso2v28
    6. identity-inbound-auth-oauth - support-6.0.53
https://github.com/wso2-support/identity-inbound-auth-oauth/tree/support-6.0.53

- [Support-6.5.349](https://github.com/wso2-support/carbon-apimgt/tree/support-6.5.349) - APIM 3.0.0
- [Support-6.6.163](https://github.com/wso2-support/carbon-apimgt/tree/support-6.6.163) - APIM 3.1.0

    We need to build following additional Repos before building support-6.6.163 branch:

    1. carbon-kernel/core/javax.cache - support-4.6.0
https://github.com/wso2-support/carbon-kernel/tree/support-4.6.0/core/javax.cache
    2. carbon-identity-framework - support-5.17.5
https://github.com/wso2-support/carbon-identity-framework/tree/support-5.17.5
    3. identity-inbound-auth-oauth - support-6.4.2
https://github.com/wso2-support/identity-inbound-auth-oauth/tree/support-6.4.2
- [Support-6.7.206](https://github.com/wso2-support/carbon-apimgt/tree/support-6.7.206) - APIM 3.2.0

    1. carbon-kernel/core/javax.cache - support-4.6.0
https://github.com/wso2-support/carbon-kernel/tree/support-4.6.0/core/javax.cache
- [Support-9.0.174.x-full](https://github.com/wso2-support/carbon-apimgt/tree/support-9.0.174.x-full) - APIM 4.0.0
>>>>>>> 9b59c9127c4 (Update README.md)
