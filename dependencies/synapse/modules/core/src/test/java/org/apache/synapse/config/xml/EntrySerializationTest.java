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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.Entry;

import java.util.Properties;

/**
 *
 */
public class EntrySerializationTest extends AbstractTestCase {

    public void testEntrySerializationScenarioOne() throws Exception {
        String inputXml = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" key=\"key\" " +
            "><description>description</description><foo/></localEntry>";
        OMElement inputOM = createOMElement(inputXml);
        Entry entry = EntryFactory.createEntry(inputOM.cloneOMElement(), new Properties());
        OMElement resultOM = EntrySerializer.serializeEntry(entry, null);
        assertTrue(compare(resultOM, inputOM));
    }

    public void testEntrySerializationScenarioTwo() throws Exception {
        String inputXml = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" key=\"key\"" +
            "><description>description</description></localEntry>";
        OMElement inputOM = createOMElement(inputXml);
        Entry entry = EntryFactory.createEntry(inputOM.cloneOMElement(), new Properties());
        OMElement resultOM = EntrySerializer.serializeEntry(entry, null);
        assertTrue(compare(resultOM, inputOM));
    }
    
    public void testEntrySerializationScenarioThree() throws Exception {
        String inputXml = "<localEntry xmlns=\"http://ws.apache.org/ns/synapse\" key=\"key\" " +
            "/>";
        OMElement inputOM = createOMElement(inputXml);
        Entry entry = EntryFactory.createEntry(inputOM.cloneOMElement(), new Properties());
        OMElement resultOM = EntrySerializer.serializeEntry(entry, null);
        assertTrue(compare(resultOM, inputOM));
    }
}
