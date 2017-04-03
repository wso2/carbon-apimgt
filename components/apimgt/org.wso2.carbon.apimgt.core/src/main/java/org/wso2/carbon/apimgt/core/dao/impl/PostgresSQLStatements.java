/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SQL Statements that are specific to Postgres Database.
 */
public class PostgresSQLStatements implements ApiDAOVendorSpecificStatements {

    private static final String API_SUMMARY_SELECT =
            "SELECT API.UUID, API.PROVIDER, API.NAME, API.CONTEXT, API.VERSION, API.DESCRIPTION,"
                    + "API.CURRENT_LC_STATUS, API.LIFECYCLE_INSTANCE_ID, API.LC_WORKFLOW_STATUS "
                    + "FROM AM_API API LEFT JOIN AM_API_GROUP_PERMISSION PERMISSION ON UUID = API_ID";

    /**
     * Creates full text search query specific to database.
     *
     * @param connection   Database connection.
     * @param searchString The search string provided
     * @param offset       The starting point of the search results.
     * @param limit        Number of search results that will be returned.
     * @return {@link   PreparedStatement} Statement build for specific database type.
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings({"SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
            "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE"})
    public PreparedStatement search(Connection connection,
            List<String> roles, String user, String searchString, int offset, int limit) throws APIMgtDAOException {

        StringBuilder roleListBuilder = new StringBuilder();
        roles.forEach(item -> roleListBuilder.append("?,"));
        roleListBuilder.append("?");
        final String query =
                API_SUMMARY_SELECT + " WHERE textsearchable_index_col @@ to_tsquery(replace(?, ' ', '+')) AND "
                        + "((GROUP_ID IN (" + roleListBuilder.toString() + ")) OR (PROVIDER = ?)) GROUP BY UUID "
                        + "ORDER BY NAME LIMIT ? OFFSET ?";
        int queryIndex = 1;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(queryIndex, searchString.toLowerCase(Locale.ENGLISH) + ":*");
            queryIndex++;
            for (String role : roles) {
                statement.setString(queryIndex, role);
                queryIndex++;
            }
            statement.setString(queryIndex, EVERYONE_ROLE);
            statement.setString(++queryIndex, user);
            statement.setInt(++queryIndex, limit);
            statement.setInt(++queryIndex, offset);
            return statement;
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Creates attribute search query specific to database.
     *
     * @param connection   Database connection.
     * @param attributeMap Map containing the attributes and search queries for those attributes
     * @param offset       The starting point of the search results.
     * @param limit        Number of search results that will be returned.
     * @return {@link   PreparedStatement} Statement build for specific database type.
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings({"SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
            "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE"})
    public PreparedStatement attributeSearch(
            Connection connection, List<String> roles, String user, Map<String, String> attributeMap, int offset,
            int limit) throws APIMgtDAOException {
        StringBuilder roleListBuilder = new StringBuilder();
        roles.forEach(item -> roleListBuilder.append("?,"));
        roleListBuilder.append("?");
        StringBuilder searchQuery = new StringBuilder();
        Iterator<Map.Entry<String, String>> entries = attributeMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            searchQuery.append("LOWER(");
            searchQuery.append(entry.getKey());
            searchQuery.append(") LIKE ?");
            if (entries.hasNext()) {
                searchQuery.append(" AND ");
            }
        }

        final String query = API_SUMMARY_SELECT + " WHERE " + searchQuery.toString() + " AND ("
                + "(GROUP_ID IN (" + roleListBuilder.toString()
                + ")) OR  (PROVIDER = ?)) GROUP BY UUID ORDER BY NAME LIMIT ? OFFSET ?";
        try {
            int queryIndex = 1;
            PreparedStatement statement = connection.prepareStatement(query);
            for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
                statement.setString(queryIndex, '%' + entry.getValue().toLowerCase(Locale.ENGLISH) + '%');
                queryIndex++;
            }
            for (String role : roles) {
                statement.setString(queryIndex, role);
                queryIndex++;
            }
            statement.setString(queryIndex, EVERYONE_ROLE);
            statement.setString(++queryIndex, user);
            statement.setInt(++queryIndex, limit);
            statement.setInt(++queryIndex, --offset);
            return statement;
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }
}
