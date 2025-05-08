package org.wso2.carbon.apimgt.persistence.dao.constants;

public class SQLConstants {
    public static final String ADD_ARTIFACT_SQL =
            "INSERT INTO AM_ARTIFACT_DATA (type, org, metadata, uuid, api_uuid) " +
            "VALUES (?, ?, ?, ?, ?)";

    public static final String GET_ALL_API_SCHEMA_SQL =
            "SELECT * FROM AM_API_JSON_SCHEMA " +
                    "WHERE TENANT_DOMAIN = ? " +
                    "ORDER BY API_SCHEMA_ID DESC " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_SCHEMA_SQL =
            "SELECT * FROM AM_API_JSON_SCHEMA " +
                    "WHERE TENANT_DOMAIN = ? AND " +
                    "LOWER(JSON_QUERY(API_SCHEMA, '$.swaggerDefinition' RETURNING CLOB)) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String GET_ALL_API_COUNT =
            "SELECT COUNT(*) AS TOTAL_API_COUNT FROM AM_API_JSON_SCHEMA " +
                    "WHERE TENANT_DOMAIN = ?";

    public static final String GET_API_BY_UUID_SQL =
            "SELECT * FROM AM_API_JSON_SCHEMA " +
                    "WHERE TENANT_DOMAIN = ? AND API_UUID = ?";

    public static final String GET_SWAGGER_DEFINITION_BY_UUID_SQL =
            "SELECT AM_JS.API_SCHEMA.swaggerDefinition AS SWAGGER_DEFINITION FROM AM_API_JSON_SCHEMA AM_JS " +
                    "WHERE TENANT_DOMAIN = ? AND API_UUID = ?";

    public static final String SEARCH_API_OAS_DEFINITIONS_SQL =
            "SELECT * FROM AM_API_JSON_SCHEMA " +
                    "WHERE TENANT_DOMAIN = ? AND " +
                    "LOWER(JSON_QUERY(API_SCHEMA, '$.swaggerDefinition' RETURNING CLOB)) LIKE ? ";

    public static final String SEARCH_API_SCHEMA_CONTENT =
            "SELECT * FROM AM_API_JSON_SCHEMA " +
                    "WHERE TENANT_DOMAIN = ? AND " +
                    "CONTAINS(API_SCHEMA_TEXT, ?) > 0";
}
