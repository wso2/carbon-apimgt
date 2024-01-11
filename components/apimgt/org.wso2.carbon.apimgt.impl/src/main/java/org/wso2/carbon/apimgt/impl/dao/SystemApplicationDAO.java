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

package org.wso2.carbon.apimgt.impl.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.SystemApplicationDTO;
import org.wso2.carbon.apimgt.api.APIMgtDAOException;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

/**
 * Implementation for SystemApplicationDAO
 */
public class SystemApplicationDAO {

    private static final Logger log = LoggerFactory.getLogger(SystemApplicationDAO.class);
    private static final String SYSTEM_APP_TABLE_NAME = "AM_SYSTEM_APPS";
    private static Semaphore semaphore = new Semaphore(1);

    /**
     * A Semaphore object which acts as a lock for doing thread safe invocations of the DAO
     *
     * @return static Semaphore object
     */
    public static Semaphore getLock() {
        return semaphore;
    }

    /**
     * Checks whether the system application table exists in the database.
     *
     * @return : True if exists, false otherwise.
     */
    public boolean isTableExists() throws APIMgtDAOException {

        boolean isExists = false;
        Connection connection = null;
        ResultSet resultSet = null;
        DatabaseMetaData databaseMetaData;

        try {
            connection = APIMgtDBUtil.getConnection();
            databaseMetaData = connection.getMetaData();

            resultSet = databaseMetaData.getTables(null, null, SYSTEM_APP_TABLE_NAME, null);
            if (resultSet.next()) {
                isExists = true;
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving database information. ", e);
            }
            handleException("Error retrieving Database information", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, connection, resultSet);
        }
        return isExists;
    }

