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

/**
 * SQL Statements that are common to H2 and MySQL DBs.
 */
public class H2MySQLStatements implements ApiDAOVendorSpecificStatements {

    private static final String API_SELECT = "SELECT a.API_ID, a.PROVIDER, a.NAME, a.CONTEXT, a.VERSION, " +
            "a.IS_DEFAULT_VERSION, a.DESCRIPTION, a.VISIBILITY, a.IS_RESPONSE_CACHED, a.CACHE_TIMEOUT, a.UUID, " +
            "a.TECHNICAL_OWNER, a.TECHNICAL_EMAIL, a.BUSINESS_OWNER, a.BUSINESS_EMAIL, a.LIFECYCLE_INSTANCE_ID, " +
            "a.CURRENT_LC_STATUS, a.API_POLICY_ID, a.CORS_ENABLED, a.CORS_ALLOW_ORIGINS, a.CORS_ALLOW_CREDENTIALS, " +
            "a.CORS_ALLOW_HEADERS, a.CORS_ALLOW_METHODS, a.CREATED_BY, a.CREATED_TIME, a.LAST_UPDATED_TIME " +
            "FROM AM_API a";

    private static final String API_SUMMARY_SELECT = "a.SELECT API_ID, a.PROVIDER, a.NAME, a.CONTEXT, a.VERSION, " +
            "a.DESCRIPTION, a.UUID, a.CURRENT_LC_STATUS FROM AM_API a";

    @Override
    public String getAPIsForRoles(int numberOfRoles) {
        return API_SELECT + ", AM_API_VISIBLE_ROLES b WHERE a.API_ID = b.API_ID AND " +
                "b.ROLE IN(" + DAOUtil.getParameterString(numberOfRoles) + ") ORDER BY a.CREATED_TIME " +
                "LIMIT ?,?";
    }

    @Override
    public String getAPIsForProvider() {
        return API_SUMMARY_SELECT + " WHERE a.PROVIDER = ? ORDER BY a.CREATED_TIME LIMIT ?,?";
    }

    @Override
    public String searchAPIsForRoles(int numberOfRoles) {
        return API_SUMMARY_SELECT + ", AM_API_VISIBLE_ROLES b WHERE b.ROLE " +
                "IN(" + DAOUtil.getParameterString(numberOfRoles) + ") AND a.NAME LIKE ? ORDER BY a.CREATED_TIME " +
                "LIMIT ?,?";
    }
}
