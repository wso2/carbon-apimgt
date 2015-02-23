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

import java.util.Properties;

/**
 * listen and handle events relating to changes in <code>DataSourceInformationRepository</code>
 */
public interface DataSourceInformationRepositoryListener {

    /**
     * Event when adding a DataSourceInformation
     *
     * @param dataSourceInformation added DataSourceInformation instance
     */
    void addDataSourceInformation(DataSourceInformation dataSourceInformation);

    /**
     * Event when removing a  DataSourceInformation instance
     *
     * @param dataSourceInformation removed DataSourceInformation instance
     */
    void removeDataSourceInformation(DataSourceInformation dataSourceInformation);

    /**
     * Event when re-configuring the DataSourceInformationRepository
     *
     * @param confProperties properties used to configure DataSourceInformationRepository
     */
    void reConfigure(Properties confProperties);
}
