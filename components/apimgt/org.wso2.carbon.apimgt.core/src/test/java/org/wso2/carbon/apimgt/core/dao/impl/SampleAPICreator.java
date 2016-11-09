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

import org.wso2.carbon.apimgt.core.models.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SampleAPICreator {

    static API createDefaultAPI() {
        List<String> transport = new ArrayList<>();
        transport.add("http");
        transport.add("https");

        List<String> tags = new ArrayList<>();
        tags.add("climate");

        List<String> policies = new ArrayList<>();
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
                apiDefinition("").
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
                lastUpdatedTime(new Date()).
                build();
    }
}
