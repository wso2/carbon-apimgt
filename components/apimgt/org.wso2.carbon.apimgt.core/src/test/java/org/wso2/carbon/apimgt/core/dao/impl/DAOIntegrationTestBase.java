/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DAOIntegrationTestBase {
    protected DataSource dataSource;
    String database;
    private static final String H2 = "h2";
    private static final String MYSQL = "mysql";
    private static final String POSTGRES = "postgres";
    private static final String MSSQL = "mssql";
    private static final String ORACLE = "oracle";

    private static final Logger log = LoggerFactory.getLogger(DAOIntegrationTestBase.class);

    public DAOIntegrationTestBase() {
        database = System.getenv("DATABASE_TYPE");
        if (StringUtils.isEmpty(database)) {
            database = H2;
        }
    }

    @BeforeClass
    public void init() throws Exception {
        // This used to check connection healthy
        if (H2.equals(database)) {
            dataSource = new H2DataSource();
        } else if (MYSQL.contains(database)) {
            dataSource = new MySQLDataSource();
        } else if (POSTGRES.contains(database)) {
            dataSource = new PostgreDataSource();
        } else if (MSSQL.contains(database)) {
            dataSource = new MSSQLDataSource();
        } else if (ORACLE.contains(database)) {
            dataSource = new OracleDataSource();
        }
        int maxRetries = 5;
        long maxWait = 5000;
        while (maxRetries > 0) {
            try (Connection connection = dataSource.getConnection()) {
                log.info("Database Connection Successful");
                break;
            } catch (Exception e) {
                if (maxRetries > 0) {
                    log.warn("Couldn't connect into database retrying after next 5 seconds");
                    maxRetries--;
                    try {
                        Thread.sleep(maxWait);
                    } catch (InterruptedException e1) {
                    }
                } else {
                    log.error("Max tries 5 exceed to connect");
                    throw e;
                }
            }
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {
        String sqlFilePath = null;
        if (H2.equals(database)) {
            ((H2DataSource) dataSource).resetDB();
            sqlFilePath = ".." + File.separator + ".." + File.separator + ".." + File.separator
                    + "features" + File.separator + "apimgt" + File.separator
                    + "org.wso2.carbon.apimgt.core.feature" + File.separator + "resources"
                    + File.separator + "dbscripts" + File.separator + "h2.sql";
        } else if (MYSQL.contains(database)) {
            ((MySQLDataSource) dataSource).resetDB();
            sqlFilePath = ".." + File.separator + ".." + File.separator + ".." + File.separator
                    + "features" + File.separator + "apimgt" + File.separator
                    + "org.wso2.carbon.apimgt.core.feature" + File.separator + "resources"
                    + File.separator + "dbscripts" + File.separator + "mysql.sql";
        } else if (POSTGRES.contains(database)) {
            ((PostgreDataSource) dataSource).resetDB();
            sqlFilePath = ".." + File.separator + ".." + File.separator + ".." + File.separator
                    + "features" + File.separator + "apimgt" + File.separator
                    + "org.wso2.carbon.apimgt.core.feature" + File.separator + "resources"
                    + File.separator + "dbscripts" + File.separator + "postgres.sql";
        } else if (MSSQL.contains(database)) {
            ((MSSQLDataSource) dataSource).resetDB();
            sqlFilePath = ".." + File.separator + ".." + File.separator + ".." + File.separator
                    + "features" + File.separator + "apimgt" + File.separator
                    + "org.wso2.carbon.apimgt.core.feature" + File.separator + "resources"
                    + File.separator + "dbscripts" + File.separator + "mssql.sql";
        } else if (ORACLE.contains(database)) {
            ((OracleDataSource) dataSource).resetDB();
            sqlFilePath = ".." + File.separator + ".." + File.separator + ".." + File.separator
                    + "features" + File.separator + "apimgt" + File.separator
                    + "org.wso2.carbon.apimgt.core.feature" + File.separator + "resources"
                    + File.separator + "dbscripts" + File.separator + "oracle.sql";
        }
        DAOUtil.clearDataSource();
        DAOUtil.initialize(dataSource);
        try (Connection connection = DAOUtil.getConnection()) {
            DBScriptRunnerUtil.executeSQLScript(sqlFilePath, connection);
        }
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        SampleTestObjectCreator.createDefaultPolicy(policyDAO);
    }

    @AfterClass
    public void tempDBCleanup() throws SQLException, IOException {
        if (H2.equals(database)) {
            ((H2DataSource) dataSource).resetDB();
        } else if (MYSQL.contains(database)) {
            ((MySQLDataSource) dataSource).resetDB();
        } else if (MSSQL.contains(database)) {
            ((MSSQLDataSource) dataSource).resetDB();
        } else if (POSTGRES.contains(database)) {
            ((PostgreDataSource) dataSource).resetDB();
        } else if (ORACLE.contains(database)) {
            ((OracleDataSource) dataSource).resetDB();
        }
    }
}
