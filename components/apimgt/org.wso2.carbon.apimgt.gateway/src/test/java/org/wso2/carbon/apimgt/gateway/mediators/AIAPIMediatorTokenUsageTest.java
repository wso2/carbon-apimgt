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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants;
import org.wso2.carbon.apimgt.api.LLMProviderMetadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link AIAPIMediator#extractTokenUsageCounters(Map, List)}.
 */
public class AIAPIMediatorTokenUsageTest {

    private static LLMProviderMetadata tokenUsageRule(String name) {

        return new LLMProviderMetadata(name, AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage." + name, false,
                AIAPIConstants.ANALYTICS_CATEGORY_TOKEN_USAGE);
    }

    private static LLMProviderMetadata plainRule(String name) {

        return new LLMProviderMetadata(name, AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage." + name, true);
    }

    /**
     * Only TOKEN_USAGE rules are published, as Integers; core and other rules are not.
     */
    @Test
    public void testPublishesOnlyTokenUsageCounters() {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("promptTokenCount", "1200");
        metadata.put("cacheReadInputTokens", "800");
        metadata.put("reasoningTokens", "120");
        List<LLMProviderMetadata> rules = Arrays.asList(plainRule("promptTokenCount"),
                tokenUsageRule("cacheReadInputTokens"), tokenUsageRule("reasoningTokens"));

        Map<String, Object> counters = AIAPIMediator.extractTokenUsageCounters(metadata, rules);

        Assert.assertEquals(2, counters.size());
        Assert.assertEquals(800, counters.get("cacheReadInputTokens"));
        Assert.assertEquals(120, counters.get("reasoningTokens"));
    }

    /**
     * A reported zero is published; a counter that was not reported is left out.
     */
    @Test
    public void testPublishesZeroAndOmitsMissingCounters() {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("cacheReadInputTokens", "0");
        List<LLMProviderMetadata> rules = Arrays.asList(tokenUsageRule("cacheReadInputTokens"),
                tokenUsageRule("cacheWriteInputTokens"));

        Map<String, Object> counters = AIAPIMediator.extractTokenUsageCounters(metadata, rules);

        Assert.assertEquals(0, counters.get("cacheReadInputTokens"));
        Assert.assertFalse(counters.containsKey("cacheWriteInputTokens"));
    }

    /**
     * Values that are not whole numbers are left out without affecting the other counters.
     */
    @Test
    public void testSkipsValuesThatAreNotWholeNumbers() {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("cacheReadInputTokens", "{\"ephemeral_5m_input_tokens\":10}");
        metadata.put("cacheWriteInputTokens", "800.0");
        metadata.put("reasoningTokens", " 120 ");
        List<LLMProviderMetadata> rules = Arrays.asList(tokenUsageRule("cacheReadInputTokens"),
                tokenUsageRule("cacheWriteInputTokens"), tokenUsageRule("reasoningTokens"));

        Map<String, Object> counters = AIAPIMediator.extractTokenUsageCounters(metadata, rules);

        Assert.assertEquals(1, counters.size());
        Assert.assertEquals(120, counters.get("reasoningTokens"));
    }

    /**
     * Null inputs produce no counters.
     */
    @Test
    public void testNullInputsReturnNoCounters() {

        Assert.assertTrue(AIAPIMediator.extractTokenUsageCounters(null, null).isEmpty());
        Assert.assertTrue(AIAPIMediator.extractTokenUsageCounters(new HashMap<>(),
                Arrays.asList(null, tokenUsageRule("cacheReadInputTokens"))).isEmpty());
    }
}
