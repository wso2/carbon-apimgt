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

public class DBLookupMediatorSerializationTest extends AbstractTestCase {

    private DBLookupMediatorFactory dbLookupFactory;
    private DBLookupMediatorSerializer dbLookupSerializer;

    public DBLookupMediatorSerializationTest() {
        super(DBLookupMediatorSerializationTest.class.getName());
        dbLookupFactory = new DBLookupMediatorFactory();
        dbLookupSerializer = new DBLookupMediatorSerializer();
    }

    public void testDBLookupMediatorSerializationScenarioOne() throws Exception {

        String inputXml = 
            "<syn:dblookup xmlns:syn=\"http://ws.apache.org/ns/synapse\">" +
                    "<syn:connection><syn:pool><syn:driver>com.some.driver.JDBCDriver</syn:driver>" +
                    "<syn:url>jdbc:/some/url</syn:url><syn:user>user</syn:user>" +
                    "<syn:password>pass</syn:password><syn:property name=\"name1\" value=\"value1\"/>" +
                    "</syn:pool></syn:connection><syn:statement><syn:sql>" +
                    "<![CDATA[insert into table values (?, ?, ..)]]></syn:sql>" +
                    "<syn:parameter value=\"ABC\" type=\"VARCHAR\"/>" +
                    "<syn:parameter expression=\"4\" type=\"INTEGER\"/>" +
                    "<syn:result name=\"2\" column=\"int\"/></syn:statement></syn:dblookup>";

        assertTrue(serialization(inputXml, dbLookupFactory, dbLookupSerializer));
        assertTrue(serialization(inputXml, dbLookupSerializer));
    }

    public void testDBLookupMediatorSerializationScenarioTwo() throws Exception {

        String inputXml =
            "<syn:dblookup xmlns:syn=\"http://ws.apache.org/ns/synapse\">" +
                    "<syn:connection><syn:pool><syn:dsName>lookupdb</syn:dsName>" +                    
                    "</syn:pool></syn:connection><syn:statement><syn:sql>" +
                    "<![CDATA[insert into table values (?, ?, ..)]]></syn:sql>" +
                    "<syn:parameter value=\"ABC\" type=\"VARCHAR\"/>" +
                    "<syn:parameter expression=\"4\" type=\"INTEGER\"/>" +
                    "<syn:result name=\"2\" column=\"int\"/></syn:statement></syn:dblookup>";

        assertTrue(serialization(inputXml, dbLookupFactory, dbLookupSerializer));
        assertTrue(serialization(inputXml, dbLookupSerializer));
    }

    public void testDBLookupMediatorSerializationScenarioThree() throws Exception {

        String inputXml =
            "<syn:dblookup xmlns:syn=\"http://ws.apache.org/ns/synapse\">" +
                    "<syn:connection><syn:pool><syn:dsName>lookupdb</syn:dsName>" +
                    "<syn:icClass>com.sun.jndi.rmi.registry.RegistryContextFactory</syn:icClass>" +
                    "<syn:url>rmi://localhost:2199</syn:url>" +
                    "<syn:user>user</syn:user>" +
                    "<syn:password>password</syn:password>" +
                    "</syn:pool></syn:connection><syn:statement><syn:sql>" +
                    "<![CDATA[insert into table values (?, ?, ..)]]></syn:sql>" +
                    "<syn:parameter value=\"ABC\" type=\"VARCHAR\"/>" +
                    "<syn:parameter expression=\"4\" type=\"INTEGER\"/>" +
                    "<syn:result name=\"2\" column=\"int\"/></syn:statement></syn:dblookup>";

        assertTrue(serialization(inputXml, dbLookupFactory, dbLookupSerializer));
        assertTrue(serialization(inputXml, dbLookupSerializer));
    }
}
