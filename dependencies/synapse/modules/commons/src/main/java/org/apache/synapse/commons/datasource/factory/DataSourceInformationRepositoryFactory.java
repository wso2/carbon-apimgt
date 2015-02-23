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
package org.apache.synapse.commons.datasource.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.*;

import java.util.List;
import java.util.Properties;

/**
 * Contains Factory methods that use to create DataSourceInformationRepository
 */
public class DataSourceInformationRepositoryFactory {

    private static final Log log = LogFactory.getLog(DataSourceInformationRepositoryFactory.class);

    /**
     * Factory method to create a DataSourceInformationRepository
     * Use 'DataSourceRepositoryManager' as RepositoryListener
     *
     * @param properties DataSource properties
     * @return DataSourceInformationRepository instance
     */
    public static DataSourceInformationRepository createDataSourceInformationRepository(
            Properties properties) {
        return createDataSourceInformationRepository(
                new DataSourceRepositoryManager(
                        new InMemoryDataSourceRepository(),
                        new JNDIBasedDataSourceRepository()), properties);
    }

    /**
     * Factory method to create a DataSourceInformationRepository
     *
     * @param listener   DataSourceInformationRepositoryListener
     * @param properties DataSource properties
     * @return a new, configured DataSourceInformationRepository instance
     */
    public static DataSourceInformationRepository createDataSourceInformationRepository(
            DataSourceInformationRepositoryListener listener, Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("Creating a new DataSourceInformationRepository");
        }
        DataSourceInformationRepository datasourceInformationRepository =
                new DataSourceInformationRepository();

        datasourceInformationRepository.setRepositoryListener(listener);
        setupDataSourceInformationRepository(datasourceInformationRepository, properties);

        return datasourceInformationRepository;
    }

    /**
     * Setup an existing datasource information repository adding the provided
     * datasource information.
     *
     * @param datasourceInformationRepository
     *                   an existing data source information repository
     * @param properties DataSource properties
     */
    public static void setupDataSourceInformationRepository(
            DataSourceInformationRepository datasourceInformationRepository,
            Properties properties) {

        if (properties != null) {
            datasourceInformationRepository.configure(properties);
        }
        List<DataSourceInformation> sourceInformationList =
                DataSourceInformationListFactory.createDataSourceInformationList(properties);

        for (DataSourceInformation information : sourceInformationList) {
            if (information != null) {
                datasourceInformationRepository.addDataSourceInformation(information);
            }
        }
    }
}
