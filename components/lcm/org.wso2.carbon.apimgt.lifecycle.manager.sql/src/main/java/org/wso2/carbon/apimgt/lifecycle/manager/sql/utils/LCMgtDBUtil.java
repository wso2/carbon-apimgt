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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.JDBCPersistenceManager;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.constants.Constants;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.exception.LCManagerDatabaseException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class LCMgtDBUtil {

    private static final Log log = LogFactory.getLog(LCMgtDBUtil.class);

    /**
     * Initializes the data source
     *
     * @throws LCManagerDatabaseException if an error occurs while loading DB configuration
     */
    public static void initialize() throws LCManagerDatabaseException {

        synchronized (LCMgtDBUtil.class) {
            JDBCPersistenceManager jdbcPersistenceManager;
            try {
                jdbcPersistenceManager = JDBCPersistenceManager
                        .getInstance();
                jdbcPersistenceManager.initializeDatabase();
            } catch (Exception e) {
                String msg = "Error in creating the Lifecycle database";
                log.fatal(msg,e);
                throw new LCManagerDatabaseException(msg, e);
            }
        }
    }


    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws SQLException if failed to get Connection
     * @throws DataSourceException
     */
    public static Connection getConnection() throws SQLException, DataSourceException {
        Connection conn;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(Constants.SUPER_TENANT_ID);
            privilegedCarbonContext.setTenantDomain(Constants.SUPER_TENANT_DOMAIN);
            CarbonDataSource carbonDataSource = DataSourceManager.getInstance().getDataSourceRepository()
                    .getDataSource(Constants.LIFECYCLE_DB_NAME);
            DataSource dataSource = (DataSource) carbonDataSource.getDSObject();
            conn = dataSource.getConnection();
            return conn;
        } catch (SQLException e) {
            log.error("Can't create JDBC connection to the SQL Server", e);
            throw e;
        } catch (DataSourceException e) {
            log.error("Can't create data source for SQL Server", e);
            throw e;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
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
                log.warn("Database error. Could not close database connection. Continuing with " +
                        "others. - " + e.getMessage(), e);
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
                log.warn("Database error. Could not close PreparedStatement. Continuing with" +
                        " others. - " + e.getMessage(), e);
            }
        }

    }


}
