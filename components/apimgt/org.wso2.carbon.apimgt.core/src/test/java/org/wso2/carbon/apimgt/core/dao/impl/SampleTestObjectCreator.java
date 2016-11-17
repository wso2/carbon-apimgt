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

package org.wso2.carbon.apimgt.core.dao.impl;

import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Environment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SampleTestObjectCreator {

    static API.APIBuilder createDefaultAPI() {
        List<java.lang.String> transport = new ArrayList<>();
        transport.add("http");
        transport.add("https");

        List<java.lang.String> tags = new ArrayList<>();
        tags.add("climate");

        List<java.lang.String> policies = new ArrayList<>();
        policies.add("Gold");
        policies.add("Silver");
        policies.add("Bronze");

        Endpoint endpoint = new Endpoint();
        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(endpoint);

        Environment environment =  new Environment();
        List<Environment> environmentList = new ArrayList<>();
        environmentList.add(environment);

        BusinessInformation businessInformation = new BusinessInformation();
        CorsConfiguration corsConfiguration =  new CorsConfiguration();

        return new API.APIBuilder("admin", "WeatherAPI", "1.0.0").
                id(UUID.randomUUID().toString()).
                context("weather").
                description("Get Weather Info").
                lifeCycleStatus("CREATED").
                apiDefinition("{\"paths\":{\"/order\":{\"post\":{\"x-auth-type\":\"Application & Application User\"," +
                        "\"x-throttling-tier\":\"Unlimited\",\"description\":\"Create a new Order\"," +
                        "\"parameters\":[{\"schema\":{\"$ref\":\"#/definitions/Order\"}," +
                        "\"description\":\"Order object that needs to be added\",\"name\":\"body\",\"required\":true," +
                        "\"in\":\"body\"}],\"responses\":{\"201\":{\"headers\":{\"Location\":" +
                        "{\"description\":\"The URL of the newly created resource.\",\"type\":\"string\"}}," +
                        "\"schema\":{\"$ref\":\"#/definitions/Order\"},\"description\":\"Created.\"}}}}," +
                        "\"/menu\":{\"get\":{\"x-auth-type\":\"Application & Application User\"," +
                        "\"x-throttling-tier\":\"Unlimited\",\"description\":\"" +
                        "Return a list of available menu items\",\"parameters\":[]," +
                        "\"responses\":{\"200\":{\"headers\":{},\"schema\":{\"title\":\"Menu\"," +
                        "\"properties\":{\"list\":{\"items\":{\"$ref\":\"#/definitions/MenuItem\"}," +
                        "\"type\":\"array\"}},\"type\":\"object\"},\"description\":\"OK.\"}}}}}," +
                        "\"schemes\":[\"https\"],\"produces\":[\"application/json\"],\"swagger\":\"2.0\"," +
                        "\"definitions\":{\"MenuItem\":{\"title\":\"Pizza menu Item\"," +
                        "\"properties\":{\"price\":{\"type\":\"string\"},\"description\":{\"type\":\"string\"}," +
                        "\"name\":{\"type\":\"string\"},\"image\":{\"type\":\"string\"}},\"required\":[\"name\"]}," +
                        "\"Order\":{\"title\":\"Pizza Order\",\"properties\":{\"customerName\":{\"type\":\"string\"}," +
                        "\"delivered\":{\"type\":\"boolean\"},\"address\":{\"type\":\"string\"}," +
                        "\"pizzaType\":{\"type\":\"string\"},\"creditCardNumber\":{\"type\":\"string\"}," +
                        "\"quantity\":{\"type\":\"number\"},\"orderId\":{\"type\":\"integer\"}}," +
                        "\"required\":[\"orderId\"]}},\"consumes\":[\"application/json\"]," +
                        "\"info\":{\"title\":\"PizzaShackAPI\"," +
                        "\"description\":\"This document describe a RESTFul API for Pizza Shack " +
                        "online pizza delivery store.\\n\",\"license\":{\"name\":\"Apache 2.0\"," +
                        "\"url\":\"http://www.apache.org/licenses/LICENSE-2.0.html\"}," +
                        "\"contact\":{\"email\":\"architecture@pizzashack.com\",\"name\":\"John Doe\"," +
                        "\"url\":\"http://www.pizzashack.com\"},\"version\":\"1.0.0\"}}").
                wsdlUri("").
                isResponseCachingEnabled(false).
                cacheTimeout(60).
                isDefaultVersion(false).
                apiPolicy("Unlimited").
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.PUBLIC).
                visibleRoles(new ArrayList<>()).
                endpoints(endpointList).
                gatewayEnvironments(environmentList).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(new Date()).
                createdBy("admin").
                lastUpdatedTime(new Date());
    }

    static API.APIBuilder createAlternativeAPI() {
        List<java.lang.String> transport = new ArrayList<>();
        transport.add("http");

        List<java.lang.String> tags = new ArrayList<>();
        tags.add("food");
        tags.add("beverage");

        List<java.lang.String> policies = new ArrayList<>();
        policies.add("Silver");
        policies.add("Bronze");

        Endpoint endpoint = new Endpoint();
        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(endpoint);

        Environment environment =  new Environment();
        List<Environment> environmentList = new ArrayList<>();
        environmentList.add(environment);

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

        return new API.APIBuilder("Adam", "restaurantAPI", "0.9").
                id(UUID.randomUUID().toString()).
                context("yummy").
                description("Get Food & Beverage Info").
                lifeCycleStatus("CREATED").
                apiDefinition("{\"paths\":{\"/order\":{\"post\":{\"x-auth-type\":\"Application & Application User\"," +
                        "\"x-throttling-tier\":\"Unlimited\",\"description\":\"Create a new Order\"," +
                        "\"parameters\":[{\"schema\":{\"$ref\":\"#/definitions/Order\"}," +
                        "\"description\":\"Order object that needs to be added\",\"name\":\"body\",\"required\":true," +
                        "\"in\":\"body\"}],\"responses\":{\"201\":{\"headers\":{\"Location\":" +
                        "{\"description\":\"The URL of the newly created resource.\",\"type\":\"string\"}}," +
                        "\"schema\":{\"$ref\":\"#/definitions/Order\"},\"description\":\"Created.\"}}}}," +
                        "\"/menu\":{\"get\":{\"x-auth-type\":\"Application & Application User\"," +
                        "\"x-throttling-tier\":\"Unlimited\",\"description\":\"" +
                        "Return a list of available menu items\",\"parameters\":[]," +
                        "\"responses\":{\"200\":{\"headers\":{},\"schema\":{\"title\":\"Menu\"," +
                        "\"properties\":{\"list\":{\"items\":{\"$ref\":\"#/definitions/MenuItem\"}," +
                        "\"type\":\"array\"}},\"type\":\"object\"},\"description\":\"OK.\"}}}}}," +
                        "\"schemes\":[\"https\"],\"produces\":[\"application/json\"],\"swagger\":\"2.0\"," +
                        "\"definitions\":{\"MenuItem\":{\"title\":\"Pizza menu Item\"," +
                        "\"properties\":{\"price\":{\"type\":\"string\"},\"description\":{\"type\":\"string\"}," +
                        "\"name\":{\"type\":\"string\"},\"image\":{\"type\":\"string\"}},\"required\":[\"name\"]}," +
                        "\"Order\":{\"title\":\"Pizza Order\",\"properties\":{\"customerName\":{\"type\":\"string\"}," +
                        "\"delivered\":{\"type\":\"boolean\"},\"address\":{\"type\":\"string\"}," +
                        "\"pizzaType\":{\"type\":\"string\"},\"creditCardNumber\":{\"type\":\"string\"}," +
                        "\"quantity\":{\"type\":\"number\"},\"orderId\":{\"type\":\"integer\"}}," +
                        "\"required\":[\"orderId\"]}},\"consumes\":[\"application/json\"]," +
                        "\"info\":{\"title\":\"PizzaShackAPI\"," +
                        "\"description\":\"This document describe a RESTFul API for Pizza Shack " +
                        "online pizza delivery store.\\n\",\"license\":{\"name\":\"Apache 2.0\"," +
                        "\"url\":\"http://www.apache.org/licenses/LICENSE-2.0.html\"}," +
                        "\"contact\":{\"email\":\"architecture@pizzashack.com\",\"name\":\"John Doe\"," +
                        "\"url\":\"http://www.pizzashack.com\"},\"version\":\"1.0.0\"}}").
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
                endpoints(endpointList).
                gatewayEnvironments(environmentList).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(new Date()).
                createdBy("Adam Doe").
                lastUpdatedTime(new Date());
    }

    static Application createDefaultApplication(){
        //created by admin
        Application application = new Application("TestApp", "admin");
        application.setUUID(UUID.randomUUID().toString());
        application.setCallbackUrl("http://localhost/myapp");
        application.setDescription("This is a test application");
        application.setGroupId("groupx");
        application.setStatus("CREATED");
        application.setTier("gold");
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser("admin");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    static Application createAlternativeApplication(){
        //created by admin and updated by admin2
        Application application = new Application("TestApp2", "admin");
        application.setUUID(UUID.randomUUID().toString());
        application.setCallbackUrl("http://localhost/myapp2");
        application.setDescription("This is test application 2");
        application.setGroupId("groupx2");
        application.setStatus("APPROVED");
        application.setTier("silver");
        application.setUpdatedUser("admin2");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }
}
