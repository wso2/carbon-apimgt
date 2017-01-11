/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core;

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.core.models.*;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SampleTestObjectCreator {
    static String apiDefinition;
    static {
        byte[] bytes = new byte[0];
        try {
            apiDefinition =  IOUtils.toString(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("swagger.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static API.APIBuilder createDefaultAPI() {
        List<String> transport = new ArrayList<>();
        transport.add("http");
        transport.add("https");

        List<String> tags = new ArrayList<>();
        tags.add("climate");

        List<String> policies = new ArrayList<>();
        policies.add("Gold");
        policies.add("Silver");
        policies.add("Bronze");

        BusinessInformation businessInformation = new BusinessInformation();
        CorsConfiguration corsConfiguration =  new CorsConfiguration();

        API.APIBuilder apiBuilder = new API.APIBuilder("admin", "WeatherAPI", "1.0.0").
                id(UUID.randomUUID().toString()).
                context("weather").
                description("Get Weather Info").
                lifeCycleStatus("CREATED").
                lifecycleInstanceId(UUID.randomUUID().toString()).
                apiDefinition(new StringBuilder(apiDefinition)).
                wsdlUri(" ").
                isResponseCachingEnabled(false).
                cacheTimeout(60).
                isDefaultVersion(false).
                apiPolicy("Unlimited").
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.PUBLIC).
                visibleRoles(new ArrayList<>()).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy("admin").
                lastUpdatedTime(LocalDateTime.now());
        try {
            APIDefinition apiDefinition = new APIDefinitionFromSwagger20();
            List<UriTemplate> uriTemplateList = new ArrayList<>();
            for (APIResource apiResource : apiDefinition.parseSwaggerAPIResources(apiBuilder.getApiDefinition())){
                uriTemplateList.add(apiResource.getUriTemplate());
            }
            apiBuilder.uriTemplates(uriTemplateList);

        } catch (APIManagementException e) {
            e.printStackTrace();
        }
        return apiBuilder;
    }

    public static API getSummaryFromAPI(API api) {
            return new API.APIBuilder(api.getProvider(), api.getName(), api.getVersion()).
                    id(api.getId()).
                    context(api.getContext()).
                    description(api.getDescription()).
                    lifeCycleStatus(api.getLifeCycleStatus()).
                    lifecycleInstanceId(api.getLifecycleInstanceId()).build();
    }

    public static API copyAPIIgnoringNonEditableFields(API fromAPI, API toAPI) {
        API.APIBuilder builder = new API.APIBuilder(toAPI);

        return builder.provider(fromAPI.getProvider()).
                    id(fromAPI.getId()).
                    name(fromAPI.getName()).
                    version(fromAPI.getVersion()).
                    context(fromAPI.getContext()).
                    createdTime(fromAPI.getCreatedTime()).
                    createdBy(fromAPI.getCreatedBy()).
                    lifecycleInstanceId(fromAPI.getLifecycleInstanceId()).
                    lifeCycleStatus(fromAPI.getLifeCycleStatus()).
                    copiedFromApiId(fromAPI.getCopiedFromApiId()).build();

    }

    public static LifecycleState getMockLifecycleStateObject(String lifecycleId) {
        LifecycleState lifecycleState = new LifecycleState();
        lifecycleState.setLcName("API_LIFECYCLE");
        lifecycleState.setLifecycleId(lifecycleId);
        lifecycleState.setState("PUBLISH");
        return lifecycleState;
    }

    public static API getMockApiSummaryObject(){
        return new API.APIBuilder("admin","Sample","1.0.0").build();
    }

    public static API.APIBuilder createAlternativeAPI() {
        List<String> transport = new ArrayList<>();
        transport.add("http");

        List<String> tags = new ArrayList<>();
        tags.add("food");
        tags.add("beverage");

        List<String> policies = new ArrayList<>();
        policies.add("Silver");
        policies.add("Bronze");

        BusinessInformation businessInformation = new BusinessInformation();
        businessInformation.setBusinessOwner("John Doe");
        businessInformation.setBusinessOwnerEmail("john.doe@annonymous.com");
        businessInformation.setTechnicalOwner("Jane Doe");
        businessInformation.setBusinessOwnerEmail("jane.doe@annonymous.com");

        CorsConfiguration corsConfiguration =  new CorsConfiguration();
        corsConfiguration.setEnabled(true);
        corsConfiguration.setAllowMethods(Arrays.asList("GET", "POST", "DELETE"));
        corsConfiguration.setAllowHeaders(Arrays.asList("Authorization", "X-Custom"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowOrigins(Arrays.asList("*"));

        API.APIBuilder apiBuilder = new API.APIBuilder("Adam", "restaurantAPI", "0.9").
                id(UUID.randomUUID().toString()).
                context("yummy").
                description("Get Food & Beverage Info").
                lifeCycleStatus("CREATED").
                apiDefinition(new StringBuilder(apiDefinition)).
                wsdlUri("http://www.webservicex.net/globalweather.asmx?op=GetWeather?wsdl").
                isResponseCachingEnabled(true).
                cacheTimeout(120).
                isDefaultVersion(true).
                apiPolicy("Gold").
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(Arrays.asList("customer", "manager", "employee")).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy("Adam Doe").
                lastUpdatedTime(LocalDateTime.now());

        try {
            APIDefinition apiDefinition = new APIDefinitionFromSwagger20();
            List<UriTemplate> uriTemplateList = new ArrayList<>();
            for (APIResource apiResource : apiDefinition.parseSwaggerAPIResources(apiBuilder.getApiDefinition())){
                uriTemplateList.add(apiResource.getUriTemplate());
            }
            apiBuilder.uriTemplates(uriTemplateList);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }

        return apiBuilder;
    }

    public static API.APIBuilder createUniqueAPI() {
        List<String> transport = new ArrayList<>();
        transport.add("http");

        List<String> tags = new ArrayList<>();
        tags.add("food");
        tags.add("beverage");

        List<String> policies = new ArrayList<>();
        policies.add("Silver");
        policies.add("Bronze");

        BusinessInformation businessInformation = new BusinessInformation();
        businessInformation.setBusinessOwner("John Doe");
        businessInformation.setBusinessOwnerEmail("john.doe@annonymous.com");
        businessInformation.setTechnicalOwner("Jane Doe");
        businessInformation.setBusinessOwnerEmail("jane.doe@annonymous.com");

        CorsConfiguration corsConfiguration =  new CorsConfiguration();
        corsConfiguration.setEnabled(true);
        corsConfiguration.setAllowMethods(Arrays.asList("GET", "POST", "DELETE"));
        corsConfiguration.setAllowHeaders(Arrays.asList("Authorization", "X-Custom"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowOrigins(Arrays.asList("*"));

        API.APIBuilder apiBuilder = new API.APIBuilder(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "1.0.0").
                id(UUID.randomUUID().toString()).
                context(UUID.randomUUID().toString()).
                description("Get Food & Beverage Info").
                lifeCycleStatus("CREATED").
                apiDefinition(new StringBuilder(apiDefinition)).
                wsdlUri("http://www.webservicex.net/globalweather.asmx?op=GetWeather?wsdl").
                isResponseCachingEnabled(true).
                cacheTimeout(120).
                isDefaultVersion(true).
                apiPolicy("Gold").
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(Arrays.asList("customer", "manager", "employee")).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy("Adam Doe").
                lastUpdatedTime(LocalDateTime.now());

        try {
            APIDefinition apiDefinition = new APIDefinitionFromSwagger20();
            List<UriTemplate> uriTemplateList = new ArrayList<>();
            for (APIResource apiResource : apiDefinition.parseSwaggerAPIResources(apiBuilder.getApiDefinition())){
                uriTemplateList.add(apiResource.getUriTemplate());
            }
            apiBuilder.uriTemplates(uriTemplateList);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }

        return apiBuilder;
    }


    public static API.APIBuilder createCustomAPI(String name, String version, String context) {
        API.APIBuilder apiBuilder = createDefaultAPI();
        apiBuilder.name(name);
        apiBuilder.version(version);
        apiBuilder.context(context);
        return apiBuilder;
    }

    public static API copyAPISummary(API api) {
        return new API.APIBuilder(api.getProvider(), api.getName(), api.getVersion()).
                id(api.getId()).
                context(api.getContext()).
                description(api.getDescription()).
                lifeCycleStatus(api.getLifeCycleStatus()).
                lifecycleInstanceId(api.getLifecycleInstanceId()).build();
    }

    public static Application createDefaultApplication(){
        //created by admin
        Application application = new Application("TestApp", "admin");
        application.setId(UUID.randomUUID().toString());
        application.setCallbackUrl("http://localhost/myapp");
        application.setDescription("This is a test application");
        application.setGroupId("groupx");
        application.setStatus("CREATED");
        application.setTier("50PerMin");
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser("admin");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static Application createAlternativeApplication(){
        //created by admin and updated by admin2
        Application application = new Application("TestApp2", "admin");
        application.setId(UUID.randomUUID().toString());
        application.setCallbackUrl("http://localhost/myapp2");
        application.setDescription("This is test application 2");
        application.setGroupId("groupx2");
        application.setStatus("APPROVED");
        application.setTier("20PerMin");
        application.setUpdatedUser("admin2");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static Application createCustomApplication(String applicationName, String owner){
        Application application = new Application(applicationName, owner);
        application.setId(UUID.randomUUID().toString());
        application.setCallbackUrl("http://localhost/myapp");
        application.setDescription("This is a test application");
        application.setGroupId("groupx");
        application.setStatus("CREATED");
        application.setTier("50PerMin");
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser("admin");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }
}
