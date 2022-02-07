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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.WebSocketTopicMappingConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.TemplateBuilderUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TemplateBuilderUtilTest {

    private static final String wsProdEndpoint = "wss://production.com";
    private static final String wsSandEndpoint = "wss://sandbox.com";
    private static final String wsProdEpName = "API--v1.0_APIproductionEndpoint_mapping__wildcard";
    private static final String wsSandEpName = "API--v1.0_APIsandboxEndpoint_mapping__wildcard";
    private static final String wildCardResource = "/*";
    private static final String mappingWildCard = "mapping__wildcard";

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
    public void addAddGqlWebSocketResourceEndpoints() throws Exception {
        APITemplateBuilder apiTemplateBuilder = Mockito.mock(APITemplateBuilder.class);
        API api = new API(new APIIdentifier("admin", "API", "1.0"));
        Set<URITemplate> uriTemplates = new HashSet<>();
        URITemplate template = new URITemplate();
        template.setAuthType("Any");
        template.setHTTPVerb("POST");
        template.setHttpVerbs("POST");
        template.setUriTemplate(wildCardResource);
        uriTemplates.add(template);
        template = new URITemplate();
        template.setUriTemplate(wildCardResource);
        uriTemplates.add(template);
        api.setUriTemplates(uriTemplates);
        Map<String, Map<String, String>> perTopicMappings = new HashMap<>();
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION, wsProdEndpoint);
        endpoints.put(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, wsSandEndpoint);
        perTopicMappings.put(wildCardResource, endpoints);
        WebSocketTopicMappingConfiguration webSocketTopicMappingConfiguration =
                new WebSocketTopicMappingConfiguration(perTopicMappings);
        webSocketTopicMappingConfiguration.setResourceKey(wildCardResource, mappingWildCard);
        api.setWebSocketTopicMappingConfiguration(webSocketTopicMappingConfiguration);
        api.setType(APIConstants.GRAPHQL_API);
        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        GatewayContentDTO dummyHttpProdEpContentDTO = new GatewayContentDTO();
        dummyHttpProdEpContentDTO.setName("API--v1.0_APIproductionEndpoint");
        dummyHttpProdEpContentDTO.setContent("dummy content");
        GatewayContentDTO[] gatewayContentDTOS = new GatewayContentDTO[1];
        gatewayContentDTOS[0] = dummyHttpProdEpContentDTO;
        gatewayAPIDTO.setEndpointEntriesToBeAdd(gatewayContentDTOS);
        String dummyProdEndpointConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><endpoint name=\""
                + wsProdEpName + "\">dummy prod content</endpoint>";
        String dummySandboxEndpointConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><endpoint name=\""
                + wsSandEpName + "\">dummy sandbox content</endpoint>";
        Mockito.when(apiTemplateBuilder.getConfigStringForWebSocketEndpointTemplate(
                APIConstants.GATEWAY_ENV_TYPE_PRODUCTION, mappingWildCard, wsProdEndpoint)).thenReturn(
                dummyProdEndpointConfig);
        Mockito.when(apiTemplateBuilder.getConfigStringForWebSocketEndpointTemplate(
                APIConstants.GATEWAY_ENV_TYPE_SANDBOX, mappingWildCard, wsSandEndpoint)).thenReturn(
                dummySandboxEndpointConfig);
        TemplateBuilderUtil.addWebSocketResourceEndpoints(api, apiTemplateBuilder, gatewayAPIDTO);
        GatewayContentDTO[] endpointEntries = gatewayAPIDTO.getEndpointEntriesToBeAdd();
        Assert.assertEquals(endpointEntries.length, 3);
        boolean httpEpConfigPresent = false;
        boolean wsProdEpConfigPresent = false;
        boolean wsSandEPConfigPresent = false;
        for (int i = 0; i < 3; i++) {
            if (dummyHttpProdEpContentDTO.getName().equals(endpointEntries[i].getName())) {
                httpEpConfigPresent = true;
                Assert.assertEquals(endpointEntries[i].getContent(), dummyHttpProdEpContentDTO.getContent());
            }
            if (wsProdEpName.equals(endpointEntries[i].getName())) {
                wsProdEpConfigPresent = true;
                Assert.assertEquals(endpointEntries[i].getContent(), dummyProdEndpointConfig);
            }
            if (wsSandEpName.equals(endpointEntries[i].getName())) {
                wsSandEPConfigPresent = true;
                Assert.assertEquals(endpointEntries[i].getContent(), dummySandboxEndpointConfig);
            }
        }
        Assert.assertTrue(wsProdEpConfigPresent);
        Assert.assertTrue(wsSandEPConfigPresent);
        Assert.assertTrue(httpEpConfigPresent);
    }

    @Test
    public void testAddGqlWebSocketTopicMappings() {

        API api = new API(new APIIdentifier("admin", "GraphQLAPI", "1.0"));
        String endpointConfig = "{\"endpoint_type\":\"graphql\", \n" +
                "\"http\":{\"endpoint_type\":\"http\",\n" +
                "\"sandbox_endpoints\":{\"url\":\"https://sandbox.com\"},\n" +
                "\"production_endpoints\":{\"url\":\"https://production.com\"}},\n" +
                "\"ws\":{\"endpoint_type\":\"ws\",\n" +
                "\"sandbox_endpoints\":{\"url\":\"" + wsSandEndpoint + "\"},\n" +
                "\"production_endpoints\":{\"url\":\"" + wsProdEndpoint + "\"}}}";
        api.setEndpointConfig(endpointConfig);
        TemplateBuilderUtil.addGqlWebSocketTopicMappings(api);
        WebSocketTopicMappingConfiguration topicMappingConfiguration = api.getWebSocketTopicMappingConfiguration();
        Assert.assertNotNull(topicMappingConfiguration);
        Map<String, String> mappings = topicMappingConfiguration.getMappings().get(wildCardResource);
        Assert.assertNotNull(mappings);
        Assert.assertEquals(mappings.get(APIConstants.GATEWAY_ENV_TYPE_SANDBOX), wsSandEndpoint);
        Assert.assertEquals(mappings.get(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION), wsProdEndpoint);
        Assert.assertEquals(mappings.get("resourceKey"), mappingWildCard);

        //test endpoint config wit only production type endpoints
        api = new API(new APIIdentifier("admin", "GraphQLAPI", "1.0"));
        api.setEndpointConfig("{\"endpoint_type\":\"graphql\", \n" +
                "\"http\":{\"endpoint_type\":\"http\",\n" +
                "\"production_endpoints\":{\"url\":\"https://production.com\"}},\n" +
                "\"ws\":{\"endpoint_type\":\"ws\",\n" +
                "\"production_endpoints\":{\"url\":\"" + wsProdEndpoint + "\"}}}");
        TemplateBuilderUtil.addGqlWebSocketTopicMappings(api);
        topicMappingConfiguration = api.getWebSocketTopicMappingConfiguration();
        mappings = topicMappingConfiguration.getMappings().get(wildCardResource);
        Assert.assertNull(mappings.get(APIConstants.GATEWAY_ENV_TYPE_SANDBOX));
        Assert.assertEquals(mappings.get(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION), wsProdEndpoint);
    }

    @Test
    public void testSubEPConfigFromSimpleRestEp() throws Exception {
        String endpointConfig = "{\"endpoint_type\": \"http\",\"sandbox_endpoints\": {\n" +
                "\"url\": \"https://sandbox.com/\"},\n" +
                "\"production_endpoints\":{\n" +
                "\"url\": \"https://production.com/\"\n" +
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
                "                      \"url\": \"https://sandbox.com/1\"\n" +
                "                   },\n" +
                "                            {\n" +
                "                      \"endpoint_type\": \"http\",\n" +
                "                      \"template_not_supported\": false,\n" +

                "                      \"url\": \"https://sandbox.com/2\"\n" +
                "                   }\n" +
                "                ],\n" +
                "                \"production_endpoints\":       [\n" +
                "                            {\n" +
                "                      \"url\": \"https://production.com/1\"\n" +
                "                   },\n" +
                "                            {\n" +
                "                      \"endpoint_type\": \"http\",\n" +
                "                      \"template_not_supported\": false,\n" +
                "                      \"url\": \"https://production.com/2\"\n" +
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
                "                      \"url\":\"https://production.com/2\"\n" +
                "                   }\n" +
                "                ],\n" +
                "                \"endpoint_type\":\"failover\",\n" +
                "                \"sandbox_endpoints\":{\n" +
                "                   \"url\":\"https://sandbox.com/1\"\n" +
                "                },\n" +
                "                \"production_endpoints\":{\n" +
                "                   \"url\":\"https://production.com/1\"\n" +
                "                },\n" +
                "                \"sandbox_failovers\":[\n" +
                "                   {\n" +
                "                      \"endpoint_type\":\"http\",\n" +
                "                      \"template_not_supported\":false,\n" +
                "                      \"url\":\"https://sandbox.com/2\"\n" +
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
                "\"url\": \"http://sandbox.com/\"},\n" +
                "\"production_endpoints\":{\n" +
                "\"url\": \"http://production.com/\"\n" +
                "}\n}";
        JSONObject oldEpConfigJson = (JSONObject) new JSONParser().parse(endpointConfig);
        String newEpConfig = TemplateBuilderUtil.populateSubscriptionEndpointConfig(endpointConfig);
        JSONObject newEpConfigJson = (JSONObject) new JSONParser().parse(newEpConfig);
        assertWSGqlSubEpConfig(newEpConfigJson, oldEpConfigJson);
    }

    @Test
    public void testSubEPConfigForUnsupportedScheme() {
        String endpointConfig = "{\"endpoint_type\": \"http\",\"sandbox_endpoints\": {\n" +
                "\"url\": \"wss://sandbox.com\"},\n" +
                "\"production_endpoints\":{\n" +
                "\"url\": \"wss://production.com\"\n" +
                "}\n}";
        try {
            TemplateBuilderUtil.populateSubscriptionEndpointConfig(endpointConfig);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Unsupported URI scheme present for Production endpoint: "
                    + wsProdEndpoint));
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
