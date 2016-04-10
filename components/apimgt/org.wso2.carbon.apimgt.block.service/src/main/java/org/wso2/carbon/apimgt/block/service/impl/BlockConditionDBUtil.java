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

package org.wso2.carbon.apimgt.block.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.block.service.dto.BlockConditionsDTO;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BlockConditionDBUtil {

    private static final Log log = LogFactory.getLog(BlockConditionDBUtil.class);

    private static volatile DataSource dataSource = null;
    private  static BlockConditionsDTO blockConditionsDTO = null;
    private static long lastAccessed;
    private static long timeBetweenUpdates = 10000;

    public static void initialize() throws Exception {
        if (dataSource != null) {
            return;
        }

        synchronized (BlockConditionDBUtil.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source");
                }
                String dataSourceName = "jdbc/WSO2AM_DB";

                if (dataSourceName != null) {
                    try {
                        Context ctx = new InitialContext();
                        dataSource = (DataSource) ctx.lookup(dataSourceName);
                        ExecutorService executor = Executors.newFixedThreadPool(1);
                            Runnable worker = new WorkerThread("");
                            executor.execute(worker);
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

    public static BlockConditionsDTO getBlockConditions() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List api = new ArrayList();
        List application = new ArrayList();
        List ip = new ArrayList();
        String sqlQuery = "select * from AM_BLOCK_CONDITIONS";
        try {
            conn = BlockConditionDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            rs = ps.executeQuery();
            while (rs.next()) {
                String type = rs.getString("TYPE");
                String value = rs.getString("VALUE");
                String enabled = rs.getString("ENABLED");
                String tenantDomain = rs.getString("DOMAIN");
                if (Boolean.parseBoolean(enabled)) {
                    if ("API".equals(type)) {
                        api.add(value);
                    } else if ("APPLICATION".equals(type)) {
                        application.add(value);
                    } else if ("IP".equals(type)) {
                        ip.add(tenantDomain+":"+value);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error while executing SQL", e);
        } finally {
            BlockConditionDBUtil.closeAllConnections(ps, conn, rs);
        }
        BlockConditionDBUtil.blockConditionsDTO = new BlockConditionsDTO();
        blockConditionsDTO.setApi(api);
        blockConditionsDTO.setApplication(application);
        blockConditionsDTO.setIp(ip);
        return  blockConditionsDTO;
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


    /**
     * TODO This is not final implementation. This need to be revised and fix issues
     * related to memory leaks caused due to web app stopping.
     */
    private static class WorkerThread implements Runnable {
        private String command;
        public WorkerThread(String s){
            this.command=s;
        }
        @Override
        public void run() {
            if(lastAccessed <1){
                lastAccessed = System.currentTimeMillis();
            }
            while(true) {
                if (System.currentTimeMillis() - lastAccessed >= timeBetweenUpdates) {
                    System.out.println("DB reading Started. Reading database");
                    lastAccessed = System.currentTimeMillis();
                    processCommand();
                    System.out.println("DB reading End.Time taken = "+ (System.currentTimeMillis() - lastAccessed) );
                    try {
                        Thread.sleep(timeBetweenUpdates);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void processCommand() {
                BlockConditionDBUtil.getBlockConditions();
        }
        @Override
        public String toString(){
            return this.command;
        }
    }

    public static BlockConditionsDTO getBlockConditionsDTO() {
        if (blockConditionsDTO == null) {
            getBlockConditions();
            System.out.println("block Conditions are null" );
        }
        return blockConditionsDTO;
    }
}
