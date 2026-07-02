/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltInLLMProviderServiceTest {

    private final BuiltInLLMProviderService service = new BuiltInLLMProviderService() {
        @Override
        public String getType() {
            return "TestProvider";
        }

        @Override
        public LLMProvider getLLMProvider() {
            return new LLMProvider("TestProvider", "1.0.0");
        }
    };

    @Test
    public void testGetResponseMetadataWithValidJsonPath() throws APIManagementException {

        String payload = "{\"usage\":{\"inputTokens\":100,\"outputTokens\":50,\"totalTokens\":150}}";
        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(payload, null, null, null);

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("promptTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.inputTokens", true));
        metadataList.add(new LLMProviderMetadata("completionTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.outputTokens", true));
        metadataList.add(new LLMProviderMetadata("totalTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.totalTokens", true));

        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertEquals("100", result.get("promptTokenCount"));
        Assert.assertEquals("50", result.get("completionTokenCount"));
        Assert.assertEquals("150", result.get("totalTokenCount"));
    }

    @Test
    public void testGetResponseMetadataWithInvalidJsonPathDoesNotThrow() throws APIManagementException {

        String payload = "{\"usage\":{\"inputTokens\":100}}";
        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(payload, null, null, null);

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("promptTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "(?<=inputTokens.:.)\\d+", true));

        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.containsKey("promptTokenCount"));
    }

    @Test
    public void testGetResponseMetadataWithRegexAsJsonPathDoesNotThrow() throws APIManagementException {

        String payload = "{\"usage\":{\"inputTokens\":100}}";
        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(payload, null, null, null);

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("promptTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "[0-9]+", true));

        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertNotNull(result);
    }

    @Test
    public void testGetResponseMetadataWithMissingPayloadFieldDoesNotThrow() throws APIManagementException {

        String payload = "{\"data\":\"value\"}";
        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(payload, null, null, null);

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("promptTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.inputTokens", true));

        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.containsKey("promptTokenCount"));
    }

    @Test
    public void testGetResponseMetadataWithNullPayload() throws APIManagementException {

        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(null, null, null, null);

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("promptTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.inputTokens", true));

        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetResponseMetadataWithNullMetadataList() throws APIManagementException {

        LLMResponseMetaData responseMetadata = new LLMResponseMetaData("{}", null, null, null);
        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, null, metadataMap);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetResponseMetadataWithEmptyMetadataList() throws APIManagementException {

        LLMResponseMetaData responseMetadata = new LLMResponseMetaData("{}", null, null, null);
        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, new ArrayList<>(), metadataMap);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetResponseMetadataWithValidPathRegex() throws APIManagementException {

        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(
                null, null, null, "model/claude-3-sonnet/invoke");

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("model",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PATH, "model/([^/]+)", true));

        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertNotNull(result);
        Assert.assertEquals("model/claude-3-sonnet", result.get("model"));
    }

    @Test
    public void testGetResponseMetadataWithNonMatchingPathRegex() throws APIManagementException {

        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(
                null, null, null, "/v1/invoke");

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("model",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PATH, "model/([^/]+)", true));

        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.containsKey("model"));
    }

    @Test
    public void testGetResponseMetadataWithMalformedJsonPayload() throws APIManagementException {

        String payload = "not-valid-json{{{";
        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(payload, null, null, null);

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("promptTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.inputTokens", true));

        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertNotNull(result);
    }

    @Test
    public void testGetResponseMetadataWithMixedPayloadAndPathSources() throws APIManagementException {

        String payload = "{\"usage\":{\"inputTokens\":100,\"outputTokens\":50}}";
        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(
                payload, null, null, "model/claude-3-sonnet/invoke");

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("promptTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.inputTokens", true));
        metadataList.add(new LLMProviderMetadata("model",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PATH, "model/([^/]+)", true));

        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertEquals("100", result.get("promptTokenCount"));
        Assert.assertEquals("model/claude-3-sonnet", result.get("model"));
    }

    @Test
    public void testGetResponseMetadataWithInvalidRegexAsPathDoesNotThrow() throws APIManagementException {

        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(
                null, null, null, "model/claude-3-sonnet");

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("model",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PATH, "[invalid(regex", true));

        Map<String, String> metadataMap = new HashMap<>();
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertNotNull(result);
    }

    @Test
    public void testGetResponseMetadataPreservesExistingMapEntries() throws APIManagementException {

        String payload = "{\"usage\":{\"inputTokens\":100}}";
        LLMResponseMetaData responseMetadata = new LLMResponseMetaData(payload, null, null, null);

        List<LLMProviderMetadata> metadataList = new ArrayList<>();
        metadataList.add(new LLMProviderMetadata("promptTokenCount",
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD, "$.usage.inputTokens", true));

        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("existingKey", "existingValue");
        Map<String, String> result = service.getResponseMetadata(responseMetadata, metadataList, metadataMap);

        Assert.assertEquals("existingValue", result.get("existingKey"));
        Assert.assertEquals("100", result.get("promptTokenCount"));
    }
}
