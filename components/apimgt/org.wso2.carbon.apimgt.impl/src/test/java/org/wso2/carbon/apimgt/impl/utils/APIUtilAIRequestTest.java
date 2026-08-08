/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.utils;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.AIRequestContext;
import org.wso2.carbon.context.CarbonContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tests the two AI request helpers added to {@link APIUtil}: {@code buildAIRequestContext}, which describes an outbound
 * AI service request to the configured property enricher, and {@code addAdditionalPropertiesToPayload}, which merges the
 * properties the enricher returned into the payload.
 * <p>
 * The merge is purely additive and best effort: it must never overwrite an attribute the product placed in the payload,
 * and must never fail a request over unusable input.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonContext.class})
// The carbon context resolves the carbon home from the environment while initializing, which is not available to a unit
// test. Only its thread local accessor is needed here, so its static initializer is suppressed, as the other tests in
// this module do for PrivilegedCarbonContext.
@SuppressStaticInitializationFor("org.wso2.carbon.context.CarbonContext")
public class APIUtilAIRequestTest {

    private static final String ORGANIZATION = "acme.com";
    private static final String RESOURCE = "/chat";
    private static final String REQUEST_ID = "corr-42";
    private static final String USERNAME = "john@acme.com";

    private CarbonContext carbonContext;

