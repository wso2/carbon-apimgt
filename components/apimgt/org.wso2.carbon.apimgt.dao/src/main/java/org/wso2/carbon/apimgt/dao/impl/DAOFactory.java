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

package org.wso2.carbon.apimgt.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.dao.APIManagementDAOException;
import org.wso2.carbon.apimgt.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.dao.ApiDAO;
import org.wso2.carbon.apimgt.dao.ApplicationDAO;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Constructs DB vendor specific DAO implementations in a transparent manner.
 */
public class DAOFactory {
    private static final Logger log = LoggerFactory.getLogger(DAOFactory.class);

    public ApiDAO getApiDAO() throws APIManagementDAOException {
        ApiDAO apiDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains("MySQL") || driverName.contains("H2")) {
                apiDAO = new DefaultApiDAOImpl();
            } else if (driverName.contains("DB2")) {

            } else if (driverName.contains("MS SQL") || driverName.contains("Microsoft")) {

            } else if (driverName.contains("PostgreSQL")) {

            } else if (driverName.contains("Oracle")) {

            } else {
                DAOUtil.handleException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            DAOUtil.handleException("Error occurred while getting DB Connection", e);
        }

        return apiDAO;
    }

    public ApplicationDAO getApplicationDAO() throws APIManagementDAOException {
        ApplicationDAO appDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains("MySQL") || driverName.contains("H2")) {
                appDAO = new DefaultApplicationDAOImpl();
            } else if (driverName.contains("DB2")) {

            } else if (driverName.contains("MS SQL") || driverName.contains("Microsoft")) {

            } else if (driverName.contains("PostgreSQL")) {

            } else if (driverName.contains("Oracle")) {

            } else {
                DAOUtil.handleException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            DAOUtil.handleException("Error occurred while getting DB Connection", e);
        }

        return appDAO;
    }

    public APISubscriptionDAO getAPISubscriptionDAO() throws APIManagementDAOException {
        APISubscriptionDAO apiSubscriptionDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains("MySQL") || driverName.contains("H2")) {
                apiSubscriptionDAO = new DefaultAPISubscriptionDAOImpl();
            } else if (driverName.contains("DB2")) {

            } else if (driverName.contains("MS SQL") || driverName.contains("Microsoft")) {

            } else if (driverName.contains("PostgreSQL")) {

            } else if (driverName.contains("Oracle")) {

            } else {
                DAOUtil.handleException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            DAOUtil.handleException("Error occurred while getting DB Connection", e);
        }

        return apiSubscriptionDAO;
    }
}
