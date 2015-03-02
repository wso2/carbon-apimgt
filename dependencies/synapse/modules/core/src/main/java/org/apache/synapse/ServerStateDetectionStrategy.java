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

import org.apache.synapse.config.SynapsePropertiesLoader;
import org.wso2.securevault.secret.SecretManager;

/**
 * Detects possible current server state
 */
public class ServerStateDetectionStrategy {

    private static final String PRODUCTION_MODE = "production";

    /**
     * Determine the next possible server state based on current states and other facts
     *
     * @param contextInformation ServerContextInformation instance
     * @param information        ServerConfigurationInformation instance
     * @return The actual current state possible states for the server
     */
    public static ServerState currentState(ServerContextInformation contextInformation,
                                           ServerConfigurationInformation information) {
        ServerState previousState = contextInformation.getServerState();
        String deploymentMode = information.getDeploymentMode();

        // if the previous state is ServerState.UNDETERMINED it should be ServerState.INITIALIZABLE
        if (previousState == ServerState.UNDETERMINED) {

            // if the server is running on production mode we need to initialize the secret manager 
            // before the server state to be ServerState.INITIALIZABLE
            if (deploymentMode != null && PRODUCTION_MODE.equals(deploymentMode.trim())) {

                SecretManager secretManager = SecretManager.getInstance();
                if (secretManager.isInitialized()) {
                    return ServerState.INITIALIZABLE;
                } else {
                    secretManager.init(SynapsePropertiesLoader.loadSynapseProperties());
                    if (secretManager.isInitialized()) {
                        return ServerState.INITIALIZABLE;
                    }
                }
                
            } else {
                return ServerState.INITIALIZABLE;
            }
        }

        // if the previous state is deterministic then the current state is the previous state
        return previousState;
    }
}
