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
