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

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Manages data sources defined in the synapse. This is an observer of the DataSourceInformationRepository
 */
public class DataSourceRepositoryManager implements DataSourceInformationRepositoryListener {

    private static final Log log = LogFactory.getLog(DataSourceRepositoryManager.class);

    private final InMemoryDataSourceRepository inMemoryDataSourceRepository;
    private final JNDIBasedDataSourceRepository jndiBasedDataSourceRepository;

    public DataSourceRepositoryManager(InMemoryDataSourceRepository inMemoryDataSourceRepository,
                                       JNDIBasedDataSourceRepository jndiBasedDataSourceRepository) {
        this.inMemoryDataSourceRepository = inMemoryDataSourceRepository;
        this.jndiBasedDataSourceRepository = jndiBasedDataSourceRepository;
    }

    /**
     * Find a DataSource using given name
     *
     * @param name Name of the DataSource to be found
     * @return DataSource if found , otherwise null
     */
    public DataSource getDataSource(String name) {

        if (name == null || "".equals(name)) {
            throw new SynapseCommonsException("DataSource name cannot be found.", log);
        }

        DataSource result = inMemoryDataSourceRepository.lookUp(name);

        if (result != null) {
            if (log.isDebugEnabled()) {
                log.debug("DataSource is found in the in-memory data source repository." +
                        " Datasource name is : " + name);
            }
            return result;
        }

        if (jndiBasedDataSourceRepository.isInitialized()) {
            result = jndiBasedDataSourceRepository.lookUp(name);
            if (result != null) {
                if (log.isDebugEnabled()) {
                    log.debug("DataSource is found in the JNDI data source repository." +
                            " Datasource name is : " + name);
                }
                return result;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Cannot find a datasource with name : " + name + " either in in-memory or" +
                    " JNDI datasource repositories");
        }

        return result;
    }

    public void addDataSourceInformation(DataSourceInformation dataSourceInformation) {

        assertDataSourceInformationNull(dataSourceInformation);

        String repositoryType = dataSourceInformation.getRepositoryType();

        if (log.isDebugEnabled()) {
            log.debug("Registering a datasource in the repository : " + repositoryType + "." +
                    " DataSource information is : " + dataSourceInformation);
        }

        if (DataSourceConstants.PROP_REGISTRY_JNDI.equals(repositoryType)) {
            jndiBasedDataSourceRepository.register(dataSourceInformation);
        } else {
            inMemoryDataSourceRepository.register(dataSourceInformation);
        }
    }

    public void removeDataSourceInformation(DataSourceInformation dataSourceInformation) {

        assertDataSourceInformationNull(dataSourceInformation);

        String repositoryType = dataSourceInformation.getRepositoryType();

        if (log.isDebugEnabled()) {
            log.debug("Un-Registering a datasource from the repository : " + repositoryType + "." +
                    " DataSource information is : " + dataSourceInformation);
        }

        if (DataSourceConstants.PROP_REGISTRY_JNDI.equals(repositoryType)) {
            jndiBasedDataSourceRepository.unRegister(dataSourceInformation.getDatasourceName());
        } else {
            inMemoryDataSourceRepository.unRegister(dataSourceInformation.getDatasourceName());
        }
    }

    public void reConfigure(Properties confProperties) {

        if (log.isDebugEnabled()) {
            log.debug("Reconfiguring datasource repositories ");
        }
        jndiBasedDataSourceRepository.init(confProperties);
        inMemoryDataSourceRepository.init(confProperties);
    }

    /**
     * Clear all DataSource Repositories
     */
    public void clear() {

        if (log.isDebugEnabled()) {
            log.debug("Clearing datasource repositories ");
        }

        if (inMemoryDataSourceRepository.isInitialized()) {
            inMemoryDataSourceRepository.clear();
        }
        if (jndiBasedDataSourceRepository.isInitialized()) {
            jndiBasedDataSourceRepository.clear();
        }
    }

    private void assertDataSourceInformationNull(DataSourceInformation dataSourceInformation) {
        if (dataSourceInformation == null) {
            throw new SynapseCommonsException("Provided DataSource Information instance is null",
                    log);
        }
    }
}
