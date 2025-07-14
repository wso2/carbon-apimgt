package org.wso2.carbon.apimgt.persistence.dao.queries.impl;

import org.wso2.carbon.apimgt.persistence.dao.queries.SQLQueryInterface;

public class Oracle implements SQLQueryInterface {
    private static String getRoleConditionForPublisher(String[] roles) {
        StringBuilder roleCondition = new StringBuilder();
        for (String role : roles) {
            if (roleCondition.length() > 0) {
                roleCondition.append(" OR ");
            }
            roleCondition.append("',' || JSON_VALUE(METADATA, '$.accessControlRoles') || ',' LIKE '%,").append(role.toLowerCase()).append(",%'");
        }
        roleCondition.append(" OR JSON_VALUE(METADATA, '$.accessControl') = 'all' ");
        return roleCondition.toString();
    }

    private static String getRoleConditionForDevPortal(String[] roles) {
        StringBuilder roleCondition = new StringBuilder();
        for (String role : roles) {
            if (roleCondition.length() > 0) {
                roleCondition.append(" OR ");
            }
            roleCondition.append("',' || JSON_VALUE(METADATA, '$.visibleRoles') || ',' LIKE '%,").append(role.toLowerCase()).append(",%'");
        }
        roleCondition.append(" OR JSON_VALUE(METADATA, '$.visibility') = 'public' ");
        return roleCondition.toString();
    }
    
    private static final String ADD_ARTIFACT_SQL =
            "INSERT INTO AM_ARTIFACT_DATA (type, org, metadata, uuid, api_uuid) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String GET_API_BY_UUID_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API'";

    private static final String GET_SWAGGER_DEFINITION_BY_UUID_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? AND API_UUID = ? " +
                    "AND TYPE = 'API_DEFINITION'";

    private static final String GET_DOCUMENTATION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND UUID = ? ";

    private static final String GET_ALL_DOCUMENTATION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND API_UUID = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String SEARCH_DOCUMENTATION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND API_UUID = ? " +
                    "AND LOWER(metadata) LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String GET_DOCUMENTATION_COUNT =
            "SELECT COUNT(*) AS TOTAL_DOC_COUNT FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND API_UUID = ?";

    private static final String ADD_DOCUMENTATION_FILE_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "ARTIFACT = ? " +
                    "WHERE UUID = ? ";

    private static final String ADD_METADATA_FOR_FILE_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "METADATA = JSON_TRANSFORM( " +
                    "METADATA, " +
                    "SET '$.fileType' = ?, " +
                    "SET '$.fileName' = ?) " +
                    "WHERE UUID = ? ";

    private static final String ADD_DOCUMENTATION_CONTENT_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "METADATA = JSON_TRANSFORM( " +
                    "METADATA, " +
                    "SET '$.textContent' = ?) " +
                    "WHERE UUID = ? ";

    private static final String GET_DOCUMENTATION_CONTENT_SQL =
            "SELECT metadata FROM AM_ARTIFACT_DATA " +
                    "WHERE UUID = ? ";

    private static final String GET_DOCUMENTATION_FILE_SQL =
            "SELECT artifact FROM AM_ARTIFACT_DATA " +
                    "WHERE UUID = ? ";

    private static final String DELETE_DOCUMENTATION_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
                    "WHERE type = 'DOCUMENTATION' " +
                    "AND UUID = ? ";

    private static final String SAVE_OAS_DEFINITION_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API_DEFINITION'";

    private static final String DELETE_API_SCHEMA_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
                    "WHERE API_UUID = ? ";

    private static final String ADD_FILE_ARTIFACT_SQL =
            "INSERT INTO AM_ARTIFACT_DATA (type, org, metadata, uuid, artifact, api_uuid) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String GET_THUMBNAIL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE type = 'THUMBNAIL' " +
                    "AND API_UUID = ? " +
                    "AND JSON_VALUE(org, '$.name') = ? ";

    private static final String SAVE_ASYNC_API_DEFINITION_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'ASYNC_API_DEFINITION'";

    private static final String GET_ASYNC_API_DEFINITION_BY_UUID_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND TYPE = 'ASYNC_API_DEFINITION' " +
            "AND JSON_VALUE(org, '$.name') = ? ";

