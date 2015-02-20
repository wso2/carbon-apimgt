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
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.synapse.commons.SynapseCommonsException;
import org.apache.synapse.commons.datasource.DataSourceConstants;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.wso2.securevault.SecurityConstants;
import org.wso2.securevault.secret.SecretInformation;
import org.wso2.securevault.secret.SecretInformationFactory;
import org.apache.synapse.commons.util.MiscellaneousUtil;

import java.util.Properties;

/**
 * Factory to create a DataSourceInformation based on given properties
 */

public class DataSourceInformationFactory {

    private static final Log log = LogFactory.getLog(DataSourceInformationFactory.class);


    private DataSourceInformationFactory() {
    }

    /**
     * Factory method to create a DataSourceInformation instance based on given properties
     *
     * @param dsName     DataSource Name
     * @param properties Properties to create and configure DataSource
     * @return DataSourceInformation instance
     */
    public static DataSourceInformation createDataSourceInformation(String dsName,
                                                                    Properties properties) {

        if (dsName == null || "".equals(dsName)) {
            if (log.isDebugEnabled()) {
                log.debug("DataSource name is either empty or null, ignoring..");
            }
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(DataSourceConstants.PROP_SYNAPSE_PREFIX_DS);
        buffer.append(DataSourceConstants.DOT_STRING);
        buffer.append(dsName);
        buffer.append(DataSourceConstants.DOT_STRING);

        // Prefix for getting particular data source's properties
        String prefix = buffer.toString();

        String driver = MiscellaneousUtil.getProperty(
                properties, prefix + DataSourceConstants.PROP_DRIVER_CLS_NAME, null);
        if (driver == null) {
            handleException(prefix + DataSourceConstants.PROP_DRIVER_CLS_NAME +
                    " cannot be found.");
        }

        String url = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_URL, null);
        if (url == null) {
            handleException(prefix + DataSourceConstants.PROP_URL +
                    " cannot be found.");
        }

        DataSourceInformation datasourceInformation = new DataSourceInformation();
        datasourceInformation.setAlias(dsName);

        datasourceInformation.setDriver(driver);
        datasourceInformation.setUrl(url);

        String dataSourceName = MiscellaneousUtil.getProperty(
                properties, prefix + DataSourceConstants.PROP_DS_NAME, dsName,
                String.class);
        datasourceInformation.setDatasourceName(dataSourceName);

        String dsType = MiscellaneousUtil.getProperty(
                properties, prefix + DataSourceConstants.PROP_TYPE,
                DataSourceConstants.PROP_BASIC_DATA_SOURCE, String.class);

        datasourceInformation.setType(dsType);

        String repositoryType = MiscellaneousUtil.getProperty(
                properties, prefix + DataSourceConstants.PROP_REGISTRY,
                DataSourceConstants.PROP_REGISTRY_MEMORY, String.class);

        datasourceInformation.setRepositoryType(repositoryType);

        Integer maxActive = MiscellaneousUtil.getProperty(
                properties, prefix + DataSourceConstants.PROP_MAX_ACTIVE,
                GenericObjectPool.DEFAULT_MAX_ACTIVE, Integer.class);
        datasourceInformation.setMaxActive(maxActive);

        Integer maxIdle = MiscellaneousUtil.getProperty(
                properties, prefix + DataSourceConstants.PROP_MAX_IDLE,
                GenericObjectPool.DEFAULT_MAX_IDLE, Integer.class);
        datasourceInformation.setMaxIdle(maxIdle);

        Long maxWait = MiscellaneousUtil.getProperty(
                properties, prefix + DataSourceConstants.PROP_MAX_WAIT,
                GenericObjectPool.DEFAULT_MAX_WAIT, Long.class);

        datasourceInformation.setMaxWait(maxWait);

        // Construct DriverAdapterCPDS reference
        String suffix = DataSourceConstants.PROP_CPDS_ADAPTER +
                DataSourceConstants.DOT_STRING +
                DataSourceConstants.PROP_CLASS_NAME;
        String className = MiscellaneousUtil.getProperty(properties, prefix + suffix,
                DataSourceConstants.PROP_CPDS_ADAPTER_DRIVER);
        datasourceInformation.addParameter(suffix, className);
        suffix = DataSourceConstants.PROP_CPDS_ADAPTER +
                DataSourceConstants.DOT_STRING +
                DataSourceConstants.PROP_FACTORY;
        String factory = MiscellaneousUtil.getProperty(properties, prefix + suffix,
                DataSourceConstants.PROP_CPDS_ADAPTER_DRIVER);
        datasourceInformation.addParameter(suffix, factory);
        suffix = DataSourceConstants.PROP_CPDS_ADAPTER +
                DataSourceConstants.DOT_STRING +
                DataSourceConstants.PROP_NAME;
        String name = MiscellaneousUtil.getProperty(properties, prefix + suffix,
                "cpds");
        datasourceInformation.addParameter(suffix, name);

        boolean defaultAutoCommit = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_DEFAULT_AUTO_COMMIT, true,
                Boolean.class);

