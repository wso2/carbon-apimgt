/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.json.simple.parser.JSONParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

public class APITest {

    private API api;
    private APIIdentifier apiIdentifier ;

    @Before
    public void init() throws Exception {
        apiIdentifier = new APIIdentifier("P1_API1_v1.0.0");
        api = new API(apiIdentifier);
        api.setEndpointSecured(true);
    }

    @Test
    public void testEndpointConfigurationsSecurity() throws Exception {
        String endpointConfig = "{\"production_endpoints\":{\"url\":\"https://webhook.site/4e659833-6181-43b6-9df6-02787e5054a0\",\"config" +
                "\":null,\"template_not_supported\":false},\"sandbox_endpoints\":{\"url\":\"https://webhook.site/4e659833-6181-43b6-9df6-02787e5054a0" +
                "\",\"config\":null,\"template_not_supported\":false},\"endpoint_type\":\"http\"}";

        api.setEndpointConfig(endpointConfig);
        api.setEndpointUTPassword("admin");
        api.setEndpointUTUsername("admin");

        JSONParser parser = new JSONParser();
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject endpointConfigJson = (JSONObject) parser.parse(api.getEndpointConfig());
        JSONObject productionEndpoint = (JSONObject) ((JSONObject) endpointConfigJson.get("endpoint_security")).get("production");
        JSONObject sandboxEndpoint = (JSONObject) ((JSONObject) endpointConfigJson.get("endpoint_security")).get("sandbox");

        Assert.assertNotNull("Endpoint Security not available for Production Endpoints" ,
                endpointConfigJson.containsKey("production_endpoints"));
        Assert.assertNotNull("Endpoint Security not available for Sandbox Endpoints" ,
                endpointConfigJson.containsKey("sandbox_endpoints"));

        Assert.assertTrue("Endpoint Security not enabled for Production Endpoints",
                (Boolean) productionEndpoint.get("enabled"));


        Assert.assertTrue("Endpoint Security not enabled for Sandbox Endpoints",
                (Boolean) sandboxEndpoint.get("enabled"));

        Assert.assertNotNull("Production Endpoint Password is null",
                productionEndpoint.containsValue("password") );
        Assert.assertNotNull("Production Endpoint Username is null",
                productionEndpoint.containsValue("Username") );

        Assert.assertNotNull("Sandbox Endpoint Password is null",
                sandboxEndpoint.containsValue("password") );
        Assert.assertNotNull("Sandbox Endpoint Username is null",
                sandboxEndpoint.containsValue("Username") );
    }

}

