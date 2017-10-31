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

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Internal interface to actual DataSource implementation to remove a direct dependency with any specific DataSource
 * implementation. This also enables DAO logic to be testable with an alternative DataSource implementation. This is
 * only used by the respective DAO implementations.
 */
public interface DataSource {
    /**
     * Get a {@link Connection} object
     * @return {@link Connection} from given DataSource
     * @throws SQLException     If failed to retrieve database connection.
     */
    Connection getConnection() throws SQLException;

    /**
     * Return Hikari Datasource
     *
     * @return {@link HikariDataSource} object
     * @throws SQLException     If failed to get data source.
     */
    HikariDataSource getDatasource() throws SQLException;
}
