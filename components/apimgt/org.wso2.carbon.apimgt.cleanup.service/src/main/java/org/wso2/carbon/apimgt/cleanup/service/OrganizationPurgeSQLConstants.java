package org.wso2.carbon.apimgt.cleanup.service;

public class OrganizationPurgeSQLConstants {
    public static final String GET_API_LIST_SQL_BY_ORG = "SELECT API.API_ID, API.API_UUID,API.API_NAME," +
            "API.API_VERSION, API.API_PROVIDER FROM AM_API API WHERE API.ORGANIZATION = ?";

    public static final String REMOVE_BULK_APIS_DATA_FROM_AM_API_SQL = "DELETE FROM AM_API WHERE ORGANIZATION = ?";

    public static final String REMOVE_BULK_APIS_DEFAULT_VERSION_SQL = "DELETE FROM AM_API_DEFAULT_VERSION WHERE "
            + "ORGANIZATION = ?";

    public static final String DELETE_BULK_API_WORKFLOWS_REQUEST_SQL = "DELETE FROM AM_WORKFLOWS WHERE " +
            "WF_TYPE=\"AM_API_STATE\" AND WF_REFERENCE IN (SELECT CONVERT(API.API_ID, CHAR) FROM AM_API API " +
            "WHERE API.ORGANIZATION = ?";

    public static final String DELETE_BULK_GW_PUBLISHED_API_DETAILS = "DELETE FROM AM_GW_PUBLISHED_API_DETAILS WHERE " +
            "TENANT_DOMAIN = ?";

    public static final String DELETE_BULK_KEY_MANAGER_LIST = "DELETE FROM AM_KEY_MANAGER WHERE ORGANIZATION = ? "
            + "AND UUID IN (_KM_UUIDS_)";

    public static final String KM_UUID_REGEX = "_KM_UUIDS_";
}
