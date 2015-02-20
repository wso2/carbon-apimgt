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
import org.wso2.securevault.SecurityConstants;
import org.wso2.securevault.secret.SecretInformation;

import java.util.Properties;

/**
 * Serialize  a  DataSourceInformation to a Properties
 */
public class DataSourceInformationSerializer {

    /**
     * Serialize  a  DataSourceInformation to a Properties
     *
     * @param information DataSourceInformation instance
     * @return DataSource configuration properties
     */
    public static Properties serialize(DataSourceInformation information) {

        final Properties properties = new Properties();

        String alias = information.getAlias();
        StringBuffer buffer = new StringBuffer();
        buffer.append(DataSourceConstants.PROP_SYNAPSE_PREFIX_DS);
        buffer.append(DataSourceConstants.DOT_STRING);
        buffer.append(alias);
        buffer.append(DataSourceConstants.DOT_STRING);

        // Prefix for getting particular data source's properties
        String prefix = buffer.toString();
        addProperty(properties, prefix + DataSourceConstants.PROP_DS_NAME,
                information.getDatasourceName());

        SecretInformation secretInformation = information.getSecretInformation();
        if (secretInformation != null) {

            String user = secretInformation.getUser();
            if (user != null && !"".equals(user)) {
                addProperty(properties, prefix + SecurityConstants.PROP_USER_NAME,
                        user);
            }

            String password = secretInformation.getAliasSecret();
            if (password != null && !"".equals(password)) {
                addProperty(properties, prefix + SecurityConstants.PROP_PASSWORD,
                        password);
            }

        }
        addProperty(properties, prefix + DataSourceConstants.PROP_MAX_ACTIVE,
                String.valueOf(information.getMaxActive()));
        addProperty(properties, prefix + DataSourceConstants.PROP_MAX_IDLE,
                String.valueOf(information.getMaxIdle()));

        addProperty(properties, prefix + DataSourceConstants.PROP_MAX_WAIT,
                String.valueOf(information.getMaxWait()));

        addProperty(properties, prefix + DataSourceConstants.PROP_DRIVER_CLS_NAME,
                String.valueOf(information.getDriver()));

        addProperty(properties, prefix + DataSourceConstants.PROP_URL,
                String.valueOf(information.getUrl()));

        addProperty(properties, prefix + DataSourceConstants.PROP_TYPE,
                String.valueOf(information.getType()));

        addProperty(properties, prefix + DataSourceConstants.PROP_DEFAULT_AUTO_COMMIT,
                String.valueOf(information.isDefaultAutoCommit()));

        addProperty(properties, prefix + DataSourceConstants.PROP_DEFAULT_READ_ONLY,
                String.valueOf(information.isDefaultReadOnly()));

        addProperty(properties, prefix + DataSourceConstants.PROP_TEST_ON_BORROW,
                String.valueOf(information.isTestOnBorrow()));

        addProperty(properties, prefix + DataSourceConstants.PROP_TEST_ON_RETURN,
                String.valueOf(information.isTestOnReturn()));

        addProperty(properties, prefix + DataSourceConstants.PROP_MIN_IDLE,
                String.valueOf(information.getMinIdle()));

        addProperty(properties, prefix + DataSourceConstants.PROP_INITIAL_SIZE,
                String.valueOf(information.getInitialSize()));

        addProperty(properties, prefix +
                DataSourceConstants.PROP_DEFAULT_TRANSACTION_ISOLATION,
                String.valueOf(information.getDefaultTransactionIsolation()));

        String defaultCatalog = information.getDefaultCatalog();
        if (defaultCatalog != null && !"".equals(defaultCatalog)) {
            addProperty(properties, prefix + DataSourceConstants.PROP_DEFAULT_CATALOG,
                    String.valueOf(defaultCatalog));
        }

        addProperty(properties, prefix +
                DataSourceConstants.PROP_ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED,
                String.valueOf(information.isAccessToUnderlyingConnectionAllowed()));

        addProperty(properties, prefix + DataSourceConstants.PROP_REMOVE_ABANDONED,
                String.valueOf(information.isRemoveAbandoned()));

        addProperty(properties, prefix +
                DataSourceConstants.PROP_REMOVE_ABANDONED_TIMEOUT,
                String.valueOf(information.getRemoveAbandonedTimeout()));

        addProperty(properties, prefix + DataSourceConstants.PROP_LOG_ABANDONED,
                String.valueOf(information.isLogAbandoned()));

        addProperty(properties, prefix +
                DataSourceConstants.PROP_POOL_PREPARED_STATEMENTS,
                String.valueOf(information.isPoolPreparedStatements()));

        addProperty(properties, prefix +
                DataSourceConstants.PROP_MAX_OPEN_PREPARED_STATEMENTS,
                String.valueOf(information.getMaxOpenPreparedStatements()));

        addProperty(properties, prefix + DataSourceConstants.PROP_REGISTRY,
                String.valueOf(information.getRepositoryType()));

        addProperty(properties, prefix +
                DataSourceConstants.PROP_TIME_BETWEEN_EVICTION_RUNS_MILLIS,
                String.valueOf(information.getTimeBetweenEvictionRunsMillis()));

        addProperty(properties, prefix +
                DataSourceConstants.PROP_NUM_TESTS_PER_EVICTION_RUN,
                String.valueOf(information.getNumTestsPerEvictionRun()));

        addProperty(properties, prefix +
                DataSourceConstants.PROP_MIN_EVICTABLE_IDLE_TIME_MILLIS,
                String.valueOf(information.getMinEvictableIdleTimeMillis()));

        addProperty(properties, prefix + DataSourceConstants.PROP_TEST_WHILE_IDLE,
                String.valueOf(information.isTestWhileIdle()));

        String validationQ = information.getValidationQuery();
        if (validationQ != null && !"".equals(validationQ)) {
            addProperty(properties, prefix + DataSourceConstants.PROP_VALIDATION_QUERY,
                    String.valueOf(validationQ));
        }

        properties.putAll(information.getAllParameters());
        properties.putAll(information.getProperties());

        return properties;

    }

    private static void addProperty(Properties properties, String key, String value) {
        if (value != null && !"".equals(value)) {
            properties.setProperty(key, value);
        }
    }
}
