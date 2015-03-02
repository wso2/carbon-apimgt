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

package org.apache.synapse.util;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;

import java.util.Map;

public class SimpleMapTest extends TestCase {

    public void testConstructionFromXML() throws Exception {
        String xml = "<map xmlns=\"http://ws.apache.org/commons/ns/payload\">" +
                        "<entry name=\"key1\" type=\"int\">525</entry>" +
                        "<entry name=\"key2\" type=\"char\">d</entry>" +
                        "<entry name=\"key3\" type=\"double\">23.45</entry>" +
                        "<entry name=\"key4\" type=\"float\">-3.45</entry>" +
                        "<entry name=\"key5\" type=\"long\">1234567890</entry>" +
                        "<entry name=\"key6\" type=\"short\">123</entry>" +
                        "<entry name=\"key7\" type=\"byte\">a</entry>" +
                        "<entry name=\"key8\" type=\"string\">hello world</entry>" +
                     "</map>";
        OMElement mapElement = AXIOMUtil.stringToOM(xml);

        SimpleMap map = new SimpleMapImpl(mapElement);
        assertEquals(525, map.getInt("key1"));
        assertEquals('d', map.getChar("key2"));
        assertEquals(23.45D, map.getDouble("key3"));
        assertEquals(-3.45F, map.getFloat("key4"));
        assertEquals(1234567890L, map.getLong("key5"));
        assertEquals(123, map.getShort("key6"));
        assertEquals("a".getBytes()[0], map.getByte("key7"));
        assertEquals("hello world", map.getString("key8"));

        map.putString("key1", "test");
        assertEquals("test", map.getString("key1"));
    }

    public void testSerialization() {
        SimpleMapImpl map = new SimpleMapImpl();
        map.putInt("key1", 123);
        map.putDouble("key2", 23.45D);
        map.putString("key3", "hello");
        map.putByte("key4", "s".getBytes()[0]);

        OMElement mapElement = map.getOMElement();
        System.out.println(mapElement.toString());

        SimpleMap copy = new SimpleMapImpl(mapElement);
        assertEquals(map.size(), copy.size());
        for (Object entryObj : map.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            assertTrue(copy.containsKey(entry.getKey()) && entry.getValue().equals(
                    map.get(entry.getKey())));
        }
    }
}
