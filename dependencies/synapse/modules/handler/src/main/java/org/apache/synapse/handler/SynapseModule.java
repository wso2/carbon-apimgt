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

package org.apache.synapse.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.modules.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.synapse.*;
import org.apache.synapse.core.SynapseEnvironment;

/**
 * This will be the Module class for the Synapse handler based mediation inside axis2 server. This
 * will just set the default system property of SYNAPSE_XML to the repository/conf/synapse.xml in
 * the axis2 servers repository and call the normal Synapse startup.
 */
public class SynapseModule implements Module {

    /**
     * Log variable to be used as the logging appender
     */
    private static final Log log = LogFactory.getLog(SynapseModule.class);

    /**
     * This method will call the normal initiation after setting the SYNAPSE_XML file to get from
     * the axis2 repository/conf folder
     *
     * @param configurationContext - ConfigurationContext of the Axis2 env
     * @param axisModule - AxisModule describing handler initializationModule of Synapse
     * @throws AxisFault - in-case of a failure in initiation
     */
    public void init(ConfigurationContext configurationContext, AxisModule axisModule)
            throws AxisFault {

        Object synEnvParameter = configurationContext.getAxisConfiguration().getParameterValue(
                SynapseConstants.SYNAPSE_ENV);

        if (synEnvParameter != null && !(((SynapseEnvironment) synEnvParameter)
                .getServerContextInformation().getServerState() == ServerState.STARTED)) {
            log.info("Initializing the Synapse as a handler");
            ServerConfigurationInformation configurationInformation =
                    ServerConfigurationInformationFactory.createServerConfigurationInformation(
                            configurationContext.getAxisConfiguration());
            ServerContextInformation contextInfo
                    = new ServerContextInformation(configurationContext, configurationInformation);
            ServerManager serverManager = new ServerManager();
            serverManager.init(configurationInformation, contextInfo);
            serverManager.start();
        } else {
            log.info("Detected an already started synapse instance using that for the mediation");
        }
    }

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {}
    public boolean canSupportAssertion(Assertion assertion) { return false; }
    public void applyPolicy(Policy policy, AxisDescription axisDescription) throws AxisFault {}
    public void shutdown(ConfigurationContext configurationContext) throws AxisFault {}
}
