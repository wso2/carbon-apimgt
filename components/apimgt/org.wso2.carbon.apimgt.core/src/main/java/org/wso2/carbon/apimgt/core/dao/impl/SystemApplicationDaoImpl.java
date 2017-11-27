/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.SystemApplicationDao;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Implementation for SystemApplicationDao
 */
public class SystemApplicationDaoImpl implements SystemApplicationDao {
    private static final Logger log = LoggerFactory.getLogger(SystemApplicationDaoImpl.class);

    @Override
    public void addApplicationKey(String appName, String consumerKey) throws APIMgtDAOException {
        final String query = "INSERT INTO AM_SYSTEM_APPS (NAME,CONSUMER_KEY,CREATED_TIME) VALUES (?,?,?)";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            try {
                statement.setString(1, appName);
                statement.setString(2, consumerKey);
                statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()), Calendar.getInstance(TimeZone
                        .getTimeZone("UTC")));
                log.debug("Executing query: {} ", query);
                statement.execute();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw new APIMgtDAOException("Couldn't Create System Application", ex);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMsg = "Error while creating database connection/prepared-statement";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public String getConsumerKeyForApplication(String appName) throws APIMgtDAOException {
        final String query = "SELECT CONSUMER_KEY FROM AM_SYSTEM_APPS WHERE NAME = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, appName);
            log.debug("Executing query: {} ", query);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("CONSUMER_KEY");
                } else {
                    throw new APIMgtDAOException("System Application with " + appName + " does not exist",
                            ExceptionCodes.SYSTEM_APP_NOT_FOUND);
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while creating database connection/prepared-statement";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void removeConsumerKeyForApplication(String appName) throws APIMgtDAOException {
        final String query = "DELETE FROM AM_SYSTEM_APPS WHERE NAME = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            try {
                statement.setString(1, appName);
                log.debug("Executing query: {} ", query);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException("Couldn't Delete System Application", e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMsg = "Error while creating database connection/prepared-statement";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public boolean isConsumerKeyExistForApplication(String appName) throws APIMgtDAOException {
        final String query = "SELECT CONSUMER_KEY FROM AM_SYSTEM_APPS WHERE NAME = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, appName);
            log.debug("Executing query: {} ", query);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while creating database connection/prepared-statement";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }
}