    /**
     * Method to add application key to the AM_SYSTEM_APPS table
     *
     * @param appName required parameter
     * @param consumerKey required parameter
     * @param consumerSecret required parameter
     * @return boolean
     * @throws APIMgtDAOException
     */
    public boolean addApplicationKey(String appName, String consumerKey, String consumerSecret,String tenantDomain)
            throws APIMgtDAOException {
        boolean result = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String addCertQuery = SQLConstants.SystemApplicationConstants.INSERT_SYSTEM_APPLICATION;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(addCertQuery);
            preparedStatement.setString(1, appName);
            preparedStatement.setString(2, consumerKey);
            preparedStatement.setString(3, consumerSecret);
            preparedStatement.setString(4, tenantDomain);
            preparedStatement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()),
                    Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            result = preparedStatement.executeUpdate() >= 1;
            connection.commit();

        } catch (SQLException e) {
            handleConnectionRollBack(connection);
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while adding client credentials to SYSTEM_APPS table ", e);
            }
            handleException("Error while persisting client credentials to SYSTEM_APPS table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
        return result;
    }

    /**
     * Method to retrieve all the system Applications for the given tenant
     *
     * @param tenantDomain required parameter
     * @return SystemApplicationDTO which hold the retrieved client credentials
     * @throws APIMgtDAOException
     */
    public SystemApplicationDTO[] getApplications(String tenantDomain)
            throws APIMgtDAOException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        SystemApplicationDTO systemApplicationDTO = null;
        List<SystemApplicationDTO> systemApplicationDTOS = new ArrayList<>();
        String getCredentialsQuery = SQLConstants.SystemApplicationConstants.GET_APPLICATIONS;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            connection.commit();
            preparedStatement = connection.prepareStatement(getCredentialsQuery);
            preparedStatement.setString(1, tenantDomain);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                systemApplicationDTO = new SystemApplicationDTO();
                systemApplicationDTO.setName(resultSet.getString("NAME"));
                systemApplicationDTO.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                systemApplicationDTO.setConsumerSecret(resultSet.getString("CONSUMER_SECRET"));
                systemApplicationDTOS.add(systemApplicationDTO);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving system applications for tenant: " + tenantDomain);
            }
            handleException("Error while retrieving system applications for tenant: " + tenantDomain, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return systemApplicationDTOS.toArray(new SystemApplicationDTO[systemApplicationDTOS.size()]);
    }

    /**
     * Method to retrieve client credentials for a given application name
     *
     * @param appName required parameter
     * @return SystemApplicationDTO which hold the retrieved client credentials
     * @throws APIMgtDAOException
     */
    public SystemApplicationDTO getClientCredentialsForApplication(String appName, String tenantDomain)
            throws APIMgtDAOException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        SystemApplicationDTO systemApplicationDTO = null;
        String getCredentialsQuery = SQLConstants.SystemApplicationConstants.GET_CLIENT_CREDENTIALS_FOR_APPLICATION;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            connection.commit();
            preparedStatement = connection.prepareStatement(getCredentialsQuery);
            preparedStatement.setString(1, appName);
            preparedStatement.setString(2, tenantDomain);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                systemApplicationDTO = new SystemApplicationDTO();
                systemApplicationDTO.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                systemApplicationDTO.setConsumerSecret(resultSet.getString("CONSUMER_SECRET"));
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving client credentials for application: " + appName);
            }
            handleException("Error while retrieving client credentials for application: " + appName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return systemApplicationDTO;
    }

    /**
     * Method to remove client credentials for given application
     *
     * @param appName required parameter
     * @return boolean
     * @throws APIMgtDAOException
     */
    public boolean removeConsumerKeyForApplication(String appName, String tenantDomain) throws APIMgtDAOException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean result = false;
        String deleteApplicationKeyQuery = SQLConstants.SystemApplicationConstants.DELETE_SYSTEM_APPLICATION;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(deleteApplicationKeyQuery);
            preparedStatement.setString(1, appName);
            preparedStatement.setString(2, tenantDomain);
            result = preparedStatement.executeUpdate() == 1;
            connection.commit();
        } catch (SQLException e) {
            handleConnectionRollBack(connection);
            handleException("Error while deleting System Application. ", e);
        } finally {
            APIMgtDBUtil.closeStatement(preparedStatement);
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
        return result;
    }

    /**
     * Method to check whether client credentials exists for a given application
     *
     * @param appName required parameter
     * @return boolean
     * @throws APIMgtDAOException
     */
    public boolean isClientCredentialsExistForApplication(String appName, String tenantDomain)
            throws APIMgtDAOException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean result = false;
        String checkClientCredentialsExistsQuery = SQLConstants.SystemApplicationConstants.CHECK_CLIENT_CREDENTIALS_EXISTS;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(checkClientCredentialsExistsQuery);
            preparedStatement.setString(1, appName);
            preparedStatement.setString(2, tenantDomain);
            result = preparedStatement.executeUpdate() == 1;
            connection.commit();
        } catch (SQLException e) {
            handleConnectionRollBack(connection);
            handleException("Error while checking for System Application. ", e);
        } finally {
            APIMgtDBUtil.closeStatement(preparedStatement);
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
        return result;
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

    /**
     * Method to check whether PKCE is enabled for the application identified by the consumer key
     * @param consumerKey consumer key of the sysetm app
     * @return true is PKCE is enabled, false otherwise
     * @throws APIMgtDAOException
     */
    public static boolean isPKCEEnabled(String consumerKey) throws APIMgtDAOException {
        boolean isPKCEMandatory = false;
        String sql = "SELECT PKCE_MANDATORY FROM IDN_OAUTH_CONSUMER_APPS WHERE CONSUMER_KEY = ? ";

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1,  consumerKey);
                try (ResultSet result = preparedStatement.executeQuery()) {
                    if (result.next()) {
                        isPKCEMandatory = result.getBoolean("PKCE_MANDATORY");
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error while checking for whether PKCE is enabled for System Application by " +
                    "consumer key: " + consumerKey, e);
        }
        return isPKCEMandatory;
    }

    /**
     * Method to check whether BypassClientCredentials is enabled for the application identified by the
     * consumerkey.
     *
     * @param consumerKey consumer key of the system app
     * @return true is Bypass Client Credentials is enabled, false otherwise
     * @throws APIMgtDAOException
     */
    public static boolean isBypassClientCredentials(String consumerKey) throws APIMgtDAOException {
        boolean bypassClientCredentials = false;
        String sql = SQLConstants.SystemApplicationConstants.GET_BYPASS_CLIENT_CREDENTIALS_ENABLED;

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, consumerKey);
                preparedStatement.setString(2, "bypassClientCredentials");
                try (ResultSet result = preparedStatement.executeQuery()) {
                    if (result.next()) {
                        bypassClientCredentials = result.getBoolean("PROPERTY_VALUE");
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error while checking for whether BypassClientCredentials "
                    + "is enabled for System Application by consumer key: " + consumerKey, e);
        }
        return bypassClientCredentials;
    }
}
