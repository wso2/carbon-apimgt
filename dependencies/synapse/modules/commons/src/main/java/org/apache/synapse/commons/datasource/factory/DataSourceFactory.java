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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.PerUserPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.SynapseCommonsException;
import org.apache.synapse.commons.datasource.DataSourceInformation;

import javax.sql.DataSource;

/**
 * Factory for creating a DataSource based on information in DataSourceInformation
 */
public class DataSourceFactory {

    private final static Log log = LogFactory.getLog(DataSourceFactory.class);


    private DataSourceFactory() {
    }

    /**
     * Factory method to create a DataSource based on provided information
     * which is encapsulated in the DataSourceInformation object.
     *
     * @param dataSourceInformation Information about DataSource
     * @return DataSource Instance if one can be created ,
     *         otherwise null or exception if provided details are not valid or enough to create
     *         a DataSource
     */
    public static DataSource createDataSource(DataSourceInformation dataSourceInformation) {

        String dsType = dataSourceInformation.getType();
        String driver = dataSourceInformation.getDriver();

        if (driver == null || "".equals(driver)) {
            handleException("Database driver class name cannot be found.");
        }

        String url = dataSourceInformation.getUrl();

        if (url == null || "".equals(url)) {
            handleException("Database connection URL cannot be found.");
        }

        String user = dataSourceInformation.getSecretInformation().getUser();
        String password = dataSourceInformation.getSecretInformation().getResolvedSecret();

        int defaultTransactionIsolation = dataSourceInformation.getDefaultTransactionIsolation();


        if (DataSourceInformation.BASIC_DATA_SOURCE.equals(dsType)) {

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(driver);
            basicDataSource.setUrl(url);

            if (user != null && !"".equals(user)) {
                basicDataSource.setUsername(user);
            }

            if (password != null && !"".equals(password)) {
                basicDataSource.setPassword(password);
            }

            basicDataSource.setMaxActive(dataSourceInformation.getMaxActive());
            basicDataSource.setMaxIdle(dataSourceInformation.getMaxIdle());
            basicDataSource.setMaxWait(dataSourceInformation.getMaxWait());
            basicDataSource.setMinIdle(dataSourceInformation.getMinIdle());
            basicDataSource.setDefaultAutoCommit(dataSourceInformation.isDefaultAutoCommit());
            basicDataSource.setDefaultReadOnly(dataSourceInformation.isDefaultReadOnly());
            basicDataSource.setTestOnBorrow(dataSourceInformation.isTestOnBorrow());
            basicDataSource.setTestOnReturn(dataSourceInformation.isTestOnReturn());
            basicDataSource.setTestWhileIdle(dataSourceInformation.isTestWhileIdle());
            basicDataSource.setMinEvictableIdleTimeMillis(
                    dataSourceInformation.getMinEvictableIdleTimeMillis());
            basicDataSource.setTimeBetweenEvictionRunsMillis(
                    dataSourceInformation.getTimeBetweenEvictionRunsMillis());
            basicDataSource.setNumTestsPerEvictionRun(
                    dataSourceInformation.getNumTestsPerEvictionRun());
            basicDataSource.setMaxOpenPreparedStatements(
                    dataSourceInformation.getMaxOpenPreparedStatements());
            basicDataSource.setAccessToUnderlyingConnectionAllowed(
                    dataSourceInformation.isAccessToUnderlyingConnectionAllowed());
            basicDataSource.setInitialSize(dataSourceInformation.getInitialSize());
            basicDataSource.setPoolPreparedStatements(
                    dataSourceInformation.isPoolPreparedStatements());


            if (defaultTransactionIsolation != -1) {
                basicDataSource.setDefaultTransactionIsolation(defaultTransactionIsolation);
            }

            String defaultCatalog = dataSourceInformation.getDefaultCatalog();
            if (defaultCatalog != null && !"".equals(defaultCatalog)) {
                basicDataSource.setDefaultCatalog(defaultCatalog);
            }

            String validationQuery = dataSourceInformation.getValidationQuery();

            if (validationQuery != null && !"".equals(validationQuery)) {
                basicDataSource.setValidationQuery(validationQuery);
            }

            return basicDataSource;

        } else if (DataSourceInformation.PER_USER_POOL_DATA_SOURCE.equals(dsType)) {

            DriverAdapterCPDS adapterCPDS = new DriverAdapterCPDS();

            try {
                adapterCPDS.setDriver(driver);
            } catch (ClassNotFoundException e) {
                handleException("Error setting driver : " + driver + " in DriverAdapterCPDS", e);
            }

            adapterCPDS.setUrl(url);

            if (user != null && !"".equals(user)) {
                adapterCPDS.setUser(user);
            }

            if (password != null && !"".equals(password)) {
                adapterCPDS.setPassword(password);
            }

            adapterCPDS.setPoolPreparedStatements(dataSourceInformation.isPoolPreparedStatements());
            adapterCPDS.setMaxIdle(dataSourceInformation.getMaxIdle());


            PerUserPoolDataSource perUserPoolDataSource = new PerUserPoolDataSource();
            perUserPoolDataSource.setConnectionPoolDataSource(adapterCPDS);

            perUserPoolDataSource.setDefaultMaxActive(dataSourceInformation.getMaxActive());
            perUserPoolDataSource.setDefaultMaxIdle(dataSourceInformation.getMaxIdle());
            perUserPoolDataSource.setDefaultMaxWait((int) dataSourceInformation.getMaxWait());
            perUserPoolDataSource.setDefaultAutoCommit(dataSourceInformation.isDefaultAutoCommit());
            perUserPoolDataSource.setDefaultReadOnly(dataSourceInformation.isDefaultReadOnly());
            perUserPoolDataSource.setTestOnBorrow(dataSourceInformation.isTestOnBorrow());
            perUserPoolDataSource.setTestOnReturn(dataSourceInformation.isTestOnReturn());
            perUserPoolDataSource.setTestWhileIdle(dataSourceInformation.isTestWhileIdle());
            perUserPoolDataSource.setMinEvictableIdleTimeMillis(
                    (int) dataSourceInformation.getMinEvictableIdleTimeMillis());
            perUserPoolDataSource.setTimeBetweenEvictionRunsMillis(
                    (int) dataSourceInformation.getTimeBetweenEvictionRunsMillis());
            perUserPoolDataSource.setNumTestsPerEvictionRun(
                    dataSourceInformation.getNumTestsPerEvictionRun());

            if (defaultTransactionIsolation != -1) {
                perUserPoolDataSource.setDefaultTransactionIsolation(defaultTransactionIsolation);
            }


            String validationQuery = dataSourceInformation.getValidationQuery();

            if (validationQuery != null && !"".equals(validationQuery)) {
                perUserPoolDataSource.setValidationQuery(validationQuery);
            }

            return perUserPoolDataSource;

        } else {
            handleException("Unsupported DataSource : " + dsType);
        }
        return null;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseCommonsException(msg);
    }

    private static void handleException(String msg, Throwable throwable) {
        log.error(msg, throwable);
        throw new SynapseCommonsException(msg, throwable);
    }
}
