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

import com.google.gson.Gson;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests the default response metadata ({@link LLMProviderService#getDefaultResponseMetadata()}) shipped by the
 * built-in LLM provider services. The gateway applies these rules in memory on top of a provider's stored rules, so
 * they must stay optional, analytics-only token usage counters that never replace a core attribute.
 */
public class LLMProviderDefaultResponseMetadataTest {

    private static final Set<String> CORE_ATTRIBUTE_NAMES = new HashSet<>(Arrays.asList(
            APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REQUEST_MODEL,
            APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_RESPONSE_MODEL,
            APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_PROMPT_TOKEN_COUNT,
            APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_COMPLETION_TOKEN_COUNT,
            APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_TOTAL_TOKEN_COUNT,
            APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REMAINING_TOKEN_COUNT));

    // OpenAI Chat Completions usage shape, also returned by Azure OpenAI v1, Azure AI Foundry and Mistral.
    private static final String CHAT_COMPLETIONS_RESPONSE = "{\"usage\":{\"prompt_tokens\":1200,"
            + "\"completion_tokens\":300,\"total_tokens\":1500,\"prompt_tokens_details\":{\"cached_tokens\":800,"
            + "\"cache_write_tokens\":40},\"completion_tokens_details\":{\"reasoning_tokens\":120}}}";

    // OpenAI Responses API usage shape, returned by Azure OpenAI v2.
    private static final String RESPONSES_API_RESPONSE = "{\"usage\":{\"input_tokens\":1200,\"output_tokens\":300,"
            + "\"total_tokens\":1500,\"input_tokens_details\":{\"cached_tokens\":800,\"cache_write_tokens\":40},"
            + "\"output_tokens_details\":{\"reasoning_tokens\":120}}}";

    // Anthropic Messages usage shape, also returned by Vertex AI Anthropic.
    private static final String ANTHROPIC_RESPONSE = "{\"usage\":{\"input_tokens\":50,\"output_tokens\":503,"
            + "\"cache_read_input_tokens\":2051,\"cache_creation_input_tokens\":1000,\"cache_creation\":"
            + "{\"ephemeral_5m_input_tokens\":600,\"ephemeral_1h_input_tokens\":400},"
            + "\"output_tokens_details\":{\"thinking_tokens\":120}}}";

    // Gemini usage shape, also returned by Vertex AI Gemini.
    private static final String GEMINI_RESPONSE = "{\"usageMetadata\":{\"promptTokenCount\":1200,"
            + "\"candidatesTokenCount\":300,\"totalTokenCount\":1550,\"cachedContentTokenCount\":800,"
            + "\"thoughtsTokenCount\":50,\"toolUsePromptTokenCount\":10}}";

    // AWS Bedrock Converse usage shape.
    private static final String BEDROCK_RESPONSE = "{\"usage\":{\"inputTokens\":1200,\"outputTokens\":300,"
            + "\"totalTokens\":1500,\"cacheReadInputTokens\":800,\"cacheWriteInputTokens\":100}}";

    @Test
    public void testDefaultsAreOptionalTokenUsageCountersWithValidJsonPath() {

        for (BuiltInLLMProviderService provider : builtInProviders()) {
            List<LLMProviderMetadata> defaults = provider.getDefaultResponseMetadata();
            String type = provider.getType();
            Assert.assertFalse("No defaults for " + type, defaults.isEmpty());
            Set<String> names = new HashSet<>();
            for (LLMProviderMetadata rule : defaults) {
                String name = rule.getAttributeName();
                Assert.assertNotNull("Unnamed default for " + type, name);
                Assert.assertTrue("Duplicate default " + name + " for " + type, names.add(name));
                Assert.assertFalse("Default " + name + " for " + type + " replaces a core attribute",
                        CORE_ATTRIBUTE_NAMES.contains(name));
                Assert.assertEquals(APIConstants.AIAPIConstants.ANALYTICS_CATEGORY_TOKEN_USAGE,
                        rule.getAnalyticsCategory());
                Assert.assertEquals(APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, rule.getInputSource());
                Assert.assertFalse("Default " + name + " for " + type + " is required", rule.isRequired());
                JsonPath.compile(rule.getAttributeIdentifier());
            }
        }
    }

    @Test
    public void testServicesWithoutAnOverrideHaveNoDefaults() {

        Assert.assertTrue(new DefaultLLMProviderService().getDefaultResponseMetadata().isEmpty());
        Assert.assertTrue(new BuiltInLLMProviderServiceTestProvider().getDefaultResponseMetadata().isEmpty());
    }

    @Test
    public void testRegisteredConfigurationContainsEachDefaultOnce() throws APIManagementException {

        for (BuiltInLLMProviderService provider : builtInProviders()) {
            String type = provider.getType();
            // Read the stored configuration back the way the gateway does.
            List<LLMProviderMetadata> stored = new Gson().fromJson(provider.getLLMProvider().getConfigurations(),
                    LLMProviderConfiguration.class).getMetadata();
            Map<String, LLMProviderMetadata> storedByName = new HashMap<>();
            for (LLMProviderMetadata rule : stored) {
                Assert.assertNull("Duplicate rule " + rule.getAttributeName() + " for " + type,
                        storedByName.put(rule.getAttributeName(), rule));
            }
            for (LLMProviderMetadata expected : provider.getDefaultResponseMetadata()) {
                LLMProviderMetadata actual = storedByName.get(expected.getAttributeName());
                Assert.assertNotNull("Missing " + expected.getAttributeName() + " for " + type, actual);
                Assert.assertEquals(expected.getAttributeIdentifier(), actual.getAttributeIdentifier());
                Assert.assertEquals(expected.getAnalyticsCategory(), actual.getAnalyticsCategory());
            }
        }
    }

    @Test
    public void testChatCompletionsProvidersExtractCacheAndReasoningTokens() throws APIManagementException {

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("cacheReadInputTokens", "800");
        expected.put("cacheWriteInputTokens", "40");
        expected.put("reasoningTokens", "120");

        assertExtracted(new OpenAiLLMProviderService(), CHAT_COMPLETIONS_RESPONSE, expected);
        assertExtracted(new AzureOpenAiLLMProviderService(), CHAT_COMPLETIONS_RESPONSE, expected);
        assertExtracted(new AzureAiFoundryLLMProviderService(), CHAT_COMPLETIONS_RESPONSE, expected);
    }

    @Test
    public void testMistralExtractsCacheReadTokens() throws APIManagementException {

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("cacheReadInputTokens", "800");

        assertExtracted(new MistralAiLLMProviderService(), CHAT_COMPLETIONS_RESPONSE, expected);
    }

    @Test
    public void testResponsesApiProviderExtractsCacheAndReasoningTokens() throws APIManagementException {

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("cacheReadInputTokens", "800");
        expected.put("cacheWriteInputTokens", "40");
        expected.put("reasoningTokens", "120");

        assertExtracted(new AzureOpenAiLLMProviderServiceV2(), RESPONSES_API_RESPONSE, expected);
    }

    @Test
    public void testAnthropicProvidersExtractCacheAndThinkingTokens() throws APIManagementException {

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("cacheReadInputTokens", "2051");
        expected.put("cacheWriteInputTokens", "1000");
        expected.put("cacheWrite5mInputTokens", "600");
        expected.put("cacheWrite1hInputTokens", "400");
        expected.put("reasoningTokens", "120");

        assertExtracted(new AnthropicLLMProviderService(), ANTHROPIC_RESPONSE, expected);
        assertExtracted(new VertexAIAnthropicLLMProviderService(), ANTHROPIC_RESPONSE, expected);
    }

    @Test
    public void testGeminiProvidersExtractCacheReasoningAndToolUseTokens() throws APIManagementException {

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("cacheReadInputTokens", "800");
        expected.put("reasoningTokens", "50");
        expected.put("toolUsePromptTokens", "10");

        assertExtracted(new GeminiLLMProviderService(), GEMINI_RESPONSE, expected);
        assertExtracted(new GeminiLLMProviderServiceV1_1_0(), GEMINI_RESPONSE, expected);
        assertExtracted(new VertexAIGeminiLLMProviderService(), GEMINI_RESPONSE, expected);
    }

    @Test
    public void testBedrockExtractsCacheTokens() throws APIManagementException {

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("cacheReadInputTokens", "800");
        expected.put("cacheWriteInputTokens", "100");

        assertExtracted(new AWSBedrockLLMProviderService(), BEDROCK_RESPONSE, expected);
    }

    @Test
    public void testDefaultsExtractNothingWhenTheResponseHasNoCounters() throws APIManagementException {

        String responseWithoutCounters = "{\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":5}}";

        for (BuiltInLLMProviderService provider : builtInProviders()) {
            assertExtracted(provider, responseWithoutCounters, new HashMap<>());
        }
    }

    /**
     * Applies the provider's defaults to the response with the Gson JsonPath provider, as in the gateway, and checks
     * that exactly the expected counters are extracted.
     */
    private static void assertExtracted(BuiltInLLMProviderService provider, String payload,
                                        Map<String, String> expected) throws APIManagementException {

        BuiltInLLMProviderServiceTest.useGsonJsonProvider();
        try {
            Map<String, String> extracted = provider.getResponseMetadata(
                    new LLMResponseMetaData(payload, null, null, null), provider.getDefaultResponseMetadata(),
                    new HashMap<>());
            Assert.assertEquals("Extracted counters for " + provider.getType(), expected, extracted);
        } finally {
            Configuration.setDefaults(null);
        }
    }

    /**
     * The built-in provider services, with the API definition read stubbed so that getLLMProvider() works without
     * the product's resource files.
     */
    private static List<BuiltInLLMProviderService> builtInProviders() {

        return Arrays.asList(
                new OpenAiLLMProviderService() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                },
                new AzureOpenAiLLMProviderService() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                },
                new AzureOpenAiLLMProviderServiceV2() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                },
                new AzureAiFoundryLLMProviderService() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                },
                new MistralAiLLMProviderService() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                },
                new AnthropicLLMProviderService() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                },
                new VertexAIAnthropicLLMProviderService() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                },
                new GeminiLLMProviderService() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                },
                new GeminiLLMProviderServiceV1_1_0() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                },
                new VertexAIGeminiLLMProviderService() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                },
                new AWSBedrockLLMProviderService() {
                    @Override
                    protected String readApiDefinition(String filePath) {
                        return "{}";
                    }
                });
    }

    /**
     * A built-in provider service that does not override getDefaultResponseMetadata().
     */
    private static class BuiltInLLMProviderServiceTestProvider extends BuiltInLLMProviderService {

        @Override
        public String getType() {
            return "TestProvider";
        }

        @Override
        public LLMProvider getLLMProvider() {
            return null;
        }
    }
}
