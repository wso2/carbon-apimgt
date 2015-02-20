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

public class SNMPConstants {

    /**
     * This OID branch has been uniquely assigned to the Synapse project by the ASF.
     * Please do not change.
     * 
     * @see <a href="https://cwiki.apache.org/confluence/display/DIRxPMGT/OID+Assignment+Scheme">ASF OID Assignments</a>
     */
    public static final String SYNAPSE_OID_BRANCH = "1.3.6.1.4.1.18060.14";
    
    public static final String SNMP_VERSION_1 = "snmpv1";
    public static final String SNMP_VERSION_2_C = "snmpv2c";
    
    // Configuration parameters
    public static final String SNMP_ENABLED = "synapse.snmp.enabled";
    public static final String SNMP_COMMUNITY_NAME = "synapse.snmp.community.name";
    public static final String SNMP_SECURITY_NAME = "synapse.snmp.security.name";
    public static final String SNMP_CONTEXT_NAME  = "synapse.snmp.context.name";
    public static final String SNMP_HOST = "synapse.snmp.host";
    public static final String SNMP_PORT = "synapse.snmp.port";
    public static final String SNMP_VERSION = "synapse.snmp.version";

    // Configuration defaults
    public static final String SNMP_DEFAULT_COMMUNITY_NAME = "public";
    public static final String SNMP_DEFAULT_SECURITY_NAME = "cpublic";
    public static final String SNMP_DEFAULT_CONTEXT_NAME = "public";
    public static final String SNMP_DEFAULT_HOST = "127.0.0.1";
    public static final int SNMP_DEFAULT_PORT = 9161;
    public static final String SNMP_DEFAULT_VERSION = SNMP_VERSION_1;

    public static final String BC_FILE = "./logs/snmp/boot-counter.cfg";
    public static final String CONFIG_FILE = "./logs/snmp/conf.cfg";

}
