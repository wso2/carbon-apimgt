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
package org.apache.synapse.commons.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.SynapseCommonsException;
import org.apache.synapse.commons.datasource.factory.DataSourceInformationRepositoryFactory;

import java.util.Properties;

/**
 * Holder for the DataSourceRepository
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DataSourceRepositoryHolder {

    private static final Log log = LogFactory.getLog(DataSourceRepositoryHolder.class);

    private static final DataSourceRepositoryHolder DATA_SOURCE_REPOSITORY_HOLDER
            = new DataSourceRepositoryHolder();

    private DataSourceInformationRepository dataSourceInformationRepository;

    private DataSourceRepositoryManager dataSourceRepositoryManager;

    private RepositoryBasedDataSourceFinder repositoryBasedDataSourceFinder;

    private boolean initialized = false;

    private DataSourceRepositoryHolder() {
    }

    public static DataSourceRepositoryHolder getInstance() {
        return DATA_SOURCE_REPOSITORY_HOLDER;
    }

    /**
     * Initialize DataSourceInformationRepository.
     *
     * @param repository to be initialized
     * @param properties DataSources configuration properties
     */
    public void init(DataSourceInformationRepository repository, Properties properties) {

        if (initialized) {
            if (log.isDebugEnabled()) {
                log.debug("Data source repository holder has already been initialized.");
            }
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Initializing the data source repository holder");
        }

        DataSourceInformationRepositoryListener repositoryListener = null;
        if (repository != null) {
            repositoryListener = repository.getRepositoryListener();
        }

        if (repositoryListener == null) {
            repositoryListener = new DataSourceRepositoryManager(
                    new InMemoryDataSourceRepository(),
                    new JNDIBasedDataSourceRepository());
            if (repository != null) {
                repository.setRepositoryListener(repositoryListener);
            }
        }

        if (repositoryListener instanceof DataSourceRepositoryManager) {
            dataSourceRepositoryManager = (DataSourceRepositoryManager) repositoryListener;
            repositoryBasedDataSourceFinder = new RepositoryBasedDataSourceFinder();
            repositoryBasedDataSourceFinder.init(dataSourceRepositoryManager);
        }

        if (repository == null) {
            repository =
                    DataSourceInformationRepositoryFactory.createDataSourceInformationRepository(
                            repositoryListener, properties);
        } else {
            DataSourceInformationRepositoryFactory.setupDataSourceInformationRepository(
                    repository, properties);
        }
        dataSourceInformationRepository = repository;
        initialized = true;
    }

    public DataSourceInformationRepository getDataSourceInformationRepository() {
        assertInitialized();
        return dataSourceInformationRepository;
    }

    private void assertInitialized() {
        if (!initialized) {
            String msg = "Data source repository holder has not been initialized";
            log.error(msg);
            throw new SynapseCommonsException(msg);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public DataSourceRepositoryManager getDataSourceRepositoryManager() {
        assertInitialized();
        return dataSourceRepositoryManager;
    }

    public RepositoryBasedDataSourceFinder getRepositoryBasedDataSourceFinder() {
        assertInitialized();
        return repositoryBasedDataSourceFinder;
    }
}
