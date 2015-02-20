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
package org.apache.synapse.commons.datasource.serializer;

import org.apache.synapse.commons.datasource.DataSourceConstants;
import org.apache.synapse.commons.datasource.DataSourceInformation;

import java.util.List;
import java.util.Properties;

/**
 * Serializes a list of DataSourceInformation into a properties
 */
public class DataSourceInformationListSerializer {

    /**
     * Serializes a list of DataSourceInformation into a properties
     *
     * @param dataSourceInformationList A list of DataSourceInformation
     * @return DataSource configuration properties
     */
    public static Properties serialize(List<DataSourceInformation> dataSourceInformationList) {
        final Properties properties = new Properties();
        StringBuffer dataSources = new StringBuffer();

        for (DataSourceInformation information : dataSourceInformationList) {
            if (information != null) {
                String name = information.getAlias();
                dataSources.append(name);
                dataSources.append(DataSourceConstants.COMMA_STRING);
                properties.putAll(DataSourceInformationSerializer.serialize(information));
            }
        }
        properties.put(DataSourceConstants.PROP_SYNAPSE_PREFIX_DS,
                dataSources.toString());
        return properties;
    }
}
