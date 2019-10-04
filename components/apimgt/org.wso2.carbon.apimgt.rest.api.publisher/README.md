## API Manager REST API Development

This guide explains the procedure of adding a new resource to API Manager CXF based REST APIs.
The procedure follows a top-down approach. We first define our API changes in the particular API's Swagger definition.
The changes are then applied to the CXF service Java classes using code generation.

The example described here is specific to the Publisher REST API. It describes the step by step procedure to add a new resource named /sample.

### Adding a new resource to Publisher REST API

#### Steps

1. Clone the code generator tool which is based on the swagger-code-gen tool.

[https://github.com/hevayo/swagger2cxf-maven-plugin](https://github.com/hevayo/swagger2cxf-maven-plugin)

There are some corresponding branches in the repo for each API Manager Version. Please checkout the correct branch and build.

|  APIM Version/API | Branch |
| :------------ |:-------------
| APIM 1.10.0 - Publisher REST API     | swagger-codegen-2.1.4
| APIM 2.0.0 - Publisher REST API      | apim-2.0.0
| APIM 2.1.0 - Publisher REST API      | master


2. Add this plug-in to the org.wso2.carbon.apimgt.rest.api.publisher component’s pom.xml file.

```
<plugin>
    <groupId>org.wso2.maven.plugins</groupId>
    <artifactId>swagger2cxf-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <inputSpec>${project.basedir}/src/main/resources/publisher-api.yaml</inputSpec>
    </configuration>
</plugin>

```

##### Adding the new changes

3. Define the new API (resource) in the /src/main/resources/publisher-api.yaml file.

Eg:
Define the resource:

```
/sample:
    get:
      x-scope: apim:api_view
      x-wso2-curl: "curl -k -H \"Authorization: Bearer ae4eae22-3f65-387b-a171-d37eaa366fa8\" https://127.0.0.1:9443/api/am/publisher/v0.15/sample"
      x-wso2-request: |
       GET https://127.0.0.1:9443/api/am/publisher/v0.15/sample
       Authorization: Bearer ae4eae22-3f65-387b-a171-d37eaa366fa8
      x-wso2-response: "HTTP/1.1 200 OK"
      summary: |
       Sample API
      description: |
       This operation provides you a list of available APIs qualifying under a given search condition.

      parameters:
       - $ref : '#/parameters/limit'
       - $ref : '#/parameters/offset'
       - name : query
         in: query
         description: |
           **Sample Description**.
         type: string
       - $ref : "#/parameters/Accept"
       - $ref : "#/parameters/If-None-Match"
      tags:
       - API (Collection)
      responses:
       200:
         description: |
           OK.
           List of qualifying APIs is returned.
         schema:
           $ref: '#/definitions/APIList'
         headers:
           Content-Type:
             description: The content type of the body.
             type: string
           ETag:
             description: |
               Entity Tag of the response resource. Used by caches, or in conditional requests (Will be supported in future).
             type: string
       304:
         description: |
           Not Modified.
           Empty body because the client has already the latest version of the requested resource (Will be supported in future).
       406:
         description: |
           Not Acceptable.
           The requested media type is not supported
         schema:
           $ref: '#/definitions/Error'

```

4. Validate the complete YAML using an [online Swagger editor](http://editor.swagger.io/#/)

##### Generating the code

5. Run **mvn swagger2cxf:generate** from the org.wso2.carbon.apimgt.rest.api.publisher component pom.xml’s path.

6. After generating the code, there will be new classes generated for the added /sample resource.

Classes generated under src/gen should not be modified manually.
Classes generated under src/main/java/impl are only one-time generated. We should write our implementation logic there.

##### Finalising and building the webapp

7. Take a backup of the src/main/webapp/WEB-INF/beans.xml file, which has changed by the code generation.

8. Revert the changes of beans.xml

9. Compare the <jaxrs:serviceBeans> … </jaxrs:serviceBeans> element of the changed and original beans.xml files.

After code generation:
```
<jaxrs:serviceBeans>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.ApisApi"/>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.ApplicationsApi"/>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.EnvironmentsApi"/>
   <!-- Newly added class -->
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.SampleApi"/>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApi"/>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.TiersApi"/>
</jaxrs:serviceBeans>
```

Original:

```
<jaxrs:serviceBeans>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.SwaggerJsonApi"/>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApi"/>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.ApisApi"/>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.EnvironmentsApi"/>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.TiersApi"/>
   <bean class="org.wso2.carbon.apimgt.rest.api.publisher.ApplicationsApi"/>
</jaxrs:serviceBeans>
```

10. As there is a new bean in the generated beans.xml when compared to the original one, add it to the original (reverted)
beans.xml file.

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:jaxrs="http://cxf.apache.org/jaxrs" xmlns:context="http://www.springframework.org/schema/context"
      xmlns:cxf="http://cxf.apache.org/core"
      xsi:schemaLocation="http://www.springframework.org/schema/beans  http://www.springframework.org/schema/beans/spring-beans-3.0.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd http://cxf.apache.org/jaxrs http://cxf.apache.org/schemas/jaxrs.xsd http://cxf.apache.org/core http://cxf.apache.org/schemas/core.xsd">
   <import resource="classpath:META-INF/cxf/cxf.xml"/>
   <context:property-placeholder/>
   <context:annotation-config/>
   <bean class="org.springframework.web.context.support.ServletContextPropertyPlaceholderConfigurer"/>
   <bean class="org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer"/>
   <jaxrs:server id="services" address="/">
       <jaxrs:serviceBeans>
           <bean class="org.wso2.carbon.apimgt.rest.api.publisher.SwaggerJsonApi"/>
           <bean class="org.wso2.carbon.apimgt.rest.api.publisher.SubscriptionsApi"/>
           <bean class="org.wso2.carbon.apimgt.rest.api.publisher.ApisApi"/>
           <bean class="org.wso2.carbon.apimgt.rest.api.publisher.EnvironmentsApi"/>
           <!-- Newly added class -->
           <bean class="org.wso2.carbon.apimgt.rest.api.publisher.SampleApi"/>
           <bean class="org.wso2.carbon.apimgt.rest.api.publisher.TiersApi"/>
           <bean class="org.wso2.carbon.apimgt.rest.api.publisher.ApplicationsApi"/>
       </jaxrs:serviceBeans>
       <jaxrs:providers>
           <bean class="com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider"/>
           <bean class="org.wso2.carbon.apimgt.rest.api.util.exception.GlobalThrowableMapper" />
       </jaxrs:providers>
       <jaxrs:properties>
           <!-- This is added to catch interceptor level exceptions in GlobalThrowableMapper. -->
           <entry key="map.cxf.interceptor.fault" value="true" />
       </jaxrs:properties>
   </jaxrs:server>
   <bean id="PreAuthenticationInterceptor" class="org.wso2.carbon.apimgt.rest.api.util.interceptors.PreAuthenticationInterceptor" />
   <!-- For Basic Authentication scheme please comment the AuthenticationInterceptor which contains "OAuthAuthenticationInterceptor"
           and uncomment the AuthenticationInterceptor which contains "BasicAuthenticationInterceptor"-->
   <bean id="AuthenticationInterceptor" class="org.wso2.carbon.apimgt.rest.api.util.interceptors.auth.OAuthAuthenticationInterceptor" />
   <!--<bean id="AuthenticationInterceptor" class="org.wso2.carbon.apimgt.rest.api.util.interceptors.auth.BasicAuthenticationInterceptor" />-->
   <bean id="PostAuthenticationInterceptor" class="org.wso2.carbon.apimgt.rest.api.util.interceptors.PostAuthenticationInterceptor" />
   <bean id="ValidationInInterceptor" class="org.wso2.carbon.apimgt.rest.api.util.interceptors.validation.ValidationInInterceptor"/>
   <cxf:bus>
       <cxf:inInterceptors>
           <ref bean="PreAuthenticationInterceptor"/>
           <ref bean="AuthenticationInterceptor"/>
           <ref bean="PostAuthenticationInterceptor"/>
           <ref bean="ValidationInInterceptor"/>
       </cxf:inInterceptors>
   </cxf:bus>
</beans>
```

11. Convert the publisher.yaml to JSON format using  [http://editor.swagger.io/#/](http://editor.swagger.io/#/)

12. Overwrite the downloaded publisher-api.json with the components/apimgt/org.wso2.carbon.apimgt.rest.api.util/src/main/resources/publisher-api.json
- **Note:** This is required for the OAuth2 scopes validation related functionality to work. (See OAuthAuthenticationInterceptor class)

13. Build the org.wso2.carbon.apimgt.rest.api.util component.

14. Build the org.wso2.carbon.apimgt.rest.api.publisher and deploy the api#am#publisher#v0.15.war in the pack
- **NOTE**: You do not need to put the org.wso2.carbon.apimgt.rest.api.util.jar as a patch because it will be bundled inside the webapp when you build the two components in that order. 

##### Invoke the new /sample resource:

15. Follow the guide https://docs.wso2.com/display/AM250/apidocs/publisher/#guide and generate an access token with scope defined in x-scope element in your new resource definition.
Here it is apim:api_view.

```
 /sample:
    get:
      x-scope: apim:api_view
      X-wso2-curl ...
```

16. Invoke /sample resource with the generated access token.

```
curl -H "Authorization: Bearer <access-token>" https://localhost:9443/api/am/publisher/v0.15/sample -k -v

v0.10 - APIM 2.0.0
v0.11 - APIM 2.1.0
v0.12 - APIM 2.2.0
v0.13 - APIM 2.5.0
v0.15 - APIM 2.6.0
```