    private static final String DELETE_THUMBNAIL_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
            "WHERE type = 'THUMBNAIL' " +
            "AND API_UUID = ? " +
            "AND JSON_VALUE(org, '$.name') = ? ";

    private static final String GET_WSDL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE type = 'WSDL' " +
            "AND API_UUID = ? " +
            "AND JSON_VALUE(org, '$.name') = ? ";

    private static final String UPDATE_GRAPHQL_SCHEMA_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
            "METADATA = ? " +
            "WHERE API_UUID = ? " +
            "AND TYPE = 'GRAPHQL_SCHEMA'";

    private static final String GET_GRAPHQL_SCHEMA_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND TYPE = 'GRAPHQL_SCHEMA' " +
            "AND JSON_VALUE(org, '$.name') = ? ";

    private static final String ADD_API_REVISION_SQL =
            "INSERT INTO AM_REVISION_ARTIFACT (TYPE, ORG, API_UUID, REVISION_UUID, REVISION_ID, METADATA) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String ADD_API_REVISION_ARTIFACT_SQL =
            "INSERT INTO AM_REVISION_ARTIFACT (TYPE, ORG, API_UUID, REVISION_UUID, REVISION_ID, ARTIFACT, METADATA) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String GET_API_REVISION_BY_ID_SQL =
            "SELECT * FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND TYPE = 'API' " +
            "AND JSON_VALUE(ORG, '$.name') = ? ";

    private static final String GET_API_REVISION_SWAGGER_DEFINITION_BY_ID_SQL =
            "SELECT * FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND TYPE = 'API_DEFINITION' " +
            "AND JSON_VALUE(ORG, '$.name') = ? ";

    private static final String GET_API_REVISION_ASYNC_DEFINITION_BY_ID_SQL =
            "SELECT * FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND TYPE = 'ASYNC_API_DEFINITION' " +
            "AND JSON_VALUE(ORG, '$.name') = ? ";

    private static final String GET_API_LIFECYCLE_STATUS_SQL =
            "SELECT JSON_VALUE(metadata, '$.status') AS STATUS FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND JSON_VALUE(ORG, '$.name') = ? " +
            "AND TYPE = 'API' ";

    private static final String UPDATE_API_SQL =
            "UPDATE AM_ARTIFACT_DATA " +
                    "SET METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API' " ;

    private static final String UPDATE_SWAGGER_DEFINITION_SQL =
            "UPDATE AM_ARTIFACT_DATA " +
                    "SET METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API_DEFINITION' ";

    private static final String UPDATE_ASYNC_DEFINITION_SQL =
            "UPDATE AM_ARTIFACT_DATA " +
                    "SET METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'ASYNC_API_DEFINITION' ";

    private static final String GET_API_REVISION_THUMBNAIL_SQL =
            "SELECT * FROM AM_REVISION_ARTIFACT " +
            "WHERE TYPE = 'THUMBNAIL' " +
            "AND REVISION_UUID = ? " +
            "AND JSON_VALUE(ORG, '$.name') = ? ";

    private static final String UPDATE_THUMBNAIL_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
            "ARTIFACT = ? " +
            "WHERE TYPE = 'THUMBNAIL' " +
            "AND API_UUID = ? ";

    private static final String DELETE_API_REVISION_SQL =
            "DELETE FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " ;

    private static final String UPDATE_DOCUMENTATION_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
            "METADATA = ? " +
            "WHERE UUID = ? ";

    private static final String GET_API_PRODUCT_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API_PRODUCT' " +
                    "AND JSON_VALUE(org, '$.name') = ? ";

    private static final String UPDATE_API_PRODUCT_SQL =
            "UPDATE AM_ARTIFACT_DATA " +
                    "SET METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API_PRODUCT' ";

    private static final String GET_API_PRODUCT_COUNT_SQL =
            "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND type = 'API_PRODUCT'";

    private static final String DELETE_API_PRODUCT_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
                    "WHERE type = 'API_PRODUCT' " +
                    "AND API_UUID = ? " +
                    "AND JSON_VALUE(org, '$.name') = ? ";

