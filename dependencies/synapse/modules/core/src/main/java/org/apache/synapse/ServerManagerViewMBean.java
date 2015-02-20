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

/**
 * A simple MBean for managing the Synapse Server.
 */
public interface ServerManagerViewMBean {

    /**
     * Shows the current state of the server.
     * 
     * @return the current state of the server
     */
    String getServerState();
    
    /**
     * Starts the Server.
     * 
     * @throws Exception if an error occurs
     */
    void start() throws Exception;

    /**
     * Stops the Server without shutting it down.
     * 
     * @throws Exception if an error occurs
     */
    void stop() throws Exception;

    /**
     * Restart the Server.
     * 
     * @throws Exception if an error occurs
     */
    void restart() throws Exception;

    /**
     * Shutdown the Server.
     * 
     * @throws Exception if an error occurs
     */
    void shutdown() throws Exception;

    /**
     * Stops the Server gracefully. This includes putting the server in maintenance and
     * stopping it.
     * 
     * @param waitSeconds number of seconds to wait for a graceful stop before initiating
     *                    a hard stop
     *                    
     * @throws Exception if an error occurs
     */
    void stopGracefully(long waitSeconds) throws Exception;

    /**
     * Puts the server into the maintenance mode.
     * 
     * @throws Exception if an error occurs
     */
    void startMaintenance() throws Exception;

    /**
     * Finishes the maintenance and put the server online.
     * 
     * @throws Exception if an error occurs
     */
    void endMaintenance() throws Exception;
    
}
