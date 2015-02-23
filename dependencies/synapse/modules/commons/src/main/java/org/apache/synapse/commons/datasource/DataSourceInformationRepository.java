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
package org.apache.synapse.commons.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.SynapseCommonsException;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.secret.SecretInformation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Keep and maintain <code>DataSourceInformation</code>
 */
@SuppressWarnings("unused")
public class DataSourceInformationRepository {

    private static final Log log = LogFactory.getLog(DataSourceInformationRepository.class);

    private final Map<String, DataSourceInformation> dataSourceInformationMap =
            new HashMap<String, DataSourceInformation>();

    private DataSourceInformationRepositoryListener listener;

    /**
     * The global secret resolver of the datasources
     */
    private SecretResolver secretResolver;

    /**
     * Configuring DataSourceInformationRepository
     *
     * @param configurationProperties properties to be used for configure
     */
    public void configure(Properties configurationProperties) {
        if (listener != null) {
            listener.reConfigure(configurationProperties);
        }
        secretResolver = SecretResolverFactory.create(configurationProperties,
                DataSourceConstants.PROP_SYNAPSE_PREFIX_DS);
    }

    /**
     * Adding a DataSourceInformation instance
     *
     * @param dataSourceInformation <code>DataSourceInformation</code> instance
     */
    public void addDataSourceInformation(DataSourceInformation dataSourceInformation) {

        if (dataSourceInformation == null) {
            throw new SynapseCommonsException("DataSource information is null", log);
        }

        // Sets the global secret resolver
        SecretInformation secretInformation = dataSourceInformation.getSecretInformation();
        if (secretInformation != null) {
            secretInformation.setGlobalSecretResolver(secretResolver);
        }

        dataSourceInformationMap.put(dataSourceInformation.getAlias(), dataSourceInformation);
        if (assertListerNotNull()) {
            listener.addDataSourceInformation(dataSourceInformation);
        }
    }

    /**
     * Get an existing <code>DataSourceInformation</code> instance for the given name
     *
     * @param name Name of the DataSourceInformation to be returned
     * @return DataSourceInformation instance if the are any with given name, otherwise
     *         , returns null
     */
    public DataSourceInformation getDataSourceInformation(String name) {

        if (name == null || "".equals(name)) {
            throw new SynapseCommonsException("Name of the datasource information instance to be " +
                    "returned is null", log);
        }

        return dataSourceInformationMap.get(name);
    }

    /**
     * Removing a DataSourceInformation instance by name
     *
     * @param name Name of the DataSourceInformation to be removed
     * @return removed DataSourceInformation instance
     */
    public DataSourceInformation removeDataSourceInformation(String name) {

        if (name == null || "".equals(name)) {
            throw new SynapseCommonsException("Name of the datasource information instance to be" +
                    " removed is null", log);

        }

        DataSourceInformation information = dataSourceInformationMap.remove(name);

        if (information == null) {
            throw new SynapseCommonsException("There is no datasource information instance" +
                    " for given name :" + name, log);

        }

        if (assertListerNotNull()) {
            listener.removeDataSourceInformation(information);
        }

        return information;
    }

    /**
     * Returns all <code>DataSourceInformation</code>s in the repository
     *
     * @return List of <code>DataSourceInformation</code>s
     */
    public Iterator<DataSourceInformation> getAllDataSourceInformation() {

        return dataSourceInformationMap.values().iterator();
    }

    /**
     * Sets a <code>DataSourceInformationRepositoryListener</code> instance
     *
     * @param listener <code>DataSourceInformationRepositoryListener</code> instance
     */
    public void setRepositoryListener(DataSourceInformationRepositoryListener listener) {

        if (listener == null) {
            throw new SynapseCommonsException("Provided DataSourceInformationRepositoryListener " +
                    "instance is null", log);
        }

        if (this.listener != null) {
            throw new SynapseCommonsException("There is already a DataSourceInformationRepositoryListener " +
                    "associated with 'DataSourceInformationRepository", log);
        }

        this.listener = listener;
    }

    /**
     * Remove existing <code>DataSourceInformationRepositoryListener</code>
     */
    public void removeRepositoryListener() {
        this.listener = null;
    }

    /**
     * Gets the existing <code>DataSourceInformationRepositoryListener</code>
     *
     * @return DataSourceInformationRepositoryListener that have been registered
     */
    public DataSourceInformationRepositoryListener getRepositoryListener() {
        return this.listener;
    }

    private boolean assertListerNotNull() {
        if (listener == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find a DataSourceInformationRepositoryListener.");
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("Using DataSourceInformationRepositoryListener as :" + listener);
        }
        return true;
    }
}