    private static final String DELETE_API_PRODUCT_SWAGGER_DEFINITION_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
                    "WHERE type = 'API_DEFINITION' " +
                    "AND API_UUID = ? ";

    private static final String GET_ALL_DOCUMENTS_FOR_API_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'DOCUMENTATION' " ;

    private static final String GET_ALL_API_REVISION_IDS_SQL =
            "SELECT REVISION_UUID FROM AM_REVISION_ARTIFACT " +
            "WHERE API_UUID = ? " +
            "AND TYPE IN ('API', 'API_PRODUCT') ";

    private static final String CHECK_API_EXISTS_SQL =
            "SELECT COUNT(*) as count FROM AM_ARTIFACT_DATA " +
            "WHERE JSON_VALUE(org, '$.name') = ? " +
            "AND type IN ('API', 'API_PRODUCT') " +
            "AND API_UUID = ? ";

    private static final String GET_ALL_TAGS_SQL =
            "SELECT DISTINCT JSON_QUERY(METADATA, '$.tags') AS TAGS " +
                    "FROM AM_ARTIFACT_DATA " +
                    "WHERE JSON_VALUE(org, '$.name') = ? " +
                    "AND JSON_QUERY(METADATA, '$.tags') IS NOT NULL " +
                    "AND (" +
                    "(type = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                    "OR " +
                    "(type = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED')" +
                    ")";

    private static final String GET_API_UUID_BY_REVISION_UUID_SQL =
            "SELECT API_UUID FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND JSON_VALUE(ORG, '$.name') = ? " +
            "AND TYPE IN ('API', 'API_PRODUCT') ";

    private static final String GET_ARTIFACT_TYPE_BY_UUID_SQL =
            "SELECT TYPE FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND JSON_VALUE(org, '$.name') = ? " +
            "AND TYPE IN ('API', 'API_PRODUCT') ";

    private static final String GET_SECURITY_SCHEME_BY_UUID_SQL =
            "SELECT JSON_QUERY(METADATA, '$.apiSecurity') as API_SECURITY_SCHEME FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND JSON_VALUE(org, '$.name') = ? " +
            "AND TYPE IN ('API', 'API_PRODUCT') ";

    @Override
    public String getAddArtifactSQL() {
        return ADD_ARTIFACT_SQL;
    }

    @Override
    public String getGetAPIByUUIDSQL() {
        return GET_API_BY_UUID_SQL;
    }

    @Override
    public String getGetSwaggerDefinitionByUUIDSQL() {
        return GET_SWAGGER_DEFINITION_BY_UUID_SQL;
    }

    @Override
    public String getGetDocumentationSQL() {
        return GET_DOCUMENTATION_SQL;
    }

    @Override
    public String getGetAllDocumentationSQL() {
        return GET_ALL_DOCUMENTATION_SQL;
    }

    @Override
    public String getSearchDocumentationSQL() {
        return SEARCH_DOCUMENTATION_SQL;
    }

    @Override
    public String getGetDocumentationCount() {
        return GET_DOCUMENTATION_COUNT;
    }

    @Override
    public String getAddDocumentationFileSQL() {
        return ADD_DOCUMENTATION_FILE_SQL;
    }

    @Override
    public String getAddMetadataForFileSQL() {
        return ADD_METADATA_FOR_FILE_SQL;
    }

    @Override
    public String getAddDocumentationContentSQL() {
        return ADD_DOCUMENTATION_CONTENT_SQL;
    }

    @Override
    public String getGetDocumentationContentSQL() {
        return GET_DOCUMENTATION_CONTENT_SQL;
    }

    @Override
    public String getGetDocumentationFileSQL() {
        return GET_DOCUMENTATION_FILE_SQL;
    }

    @Override
    public String getDeleteDocumentationSQL() {
        return DELETE_DOCUMENTATION_SQL;
    }

    @Override
    public String getSaveOASDefinitionSQL() {
        return SAVE_OAS_DEFINITION_SQL;
    }

    @Override
    public String getDeleteAPISchemaSQL() {
        return DELETE_API_SCHEMA_SQL;
    }

