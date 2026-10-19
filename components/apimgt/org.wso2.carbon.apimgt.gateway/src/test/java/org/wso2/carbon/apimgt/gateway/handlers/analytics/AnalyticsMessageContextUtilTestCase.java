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

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.synapse.MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tests for {@link AnalyticsMessageContextUtil#mergeSection(MessageContext, String, String, Map)}.
 */
public class AnalyticsMessageContextUtilTestCase {

    private static final String PROPERTY = APIMgtGatewayConstants.PRODUCT_ANALYTICS_PROPS;
    private static final String SECTION = Constants.AI_TOKEN_USAGE;
    // A section contributed by another product feature.
    private static final String OTHER_SECTION = "otherFeatureUsage";
    private static final Map<String, Object> OTHER_VALUE = Collections.singletonMap("requests", 1);

    private Map<String, Object> properties;
    private MessageContext messageContext;

    @Before
    public void setUp() {

        properties = new HashMap<>();
        messageContext = Mockito.mock(MessageContext.class);
        Mockito.when(messageContext.getProperty(Mockito.anyString()))
                .thenAnswer(invocation -> properties.get((String) invocation.getArguments()[0]));
        Mockito.doAnswer(invocation -> properties.put((String) invocation.getArguments()[0],
                invocation.getArguments()[1])).when(messageContext).setProperty(Mockito.anyString(), Mockito.any());
    }

    /**
     * Verifies the property is created holding only the section when nothing has been contributed yet.
     */
    @Test
    public void testMergeSectionCreatesPropertyWhenAbsent() {

        AnalyticsMessageContextUtil.mergeSection(messageContext, PROPERTY, SECTION, counters(800));

        Assert.assertEquals(Collections.singletonMap(SECTION, counters(800)), properties.get(PROPERTY));
    }

    /**
     * Verifies a section is added after the sections other features contributed, which are kept as they are.
     */
    @Test
    public void testMergeSectionKeepsOtherSections() {

        properties.put(PROPERTY, new LinkedHashMap<>(Collections.singletonMap(OTHER_SECTION, OTHER_VALUE)));

        AnalyticsMessageContextUtil.mergeSection(messageContext, PROPERTY, SECTION, counters(800));

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put(OTHER_SECTION, OTHER_VALUE);
        expected.put(SECTION, counters(800));
        Assert.assertEquals(expected, properties.get(PROPERTY));
    }

    /**
     * Verifies a later call (e.g. a retried backend attempt) replaces the whole earlier section rather than merging
     * into it, so values from an earlier attempt are never published.
     */
    @Test
    public void testMergeSectionReplacesWholeEarlierSection() {

        Map<String, Object> earlier = new LinkedHashMap<>();
        earlier.put("cacheReadInputTokens", 800);
        earlier.put("reasoningTokens", 120);
        Map<String, Object> sections = new LinkedHashMap<>();
        sections.put(OTHER_SECTION, OTHER_VALUE);
        sections.put(SECTION, earlier);
        properties.put(PROPERTY, sections);

        AnalyticsMessageContextUtil.mergeSection(messageContext, PROPERTY, SECTION, counters(50));

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put(OTHER_SECTION, OTHER_VALUE);
        expected.put(SECTION, counters(50));
        Assert.assertEquals(expected, properties.get(PROPERTY));
    }

    /**
     * Verifies an empty section replaces the earlier one, so an attempt that reports nothing clears stale values.
     */
    @Test
    public void testMergeSectionWithEmptySectionClearsEarlierValues() {

        properties.put(PROPERTY, new LinkedHashMap<>(Collections.singletonMap(SECTION, counters(800))));

        AnalyticsMessageContextUtil.mergeSection(messageContext, PROPERTY, SECTION, Collections.emptyMap());

        Assert.assertEquals(Collections.singletonMap(SECTION, Collections.emptyMap()), properties.get(PROPERTY));
    }

    /**
     * Verifies the existing map is copied, not changed in place: a cloned message context may share it.
     */
    @Test
    public void testMergeSectionDoesNotChangeExistingMap() {

        Map<String, Object> existing = new LinkedHashMap<>(Collections.singletonMap(OTHER_SECTION, OTHER_VALUE));
        properties.put(PROPERTY, existing);

        AnalyticsMessageContextUtil.mergeSection(messageContext, PROPERTY, SECTION, counters(800));

        Assert.assertEquals(Collections.singletonMap(OTHER_SECTION, OTHER_VALUE), existing);
        Assert.assertNotSame(existing, properties.get(PROPERTY));
    }

    /**
     * Verifies an unmodifiable existing map is handled, since the property is never changed in place.
     */
    @Test
    public void testMergeSectionWithUnmodifiableExistingMap() {

        properties.put(PROPERTY, Collections.unmodifiableMap(Collections.singletonMap(OTHER_SECTION, OTHER_VALUE)));

        AnalyticsMessageContextUtil.mergeSection(messageContext, PROPERTY, SECTION, counters(800));

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put(OTHER_SECTION, OTHER_VALUE);
        expected.put(SECTION, counters(800));
        Assert.assertEquals(expected, properties.get(PROPERTY));
    }

    /**
     * Verifies a value that is not a map (which only a misbehaving writer would set) is replaced by a map holding
     * the section, instead of causing a failure.
     */
    @Test
    public void testMergeSectionReplacesValueThatIsNotAMap() {

        properties.put(PROPERTY, "not-a-map");

        AnalyticsMessageContextUtil.mergeSection(messageContext, PROPERTY, SECTION, counters(800));

        Assert.assertEquals(Collections.singletonMap(SECTION, counters(800)), properties.get(PROPERTY));
    }

    /**
     * Verifies only the given property is written.
     */
    @Test
    public void testMergeSectionDoesNotTouchOtherProperties() {

        Map<String, Object> listenerProps = Collections.singletonMap("salesRegion", "EMEA");
        properties.put(APIMgtGatewayConstants.ADDITIONAL_ANALYTICS_PROPS, listenerProps);

        AnalyticsMessageContextUtil.mergeSection(messageContext, PROPERTY, SECTION, counters(800));

        Assert.assertSame(listenerProps, properties.get(APIMgtGatewayConstants.ADDITIONAL_ANALYTICS_PROPS));
        Assert.assertEquals(2, properties.size());
    }

    private static Map<String, Object> counters(int cacheReadInputTokens) {

        return Collections.singletonMap("cacheReadInputTokens", cacheReadInputTokens);
    }
}
