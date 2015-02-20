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
package org.apache.synapse.util;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.SynapseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for replicating states
 */
public class Replicator {

    private static final Log log = LogFactory.getLog(Replicator.class);

    /**
     * Helper method to replicates states of the property with given key
     * Removes the property and then replicates the current state so that all instances
     * across cluster can see this state
     *
     * @param key       The key of the property
     * @param configCtx Axis2 configuration context
     */
    public static void removeAndReplicateState(String key, ConfigurationContext configCtx) {

        if (configCtx != null && key != null) {

            try {
                if (log.isDebugEnabled()) {
                    log.debug("Start replicating the property removal with key : " + key);
                }

                configCtx.removePropertyNonReplicable(key);
                org.apache.axis2.clustering.state.Replicator.replicate(
                        configCtx, new String[]{key});

                if (log.isDebugEnabled()) {
                    log.debug("Completed replication of the property removal with key : " + key);
                }

            } catch (ClusteringFault clusteringFault) {
                handleException("Error during the replicating states ", clusteringFault);
            }
        }
    }

    /**
     * Helper method to replicates states of the property with given key
     * replicates  the given state so that all instances across cluster can see this state
     *
     * @param key       The key of the property
     * @param value     The value of the property
     * @param configCtx Axis2 COnfiguration Context
     */
    public static void setAndReplicateState(String key, Object value, ConfigurationContext configCtx) {

        if (configCtx != null && key != null && value != null) {

            try {
                if (log.isDebugEnabled()) {
                    log.debug("Start replicating the property with key : " + key +
                            " value : " + value);
                }

                configCtx.setNonReplicableProperty(key, value);
                org.apache.axis2.clustering.state.Replicator.replicate(
                        configCtx, new String[]{key});

                if (log.isDebugEnabled()) {
                    log.debug("Completed replication of the property with key : " + key);
                }

            } catch (ClusteringFault clusteringFault) {
                handleException("Error during the replicating states ", clusteringFault);
            }
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
        throw new SynapseException(msg, e);
    }
}
