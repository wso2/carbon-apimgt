/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * SQL Statements that are specific to tag search in H2 Database.
 */
class StoreTagSearchImpl implements StoreApiAttributeSearch {

    @Override
    public String getStoreAttributeSearchQuery(int roleCount,
                                               StringBuilder searchQuery) {

        //for tag search, need to check AM_API_TAG_MAPPING and AM_TAGS tables
        return SQLConstants.API_SUMMARY_SELECT_STORE +
                " WHERE " +
                SQLConstants.API_LC_STATUS_PUBLISHED_OR_PROTOTYPED +
                " AND " +
                SQLConstants.API_VISIBILITY_PUBLIC +
                " AND " +
                SQLConstants.getApiTagSearch(searchQuery) +
                " UNION " +
                SQLConstants.API_SUMMARY_SELECT_STORE +
                " WHERE " +
                SQLConstants.API_LC_STATUS_PUBLISHED_OR_PROTOTYPED +
                " AND " +
                SQLConstants.getApiVisibilityRestricted(roleCount) +
                " AND " +
                SQLConstants.getApiTagSearch(searchQuery);
    }
}
