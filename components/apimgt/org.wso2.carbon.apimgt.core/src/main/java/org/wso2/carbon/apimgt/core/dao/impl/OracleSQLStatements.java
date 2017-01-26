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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * SQL Statements that are specific to Oracle Database.
 */
public class OracleSQLStatements implements ApiDAOVendorSpecificStatements {

    private static Logger log = LoggerFactory.getLogger(OracleSQLStatements.class);
    private static final String API_SUMMARY_SELECT = "SELECT UUID, PROVIDER, NAME, CONTEXT, VERSION, DESCRIPTION, " +
            "CURRENT_LC_STATUS, LIFECYCLE_INSTANCE_ID, ROW_NUMBER() OVER (ORDER BY NAME) Row_Num FROM AM_API";

    /**
     * Creates full text search query specific to database.
     *
     * @param connection  Database connection.
     * @param searchString The search string provided
     * @param offset  The starting point of the search results.
     * @param limit   Number of search results that will be returned.
     * @return {@link   PreparedStatement} Statement build for specific database type.
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings ("OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE")
    public PreparedStatement search(Connection connection, String searchString, int offset, int limit) throws
            APIMgtDAOException {
        /*final String query = API_SUMMARY_SELECT
                + " WHERE (CONTAINS(DESCRIPTION, ?, 1) > 0) OR (CONTAINS(TECHNICAL_OWNER, ?, 2) > 0) "
                + "OR (CONTAINS(CURRENT_LC_STATUS, ?, 3) > 0)";*/
        final String query = " SELECT * FROM (" + API_SUMMARY_SELECT +   " WHERE (CONTAINS(INDEXER, ?, 1) > 0)) "
                + "WHERE Row_Num BETWEEN ? and ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, '%' + searchString.toLowerCase(Locale.ENGLISH) + '%');
            statement.setInt(2, offset);
            statement.setInt(3, (offset + limit - 1));
            return statement;
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Creates attribute search query specific to database.
     *
     * @param connection  Database connection.
     * @param attributeMap Map containing the attributes and search queries for those attributes
     * @param offset  The starting point of the search results.
     * @param limit   Number of search results that will be returned.
     * @return {@link   PreparedStatement} Statement build for specific database type.
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings ("OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE")
    public PreparedStatement attributeSearch(Connection connection, Map<String, String> attributeMap, int offset,
            int limit) throws APIMgtDAOException {

        StringBuffer searchQuery = new StringBuffer();
        Iterator<Map.Entry<String, String>> entries = attributeMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            searchQuery.append("LOWER(");
            searchQuery.append(entry.getKey());
            searchQuery.append(") LIKE ?");
            if (entries.hasNext()) {
                searchQuery.append(" OR ");
            }
        }

        final String query = " SELECT * FROM (" + API_SUMMARY_SELECT + " WHERE " + searchQuery.toString() + ") "
                + "WHERE Row_Num BETWEEN ? and ?";
        try {
            int queryIndex = 1;
            PreparedStatement statement = connection.prepareStatement(query);
            for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
                statement.setString(queryIndex, '%' + entry.getValue().toLowerCase(Locale.ENGLISH) + '%');
                queryIndex++;
            }
            statement.setInt(queryIndex, offset);
            statement.setInt(++queryIndex, (offset + limit - 1));
            return statement;
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }
}
