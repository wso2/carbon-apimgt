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

package org.apache.synapse.config.xml;

/**
 * Factory and Serializer tests for the CacheMediator
 */
public class CacheMediatorSerializationTest extends AbstractTestCase {

    private CacheMediatorFactory cacheMediatorFactory;
    private CacheMediatorSerializer cacheMediatorSerializer;

    public CacheMediatorSerializationTest() {
        super(CacheMediatorSerializationTest.class.getName());
        cacheMediatorFactory = new CacheMediatorFactory();
        cacheMediatorSerializer = new CacheMediatorSerializer();
    }

    public void testCacheMediatorSerializationScenarioOne() {
        String inputXml = "<cache xmlns=\"http://ws.apache.org/ns/synapse\" " +
                          "id=\"string\" hashGenerator=\"org.wso2.caching.digest.DOMHASHGenerator\" " +
                          "timeout=\"10\" scope=\"per-host\" collector=\"false\" " +
                          "maxMessageSize=\"10000\"><onCacheHit><send/></onCacheHit><implementation " +
                          "type=\"memory\" maxSize=\"10\"/></cache>";
        assertTrue(serialization(inputXml, cacheMediatorFactory, cacheMediatorSerializer));
        assertTrue(serialization(inputXml, cacheMediatorSerializer));
    }

    public void testCacheMediatorSerializationScenarioTwo() {
        String inputXml = "<cache xmlns=\"http://ws.apache.org/ns/synapse\" " +
                          "id=\"string\" hashGenerator=\"org.wso2.caching.digest.DOMHASHGenerator\" " +
                          "timeout=\"10\" scope=\"per-mediator\" collector=\"false\" " +
                          "maxMessageSize=\"10000\"><onCacheHit sequence=\"seq\"></onCacheHit>" +
                          "<implementation type=\"memory\" maxSize=\"10\"/></cache>";
        assertTrue(serialization(inputXml, cacheMediatorFactory, cacheMediatorSerializer));
        assertTrue(serialization(inputXml, cacheMediatorSerializer));
    }

    public void testCacheMediatorSerializationResponseCache() {
        String inputXml = "<cache xmlns=\"http://ws.apache.org/ns/synapse\" " +
                          "id=\"string\" scope=\"per-host\" collector=\"true\" />";
        assertTrue(serialization(inputXml, cacheMediatorFactory, cacheMediatorSerializer));
        assertTrue(serialization(inputXml, cacheMediatorSerializer));
    }
}
