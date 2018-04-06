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
import org.wso2.carbon.apimgt.core.dao.SearchType;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * SQL Statements that are specific to Oracle Database.
 */
public class OracleSQLStatements implements ApiDAOVendorSpecificStatements {

    private static Logger log = LoggerFactory.getLogger(OracleSQLStatements.class);

    private static final String PAGINATION_PREFIX = "SELECT * FROM (SELECT A.*, rownum rnum from (";

    private static final String API_SUMMARY_SELECT =
            "SELECT DISTINCT API.UUID, API.PROVIDER, API.NAME, API.CONTEXT, API"
                    + ".VERSION, API.DESCRIPTION,"
                    + "API.CURRENT_LC_STATUS, API.LIFECYCLE_INSTANCE_ID, API.LC_WORKFLOW_STATUS, API.API_TYPE_ID, "
                    + "API.SECURITY_SCHEME FROM AM_API API LEFT JOIN AM_API_GROUP_PERMISSION PERMISSION ON "
                    + "UUID = API_ID ";

    private static final String API_FULL_TEXT_SEARCH = "(CONTAINS(INDEXER, ?, 1) > 0)";

    private static final String API_SEARCH_ORDER_BY_NAME = " ORDER BY NAME";

    private static final String PAGINATION_POSTFIX = ") A WHERE rownum <= ?) WHERE rnum >= ?";

    OracleSQLStatements() {
    }

    /**
     * @see ApiDAOVendorSpecificStatements#getPermissionBasedApiFullTextSearchQuery(int)
     */
    @Override
    public String getPermissionBasedApiFullTextSearchQuery(int roleCount) {
        String query = PAGINATION_PREFIX +
                        API_SUMMARY_SELECT +
                        " WHERE " +
                        API_FULL_TEXT_SEARCH +
                        " AND API.API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

        if (roleCount > 0) {
            query += " AND (((GROUP_ID IN (" + DAOUtil.getParameterString(roleCount) + "))" +
                    " AND PERMISSION.PERMISSION >= " + APIMgtConstants.Permission.READ_PERMISSION +
                    ") OR (PROVIDER = ?) OR (PERMISSION.GROUP_ID IS NULL))";
        } else {
            query += " AND ((PROVIDER = ?) OR (PERMISSION.GROUP_ID IS NULL))";
        }

        return query + API_SEARCH_ORDER_BY_NAME +
                PAGINATION_POSTFIX;
    }


    /**
     * @see ApiDAOVendorSpecificStatements#setPermissionBasedApiFullTextSearchStatement(PreparedStatement, Set, String,
     * String, ApiType, int, int)
     */
    @Override
    @SuppressFBWarnings ({ "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
            "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE" })
    public void setPermissionBasedApiFullTextSearchStatement(PreparedStatement statement, Set<String> roles,
                                                             String user, String searchString, ApiType apiType,
                                                             int offset, int limit) throws SQLException {
        int index = 0;
        // Replacing special characters and allowing only alphabetical letters, numbers and space
        statement.setString(++index, '%' + searchString.toLowerCase(Locale.ENGLISH) + '%');
        statement.setString(++index, apiType.toString());

        for (String role : roles) {
            statement.setString(++index, role);
        }

        statement.setString(++index, user);
        statement.setInt(++index, limit);
        statement.setInt(++index, offset);
    }


    /**
     * @see ApiDAOVendorSpecificStatements#getPermissionBasedApiAttributeSearchQuery(Map, int)
     */
    @Override
    public String getPermissionBasedApiAttributeSearchQuery(Map<SearchType, String> attributeMap, int roleCount) {
        StringBuilder searchQuery = new StringBuilder();
        Iterator<Map.Entry<SearchType, String>> entries = attributeMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<SearchType, String> entry = entries.next();
            searchQuery.append("LOWER(");
            searchQuery.append(entry.getKey());
            searchQuery.append(") LIKE ?");
            if (entries.hasNext()) {
                searchQuery.append(" AND ");
            }
        }

        String query = PAGINATION_PREFIX + API_SUMMARY_SELECT + " WHERE " + searchQuery.toString() +
                " AND API.API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

        if (roleCount > 0) {
            query += " AND (((GROUP_ID IN (" + DAOUtil.getParameterString(roleCount) +
                    ")) AND PERMISSION.PERMISSION >= " + APIMgtConstants.Permission.READ_PERMISSION +
                    ") OR (PROVIDER = ?) OR (PERMISSION.GROUP_ID IS NULL))";
        } else {
            query += " AND ((PROVIDER = ?) OR (PERMISSION.GROUP_ID IS NULL))";
        }

