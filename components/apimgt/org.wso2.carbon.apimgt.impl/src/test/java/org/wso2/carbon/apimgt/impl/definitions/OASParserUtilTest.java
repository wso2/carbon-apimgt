/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.definitions;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OASParserUtilTest {

    @Test
    public void testGetOASParser() throws Exception {
        String oas3 = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_v3.yaml"),
                "UTF-8");
        APIDefinition definition = OASParserUtil.getOASParser(oas3);
        Assert.assertNotNull(definition);
        Assert.assertTrue(definition instanceof OAS3Parser);

        String oas2 = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_v2.yaml"),
                "UTF-8");
        definition = OASParserUtil.getOASParser(oas2);
        Assert.assertNotNull(definition);
        Assert.assertTrue(definition instanceof OAS2Parser);

        String oasError = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_error.json"),
                "UTF-8");
        try {
            definition = OASParserUtil.getOASParser(oasError);
            Assert.fail("Exception expected");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getCause() instanceof IOException);
        }

        String oasInvalid = IOUtils.toString(getClass().getClassLoader()
                .getResourceAsStream("definitions" + File.separator + "petstore_invalid.yaml"), "UTF-8");
        try {
            definition = OASParserUtil.getOASParser(oasInvalid);
            Assert.fail("Exception expected");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid OAS definition provided."));
        }
    }

    @Test
    public void testGenerateOASConfigForHTTPEndpoints() throws Exception {
        String endpoints = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "endpointsData.json"),
                "UTF-8");
        JSONObject jsonObject = new JSONObject(endpoints);

        APIIdentifier identifier = new APIIdentifier("provider", "name", "version");
        API api = new API(identifier);
        JsonNode prodNode;
        JsonNode sandNode;

        //start http production only
        String httpProduction = jsonObject.getJSONObject("http_production").toString();
        api.setEndpointConfig(httpProduction);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(1, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_HTTP, prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNull(sandNode);

        String httpProductionFailover = jsonObject.getJSONObject("http_production_failover").toString();
        api.setEndpointConfig(httpProductionFailover);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(2, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_FAILOVER,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNull(sandNode);

        String httpProductionLoadbalance = jsonObject.getJSONObject("http_production_loadbalance").toString();
        api.setEndpointConfig(httpProductionLoadbalance);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(2, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_LOADBALANCE,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNull(sandNode);

        //start http sandbox only
        String httpSandbox = jsonObject.getJSONObject("http_sandbox").toString();
        api.setEndpointConfig(httpSandbox);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNull(prodNode);
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(1, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_HTTP, sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        String httpSandboxFailover = jsonObject.getJSONObject("http_sandbox_failover").toString();
        api.setEndpointConfig(httpSandboxFailover);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNull(prodNode);
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(2, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_FAILOVER,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        String httpSandboxLoadbalance = jsonObject.getJSONObject("http_sandbox_loadbalance").toString();
        api.setEndpointConfig(httpSandboxLoadbalance);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNull(prodNode);
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(2, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_LOADBALANCE,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        //start http hybrid
        String httpHybrid = jsonObject.getJSONObject("http_hybrid").toString();
        api.setEndpointConfig(httpHybrid);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(1, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_HTTP, prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(1, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_HTTP, sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        String httpHybridFailover = jsonObject.getJSONObject("http_hybrid_failover").toString();
        api.setEndpointConfig(httpHybridFailover);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(2, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_FAILOVER,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(2, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_FAILOVER,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        String httpHybridLoadbalance = jsonObject.getJSONObject("http_hybrid_loadbalance").toString();
        api.setEndpointConfig(httpHybridLoadbalance);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(2, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_LOADBALANCE,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(2, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_LOADBALANCE,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
    }

    @Test
    public void testGenerateOASConfigForSOAPEndpoints() throws Exception {
        String endpoints = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "endpointsData.json"),
                "UTF-8");
        JSONObject jsonObject = new JSONObject(endpoints);

        APIIdentifier identifier = new APIIdentifier("provider", "name", "version");
        API api = new API(identifier);
        JsonNode prodNode;
        JsonNode sandNode;

        //start soap production only
        String soapProduction = jsonObject.getJSONObject("soap_production").toString();
        api.setEndpointConfig(soapProduction);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(1, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_ADDRESS,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNull(sandNode);

        String soapProductionFailover = jsonObject.getJSONObject("soap_production_failover").toString();
        api.setEndpointConfig(soapProductionFailover);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(2, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_FAILOVER,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNull(sandNode);

        String soapProductionLoadbalance = jsonObject.getJSONObject("soap_production_loadbalance").toString();
        api.setEndpointConfig(soapProductionLoadbalance);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(2, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_LOADBALANCE,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNull(sandNode);

        //start soap sandbox only
        String soapSandbox = jsonObject.getJSONObject("soap_sandbox").toString();
        api.setEndpointConfig(soapSandbox);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNull(prodNode);
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(1, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_ADDRESS,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        String soapSandboxFailover = jsonObject.getJSONObject("soap_sandbox_failover").toString();
        api.setEndpointConfig(soapSandboxFailover);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNull(prodNode);
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(2, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_FAILOVER,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        String soapSandboxLoadbalance = jsonObject.getJSONObject("soap_sandbox_loadbalance").toString();
        api.setEndpointConfig(soapSandboxLoadbalance);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNull(prodNode);
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(2, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_LOADBALANCE,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        //start soap hybrid
        String soapHybrid = jsonObject.getJSONObject("soap_hybrid").toString();
        api.setEndpointConfig(soapHybrid);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(1, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_ADDRESS,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(1, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_ADDRESS,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        String soapHybridFailover = jsonObject.getJSONObject("soap_hybrid_failover").toString();
        api.setEndpointConfig(soapHybridFailover);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(2, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_FAILOVER,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(2, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_FAILOVER,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        String soapHybridLoadbalance = jsonObject.getJSONObject("soap_hybrid_loadbalance").toString();
        api.setEndpointConfig(soapHybridLoadbalance);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(2, prodNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_LOADBALANCE,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(2, sandNode.get(APIConstants.ENDPOINT_URLS).size());
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_LOADBALANCE,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
    }

    @Test
    public void testGenerateOASConfigForDefaultEndpoints() throws Exception {
        String endpoints = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "endpointsData.json"),
                "UTF-8");
        JSONObject jsonObject = new JSONObject(endpoints);

        APIIdentifier identifier = new APIIdentifier("provider", "name", "version");
        API api = new API(identifier);
        JsonNode prodNode;
        JsonNode sandNode;

        String defaultEndpoints = jsonObject.getJSONObject("default_endpoints").toString();
        api.setEndpointConfig(defaultEndpoints);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_DEFAULT,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());

        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_DEFAULT,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
    }

    @Test
    public void testGenerateOASConfigWithSecuredEndpoints() throws Exception {
        String endpoints = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "endpointsData.json"),
                "UTF-8");
        JSONObject jsonObject = new JSONObject(endpoints);

        APIIdentifier identifier = new APIIdentifier("provider", "name", "version");
        API api = new API(identifier);
        String endUserName = "end_user";
        JsonNode prodNode;
        JsonNode sandNode;
        JsonNode securityConfig;

        String defaultEndpoints = jsonObject.getJSONObject("default_endpoints").toString();
        api.setEndpointSecured(true);
        api.setEndpointConfig(defaultEndpoints);
        api.setEndpointAuthDigest(false);
        api.setEndpointUTUsername(endUserName);

        //check default production endpoint security
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_DEFAULT,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        Assert.assertNotNull(prodNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG));
        securityConfig = prodNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG);
        Assert.assertEquals(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC,
                securityConfig.get(APIConstants.ENDPOINT_SECURITY_TYPE).asText());
        Assert.assertEquals(endUserName, securityConfig.get(APIConstants.ENDPOINT_SECURITY_USERNAME).asText());

        //check default sandbox endpoint security
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, false);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_DEFAULT,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        Assert.assertNotNull(sandNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG));
        securityConfig = sandNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG);
        Assert.assertEquals(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC,
                securityConfig.get(APIConstants.ENDPOINT_SECURITY_TYPE).asText());
        Assert.assertEquals(endUserName, securityConfig.get(APIConstants.ENDPOINT_SECURITY_USERNAME).asText());

        //check default production endpoint digest auth security
        api.setEndpointAuthDigest(true);
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_DEFAULT,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        Assert.assertNotNull(prodNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG));
        securityConfig = prodNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG);
        Assert.assertEquals(APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST,
                securityConfig.get(APIConstants.ENDPOINT_SECURITY_TYPE).asText());
        Assert.assertEquals(endUserName, securityConfig.get(APIConstants.ENDPOINT_SECURITY_USERNAME).asText());

        // --------- check http endpoints security
        String httpHybrid = jsonObject.getJSONObject("http_hybrid").toString();
        api.setEndpointConfig(httpHybrid);
        api.setEndpointAuthDigest(false);
        api.setEndpointUTUsername(endUserName);

        //check http production
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_HTTP, prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        Assert.assertNotNull(prodNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG));
        securityConfig = prodNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG);
        Assert.assertEquals(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC,
                securityConfig.get(APIConstants.ENDPOINT_SECURITY_TYPE).asText());
        Assert.assertEquals(endUserName, securityConfig.get(APIConstants.ENDPOINT_SECURITY_USERNAME).asText());

        //check http sandbox
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_HTTP, sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        Assert.assertNotNull(sandNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG));
        securityConfig = sandNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG);
        Assert.assertEquals(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC,
                securityConfig.get(APIConstants.ENDPOINT_SECURITY_TYPE).asText());
        Assert.assertEquals(endUserName, securityConfig.get(APIConstants.ENDPOINT_SECURITY_USERNAME).asText());

        // ----------- check address endpoints security
        String soapHybrid = jsonObject.getJSONObject("soap_hybrid").toString();
        api.setEndpointConfig(soapHybrid);
        api.setEndpointAuthDigest(false);
        api.setEndpointUTUsername(endUserName);

        //check address production
        prodNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(prodNode);
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_ADDRESS,
                prodNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        Assert.assertNotNull(prodNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG));
        securityConfig = prodNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG);
        Assert.assertEquals(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC,
                securityConfig.get(APIConstants.ENDPOINT_SECURITY_TYPE).asText());
        Assert.assertEquals(endUserName, securityConfig.get(APIConstants.ENDPOINT_SECURITY_USERNAME).asText());

        //check address sandbox
        sandNode = OASParserUtil.generateOASConfigForEndpoints(api, true);
        Assert.assertNotNull(sandNode);
        Assert.assertEquals(APIConstants.ENDPOINT_TYPE_ADDRESS,
                sandNode.get(APIConstants.X_WSO2_ENDPOINT_TYPE).asText());
        Assert.assertNotNull(sandNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG));
        securityConfig = sandNode.get(APIConstants.ENDPOINT_SECURITY_CONFIG);
        Assert.assertEquals(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC,
                securityConfig.get(APIConstants.ENDPOINT_SECURITY_TYPE).asText());
        Assert.assertEquals(endUserName, securityConfig.get(APIConstants.ENDPOINT_SECURITY_USERNAME).asText());
    }

    @Test
    public void testSyncOpenAPIResourcePaths() throws Exception {
        String calculatorSwaggerString = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "calculator_scopes_v3.json"),
                "UTF-8");

        String calcSmallSwaggerString = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "calc_small_v3.json"),
                "UTF-8");

        final String verb = "POST";
        final String existingPathString = "/add";
        final String nonExistingPathString = "/divide";

        API api = Mockito.mock(API.class);
        Mockito.when(api.getSwaggerDefinition()).thenReturn(calculatorSwaggerString);

        APIProductResource apiProductResource = Mockito.mock(APIProductResource.class);
        URITemplate uriTemplate = Mockito.mock(URITemplate.class);
        Mockito.when(apiProductResource.getUriTemplate()).thenReturn(uriTemplate);
        Mockito.when(uriTemplate.getHTTPVerb()).thenReturn(verb);
        Mockito.when(uriTemplate.getUriTemplate()).thenReturn(existingPathString);

        Map<API, List<APIProductResource>> apiToProductResourceMapping = new HashMap<>();
        List<APIProductResource> resources = new ArrayList<>();
        resources.add(apiProductResource);
        apiToProductResourceMapping.put(api, resources);

        String updatedCalcSmallSwaggerString = OASParserUtil.updateAPIProductSwaggerOperations(apiToProductResourceMapping,
                calcSmallSwaggerString);

        JSONObject calculatorSwagger = new JSONObject(calculatorSwaggerString);
        JSONObject updatedCalcSmallSwagger = new JSONObject(updatedCalcSmallSwaggerString);

        JSONObject calculatorPaths = (JSONObject) calculatorSwagger.get(APIConstants.SWAGGER_PATHS);
        JSONObject smallCalcPaths = (JSONObject) updatedCalcSmallSwagger.get(APIConstants.SWAGGER_PATHS);

        JSONObject calculatorPath = (JSONObject) calculatorPaths.get(existingPathString);
        JSONObject smallCalculatorPath = (JSONObject) smallCalcPaths.get(existingPathString);

        // Check if both paths match
        Assert.assertEquals(calculatorPath.toString(), smallCalculatorPath.toString());

        // Ensure that an originally non existing path was not added
        Assert.assertFalse(smallCalcPaths.has(nonExistingPathString));
    }
    
    @Test
    public void testGetOASDefinitionWithTierContentAwareProperty() throws Exception {

        ArrayList<String> contentAwareTiersList;
        String apiLevelTier;
        String openAPIdef = "{\n" + 
                "  \"openapi\": \"3.0.0\",\n" + 
                "  \"info\": {\n" + 
                "    \"title\": \"Sample OpenAPI\",\n" + 
                "    \"version\": \"1.0.0\"\n" + 
                "  },\n" + 
                "  \"paths\": {\n" + 
                "    \"/users\": {\n" + 
                "      \"get\": {\n" + 
                "        \"x-throttling-tier\": \"Tier1\",\n" + 
                "        \"responses\": {\n" + 
                "          \"200\": {\n" + 
                "            \"description\": \"\"\n" + 
                "          }\n" + 
                "        }\n" + 
                "      },\n" + 
                "      \"post\": {\n" + 
                "        \"x-throttling-tier\": \"Tier2\",\n" + 
                "        \"responses\": {\n" + 
                "          \"200\": {\n" + 
                "            \"description\": \"\"\n" + 
                "          }\n" + 
                "        }\n" + 
                "      }\n" + 
                "    },\n" + 
                "    \"/resource\": {\n" + 
                "      \"get\": {\n" + 
                "        \"x-throttling-tier\": \"Tier2\",\n" + 
                "        \"responses\": {\n" + 
                "          \"200\": {\n" + 
                "            \"description\": \"\"\n" + 
                "          }\n" + 
                "        }\n" + 
                "      }\n" + 
                "    }\n" + 
                "  }\n" + 
                "}";
        
        // Content aware tiers TierX and Tier2.
        // Test 1: API level content-aware tier: TierX
        contentAwareTiersList = new ArrayList<String>();
        contentAwareTiersList.add("TierX");
        contentAwareTiersList.add("Tier2");
        apiLevelTier = "TierX";
        String definition = OASParserUtil.getOASDefinitionWithTierContentAwareProperty(openAPIdef,
                contentAwareTiersList, apiLevelTier);
        JSONObject json = new JSONObject(definition);
        // check whether 'x-throttling-bandwidth' exists only in root level and it is true and not set in resource
        // level
        Assert.assertNotNull(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH + " does not exist on root level",
                json.get(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH));
        Assert.assertTrue(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH + " is not true",
                (boolean) json.get(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH));
        // check for resource level
        JSONObject pathsObj = (JSONObject) ((JSONObject) ((JSONObject) json.get("paths")).get("/users")).get("post");
        Assert.assertFalse(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH + " exists on resource level",
                pathsObj.has(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH));

        // Test 2: API level tier not set. resource level tier set. resource POST /users is set with Tier2 content-aware
        // tier
        contentAwareTiersList = new ArrayList<String>();
        contentAwareTiersList.add("TierX");
        contentAwareTiersList.add("Tier2");
        apiLevelTier = null;
        definition = OASParserUtil.getOASDefinitionWithTierContentAwareProperty(openAPIdef, contentAwareTiersList,
                apiLevelTier);
        json = new JSONObject(definition);
        // check whether 'x-throttling-bandwidth' exists in root level. it should not be there
        Assert.assertFalse(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH + " exists on root level",
                json.has(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH));

        pathsObj = (JSONObject) ((JSONObject) ((JSONObject) json.get("paths")).get("/users")).get("post");
        Assert.assertTrue(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH + " does not exist on resource level",
                pathsObj.has(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH));
        Assert.assertTrue(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH + " is not true",
                (boolean) pathsObj.get(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH));

        // check for another resource which has same tier (Tier2) -> GET /resource has this property
        pathsObj = (JSONObject) ((JSONObject) ((JSONObject) json.get("paths")).get("/resource")).get("get");
        Assert.assertTrue(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH + " does not exist on resource level",
                pathsObj.has(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH));
        Assert.assertTrue(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH + " is not true",
                (boolean) pathsObj.get(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH));

        // check for another resource which does not have content-aware tier (Tier3) -> GET /users has this property. it
        // should not be there
        pathsObj = (JSONObject) ((JSONObject) ((JSONObject) json.get("paths")).get("/users")).get("get");
        Assert.assertFalse(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH + " exists on resource level",
                pathsObj.has(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH));

    }
}
