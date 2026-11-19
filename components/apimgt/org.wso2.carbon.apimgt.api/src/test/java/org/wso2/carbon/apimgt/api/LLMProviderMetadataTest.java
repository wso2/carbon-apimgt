/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Tests the JSON mapping of {@link LLMProviderMetadata#getAnalyticsCategory()}. Provider configurations are stored
 * with Jackson ({@link LLMProviderConfiguration#toJsonString()}) and read back with Gson in the gateway.
 */
public class LLMProviderMetadataTest {

    private static final String CORE_RULE_JSON = "{\"attributeName\":\"promptTokenCount\",\"inputSource\":\"payload\","
            + "\"attributeIdentifier\":\"$.usage.prompt_tokens\",\"required\":true}";
    private static final String TOKEN_USAGE_RULE_JSON = "{\"attributeName\":\"cacheReadInputTokens\","
            + "\"inputSource\":\"payload\",\"attributeIdentifier\":\"$.usage.prompt_tokens_details.cached_tokens\","
            + "\"required\":false,\"analyticsCategory\":\"TOKEN_USAGE\"}";

    @Test
    public void testAnalyticsCategoryIsOmittedFromJsonWhenNotSet() throws Exception {

        LLMProviderMetadata coreRule = new LLMProviderMetadata("promptTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.prompt_tokens", true);

        String json = new ObjectMapper().writeValueAsString(coreRule);

        // Stored configurations of rules without a category stay exactly as before the field existed.
        Assert.assertFalse(json.contains("analyticsCategory"));
    }

    @Test
    public void testAnalyticsCategoryIsWrittenToJsonWhenSet() throws Exception {

        String json = new ObjectMapper().writeValueAsString(tokenUsageRule());

        Assert.assertTrue(json.contains("\"analyticsCategory\":\"TOKEN_USAGE\""));
    }

    @Test
    public void testAnalyticsCategoryIsReadWithJackson() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        Assert.assertEquals(APIConstants.AIAPIConstants.ANALYTICS_CATEGORY_TOKEN_USAGE,
                objectMapper.readValue(TOKEN_USAGE_RULE_JSON, LLMProviderMetadata.class).getAnalyticsCategory());
        Assert.assertNull(objectMapper.readValue(CORE_RULE_JSON, LLMProviderMetadata.class).getAnalyticsCategory());
    }

    @Test
    public void testAnalyticsCategoryIsReadWithGson() {

        Gson gson = new Gson();

        Assert.assertEquals(APIConstants.AIAPIConstants.ANALYTICS_CATEGORY_TOKEN_USAGE,
                gson.fromJson(TOKEN_USAGE_RULE_JSON, LLMProviderMetadata.class).getAnalyticsCategory());
        Assert.assertNull(gson.fromJson(CORE_RULE_JSON, LLMProviderMetadata.class).getAnalyticsCategory());
    }

    @Test
    public void testAnalyticsCategorySurvivesStoreAndGatewayReadRoundTrip() throws APIManagementException {

        LLMProviderConfiguration configuration = new LLMProviderConfiguration();
        configuration.setConnectorType("openAi_2.0.0");
        configuration.setMetadata(Arrays.asList(
                new LLMProviderMetadata("promptTokenCount", APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                        "$.usage.prompt_tokens", true),
                tokenUsageRule()));

        LLMProviderConfiguration readBack = new Gson().fromJson(configuration.toJsonString(),
                LLMProviderConfiguration.class);

        Assert.assertEquals(2, readBack.getMetadata().size());
        Assert.assertNull(readBack.getMetadata().get(0).getAnalyticsCategory());
        LLMProviderMetadata counter = readBack.getMetadata().get(1);
        Assert.assertEquals("cacheReadInputTokens", counter.getAttributeName());
        Assert.assertEquals(APIConstants.AIAPIConstants.ANALYTICS_CATEGORY_TOKEN_USAGE, counter.getAnalyticsCategory());
        Assert.assertFalse(counter.isRequired());
    }

    private static LLMProviderMetadata tokenUsageRule() {

        return new LLMProviderMetadata("cacheReadInputTokens", APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                "$.usage.prompt_tokens_details.cached_tokens", false,
                APIConstants.AIAPIConstants.ANALYTICS_CATEGORY_TOKEN_USAGE);
    }
}
