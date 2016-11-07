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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides access to Resource Types which maybe shared across multiple entities
 */
public class ResourceTypeDAO {

    static int getResourceTypeID(Connection connection, String resourceType) throws SQLException {
        final String query = "SELECT RESOURCE_TYPE_ID FROM AM_RESOURCE_TYPES WHERE RESOURCE_TYPE_NAME = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceType);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("RESOURCE_TYPE_ID");
                }
            }
        }

        return -1;
    }

    static void addResourceType(Connection connection, String resourceType) throws SQLException {
        final String query = "INSERT INTO AM_RESOURCE_TYPES (RESOURCE_TYPE_NAME) VALUES (?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceType);
            statement.execute();
        }
    }

}
