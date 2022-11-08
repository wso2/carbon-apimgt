/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtDAOException;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.concurrent.Semaphore;

/**
 * Implementation for SystemConfigurationDAO
 */
public class SystemConfigurationsDAO {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigurationsDAO.class);
    private static Semaphore semaphore = new Semaphore(1);
    private static SystemConfigurationsDAO INSTANCE = null;

    /**
     * A Semaphore object which acts as a lock for doing thread safe invocations of the DAO
     *
     * @return static Semaphore object
     */
    public static Semaphore getLock() {
        return semaphore;
    }

    /**
     * Method to get the instance of the ApiMgtDAO.
     *
     * @return {@link ApiMgtDAO} instance
     */
    public static SystemConfigurationsDAO getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new SystemConfigurationsDAO();
        }

        return INSTANCE;
    }

    /**
     * Add System Configuration
     *
     * @param organization  Organization
     * @param type  Config Type
     * @param config  Configuration to be added
     */
    public void addSystemConfig(String organization, String type, String config) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SQLConstants.SystemConfigsConstants.ADD_SYSTEM_CONFIG_SQL)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, organization);
                statement.setString(2, type);
                statement.setBinaryStream(3, new ByteArrayInputStream(config.getBytes()));
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                handleConnectionRollBack(connection);
                throw e;
            }
        } catch (SQLException e) {
            handleException("Failed to add " + type + " Configuration for org: " + organization, e);
        }
    }

    /**
     * Retrieve System Configuration
     *
     * @param organization  Organization
     * @param type  Config Type
     * @return System Configuration
     */
    public String getSystemConfig(String organization, String type) throws APIManagementException {

        ResultSet rs;
        String systemConfig = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SQLConstants.SystemConfigsConstants.GET_SYSTEM_CONFIG_SQL)) {
            statement.setString(1, organization);
            statement.setString(2, type);
            rs = statement.executeQuery();
            if (rs.next()) {
                InputStream systemConfigBlob = rs.getBinaryStream("CONFIGURATION");
                if (systemConfigBlob != null) {
                    systemConfig = APIMgtDBUtil.getStringFromInputStream(systemConfigBlob);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve " + type + " Configuration for org: " + organization, e);
        }
        return systemConfig;
    }

    /**
     * Update System Configuration
     *
     * @param organization  Organization
     * @param type  Config Type
     * @param config  Configuration to be updated
     */
    public void updateSystemConfig(String organization, String type, String config) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SQLConstants.SystemConfigsConstants.UPDATE_SYSTEM_CONFIG_SQL)) {
            try {
                connection.setAutoCommit(false);
                statement.setBinaryStream(1, new ByteArrayInputStream(config.getBytes()));
                statement.setString(2, organization);
                statement.setString(3, type);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                handleConnectionRollBack(connection);
                throw e;
            }
        } catch (SQLException e) {
            handleException("Failed to update " + type + " Configuration for org: " + organization, e);
        }
    }

    /**
     * Method to handle the SQL Exception.
     *
     * @param message : Error message.
     * @param e       : Throwable cause.
     * @throws APIMgtDAOException :
     */
    private void handleException(String message, Throwable e) throws APIMgtDAOException {

        throw new APIMgtDAOException(message, e);
    }

    /**
     * This method handles the connection roll back.
     *
     * @param connection Relevant database connection that need to be rolled back.
     */
    private void handleConnectionRollBack(Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            } else {
                log.warn("Could not perform rollback since the connection is null.");
            }
        } catch (SQLException e1) {
            log.error("Error while rolling back the transaction.", e1);
        }
    }
}
