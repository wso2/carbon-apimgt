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

import org.wso2.carbon.apimgt.core.dao.ApiType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 *  Interface for getting SQL Statement strings. Implementation of the interface could return different values based
 *  on DB vendor type being used
 */
public interface ApiDAOVendorSpecificStatements {

    String EVERYONE_ROLE = "EVERYONE";

    /**
     * Returns the query string to be used for the search query. This is required to construct the PreparedStatement
     * which will be created externally
     * @param roleCount Number of roles to be passed to query
     * @return String
     */
    String getApiSearchQuery(int roleCount);

    /**
     * Returns the query string to be used for the attribute search query. This is required to construct the
     * PreparedStatement which will be created externally
     * @param attributeMap Search attributes to be queried
     * @param roleCount Number of roles to be passed to query
     * @return String
     */
    String getApiAttributeSearchQuery(Map<String, String> attributeMap, int roleCount);

    /**
     * Format supplied search string to DB compatible value
     * @param statement SQL PreparedStatement
     * @param roles roles assigned to the user doing the search
     * @param user user doing the search
     * @param searchString search string supplied
     * @param apiType API type to be considered for the search
     * @param offset result pagination offset
     * @param limit result pagination limit
     */
    void setApiSearchStatement(PreparedStatement statement, Set<String> roles, String user,
                           String searchString, ApiType apiType, int offset, int limit) throws SQLException;

    /**
     * Set parameters of the PreparedStatement created for the attribute search query
     * @param statement SQL PreparedStatement
     * @param roles roles assigned to the user doing the search
     * @param user user doing the search
     * @param attributeMap Search attributes to be queried
     * @param apiType API type to be considered for the search
     * @param offset result pagination offset
     * @param limit result pagination limit
     */
    void setApiAttributeSearchStatement(PreparedStatement statement, Set<String> roles, String user,
                                        Map<String, String> attributeMap, ApiType apiType, int offset, int limit)
                                        throws SQLException;

    /**
     * Creates attribute search query in API store, specific to database
     *
     * @param connection DB connection
     * @param roles user roles
     * @param attributeMap map containing the attributes and search queries for those attributes
     * @param offset the starting point of the search results.
     * @param limit number of search results that will be returned.
     * @return statement build for specific database type.
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    PreparedStatement attributeSearchStore(Connection connection, List<String> roles, Map<String,
            String> attributeMap, int offset, int limit) throws APIMgtDAOException;

}
