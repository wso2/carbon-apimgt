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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OracleDataSource implements DataSource {
    static HikariDataSource basicDataSource = new HikariDataSource();
    static String databaseName = "xe";

    OracleDataSource() throws Exception {
        String ipAddress = TestUtil.getInstance().getIpAddressOfContainer();
        basicDataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        basicDataSource.setJdbcUrl("jdbc:oracle:thin:@" + ipAddress + ":" + System.getenv("PORT") + "/" + databaseName);
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
        Map<String, String> objectMap = new HashMap<>();
        try (Connection connection = basicDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try {
                CallableStatement callableStatement = connection.prepareCall("SELECT PRE_NAME FROM CTXSYS" +
                        ".CTX_PREFERENCES WHERE PRE_OBJECT='MULTI_COLUMN_DATASTORE' AND PRE_OWNER = 'TESTAMDB'");
                ResultSet resultSet = callableStatement.executeQuery();
                while (resultSet.next()) {
                    statement.execute("BEGIN\nctx_ddl.drop_preference('" + resultSet.getString("PRE_NAME") + "');" +
                            "\nEND;");
                }
            } catch (SQLException e) {
                throw e;
            }

            List<String> fullTestIndexes = new ArrayList<>();

            try (ResultSet rs = statement.executeQuery("SELECT INDEX_NAME FROM DBA_INDEXES WHERE" +
                    " ITYP_OWNER = 'CTXSYS' AND ITYP_NAME IN ('CONTEXT', 'CTXCAT', 'CTXRULE', 'CTXXPATH')")) {
                while (rs.next()) {
                    fullTestIndexes.add(rs.getString("INDEX_NAME"));
                }
            }

            for (String fullTextIndex : fullTestIndexes) {
                statement.execute("drop index " + fullTextIndex + " FORCE");
            }

            List<String> sequences = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery("SELECT sequence_name FROM USER_SEQUENCES")) {
                while (rs.next()) {
                    sequences.add(rs.getString("sequence_name"));
                }
            }

            List<String> tables = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery("SELECT table_name FROM USER_TABLES")) {
                while (rs.next()) {
                    tables.add(rs.getString("table_name"));
                }
            }

            try {
                for (String sequence : sequences) {
                    statement.execute("DROP SEQUENCE " + sequence);
                }

                for (String table : tables) {
                    statement.execute("DROP TABLE " + table + " CASCADE CONSTRAINTS");
                }
            } catch (SQLException e) {
                e.getErrorCode();
            }

        }
    }
}
