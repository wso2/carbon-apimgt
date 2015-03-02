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

/**
 * The controller for synapse
 * Create, Start, Stop and Destroy synapse artifacts in a particular environment
 */
public interface SynapseController {

    /**
     * Initialization of the synapse controller
     *
     * @param configurationInformation server information instance Information about the server
     * @param contextInformation    if there is a context already has been built.
     */
    void init(ServerConfigurationInformation configurationInformation,
                     ServerContextInformation contextInformation);

    /**
     * Destroys the Synapse Controller.
     */
    void destroy();

    /**
     * Explicitly checks the initialization.
     *
     * @return true if the initialization has been a success.
     */
    boolean isInitialized();

    /**
     * Starts the synapse controller and in turn the synapse server.
     */
    void start();

    /**
     * Stops the synapse controller and in turn the synapse server.
     */
    void stop();

    /**
     * Creates the SynapseEnvironment instance.
     *
     * @return SynapseEnvironment instance if success
     */
    SynapseEnvironment createSynapseEnvironment();

    /**
     * Destroys the SynapseEnvironment instance.
     */
    void destroySynapseEnvironment();

    /**
     * Creates the Synapse configuration by reading and processing the synapse configuration file.
     * 
     * @return the synapse configuration
     */
    SynapseConfiguration createSynapseConfiguration();

    /**
     * Destroys the SynapseConfiguration instance
     */
    void destroySynapseConfiguration();

    /**
     * Returns underlying environment context
     *
     * @return Underlying environment context
     */
    Object getContext();

    /**
     * Puts the Synapse Server into a maintenance mode pausing transport listeners, senders 
     * and tasks.
     */
    void startMaintenance();

    /**
     * Recovers the Synapse Server from maintenance mode resuming transport listeners, senders 
     * and tasks.
     */
    void endMaintenance();

    /**
     * Waits until it is safe to stop or the specified end time has been reached. A delay
     * of <code>waitIntervalMillis</code> milliseconds is used between each subsequent check.
     * If the state "safeToStop" is reached before the specified <code>endTime</code>, 
     * the return value is true.
     * 
     * @param waitIntervalMillis the pause time (delay) in milliseconds between subsequent checks
     * @param endTime            the time until which the checks need to finish successfully
     * 
     * @return true, if a safe state is reached before the specified <code>endTime</code>,
     *         otherwise false (forceful stop required)
     */
    boolean waitUntilSafeToStop(long waitIntervalMillis, long endTime);
}
