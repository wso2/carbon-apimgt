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

package org.apache.synapse.commons.snmp;

import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for generating Synapse OIDs and MIB entries
 */
public class SynapseMIBUtils {

    private static final String PROPERTY_CONNECTOR_NAME = "ConnectorName";
    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_TYPE = "Type";

    /**
     * Contains MBean name to OID mappings. MBeans which are not included
     * in this map are not exposed over SNMP.
     */
    private static final Map<String,Integer> type2oid = new HashMap<String, Integer>();

    /**
     * A basic symbol table to ensure consistent OID assignment for MBean
     * attributes.
     */
    private static final Map<String,Integer> name2oid = new HashMap<String, Integer>();

    static {
        type2oid.put("ServerManager", 1);
        type2oid.put("Transport", 2);
        type2oid.put("NhttpConnections", 3);
        type2oid.put("NHTTPLatencyView", 4);
        type2oid.put("NHTTPS2SLatencyView", 5);

        name2oid.put("nio-http-listener", 1);
        name2oid.put("nio-http-sender", 2);
        name2oid.put("nio-https-listener", 3);
        name2oid.put("nio-https-sender", 4);
        name2oid.put("jms-listener", 5);
        name2oid.put("jms-sender", 6);
        name2oid.put("vfs-listener", 7);
        name2oid.put("vfs-sender", 8);
        name2oid.put("mailto-listener", 9);
        name2oid.put("mailto-sender", 10);
        name2oid.put("http-listener", 11);
        name2oid.put("http-sender", 12);
        name2oid.put("https-listener", 13);
        name2oid.put("https-sender", 14);
        name2oid.put("nio-http", 15);
        name2oid.put("nio-https", 16);
        name2oid.put("passthru-http-sender",17);
        name2oid.put("passthru-https-sender",18);
        name2oid.put("passthru-http-receiver",19);
        name2oid.put("passthru-https-receiver",20);
    }

    public synchronized static String getOID(ObjectName objectName) {
        String type = objectName.getKeyProperty(PROPERTY_TYPE);
        Integer typeOID = type2oid.get(type);
        if (typeOID == null) {
            return null;
        }

        //Seems this code segment is no longer required
        //https://wso2.org/jira/browse/ESBJAVA-1932
//        String name;
//        if ("Transport".equals(type)) {
//            // ditch the time stamp suffix at the end of the connector name
//            String connector = objectName.getKeyProperty(PROPERTY_CONNECTOR_NAME);
//            name = connector.substring(0, connector.lastIndexOf('-'));
//        } else {
//            name = objectName.getKeyProperty(PROPERTY_NAME);
//        }

        String name = objectName.getKeyProperty(PROPERTY_NAME);

        if (name != null) {
            Integer nameOID = name2oid.get(name);
            if (nameOID == null) {
                nameOID = new Integer(name2oid.size() + 1);
                name2oid.put(name, nameOID);
            }
            return SNMPConstants.SYNAPSE_OID_BRANCH + "." + typeOID.intValue() +
                    "." + nameOID.intValue();
        } else {
            return SNMPConstants.SYNAPSE_OID_BRANCH + "." + typeOID.intValue();
        }
    }

}
