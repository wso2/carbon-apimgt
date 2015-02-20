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

package org.apache.synapse.core.axis2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;

/**
 * The MessageContext needs to be set up and then is used by the SynapseMessageReceiver to inject messages.
 * This class is used by the SynapseMessageReceiver to find the environment. The env is stored in a Parameter to the Axis2 config
 */
public class MessageContextCreatorForAxis2 {

    private static final Log log = LogFactory.getLog(MessageContextCreatorForAxis2.class);

    private static SynapseConfiguration synCfg = null;
    private static SynapseEnvironment   synEnv = null;

    public static MessageContext getSynapseMessageContext(
            org.apache.axis2.context.MessageContext axisMsgCtx) throws AxisFault {

        if (synCfg == null || synEnv == null) {
            String msg = "Synapse environment has not initialized properly..";
            log.fatal(msg);
            throw new SynapseException(msg);
        }

        // we should try to get the synapse configuration and environment from
        // the axis2 configuration.
        SynapseEnvironment synapseEnvironment = getSynapseEnvironment(axisMsgCtx);
        SynapseConfiguration synapseConfiguration = getSynapseConfiguration(axisMsgCtx);
        if (synapseConfiguration != null && synapseEnvironment != null) {
            return new Axis2MessageContext(axisMsgCtx, synapseConfiguration, synapseEnvironment);
        } else {
            return new Axis2MessageContext(axisMsgCtx, synCfg, synEnv);
        }
    }

    public static void setSynConfig(SynapseConfiguration synCfg) {
        MessageContextCreatorForAxis2.synCfg = synCfg;
    }

    public static void setSynEnv(SynapseEnvironment synEnv) {
        MessageContextCreatorForAxis2.synEnv = synEnv;
    }

    private static SynapseConfiguration getSynapseConfiguration(
            org.apache.axis2.context.MessageContext axisMsgCtx) {
        AxisConfiguration axisCfg = axisMsgCtx.getConfigurationContext().getAxisConfiguration();
        Parameter param = axisCfg.getParameter(SynapseConstants.SYNAPSE_CONFIG);
        if (param != null) {
            return (SynapseConfiguration) param.getValue();
        }
        return null;
    }

    private static SynapseEnvironment getSynapseEnvironment(
            org.apache.axis2.context.MessageContext axisMsgCtx) {
        AxisConfiguration axisCfg = axisMsgCtx.getConfigurationContext().getAxisConfiguration();
        Parameter param = axisCfg.getParameter(SynapseConstants.SYNAPSE_ENV);
        if (param != null) {
            return (SynapseEnvironment) param.getValue();
        }
        return null;
    }
}
