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
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.io.ImportModes;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;

import javax.management.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.BindException;
import java.util.*;

/**
 * SNMP agent which is capable of listening for incoming SNMP GET/GETNEXT requests
 * and responding to them accordingly. This agent implementation exposed all the
 * standard Synapse MBeans over SNMP. The view exposed by the agent is read-only as of
 * now (this may be changed in a future version). The relevant OID mappings are
 * defined in the SynapseMIBUtils class. Each MBean attribute becomes a leaf in the MIB
 * exposed by the agent. For each MBean, attributes are arranged in the alphabetical
 * order for OID assignment. MBean APIs rarely change. Therefore this scheme will
 * guarantee a fairly consistent OID scheme.
 */
class SNMPAgent extends BaseAgent {

    private static final Log log = LogFactory.getLog(SNMPAgent.class);
    
    private static final String FULL_READ_VIEW = "fullReadView";
    private static final String GROUP_NAME = "synapseSNMPGroup";
    private static final String COMMUNITY_RECORD = "public2public";

    private Properties properties;

    private Set<OID> registeredOIDs = new HashSet<OID>();
    private int snmpVersion;

    public SNMPAgent(Properties properties) {
        super(new File(SNMPConstants.BC_FILE), new File(SNMPConstants.CONFIG_FILE),
                new CommandProcessor(new OctetString(MPv3.createLocalEngineID())));
        this.properties = properties;
        
        String version = getProperty(SNMPConstants.SNMP_VERSION, SNMPConstants.SNMP_DEFAULT_VERSION);
        if (SNMPConstants.SNMP_VERSION_1.equals(version)) {
            this.snmpVersion = SnmpConstants.version1;
        } else if (SNMPConstants.SNMP_VERSION_2_C.equals(version)) {
            this.snmpVersion = SnmpConstants.version2c;
        } else {
            log.warn("Unsupported SNMP version: " + version + " - Using defaults");
            this.snmpVersion = SnmpConstants.version1;
        }
    }

