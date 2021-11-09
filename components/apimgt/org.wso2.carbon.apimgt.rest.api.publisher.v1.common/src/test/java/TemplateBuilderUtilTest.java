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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.TemplateBuilderUtil;

public class TemplateBuilderUtilTest {

    @Test
    public void testAdditionalPropertyWithStoreVisibilityReplacement() {
        API api = new API(new APIIdentifier("admin", "API", "1.0"));
        api.addProperty("test__display", "true");
        api.addProperty("property", "true");

        JSONObject modifiedProperties = TemplateBuilderUtil.getModifiedProperties(api.getAdditionalProperties());
        Assert.assertEquals("Additional Properties count mismatched", 3, modifiedProperties.size());
        Assert.assertNotNull("Converted additional Property is not available", modifiedProperties.get("test"));
        Assert.assertEquals("Converted property does not have the original value", api.getProperty("test__display"),
                modifiedProperties.get("test"));

    }

    @Test
    public void testAdditionalPropertyWithoutStoreVisibility() {
        API api = new API(new APIIdentifier("admin", "API", "1.0"));

        JSONObject modifiedProperties = TemplateBuilderUtil.getModifiedProperties(api.getAdditionalProperties());
        Assert.assertEquals("Additional Properties count mismatched", 0, modifiedProperties.size());

        api.addProperty("testproperty", "true");
        modifiedProperties = TemplateBuilderUtil.getModifiedProperties(api.getAdditionalProperties());
        Assert.assertEquals("Additional Properties count mismatched", 1, modifiedProperties.size());
    }

    @Test
    public void testSubEPConfigFromSimpleRestEp() throws Exception {
        String endpointConfig = "{\"endpoint_type\": \"http\",\"sandbox_endpoints\": {\n" +
                "\"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/\"},\n" +
                "\"production_endpoints\":{\n" +
                "\"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/\"\n" +
                "}\n}";
        JSONObject oldEpConfigJson = (JSONObject) new JSONParser().parse(endpointConfig);
        String newEpConfig = TemplateBuilderUtil.populateSubscriptionEndpointConfig(endpointConfig);
        JSONObject newEpConfigJson = (JSONObject) new JSONParser().parse(newEpConfig);
        assertWSGqlSubEpConfig(newEpConfigJson, oldEpConfigJson);
    }

    @Test
    public void testSubEPConfigFromLoadBalancedRestEp() throws Exception {
        String endpointConfig = "              {\n" +
                "                \"endpoint_type\": \"load_balance\",\n" +
                "                \"algoCombo\": \"org.apache.synapse.endpoints.algorithms.RoundRobin\",\n" +
                "                \"sessionManagement\": \"\",\n" +
                "                \"sandbox_endpoints\":       [\n" +
                "                            {\n" +
                "                      \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/1\"\n" +
                "                   },\n" +
                "                            {\n" +
                "                      \"endpoint_type\": \"http\",\n" +
                "                      \"template_not_supported\": false,\n" +
                "                      \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/2\"\n" +
                "                   }\n" +
                "                ],\n" +
                "                \"production_endpoints\":       [\n" +
                "                            {\n" +
                "                      \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/3\"\n" +
                "                   },\n" +
                "                            {\n" +
                "                      \"endpoint_type\": \"http\",\n" +
                "                      \"template_not_supported\": false,\n" +
                "                      \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/4\"\n" +
                "                   }\n" +
                "                ],\n" +
                "                \"sessionTimeOut\": \"\",\n" +
                "                \"algoClassName\": \"org.apache.synapse.endpoints.algorithms.RoundRobin\"\n" +
                "              }\n";
        JSONObject oldEpConfigJson = (JSONObject) new JSONParser().parse(endpointConfig);
        String newEpConfig = TemplateBuilderUtil.populateSubscriptionEndpointConfig(endpointConfig);
        JSONObject newEpConfigJson = (JSONObject) new JSONParser().parse(newEpConfig);
        assertWSGqlSubEpConfig(newEpConfigJson, oldEpConfigJson);
    }

    @Test
    public void testSubEPConfigFromFailOverEp() throws Exception {
        String endpointConfig = "              {\n" +
                "                \"production_failovers\":[\n" +
                "                   {\n" +
                "                      \"endpoint_type\":\"http\",\n" +
                "                      \"template_not_supported\":false,\n" +
                "                      \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/1\"\n" +
                "                   }\n" +
                "                ],\n" +
                "                \"endpoint_type\":\"failover\",\n" +
                "                \"sandbox_endpoints\":{\n" +
                "                   \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/2\"\n" +
                "                },\n" +
                "                \"production_endpoints\":{\n" +
                "                   \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/3\"\n" +
                "                },\n" +
                "                \"sandbox_failovers\":[\n" +
                "                   {\n" +
                "                      \"endpoint_type\":\"http\",\n" +
                "                      \"template_not_supported\":false,\n" +
                "                      \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/4\"\n" +
                "                   }\n" +
                "                ]\n" +
                "              }";
        JSONObject oldEpConfigJson = (JSONObject) new JSONParser().parse(endpointConfig);
        String newEpConfig = TemplateBuilderUtil.populateSubscriptionEndpointConfig(endpointConfig);
        JSONObject newEpConfigJson = (JSONObject) new JSONParser().parse(newEpConfig);
        assertWSGqlSubEpConfig(newEpConfigJson, oldEpConfigJson);
    }

