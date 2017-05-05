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
import org.wso2.carbon.apimgt.core.dao.ApiType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * SQL Statements that are specific to H2 Database.
 */
public class H2SQLStatements implements ApiDAOVendorSpecificStatements {

    private static final Logger log = LoggerFactory.getLogger(H2SQLStatements.class);
    private static final String API_SUMMARY_SELECT =
            "SELECT API.UUID, API.PROVIDER, API.NAME, API.CONTEXT, API.VERSION, API.DESCRIPTION,"
                    + "API.CURRENT_LC_STATUS, API.LIFECYCLE_INSTANCE_ID, API.LC_WORKFLOW_STATUS, API.API_TYPE_ID "
                    + "FROM AM_API API LEFT JOIN AM_API_GROUP_PERMISSION PERMISSION ON UUID = API_ID";

    /**
     * @see ApiDAOVendorSpecificStatements#getApiSearchQuery(int)
     */
    @Override
    public String getApiSearchQuery(int roleCount) {
        return API_SUMMARY_SELECT +
                " LEFT JOIN FTL_SEARCH_DATA (?, 0, 0) FT ON API.UUID=FT.KEYS[0]" +
                " WHERE API.API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)" +
                " AND ((`GROUP_ID` IN (" + DAOUtil.getParameterString(roleCount) + ")) OR (PROVIDER = ?))" +
                " AND FT.TABLE='AM_API'" +
                " GROUP BY UUID ORDER BY NAME OFFSET ? LIMIT ?";
    }

    /**
     * @see ApiDAOVendorSpecificStatements#setApiSearchStatement(PreparedStatement, Set, String, String, ApiType,
     * int, int)
     */
    @Override
    @SuppressFBWarnings({"SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
            "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE"})
    public void setApiSearchStatement(PreparedStatement statement, Set<String> roles, String user,
                                        String searchString, ApiType apiType, int offset, int limit)
                                        throws SQLException {
        int index = 0;

        // Replacing special characters and allowing only alphabetical letters, numbers and space
        statement.setString(++index, searchString.toLowerCase(Locale.ENGLISH).
                replaceAll("[^a-zA-Z0-9\\s]", "") + '*');
        statement.setString(++index, apiType.toString());

        for (String role : roles) {
            statement.setString(++index, role);
        }

        statement.setString(++index, user);
        statement.setInt(++index, offset);
        statement.setInt(++index, limit);
    }

    /**
     * @see ApiDAOVendorSpecificStatements#getApiAttributeSearchQuery(Map, int)
     */
    @Override
    public String getApiAttributeSearchQuery(Map<String, String> attributeMap, int roleCount) {
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

        return API_SUMMARY_SELECT +
                " WHERE " + searchQuery.toString() +
                " AND API.API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)" +
                " AND ((GROUP_ID IN (" + DAOUtil.getParameterString(roleCount) + ")) OR  (PROVIDER = ?))" +
                " GROUP BY UUID ORDER BY NAME OFFSET ? LIMIT ?";
    }

    /**
     * @see ApiDAOVendorSpecificStatements#setApiAttributeSearchStatement(PreparedStatement, Set, String ,Map, ApiType,
     * int, int)
     */
    @Override
    @SuppressFBWarnings({"SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
            "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE"})
    public void setApiAttributeSearchStatement(PreparedStatement statement, Set<String> roles, String user,
                                               Map<String, String> attributeMap, ApiType apiType,
                                               int offset, int limit) throws SQLException {
        int index = 0;

        for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
            entry.setValue('%' + entry.getValue().toLowerCase(Locale.ENGLISH) + '%');
            statement.setString(++index, entry.getValue());
        }

        statement.setString(++index, apiType.toString());

        for (String role : roles) {
            statement.setString(++index, role);
        }

        statement.setString(++index, user);
        statement.setInt(++index, offset);
        statement.setInt(++index, limit);
    }
}
