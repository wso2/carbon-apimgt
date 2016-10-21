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


import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.dao.APIManagementDAOException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides Utility functionality required by the DAO layer
 */
public class DAOUtil {
    private static final Logger log = LoggerFactory.getLogger(DAOUtil.class);
    private static HikariDataSource dataSource;

    public static synchronized void initialize(HikariDataSource dataSource) {
        if (DAOUtil.dataSource != null) {
            return;
        }

        DAOUtil.dataSource = dataSource;
    }

    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws java.sql.SQLException if failed to get Connection
     */

    static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }

        throw new SQLException("Data source is not configured properly.");
    }

    static void handleException(String msg) throws APIManagementDAOException {
        log.error(msg);
        throw new APIManagementDAOException(msg);
    }

    public static void handleException(String msg, Throwable t) throws APIManagementDAOException {
        log.error(msg, t);
        throw new APIManagementDAOException(msg, t);
    }
}

