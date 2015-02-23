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

package org.apache.synapse.endpoints;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.Entry;

import java.util.List;

/**
 * This class represents a real endpoint referred by a key. An Indirect endpoint does not really
 * have a life, but merely acts as a virtual endpoint for the actual endpoint refferred.
 */
public class IndirectEndpoint extends AbstractEndpoint {

    private String key = null;
    private Endpoint realEndpoint = null;

    /**
     * Send by calling to the real endpoint
     * @param synCtx the message to send
     */
    public void send(MessageContext synCtx) {

        reLoadAndInitEndpoint(((Axis2MessageContext) synCtx).
                getAxis2MessageContext().getConfigurationContext());

        if (realEndpoint != null) {
            realEndpoint.send(synCtx);
        } else {
            informFailure(synCtx, SynapseConstants.ENDPOINT_IN_DIRECT_NOT_READY,
                    "Couldn't find the endpoint with the key : " + key);
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    /**
     * Ready to send, if the real endpoint is ready
     */
    public boolean readyToSend() {
        try {
            return realEndpoint.readyToSend();
        } catch (NullPointerException e) {
            log.info("Could not find endpoint with key " + this.key + ". Could not verify Endpoint ready to send status.");
            return false;
        }
    }

    @Override
    public void setName(String endpointName) {
        // do nothing, also prevent this endpoint from binding to JMX
    }

    @Override
    public EndpointContext getContext() {
        return realEndpoint.getContext();
    }

    @Override
    public List<Endpoint> getChildren() {
        return realEndpoint.getChildren();
    }

    @Override
    /**
     * Since an Indirect never sends messages for real, it has no moetrics.. but those of its
     * actual endpoint
     */
    public EndpointView getMetricsMBean() {
        return realEndpoint.getMetricsMBean();
    }

    @Override
    /**
     * Figure out the real endpoint we proxy for, and make sure its initialized
     */
    public void init(SynapseEnvironment synapseEnvironment) {
        ConfigurationContext cc =
                ((Axis2SynapseEnvironment) synapseEnvironment).getAxis2ConfigurationContext();
        reLoadAndInitEndpoint(cc);
    }

    @Override
    public String toString() {
        return "Indirect Endpoint [" + key + "]";
    }

    /**
     * Get the real endpoint
     *
     * @param synCtx Message Context
     * @return real endpoint which is referred by the indirect endpoint
     */
    public Endpoint getRealEndpoint(MessageContext synCtx) {

        reLoadAndInitEndpoint(((Axis2MessageContext) synCtx).
                getAxis2MessageContext().getConfigurationContext());
        return realEndpoint;
    }


    /**
     * Reload as needed , either from registry , local entries or predefined endpoints 
     * @param cc ConfigurationContext
     */
    private synchronized void reLoadAndInitEndpoint(ConfigurationContext cc) {

        Parameter parameter = cc.getAxisConfiguration().getParameter(
                SynapseConstants.SYNAPSE_CONFIG);
        Parameter synEnvParameter = cc.getAxisConfiguration().getParameter(
                SynapseConstants.SYNAPSE_ENV);
        if (parameter.getValue() instanceof SynapseConfiguration &&
                synEnvParameter.getValue() instanceof SynapseEnvironment) {

            SynapseConfiguration synCfg = (SynapseConfiguration) parameter.getValue();
            SynapseEnvironment synapseEnvironment = (SynapseEnvironment) synEnvParameter.getValue();

            boolean reLoad = (realEndpoint == null);
            if (!reLoad) {

                Entry entry = synCfg.getEntryDefinition(key);
                if (entry != null && entry.isDynamic()) {

                    if (!entry.isCached() || entry.isExpired()) {
                        reLoad = true;
                    }
                } else {
                    // If the endpoint is static we should reload it from the Synapse config
                    reLoad = true;
                }
            }

            if (reLoad) {

                if (log.isDebugEnabled()) {
                    log.debug("Loading real endpoint with key : " + key);
                }

                realEndpoint = synCfg.getEndpoint(key);
                if (realEndpoint != null && !realEndpoint.isInitialized()) {
                    realEndpoint.init(synapseEnvironment);
                }
            } else {
                Endpoint epr = synCfg.getEndpoint(key);
                if (epr != realEndpoint) {
                    realEndpoint = epr;
                    if (realEndpoint != null && !realEndpoint.isInitialized()
                            && realEndpoint instanceof ManagedLifecycle) {
                        realEndpoint.init(synapseEnvironment);
                    }
                }
            }
        }
    }
}
