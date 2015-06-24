/**
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

package org.apache.synapse.commons.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.SynapseCommonsException;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates and manages RMI registries
 */
public class RMIRegistryController {

    public static final Log log = LogFactory.getLog(RMIRegistryController.class);

    private static RMIRegistryController ourInstance = new RMIRegistryController();
    private final Map<String, Registry> registriesCache = new HashMap<String, Registry>();

    public static RMIRegistryController getInstance() {
        return ourInstance;
    }

    private RMIRegistryController() {
    }

    /**
     * Creates a RMI local registry with given port
     *
     * @param port The port of the RMI registry to be created
     */
    public void createLocalRegistry(int port) {

        try {

            String key = toKey(port);
            
            synchronized (registriesCache) {
                if (registriesCache.containsKey(key)) {
                    if (log.isDebugEnabled()) {
                        log.debug("There is an RMI registry bound to given port :" + port);
                    }
                    return;
                }

                Registry locateRegistry = LocateRegistry.createRegistry(port);
                if (locateRegistry == null) {
                    handleException("Unable to create a RMI registry with port : " + port);
                }

                registriesCache.put(key, locateRegistry);
            }

        } catch (RemoteException e) {
            String msg = "Couldn't create a local registry(RMI) : port " + port +
                    " already in use.";
            handleException(msg, e);
        }
    }

    /**
     * removes if there is a RMI local registry instance
     *
     * @param port The port of the RMI registry to be removed
     */
    public void removeLocalRegistry(int port) {

        String key = toKey(port);
        synchronized (registriesCache) {
            if (registriesCache.containsKey(key)) {
                removeRegistry(key, registriesCache.get(key));
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("There is no RMi registry for port : " + port);
                }
            }
        }
    }

    /**
     * Removes all registered RMI registries
     */
    public void shutDown() {

        synchronized (registriesCache) {
            Collection<String> registryKeys = new ArrayList<String>(registriesCache.size());
            registryKeys.addAll(registriesCache.keySet());
            for (String key : registryKeys) {
                removeRegistry(key, registriesCache.get(key));
            }
        }
    }

    /**
     * Helper method to remove a RMI registry instance
     *
     * @param key      The port of the RMI registry to be removed
     * @param registry Registry instance
     */
    private void removeRegistry(String key, Registry registry) {

        if (registry != null) {
            synchronized (registriesCache) {
                try {
                    log.info("Removing the RMI registry bound to port : " + key);
                    UnicastRemoteObject.unexportObject(registry, true);
                    registriesCache.remove(key);
                } catch (NoSuchObjectException e) {
                    String msg = "Error when stopping local registry(RMI)";
                    handleException(msg, e);
                }
            }
            
        }

    }

    private static String toKey(int port) {

        assertPositive(port);
        return String.valueOf(port);
    }

    private static void assertPositive(int port) {

        if (port < 0) {
            handleException("Invalid port number : " + port);
        }
    }

    /**
     * Helper methods for handle errors.
     *
     * @param msg The error message
     * @param e   The exception
     */
    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseCommonsException(msg, e);
    }

    /**
     * Helper methods for handle errors.
     *
     * @param msg The error message
     */
    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseCommonsException(msg);
    }

}
