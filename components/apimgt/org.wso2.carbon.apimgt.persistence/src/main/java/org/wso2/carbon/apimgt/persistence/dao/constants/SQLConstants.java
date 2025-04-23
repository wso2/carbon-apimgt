package org.wso2.carbon.apimgt.persistence.dao.constants;

public class SQLConstants {
    public static final String ADD_API_SCHEMA_SQL =
            "INSERT INTO AM_API_JSON_SCHEMA (API_SCHEMA, API_UUID, TENANT_DOMAIN)" +
            "VALUES (?, ?, ?)";

    public static final String GET_ALL_API_SCHEMA_SQL =
            "SELECT * FROM AM_API_JSON_SCHEMA " +
                    "WHERE TENANT_DOMAIN = ?";

    public static final String GET_ALL_API_COUNT =
            "SELECT COUNT(*) AS TOTAL_API_COUNT FROM AM_API_JSON_SCHEMA " +
                    "WHERE TENANT_DOMAIN = ?";
}