    @Override
    public String getAddFileArtifactSQL() {
        return ADD_FILE_ARTIFACT_SQL;
    }

    @Override
    public String getGetThumbnailSQL() {
        return GET_THUMBNAIL_SQL;
    }

    @Override
    public String getSaveAsyncAPIDefinitionSQL() {
        return SAVE_ASYNC_API_DEFINITION_SQL;
    }

    @Override
    public String getGetAsyncAPIDefinitionByUUIDSQL() {
        return GET_ASYNC_API_DEFINITION_BY_UUID_SQL;
    }

    @Override
    public String getDeleteThumbnailSQL() {
        return DELETE_THUMBNAIL_SQL;
    }

    @Override
    public String getGetWSDLSQL() {
        return GET_WSDL_SQL;
    }

    @Override
    public String getUpdateGraphQLSchemaSQL() {
        return UPDATE_GRAPHQL_SCHEMA_SQL;
    }

    @Override
    public String getGetGraphQLSchemaSQL() {
        return GET_GRAPHQL_SCHEMA_SQL;
    }

    @Override
    public String getAddAPIRevisionSQL() {
        return ADD_API_REVISION_SQL;
    }

    @Override
    public String getAddAPIRevisionArtifactSQL() {
        return ADD_API_REVISION_ARTIFACT_SQL;
    }

    @Override
    public String getGetAPIRevisionByIdSQL() {
        return GET_API_REVISION_BY_ID_SQL;
    }

    @Override
    public String getGetAPIRevisionSwaggerDefinitionByIdSQL() {
        return GET_API_REVISION_SWAGGER_DEFINITION_BY_ID_SQL;
    }

    @Override
    public String getGetAPIRevisionAsyncDefinitionByIdSQL() {
        return GET_API_REVISION_ASYNC_DEFINITION_BY_ID_SQL;
    }

    @Override
    public String getGetAPILifecycleStatusSQL() {
        return GET_API_LIFECYCLE_STATUS_SQL;
    }

    @Override
    public String getUpdateAPISQL() {
        return UPDATE_API_SQL;
    }

    @Override
    public String getUpdateSwaggerDefinitionSQL() {
        return UPDATE_SWAGGER_DEFINITION_SQL;
    }

    @Override
    public String getUpdateAsyncDefinitionSQL() {
        return UPDATE_ASYNC_DEFINITION_SQL;
    }

    @Override
    public String getGetAPIRevisionThumbnailSQL() {
        return GET_API_REVISION_THUMBNAIL_SQL;
    }

    @Override
    public String getUpdateThumbnailSQL() {
        return UPDATE_THUMBNAIL_SQL;
    }

    @Override
    public String getDeleteAPIRevisionSQL() {
        return DELETE_API_REVISION_SQL;
    }

    @Override
    public String getUpdateDocumentationSQL() {
        return UPDATE_DOCUMENTATION_SQL;
    }

    @Override
    public String getGetAPIProductSQL() {
        return GET_API_PRODUCT_SQL;
    }

    @Override
    public String getUpdateAPIProductSQL() {
        return UPDATE_API_PRODUCT_SQL;
    }

    @Override
    public String getGetAPIProductCountSQL() {
        return GET_API_PRODUCT_COUNT_SQL;
    }

    @Override
    public String getDeleteAPIProductSQL() {
        return DELETE_API_PRODUCT_SQL;
    }

    @Override
    public String getDeleteAPIProductSwaggerDefinitionSQL() {
        return DELETE_API_PRODUCT_SWAGGER_DEFINITION_SQL;
    }

    @Override
    public String getGetAllDocumentsForAPISQL() {
        return GET_ALL_DOCUMENTS_FOR_API_SQL;
    }

    @Override
    public String getGetAllAPIRevisionIdsSQL() {
        return GET_ALL_API_REVISION_IDS_SQL;
    }

    @Override
    public String getCheckAPIExistsSQL() {
        return CHECK_API_EXISTS_SQL;
    }

    @Override
    public String getGetAllTagsSQL() {
        return GET_ALL_TAGS_SQL;
    }

