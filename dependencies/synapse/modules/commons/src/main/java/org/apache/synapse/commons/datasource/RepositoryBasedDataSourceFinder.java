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

/**
 * Finds a DataSource from  DataSource Repositories
 */
public class RepositoryBasedDataSourceFinder {

    private final static Log log = LogFactory.getLog(RepositoryBasedDataSourceFinder.class);
    private DataSourceRepositoryManager dataSourceRepositoryManager;
    private boolean initialized;

    public void init(DataSourceRepositoryManager dataSourceRepositoryManager) {
        this.dataSourceRepositoryManager = dataSourceRepositoryManager;
        this.initialized = true;
    }

    /**
     * Find a DataSource using the given name
     *
     * @param name name of the DataSource to be found
     * @return DataSource if found , otherwise null
     */
    public DataSource find(String name) {
        assertInitialized();
        if (name == null || "".equals(name)) {
            throw new SynapseCommonsException("DataSource name cannot be found.", log);
        }
        return dataSourceRepositoryManager.getDataSource(name);
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new SynapseCommonsException("RepositoryBasedDataSourceFinder has not been " +
                    "initialized with a 'DataSourceRepositoryManager' instance ", log);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}
