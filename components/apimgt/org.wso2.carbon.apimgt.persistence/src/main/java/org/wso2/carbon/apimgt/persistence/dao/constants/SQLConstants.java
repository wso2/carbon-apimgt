package org.wso2.carbon.apimgt.persistence.dao.constants;

public class SQLConstants {
    public static final String ADD_ARTIFACT_SQL =
            "INSERT INTO AM_ARTIFACT_DATA (type, org, metadata, uuid, api_uuid) " +
            "VALUES (?, ?, ?, ?, ?)";

    public static final String GET_ALL_API_ARTIFACT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND TYPE = 'API' " +
                    "ORDER BY LAST_MODIFIED DESC " +
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
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? AND " +
                    "CONTAINS(METADATA, ?) > 0";

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

    public static final String UPDATE_API_PRODUCT_SQL =
            "UPDATE AM_ARTIFACT_DATA " +
                    "SET METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API_PRODUCT' ";

    public static final String SEARCH_API_PRODUCT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND LOWER(metadata) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

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

    // API Search SQL Queries
    public static final String SEARCH_API_BY_CONTENT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "AND LOWER(metadata) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_NAME_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "AND LOWER(JSON_VALUE(metadata, '$.id.apiName')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_PROVIDER_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.id.providerName')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_VERSION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.id.version')) LIKE ?" +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_CONTEXT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "AND LOWER(JSON_QUERY(a2.METADATA, '$.context')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_STATUS_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.status')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_DESCRIPTION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.description')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_TAGS_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.tags')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_API_CATEGORY_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.apiCategories')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static String SEARCH_API_BY_OTHER_SQL(String propertyName) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    // Content Search SQL Queries

    public static final String SEARCH_CONTENT_BY_CONTENT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                        "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                        "WHERE a1.API_UUID = a2.API_UUID " +
                        "AND LOWER(a2.METADATA) LIKE ? " +
                    ") " +
                    "AND JSON_VALUE(a1.org, '$.name') = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_NAME_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                        "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                        "WHERE a1.API_UUID = a2.API_UUID " +
                        "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.apiName')) LIKE ? " +
                    " ) " +
                    "AND JSON_VALUE(a1.org, '$.name') = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_PROVIDER_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                        "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                        "WHERE a1.API_UUID = a2.API_UUID " +
                        "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.providerName')) LIKE ? " +
                    ") " +
                    "AND JSON_VALUE(a1.org, '$.name') = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_VERSION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                        "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                        "WHERE a1.API_UUID = a2.API_UUID " +
                        "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.version')) LIKE ? " +
                    ") " +
                    "AND JSON_VALUE(a1.org, '$.name') = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_CONTEXT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                        "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                        "WHERE a1.API_UUID = a2.API_UUID " +
                        "AND LOWER(JSON_QUERY(a2.METADATA, '$.context')) LIKE ? " +
                    ") " +
                    "AND JSON_VALUE(a1.org, '$.name') = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_STATUS_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                        "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                        "WHERE a1.API_UUID = a2.API_UUID " +
                        "AND LOWER(JSON_QUERY(a2.METADATA, '$.status')) LIKE ? " +
                    ") " +
                    "AND JSON_VALUE(a1.org, '$.name') = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_DESCRIPTION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                        "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                        "WHERE a1.API_UUID = a2.API_UUID " +
                        "AND LOWER(JSON_QUERY(a2.METADATA, '$.description')) LIKE ? " +
                    ") " +
                    "AND JSON_VALUE(a1.org, '$.name') = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_TAGS_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                        "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                        "WHERE a1.API_UUID = a2.API_UUID " +
                        "AND LOWER(JSON_QUERY(a2.METADATA, '$.tags')) LIKE ? " +
                    ") " +
                    "AND JSON_VALUE(a1.org, '$.name') = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_API_CATEGORY_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                        "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                        "WHERE a1.API_UUID = a2.API_UUID " +
                        "AND LOWER(JSON_QUERY(a2.METADATA, '$.apiCategories')) LIKE ? " +
                    ") " +
                    "AND JSON_VALUE(a1.org, '$.name') = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static String SEARCH_CONTENT_BY_OTHER_SQL(String propertyName) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                    "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                    "WHERE a1.API_UUID = a2.API_UUID " +
                    "AND LOWER(JSON_QUERY(a2.METADATA, '$." + propertyName + "')) LIKE ? " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static final String GET_ASSOCIATED_TYPE_SQL =
            "SELECT type FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND JSON_VALUE(org, '$.name') = ? " +
            "AND type IN ('API', 'API_PRODUCT') ";

    public static final String SEARCH_API_BY_CONTENT_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(METADATA) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
               ")" +
            "ORDER BY LAST_MODIFIED DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_NAME_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND LOWER(JSON_VALUE(metadata, '$.id.apiName')) LIKE ? " +
            "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
            ") " +
            "ORDER BY LAST_MODIFIED DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_PROVIDER_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND LOWER(JSON_VALUE(metadata, '$.id.providerName')) LIKE ? " +
            "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
            ") " +
            "ORDER BY LAST_MODIFIED DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_VERSION_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND LOWER(JSON_VALUE(metadata, '$.id.version')) LIKE ? " +
            "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
            ") " +
            "ORDER BY LAST_MODIFIED DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_CONTEXT_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND LOWER(JSON_VALUE(metadata, '$.context')) LIKE ? " +
            "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
            ") " +
            "ORDER BY LAST_MODIFIED DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_STATUS_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND LOWER(JSON_VALUE(metadata, '$.status')) LIKE ? " +
            "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
            ") " +
            "ORDER BY LAST_MODIFIED DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_DESCRIPTION_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND LOWER(JSON_VALUE(metadata, '$.description')) LIKE ? " +
            "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
            ") " +
            "ORDER BY LAST_MODIFIED DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_TAGS_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND LOWER(JSON_QUERY(metadata, '$.tags')) LIKE ? " +
            "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
            ") " +
            "ORDER BY LAST_MODIFIED DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_BY_API_CATEGORY_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND LOWER(JSON_QUERY(metadata, '$.apiCategories')) LIKE ? " +
            "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
            ") " +
            "ORDER BY LAST_MODIFIED DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static String SEARCH_API_BY_OTHER_FOR_DEV_PORTAL_SQL(String propertyName) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static final String GET_ALL_API_ARTIFACTS_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
            ") " +
            "ORDER BY LAST_MODIFIED DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_CONTENT_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                        "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                        "WHERE a1.API_UUID = a2.API_UUID " +
                        "AND LOWER(a2.METADATA) LIKE ? " +
                        "AND (" +
                            "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                            "OR " +
                            "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                        ") " +
                    ") " +
                    "AND JSON_VALUE(a1.org, '$.name') = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_NAME_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.apiName')) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
            ") " +
            "AND JSON_VALUE(a1.org, '$.name') = ? " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_PROVIDER_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.providerName')) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
            ") " +
            "AND JSON_VALUE(a1.org, '$.name') = ? " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_VERSION_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.version')) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
            ") " +
            "AND JSON_VALUE(a1.org, '$.name') = ? " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_CONTEXT_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.context')) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
            ") " +
            "AND JSON_VALUE(a1.org, '$.name') = ? " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_STATUS_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.status')) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
            ") " +
            "AND JSON_VALUE(a1.org, '$.name') = ? " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_DESCRIPTION_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.description')) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
            ") " +
            "AND JSON_VALUE(a1.org, '$.name') = ? " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_TAGS_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.tags')) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
            ") " +
            "AND JSON_VALUE(a1.org, '$.name') = ? " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_CONTENT_BY_API_CATEGORY_FOR_DEV_PORTAL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                    "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.apiCategories')) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
            ") " +
            "AND JSON_VALUE(a1.org, '$.name') = ? " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static String SEARCH_CONTENT_BY_OTHER_FOR_DEV_PORTAL_SQL(String propertyName) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$." + propertyName + "')) LIKE ? " +
                "AND (" +
                    "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
            ") " +
            "AND JSON_VALUE(a1.org, '$.name') = ? " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static final String SEARCH_API_PRODUCT_BY_CONTENT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND LOWER(metadata) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_PRODUCT_BY_NAME_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND LOWER(JSON_VALUE(metadata, '$.id.apiName')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_PRODUCT_BY_PROVIDER_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.id.providerName')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_PRODUCT_BY_VERSION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.id.version')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_PRODUCT_BY_CONTEXT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.context')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_PRODUCT_BY_STATUS_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.status')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_PRODUCT_BY_DESCRIPTION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.description')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_PRODUCT_BY_TAGS_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.tags')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String SEARCH_API_PRODUCT_BY_API_CATEGORY_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND LOWER(JSON_QUERY(METADATA, '$.apiCategories')) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static String SEARCH_API_PRODUCT_BY_OTHER_SQL(String propertyName) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static final String GET_ALL_API_PRODUCT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "ORDER BY LAST_MODIFIED DESC " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String GET_API_PRODUCT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT' " +
                    "AND API_UUID = ? ";

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
}