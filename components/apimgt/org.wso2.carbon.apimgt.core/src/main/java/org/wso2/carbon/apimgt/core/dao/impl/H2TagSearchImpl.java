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

import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;

/**
 * SQL Statements that are specific to tag search in H2 Database.
 */
class H2TagSearchImpl implements StoreApiAttributeSearch {

    private static final String API_SUMMARY_SELECT_STORE = "SELECT UUID, PROVIDER, NAME, CONTEXT, " +
            "VERSION, DESCRIPTION, CURRENT_LC_STATUS, LIFECYCLE_INSTANCE_ID, LC_WORKFLOW_STATUS, SECURITY_SCHEME " +
            "FROM AM_API ";

    @Override
    public String getStoreAttributeSearchQuery(StringBuilder roleListBuilder,
                                               StringBuilder searchQuery, int offset, int limit) {

        //for tag search, need to check AM_API_TAG_MAPPING and AM_TAGS tables
        String tagSearchQuery = API_SUMMARY_SELECT_STORE + " WHERE CURRENT_LC_STATUS  IN ('" +
                APIStatus.PUBLISHED.getStatus() + "','" +
                APIStatus.PROTOTYPED.getStatus() + "') AND " +
                "VISIBILITY = '" + API.Visibility.PUBLIC + "' AND " +
                "UUID IN (SELECT API_ID FROM AM_API_TAG_MAPPING WHERE TAG_ID IN " +
                "(SELECT TAG_ID FROM AM_TAGS WHERE " + searchQuery.toString() + ")) " +
                "UNION " +
                API_SUMMARY_SELECT_STORE + " WHERE CURRENT_LC_STATUS  IN ('" +
                APIStatus.PUBLISHED.getStatus() + "','" +
                APIStatus.PROTOTYPED.getStatus() + "') AND " +
                "VISIBILITY = '" + API.Visibility.RESTRICTED + "' AND " +
                "UUID IN (SELECT API_ID FROM AM_API_VISIBLE_ROLES WHERE ROLE IN (" +
                roleListBuilder.toString() + ")) AND " +
                "UUID IN (SELECT API_ID FROM AM_API_TAG_MAPPING WHERE TAG_ID IN " +
                "(SELECT TAG_ID FROM AM_TAGS WHERE " + searchQuery.toString() + ")) " +
                "LIMIT ? OFFSET ?";

        return tagSearchQuery;
    }
}
