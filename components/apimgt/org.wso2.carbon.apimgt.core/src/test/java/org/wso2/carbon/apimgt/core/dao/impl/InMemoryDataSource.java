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
import org.h2.jdbcx.JdbcDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * Implements DataSource interface which supports in memory h2 DB with a reusable single connection
 */
public class InMemoryDataSource implements DataSource {
    static HikariDataSource dataSource = new HikariDataSource();


    InMemoryDataSource() throws SQLException {
        dataSource.setJdbcUrl("jdbc:h2:src/test/resources/amdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setAutoCommit(false);
    }

    /**
     * Get a {@link Connection} object
     *
     * @return {@link Connection} from given DataSource
     */
    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void resetDB() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS DELETE FILES");
        }
    }
}
