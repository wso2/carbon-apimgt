1. Getting Started
1.1 Overview

In WSO2 API Manager, service discovery enables you to get the maximum advantage from a microservices architecture. The API Publisher brings all endpoint URLs to one place, where you can discover service endpoints from one or more systems. When creating an API, you can select the service endpoint that is discovered. The initial development is based on Kubernetes and this is designed to plug any service discovery system.
For the implementation, we are introducing two new REST API in publisher as follows to list down the available service discovery systems and to list down the available services in a particular service discovery system.

Swagger definitions of the REST api to list down the service discovery systems

/service-discovery/endpoints:

  #########################################################
  # Retrieving the list of available types
  #########################################################
    get:
      x-scope: apim:serviceEndpoints_view
      x-wso2-curl: ""
      summary: Get list of services discovery system.
      description: |
        Using this operation, you can retrieve complete list of available services 
    discovery system.
      parameters:
        - $ref: '#/parameters/limit'
        - $ref: '#/parameters/offset'
      tags:
        - Service Discovery Types
      responses:
        200:
          description: |
            Ok.
            List of service discovery system is returned.
          headers:
            Content-type:
              description: |
                The content type of the body.
              type: string
            ETag:
              description: |
                Entity Tag of the response resource. Used by caches, or in conditional      requests
                (Will be supported in future).
              type: string
            Last-Modified:
              description: |
                Date and time the resource has been modified the last time.
                Used by caches, or in conditional requests (Will be supported in future).
              type: string
          schema:
            $ref: '#/definitions/ServiceDiscoverySystemTypeList'
        304:
          description: |
            Not Modified.
            Empty body because the client has already the latest version of the requested
            resource (Will be supported in future).
        404:
          description: |
            Not Found.
            No services discovery systems are available .
          schema:
            $ref: '#/definitions/Error'
        406:
          description: |
            Not Acceptable.
            The requested media type is not supported
          schema:
            $ref: '#/definitions/Error'






Swagger definitions of the REST api to list down the services

service-discovery/endpoints/{type}:

    #########################################################
    # Retrieving the list of available services in the cluster
    #########################################################
    get:
      x-scope: apim:serviceEndpoints_view
      x-wso2-curl: ""
      summary: Get list of services in the cluster.
      description: |
        Using this operation, you can retrieve complete list of available services in the clusters.
      parameters:
        - $ref: '#/parameters/type'
        - $ref: '#/parameters/limit'
        - $ref: '#/parameters/offset'
      tags:
        - Service Discovery
      responses:
        200:
          description: |
            Ok.
            List of services for requested type is returned.
          headers:
            Content-type:
              description: |
                The content type of the body.
              type: string
            ETag:
              description: |
                Entity Tag of the response resource. Used by caches, or in conditional requests
                (Will be supported in future).
              type: string
            Last-Modified:
              description: |
                Date and time the resource has been modified the last time.
                Used by caches, or in conditional requests (Will be supported in future).
              type: string
          schema:
            $ref: '#/definitions/ServiceDiscoveriesInfoList'
        304:
          description: |
            Not Modified.
            Empty body because the client has already the latest version of the requested
            resource (Will be supported in future).
        404:
          description: |
            Not Found.
            No services are available in the cluster in requested namespace or type.
          schema:
            $ref: '#/definitions/Error'
        406:
          description: |
            Not Acceptable.
            The requested media type is not supported
          schema:
            $ref: '#/definitions/Error'


Resource definitions for the apis
#------------------------------------------------------
  # service_discoveriesResponse definition.
  #------------------------------------------------------
  ServiceDiscoveriesInfo:
    title: details of the service
     properties:
      serviceName:
        type: string
        description: service name available in the cluster
        example: foo-service
      serviceURL:
        type: string
        description: url of the service
        example: http://10.0.0.11:6379
      properties:
        type: string
        description: properties of the available services
        example:

