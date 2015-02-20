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

public class DBReportMediatorSerializationTest extends AbstractTestCase {

    private DBReportMediatorFactory dbReportMediatorFactory;
    private DBReportMediatorSerializer dbReportMediatorSerializer;

    public DBReportMediatorSerializationTest() {
        super(DBReportMediatorSerializationTest.class.getName());
        dbReportMediatorFactory = new DBReportMediatorFactory();
        dbReportMediatorSerializer = new DBReportMediatorSerializer();
    }

    public void testDBReportMediatorSerializationScenarioWithConnectionPool() throws Exception {
        String inputXml = "<dbreport xmlns=\"http://ws.apache.org/ns/synapse\">" +
                          "<connection><pool><driver>org.apache.derby.jdbc.ClientDriver</driver>" +
                          "<url>jdbc:derby://localhost:1527/synapsedb;create=false</url>" +
                          "<password>synapse</password><user>synapse</user>" +
                          "<property name=\"autocommit\" value=\"true\"/></pool>" +
                          "</connection><statement><sql><![CDATA[update company set price=? " +
                          "where name =?]]></sql><parameter expression=\"//m0:return/m0:last/child::text()\" " +
                          "xmlns:m0=\"http://services.samples/xsd\" type=\"DOUBLE\"/><parameter " +
                          "expression=\"//m0:return/m0:symbol/child::text()\" xmlns:m0=\"http://services.samples/xsd\" " +
                          "type=\"VARCHAR\"/></statement></dbreport>";
        assertTrue(serialization(inputXml, dbReportMediatorFactory, dbReportMediatorSerializer));
        assertTrue(serialization(inputXml, dbReportMediatorSerializer));
    }

    public void testDBReportMediatorSerializationWithExternalDataSource() throws Exception {
        String inputXml = "<dbreport xmlns=\"http://ws.apache.org/ns/synapse\">" +
                          "<connection><pool><icClass>ClassName</icClass>" +
                          "<url>jdbc:derby://localhost:1527/synapsedb;create=false</url>" +
                          "<password>synapse</password><user>synapse</user>" +
                          "<dsName>DataServiceName</dsName></pool>" +
                          "</connection><statement><sql><![CDATA[update company set price=? " +
                          "where name =?]]></sql><parameter expression=\"//m0:return/m0:last/child::text()\" " +
                          "xmlns:m0=\"http://services.samples/xsd\" type=\"DOUBLE\"/><parameter " +
                          "expression=\"//m0:return/m0:symbol/child::text()\" " +
                          "xmlns:m0=\"http://services.samples/xsd\" type=\"VARCHAR\"/></statement></dbreport>";
        assertTrue(serialization(inputXml, dbReportMediatorFactory, dbReportMediatorSerializer));
        assertTrue(serialization(inputXml, dbReportMediatorSerializer));
    }

    public void testDBReportMediatorSerializationWithExternalDataSource2() throws Exception {
        String inputXml = "<dbreport xmlns=\"http://ws.apache.org/ns/synapse\">" +
                          "<connection><pool>" +
                          "<dsName>DataServiceName</dsName></pool>" +
                          "</connection><statement><sql><![CDATA[update company set price=? " +
                          "where name =?]]></sql><parameter expression=\"//m0:return/m0:last/child::text()\" " +
                          "xmlns:m0=\"http://services.samples/xsd\" type=\"DOUBLE\"/><parameter " +
                          "expression=\"//m0:return/m0:symbol/child::text()\" " +
                          "xmlns:m0=\"http://services.samples/xsd\" type=\"VARCHAR\"/></statement></dbreport>";
        assertTrue(serialization(inputXml, dbReportMediatorFactory, dbReportMediatorSerializer));
        assertTrue(serialization(inputXml, dbReportMediatorSerializer));
    }
}