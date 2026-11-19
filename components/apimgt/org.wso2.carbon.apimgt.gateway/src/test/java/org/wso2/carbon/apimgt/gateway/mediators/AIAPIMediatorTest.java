/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants;
import org.wso2.carbon.apimgt.api.LLMProviderConfiguration;
import org.wso2.carbon.apimgt.api.LLMProviderMetadata;
import org.wso2.carbon.apimgt.api.LLMProviderService;
import org.wso2.carbon.apimgt.api.LLMResponseMetaData;
import org.wso2.carbon.apimgt.api.model.LLMProviderInfo;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the token usage analytics of {@link AIAPIMediator}: applying a provider's default response metadata on top of
 * its stored rules, extracting the token usage counters, and publishing them through
 * PRODUCT_ANALYTICS_PROPS_TO_PUBLISH.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, RelayUtils.class})
public class AIAPIMediatorTest {

    private Map<String, Object> properties;
    private Axis2MessageContext messageContext;
    private LLMProviderService llmProviderService;
    // Values in the simulated LLM response, by attribute name.
    private Map<String, String> responseValues;
    // Rules the mediator passed to the provider service for extraction.
    private List<LLMProviderMetadata> extractionRules;

    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(RelayUtils.class);

        properties = new HashMap<>();
        messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getAxis2MessageContext())
                .thenReturn(Mockito.mock(org.apache.axis2.context.MessageContext.class));
        Mockito.when(messageContext.getProperty(Mockito.anyString()))
                .thenAnswer(invocation -> properties.get((String) invocation.getArguments()[0]));
        Mockito.doAnswer(invocation -> properties.put((String) invocation.getArguments()[0],
                invocation.getArguments()[1])).when(messageContext).setProperty(Mockito.anyString(), Mockito.any());

        responseValues = new HashMap<>();
        llmProviderService = Mockito.mock(LLMProviderService.class);
        Mockito.when(llmProviderService.getDefaultResponseMetadata()).thenReturn(Arrays.asList(
                tokenUsageRule("cacheReadInputTokens"), tokenUsageRule("reasoningTokens")));
        // Extracts each attribute the given rules name and the simulated response has.
        Mockito.when(llmProviderService.getResponseMetadata(Mockito.any(LLMResponseMetaData.class), Mockito.anyList(),
                Mockito.anyMap())).thenAnswer(invocation -> {
            List<LLMProviderMetadata> rules = invocation.getArgument(1);
            Map<String, String> metadataMap = invocation.getArgument(2);
            extractionRules = new ArrayList<>(rules);
            for (LLMProviderMetadata rule : rules) {
                if (responseValues.containsKey(rule.getAttributeName())) {
                    metadataMap.put(rule.getAttributeName(), responseValues.get(rule.getAttributeName()));
                }
            }
            return metadataMap;
        });
    }

    @Test
    public void testWithDefaultResponseMetadataAddsDefaultsMissingFromStoredRules() {

        List<LLMProviderMetadata> stored = Arrays.asList(coreRule("promptTokenCount"), coreRule("totalTokenCount"));
        List<LLMProviderMetadata> defaults = Arrays.asList(tokenUsageRule("cacheReadInputTokens"),
                tokenUsageRule("reasoningTokens"));

        List<LLMProviderMetadata> rules = AIAPIMediator.withDefaultResponseMetadata(stored, defaults);

        Assert.assertEquals(Arrays.asList("promptTokenCount", "totalTokenCount", "cacheReadInputTokens",
                "reasoningTokens"), names(rules));
    }

    @Test
    public void testWithDefaultResponseMetadataKeepsStoredRuleOverDefault() {

        LLMProviderMetadata customised = new LLMProviderMetadata("cacheReadInputTokens",
                AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.custom_cache", false);
        LLMProviderMetadata blank = new LLMProviderMetadata("reasoningTokens", AIAPIConstants.INPUT_SOURCE_PAYLOAD,
                "", false);
        List<LLMProviderMetadata> defaults = Arrays.asList(tokenUsageRule("cacheReadInputTokens"),
                tokenUsageRule("reasoningTokens"));

        List<LLMProviderMetadata> rules = AIAPIMediator.withDefaultResponseMetadata(Arrays.asList(customised, blank),
                defaults);

        Assert.assertEquals(2, rules.size());
        Assert.assertSame(customised, rules.get(0));
        Assert.assertSame(blank, rules.get(1));
    }

    @Test
    public void testWithDefaultResponseMetadataDoesNotChangeItsInputs() {

        List<LLMProviderMetadata> stored = Collections.unmodifiableList(
                Collections.singletonList(coreRule("promptTokenCount")));
        List<LLMProviderMetadata> defaults = Collections.unmodifiableList(
                Collections.singletonList(tokenUsageRule("cacheReadInputTokens")));

        List<LLMProviderMetadata> rules = AIAPIMediator.withDefaultResponseMetadata(stored, defaults);

        Assert.assertEquals(Arrays.asList("promptTokenCount", "cacheReadInputTokens"), names(rules));
        Assert.assertEquals(1, stored.size());
        Assert.assertEquals(1, defaults.size());
    }

    @Test
    public void testWithDefaultResponseMetadataReturnsCopyWhenThereAreNoDefaults() {

        List<LLMProviderMetadata> stored = Collections.singletonList(coreRule("promptTokenCount"));

        for (List<LLMProviderMetadata> defaults : Arrays.<List<LLMProviderMetadata>>asList(null,
                Collections.emptyList())) {
            List<LLMProviderMetadata> rules = AIAPIMediator.withDefaultResponseMetadata(stored, defaults);

            Assert.assertEquals(stored, rules);
            Assert.assertNotSame(stored, rules);
        }
    }

    @Test
    public void testWithDefaultResponseMetadataWithoutStoredRules() {

        List<LLMProviderMetadata> rules = AIAPIMediator.withDefaultResponseMetadata(null,
                Collections.singletonList(tokenUsageRule("cacheReadInputTokens")));

        Assert.assertEquals(Collections.singletonList("cacheReadInputTokens"), names(rules));
    }

    @Test
    public void testWithDefaultResponseMetadataSkipsNullAndDuplicateDefaults() {

        List<LLMProviderMetadata> stored = new ArrayList<>(Arrays.asList(null, coreRule("promptTokenCount"),
                coreRule(null)));
        List<LLMProviderMetadata> defaults = Arrays.asList(null, tokenUsageRule(null),
                tokenUsageRule("cacheReadInputTokens"), tokenUsageRule("cacheReadInputTokens"));

        List<LLMProviderMetadata> rules = AIAPIMediator.withDefaultResponseMetadata(stored, defaults);

        // Stored entries are kept as they are; only the first default with a given name is added.
        Assert.assertEquals(4, rules.size());
        Assert.assertEquals("cacheReadInputTokens", rules.get(3).getAttributeName());
    }

    @Test
    public void testExtractTokenUsageCountersIncludesOnlyTokenUsageRules() {

        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("promptTokenCount", "1200");
        metadataMap.put("cacheReadInputTokens", "800");
        metadataMap.put("reasoningTokens", "120");
        List<LLMProviderMetadata> rules = Arrays.asList(coreRule("promptTokenCount"),
                tokenUsageRule("reasoningTokens"), tokenUsageRule("cacheReadInputTokens"));

        Map<String, Object> counters = AIAPIMediator.extractTokenUsageCounters(metadataMap, rules);

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("reasoningTokens", 120);
        expected.put("cacheReadInputTokens", 800);
        Assert.assertEquals(expected, counters);
        Assert.assertEquals(Arrays.asList("reasoningTokens", "cacheReadInputTokens"),
                new ArrayList<>(counters.keySet()));
    }

    @Test
    public void testExtractTokenUsageCountersKeepsZeroAndSkipsMissingCounters() {

        Map<String, String> metadataMap = Collections.singletonMap("cacheReadInputTokens", " 0 ");
        List<LLMProviderMetadata> rules = Arrays.asList(tokenUsageRule("cacheReadInputTokens"),
                tokenUsageRule("cacheWriteInputTokens"));

        Map<String, Object> counters = AIAPIMediator.extractTokenUsageCounters(metadataMap, rules);

        Assert.assertEquals(Collections.singletonMap("cacheReadInputTokens", 0), counters);
    }

    @Test
    public void testExtractTokenUsageCountersSkipsValuesThatAreNotWholeNumbers() {

        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("objectValue", "{\"ephemeral_5m_input_tokens\":10}");
        metadataMap.put("decimalValue", "800.0");
        metadataMap.put("textValue", "null");
        metadataMap.put("tooLarge", "99999999999");
        metadataMap.put("emptyValue", "");
        metadataMap.put("cacheReadInputTokens", "800");
        List<LLMProviderMetadata> rules = new ArrayList<>();
        for (String name : metadataMap.keySet()) {
            rules.add(tokenUsageRule(name));
        }

        Map<String, Object> counters = AIAPIMediator.extractTokenUsageCounters(metadataMap, rules);

        Assert.assertEquals(Collections.singletonMap("cacheReadInputTokens", 800), counters);
    }

    @Test
    public void testExtractTokenUsageCountersWithMissingInputs() {

        Map<String, String> metadataMap = Collections.singletonMap("cacheReadInputTokens", "800");

        Assert.assertTrue(AIAPIMediator.extractTokenUsageCounters(null,
                Collections.singletonList(tokenUsageRule("cacheReadInputTokens"))).isEmpty());
        Assert.assertTrue(AIAPIMediator.extractTokenUsageCounters(metadataMap, null).isEmpty());
        Assert.assertTrue(AIAPIMediator.extractTokenUsageCounters(metadataMap,
                Arrays.asList(null, tokenUsageRule(null))).isEmpty());
    }

    @Test
    public void testProcessOutboundResponsePublishesTokenUsageCountersWhenAnalyticsEnabled() throws Exception {

        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(true);
        responseValues.put("promptTokenCount", "1200");
        responseValues.put("cacheReadInputTokens", "800");
        responseValues.put("reasoningTokens", "120");

        Map<String, String> metadataMap = processOutboundResponse(coreRule("promptTokenCount"));

        Assert.assertEquals(Arrays.asList("promptTokenCount", "cacheReadInputTokens", "reasoningTokens"),
                names(extractionRules));
        Assert.assertSame(metadataMap, properties.get(AIAPIConstants.AI_API_RESPONSE_METADATA));
        Map<String, Object> counters = new LinkedHashMap<>();
        counters.put("cacheReadInputTokens", 800);
        counters.put("reasoningTokens", 120);
        Assert.assertEquals(Collections.singletonMap(Constants.AI_TOKEN_USAGE, counters),
                properties.get(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS));
    }

    @Test
    public void testProcessOutboundResponseUsesOnlyStoredRulesWhenAnalyticsDisabled() throws Exception {

        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(false);
        responseValues.put("promptTokenCount", "1200");
        responseValues.put("cacheReadInputTokens", "800");

        Map<String, String> metadataMap = processOutboundResponse(coreRule("promptTokenCount"));

        Assert.assertEquals(Collections.singletonList("promptTokenCount"), names(extractionRules));
        Assert.assertEquals(Collections.singletonMap("promptTokenCount", "1200"), metadataMap);
        Assert.assertFalse(properties.containsKey(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS));
        Mockito.verify(llmProviderService, Mockito.never()).getDefaultResponseMetadata();
    }

    @Test
    public void testProcessOutboundResponseKeepsStoredRuleOverDefault() throws Exception {

        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(true);
        responseValues.put("cacheReadInputTokens", "800");
        responseValues.put("reasoningTokens", "120");
        // A stored rule without the TOKEN_USAGE category, which the default with the same name must not replace.
        LLMProviderMetadata storedReasoningRule = new LLMProviderMetadata("reasoningTokens",
                AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.custom_reasoning", false);

        processOutboundResponse(storedReasoningRule);

        Assert.assertEquals(Arrays.asList("reasoningTokens", "cacheReadInputTokens"), names(extractionRules));
        Assert.assertSame(storedReasoningRule, extractionRules.get(0));
        Assert.assertEquals(Collections.singletonMap(Constants.AI_TOKEN_USAGE,
                        Collections.singletonMap("cacheReadInputTokens", 800)),
                properties.get(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS));
    }

    @Test
    public void testProcessOutboundResponseReplacesCountersOfEarlierAttempt() throws Exception {

        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(true);
        Map<String, Object> earlierCounters = new LinkedHashMap<>();
        earlierCounters.put("cacheReadInputTokens", 5);
        earlierCounters.put("reasoningTokens", 9);
        Map<String, Object> otherSection = Collections.singletonMap("requests", 1);
        Map<String, Object> sections = new LinkedHashMap<>();
        sections.put("otherFeatureUsage", otherSection);
        sections.put(Constants.AI_TOKEN_USAGE, earlierCounters);
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS, sections);
        responseValues.put("cacheReadInputTokens", "800");

        processOutboundResponse(coreRule("promptTokenCount"));

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("otherFeatureUsage", otherSection);
        expected.put(Constants.AI_TOKEN_USAGE, Collections.singletonMap("cacheReadInputTokens", 800));
        Assert.assertEquals(expected, properties.get(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS));
    }

    @Test
    public void testProcessOutboundResponseWithoutCountersClearsCountersOfEarlierAttempt() throws Exception {

        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(true);
        properties.put(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS, Collections.singletonMap(
                Constants.AI_TOKEN_USAGE, Collections.singletonMap("cacheReadInputTokens", 800)));
        responseValues.put("promptTokenCount", "1200");

        processOutboundResponse(coreRule("promptTokenCount"));

        Assert.assertEquals(Collections.singletonMap(Constants.AI_TOKEN_USAGE, Collections.emptyMap()),
                properties.get(APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS));
    }

    @Test
    public void testProcessOutboundResponseDoesNotTouchListenerAnalyticsProps() throws Exception {

        PowerMockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(true);
        Map<String, Object> listenerProps = new LinkedHashMap<>();
        listenerProps.put("salesRegion", "EMEA");
        properties.put(APIMgtGatewayConstants.ADDITIONAL_ANALYTICS_PROPS, listenerProps);
        responseValues.put("cacheReadInputTokens", "800");

        processOutboundResponse(coreRule("promptTokenCount"));

        Assert.assertSame(listenerProps, properties.get(APIMgtGatewayConstants.ADDITIONAL_ANALYTICS_PROPS));
        Assert.assertEquals(Collections.singletonMap("salesRegion", "EMEA"), listenerProps);
    }

    /**
     * Runs the mediator's outbound response processing for a provider with the given stored rules and no failover or
     * round robin configuration.
     */
    private Map<String, String> processOutboundResponse(LLMProviderMetadata... storedRules) throws Exception {

        LLMProviderConfiguration configuration = new LLMProviderConfiguration();
        configuration.setConnectorType("openAi_2.0.0");
        configuration.setMetadata(Arrays.asList(storedRules));
        LLMProviderInfo provider = new LLMProviderInfo();
        provider.setName("OpenAI");
        provider.setApiVersion("2.0.0");
        provider.setConfigurations(configuration);
        Map<String, String> metadataMap = new HashMap<>();

        Whitebox.invokeMethod(new AIAPIMediator(), "processOutboundResponse", messageContext, configuration,
                llmProviderService, metadataMap, provider);
        return metadataMap;
    }

    private static LLMProviderMetadata coreRule(String name) {

        return new LLMProviderMetadata(name, AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage." + name, true);
    }

    private static LLMProviderMetadata tokenUsageRule(String name) {

        return new LLMProviderMetadata(name, AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage." + name, false,
                AIAPIConstants.ANALYTICS_CATEGORY_TOKEN_USAGE);
    }

    private static List<String> names(List<LLMProviderMetadata> rules) {

        List<String> names = new ArrayList<>();
        for (LLMProviderMetadata rule : rules) {
            names.add(rule.getAttributeName());
        }
        return names;
    }
}
