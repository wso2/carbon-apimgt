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
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIDefinitionProcessor;
import org.wso2.carbon.apimgt.api.model.API;

/**
 * Unit tests for {@link APIDefinitionProcessorFactory}.
 */
public class APIDefinitionProcessorFactoryTest {

    // ==================== getDefinitionProcessor(API) ====================

    @Test
    public void testGetDefinitionProcessorForHTTPApi() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("HTTP");
        Mockito.when(api.isAsync()).thenReturn(false);

        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor(api);
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof OASDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorForGraphQLApi() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("GRAPHQL");
        Mockito.when(api.isAsync()).thenReturn(false);

        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor(api);
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof OASDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorForWSApi() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("WS");
        Mockito.when(api.isAsync()).thenReturn(true);

        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor(api);
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorForWebSubApi() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("WEBSUB");
        Mockito.when(api.isAsync()).thenReturn(true);

        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor(api);
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorForSSEApi() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("SSE");
        Mockito.when(api.isAsync()).thenReturn(true);

        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor(api);
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorForAsyncApi() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("ASYNC");
        Mockito.when(api.isAsync()).thenReturn(true);

        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor(api);
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorForWebhookApi() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("WEBHOOK");
        Mockito.when(api.isAsync()).thenReturn(false);

        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor(api);
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorForWebhookCaseInsensitive() {
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn("webhook");
        Mockito.when(api.isAsync()).thenReturn(false);

        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor(api);
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorForNullApi() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor((API) null);
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof OASDefinitionProcessor);
    }

    // ==================== getDefinitionProcessor(String) ====================

    @Test
    public void testGetDefinitionProcessorByTypeHTTP() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor("HTTP");
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof OASDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorByTypeWS() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor("WS");
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorByTypeWebSub() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor("WEBSUB");
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorByTypeSSE() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor("SSE");
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorByTypeAsync() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor("ASYNC");
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorByTypeWebhook() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor("WEBHOOK");
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorByTypeCaseInsensitive() {
        Assert.assertTrue(
                APIDefinitionProcessorFactory.getDefinitionProcessor("ws")
                        instanceof AsyncAPIDefinitionProcessor);
        Assert.assertTrue(
                APIDefinitionProcessorFactory.getDefinitionProcessor("Websub")
                        instanceof AsyncAPIDefinitionProcessor);
        Assert.assertTrue(
                APIDefinitionProcessorFactory.getDefinitionProcessor("sse")
                        instanceof AsyncAPIDefinitionProcessor);
        Assert.assertTrue(
                APIDefinitionProcessorFactory.getDefinitionProcessor("async")
                        instanceof AsyncAPIDefinitionProcessor);
        Assert.assertTrue(
                APIDefinitionProcessorFactory.getDefinitionProcessor("Webhook")
                        instanceof AsyncAPIDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorByTypeGraphQL() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor("GRAPHQL");
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof OASDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorByTypeNull() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor((String) null);
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof OASDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorByTypeEmpty() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor("");
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof OASDefinitionProcessor);
    }

    @Test
    public void testGetDefinitionProcessorByTypeUnknown() {
        APIDefinitionProcessor processor = APIDefinitionProcessorFactory.getDefinitionProcessor("UNKNOWN");
        Assert.assertNotNull(processor);
        Assert.assertTrue(processor instanceof OASDefinitionProcessor);
    }

    // ==================== Singleton behavior ====================

    @Test
    public void testSameProcessorInstanceReturned() {
        APIDefinitionProcessor proc1 = APIDefinitionProcessorFactory.getDefinitionProcessor("HTTP");
        APIDefinitionProcessor proc2 = APIDefinitionProcessorFactory.getDefinitionProcessor("GRAPHQL");
        Assert.assertSame("OAS processor should be singleton", proc1, proc2);

        APIDefinitionProcessor proc3 = APIDefinitionProcessorFactory.getDefinitionProcessor("WS");
        APIDefinitionProcessor proc4 = APIDefinitionProcessorFactory.getDefinitionProcessor("SSE");
        Assert.assertSame("Async processor should be singleton", proc3, proc4);
    }
}
