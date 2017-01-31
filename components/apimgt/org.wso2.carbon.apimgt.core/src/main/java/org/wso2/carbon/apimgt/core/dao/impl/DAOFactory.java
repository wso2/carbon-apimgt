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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Constructs DB vendor specific DAO implementations in a transparent manner.
 */
public class DAOFactory {
    private static final Logger log = LoggerFactory.getLogger(DAOFactory.class);

    public static ApiDAO getApiDAO() throws APIMgtDAOException {
        ApiDAO apiDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains("MySQL") || driverName.contains("H2")) {
                apiDAO = new ApiDAOImpl(new H2MySQLStatements());
            } else if (driverName.contains("DB2")) {

            } else if (driverName.contains("MS SQL") || driverName.contains("Microsoft")) {
                apiDAO = new ApiDAOImpl(new H2MySQLStatements());
            } else if (driverName.contains("PostgreSQL")) {
                apiDAO = new ApiDAOImpl(new H2MySQLStatements());

            } else if (driverName.contains("Oracle")) {
                apiDAO = new ApiDAOImpl(new H2MySQLStatements());

            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return apiDAO;
    }

    public static ApplicationDAO getApplicationDAO() throws APIMgtDAOException {
        ApplicationDAO appDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains("MySQL") || driverName.contains("H2")) {
                appDAO = new ApplicationDAOImpl();
            } else if (driverName.contains("DB2")) {

            } else if (driverName.contains("MS SQL") || driverName.contains("Microsoft")) {
                appDAO = new ApplicationDAOImpl();
            } else if (driverName.contains("PostgreSQL")) {
                appDAO = new ApplicationDAOImpl();
            } else if (driverName.contains("Oracle")) {
                appDAO = new ApplicationDAOImpl();
            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return appDAO;
    }

    public static APISubscriptionDAO getAPISubscriptionDAO() throws APIMgtDAOException {
        APISubscriptionDAO apiSubscriptionDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains("MySQL") || driverName.contains("H2")) {
                apiSubscriptionDAO = new APISubscriptionDAOImpl();
            } else if (driverName.contains("DB2")) {

            } else if (driverName.contains("MS SQL") || driverName.contains("Microsoft")) {
                apiSubscriptionDAO = new APISubscriptionDAOImpl();

            } else if (driverName.contains("PostgreSQL")) {
                apiSubscriptionDAO = new APISubscriptionDAOImpl();

            } else if (driverName.contains("Oracle")) {
                apiSubscriptionDAO = new APISubscriptionDAOImpl();
            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return apiSubscriptionDAO;
    }

    public static PolicyDAO getPolicyDAO() throws APIMgtDAOException {
        PolicyDAO policyDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains("MySQL") || driverName.contains("H2")) {
                policyDAO = new PolicyDAOImpl();
            } else if (driverName.contains("DB2")) {

            } else if (driverName.contains("MS SQL") || driverName.contains("Microsoft")) {
                policyDAO = new PolicyDAOImpl();

            } else if (driverName.contains("PostgreSQL")) {
                policyDAO = new PolicyDAOImpl();

            } else if (driverName.contains("Oracle")) {
                policyDAO = new PolicyDAOImpl();
            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return policyDAO;
    }

    public static TagDAO getTagDAO() throws APIMgtDAOException {
        TagDAO tagDAO = null;

        try (Connection connection = DAOUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains("MySQL") || driverName.contains("H2")) {
                tagDAO = new TagDAOImpl();
            } else if (driverName.contains("DB2")) {

            } else if (driverName.contains("MS SQL") || driverName.contains("Microsoft")) {

            } else if (driverName.contains("PostgreSQL")) {

            } else if (driverName.contains("Oracle")) {

            } else {
                throw new APIMgtDAOException("Unhandled DB Type detected");
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        setup();

        return tagDAO;
    }

    private static void setup() throws APIMgtDAOException {
        ApiDAOImpl.initResourceCategories();
        PolicyDAOImpl.initDefaultPolicies();
    }

}
