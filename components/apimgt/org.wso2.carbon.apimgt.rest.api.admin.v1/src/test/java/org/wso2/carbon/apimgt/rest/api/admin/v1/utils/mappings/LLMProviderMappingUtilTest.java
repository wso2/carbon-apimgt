/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

public class LLMProviderMappingUtilTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testResolveProviderConfigurationsUpdatesOnlyAuthConfigForBuiltInProvider() throws Exception {

        String existingConfigurations = "{"
                + "\"connectorType\":\"awsBedrock_1.0.0\","
                + "\"metadata\":[{\"attributeName\":\"promptTokenCount\",\"attributeIdentifier\":\"$.usage.input\"}],"
                + "\"authenticationConfiguration\":{\"enabled\":true,\"type\":\"aws\","
                + "\"parameters\":{\"awsServiceName\":\"bedrock\"}}"
                + "}";
        String updatedConfigurations = "{"
                + "\"connectorType\":\"updatedConnector\","
                + "\"metadata\":[{\"attributeName\":\"totalTokenCount\",\"attributeIdentifier\":\"$.usage.total\"}],"
                + "\"authenticationConfiguration\":{\"enabled\":true,\"type\":\"apikey\","
                + "\"parameters\":{\"headerEnabled\":true,\"headerName\":\"x-api-key\"}}"
                + "}";
        LLMProvider retrievedProvider = new LLMProvider();
        retrievedProvider.setBuiltInSupport(true);
        retrievedProvider.setConfigurations(existingConfigurations);

        String resolvedConfigurations = LLMProviderMappingUtil.resolveProviderConfigurations(retrievedProvider,
                updatedConfigurations);

        JsonNode resolvedConfig = OBJECT_MAPPER.readTree(resolvedConfigurations);
        Assert.assertEquals("awsBedrock_1.0.0", resolvedConfig.get("connectorType").asText());
        Assert.assertEquals("promptTokenCount", resolvedConfig.get("metadata").get(0).get("attributeName").asText());
        Assert.assertEquals("apikey", resolvedConfig.get("authenticationConfiguration").get("type").asText());
        Assert.assertEquals("x-api-key", resolvedConfig.get("authenticationConfiguration")
                .get("parameters").get("headerName").asText());
    }

    @Test
    public void testResolveProviderConfigurationsPreservesBuiltInConfigWhenAuthConfigIsAbsent() throws Exception {

        String existingConfigurations = "{"
                + "\"connectorType\":\"awsBedrock_1.0.0\","
                + "\"authenticationConfiguration\":{\"enabled\":true,\"type\":\"aws\"}"
                + "}";
        String updatedConfigurations = "{\"connectorType\":\"updatedConnector\"}";
        LLMProvider retrievedProvider = new LLMProvider();
        retrievedProvider.setBuiltInSupport(true);
        retrievedProvider.setConfigurations(existingConfigurations);

        String resolvedConfigurations = LLMProviderMappingUtil.resolveProviderConfigurations(retrievedProvider,
                updatedConfigurations);

        Assert.assertEquals(OBJECT_MAPPER.readTree(existingConfigurations),
                OBJECT_MAPPER.readTree(resolvedConfigurations));
    }

    @Test
    public void testResolveProviderConfigurationsReplacesConfigForCustomProvider() throws Exception {

        String existingConfigurations = "{\"connectorType\":\"oldConnector\"}";
        String updatedConfigurations = "{\"connectorType\":\"newConnector\"}";
        LLMProvider retrievedProvider = new LLMProvider();
        retrievedProvider.setBuiltInSupport(false);
        retrievedProvider.setConfigurations(existingConfigurations);

        String resolvedConfigurations = LLMProviderMappingUtil.resolveProviderConfigurations(retrievedProvider,
                updatedConfigurations);

        Assert.assertEquals(OBJECT_MAPPER.readTree(updatedConfigurations),
                OBJECT_MAPPER.readTree(resolvedConfigurations));
    }
}