    @Test
    public void testSubEPConfigForWSScheme() throws Exception {
        String endpointConfig = "{\"endpoint_type\": \"http\",\"sandbox_endpoints\": {\n" +
                "\"url\": \"http://localhost:9443/am/sample/pizzashack/v1/api/\"},\n" +
                "\"production_endpoints\":{\n" +
                "\"url\": \"http://localhost:9443/am/sample/pizzashack/v1/api/\"\n" +
                "}\n}";
        JSONObject oldEpConfigJson = (JSONObject) new JSONParser().parse(endpointConfig);
        String newEpConfig = TemplateBuilderUtil.populateSubscriptionEndpointConfig(endpointConfig);
        JSONObject newEpConfigJson = (JSONObject) new JSONParser().parse(newEpConfig);
        assertWSGqlSubEpConfig(newEpConfigJson, oldEpConfigJson);
    }

    @Test
    public void testSubEPConfigForUnsupportedScheme() throws ParseException {
        String endpointConfig = "{\"endpoint_type\": \"http\",\"sandbox_endpoints\": {\n" +
                "\"url\": \"ws://localhost:9443/am/sample/pizzashack/v1/api/\"},\n" +
                "\"production_endpoints\":{\n" +
                "\"url\": \"wss://localhost:9443/am/sample/pizzashack/v1/api/\"\n" +
                "}\n}";
        try {
            TemplateBuilderUtil.populateSubscriptionEndpointConfig(endpointConfig);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Unsupported URI scheme for Production endpoint"));
        }
    }

    private void assertWSGqlSubEpConfig(JSONObject newEpConfigJson, JSONObject oldEpConfigJson) {
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_GRAPHQL,
                newEpConfigJson.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE));
        Assert.assertTrue(newEpConfigJson.containsKey(APIConstants.ENDPOINT_TYPE_HTTP));
        Assert.assertTrue(newEpConfigJson.containsKey(APIConstants.WS_PROTOCOL));
        JSONObject httpEpConfig = (JSONObject) newEpConfigJson.get(APIConstants.ENDPOINT_TYPE_HTTP);
        Assert.assertEquals(httpEpConfig.toJSONString(), oldEpConfigJson.toJSONString());
        JSONObject wsEpConfig = (JSONObject) newEpConfigJson.get(APIConstants.WS_PROTOCOL);
        String httpProdUrl;
        String httpSandboxUrl;
        if (APIConstants.ENDPOINT_TYPE_LOADBALANCE.equals(
                httpEpConfig.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE))) {
            httpProdUrl = (String) ((JSONObject) ((JSONArray) httpEpConfig
                    .get(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS)).get(0)).get(APIConstants.ENDPOINT_URL);
            httpSandboxUrl = (String) ((JSONObject) ((JSONArray) httpEpConfig
                    .get(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS)).get(0)).get(APIConstants.ENDPOINT_URL);
        } else {
            httpProdUrl = (String) ((JSONObject) httpEpConfig.get(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS))
                    .get(APIConstants.ENDPOINT_URL);
            httpSandboxUrl = (String) ((JSONObject) httpEpConfig.get(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS))
                    .get(APIConstants.ENDPOINT_URL);
        }
        //assert prod urls
        String wsProdUrl = (String) ((JSONObject) wsEpConfig.get(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS))
                .get(APIConstants.ENDPOINT_URL);
        if (httpProdUrl.indexOf(APIConstants.HTTPS_PROTOCOL_URL_PREFIX) == 0) {
            Assert.assertEquals(httpProdUrl.replace(APIConstants.HTTPS_PROTOCOL_URL_PREFIX,
                    APIConstants.WSS_PROTOCOL_URL_PREFIX), wsProdUrl);
        } else {
            Assert.assertEquals(httpProdUrl.replace(APIConstants.HTTP_PROTOCOL_URL_PREFIX,
                    APIConstants.WS_PROTOCOL_URL_PREFIX), wsProdUrl);
        }
        //assert sandbox urls
        String wsSandboxUrl = (String) ((JSONObject) wsEpConfig.get(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS))
                .get(APIConstants.ENDPOINT_URL);
        if (httpSandboxUrl.indexOf(APIConstants.HTTPS_PROTOCOL_URL_PREFIX) == 0) {
            Assert.assertEquals(httpProdUrl.replace(APIConstants.HTTPS_PROTOCOL_URL_PREFIX,
                    APIConstants.WSS_PROTOCOL_URL_PREFIX), wsProdUrl);
        } else {
            Assert.assertEquals(httpSandboxUrl.replace(APIConstants.HTTP_PROTOCOL_URL_PREFIX,
                    APIConstants.WS_PROTOCOL_URL_PREFIX), wsSandboxUrl);
        }
    }
}
