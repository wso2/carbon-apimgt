/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.definitions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.List;

/**
 * Unit tests for {@link AsyncAPIDefinitionProcessor}.
 */
public class AsyncAPIDefinitionProcessorTest {

    private AsyncAPIDefinitionProcessor processor;

    @Before
    public void init() {
        processor = new AsyncAPIDefinitionProcessor();
    }

    @Test
    public void testGetType() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("WS");
        Assert.assertEquals("WS", processor.getType(api));
    }

    @Test
    public void testGetTypeSSE() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("SSE");
        Assert.assertEquals("SSE", processor.getType(api));
    }

    @Test
    public void testGetTypeWebSub() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("WEBSUB");
        Assert.assertEquals("WEBSUB", processor.getType(api));
    }

    @Test
    public void testIsAsyncReturnsTrue() {
        API api = Mockito.mock(API.class);
        Assert.assertTrue(processor.isAsync(api));
    }

    @Test
    public void testGetDefinitionFromAPI() {
        API api = Mockito.mock(API.class);
        String asyncDef = "{\"asyncapi\": \"2.0.0\"}";
        Mockito.when(api.getAsyncApiDefinition()).thenReturn(asyncDef);
        Assert.assertEquals(asyncDef, processor.getDefinitionFromAPI(api));
    }

    @Test
    public void testGetDefinitionFromAPIReturnsNull() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getAsyncApiDefinition()).thenReturn(null);
        Assert.assertNull(processor.getDefinitionFromAPI(api));
    }

    @Test
    public void testSetDefinitionToAPI() {
        API api = Mockito.mock(API.class);
        String definition = "{\"asyncapi\": \"2.0.0\"}";
        processor.setDefinitionToAPI(api, definition);
        Mockito.verify(api).setAsyncApiDefinition(definition);
    }

    @Test
    public void testExtractEndpointUrlJsonWithServers() {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"servers\": {\n" +
                "    \"production\": {\n" +
                "      \"url\": \"wss://api.example.com\",\n" +
                "      \"protocol\": \"wss\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String url = processor.extractEndpointUrl(definition);
        Assert.assertEquals("wss://api.example.com", url);
    }

    @Test
    public void testExtractEndpointUrlWithProtocolPrefix() {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"servers\": {\n" +
                "    \"production\": {\n" +
                "      \"url\": \"api.example.com\",\n" +
                "      \"protocol\": \"wss\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String url = processor.extractEndpointUrl(definition);
        Assert.assertEquals("wss://api.example.com", url);
    }

    @Test
    public void testExtractEndpointUrlWithDoubleSlashPrefix() {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"servers\": {\n" +
                "    \"production\": {\n" +
                "      \"url\": \"//api.example.com\",\n" +
                "      \"protocol\": \"wss\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String url = processor.extractEndpointUrl(definition);
        Assert.assertEquals("wss://api.example.com", url);
    }

    @Test
    public void testExtractEndpointUrlWithoutProtocol() {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"servers\": {\n" +
                "    \"production\": {\n" +
                "      \"url\": \"https://api.example.com\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String url = processor.extractEndpointUrl(definition);
        Assert.assertEquals("https://api.example.com", url);
    }

    @Test
    public void testExtractEndpointUrlNoServers() {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"info\": { \"title\": \"Test\", \"version\": \"1.0\" }\n" +
                "}";
        String url = processor.extractEndpointUrl(definition);
        Assert.assertNull(url);
    }

    @Test
    public void testExtractEndpointUrlEmptyServers() {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"servers\": {}\n" +
                "}";
        String url = processor.extractEndpointUrl(definition);
        Assert.assertNull(url);
    }

    @Test
    public void testExtractEndpointUrlNullDefinition() {
        Assert.assertNull(processor.extractEndpointUrl(null));
    }

    @Test
    public void testExtractEndpointUrlEmptyDefinition() {
        Assert.assertNull(processor.extractEndpointUrl(""));
    }

    @Test
    public void testExtractEndpointUrlWhitespaceDefinition() {
        Assert.assertNull(processor.extractEndpointUrl("   "));
    }

    @Test
    public void testExtractEndpointUrlInvalidJson() {
        String url = processor.extractEndpointUrl("not valid json or yaml");
        Assert.assertNull(url);
    }

    @Test
    public void testExtractEndpointUrlYamlDefinition() {
        String definition =
                "asyncapi: '2.0.0'\n" +
                "servers:\n" +
                "  production:\n" +
                "    url: wss://api.example.com\n" +
                "    protocol: wss\n";
        String url = processor.extractEndpointUrl(definition);
        Assert.assertEquals("wss://api.example.com", url);
    }

    @Test
    public void testExtractEndpointUrlServerWithoutUrl() {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"servers\": {\n" +
                "    \"production\": {\n" +
                "      \"protocol\": \"wss\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String url = processor.extractEndpointUrl(definition);
        Assert.assertNull(url);
    }

    @Test
    public void testExtractEndpointUrlUrlAlreadyHasScheme() {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"servers\": {\n" +
                "    \"production\": {\n" +
                "      \"url\": \"wss://api.example.com\",\n" +
                "      \"protocol\": \"wss\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String url = processor.extractEndpointUrl(definition);
        Assert.assertEquals("wss://api.example.com", url);
    }

    @Test
    public void testExtractEndpointUrlEmptyProtocol() {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"servers\": {\n" +
                "    \"production\": {\n" +
                "      \"url\": \"api.example.com\",\n" +
                "      \"protocol\": \"\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String url = processor.extractEndpointUrl(definition);
        Assert.assertEquals("api.example.com", url);
    }

    @Test
    public void testExtractOperationsWithValidAsyncAPIDefinition() throws APIManagementException {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"info\": { \"title\": \"Test\", \"version\": \"1.0\" },\n" +
                "  \"channels\": {\n" +
                "    \"/events\": {\n" +
                "      \"subscribe\": {\n" +
                "        \"message\": {\n" +
                "          \"payload\": { \"type\": \"string\" }\n" +
                "        }\n" +
                "      },\n" +
                "      \"publish\": {\n" +
                "        \"message\": {\n" +
                "          \"payload\": { \"type\": \"string\" }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        List<URITemplate> result = processor.extractOperations(definition);
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testExtractOperationsWithMultipleChannels() throws APIManagementException {
        String definition = "{\n" +
                "  \"asyncapi\": \"2.0.0\",\n" +
                "  \"info\": { \"title\": \"Test\", \"version\": \"1.0\" },\n" +
                "  \"channels\": {\n" +
                "    \"/events\": {\n" +
                "      \"subscribe\": {\n" +
                "        \"message\": { \"payload\": { \"type\": \"string\" } }\n" +
                "      }\n" +
                "    },\n" +
                "    \"/notifications\": {\n" +
                "      \"subscribe\": {\n" +
                "        \"message\": { \"payload\": { \"type\": \"string\" } }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        List<URITemplate> result = processor.extractOperations(definition);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() >= 2);
    }

    @Test(expected = APIManagementException.class)
    public void testExtractOperationsNullDefinitionThrows() throws APIManagementException {
        processor.extractOperations(null);
    }

    @Test(expected = APIManagementException.class)
    public void testExtractOperationsEmptyDefinitionThrows() throws APIManagementException {
        processor.extractOperations("");
    }

    @Test(expected = APIManagementException.class)
    public void testExtractOperationsInvalidDefinitionThrows() throws APIManagementException {
        processor.extractOperations("not valid json");
    }

    @Test
    public void testGetDefinitionFileName() {
        Assert.assertEquals("Definitions/asyncapi.yaml", processor.getDefinitionFileName());
    }
}
