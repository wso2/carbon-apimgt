/*
 *
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
 *
 */
package org.wso2.carbon.apimgt.core.dao.impl;


import com.zaxxer.hikari.HikariDataSource;
import org.wso2.carbon.apimgt.core.TestUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySQLDataSource implements DataSource {
    static HikariDataSource basicDataSource = new HikariDataSource();
    static String databaseName = "testamdb";


    MySQLDataSource() throws Exception {
        String ipAddress = TestUtil.getInstance().getIpAddressOfContainer("apim-mysql");
        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setJdbcUrl("jdbc:mysql://" + ipAddress + ":3306/" + databaseName);
        basicDataSource.setUsername("root");
        basicDataSource.setPassword("root");
        basicDataSource.setAutoCommit(true);
        basicDataSource.setMaximumPoolSize(20);
    }

    /**
     * Get a {@link Connection} object
     *
     * @return {@link Connection} from given DataSource
     */
    @Override
    public Connection getConnection() throws SQLException {
        return basicDataSource.getConnection();
    }

    public void resetDB() throws SQLException {
        List<String> tables = new ArrayList<>();
        try (Connection connection = basicDataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                connection.setAutoCommit(false);
                try (ResultSet resultSet = statement.executeQuery("SELECT table_name as TABLE_NAME FROM " +
                        "information_schema.tables WHERE table_type = 'base table' AND table_schema='" + databaseName
                        + '\'')) {
                    while (resultSet.next()) {
                        tables.add(resultSet.getString("TABLE_NAME"));
                    }
                }
                statement.execute("SET FOREIGN_KEY_CHECKS = 0");
                for (String table : tables) {
                    statement.addBatch("DROP TABLE " + table);
                }
                statement.executeBatch();
                statement.execute("SET FOREIGN_KEY_CHECKS = 1");
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
            }
        }
    }
}