#-----------------------------------------------------
  # service_discoveriesResponse list definition.
  #-----------------------------------------------------
  ServiceDiscoveriesInfoList:
    title: service list
    properties:
      count:
        type: integer
        description: |
          Number of services returned.
        example: 1
      type:
        type: string
        description: |
          Type of the service discovery system
        example: Kubernetes
      list:
        type: array
        items:
          $ref: '#/definitions/ServiceDiscoveriesInfo'
      pagination:
        $ref: '#/definitions/Pagination'


  #-----------------------------------------------------
  # service_discoveriesResponse type list definition.
  #-----------------------------------------------------
  ServiceDiscoverySystemTypeList:
    title: type list
    properties:
      count:
        type: integer
        description: |
          Number of types returned.
        example: 1
      list:
        type: array
        items:
          $ref: '#/definitions/TypeInfo'
        description: |
          Type of the service discovery system
        example: Kubernetes


  #-----------------------------------------------------
  # service_discoveriesResponse type definition.
  #-----------------------------------------------------
  TypeInfo:
    title: Type info with details.
    properties:
      name:
        type: string
        example: Kubernetes


Reading configuration
There will be two scenarios to read configuration. When the user is a supertenat user the configuration details will be included in the deployment.toml file , when the user is a tenant user the configuration details will be included int tenant-config.json file.

Scenario 1
User - super tenant
File - deployment.toml file



[[apim.service_discovery]]
enable = false
type ="ETCD"
displayName="K8s"
className="org.wso2.carbon.apimgt.impl.containermgt.K8sServiceDiscovery"
masterURL=""
SAToken=""

File - api-manager.xml.j2 

