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

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Keep all DataSources defined in the Synapse
 */
public interface DataSourceRepository {

    /**
     * Initialization with given properties
     *
     * @param properties configuration properties
     */
    public void init(Properties properties);

    /**
     * Explicitly check for init
     *
     * @return True , if has already initialized
     */
    public boolean isInitialized();

    /**
     * Register a DataSource based on given information
     * Information is encapsulated in a  DataSourceInformation instance
     *
     * @param information DataSourceInformation instance
     */
    void register(DataSourceInformation information);

    /**
     * Removing datasource
     *
     * @param name name of the datasource to be removed
     */
    void unRegister(String name);

    /**
     * Find and Returns an registered  DataSource in the DataSourceRegistry
     *
     * @param name Name of the DataSource to be looked up
     * @return DataSource Instance
     */
    DataSource lookUp(String name);

    /**
     * Clear already registered datasources
     */
    public void clear();

}
