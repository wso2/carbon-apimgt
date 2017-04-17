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

public class MSSQLDataSource implements DataSource {
    static HikariDataSource basicDataSource = new HikariDataSource();
    static String databaseName = "testamdb";

    MSSQLDataSource() throws Exception {
        String ipAddress = TestUtil.getInstance().getIpAddressOfContainer();
        basicDataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        basicDataSource.setJdbcUrl("jdbc:sqlserver://" + ipAddress + ":" + System.getenv("PORT") + ";" +
                "databaseName=testamdb");
        basicDataSource.setUsername("SA");
        basicDataSource.setPassword("wso2apim123#");
        basicDataSource.setAutoCommit(true);
        basicDataSource.setMaximumPoolSize(20);
        basicDataSource.setConnectionTestQuery("SELECT 1");
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
     * Return javax.sql.DataSource object
     *
     * @return {@link javax.sql.DataSource} object
     */
    @Override
    public HikariDataSource getDatasource() throws SQLException {
        return basicDataSource;
    }

    public void resetDB() throws SQLException {
        List<String> tables = new ArrayList<>();
        try (Connection connection = basicDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT table_name as TABLE_NAME FROM " +
                    "information_schema.tables WHERE TABLE_TYPE='BASE TABLE' AND TABLE_CATALOG='" + databaseName +
                    "'")) {
                while (resultSet.next()) {
                    tables.add(resultSet.getString("TABLE_NAME"));
                }
            }
            for (String table : tables) {
                // drop full text indexes
                try (ResultSet resultSet = statement.executeQuery("SELECT object_id, property_list_id, stoplist_id " +
                        "FROM sys.fulltext_indexes where object_id = object_id('" + table + "')")) {
                    if (resultSet.next()) {
                        statement.addBatch("DROP FULLTEXT INDEX ON " + table);
                    }
                }
                // drop full text catalogues
                try (ResultSet resultSet = statement.executeQuery("SELECT fts.name as name FROM sys.fulltext_indexes " +
                        "as fis, sys.fulltext_catalogs as fts where object_id = object_id('" + table + "') and fis" +
                        ".fulltext_catalog_id=fts.fulltext_catalog_id")) {
                    while (resultSet.next()) {
                        statement.addBatch("DROP FULLTEXT CATALOG " + resultSet.getString("name"));
                    }
                }
                try (ResultSet resultSet = statement.executeQuery("SELECT name as ForeignKey_Name,OBJECT_SCHEMA_NAME" +
                        "(parent_object_id) as Schema_Name,OBJECT_NAME(parent_object_id) as Table_Name FROM sys" +
                        ".foreign_keys " +
                        "WHERE referenced_object_id = object_id('" + table + "')")) {
                    while (resultSet.next()) {
                        try (Statement statement1 = connection.createStatement()) {
                            statement1.execute("ALTER TABLE " + resultSet.getString("Schema_Name") + "." +
                                    resultSet.getString("Table_Name") + " DROP CONSTRAINT " +
                                    resultSet.getString("ForeignKey_Name"));
                        }
                    }
                    statement.addBatch("DROP TABLE " + table);
                }
            }
            statement.executeBatch();
        }
    }
}
