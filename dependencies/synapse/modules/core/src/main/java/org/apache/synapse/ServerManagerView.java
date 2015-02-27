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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @see org.apache.synapse.ServerManagerViewMBean
 */
public class ServerManagerView implements ServerManagerViewMBean {

    private static final Log log = LogFactory.getLog(ServerManagerView.class);

    private final ServerManager serverManager;

    public ServerManagerView(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    /**
     * {@inheritDoc}
     */
    public String getServerState() {
        return serverManager.getServerState().toString();
    }

    /**
     * {@inheritDoc}
     */
    public void start()  throws Exception {
        try {
            Thread.currentThread().setContextClassLoader(serverManager.getClassLoader());
            serverManager.start();
        } catch (Exception ex) {
            // create a plain exception copying only the message
            throw new Exception("Error performing a server start: " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() throws Exception {
        try {
            Thread.currentThread().setContextClassLoader(serverManager.getClassLoader());
            serverManager.stop();
        } catch (Exception ex) {
            // create a plain exception copying only the message
            throw new Exception("Error performing a server stop: " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void restart() throws Exception {
        try {
            log.info("Re-starting Synapse ..");
            stop();
            start();
        } catch (Exception ex) {
            // create a plain exception copying only the message
            throw new Exception("Error performing restart: " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown() throws Exception {
        try {
            Thread.currentThread().setContextClassLoader(serverManager.getClassLoader());
            serverManager.shutdown();
        } catch (Exception ex) {
            // create a plain exception copying only the message
            throw new Exception("Error performing shutdown: " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @param waitSeconds number of seconds to wait for a graceful stop before initiating
     *                    a hard stop
     */
    public void stopGracefully(long waitSeconds) throws Exception {
        try {
            Thread.currentThread().setContextClassLoader(serverManager.getClassLoader());
            serverManager.stopGracefully(waitSeconds * 1000);
        } catch (Exception ex) {
            // create a plain exception copying only the message
            throw new Exception("Error performing graceful stop: " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startMaintenance() throws Exception {
        try {
            Thread.currentThread().setContextClassLoader(serverManager.getClassLoader());
            serverManager.startMaintenance();
        } catch (Exception ex) {
            // create a plain exception copying only the message
            throw new Exception("Error switching to maintenance mode: " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endMaintenance() throws Exception {
        try {
            Thread.currentThread().setContextClassLoader(serverManager.getClassLoader());
            serverManager.endMaintenance();
        } catch (Exception ex) {
            // create a plain exception copying only the message
            throw new Exception("Error switching back from maintenance mode: " + ex.getMessage());
        }
    }
}
