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
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.File;
import java.io.IOException;

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
}