/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.apimgt.core.dao.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Queries that accessing columns that are common to all entity tables goes into this class
 */
class EntityDAO {

    /**
     * Returns the last access time of the given entity identified by the UUID field.
     * 
     * @param resourceTableName Table name of the entity
     * @param uuid value of the UUID field of the entity
     * @return Last access time of the requested resource
     * @throws APIMgtDAOException
     */
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    static String getLastUpdatedTimeOfResourceByUUID(String resourceTableName, String uuid)
            throws APIMgtDAOException {
        final String query = "SELECT LAST_UPDATED_TIME FROM " + resourceTableName + " WHERE UUID = ?";
        String lastUpdatedTime = null;
        try (Connection connection = DAOUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    lastUpdatedTime = rs.getString("LAST_UPDATED_TIME");
                }
            }
            return lastUpdatedTime;
        } catch (SQLException e) {
            throw new APIMgtDAOException(
                    "Error while retrieving last access time from table : " + resourceTableName + " and entity " + uuid,
                    e);
        }
    }

    /**
     * Returns the last access time of the given entity identified by the NAME field.
     *
     * @param resourceTableName Table name of the entity
     * @param name value in the NAME field of the entity
     * @return Last access time of the requested resource
     * @throws APIMgtDAOException
     */
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    static String getLastUpdatedTimeOfResourceByName(String resourceTableName, String name)
            throws APIMgtDAOException {
        final String query = "SELECT LAST_UPDATED_TIME FROM " + resourceTableName + " WHERE NAME = ?";
        String lastUpdatedTime = null;
        try (Connection connection = DAOUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    lastUpdatedTime = rs.getString("LAST_UPDATED_TIME");
                }
            }
            return lastUpdatedTime;
        } catch (SQLException e) {
            throw new APIMgtDAOException(
                    "Error while retrieving last access time from table : " + resourceTableName + " and entity " + name,
                    e);
        }
    }
}