        return query + API_SEARCH_ORDER_BY_NAME +
                PAGINATION_POSTFIX;
    }

    /**
     * @see ApiDAOVendorSpecificStatements#setPermissionBasedApiAttributeSearchStatement(PreparedStatement, Set,
     * String, Map, ApiType,int, int)
     */
    @Override
    @SuppressFBWarnings ({ "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
            "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE" })
    public void setPermissionBasedApiAttributeSearchStatement(PreparedStatement statement, Set<String> roles,
                                                              String user, Map<SearchType, String> attributeMap,
                                                              ApiType apiType,
                                                              int offset, int limit) throws SQLException {
        int index = 0;

        for (Map.Entry<SearchType, String> entry : attributeMap.entrySet()) {
            entry.setValue('%' + entry.getValue().toLowerCase(Locale.ENGLISH) + '%');
            statement.setString(++index, entry.getValue());
        }

        statement.setString(++index, apiType.toString());

        for (String role : roles) {
            statement.setString(++index, role);
        }

        statement.setString(++index, user);
        statement.setInt(++index, limit);
        statement.setInt(++index, offset);
    }

    @Override
    public String getVisibilityBasedApiFullTextSearchQuery(int roleCount, int labelCount) {
        String query = SQLConstants.API_SUMMARY_SELECT_STORE +
                        " WHERE " +
                        API_FULL_TEXT_SEARCH +
                        " AND " +
                        SQLConstants.API_LC_STATUS_PUBLISHED_OR_PROTOTYPED +
                        " AND " +
                        SQLConstants.API_VISIBILITY_PUBLIC;

        if (roleCount > 0) {
            query += " UNION " +
                    SQLConstants.API_SUMMARY_SELECT_STORE +
                    " WHERE " +
                    API_FULL_TEXT_SEARCH +
                    " AND " +
                    SQLConstants.API_LC_STATUS_PUBLISHED_OR_PROTOTYPED +
                    " AND " +
                    SQLConstants.getApiVisibilityRestricted(roleCount);
        }

        return PAGINATION_PREFIX + " Select * from (" + query + ") " + getStoreAPIsByLabelJoinQuery(labelCount) +
                API_SEARCH_ORDER_BY_NAME +
                PAGINATION_POSTFIX;
    }

    @Override
    public void setVisibilityBasedApiFullTextSearchStatement(PreparedStatement statement, Set<String> roles,
                                                             Set<String> labels, String searchString,
                                                             int offset, int limit) throws SQLException {
        int index = 0;

        // Replacing special characters and allowing only alphabetical letters, numbers and space
        statement.setString(++index, '%' + searchString.toLowerCase(Locale.ENGLISH) + '%');

        if (!roles.isEmpty()) {
            // Set values after UNION
            statement.setString(++index, '%' + searchString.toLowerCase(Locale.ENGLISH) + '%');

            for (String role: roles) {
                statement.setString(++index, role);
            }
        }

        for (String label : labels) {
            statement.setString(++index, label);
        }

        statement.setInt(++index, limit);
        statement.setInt(++index, offset);
    }

    @Override
    public String getVisibilityBasedApiAttributeSearchQuery(int roleCount, int labelCount,
                                                            Map<SearchType, String> attributeMap) {
        StringBuilder searchQuery = new StringBuilder();
        Iterator<Map.Entry<SearchType, String>> entries = attributeMap.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<SearchType, String> entry = entries.next();
            searchQuery.append("LOWER(");
            if (SearchType.TAG == entry.getKey()) {
                searchQuery.append(APIMgtConstants.TAG_NAME_COLUMN);
            } else if (SearchType.SUBCONTEXT == entry.getKey()) {
                searchQuery.append(APIMgtConstants.URL_PATTERN_COLUMN);
            } else {
                searchQuery.append(entry.getKey());
            }
            searchQuery.append(") LIKE ?");
            if (entries.hasNext()) {
                searchQuery.append(" AND ");
            }
        }

        //retrieve the attribute applicable for the search
        SearchType searchAttribute = attributeMap.entrySet().iterator().next().getKey();
        //get the corresponding implementation based on the attribute to be searched
        String query = SearchQueryDictionary.getSearchQuery(searchAttribute).
                getStoreAttributeSearchQuery(roleCount, searchQuery);

        return PAGINATION_PREFIX  + " Select * from (" + query + ") " + getStoreAPIsByLabelJoinQuery(labelCount) +
                API_SEARCH_ORDER_BY_NAME +
                PAGINATION_POSTFIX;
    }

    @Override
    public void setVisibilityBasedApiAttributeSearchStatement(PreparedStatement statement, Set<String> roles,
                                                              Set<String> labels, Map<SearchType, String> attributeMap,
                                                              int offset, int limit) throws SQLException {
        int index = 0;
        //include the attribute in the query (for APIs with public visibility)
        for (Map.Entry<SearchType, String> entry : attributeMap.entrySet()) {
            statement.setString(++index, '%' + entry.getValue().
                    toLowerCase(Locale.ENGLISH) + '%');
        }
        //include user roles in the query
        for (String role : roles) {
            statement.setString(++index, role);
        }
        //include the attribute in the query (for APIs with restricted visibility)
        for (Map.Entry<SearchType, String> entry : attributeMap.entrySet()) {
            statement.setString(++index, '%' + entry.getValue().
                    toLowerCase(Locale.ENGLISH) + '%');
        }

        for (String label : labels) {
            statement.setString(++index, label);
        }

        statement.setInt(++index, limit);
        //setting 0 as the default offset based on store-api.yaml and Oracle specifications
        statement.setInt(++index, (offset < 0) ? 0 : offset);
    }
}
