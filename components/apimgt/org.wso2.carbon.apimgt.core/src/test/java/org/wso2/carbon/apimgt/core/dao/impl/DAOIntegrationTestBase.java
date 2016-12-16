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

import bsh.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DAOIntegrationTestBase {
    protected DataSource dataSource;
    String database;
    public DAOIntegrationTestBase() {
         database = System.getenv("DATABASE_TYPE");
        if (StringUtils.isEmpty(database)){
            database = "h2";
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {
        String sqlFilePath = null;
        if ("h2".equals(database)){
            dataSource = new InMemoryDataSource();
            ((InMemoryDataSource) dataSource).resetDB();
            sqlFilePath = ".." + File.separator + ".." + File.separator + ".." + File.separator
                    + "features" + File.separator + "apimgt" + File.separator
                    + "org.wso2.carbon.apimgt.core.feature" + File.separator + "resources"
                    + File.separator + "dbscripts" + File.separator + "h2.sql";
        }else if ("mysql".contains(database)){
            dataSource = new MysqlDataSource();
            ((MysqlDataSource) dataSource).resetDB();
            sqlFilePath = ".." + File.separator + ".." + File.separator + ".." + File.separator
                    + "features" + File.separator + "apimgt" + File.separator
                    + "org.wso2.carbon.apimgt.core.feature" + File.separator + "resources"
                    + File.separator + "dbscripts" + File.separator + "mysql.sql";
        }
        DAOUtil.clearDataSource();
        DAOUtil.initialize(dataSource);
        try (Connection connection = DAOUtil.getConnection()) {
            DBScriptRunnerUtil.executeSQLScript(sqlFilePath, connection);
        }
    }
@AfterClass
    public void tempDBCleanup() throws SQLException, IOException {
        if ("h2".equals(database)){
            ((InMemoryDataSource) dataSource).resetDB();
        }else if ("mysql".contains(database)){
            ((MysqlDataSource) dataSource).resetDB();
        }
    }
}
