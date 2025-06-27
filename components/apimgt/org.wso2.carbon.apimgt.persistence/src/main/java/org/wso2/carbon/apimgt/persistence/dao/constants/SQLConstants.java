package org.wso2.carbon.apimgt.persistence.dao.constants;

public class SQLConstants {
    public static final String ADD_ARTIFACT_SQL =
            "INSERT INTO AM_ARTIFACT_DATA (type, org, metadata, uuid, api_uuid) " +
            "VALUES (?, ?, ?, ?, ?)";

    public static final String GET_API_BY_UUID_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API'";

    public static final String GET_SWAGGER_DEFINITION_BY_UUID_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? AND API_UUID = ? " +
                    "AND TYPE = 'API_DEFINITION'";

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

    public static final String DELETE_DOCUMENTATION_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
                    "WHERE type = 'DOCUMENTATION' " +
                    "AND UUID = ? ";

    public static final String SAVE_OAS_DEFINITION_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API_DEFINITION'";

    public static final String DELETE_API_SCHEMA_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
                    "WHERE API_UUID = ? ";

    public static final String ADD_FILE_ARTIFACT_SQL =
            "INSERT INTO AM_ARTIFACT_DATA (type, org, metadata, uuid, artifact, api_uuid) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    public static final String GET_THUMBNAIL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE type = 'THUMBNAIL' " +
                    "AND API_UUID = ? " +
                    "AND JSON_VALUE(org, '$.name') = ? ";

    public static final String SAVE_ASYNC_API_DEFINITION_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'ASYNC_API_DEFINITION'";

    public static final String GET_ASYNC_API_DEFINITION_BY_UUID_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND TYPE = 'ASYNC_API_DEFINITION' " +
            "AND JSON_VALUE(org, '$.name') = ? ";

    public static final String DELETE_THUMBNAIL_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
            "WHERE type = 'THUMBNAIL' " +
            "AND API_UUID = ? " +
            "AND JSON_VALUE(org, '$.name') = ? ";

    public static final String GET_WSDL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE type = 'WSDL' " +
            "AND API_UUID = ? " +
            "AND JSON_VALUE(org, '$.name') = ? ";

    public static final String UPDATE_GRAPHQL_SCHEMA_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
            "METADATA = ? " +
            "WHERE API_UUID = ? " +
            "AND TYPE = 'GRAPHQL_SCHEMA'";

    public static final String GET_GRAPHQL_SCHEMA_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND TYPE = 'GRAPHQL_SCHEMA' " +
            "AND JSON_VALUE(org, '$.name') = ? ";

    public static final String ADD_API_REVISION_SQL =
            "INSERT INTO AM_REVISION_ARTIFACT (TYPE, ORG, API_UUID, REVISION_UUID, REVISION_ID, METADATA) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    public static final String ADD_API_REVISION_ARTIFACT_SQL =
            "INSERT INTO AM_REVISION_ARTIFACT (TYPE, ORG, API_UUID, REVISION_UUID, REVISION_ID, ARTIFACT, METADATA) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static final String GET_API_REVISION_BY_ID_SQL =
            "SELECT * FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND TYPE = 'API' " +
            "AND JSON_VALUE(ORG, '$.name') = ? ";

    public static final String GET_API_REVISION_SWAGGER_DEFINITION_BY_ID_SQL =
            "SELECT * FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND TYPE = 'API_DEFINITION' " +
            "AND JSON_VALUE(ORG, '$.name') = ? ";

    public static final String GET_API_REVISION_ASYNC_DEFINITION_BY_ID_SQL =
            "SELECT * FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND TYPE = 'ASYNC_API_DEFINITION' " +
            "AND JSON_VALUE(ORG, '$.name') = ? ";

    public static final String GET_API_LIFECYCLE_STATUS_SQL =
            "SELECT JSON_VALUE(metadata, '$.status') AS STATUS FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND JSON_VALUE(ORG, '$.name') = ? " +
            "AND TYPE = 'API' ";

