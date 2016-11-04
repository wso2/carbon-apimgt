/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.lifecycle.manager.sql.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.LifecycleDatabaseCreator;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.config.LifecycleConfigBuilder;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.constants.Constants;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.exception.LifecycleManagerDatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * This utility class provide methods to handle database connection related operations.
 */
public class LifecycleMgtDBUtil {

    private static final Logger log = LoggerFactory.getLogger(LifecycleMgtDBUtil.class);
    private static volatile DataSource dataSource = null;

    /**
     * Initializes the data source and creates lifecycle database.
     *
     * @throws LifecycleManagerDatabaseException if an error occurs while loading DB configuration
     */
    public static void initialize() throws LifecycleManagerDatabaseException {

        synchronized (LifecycleMgtDBUtil.class) {
            String dataSourceName = LifecycleConfigBuilder.getLifecycleConfig().getDataSourceName();
            if (dataSourceName == null) {
                dataSourceName = Constants.LIFECYCLE_DATASOURCE;
            }

            try {

                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup(dataSourceName);
                LifecycleDatabaseCreator dbInitializer = new LifecycleDatabaseCreator(dataSource);

                dbInitializer.createLifecycleDatabase();
            } catch (NamingException e) {
                throw new LifecycleManagerDatabaseException(
                        "Error while looking up the data " + "source: " + dataSourceName, e);
            }
        }
    }

    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws SQLException if failed to get Connection
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("Data source is not configured properly.");
    }

    /**
     * Utility method to close the connection streams.
     * @param preparedStatement PreparedStatement
     * @param connection Connection
     * @param resultSet ResultSet
     */
    public static void closeAllConnections(PreparedStatement preparedStatement, Connection connection,
            ResultSet resultSet) {
        closeConnection(connection);
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
    }

    /**
     * Close Connection
     * @param dbConnection Connection
     */
    private static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close database connection. Continuing with " + "others. - " + e
                        .getMessage(), e);
            }
        }
    }

    /**
     * Close ResultSet
     * @param resultSet ResultSet
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close ResultSet  - " + e.getMessage(), e);
            }
        }

    }

    /**
     * Close PreparedStatement
     * @param preparedStatement PreparedStatement
     */
    public static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close PreparedStatement. Continuing with" + " others. - " + e
                        .getMessage(), e);
            }
        }

    }

    @SuppressFBWarnings("DM_CONVERT_CASE")
    public static String getConvertedAutoGeneratedColumnName(String dbProductName, String columnName) {
        String autoGeneratedColumnName = columnName;
        if ("PostgreSQL".equals(dbProductName)) {
            autoGeneratedColumnName = columnName.toLowerCase();
            if (log.isDebugEnabled()) {
                log.debug(
                        "Database product name is PostgreSQL. Converting column name " + columnName + " to lowercase ("
                                + autoGeneratedColumnName + ").");
            }
        }

        return autoGeneratedColumnName;
    }

    public static boolean canReturnGeneratedKeys(String dbProductName) {
        return !dbProductName.equals("OpenEdge RDBMS");
    }

}
