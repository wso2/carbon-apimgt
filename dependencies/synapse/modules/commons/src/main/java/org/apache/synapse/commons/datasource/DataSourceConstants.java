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

/**
 * Constants related to the DataSource component
 */
public class DataSourceConstants {

    public static final String PROP_SYNAPSE_PREFIX_DS = "synapse.datasources";

    public static final String PROP_DRIVER_CLS_NAME = "driverClassName";

    public static final String PROP_DS_NAME = "dsName";

    public static final String PROP_URL = "url";

    public static final String PROP_DRIVER = "driver";

    public static final String PROP_USER = "user";

    public static final String PROP_CPDS_ADAPTER = "cpdsadapter";

    public static final String PROP_JNDI_ENV = "jndiEnvironment";

    public static final String PROP_DEFAULT_MAX_ACTIVE = "defaultMaxActive";

    public static final String PROP_DEFAULT_MAX_IDLE = "defaultMaxIdle";

    public static final String PROP_DEFAULT_MAX_WAIT = "defaultMaxWait";

    public static final String PROP_DATA_SOURCE_NAME = "dataSourceName";

    public static final String PROP_CPDS_CLASS_NAME = "className";

    public static final String PROP_CPDS_FACTORY = "factory";

    public static final String PROP_CPDS_NAME = "name";

    public final static String PROP_DEFAULT_AUTO_COMMIT = "defaultAutoCommit";

    public final static String PROP_DEFAULT_READ_ONLY = "defaultReadOnly";

    public final static String PROP_TEST_ON_BORROW = "testOnBorrow";

    public final static String PROP_TEST_ON_RETURN = "testOnReturn";

    public final static String PROP_TIME_BETWEEN_EVICTION_RUNS_MILLIS
            = "timeBetweenEvictionRunsMillis";

    public final static String PROP_NUM_TESTS_PER_EVICTION_RUN = "numTestsPerEvictionRun";

    public final static String PROP_MIN_EVICTABLE_IDLE_TIME_MILLIS
            = "minEvictableIdleTimeMillis";

    public final static String PROP_TEST_WHILE_IDLE = "testWhileIdle";

    public final static String PROP_VALIDATION_QUERY = "validationQuery";

    public final static String PROP_MAX_ACTIVE = "maxActive";

    public final static String PROP_MAX_IDLE = "maxIdle";

    public final static String PROP_MAX_WAIT = "maxWait";

    public final static String PROP_MIN_IDLE = "minIdle";

    public final static String PROP_INITIAL_SIZE = "initialSize";

    public final static String PROP_DEFAULT_TRANSACTION_ISOLATION
            = "defaultTransactionIsolation";

    public final static String PROP_DEFAULT_CATALOG = "defaultCatalog";

    public final static String PROP_ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED
            = "accessToUnderlyingConnectionAllowed";

    public final static String PROP_REMOVE_ABANDONED = "removeAbandoned";

    public final static String PROP_REMOVE_ABANDONED_TIMEOUT = "removeAbandonedTimeout";

    public final static String PROP_LOG_ABANDONED = "logAbandoned";

    public final static String PROP_POOL_PREPARED_STATEMENTS = "poolPreparedStatements";

    public final static String PROP_MAX_OPEN_PREPARED_STATEMENTS = "maxOpenPreparedStatements";

    public static final String PROP_PROVIDER_PORT = "providerPort";

    public final static String PROP_REGISTRY = "registry";

    public final static String PROP_REGISTRY_MEMORY = "memory";

    public final static String PROP_REGISTRY_JNDI = "JNDI";

    public static final String PROP_IC_FACTORY = "icFactory";

    public static final String PROP_PROVIDER_URL = "providerUrl";

    public static final String DOT_STRING = ".";

    public static final String COMMA_STRING = ",";

    public static final String PROP_TYPE = "type";

    public static final String PROP_BASIC_DATA_SOURCE = "BasicDataSource";

    public static final String PROP_CLASS_NAME = "className";

    public static final String PROP_CPDS_ADAPTER_DRIVER
            = "org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS";

    public static final String PROP_FACTORY = "factory";

    public static final String PROP_NAME = "name";

    public static final String DATA_SOURCE_INFORMATION_REPOSITORY
            = "DataSourceInformationRepository";

    public static final String DEFAULT_IC_FACTORY
            = "com.sun.jndi.rmi.registry.RegistryContextFactory";

    public static final int DEFAULT_PROVIDER_PORT = 2199;

}
