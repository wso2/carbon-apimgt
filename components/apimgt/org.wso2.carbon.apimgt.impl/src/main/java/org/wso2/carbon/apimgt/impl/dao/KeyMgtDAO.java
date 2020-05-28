/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is responsible for performing the DAO operations related to key management of the registered
 * authorization servers.
 */
public class KeyMgtDAO {

    private static final Logger log = LoggerFactory.getLogger(KeyMgtDAO.class);

    private KeyMgtDAO() {

    }

    private static class KMAppDAOInstanceHolder {

        private static final KeyMgtDAO INSTANCE = new KeyMgtDAO();
    }

    public static KeyMgtDAO getInstance() {

        return KMAppDAOInstanceHolder.INSTANCE;
    }

    /**
     * Add KM application info.
     *
     * @param clientId     Client Id
     * @param clientSecret Client Secret
     * @param tenantId     Tenant Id
     * @throws APIManagementException If an error occurs while adding application
     */
    public void addApplication(String clientId, String clientSecret, int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.KeyMgtConstants.ADD_KM_APPLICATION)) {
            try {
                connection.setAutoCommit(false);
                preparedStatement.setString(1, clientId);
                preparedStatement.setString(2, clientSecret);
                preparedStatement.setInt(3, tenantId);
                preparedStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to add KM Mgt application: " + clientId + " for tenant: " + tenantId, e);
            }
        } catch (SQLException e) {
            handleException("Failed to add KM Mgt application: " + clientId + " for tenant: " + tenantId, e);
        }
    }

    /**
     * Handles exception.
     *
     * @param msg Error message
     * @param t   Throwable object
     * @throws APIManagementException Returns APIManagementException
     */
    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    /**
     * Get application for tenant.
     *
     * @param tenantId Tenant Id
     * @return KM Application DTO
     * @throws APIManagementException If an error occurs while getting application
     */
    public OAuthApplicationInfo getApplicationForTenant(int tenantId) throws APIManagementException {

        OAuthApplicationInfo oAuthApplicationInfo = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement =
                     connection
                             .prepareStatement(SQLConstants.KeyMgtConstants.GET_KM_APPLICATION_FOR_TENANT)) {
            statement.setInt(1, tenantId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    oAuthApplicationInfo = new OAuthApplicationInfo();
                    oAuthApplicationInfo.setClientId(rs.getString("CONSUMER_KEY"));
                    oAuthApplicationInfo.setClientSecret(rs.getString("CONSUMER_SECRET"));
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get KM mgt Application for tenant: " + tenantId, e);
        }
        return oAuthApplicationInfo;
    }
}
