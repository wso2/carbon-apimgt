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

import java.io.IOException;
import java.util.Properties;

/**
 * This is the public API for initializing and stopping the Synapse SNMP agent.
 */
public class SynapseSNMPAgent {

    /*
     * The purpose of this class is to conceal the API of the SNMPAgent class which
     * is an extension of the SNMP4J BaseAgent class. The BaseAgent exposes a whole
     * bunch of methods which can be used to alter the behavior of the SNMP agent.
     * We certainly don't want other modules messing around with these methods. So
     * we have given package access to the SNMPAgent class and wrapped it up using
     * the SynapseSNMPAgent which has a much cleaner and simple API.
     */

    private SNMPAgent agent;
    
    public SynapseSNMPAgent(Properties properties) {
        this.agent = new SNMPAgent(properties);
    }

    /**
     * Start the SNMP agent for Synapse. This will initialize the SNMP transport bindings,
     * initialize the MIB and start the SNMP agent to accept incoming requests.
     *
     * @throws IOException If an error occurs while starting the SNMP agent
     */
    public void start() throws IOException {
        agent.start();
    }

    /**
     * Stop and shutdown the Synapse SNMP agent.
     */
    public void stop() {
        agent.stop();
    }
}
