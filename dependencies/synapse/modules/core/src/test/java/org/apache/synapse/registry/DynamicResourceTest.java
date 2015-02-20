/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.registry;

import junit.framework.TestCase;
import org.apache.synapse.MessageContext;
import org.apache.synapse.Mediator;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.axiom.om.OMNode;

import java.util.Map;
import java.util.HashMap;

public class DynamicResourceTest extends TestCase {

    private static final String DYNAMIC_ENDPOINT_1 =
            "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
            "    <address uri=\"http://test.url\"/>\n" +
            "</endpoint>";

    private static final String DYNAMIC_ENDPOINT_2 =
            "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
            "    <address uri=\"http://test2.url\"/>\n" +
            "</endpoint>";

    private static final String DYNAMIC_SEQUENCE_1 =
            "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"seq1\">\n" +
            "    <property name=\"foo\" value=\"bar\" />" +
            "</sequence>";

    private static final String DYNAMIC_SEQUENCE_2 =
            "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"seq1\">\n" +
            "    <property name=\"foo\" value=\"baz\" />" +
            "</sequence>";

    private static final String KEY_DYNAMIC_SEQUENCE_1 = "dynamic_sequence_1";
    private static final String KEY_DYNAMIC_ENDPOINT_1 = "dynamic_endpoint_1";


    private SimpleInMemoryRegistry registry;
    private SynapseConfiguration config;

    public void setUp() {
        System.out.println("Initializing in-memory registry for dynamic resource tests...");

        Map<String, OMNode> data = new HashMap<String, OMNode>();
        data.put(KEY_DYNAMIC_ENDPOINT_1, TestUtils.createOMElement(DYNAMIC_ENDPOINT_1));
        data.put(KEY_DYNAMIC_SEQUENCE_1, TestUtils.createOMElement(DYNAMIC_SEQUENCE_1));

        registry = new SimpleInMemoryRegistry(data, 8000L);
        config = new SynapseConfiguration();
        config.setRegistry(registry);
    }

    public void testDynamicSequenceLookup() throws Exception {
        System.out.println("Testing dynamic sequence lookup...");

        // Phase 1
        System.out.println("Testing basic registry lookup functionality...");
        MessageContext synCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>", config);
        Mediator seq1 = synCtx.getSequence(KEY_DYNAMIC_SEQUENCE_1);
        assertNotNull(seq1);
        assertTrue(((SequenceMediator) seq1).isInitialized());
        assertEquals(1, registry.getHitCount());
        seq1.mediate(synCtx);
        assertEquals("bar", synCtx.getProperty("foo"));

        // Phase 2
        System.out.println("Testing basic sequence caching...");
        synCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>", config);
        Mediator seq2 = synCtx.getSequence(KEY_DYNAMIC_SEQUENCE_1);
        assertNotNull(seq2);
        assertTrue(((SequenceMediator) seq2).isInitialized());
        assertEquals(1, registry.getHitCount());
        seq2.mediate(synCtx);
        assertEquals("bar", synCtx.getProperty("foo"));
        assertTrue(seq1 == seq2);

        // Phase 3
        System.out.println("Testing advanced sequence caching...");
        synCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>", config);
        System.out.println("Waiting for the cache to expire...");
        Thread.sleep(8500L);
        Mediator seq3 = synCtx.getSequence(KEY_DYNAMIC_SEQUENCE_1);
        assertNotNull(seq3);
        assertTrue(((SequenceMediator) seq3).isInitialized());
        assertEquals(1, registry.getHitCount());
        seq3.mediate(synCtx);
        assertEquals("bar", synCtx.getProperty("foo"));
        assertTrue(seq1 == seq3);

        // Phase 4
        System.out.println("Testing sequence reloading...");
        registry.updateResource(KEY_DYNAMIC_SEQUENCE_1, TestUtils.createOMElement(DYNAMIC_SEQUENCE_2));
        System.out.println("Waiting for the cache to expire...");
        Thread.sleep(8500L);
        synCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>", config);
        Mediator seq4 = synCtx.getSequence(KEY_DYNAMIC_SEQUENCE_1);
        assertNotNull(seq4);
        assertTrue(((SequenceMediator) seq4).isInitialized());
        assertEquals(2, registry.getHitCount());
        seq4.mediate(synCtx);
        assertEquals("baz", synCtx.getProperty("foo"));
        assertTrue(seq1 != seq4);
        assertTrue(!((SequenceMediator) seq1).isInitialized());

        // Phase 5
        System.out.println("Testing for non-existing sequences...");
        synCtx = TestUtils.createSynapseMessageContext("<empty/>", config);
        Mediator seq5 = synCtx.getSequence("non-existing-sequence");
        assertNull(seq5);

        System.out.println("Dynamic sequence lookup tests were successful...");
    }

    public void testDynamicEndpointLookup() throws Exception {
        System.out.println("Testing dynamic endpoint lookup...");

        // Phase 1
        System.out.println("Testing basic registry lookup functionality...");
        MessageContext synCtx = TestUtils.createSynapseMessageContext("<empty/>", config);
        Endpoint ep1 = synCtx.getEndpoint(KEY_DYNAMIC_ENDPOINT_1);
        assertNotNull(ep1);
        assertTrue(ep1.isInitialized());
        assertEquals(1, registry.getHitCount());
        assertEquals("http://test.url", ((AddressEndpoint) ep1).getDefinition().getAddress());

        // Phase 2
        System.out.println("Testing basic endpoint caching...");
        synCtx = TestUtils.createSynapseMessageContext("<empty/>", config);
        Endpoint ep2 = synCtx.getEndpoint(KEY_DYNAMIC_ENDPOINT_1);
        assertNotNull(ep2);
        assertEquals(1, registry.getHitCount());
        assertTrue(ep1 == ep2);

        // Phase 3
        System.out.println("Testing advanced endpoint caching...");
        synCtx = TestUtils.createSynapseMessageContext("<empty/>", config);
        System.out.println("Waiting for the cache to expire...");
        Thread.sleep(8500L);
        Endpoint ep3 = synCtx.getEndpoint(KEY_DYNAMIC_ENDPOINT_1);
        assertNotNull(ep3);
        assertEquals(1, registry.getHitCount());
        assertTrue(ep1 == ep3);

        // Phase 4
        System.out.println("Testing endpoint reloading...");
        registry.updateResource(KEY_DYNAMIC_ENDPOINT_1, TestUtils.createOMElement(DYNAMIC_ENDPOINT_2));
        System.out.println("Waiting for the cache to expire...");
        Thread.sleep(8500L);
        synCtx = TestUtils.createSynapseMessageContext("<empty/>", config);
        Endpoint ep4 = synCtx.getEndpoint(KEY_DYNAMIC_ENDPOINT_1);
        assertNotNull(ep4);
        assertTrue(ep4.isInitialized());
        assertEquals(2, registry.getHitCount());
        assertEquals("http://test2.url", ((AddressEndpoint) ep4).getDefinition().getAddress());
        assertTrue(ep1 != ep4);
        assertTrue(!ep1.isInitialized());

        // Phase 5
        System.out.println("Testing for non-existing endpoints...");
        synCtx = TestUtils.createSynapseMessageContext("<empty/>", config);
        Endpoint ep5 = synCtx.getEndpoint("non-existing-endpoint");
        assertNull(ep5);

        System.out.println("Dynamic endpoint lookup tests were successful...");
    }
}
