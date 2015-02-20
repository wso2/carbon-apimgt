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
 * 
 *
 */

public class ClassMediatorSerializationTest extends AbstractTestCase {

    ClassMediatorFactory classMediatorFactory;
    ClassMediatorSerializer classMediatorSerializer;

    public ClassMediatorSerializationTest() {
        super(ClassMediatorSerializationTest.class.getName());
        classMediatorFactory = new ClassMediatorFactory();
        classMediatorSerializer = new ClassMediatorSerializer();
    }

    public void testClassMediatorSerializationScenarioOne() throws Exception {
        String inputXml = "<class xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.config.xml.TestMediator\"></class> ";
        assertTrue(serialization(inputXml, classMediatorFactory, classMediatorSerializer));
        assertTrue(serialization(inputXml, classMediatorSerializer));
    }

    public void testClassMediatorSerializationWithProperty() throws Exception {
        String inputXml = "<class xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.config.xml.TestMediator\"><property name=\"testProp\" " +
                "value=\"This is a test\"/></class> ";
        assertTrue(serialization(inputXml, classMediatorFactory, classMediatorSerializer));
        assertTrue(serialization(inputXml, classMediatorSerializer));
    }

    public void testClassMediatorSerializationWithInlineProperty() throws Exception {
        String inputXml = "<class xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "name=\"org.apache.synapse.config.xml.TestMediator\">" +
                "<property name=\"testElemProp\"><test/></property></class> ";
        assertTrue(serialization(inputXml, classMediatorFactory, classMediatorSerializer));
        assertTrue(serialization(inputXml, classMediatorSerializer));
    }
    
}
