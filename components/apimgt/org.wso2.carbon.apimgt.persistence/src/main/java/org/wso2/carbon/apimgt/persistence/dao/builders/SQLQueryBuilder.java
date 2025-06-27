package org.wso2.carbon.apimgt.persistence.dao.builders;

public class SQLQueryBuilder {
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

    public static String GET_ALL_API_ARTIFACT_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND TYPE = 'API' " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String GET_ALL_API_COUNT(String[] roles) {
        return "SELECT COUNT(*) AS TOTAL_API_COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    // API Search SQL Queries
    public static String SEARCH_API_BY_CONTENT_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(metadata) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_BY_CONTENT_COUNT_SQL(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(metadata) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    public static String SEARCH_API_BY_NAME_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_VALUE(metadata, '$.id.apiName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_BY_NAME_COUNT_SQL(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_VALUE(metadata, '$.id.apiName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    public static String SEARCH_API_BY_PROVIDER_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.providerName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles)+ ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_BY_PROVIDER_COUNT_SQL(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.providerName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    public static String SEARCH_API_BY_VERSION_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.version')) LIKE ?" +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_BY_VERSION_COUNT_SQL(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.version')) LIKE ?" +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    public static String SEARCH_API_BY_CONTEXT_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(a2.METADATA, '$.context')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_BY_CONTEXT_COUNT_SQL(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.context')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    public static String SEARCH_API_BY_STATUS_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.status')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_BY_STATUS_COUNT_SQL(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.status')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    public static String SEARCH_API_BY_DESCRIPTION_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.description')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_BY_DESCRIPTION_COUNT_SQL(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.description')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    public static String SEARCH_API_BY_TAGS_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.tags')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_BY_TAGS_COUNT_SQL(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.tags')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    public static String SEARCH_API_BY_API_CATEGORY_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.apiCategories')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_BY_API_CATEGORY_COUNT_SQL(String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.apiCategories')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    public static String SEARCH_API_BY_OTHER_SQL(String propertyName, String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_BY_OTHER_COUNT_SQL(String propertyName, String[] roles) {
        return "SELECT COUNT(*) AS COUNT FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ")";
    }

    // Content Search SQL Queries
    public static String SEARCH_CONTENT_BY_CONTENT_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_NAME_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_PROVIDER_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_VERSION_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_CONTEXT_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_STATUS_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_DESCRIPTION_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_TAGS_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_API_CATEGORY_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_OTHER_SQL(String propertyName, String[] roles) {
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

    // API Product Search SQL Queries
    public static String SEARCH_API_PRODUCT_BY_CONTENT_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(metadata) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_PRODUCT_BY_NAME_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_VALUE(metadata, '$.id.apiName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_PRODUCT_BY_PROVIDER_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.providerName')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_PRODUCT_BY_VERSION_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.id.version')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_PRODUCT_BY_CONTEXT_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.context')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_PRODUCT_BY_STATUS_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.status')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_PRODUCT_BY_DESCRIPTION_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.description')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_PRODUCT_BY_TAGS_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.tags')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_PRODUCT_BY_API_CATEGORY_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(METADATA, '$.apiCategories')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    public static String SEARCH_API_PRODUCT_BY_OTHER_SQL(String propertyName, String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND LOWER(JSON_QUERY(metadata, '$." + propertyName + "')) LIKE ? " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    }

    public static String GET_ALL_API_PRODUCT_SQL(String[] roles) {
        return "SELECT * FROM AM_ARTIFACT_DATA " +
                "WHERE JSON_VALUE(org, '$.name') = ? " +
                "AND type = 'API_PRODUCT' " +
                "AND (" + getRoleConditionForPublisher(roles) + ") " +
                "ORDER BY LAST_MODIFIED DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

    // Keep all DEV_PORTAL related methods unchanged
    public static String SEARCH_API_BY_CONTENT_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_API_BY_NAME_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_API_BY_PROVIDER_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_API_BY_VERSION_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_API_BY_CONTEXT_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_API_BY_STATUS_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_API_BY_DESCRIPTION_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_API_BY_TAGS_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_API_BY_API_CATEGORY_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_API_BY_OTHER_FOR_DEV_PORTAL_SQL(String propertyName, String[] roles) {
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

    public static String GET_ALL_API_ARTIFACTS_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    // Content search methods with role-based access
    public static String SEARCH_CONTENT_BY_CONTENT_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_NAME_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_PROVIDER_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_VERSION_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_CONTEXT_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_STATUS_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_DESCRIPTION_FOR_DEV_PORTAL_SQL(String[] roles) {
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

    public static String SEARCH_CONTENT_BY_TAGS_FOR_DEV_PORTAL_SQL(String[] roles) {
        return "SELECT 1 FROM AM_ARTIFACT_DATA a2 " +
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


    public static String SEARCH_CONTENT_BY_API_CATEGORY_FOR_DEV_PORTAL_SQL(String[] roles) {
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


    public static String SEARCH_CONTENT_BY_OTHER_FOR_DEV_PORTAL_SQL(String propertyName, String[] roles) {
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
