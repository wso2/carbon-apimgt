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

public class URLRewriteMediatorSerializationTest extends AbstractTestCase {

    private URLRewriteMediatorFactory urlRewriteMediatorFactory = null;
    private URLRewriteMediatorSerializer urlRewriteMediatorSerializer = null;

    public URLRewriteMediatorSerializationTest() {
        urlRewriteMediatorFactory = new URLRewriteMediatorFactory();
        urlRewriteMediatorSerializer = new URLRewriteMediatorSerializer();
    }

    public void testSerializationScenario1() {
        String xml = "<rewrite xmlns=\"http://ws.apache.org/ns/synapse\"><rewriterule>" +
                "<action fragment=\"protocol\" value=\"https\" type=\"set\"/><action " +
                "fragment=\"host\" value=\"www.test.com\" type=\"set\"/><action " +
                "fragment=\"port\" value=\"9090\" type=\"set\"/><action fragment=\"path\" " +
                "value=\"/services\" type=\"prepend\"/></rewriterule></rewrite>";
        assertTrue(serialization(xml, urlRewriteMediatorFactory, urlRewriteMediatorSerializer));
    }

    public void testSerializationScenario2() {
        String xml = "<rewrite xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<rewriterule><condition><match type=\"url\" source=\"host\" regex=\"localhost\"/>" +
                "</condition><action fragment=\"protocol\" value=\"https\" type=\"set\"/>" +
                "<action fragment=\"host\" value=\"www.test.com\" type=\"set\"/><action " +
                "fragment=\"port\" value=\"9090\" type=\"set\"/><action fragment=\"path\" " +
                "value=\"/services\" type=\"prepend\"/></rewriterule>" +
                "</rewrite>";
        assertTrue(serialization(xml, urlRewriteMediatorFactory, urlRewriteMediatorSerializer));
    }

    public void testSerializationScenario3() {
        String xml = "<rewrite xmlns=\"http://ws.apache.org/ns/synapse\"><rewriterule>" +
                "<action fragment=\"full\" value=\"http://localhost:8080/synapse\" type=\"set\"/></rewriterule>" +
                "<rewriterule><condition><match type=\"url\" source=\"host\" regex=\"localhost\"/>" +
                "</condition><action fragment=\"protocol\" value=\"https\" type=\"set\"/>" +
                "<action fragment=\"host\" value=\"www.test.com\" type=\"set\"/>" +
                "<action fragment=\"port\" xpath=\"get-property('port')\" type=\"set\"/>" +
                "<action fragment=\"path\" value=\"/services\" type=\"prepend\"/></rewriterule><rewriterule>" +
                "<condition><and><match type=\"url\" source=\"host\" regex=\"www.test.com\"/>" +
                "<equal type=\"url\" source=\"port\" value=\"9090\"/></and></condition>" +
                "<action fragment=\"path\" regex=\".*/FooService\" type=\"replace\" " +
                "value=\"/BarService\"/></rewriterule>" +
                "</rewrite>";
        assertTrue(serialization(xml, urlRewriteMediatorFactory, urlRewriteMediatorSerializer));
    }

}
