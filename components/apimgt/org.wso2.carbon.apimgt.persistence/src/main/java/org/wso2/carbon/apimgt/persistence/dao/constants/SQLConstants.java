package org.wso2.carbon.apimgt.persistence.dao.constants;

public class SQLConstants {
    public static final String ADD_ARTIFACT_SQL =
            "INSERT INTO AM_ARTIFACT_DATA (type, org, metadata, uuid, api_uuid) " +
            "VALUES (?, ?, ?, ?, ?)";

    public static final String GET_ALL_API_ARTIFACT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "ORDER BY ARTIFACT_ID DESC " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_ARTIFACT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "AND LOWER(metadata) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String GET_ALL_API_COUNT =
            "SELECT COUNT(*) AS TOTAL_API_COUNT FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API'";

    public static final String GET_API_BY_UUID_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API'";

    public static final String GET_SWAGGER_DEFINITION_BY_UUID_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? AND API_UUID = ? " +
                    "AND TYPE = 'API_DEFINITION'";

    public static final String SEARCH_API_OAS_DEFINITIONS_SQL =
            "SELECT * FROM AM_API_JSON_SCHEMA " +
                    "WHERE TENANT_DOMAIN = ? AND " +
                    "LOWER(JSON_QUERY(API_SCHEMA, '$.swaggerDefinition' RETURNING CLOB)) LIKE ? ";

    public static final String SEARCH_API_SCHEMA_CONTENT =
            "SELECT * FROM AM_API_JSON_SCHEMA " +
                    "WHERE TENANT_DOMAIN = ? AND " +
                    "CONTAINS(API_SCHEMA_TEXT, ?) > 0";

    public static final String GET_DOCUMENTATION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND UUID = ? ";

    public static final String GET_ALL_DOCUMENTATION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND API_UUID = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_DOCUMENTATION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND API_UUID = ? " +
                    "AND LOWER(metadata) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String GET_DOCUMENTATION_COUNT =
            "SELECT COUNT(*) AS TOTAL_DOC_COUNT FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND API_UUID = ?";

    public static final String ADD_DOCUMENTATION_FILE_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "ARTIFACT = ? " +
                    "WHERE UUID = ? ";

    public static final String ADD_METADATA_FOR_FILE_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "METADATA = JSON_TRANSFORM( " +
                    "METADATA, " +
                    "SET '$.fileType' = ?, " +
                    "SET '$.fileName' = ?) " +
                    "WHERE UUID = ? ";

    public static final String ADD_DOCUMENTATION_CONTENT_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "METADATA = JSON_TRANSFORM( " +
                    "METADATA, " +
                    "SET '$.textContent' = ?) " +
                    "WHERE UUID = ? ";

    public static final String GET_DOCUMENTATION_CONTENT_SQL =
            "SELECT metadata FROM AM_ARTIFACT_DATA " +
                    "WHERE UUID = ? ";

    public static final String GET_DOCUMENTATION_FILE_SQL =
            "SELECT artifact FROM AM_ARTIFACT_DATA " +
                    "WHERE UUID = ? ";
}
