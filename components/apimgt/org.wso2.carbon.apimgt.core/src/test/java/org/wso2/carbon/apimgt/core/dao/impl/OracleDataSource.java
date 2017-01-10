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
import java.util.HashMap;
import java.util.Map;

public class OracleDataSource implements DataSource {
    static HikariDataSource basicDataSource = new HikariDataSource();
    static String databaseName = "xe";

    OracleDataSource() throws Exception {
        String ipAddress = TestUtil.getInstance().getIpAddressOfContainer("apim-oracle");
        basicDataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        basicDataSource.setJdbcUrl("jdbc:oracle:thin:@" + ipAddress + ":1521/" + databaseName);
        basicDataSource.setUsername("testamdb");
        basicDataSource.setPassword("testamdb");
        basicDataSource.setAutoCommit(true);
        basicDataSource.setMaximumPoolSize(20);
        basicDataSource.setConnectionTestQuery("SELECT 1 FROM DUAL");
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
        Map<String,String> objectMap = new HashMap<>();
        try (Connection connection = basicDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT object_name, object_type FROM user_objects " +
                    "WHERE object_type IN ('TABLE','VIEW','PACKAGE','PROCEDURE','FUNCTION','SEQUENCE')")) {
                while (resultSet.next()) {
                    objectMap.put(resultSet.getString("object_name"), resultSet.getString("object_type"));
                }
            }
            for (Map.Entry<String,String> entry : objectMap.entrySet()) {
                if ("TABLE".equals(entry.getValue())){
                    statement.addBatch("DROP TABLE "+entry.getKey()+" CASCADE CONSTRAINTS");
                }else{
                    statement.addBatch("DROP "+entry.getValue()+" " + entry.getKey() + "");
                }
            }
            statement.executeBatch();
        }
    }
}
