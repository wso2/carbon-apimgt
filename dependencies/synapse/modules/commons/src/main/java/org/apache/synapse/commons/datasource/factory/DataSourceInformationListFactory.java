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
import org.apache.synapse.commons.datasource.DataSourceConstants;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.apache.synapse.commons.util.MiscellaneousUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Contains Factory method that use to create List of DataSourceInformations -
 * a DataSourceInformation Collection
 */
public class DataSourceInformationListFactory {

    private static final Log log = LogFactory.getLog(DataSourceInformationListFactory.class);

    /**
     * Factory method for creating a  a DataSourceInformation Collection
     *
     * @param dsProperties Datasource configuration properties
     * @return A List of DataSourceInformation
     */
    public static List<DataSourceInformation> createDataSourceInformationList(
            Properties dsProperties) {

        final List<DataSourceInformation> dataSourceInformations =
                new ArrayList<DataSourceInformation>();
        if (dsProperties == null) {
            if (log.isDebugEnabled()) {
                log.debug("DataSource properties cannot be found..");
            }
            return dataSourceInformations;
        }

        String dataSources = MiscellaneousUtil.getProperty(dsProperties,
                DataSourceConstants.PROP_SYNAPSE_PREFIX_DS, null);

        if (dataSources == null || "".equals(dataSources)) {
            if (log.isDebugEnabled()) {
                log.debug("No DataSources defined for initialization..");
            }
            return dataSourceInformations;
        }

        String[] dataSourcesNames = dataSources.split(",");
        if (dataSourcesNames == null || dataSourcesNames.length == 0) {
            if (log.isDebugEnabled()) {
                log.debug("No DataSource definitions found for initialization..");
            }
            return dataSourceInformations;
        }


        for (String dsName : dataSourcesNames) {

            if (dsName == null) {
                continue;
            }
            DataSourceInformation information =
                    DataSourceInformationFactory.
                            createDataSourceInformation(dsName, dsProperties);
            if (information == null) {
                continue;
            }
            dataSourceInformations.add(information);
        }
        return dataSourceInformations;
    }

}
