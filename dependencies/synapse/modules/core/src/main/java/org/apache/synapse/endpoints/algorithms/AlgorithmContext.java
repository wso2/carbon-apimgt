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
package org.apache.synapse.endpoints.algorithms;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.util.Replicator;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps the runtime state of the algorithm
 */
public class AlgorithmContext {

    private static final Log log = LogFactory.getLog(AlgorithmContext.class);

    private static final String KEY_PREFIX = "synapse.endpoint.lb.algorithm.";
    private static final String CURRENT_EPR = ".current_epr";

    /* The axis2 configuration context - this hold state in a clustered environment. */
    private ConfigurationContext cfgCtx;

    /* Are we supporting clustering ? */
    private boolean isClusteringEnabled = false;

    /* The key for 'currentEPR' attribute when replicated in a cluster */
    private String CURRENT_EPR_PROP_KEY;

    /* Prefix for uniquely identify  properties of a particular endpoint  */
    private String PROPERTY_KEY_PREFIX;

    /* The pointer to current epr - The position of the current EPR */
    private int currentEPR = 0;

    /* The map of properties stored locally */
    private Map<String, Object> localProperties;

    public AlgorithmContext(boolean clusteringEnabled, ConfigurationContext cfgCtx, String endpointName) {

        this.cfgCtx = cfgCtx;
        this.isClusteringEnabled = clusteringEnabled;

        if (!clusteringEnabled) {
            localProperties = new HashMap<String, Object>();
        } else {
            PROPERTY_KEY_PREFIX = KEY_PREFIX + endpointName;
            CURRENT_EPR_PROP_KEY = PROPERTY_KEY_PREFIX + CURRENT_EPR;
        }
    }

    /**
     * To get the position of the current EPR for use. Default to 0 - i.e. first endpoint
     *
     * @return The  position of the current EPR
     */
    public int getCurrentEndpointIndex() {

        if (isClusteringEnabled) {

            Object value = cfgCtx.getPropertyNonReplicable(this.CURRENT_EPR_PROP_KEY);
            if (value == null) {
                return 0;
            } else if (value instanceof Integer) {
                return ((Integer) value);
            }
        } else {
            return currentEPR;
        }
        return 0;
    }

    /**
     * The  position of the current EPR
     *
     * @param currentEPR The current position
     */
    public void setCurrentEndpointIndex(int currentEPR) {
        this.currentEPR = currentEPR;
        cfgCtx.setNonReplicableProperty(CURRENT_EPR_PROP_KEY, currentEPR);
    }

    /**
     * Get the configuration context instance . This is only available for cluster env.
     *
     * @return Returns the ConfigurationContext instance
     */
    public ConfigurationContext getConfigurationContext() {
        return cfgCtx;
    }

    /**
     * Get the property value corresponding to a specified key
     *
     * @param key The key of the property
     * @return The value of the property or null if the key does not exist
     */
    public Object getProperty(String key) {
        if (isClusteringEnabled) {
            return cfgCtx.getPropertyNonReplicable(PROPERTY_KEY_PREFIX + key);
        } else {
            return localProperties.get(key);
        }
    }

    /**
     * Store a property in the algorithm context. In a clustered environment
     * properties will be saved in the configuration context and replicated.
     * In non-clustered environments properties will be stored in a local property
     * map.
     *
     * @param key   The key of the property
     * @param value The value of the property
     */
    public void setProperty(String key, Object value) {

        if (key != null && value != null) {
            if (isClusteringEnabled) {
                Replicator.setAndReplicateState(PROPERTY_KEY_PREFIX + key, value, cfgCtx);
            } else {
                localProperties.put(key, value);
            }
        }
    }

}