/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.core.util;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import java.util.UUID;

/**
 * 
 * Test cases for Endpoint comparison
 *
 */
public class EndpointComparatorTestCase {

    @Test
    public void testEndpointComparison() {

        EndPointComparator endPointComparator = new EndPointComparator();
        String testUUID = UUID.randomUUID().toString();
        Endpoint ep1 = new Endpoint.Builder().endpointConfig("{'type':'http','url':'http://localhost:8280'}")
                .id(testUUID).maxTps(1000L).security("{'enabled':false}").name("Endpoint1")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).type("http").build();

        Endpoint ep2 = new Endpoint.Builder().endpointConfig("{'type':'http','url':'http://localhost:8280'}")
                .id(testUUID).maxTps(1000L).security("{'enabled':false}").name("Endpoint1")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).type("http").build();

        Endpoint ep3 = new Endpoint.Builder().endpointConfig("{'type':'http','url':'http://localhost:8280'}")
                .id(UUID.randomUUID().toString()).maxTps(2000L).security("{'enabled':true}").name("Endpoint1")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).type("http").build();

        Endpoint ep4 = new Endpoint.Builder().endpointConfig("{'type':'http','url':'http://localhost:8280'}")
                .id(UUID.randomUUID().toString()).maxTps(1000L).security("{'enabled':false}").name("Endpoint1")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).type("http").build();

        Endpoint ep5 = new Endpoint.Builder().endpointConfig("{'type':'http','url':'http://localhost:6060'}")
                .id(UUID.randomUUID().toString()).maxTps(1000L).security("{'enabled':false}").name("Endpoint1")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).type("http").build();

        int endpointComparison1 = endPointComparator.compare(ep1, ep2);
        int endpointComparison2 = endPointComparator.compare(ep2, ep3);
        int endpointComparison3 = endPointComparator.compare(ep1, ep4);
        int endpointComparison4 = endPointComparator.compare(ep1, ep5);

        Assert.assertEquals(endpointComparison1, 0, "Endpoints are not equal. ");
        Assert.assertNotEquals(endpointComparison2, 0, "Endpoints are equal. ");
        Assert.assertEquals(endpointComparison3, 0, "Endpoints are not equal. ");
        Assert.assertNotEquals(endpointComparison4, 0, "Endpoints are equal. ");

    }
}
