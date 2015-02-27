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
package org.apache.synapse;

import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.SynapseCallbackReceiver;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates server context information
 */
public class ServerContextInformation {
    
    /* Underlying server's context - EX : Axis2 ConfigurationContext */
    private Object serverContext;
    /* A map to hold any context information*/
    private final Map<String, Object> properties = new HashMap<String, Object>();
    /* Keeps the SynapseConfiguration */
    private SynapseConfiguration synapseConfiguration;
    /* Keeps the SynapseEnvironment */
    private SynapseEnvironment synapseEnvironment;
    /** Callback receiver */    
    private SynapseCallbackReceiver synapseCallbackReceiver;
    /** State of the server */
    private ServerState serverState = ServerState.UNDETERMINED;
    /** Reference to the server configuration */
    private ServerConfigurationInformation serverConfigurationInformation;

    public ServerContextInformation(ServerConfigurationInformation serverConfigurationInformation) {
        this.serverConfigurationInformation = serverConfigurationInformation;
    }

    public ServerContextInformation(Object serverContext,
                                    ServerConfigurationInformation serverConfigurationInformation) {
        this.serverContext = serverContext;
        this.serverConfigurationInformation = serverConfigurationInformation;
    }

    public Object getServerContext() {
        return serverContext;
    }

    public void setServerContext(Object serverContext) {
        this.serverContext = serverContext;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public ServerState getServerState() {
        return serverState;
    }

    public void setServerState(ServerState serverState) {
        this.serverState = serverState;
    }

    public SynapseConfiguration getSynapseConfiguration() {
        return synapseConfiguration;
    }

    public void setSynapseConfiguration(SynapseConfiguration synapseConfiguration) {
        this.synapseConfiguration = synapseConfiguration;
    }

    public SynapseEnvironment getSynapseEnvironment() {
        return synapseEnvironment;
    }

    public void setSynapseEnvironment(SynapseEnvironment synapseEnvironment) {
        this.synapseEnvironment = synapseEnvironment;
    }

    public SynapseCallbackReceiver getSynapseCallbackReceiver() {
        return synapseCallbackReceiver;
    }

    public void setSynapseCallbackReceiver(SynapseCallbackReceiver synapseCallbackReceiver) {
        this.synapseCallbackReceiver = synapseCallbackReceiver;
    }

    public ServerConfigurationInformation getServerConfigurationInformation() {
        return serverConfigurationInformation;
    }

    /**
     * Returns the number of current callbacks.
     *
     * @return the number of current callbacks.
     */
    public int getCallbackCount() {
        if (synapseCallbackReceiver != null) {
            return synapseCallbackReceiver.getCallbackCount();
        }
        return 0;
    }
}