    @Override
    public String getGetAPIUUIDByRevisionUUIDSQL() {
        return GET_API_UUID_BY_REVISION_UUID_SQL;
    }

    @Override
    public String getGetArtifactTypeByUUIDSQL() {
        return GET_ARTIFACT_TYPE_BY_UUID_SQL;
    }

    @Override
    public String getGetSecuritySchemeByUUIDSQL() {
        return GET_SECURITY_SCHEME_BY_UUID_SQL;
    }

    @Override
    public String getAllApiArtifactSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND TYPE = 'API' " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String getAllApiCountSql(String[] roles) {
        return "SELECT COUNT(*) AS TOTAL_API_COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByContentSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(metadata) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByContentCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(metadata) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByNameSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_VALUE(metadata, '$.id.apiName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByNameCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_VALUE(metadata, '$.id.apiName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByProviderSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.providerName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles)+ ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByProviderCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.providerName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByVersionSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.version')) LIKE ?" +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByVersionCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.version')) LIKE ?" +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByContextSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.context')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByContextCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.context')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByStatusSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.status')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByStatusCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.status')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByDescriptionSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.description')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByDescriptionCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.description')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByTagsSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.tags')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByTagsCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.tags')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByApiCategorySql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.apiCategories')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByApiCategoryCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.apiCategories')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByOtherSql(String propertyName, String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByOtherCountSql(String propertyName, String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchContentByContentSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(a2.METADATA) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByNameSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.apiName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                " ) " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByProviderSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.providerName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByVersionSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.version')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByContextSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.context')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByStatusSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.status')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByDescriptionSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.description')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByTagsSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.tags')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByApiCategorySql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.apiCategories')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByOtherSql(String propertyName, String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$." + propertyName + "')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByContentSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(metadata) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByNameSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_VALUE(metadata, '$.id.apiName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByProviderSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.providerName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByVersionSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.version')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByContextSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.context')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByStatusSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.status')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByDescriptionSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.description')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByTagsSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.tags')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByApiCategorySql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.apiCategories')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByOtherSql(String propertyName, String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String getAllApiProductSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByContentForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(METADATA) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ")" +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByNameForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(JSON_VALUE(metadata, '$.id.apiName')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByProviderForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(JSON_VALUE(metadata, '$.id.providerName')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByVersionForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(JSON_VALUE(metadata, '$.id.version')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByContextForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(JSON_VALUE(metadata, '$.context')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByStatusForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(JSON_VALUE(metadata, '$.status')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByDescriptionForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(JSON_VALUE(metadata, '$.description')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByTagsForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(JSON_QUERY(metadata, '$.tags')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByApiCategoryForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(JSON_QUERY(metadata, '$.apiCategories')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByOtherForDevPortalSql(String propertyName, String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String getAllApiArtifactsForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE (" +
                "(TYPE = 'API' AND JSON_VALUE(METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND JSON_VALUE(org, '$.name') = ? " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC"
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByContentForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(a2.METADATA) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByNameForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.apiName')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByProviderForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.providerName')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByVersionForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.id.version')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByContextForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.context')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByStatusForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.status')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByDescriptionForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.description')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByTagsForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.tags')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByApiCategoryForDevPortalSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA a1 " +
                "WHERE EXISTS ( " +
                "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
                "WHERE a1.API_UUID = a2.API_UUID " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.apiCategories')) LIKE ? " +
                "AND (" +
                "(TYPE = 'API' AND JSON_VALUE(a2.METADATA, '$.status') = 'PUBLISHED') " +
                "OR " +
                "(TYPE = 'API_PRODUCT' AND JSON_VALUE(a2.METADATA, '$.state') = 'PUBLISHED') " +
                ") " +
                "AND (" + getRoleConditionForDevPortal(roles) + ")" +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByOtherForDevPortalSql(String propertyName, String[] roles) {
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
                "AND (" + getRoleConditionForDevPortal(roles) + ")" +
                ") " +
                "AND JSON_VALUE(a1.org, '$.name') = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }
}
