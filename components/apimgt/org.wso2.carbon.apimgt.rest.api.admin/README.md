## API Manager REST API Development

This guide explains the procedure of adding a new resource to API Manager CXF based REST APIs.
The procedure follows a top-down approach. We first define our API changes in the particular APIs swagger definition.
The changes are then applied to the CXF service java classes using code generation.

The example described here is specific to Admin REST API. It describes step by step procedure to add a new resource
called /sample.

### Adding a new resource to Admin REST API

#### Steps

1. Clone the code generator tool which is based on swagger-code-gen tool.

[https://github.com/hevayo/swagger2cxf-maven-plugin](https://github.com/hevayo/swagger2cxf-maven-plugin)

There are some corresponding branches in the repo for each API Manager Version. Please checkout the correct branch and build.

|  APIM Version/API | Branch |
| :------------ |:-------------
| APIM 2.0.0 - Admin REST API      | apim-2.0.0
| APIM 2.1.0 - Admin REST API      | master


2. Add this plugin to org.wso2.carbon.apimgt.rest.api.admin component’s pom.xml

```
<plugin>
    <groupId>org.wso2.maven.plugins</groupId>
    <artifactId>swagger2cxf-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <inputSpec>${project.basedir}/src/main/resources/admin-api.yaml</inputSpec>
    </configuration>
</plugin>

```

##### Adding the new changes

3. Add the new API (resource) to /src/main/resources/admin-api.yaml

Eg:
Add this resource:

```
/sample:
    get:
      x-scope: apim:api_view
      x-wso2-curl: "curl -k -H \"Authorization: Bearer ae4eae22-3f65-387b-a171-d37eaa366fa8\" https://127.0.0
      .1:9443/api/am/admin/v0.10/sample"
      x-wso2-request: |
       GET https://127.0.0.1:9443/api/am/admin/v0.10/sample
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

4. Validate the complete yaml using  [online swagger editor](http://editor.swagger.io/#/)

##### Generating the code

5. Run **mvn swagger2cxf:generate** from org.wso2.carbon.apimgt.rest.api.admin component pom.xml’s path

6. After generating the code, there will be new classes generated for the added /sample resource.

Classes generated under src/gen should not be modified manually.
Classes generated under src/main/java/impl are only one-time generated. We should write our implementation logic there.

##### Finalising and building the webapp

7. Take a backup of src/main/webapp/WEB-INF/beans.xml (which is changed by code generation)

8. Revert the changes of beans.xml

9. Compare the <jaxrs:serviceBeans> … </jaxrs:serviceBeans> element of changed and original beans.xml

After code generation:
```
<jaxrs:serviceBeans>
    <bean class="org.wso2.carbon.apimgt.rest.api.admin.SwaggerJsonApi"/>
    <bean class="org.wso2.carbon.apimgt.rest.api.admin.PoliciesApi"/>
    <!-- Newly added class -->
    <bean class="org.wso2.carbon.apimgt.rest.api.admin.SampleApi"/>
    <bean class="org.wso2.carbon.apimgt.rest.api.admin.ThrottlingApi"/>
</jaxrs:serviceBeans>

```

Original:

```
<jaxrs:serviceBeans>
    <bean class="org.wso2.carbon.apimgt.rest.api.admin.SwaggerJsonApi"/>
    <bean class="org.wso2.carbon.apimgt.rest.api.admin.PoliciesApi"/>
    <bean class="org.wso2.carbon.apimgt.rest.api.admin.ThrottlingApi"/>
</jaxrs:serviceBeans>
```

10. Since there is a new bean in generated beans.xml compared to the original one. Add it to the original (reverted)
beans.xml

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
            <bean class="org.wso2.carbon.apimgt.rest.api.admin.SwaggerJsonApi"/>
            <bean class="org.wso2.carbon.apimgt.rest.api.admin.PoliciesApi"/>
            <!-- Newly added class -->
            <bean class="org.wso2.carbon.apimgt.rest.api.admin.SampleApi"/>
            <bean class="org.wso2.carbon.apimgt.rest.api.admin.ThrottlingApi"/>
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

    <bean id="ValidationInInterceptor" class="org.wso2.carbon.apimgt.rest.api.util.interceptors.validation.ValidationInInterceptor"/>
    <cxf:bus>
        <cxf:inInterceptors>
            <ref bean="PreAuthenticationInterceptor"/>
            <ref bean="AuthenticationInterceptor"/>
            <ref bean="ValidationInInterceptor"/>
        </cxf:inInterceptors>
    </cxf:bus>
</beans>
```

11. Convert admin.yaml to json format using  [http://editor.swagger.io/#/](http://editor.swagger.io/#/)

12. Overwrite the downloaded admin-api.json with components/apimgt/org.wso2.carbon.apimgt.rest.api
.util/src/main/resources/admin-api.json
- **Note:** This is required to OAuth2 scopes validation related functionality to work. (See OAuthAuthenticationInterceptor class)

13. Build org.wso2.carbon.apimgt.rest.api.util component

14. Build org.wso2.carbon.apimgt.rest.api.admin and deploy the api#am#admin#v0.10.war in the pack
- **NOTE**: No need to put the org.wso2.carbon.apimgt.rest.api.util.jar as a patch. This will be bundled inside the
webapp when we build the two components in that order. (v0.10 is for APIM 2.0.0 version)

##### Invoke the new /sample resource:

15. Follow the guide https://docs.wso2.com/display/AM200/apidocs/admin/#guide and generate an access token with scope
defined in x-scope element in your new resource definition.
Here it is apim:api_view.

```
 /sample:
    get:
      x-scope: apim:api_view
      X-wso2-curl ...
```

16. Invoke /sample resource with the generated access token.

```
curl -H "Authorization: Bearer <access-token>" https://localhost:9443/api/am/admin/v0.10/sample -k -v

v0.10 - APIM 2.0.0
v0.11 - APIM 2.1.0
```

