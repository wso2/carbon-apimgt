package org.wso2.carbon.apimgt.persistence.dao.queries;

public interface SQLQueryInterface {
    String getAddArtifactSQL();
    String getGetAPIByUUIDSQL();
    String getGetSwaggerDefinitionByUUIDSQL();
    String getGetDocumentationSQL();
    String getGetAllDocumentationSQL();
    String getSearchDocumentationSQL();
    String getGetDocumentationCount();
    String getAddDocumentationFileSQL();
    String getAddMetadataForFileSQL();
    String getAddDocumentationContentSQL();
    String getGetDocumentationContentSQL();
    String getGetDocumentationFileSQL();
    String getDeleteDocumentationSQL();
    String getSaveOASDefinitionSQL();
    String getDeleteAPISchemaSQL();
    String getAddFileArtifactSQL();
    String getGetThumbnailSQL();
    String getSaveAsyncAPIDefinitionSQL();
    String getGetAsyncAPIDefinitionByUUIDSQL();
    String getDeleteThumbnailSQL();
    String getGetWSDLSQL();
    String getUpdateGraphQLSchemaSQL();
    String getGetGraphQLSchemaSQL();
    String getAddAPIRevisionSQL();
    String getAddAPIRevisionArtifactSQL();
    String getGetAPIRevisionByIdSQL();
    String getGetAPIRevisionSwaggerDefinitionByIdSQL();
    String getGetAPIRevisionAsyncDefinitionByIdSQL();
    String getGetAPILifecycleStatusSQL();
    String getUpdateAPISQL();
    String getUpdateSwaggerDefinitionSQL();
    String getUpdateAsyncDefinitionSQL();
    String getGetAPIRevisionThumbnailSQL();
    String getUpdateThumbnailSQL();
    String getDeleteAPIRevisionSQL();
    String getUpdateDocumentationSQL();
    String getGetAPIProductSQL();
    String getUpdateAPIProductSQL();
    String getGetAPIProductCountSQL();
    String getDeleteAPIProductSQL();
    String getDeleteAPIProductSwaggerDefinitionSQL();
    String getGetAllDocumentsForAPISQL();
    String getGetAllAPIRevisionIdsSQL();
    String getCheckAPIExistsSQL();
    String getGetAllTagsSQL();
    String getGetAPIUUIDByRevisionUUIDSQL();
    String getGetArtifactTypeByUUIDSQL();
    String getGetSecuritySchemeByUUIDSQL();

