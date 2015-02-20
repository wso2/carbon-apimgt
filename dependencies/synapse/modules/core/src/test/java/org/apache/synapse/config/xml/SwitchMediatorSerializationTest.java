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

import org.apache.axiom.om.impl.exception.XMLComparisonException;

public class SwitchMediatorSerializationTest extends AbstractTestCase {

    private SwitchMediatorFactory switchMediatorFactory = null;
    private SwitchMediatorSerializer switchMediatorSerializer = null;

    public SwitchMediatorSerializationTest() {
        switchMediatorFactory = new SwitchMediatorFactory();
        switchMediatorSerializer = new SwitchMediatorSerializer();
    }

    public void testSwitchMediatorSerializationScenarioOne() {

        String switchConfiguration = "<syn:switch xmlns:syn=\"http://ws.apache.org/ns/synapse\" source=\"synapse:get-property('to')\">" +
                "<syn:case regex=\"MyService1\"><syn:drop/></syn:case>" +
                "<syn:case regex=\"MyService2\"><syn:drop/></syn:case>" +
                "<syn:default><syn:drop/></syn:default>" +
                "</syn:switch>";

        try {
            assertTrue(serialization(switchConfiguration, switchMediatorFactory, switchMediatorSerializer));
        } catch (Exception e) {
            fail("Exception in test");
        }
    }

    public void testSwitchMediatorSerializationScenarioTwo() {

        String switchConfiguration = "<syn:switch xmlns:syn=\"http://ws.apache.org/ns/synapse\" source=\"synapse:get-property('to')\">" +
                "<syn:case regex=\"MyService1\"><syn:drop/></syn:case>" +
                "<syn:case regex=\"MyService2\"><syn:drop/></syn:case>" +
                "</syn:switch>";

        try {
            assertTrue(serialization(switchConfiguration, switchMediatorFactory, switchMediatorSerializer));
        } catch (Exception e) {
            fail("Exception in test");
        }
    }
}
