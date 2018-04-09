/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;

class SQLConstants {
    static final String AM_API_OPERATION_MAPPING_TABLE_NAME = "AM_API_OPERATION_MAPPING";
    static final String AM_TAGS_TABLE_NAME = "AM_TAGS";
    static final String AM_API_TABLE_NAME = "AM_API";

    static final String API_SUMMARY_SELECT_STORE = "SELECT UUID, PROVIDER, NAME, CONTEXT, " +
            "VERSION, DESCRIPTION, CURRENT_LC_STATUS, LIFECYCLE_INSTANCE_ID, LC_WORKFLOW_STATUS, SECURITY_SCHEME " +
            "FROM AM_API ";

    static final String API_LC_STATUS_PUBLISHED_OR_PROTOTYPED =
    "CURRENT_LC_STATUS  IN ('" + APIStatus.PUBLISHED.getStatus() + "','" + APIStatus.PROTOTYPED.getStatus() + "')";

    static final String API_VISIBILITY_PUBLIC = "VISIBILITY = '" + API.Visibility.PUBLIC + "'";

    static String getApiVisibilityRestricted(int roleCount) {
        return "VISIBILITY = '" + API.Visibility.RESTRICTED + "' AND " +
                "UUID IN (SELECT API_ID FROM AM_API_VISIBLE_ROLES WHERE ROLE IN (" +
                DAOUtil.getParameterString(roleCount) + "))";
    }

    static String getApiOperationMappingSearch(StringBuilder searchQuery) {
        return "UUID IN (SELECT API_ID FROM AM_API_OPERATION_MAPPING WHERE " + searchQuery.toString() + ")";
    }

    static String getApiTagSearch(StringBuilder searchQuery) {
        return "UUID IN (SELECT API_ID FROM AM_API_TAG_MAPPING WHERE TAG_ID IN " +
                "(SELECT TAG_ID FROM AM_TAGS WHERE " + searchQuery.toString() + "))";
    }
}
