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

public class PostgreDataSource implements DataSource {
    static HikariDataSource basicDataSource = new HikariDataSource();
    static String databaseName = "testamdb";


    PostgreDataSource() throws Exception {
        String ipAddress = TestUtil.getInstance().getIpAddressOfContainer();
        basicDataSource.setDriverClassName("org.postgresql.Driver");
        basicDataSource.setJdbcUrl("jdbc:postgresql://" + ipAddress + ":" + System.getenv("PORT") + "/" + databaseName);
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

    /**
     * Return Hikari Datasource
     *
     * @return {@link HikariDataSource} object
     */
    @Override
    public HikariDataSource getDatasource() throws SQLException {
        return basicDataSource;
    }

    public void resetDB() throws SQLException {
        List<String> listOfTables = new ArrayList();
        List<String> listOfSequences = new ArrayList<>();
        try (Connection connection = basicDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT table_name as TABLE_NAME FROM " +
                    "information_schema.tables WHERE table_type = 'BASE TABLE' AND table_catalog='" + databaseName +
                    "' AND table_schema='public'")) {
                while (resultSet.next()) {
                    listOfTables.add(resultSet.getString("TABLE_NAME"));
                }
            }
            try (ResultSet resultSet = statement.executeQuery("SELECT sequence_name as SEQUENCE FROM " +
                    "information_schema.sequences WHERE sequence_catalog='" + databaseName + "' AND " +
                    "sequence_schema='public'")) {
                while (resultSet.next()) {
                    listOfSequences.add(resultSet.getString("SEQUENCE"));
                }
            }
            for (int i = 0; i < listOfTables.size(); i++) {
                statement.addBatch("DROP TABLE " + listOfTables.get(i) + " CASCADE");
            }
            for (int i = 0; i < listOfSequences.size(); i++) {
                statement.addBatch("DROP SEQUENCE " + listOfSequences.get(i) + " CASCADE");
            }
            statement.executeBatch();
        }
    }
}
