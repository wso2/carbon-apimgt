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

//package org.apache.synapse.mediators.bsf;
//
//import junit.framework.TestCase;
//import org.apache.synapse.mediators.TestUtils;
//
//public class GroovyScriptMediatorTest extends TestCase {
//
//    public void testXMLMediator2() throws Exception {
//       ScriptMediator mediator = new ScriptMediator("groovy", "mc.setPayloadXML(mc.getPayloadXML())",null);
//       mediator.initInlineScript();
//       assertTrue(mediator.mediate(TestUtils.getTestContext("<a><b>petra</b></a>")));
//    }
//
//// TODO: doesn't work yet
////  public void testXMLMediator3() throws Exception {
////  String script =
////      "import groovy.xml.StreamingMarkupBuilder\n" +
////      "def xml = mc.getPayloadXML()\n" +
////      "def builder = new StreamingMarkupBuilder()\n" +
////      "def copier = builder.bind{ mkp.yield(xml) }\n" +
////      "mc.setPayloadXML(\"$copier\")\n";
////
////  InlineScriptMediator mediator = new InlineScriptMediator("xml.groovy", script);
////  mediator.init();
////  assertTrue(mediator.mediate(TestUtils.getTestContext("<a><b>petra</b></a>")));
////}
//}
