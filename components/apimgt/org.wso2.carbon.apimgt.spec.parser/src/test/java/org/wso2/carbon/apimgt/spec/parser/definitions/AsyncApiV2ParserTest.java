/*
 *   Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.spec.parser.definitions;

import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models.AsyncApiV2Parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;


/**
 * Basic unit tests for AsyncApiV2Parser
 */
public class AsyncApiV2ParserTest {

    @Test
    public void testValidateV3DefinitionWithURITemplates() throws Exception {
        AsyncApiV2Parser parser = new AsyncApiV2Parser();

        // Validate
        APIDefinitionValidationResponse resp = parser.validateAPIDefinition(AsyncApiTestUtils.ASYNCAPI_V2_SAMPLE,
                true);
        assertNotNull("Validation response should not be null", resp);
        assertTrue("V2 sample should be valid", resp.isValid());

        // Get URI templates
        Set<URITemplate> templates = parser.getURITemplates(AsyncApiTestUtils.ASYNCAPI_V2_SAMPLE, true);
        assertNotNull("getURITemplates result should not be null", templates);
        assertFalse("Expect at least one URI template for the V2 sample", templates.isEmpty());
        // check that 'orders' channel produced a template
        boolean foundOrders = templates.stream().anyMatch(t -> "orders".equals(t.getUriTemplate()));
        assertTrue("orders channel template should be present", foundOrders);
    }

    @Test
    public void testInvalidAPIDefinition() throws Exception {
        AsyncApiV2Parser parser = new AsyncApiV2Parser();
        String invalid = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"missing version\"}}";
        APIDefinitionValidationResponse resp = parser.validateAPIDefinition(invalid, true);
        assertNotNull(resp);
        assertFalse("Invalid v2 definition should be marked invalid", resp.isValid());
    }

    @Test
    public void testUpdateAsyncAPIDefinition() throws Exception {
        AsyncApiV2Parser parser = new AsyncApiV2Parser();

        API minimal = Mockito.mock(API.class);
        when(minimal.getScopes()).thenReturn(Collections.emptySet());
        when(minimal.getEndpointConfig()).thenReturn("");
        when(minimal.getTransports()).thenReturn("ws,wss");
        when(minimal.getId()).thenReturn(new APIIdentifier(
                "testProvider", "testApiV2", "1.0.0"));

        String oldDef = AsyncApiTestUtils.ASYNCAPI_V2_SAMPLE;
        String updated = parser.updateAsyncAPIDefinition(oldDef, minimal);
        assertNotNull("updateAsyncAPIDefinition should not return null", updated);
        assertTrue("Updated v2 definition should contain asyncapi property", updated.contains("\"asyncapi\""));
    }

    @Test
    public void testGenerateAsyncAPIDefinitionSuccess() throws Exception {
        AsyncApiV2Parser parser = new AsyncApiV2Parser();

        URITemplate template = new URITemplate();
        template.setUriTemplate("orders");
        Set<URITemplate> uriTemplates = new HashSet<>();
        uriTemplates.add(template);

        API apiSuccess = Mockito.mock(API.class);
        when(apiSuccess.getUriTemplates()).thenReturn(uriTemplates);
        when(apiSuccess.getEndpointConfig()).thenReturn("{\"production_endpoints\":{\"url\":\"wss://broker.example.com/topic/test\"}}");
        when(apiSuccess.getTransports()).thenReturn("wss");
        when(apiSuccess.getType()).thenReturn(APISpecParserConstants.API_TYPE_WS);
        when(apiSuccess.getScopes()).thenReturn(Collections.emptySet());
        when(apiSuccess.getId()).thenReturn(new APIIdentifier("prov", "v2gen", "1.0.0"));

        String generated = parser.generateAsyncAPIDefinition(apiSuccess);
        assertNotNull("generateAsyncAPIDefinition should return a string", generated);
        assertTrue("Generated definition should contain asyncapi", generated.contains("asyncapi"));
    }

    @Test
    public void testGenerateAsyncAPIDefinitionFailure() {
        AsyncApiV2Parser parser = new AsyncApiV2Parser();

        URITemplate template = new URITemplate();
        template.setUriTemplate("orders");
        Set<URITemplate> uriTemplates = new HashSet<>();
        uriTemplates.add(template);

        API apiFail = Mockito.mock(API.class);
        when(apiFail.getUriTemplates()).thenReturn(uriTemplates);
        when(apiFail.getEndpointConfig()).thenReturn("not-a-json");
        when(apiFail.getTransports()).thenReturn("wss");
        when(apiFail.getType()).thenReturn(APISpecParserConstants.API_TYPE_WS);
        when(apiFail.getScopes()).thenReturn(Collections.emptySet());
        when(apiFail.getId()).thenReturn(new APIIdentifier("prov", "v2genfail", "1.0.0"));

        try {
            parser.generateAsyncAPIDefinition(apiFail);
            fail("Expected an exception for malformed endpointConfig");
        } catch (Exception ex) {
            assertNotNull(ex.getMessage());
        }
    }

}