    @Before
    public void setUp() {

        carbonContext = Mockito.mock(CarbonContext.class);
        PowerMockito.mockStatic(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
    }

    // buildAIRequestContext

    @Test
    public void testBuildContextCarriesOrganizationResourceAndRequestId() {

        Mockito.when(carbonContext.getUsername()).thenReturn(USERNAME);

        AIRequestContext context = APIUtil.buildAIRequestContext(ORGANIZATION, RESOURCE, REQUEST_ID);

        Assert.assertEquals("Organization mismatch", ORGANIZATION, context.getOrganization());
        Assert.assertEquals("Resource mismatch", RESOURCE, context.getResource());
        Assert.assertEquals("Request id mismatch", REQUEST_ID, context.getRequestId());
    }

    @Test
    public void testBuildContextResolvesUsernameFromCarbonContext() {

        Mockito.when(carbonContext.getUsername()).thenReturn(USERNAME);

        Assert.assertEquals("The invoking user must be resolved from the carbon context", USERNAME,
                APIUtil.buildAIRequestContext(ORGANIZATION, RESOURCE, REQUEST_ID).getUsername());
    }

    /**
     * The asynchronous API publish path has no end user, so the carbon context carries no user name and the context
     * must report null rather than an empty string.
     */
    @Test
    public void testBuildContextLeavesUsernameNullWhenCarbonContextHasNone() {

        Mockito.when(carbonContext.getUsername()).thenReturn("");

        Assert.assertNull("An absent user name must be reported as null",
                APIUtil.buildAIRequestContext(ORGANIZATION, RESOURCE, null).getUsername());

        Mockito.when(carbonContext.getUsername()).thenReturn(null);

        Assert.assertNull("An absent user name must be reported as null",
                APIUtil.buildAIRequestContext(ORGANIZATION, RESOURCE, null).getUsername());
    }

    @Test
    public void testBuildContextAcceptsAnAbsentRequestId() {

        Mockito.when(carbonContext.getUsername()).thenReturn(USERNAME);

        Assert.assertNull("An operation that does not correlate its requests must leave the request id null",
                APIUtil.buildAIRequestContext(ORGANIZATION, RESOURCE, null).getRequestId());
    }

    // addAdditionalPropertiesToPayload

    @Test
    public void testNullPropertiesLeaveThePayloadUnchanged() {

        String payload = "{\"query\":\"q\"}";

        Assert.assertEquals("A null property map must leave the payload unchanged", payload,
                APIUtil.addAdditionalPropertiesToPayload(payload, null));
    }

    @Test
    public void testEmptyPropertiesLeaveThePayloadUnchanged() {

        String payload = "{\"query\":\"q\"}";

        Assert.assertEquals("An empty property map must leave the payload unchanged", payload,
                APIUtil.addAdditionalPropertiesToPayload(payload, Collections.<String, Object>emptyMap()));
    }

    @Test
    public void testBlankPayloadIsReturnedAsItIs() {

        Map<String, Object> properties = Collections.<String, Object>singletonMap("username", USERNAME);

        Assert.assertNull("A null payload must be returned as it is",
                APIUtil.addAdditionalPropertiesToPayload(null, properties));
        Assert.assertEquals("A blank payload must be returned as it is", "   ",
                APIUtil.addAdditionalPropertiesToPayload("   ", properties));
    }

    /**
     * A payload that is not a JSON object cannot be merged into. That degrades to the original payload rather than
     * failing the request.
     */
    @Test
    public void testNonJsonPayloadIsReturnedUnchanged() {

        String payload = "not json at all";

        Assert.assertEquals("A non-JSON payload must be returned unchanged", payload,
                APIUtil.addAdditionalPropertiesToPayload(payload,
                        Collections.<String, Object>singletonMap("username", USERNAME)));
    }

    @Test
    public void testPropertiesAreAddedAlongsideTheExistingAttributes() {

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("username", USERNAME);
        properties.put("count", 7);
        properties.put("enabled", Boolean.TRUE);

        JSONObject enriched = new JSONObject(
                APIUtil.addAdditionalPropertiesToPayload("{\"query\":\"q\"}", properties));

        Assert.assertEquals("The existing attribute must be preserved", "q", enriched.getString("query"));
        Assert.assertEquals("String property mismatch", USERNAME, enriched.getString("username"));
        Assert.assertEquals("Numeric property mismatch", 7, enriched.getInt("count"));
        Assert.assertTrue("Boolean property mismatch", enriched.getBoolean("enabled"));
    }

    /**
     * The merge is additive only. An attribute the product already placed in the payload wins, so an enricher cannot
     * change what a feature sends.
     */
    @Test
    public void testExistingAttributeIsNeverOverridden() {

        JSONObject enriched = new JSONObject(APIUtil.addAdditionalPropertiesToPayload(
                "{\"query\":\"q\",\"username\":\"product\"}",
                Collections.<String, Object>singletonMap("username", "enricher")));

        Assert.assertEquals("The product value must be retained", "product", enriched.getString("username"));
    }

    /**
     * Keys are canonicalised before the collision check, so a key returned with surrounding whitespace resolves to the
     * attribute the payload already carries instead of becoming a second, near identical attribute.
     */
    @Test
    public void testPaddedKeyCollidesWithTheExistingAttribute() {

        String enriched = APIUtil.addAdditionalPropertiesToPayload("{\"query\":\"q\",\"username\":\"product\"}",
                Collections.<String, Object>singletonMap("  username  ", "enricher"));

        Assert.assertFalse("A padded key must not be added as a distinct attribute", enriched.contains("  username"));
        Assert.assertEquals("The product value must be retained", "product",
                new JSONObject(enriched).getString("username"));
        Assert.assertEquals("Only the existing attributes must remain", 2, new JSONObject(enriched).length());
    }

    @Test
    public void testPaddedKeyIsAddedInItsCanonicalForm() {

        JSONObject enriched = new JSONObject(APIUtil.addAdditionalPropertiesToPayload("{\"query\":\"q\"}",
                Collections.<String, Object>singletonMap("  tenant\t", ORGANIZATION)));

        Assert.assertEquals("A padded key must be added trimmed", ORGANIZATION, enriched.getString("tenant"));
        Assert.assertEquals("The padded form must not also be present", 2, enriched.length());
    }

    @Test
    public void testKeysDifferingOnlyByPaddingCollapseToOneAttribute() {

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("role", "first");
        properties.put(" role ", "second");

        JSONObject enriched = new JSONObject(
                APIUtil.addAdditionalPropertiesToPayload("{\"query\":\"q\"}", properties));

        Assert.assertEquals("The first value must be kept", "first", enriched.getString("role"));
        Assert.assertEquals("The padded duplicate must not be added separately", 2, enriched.length());
    }

    @Test
    public void testBlankAndNullKeysAreSkipped() {

        String payload = "{\"query\":\"q\"}";
        Map<String, Object> properties = new HashMap<>();
        properties.put("   ", "whitespace");
        properties.put("", "empty");
        properties.put(null, "null");

        Assert.assertEquals("Unusable keys must leave the payload unchanged", payload,
                APIUtil.addAdditionalPropertiesToPayload(payload, properties));
    }

    /**
     * When nothing could be added the original payload string is returned, rather than a re-serialised equivalent whose
     * attribute order may differ.
     */
    @Test
    public void testPayloadIsNotReserialisedWhenNothingIsAdded() {

        String payload = "{\"query\":\"q\",\"username\":\"product\"}";

        Assert.assertSame("The original payload instance must be returned when nothing is added", payload,
                APIUtil.addAdditionalPropertiesToPayload(payload,
                        Collections.<String, Object>singletonMap("username", "enricher")));
    }

    @Test
    public void testNestedAndCollectionValuesAreSupported() {

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("department", "platform");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("attributes", nested);
        properties.put("roles", Collections.singletonList("admin"));

        JSONObject enriched = new JSONObject(
                APIUtil.addAdditionalPropertiesToPayload("{\"query\":\"q\"}", properties));

        Assert.assertEquals("A nested map must be serialised as a JSON object", "platform",
                enriched.getJSONObject("attributes").getString("department"));
        Assert.assertEquals("A list must be serialised as a JSON array", "admin",
                enriched.getJSONArray("roles").getString(0));
    }
}