{# <?xml version="1.0" encoding="UTF-8"?> #}
<ServiceDiscoveryConfiguration>
    {% for services in apim.service_discovery %}
        <Enabled>{{services.enable}}</Enabled>
        <ServiceDiscovery type="{{services.type}}">
            <ClassName>{{services.className}}</ClassName>
            <DisplayName>{{services.displayName}}</DisplayName>
            <ImplParameters>
                <MasterURL>{{services.masterURL}}</MasterURL>
                <SAToken>{{services.SAToken}}</SAToken>

                    </ImplParameters>
        </ServiceDiscovery>
    {% endfor %}
</ServiceDiscoveryConfiguration>

Scenario 2
User - tenant user
File - tenant-config.json file

"ServiceDiscovery":{
    "enable":"true",
    "ServiceDiscoveryTypes":[
          {
        "Type":"Kubernetes",
        "DisplayName":"K8s",
        "ImplParameters":{
          "MasterURL":"",
          "SAToken":"",
          "Namespace":"default"
                }
          }
    ]
  }



Connecting with the cluster
In order to connect with the cluster we need to add the parameters in the configuration and we need to update the registry 

Quick start Guide 
Add the configuration details that are need to connect with the cluster in the deployment.toml file (if the user is a super tenant user) or tenant-config.json file (if the user is tenant user).
Update the registry by adding your cluster name.
Invoke the api to get the service discovery systems that are available.
Invoke the api to get the services that are available in the particular type.


Adding configuration details.
If you are a super tenant user, navigate to the file :wso2am-3.2.0-SNAPSHOT/repository/conf/deployment.toml in your pack and add the configuration.
[[apim.service_discovery]]
enable = false
type ="Kubernetes"
displayName="K8s"
className="org.wso2.carbon.apimgt.impl.containermgt.K8sServiceDiscovery"
masterURL=""
SAToken=""

If you are a tenant user go to  this file in carbon portal “/_system/config/apimgt/applicationdata/tenant-conf.json” and update the configuration details.
"ServiceDiscovery":{
    "enable":"true",
    "ServiceDiscoveryTypes":[
          {
        "Type":"Kubernetes",
        "DisplayName":"K8s",
        "ImplParameters":{
          "MasterURL":"",
          "SAToken":"",
          "Namespace":"default"
                }
          }
    ]
  }


Updating the registry
Create a REST api with appropriate details.
Open the carbon portal (https://localhost:9443/carbon/).
If you are a super tenant user
Go to Home > Resources > Browse and navigate /_system/governance/apimgt/applicationdata/provider/test1/1.0.0/api


Add the service discovery system type and the cluster names here in the deployment Environments section  then press Save API button.

If you are a tenant user
Go to Home > Resources > Browse and navigate /_system/governance/apimgt/applicationdata/provider/tenantuser-AT-wso2.com/test1/1.0.0/api


Add the service discovery system type and the cluster names here in the deployment Environments section  then press Save API button.



Invoking the api for super tenant
Create a client and getdetails

curl -k -X POST -H "Authorization: Basic YWRtaW46YWRtaW4=" -H "Content-Type: application/json" -d @payload.json https://localhost:9443/client-registration/v0.16/register

The response  will be similar to this……

{"clientId":"V0gc0TU_0rO3lTlKOyVNsqZ_PhIa","clientName":"admin_rest_api_publisher","callBackURL":"www.google.lk","clientSecret":"8xqfsbO9FuEzTJAjGG16beZKNeQa","isSaasApplication":true,"appOwner":null,"jsonString":"{\"grant_types\":\"password refresh_token\"}","jsonAppAttribute":"{}","tokenType":null}m

Copy clientId and clientSecret in below format and convert it to base64

No go to the online encoder

https://www.base64encode.org/

Paste the details in the format



clientId:clientSecret
Example :
V0gc0TU_0rO3lTlKOyVNsqZ_PhIa:8xqfsbO9FuEzTJAjGG16beZKNeQa
You will get a encoded value like this
VjBnYzBUVV8wck8zbFRsS095Vk5zcVpfUGhJYTo4eHFmc2JPOUZ1RXpUSkFqR0cxNmJlWktOZVFh

Get a access token using this value

curl -k -d "grant_type=password&username=admin&password=admin&scope=apim:serviceEndpoints_view" -H "Authorization: Basic <encodedtoken>" https://localhost:8243/token

Response will be like this
{"access_token":"0b4a5979-4413-3314-8de6-cc0809cabd28","refresh_token":"eb15ed0b-c978-3231-98d8-77f5a5e700d0","scope":"apim:serviceEndpoints_view","token_type":"Bearer","expires_in":2936}

Now using that acces token we can invoke our api
$ curl -k -H "Authorization: Bearer <access_token>" "https://localhost:9443/api/am/publisher/v1.1/service-discovery/endpoints/Kubernetes?limit=1&offset=2"

$ curl -k -H "Authorization: Bearer <access_token>" "https://localhost:9443/api/am/publisher/v1.1/service-discovery/endpoints”





Invoking the api for tenant
Create a client and getdetails

curl -k -X POST -H "Authorization: Basic YWRtaW46YWRtaW4=" -H "Content-Type: application/json" -d @payload.json https://localhost:9443/client-registration/v0.16/register

The response  will be similar to this……

{"clientId":"V0gc0TU_0rO3lTlKOyVNsqZ_PhIa","clientName":"admin_rest_api_publisher","callBackURL":"www.google.lk","clientSecret":"8xqfsbO9FuEzTJAjGG16beZKNeQa","isSaasApplication":true,"appOwner":null,"jsonString":"{\"grant_types\":\"password refresh_token\"}","jsonAppAttribute":"{}","tokenType":null}m

Copy clientId and clientSecret in below format and convert it to base64

No go to the online encoder

https://www.base64encode.org/

Paste the details in the format



clientId:clientSecret
Example :
V0gc0TU_0rO3lTlKOyVNsqZ_PhIa:8xqfsbO9FuEzTJAjGG16beZKNeQa
You will get a encoded value like this
VjBnYzBUVV8wck8zbFRsS095Vk5zcVpfUGhJYTo4eHFmc2JPOUZ1RXpUSkFqR0cxNmJlWktOZVFh

Get a access token using this value

curl -k -d "grant_type=password&username=tenantuser@wso2.com&password=tenantuser&scope=apim:serviceEndpoints_view" -H "Authorization: Basic <encodedtoken>" https://localhost:8243/token

Response will be like this
{"access_token":"0b4a5979-4413-3314-8de6-cc0809cabd28","refresh_token":"eb15ed0b-c978-3231-98d8-77f5a5e700d0","scope":"apim:serviceEndpoints_view","token_type":"Bearer","expires_in":2936}

Now using that acces token we can invoke our api
$ curl -k -H "Authorization: Bearer <access_token>" "https://localhost:9443/api/am/publisher/v1.1/service-discovery/endpoints/Kubernetes?limit=1&offset=2"

$ curl -k -H "Authorization: Bearer <access_token>" "https://localhost:9443/api/am/publisher/v1.1/service-discovery/endpoints”