    /**
     * Returns SQL query to get all API artifacts.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String getAllApiArtifactSql(String[] roles);

    /**
     * Returns SQL query to get total API count.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String getAllApiCountSql(String[] roles);

    /**
     * Returns SQL query to search APIs by content.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByContentSql(String[] roles);

    /**
     * Returns SQL query to count APIs by content search.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByContentCountSql(String[] roles);

    /**
     * Returns SQL query to search APIs by name.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByNameSql(String[] roles);

    /**
     * Returns SQL query to count APIs by name search.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByNameCountSql(String[] roles);

    /**
     * Returns SQL query to search APIs by provider.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByProviderSql(String[] roles);

    /**
     * Returns SQL query to count APIs by provider search.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByProviderCountSql(String[] roles);

    /**
     * Returns SQL query to search APIs by version.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByVersionSql(String[] roles);

    /**
     * Returns SQL query to count APIs by version search.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByVersionCountSql(String[] roles);

    /**
     * Returns SQL query to search APIs by context.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByContextSql(String[] roles);

    /**
     * Returns SQL query to count APIs by context search.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByContextCountSql(String[] roles);

    /**
     * Returns SQL query to search APIs by status.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByStatusSql(String[] roles);

    /**
     * Returns SQL query to count APIs by status search.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByStatusCountSql(String[] roles);

    /**
     * Returns SQL query to search APIs by description.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByDescriptionSql(String[] roles);

    /**
     * Returns SQL query to count APIs by description search.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByDescriptionCountSql(String[] roles);

    /**
     * Returns SQL query to search APIs by tags.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByTagsSql(String[] roles);

    /**
     * Returns SQL query to count APIs by tags search.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByTagsCountSql(String[] roles);

    /**
     * Returns SQL query to search APIs by API category.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByApiCategorySql(String[] roles);

    /**
     * Returns SQL query to count APIs by API category search.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByApiCategoryCountSql(String[] roles);

    /**
     * Returns SQL query to search APIs by a custom property.
     *
     * @param propertyName Name of the custom property to search by
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByOtherSql(String propertyName, String[] roles);

    /**
     * Returns SQL query to count APIs by a custom property search.
     *
     * @param propertyName Name of the custom property to search by
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByOtherCountSql(String propertyName, String[] roles);

    /**
     * Returns SQL query to get all API Product artifacts.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String getAllApiProductSql(String[] roles);

    /**
     * Returns SQL query to search API Products by content.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiProductByContentSql(String[] roles);

    /**
     * Returns SQL query to search API Products by name.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiProductByNameSql(String[] roles);

    /**
     * Returns SQL query to search API Products by provider.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiProductByProviderSql(String[] roles);

    /**
     * Returns SQL query to search API Products by version.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiProductByVersionSql(String[] roles);

    /**
     * Returns SQL query to search API Products by context.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiProductByContextSql(String[] roles);

    /**
     * Returns SQL query to search API Products by status.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiProductByStatusSql(String[] roles);

    /**
     * Returns SQL query to search API Products by description.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiProductByDescriptionSql(String[] roles);

    /**
     * Returns SQL query to search API Products by tags.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiProductByTagsSql(String[] roles);

    /**
     * Returns SQL query to search API Products by API category.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiProductByApiCategorySql(String[] roles);

    /**
     * Returns SQL query to search API Products by a custom property.
     *
     * @param propertyName Name of the custom property to search by
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiProductByOtherSql(String propertyName, String[] roles);

    /**
     * Returns SQL query to get all API artifacts for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String getAllApiArtifactsForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search content by API content.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByContentSql(String[] roles, String searchContent);


    /**
     * Returns SQL query to search content by API name.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByNameSql(String[] roles);

    /**
     * Returns SQL query to search content by API provider.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByProviderSql(String[] roles);

    /**
     * Returns SQL query to search content by API version.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByVersionSql(String[] roles);

    /**
     * Returns SQL query to search content by API context.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByContextSql(String[] roles);

    /**
     * Returns SQL query to search content by API status.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByStatusSql(String[] roles);

    /**
     * Returns SQL query to search content by API description.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByDescriptionSql(String[] roles);

    /**
     * Returns SQL query to search content by API tags.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByTagsSql(String[] roles);

    /**
     * Returns SQL query to search content by API category.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByApiCategorySql(String[] roles);

    /**
     * Returns SQL query to search content by a custom property.
     *
     * @param propertyName Name of the custom property to search by
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByOtherSql(String propertyName, String[] roles);

    /**
     * Returns SQL query to search APIs by content for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByContentForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search APIs by name for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByNameForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search APIs by provider for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByProviderForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search APIs by version for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByVersionForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search APIs by context for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByContextForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search APIs by status for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByStatusForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search APIs by description for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByDescriptionForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search APIs by tags for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByTagsForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search APIs by API category for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByApiCategoryForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search APIs by a custom property for Dev Portal.
     *
     * @param propertyName Name of the custom property to search by
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchApiByOtherForDevPortalSql(String propertyName, String[] roles);

    /**
     * Returns SQL query to get all API Product artifacts for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByContentForDevPortalSql(String[] roles, String searchContent);

    /**
     * Returns SQL query to search API Products by content for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByNameForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search API Products by name for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByProviderForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search API Products by version for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByVersionForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search API Products by context for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByContextForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search API Products by status for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByStatusForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search API Products by description for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByDescriptionForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search API Products by tags for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByTagsForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search API Products by API category for Dev Portal.
     *
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByApiCategoryForDevPortalSql(String[] roles);

    /**
     * Returns SQL query to search API Products by a custom property for Dev Portal.
     *
     * @param propertyName Name of the custom property to search by
     * @param roles User roles for access control
     * @return SQL query string
     */
    String searchContentByOtherForDevPortalSql(String propertyName, String[] roles);

}