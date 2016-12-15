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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MysqlDataSource implements DataSource{
   static HikariDataSource basicDataSource = new HikariDataSource();

    MysqlDataSource() throws SQLException {
        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/testamdb");
        basicDataSource.setUsername("root");
        basicDataSource.setPassword("root");
        basicDataSource.setAutoCommit(false);
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
        List<String> listOfTables = new ArrayList();
        try (Connection connection = basicDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT table_name as TABLE_NAME FROM " +
                    "information_schema.tables WHERE table_type = 'base table' AND table_schema='amdb'")) {
                while (resultSet.next()) {
                    listOfTables.add(resultSet.getString("TABLE_NAME"));
                }
            }
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            for(int i = 0; i<listOfTables.size();i++){
                statement.addBatch("DROP TABLE " + listOfTables.get(i));
            }
            statement.executeBatch();
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");

            connection.commit();
        }
    }
}
