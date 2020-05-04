/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.internal.service.dto.ThrottledEventDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class ThrottlingDBUtil {

    private static final Log log = LogFactory.getLog(ThrottlingDBUtil.class);

    private static volatile DataSource dataSource = null;
    private  static volatile ThrottledEventDTO[] throttledEventDTOs = null;
    private static volatile Set<String> throttledEvents ;
    private static long lastAccessed;
    private static long timeBetweenUpdates = 10000;

    public static void initialize() throws Exception {
        //Stop the initialization as this service currently no longer used
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
        else {
            try {
                initialize();
                return dataSource.getConnection();

            } catch (Exception e) {
                throw new SQLException("Data source is not configured properly.");
            }
        }

    }

    public static ThrottledEventDTO[] getThrottledEvents(String query) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ThrottledEventDTO> throttledEventDTOList = new ArrayList<ThrottledEventDTO>();
        String sqlQuery = "select THROTTLEKEY from ThrottleTable";
        try {
            conn = ThrottlingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            rs = ps.executeQuery();
            while (rs.next()) {
                String throttleKey = rs.getString("THROTTLEKEY");
                ThrottledEventDTO throttledEventDTO = new ThrottledEventDTO();
                throttledEventDTO.setThrottleKey(throttleKey);
                throttledEventDTOList.add(throttledEventDTO);
            }
            int count;
            if(query != null && query.length()> 0){
                count =  Integer.parseInt(query);
                for (int i = 0; i < count; i++ ){
                    ThrottledEventDTO throttledEventDTO = new ThrottledEventDTO();
                    throttledEventDTO.setThrottleKey("key_"+i);
                    throttledEventDTOList.add(throttledEventDTO);
                }
            }

            throttledEventDTOs = throttledEventDTOList.toArray(new ThrottledEventDTO[throttledEventDTOList.size()]);
        } catch (SQLException e) {
            log.error("Error while executing SQL", e);
        } finally {
            ThrottlingDBUtil.closeAllConnections(ps, conn, rs);
        }
        return throttledEventDTOs;
    }


    public static Set<String> getThrottledEventsAsString(String query) {
        if (throttledEvents != null && !throttledEvents.isEmpty()) {
            return throttledEvents;
        } else {
            return getThrottledEventsAsStringFromDB(query);
        }
    }


    public static Set<String> getThrottledEventsAsStringFromDB(String query) {
            Set<String> throttledEventString = new HashSet<>();
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            String sqlQuery = "select THROTTLEKEY from ThrottleTable";
            try {
                conn = ThrottlingDBUtil.getConnection();
                ps = conn.prepareStatement(sqlQuery);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String throttleKey = rs.getString("THROTTLEKEY");
                    throttledEventString.add(throttleKey);
                }
                int count = 1;
            } catch (SQLException e) {
                log.error("Error while executing SQL", e);
            } finally {
                ThrottlingDBUtil.closeAllConnections(ps, conn, rs);
            }
            throttledEvents = throttledEventString;
            //log.info("================================updated String"+throttledEventString);
            return throttledEventString;
    }


    public static ThrottledEventDTO isThrottled(String query) {
        ThrottledEventDTO throttledEventDTO = new ThrottledEventDTO();
        throttledEventDTO.setThrottleState("ALLOWED");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = "select THROTTLEKEY from ThrottleTable WHERE THROTTLEKEY = ? ";
        try {
            conn = ThrottlingDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, query);
            rs = ps.executeQuery();
            while (rs.next()) {
                String throttleKey = rs.getString("THROTTLEKEY");
                throttledEventDTO.setThrottleState("THROTTLED");
            }
        } catch (SQLException e) {
            log.error("Error while executing SQL", e);
        } finally {
            ThrottlingDBUtil.closeAllConnections(ps, conn, rs);
        }
        return throttledEventDTO;
    }


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