    public static final String UPDATE_API_SQL =
            "UPDATE AM_ARTIFACT_DATA " +
                    "SET METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API' " ;

    public static final String UPDATE_SWAGGER_DEFINITION_SQL =
            "UPDATE AM_ARTIFACT_DATA " +
                    "SET METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API_DEFINITION' ";

    public static final String UPDATE_ASYNC_DEFINITION_SQL =
            "UPDATE AM_ARTIFACT_DATA " +
                    "SET METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'ASYNC_API_DEFINITION' ";

    public static final String GET_API_REVISION_THUMBNAIL_SQL =
            "SELECT * FROM AM_REVISION_ARTIFACT " +
            "WHERE TYPE = 'THUMBNAIL' " +
            "AND REVISION_UUID = ? " +
            "AND JSON_VALUE(ORG, '$.name') = ? ";

    public static final String UPDATE_THUMBNAIL_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
            "ARTIFACT = ? " +
            "WHERE TYPE = 'THUMBNAIL' " +
            "AND API_UUID = ? ";

    public static final String DELETE_API_REVISION_SQL =
            "DELETE FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " ;

    public static final String UPDATE_DOCUMENTATION_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
            "METADATA = ? " +
            "WHERE UUID = ? ";

    public static final String GET_API_PRODUCT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API_PRODUCT' " +
                    "AND JSON_VALUE(org, '$.name') = ? ";

    public static final String UPDATE_API_PRODUCT_SQL =
            "UPDATE AM_ARTIFACT_DATA " +
                    "SET METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API_PRODUCT' ";

    public static final String GET_API_PRODUCT_COUNT_SQL =
            "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT'";

    public static final String DELETE_API_PRODUCT_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
                    "WHERE type = 'API_PRODUCT' " +
                    "AND API_UUID = ? " +
                    "AND JSON_VALUE(org, '$.name') = ? ";

    public static final String DELETE_API_PRODUCT_SWAGGER_DEFINITION_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
                    "WHERE type = 'API_DEFINITION' " +
                    "AND API_UUID = ? ";

    public static final String GET_ALL_DOCUMENTS_FOR_API_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'DOCUMENTATION' " ;

    public static final String GET_ALL_API_REVISION_IDS_SQL =
            "SELECT REVISION_UUID FROM AM_REVISION_ARTIFACT " +
            "WHERE API_UUID = ? " +
            "AND TYPE IN ('API', 'API_PRODUCT') ";

    public static final String CHECK_API_EXISTS_SQL =
            "SELECT COUNT(*) as count FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND type IN ('API', 'API_PRODUCT') " +
            "AND API_UUID = ? ";

    public static final String GET_ALL_TAGS_SQL =
            "SELECT DISTINCT JSON_QUERY(METADATA, '$.tags') AS TAGS " +
                    "FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND JSON_QUERY(METADATA, '$.tags') IS NOT NULL " +
                    "AND (" +
                    "(type = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(type = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED')" +
                    ")";

    public static final String GET_API_UUID_BY_REVISION_UUID_SQL =
            "SELECT API_UUID FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND JSON_VALUE(ORG, '$.name') = ? " +
            "AND TYPE IN ('API', 'API_PRODUCT') ";

    public static final String GET_ARTIFACT_TYPE_BY_UUID_SQL =
            "SELECT TYPE FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND JSON_VALUE(org, '$.name') = ? " +
            "AND TYPE IN ('API', 'API_PRODUCT') ";

    public static final String GET_SECURITY_SCHEME_BY_UUID_SQL =
            "SELECT JSON_QUERY(METADATA, '$.apiSecurity') as API_SECURITY_SCHEME FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND JSON_VALUE(org, '$.name') = ? " +
            "AND TYPE IN ('API', 'API_PRODUCT') ";
}
