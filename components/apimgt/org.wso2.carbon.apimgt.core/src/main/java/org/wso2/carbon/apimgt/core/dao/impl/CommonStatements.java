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
 * Defines SQL statements common across all DB vendors
 */
public class CommonStatements implements SQLStatements {
    private static final String IS_API_EXISTS = "SELECT API_ID FROM AM_API WHERE " +
            "PROVIDER = ? AND NAME = ? AND VERSION = ?";

    private static final String ADD_API = "INSERT INTO AM_API (PROVIDER, NAME, CONTEXT, VERSION, " +
            "IS_DEFAULT_VERSION, DESCRIPTION, VISIBILITY, IS_RESPONSE_CACHED, CACHE_TIMEOUT, " +
            "UUID, TECHNICAL_OWNER, TECHNICAL_EMAIL, BUSINESS_OWNER, BUSINESS_EMAIL, CREATED_BY, " +
            "CREATED_TIME, LAST_UPDATED_TIME)" +
            " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String GET_API = "SELECT PROVIDER, NAME, CONTEXT, VERSION, " +
            "IS_DEFAULT_VERSION, DESCRIPTION, VISIBILITY, IS_RESPONSE_CACHED, CACHE_TIMEOUT, " +
            "UUID, TECHNICAL_OWNER, TECHNICAL_EMAIL, BUSINESS_OWNER, BUSINESS_EMAIL, CREATED_BY, " +
            "CREATED_TIME, LAST_UPDATED_TIME FROM AM_API WHERE UUID = ?";

    private static final String DELETE_API = "DELETE FROM AM_API WHERE UUID = ?";

    @Override
    public String getIsApiExists() {
        return IS_API_EXISTS;
    }

    @Override
    public String getAddApi() {
        return ADD_API;
    }

    @Override
    public String getGetApi() {
        return GET_API;
    }

    @Override
    public String getDeleteApi() {
        return DELETE_API;
    }
}
