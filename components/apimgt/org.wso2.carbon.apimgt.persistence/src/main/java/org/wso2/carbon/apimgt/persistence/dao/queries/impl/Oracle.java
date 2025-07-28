package org.wso2.carbon.apimgt.persistence.dao.queries.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.persistence.dao.queries.SQLQueryInterface;

import java.util.Arrays;
import java.util.List;

public class Oracle implements SQLQueryInterface {
    private static final Log log = LogFactory.getLog(SQLQueryInterface.class);

    private static String getRoleConditionForPublisher(String[] roles) {
        StringBuilder roleCondition = new StringBuilder();
        for (String role : roles) {
            if (roleCondition.length() > 0) {
                roleCondition.append(" OR ");
            }
            roleCondition.append("access_roles LIKE '%").append(role.toLowerCase()).append("%'");
        }
        roleCondition.append(" OR access_control = 'all' ");
        return roleCondition.toString();
    }

    private static String getRoleConditionForDevPortal(String[] roles) {
        StringBuilder roleCondition = new StringBuilder();
        for (String role : roles) {
            if (roleCondition.length() > 0) {
                roleCondition.append(" OR ");
            }
            roleCondition.append("visible_roles LIKE '%").append(role.toLowerCase()).append("%'");
        }
        roleCondition.append(" OR visibility = 'public' ");
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
                    "WHERE ORG_NAME = ? AND API_UUID = ? " +
                    "AND TYPE = 'API_DEFINITION'";

    private static final String GET_DOCUMENTATION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE ORG_NAME = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND UUID = ? ";

    private static final String GET_ALL_DOCUMENTATION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE ORG_NAME = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND API_UUID = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String SEARCH_DOCUMENTATION_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
                    "WHERE ORG_NAME = ? " +
                    "AND type = 'DOCUMENTATION' " +
                    "AND API_UUID = ? " +
                    "AND CONTAINS(metadata, ?) > 0 " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    private static final String GET_DOCUMENTATION_COUNT =
            "SELECT COUNT(*) AS TOTAL_DOC_COUNT FROM AM_ARTIFACT_DATA " +
                    "WHERE ORG_NAME = ? " +
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
                    "SET '$.fileName' = ?, " +
                    "SET '$.textContent' = ? ) " +
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
                    "AND ORG_NAME = ? ";

    private static final String SAVE_ASYNC_API_DEFINITION_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
                    "METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'ASYNC_API_DEFINITION'";

    private static final String GET_ASYNC_API_DEFINITION_BY_UUID_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND TYPE = 'ASYNC_API_DEFINITION' " +
            "AND ORG_NAME = ? ";

    private static final String DELETE_THUMBNAIL_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
            "WHERE type = 'THUMBNAIL' " +
            "AND API_UUID = ? " +
            "AND ORG_NAME = ? ";

    private static final String GET_WSDL_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE type = 'WSDL' " +
            "AND API_UUID = ? " +
            "AND ORG_NAME = ? ";

    private static final String UPDATE_GRAPHQL_SCHEMA_SQL =
            "UPDATE AM_ARTIFACT_DATA SET " +
            "METADATA = ? " +
            "WHERE API_UUID = ? " +
            "AND TYPE = 'GRAPHQL_SCHEMA'";

    private static final String GET_GRAPHQL_SCHEMA_SQL =
            "SELECT * FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND TYPE = 'GRAPHQL_SCHEMA' " +
            "AND ORG_NAME = ? ";

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
            "AND ORG_NAME = ? ";

    private static final String GET_API_REVISION_SWAGGER_DEFINITION_BY_ID_SQL =
            "SELECT * FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND TYPE = 'API_DEFINITION' " +
            "AND ORG_NAME = ? ";

    private static final String GET_API_REVISION_ASYNC_DEFINITION_BY_ID_SQL =
            "SELECT * FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND TYPE = 'ASYNC_API_DEFINITION' " +
            "AND ORG_NAME = ? ";

    private static final String GET_API_LIFECYCLE_STATUS_SQL =
            "SELECT JSON_VALUE(metadata, '$.status') AS STATUS FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND ORG_NAME = ? " +
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
            "AND ORG_NAME = ? ";

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
                    "AND ORG_NAME = ? ";

    private static final String UPDATE_API_PRODUCT_SQL =
            "UPDATE AM_ARTIFACT_DATA " +
                    "SET METADATA = ? " +
                    "WHERE API_UUID = ? " +
                    "AND TYPE = 'API_PRODUCT' ";

    private static final String GET_API_PRODUCT_COUNT_SQL =
            "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                    "WHERE ORG_NAME = ? " +
                    "AND type = 'API_PRODUCT'";

    private static final String DELETE_API_PRODUCT_SQL =
            "DELETE FROM AM_ARTIFACT_DATA " +
                    "WHERE type = 'API_PRODUCT' " +
                    "AND API_UUID = ? " +
                    "AND ORG_NAME = ? ";

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
            "WHERE ORG_NAME = ? " +
            "AND type IN ('API', 'API_PRODUCT') " +
            "AND API_UUID = ? ";

    private static final String GET_ALL_TAGS_SQL =
            "SELECT DISTINCT JSON_QUERY(METADATA, '$.tags') AS TAGS " +
                    "FROM AM_ARTIFACT_DATA " +
                    "WHERE ORG_NAME = ? " +
                    "AND API_TAGS IS NOT NULL " +
                    "AND API_STATUS = 'published'";

    private static final String GET_API_UUID_BY_REVISION_UUID_SQL =
            "SELECT API_UUID FROM AM_REVISION_ARTIFACT " +
            "WHERE REVISION_UUID = ? " +
            "AND ORG_NAME = ? " +
            "AND TYPE IN ('API', 'API_PRODUCT') ";

    private static final String GET_ARTIFACT_TYPE_BY_UUID_SQL =
            "SELECT TYPE FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND ORG_NAME = ? " +
            "AND TYPE IN ('API', 'API_PRODUCT') ";

    private static final String GET_SECURITY_SCHEME_BY_UUID_SQL =
            "SELECT JSON_QUERY(METADATA, '$.apiSecurity') as API_SECURITY_SCHEME FROM AM_ARTIFACT_DATA " +
            "WHERE API_UUID = ? " +
            "AND ORG_NAME = ? " +
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
        log.debug("Retrieving all API artifacts for roles: " + Arrays.toString(roles));
        return "SELECT METADATA FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND TYPE = 'API' " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String getAllApiCountSql(String[] roles) {
        log.debug("Retrieving count of all APIs for roles: " + Arrays.toString(roles));
        return "SELECT COUNT(UUID) AS TOTAL_API_COUNT FROM ( " +
                    "SELECT UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE TYPE = 'API' " +
                    "AND ORG_NAME = ? " +
                    "AND ACCESS_CONTROL = 'all' " +
                    "UNION ALL " +
                    "SELECT UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE TYPE = 'API' " +
                    "AND ORG_NAME = ? " +
                    "AND ACCESS_ROLES IS NOT NULL " +
                    "AND (" + getRoleConditionForPublisher(roles) + ") " +
                ")";
    }

    @Override
    public String searchApiByContentSql(String[] roles) {
        log.debug("Searching APIs by content for roles: " + Arrays.toString(roles));
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND CONTAINS(metadata, ?) > 0 " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByContentCountSql(String[] roles) {
        log.debug("Retrieving count of APIs matching content search for roles: " + Arrays.toString(roles));
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND CONTAINS(metadata, ?) > 0 " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByNameSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_NAME LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByNameCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_NAME LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByProviderSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND PROVIDER_NAME LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles)+ ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByProviderCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND PROVIDER_NAME LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByVersionSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_VERSION LIKE ?" +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByVersionCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_VERSION LIKE ?" +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByContextSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_CONTEXT LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByContextCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_CONTEXT LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByStatusSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_STATUS LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByStatusCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_STATUS LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByDescriptionSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_DESCRIPTION LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByDescriptionCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_DESCRIPTION LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByTagsSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_TAGS LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByTagsCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_TAGS LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByApiCategorySql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_CATEGORIES LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByApiCategoryCountSql(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND API_CATEGORIES LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchApiByOtherSql(String propertyName, String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByOtherCountSql(String propertyName, String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    @Override
    public String searchContentByContentSql(String[] roles, String searchContent) {
        List<String> searchTerms = List.of(searchContent.split(" "));
        String searchQuery;

        if (searchTerms.size() > 1) {
            StringBuilder queryBuilder = new StringBuilder();
            for (String term : searchTerms) {
                queryBuilder.append("FUZZY(").append(term).append(")").append(" AND ");
            }
            queryBuilder.setLength(queryBuilder.length() - 5); // Remove the last " AND "
            searchQuery = queryBuilder.toString();
        } else {
            searchQuery = "FUZZY(" + searchContent + ")";
        }

        return "SELECT a1.metadata, a1.api_uuid, a1.type, a1.uuid " +
                "FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT API_UUID " +
                    "FROM ( " +
                        "SELECT DISTINCT API_UUID " +
                        "FROM AM_ARTIFACT_DATA " +
                        "WHERE CONTAINS(METADATA, '" + searchQuery + "') > 0 " +
                    ") metadata_uuids " +
                    "INTERSECT " +
                    "SELECT DISTINCT API_UUID " +
                    "FROM AM_ARTIFACT_DATA " +
                    "WHERE TYPE = 'API' " +
                    "AND (" +
                        getRoleConditionForPublisher(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByNameSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_NAME LIKE ? " +
                    "AND (" +
                        getRoleConditionForPublisher(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByProviderSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE PROVIDER_NAME LIKE ? " +
                    "AND (" +
                        getRoleConditionForPublisher(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByVersionSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_VERSION LIKE ? " +
                    "AND (" +
                        getRoleConditionForPublisher(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByContextSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_CONTEXT LIKE ? " +
                    "AND (" +
                        getRoleConditionForPublisher(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByStatusSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_STATUS LIKE ? " +
                    "AND (" +
                        getRoleConditionForPublisher(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByDescriptionSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_DESCRIPTION LIKE ? " +
                    "AND (" +
                        getRoleConditionForPublisher(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByTagsSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_TAGS LIKE ? " +
                    "AND (" +
                        getRoleConditionForPublisher(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByApiCategorySql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_CATEGORIES LIKE ? " +
                    "AND (" +
                        getRoleConditionForPublisher(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByOtherSql(String propertyName, String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE LOWER(JSON_QUERY(METADATA, '$." + propertyName + "')) LIKE ? " +
                    "AND (" +
                        getRoleConditionForPublisher(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByContentSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND CONTAINS(metadata, ?) > 0 " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByNameSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND API_NAME LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByProviderSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND PROVIDER_NAME LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByVersionSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND API_VERSION LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByContextSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND API_CONTEXT LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByStatusSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND API_STATUS LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByDescriptionSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND API_DESCRIPTION LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByTagsSql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND API_TAGS LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByApiCategorySql(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND API_CATEGORIES LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiProductByOtherSql(String propertyName, String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String getAllApiProductSql(String[] roles) {
        log.debug("Retrieving all API Products for roles: " + Arrays.toString(roles));
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE ORG_NAME = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByContentForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE CONTAINS(METADATA, ?) > 0 " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE IN ('API', 'API_PRODUCT') " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByNameForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_NAME LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE IN ('API', 'API_PRODUCT') " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByProviderForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE PROVIDER_NAME LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE IN ('API', 'API_PRODUCT') " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByVersionForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_VERSION LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE IN ('API', 'API_PRODUCT') " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByContextForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_CONTEXT LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE IN ('API', 'API_PRODUCT') " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByStatusForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_STATUS LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE IN ('API', 'API_PRODUCT') " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByDescriptionForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_DESCRIPTION LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE IN ('API', 'API_PRODUCT') " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByTagsForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_TAGS LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE IN ('API', 'API_PRODUCT') " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByApiCategoryForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_CATEGORIES LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE IN ('API', 'API_PRODUCT') " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchApiByOtherForDevPortalSql(String propertyName, String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE IN ('API', 'API_PRODUCT') " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String getAllApiArtifactsForDevPortalSql(String[] roles) {
        log.debug("Retrieving all API artifacts for Developer Portal with roles: " + Arrays.toString(roles));
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE API_STATUS = 'published' " +
                "AND ORG_NAME = ? " +
                "AND (" + getRoleConditionForDevPortal(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC"
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByContentForDevPortalSql(String[] roles, String searchContent) {
        List<String> searchTerms = List.of(searchContent.split(" "));

        String searchQuery;

        if (searchTerms.size() > 1) {
            StringBuilder queryBuilder = new StringBuilder();
            for (String term : searchTerms) {
                queryBuilder.append("FUZZY(").append(term).append(")").append(" AND ");
            }
            queryBuilder.setLength(queryBuilder.length() - 5); // Remove the last " AND "
            searchQuery = queryBuilder.toString();
        } else {
            searchQuery = "FUZZY(" + searchContent + ")";
        }

        return "SELECT a1.metadata, a1.api_uuid, a1.type, a1.uuid " +
                "FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                "SELECT API_UUID " +
                "FROM ( " +
                "SELECT DISTINCT API_UUID " +
                "FROM AM_ARTIFACT_DATA " +
                "WHERE CONTAINS(METADATA, '" + searchQuery + "') > 0 " +
                ") metadata_uuids " +
                "INTERSECT " +
                "SELECT DISTINCT API_UUID " +
                "FROM AM_ARTIFACT_DATA " +
                "WHERE TYPE = 'API' " +
                "AND API_STATUS = 'published' " +
                "AND (" +
                getRoleConditionForDevPortal(roles) +
                ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByNameForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_NAME LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByProviderForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE PROVIDER_NAME LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByVersionForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_VERSION LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByContextForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_CONTEXT LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByStatusForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_STATUS LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByDescriptionForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_DESCRIPTION LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByTagsForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_TAGS LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByApiCategoryForDevPortalSql(String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "WHERE API_CATEGORIES LIKE ? " +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public String searchContentByOtherForDevPortalSql(String propertyName, String[] roles) {
        return "SELECT a1.* FROM AM_ARTIFACT_DATA a1 " +
                "JOIN (" +
                    "SELECT DISTINCT API_UUID FROM AM_ARTIFACT_DATA " +
                    "AND LOWER(JSON_QUERY(METADATA, '$." + propertyName + "')) LIKE ? "  +
                    "AND (" +
                        getRoleConditionForDevPortal(roles) +
                    ") " +
                ") filtered_api " +
                "ON a1.API_UUID = filtered_api.API_UUID " +
                "WHERE a1.ORG_NAME = ? " +
                "AND TYPE != 'THUMBNAIL' " +
                "AND API_STATUS = 'published' " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }
}
