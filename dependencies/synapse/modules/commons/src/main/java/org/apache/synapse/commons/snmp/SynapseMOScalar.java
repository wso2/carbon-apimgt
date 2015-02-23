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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Date;

/**
 * Synapse managed object scalar implementation. This class queries built-in JMX MBeans
 * to retrieve the value of the specified OID. 
 */
public class SynapseMOScalar extends MOScalar<Variable> {
    
    private static final Log log = LogFactory.getLog(SynapseMOScalar.class);
    
    private ObjectName objectName;
    private String attribute;
    private int snmpVersion;

    public SynapseMOScalar(OID id, ObjectName objectName, String attribute, int snmpVersion) {
        super(id, MOAccessImpl.ACCESS_READ_ONLY, new OctetString());
        this.objectName = objectName;
        this.attribute = attribute;
        this.snmpVersion = snmpVersion;
    }

    @Override
    public Variable getValue() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            Object obj = mbs.getAttribute(objectName, attribute);
            if (obj instanceof Integer) {
                return new Integer32((Integer) obj);
            }
            
            if (snmpVersion > SnmpConstants.version1) {
                if (obj instanceof Long) {
                    return new Counter64(((Long) obj).longValue());
                } else if (obj instanceof Date) {
                    return new Counter64(((Date) obj).getTime());
                }
            }

            return new OctetString(obj.toString());
        } catch (Exception e) {
            log.error("Unexpected error while retrieving the value of OID: " + getID(), e);
            return null;
        }
    }
}