    /**
     * Initialize and start this SNMP agent
     *
     * @throws IOException If an error occurs while initializing the agent
     */
    public void start() throws IOException {
        String context = getProperty(SNMPConstants.SNMP_CONTEXT_NAME,
                SNMPConstants.SNMP_DEFAULT_CONTEXT_NAME);
        try{
            init();
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof BindException) {
                log.info("SNMP agent is already running, not initializing again");
            } else {
                log.error("Unable to initialize SNMP agent", e);
            }
        }
        loadConfig(ImportModes.REPLACE_CREATE);
        addShutdownHook();
        getServer().addContext(new OctetString(context));
        finishInit();
        run();
        sendColdStartNotification();
    }

    @Override
    protected void initTransportMappings() throws IOException {
        String host = getProperty(SNMPConstants.SNMP_HOST, SNMPConstants.SNMP_DEFAULT_HOST);
        int port = Integer.parseInt(getProperty(SNMPConstants.SNMP_PORT,
                String.valueOf(SNMPConstants.SNMP_DEFAULT_PORT)));
        String address = host + "/" + port;
        Address adr = GenericAddress.parse(address);
        TransportMapping tm =
                TransportMappings.getInstance().createTransportMapping(adr);
        transportMappings = new TransportMapping[] { tm };
        log.info("SNMP transport adapter initialized on udp:" + address);
    }

    @Override
    protected void registerManagedObjects() {
        log.info("Initializing Synapse SNMP MIB");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectInstance> instances = mbs.queryMBeans(null, null);

        try {
            for (ObjectInstance instance : instances) {
                ObjectName objectName = instance.getObjectName();
                if (objectName.getDomain().equals("org.apache.synapse")) {
                    String oidString = SynapseMIBUtils.getOID(objectName);
                    if (oidString == null) {
                        continue;
                    }
                    
                    MBeanInfo info = mbs.getMBeanInfo(objectName);
                    MBeanAttributeInfo[] attributes = info.getAttributes();
                    List<String> attributeNames = new ArrayList<String>();
                    List<String> mapAttributes = new ArrayList<String>();
                    for (MBeanAttributeInfo attributeInfo : attributes) {
                        attributeNames.add(attributeInfo.getName());
                        if (Map.class.getName().equals(attributeInfo.getType())) {
                            mapAttributes.add(attributeInfo.getName());
                        }
                    }
                    Collections.sort(attributeNames);

                    doRegister(attributeNames, mapAttributes, oidString, objectName);
                }
            }
        } catch (Exception e) {
            log.error("Error while initializing the SNMP MIB", e);
        }
    }
    
    private void doRegister(List<String> attributeNames, List<String> mapAttributes, 
                            String oidString, ObjectName objectName) {
        
        for (int i = 0; i < attributeNames.size(); i++) {
            String attributeName = attributeNames.get(i);
            if (mapAttributes.contains(attributeName)) {
                continue;
            }
            OID oid = new OID(oidString + "." + (i + 1) + ".0");
            if (log.isDebugEnabled()) {
                log.debug("Registering " + objectName + "@" + attributeName +
                        " as OID: " + oid);
            }
            try {
                server.register(new SynapseMOScalar(
                        oid, objectName, attributeName, snmpVersion), null);
                registeredOIDs.add(oid);
            } catch (DuplicateRegistrationException e) {
                log.error("Error while registering the OID: " + oid + " for object: " +
                        objectName + " and attribute: " + attributeName, e);
            }
        }    
    }

    @Override
    protected void unregisterManagedObjects() {
        if (log.isDebugEnabled()) {
            log.debug("Cleaning up registered OIDs");
        }

        for (OID oid : registeredOIDs) {
            ManagedObject mo = server.getManagedObject(oid, null);
            if (mo != null) {
                server.unregister(mo, null);
            }
        }
        registeredOIDs.clear();
    }

    @Override
    protected void addUsmUser(USM usm) {

    }

    @Override
    protected void addNotificationTargets(SnmpTargetMIB snmpTargetMIB,
                                          SnmpNotificationMIB snmpNotificationMIB) {
        
    }

    @Override
    protected void addViews(VacmMIB vacm) {
        String communityString = getProperty(SNMPConstants.SNMP_COMMUNITY_NAME, 
                SNMPConstants.SNMP_DEFAULT_COMMUNITY_NAME);
        String securityName = getProperty(SNMPConstants.SNMP_SECURITY_NAME,
                SNMPConstants.SNMP_DEFAULT_SECURITY_NAME);

        int securityModel = SecurityModel.SECURITY_MODEL_SNMPv1;
        if (snmpVersion == SnmpConstants.version2c) {
            securityModel = SecurityModel.SECURITY_MODEL_SNMPv2c;
        }

        vacm.addGroup(securityModel,
                new OctetString(securityName),
                new OctetString(GROUP_NAME),
                StorageType.nonVolatile);

        vacm.addAccess(new OctetString(GROUP_NAME), new OctetString(communityString),
                securityModel,
                SecurityLevel.NOAUTH_NOPRIV,
                MutableVACM.VACM_MATCH_EXACT,
                new OctetString(FULL_READ_VIEW), // read permission granted
                new OctetString(),               // no write permissions
                new OctetString(),               // no notify permissions
                StorageType.nonVolatile);

        vacm.addViewTreeFamily(new OctetString(FULL_READ_VIEW),
                new OID(SNMPConstants.SYNAPSE_OID_BRANCH),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile);
    }

    @Override
    protected void addCommunities(SnmpCommunityMIB communityMIB) {
        String community = getProperty(SNMPConstants.SNMP_COMMUNITY_NAME, 
                SNMPConstants.SNMP_DEFAULT_COMMUNITY_NAME);
        String securityName = getProperty(SNMPConstants.SNMP_SECURITY_NAME,
                SNMPConstants.SNMP_DEFAULT_SECURITY_NAME);
        String context = getProperty(SNMPConstants.SNMP_CONTEXT_NAME,
                SNMPConstants.SNMP_DEFAULT_CONTEXT_NAME);

        if (log.isDebugEnabled()) {
            log.debug("Registering SNMP community string: " + community + " under the " +
                    "context: " + context);
        }

        Variable[] com2sec = new Variable[] {
                new OctetString(community),              // community name
                new OctetString(securityName),           // security name
                getAgent().getContextEngineID(),         // local engine ID
                new OctetString(context),                // default context name
                new OctetString(),                       // transport tag
                new Integer32(StorageType.nonVolatile),  // storage type
                new Integer32(RowStatus.active)          // row status
        };
        MOTableRow row =
                communityMIB.getSnmpCommunityEntry().createRow(
                        new OctetString(COMMUNITY_RECORD).toSubIndex(true), com2sec);
        communityMIB.getSnmpCommunityEntry().addRow(row);
    }
    
    private String getProperty(String name, String def) {
        String value = properties.getProperty(name);
        if (value == null) {
            value = def;
        }
        return value;
    }
}