        boolean defaultReadOnly = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_DEFAULT_READ_ONLY, false,
                Boolean.class);

        boolean testOnBorrow = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_TEST_ON_BORROW, true,
                Boolean.class);

        boolean testOnReturn = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_TEST_ON_RETURN, false,
                Boolean.class);

        long timeBetweenEvictionRunsMillis = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_TIME_BETWEEN_EVICTION_RUNS_MILLIS,
                GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS, Long.class);

        int numTestsPerEvictionRun = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_NUM_TESTS_PER_EVICTION_RUN,
                GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN, Integer.class);

        long minEvictableIdleTimeMillis = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_MIN_EVICTABLE_IDLE_TIME_MILLIS,
                GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS, Long.class);

        boolean testWhileIdle = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_TEST_WHILE_IDLE, false,
                Boolean.class);

        String validationQuery = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_VALIDATION_QUERY, null);

        int minIdle = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_MIN_IDLE,
                GenericObjectPool.DEFAULT_MIN_IDLE,
                Integer.class);

        int initialSize = MiscellaneousUtil.getProperty(
                properties, prefix + DataSourceConstants.PROP_INITIAL_SIZE, 0,
                Integer.class);

        int defaultTransactionIsolation = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_DEFAULT_TRANSACTION_ISOLATION, -1,
                Integer.class);

        String defaultCatalog = MiscellaneousUtil.getProperty(
                properties, prefix + DataSourceConstants.PROP_DEFAULT_CATALOG, null);

        boolean accessToUnderlyingConnectionAllowed =
                MiscellaneousUtil.getProperty(properties,
                        prefix +
                                DataSourceConstants.
                                        PROP_ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED,
                        false, Boolean.class);

        boolean removeAbandoned = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_REMOVE_ABANDONED, false,
                Boolean.class);

        int removeAbandonedTimeout = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_REMOVE_ABANDONED_TIMEOUT, 300,
                Integer.class);

        boolean logAbandoned = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_LOG_ABANDONED, false,
                Boolean.class);

        boolean poolPreparedStatements = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_POOL_PREPARED_STATEMENTS, false,
                Boolean.class);

        int maxOpenPreparedStatements = MiscellaneousUtil.getProperty(properties,
                prefix + DataSourceConstants.PROP_MAX_OPEN_PREPARED_STATEMENTS,
                GenericKeyedObjectPool.DEFAULT_MAX_TOTAL, Integer.class);

        datasourceInformation.setDefaultAutoCommit(defaultAutoCommit);
        datasourceInformation.setDefaultReadOnly(defaultReadOnly);
        datasourceInformation.setTestOnBorrow(testOnBorrow);
        datasourceInformation.setTestOnReturn(testOnReturn);
        datasourceInformation.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasourceInformation.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        datasourceInformation.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasourceInformation.setTestWhileIdle(testWhileIdle);
        datasourceInformation.setMinIdle(minIdle);
        datasourceInformation.setDefaultTransactionIsolation(defaultTransactionIsolation);
        datasourceInformation.setAccessToUnderlyingConnectionAllowed(
                accessToUnderlyingConnectionAllowed);
        datasourceInformation.setRemoveAbandoned(removeAbandoned);
        datasourceInformation.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        datasourceInformation.setLogAbandoned(logAbandoned);
        datasourceInformation.setPoolPreparedStatements(poolPreparedStatements);
        datasourceInformation.setMaxOpenPreparedStatements(maxOpenPreparedStatements);
        datasourceInformation.setInitialSize(initialSize);

        if (validationQuery != null && !"".equals(validationQuery)) {
            datasourceInformation.setValidationQuery(validationQuery);
        }

        if (defaultCatalog != null && !"".equals(defaultCatalog)) {
            datasourceInformation.setDefaultCatalog(defaultCatalog);
        }

        datasourceInformation.addProperty(
                prefix + DataSourceConstants.PROP_IC_FACTORY,
                MiscellaneousUtil.getProperty(
                        properties, prefix + DataSourceConstants.PROP_IC_FACTORY,
                        null));
        //Provider URL
        datasourceInformation.addProperty(
                prefix + DataSourceConstants.PROP_PROVIDER_URL,
                MiscellaneousUtil.getProperty(
                        properties, prefix + DataSourceConstants.PROP_PROVIDER_URL,
                        null));

        datasourceInformation.addProperty(
                prefix + DataSourceConstants.PROP_PROVIDER_PORT,
                MiscellaneousUtil.getProperty(
                        properties, prefix + DataSourceConstants.PROP_PROVIDER_PORT,
                        null));

        String passwordPrompt = MiscellaneousUtil.getProperty(
                properties, prefix + SecurityConstants.PROP_PASSWORD_PROMPT,
                "Password for datasource " + dsName, String.class);

        SecretInformation secretInformation = SecretInformationFactory.createSecretInformation(
                properties, prefix, passwordPrompt);
        secretInformation.setToken(dsName + "." + SecurityConstants.PROP_PASSWORD);
        datasourceInformation.setSecretInformation(secretInformation);

        return datasourceInformation;
    }

    /**
     * Helper methods for handle errors.
     *
     * @param msg The error message
     */
    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseCommonsException(msg);
    }
}
