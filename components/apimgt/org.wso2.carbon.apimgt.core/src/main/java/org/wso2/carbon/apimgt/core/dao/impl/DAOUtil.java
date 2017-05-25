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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides Utility functionality required by the DAO layer
 */
public class DAOUtil {
    private static final Logger log = LoggerFactory.getLogger(DAOUtil.class);
    private static DataSource dataSource;

    public static synchronized void initialize(DataSource dataSource) {
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
        throw new SQLException("Datasource is not configured properly.");
    }

    /**
     * Get is auto commit enabled
     *
     * @return true if auto commit is enabled, false otherwise
     * @throws SQLException Error while getting if auto commit is enabled
     */
    public static boolean isAutoCommit() throws SQLException {
        return dataSource.getDatasource().isAutoCommit();
    }

    static String getParameterString(int numberOfParameters) {
        List<String> questionMarks = new ArrayList<>(Collections.nCopies(numberOfParameters, "?"));
        return String.join(",", questionMarks);
    }

    static List<String> commaSeperatedStringToList(String strValue) {
        if (strValue != null && !strValue.isEmpty()) {
            return Arrays.asList(strValue.split("\\s*,\\s*"));
        }

        return new ArrayList<>();
    }

    public static void clearDataSource() {
        dataSource = null;
    }
}

