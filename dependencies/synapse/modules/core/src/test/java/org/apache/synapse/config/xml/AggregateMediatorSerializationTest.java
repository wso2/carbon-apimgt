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
 * Factory and Serializer tests for the AggregateMediator
 */

public class AggregateMediatorSerializationTest extends AbstractTestCase {
    private AggregateMediatorFactory aggregateMediatorFactory;
    private AggregateMediatorSerializer aggregateMediatorSerializer;

    public AggregateMediatorSerializationTest() {
        super(ClassMediatorSerializationTest.class.getName());
        aggregateMediatorFactory = new AggregateMediatorFactory();
        aggregateMediatorSerializer = new AggregateMediatorSerializer();
    }

    public void testAggregateMediatorSerialization() {
        String inputXml = "<aggregate xmlns=\"http://ws.apache.org/ns/synapse\">" +
                          "<correlateOn expression=\"get-property('To')\" /><completeCondition timeout=\"10\">" +
                          "<messageCount min=\"1\" max=\"10\" /></completeCondition><onComplete " +
                          "expression=\"get-property('To')\"><send /></onComplete></aggregate>";
        assertTrue(serialization(inputXml, aggregateMediatorFactory, aggregateMediatorSerializer));
        assertTrue(serialization(inputXml, aggregateMediatorSerializer));
    }
}