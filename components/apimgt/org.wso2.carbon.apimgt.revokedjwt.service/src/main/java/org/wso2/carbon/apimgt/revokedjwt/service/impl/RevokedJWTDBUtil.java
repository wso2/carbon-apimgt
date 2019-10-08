/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.revokedjwt.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.wso2.carbon.apimgt.revokedjwt.service.dto.RevokedJWTDTO;
import org.wso2.carbon.apimgt.revokedjwt.service.dto.RevokedJWTListDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * DB util class to fetch revoked JWTs
 */
public final class RevokedJWTDBUtil {
    private static final Log log = LogFactory.getLog(RevokedJWTDBUtil.class);

    private static volatile DataSource dataSource = null;
    private  static volatile RevokedJWTDTO[] revokedJWTDTOS = null;
    private static volatile Set<String> throttledEvents ;
    private static long lastAccessed;
    private static long timeBetweenUpdates = 10000;

    public static void initialize() throws Exception {

        if (dataSource != null) {
            return;
        }
        Properties properties = new Properties();
        properties.load(new ClassPathResource("../revokedjwt.properties").getInputStream());
        String dataSourceName = (String) properties.get("revokedjwt.datasource.name");
        synchronized (RevokedJWTDBUtil.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source");
                }

                if (dataSourceName != null) {
                    try {
                        Context ctx = new InitialContext();
                        dataSource = (DataSource) ctx.lookup(dataSourceName);
                    } catch (NamingException e) {
                        throw new Exception("Error while looking up the data " +
                                "source: " + dataSourceName, e);
                    }
                }
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
        } else {
            try {
                initialize();
                return dataSource.getConnection();

            } catch (Exception e) {
                throw new SQLException("Data source is not configured properly.",e);
            }
        }
    }

    /**
     * Fetches all revoked JWTs from DB.
     * @return
     */
    public static RevokedJWTListDTO getRevokedJWTs() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        RevokedJWTListDTO revokedJWTListDTO = new RevokedJWTListDTO();
        String sqlQuery = "select SIGNATURE,EXPIRY_TIMESTAMP from AM_REVOKED_JWT";
        try {
            conn = RevokedJWTDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            rs = ps.executeQuery();
            while (rs.next()) {
                String signature = rs.getString("SIGNATURE");
                Long expiryTimestamp = rs.getLong("EXPIRY_TIMESTAMP");
                RevokedJWTDTO revokedJWTDTO = new RevokedJWTDTO();
                revokedJWTDTO.setJwtSignature(signature);
                revokedJWTDTO.setExpiryTime(expiryTimestamp);
                revokedJWTListDTO.add(revokedJWTDTO);
            }
        } catch (SQLException e) {
            log.error("Error while executing SQL", e);
        } finally {
            closeAllConnections(ps, conn, rs);
        }
        return revokedJWTListDTO;
    }

    /**
     *  Closes all connections.
     * @param preparedStatement prepared statement to be closed.
     * @param connection connection to be closed.
     * @param resultSet resultset to be closed.
     */
    public static void closeAllConnections(PreparedStatement preparedStatement, Connection connection,
                                           ResultSet resultSet) {
        closeConnection(connection);
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
    }


    /**
     * Close Connection
     *
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
     *
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
     *
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
