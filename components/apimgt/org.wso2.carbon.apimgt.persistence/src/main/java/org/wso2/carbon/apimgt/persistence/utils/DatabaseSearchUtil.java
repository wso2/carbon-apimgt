package org.wso2.carbon.apimgt.persistence.utils;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.persistence.dao.PersistenceDAO;
import org.wso2.carbon.apimgt.persistence.dto.ContentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.SearchQuery;
import org.wso2.carbon.apimgt.persistence.dto.SearchResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

enum SearchType {
    CONTENT("content"),
    NAME("name"),
    PROVIDER("provider"),
    VERSION("version"),
    CONTEXT("context"),
    STATUS("status"),
    DESCRIPTION("description"),
    TAGS("tags"),
    TAG("tag"),
    API_CATEGORY("api-category"),
    OTHER("other");

    private final String value;

    SearchType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

public class DatabaseSearchUtil {
    private static final Log log = LogFactory.getLog(DatabaseSearchUtil.class);
    private static final PersistenceDAO persistenceDAO = PersistenceDAO.getInstance();

    private static SearchType getSearchType(String typeString) {
        if (typeString == null || typeString.isEmpty()) {
            return SearchType.OTHER;
        }

        // Handle special case for api-category
        if ("api-category".equalsIgnoreCase(typeString)) {
            return SearchType.API_CATEGORY;
        }

        try {
            return SearchType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SearchType.OTHER;
        }
    }

    public static SearchResult searchAPIsForPublisher(SearchQuery searchQuery, String orgName, int start, int offset, String[] roles) throws APIManagementException {
        String searchContent;
        SearchType searchType;
        String property = null;

        try {
            searchType = getSearchType(searchQuery.getType());
        } catch (IllegalArgumentException e) {
            searchType = SearchType.OTHER;
            property = searchQuery.getType();
        }
        searchContent = searchQuery.getContent();

        SearchResult searchResult = null;

        switch (searchType) {
            case CONTENT:
                try {
                    searchResult = persistenceDAO.searchAPIsByContent(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by content: " + searchContent, e);
                }
                break;
            case NAME:
                try {
                    searchResult = persistenceDAO.searchAPIsByName(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by name: " + searchContent, e);
                }
                break;
            case PROVIDER:
                try {
                    searchResult = persistenceDAO.searchAPIsByProvider(searchContent, orgName, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by provider: " + searchContent, e);
                }
                break;
            case VERSION:
                try {
                    searchResult = persistenceDAO.searchAPIsByVersion(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by version: " + searchContent, e);
                }
                break;
            case CONTEXT:
                try {
                    searchResult = persistenceDAO.searchAPIsByContext(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by context: " + searchContent, e);
                }
                break;
            case STATUS:
                try {
                    searchResult = persistenceDAO.searchAPIsByStatus(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by status: " + searchContent, e);
                }
                break;
            case DESCRIPTION:
                try {
                    searchResult = persistenceDAO.searchAPIsByDescription(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by description: " + searchContent, e);
                }
                break;
            case TAGS:
                try {
                    searchResult = persistenceDAO.searchAPIsByTags(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by tags: " + searchContent, e);
                }
                break;
            case TAG:
                try {
                    searchResult = persistenceDAO.searchAPIsByTags(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by tag: " + searchContent, e);
                }
                break;
            case API_CATEGORY:
                try {
                    searchResult = persistenceDAO.searchAPIsByCategory(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by category: " + searchContent, e);
                }
                break;
            default:
                try {
                    if (property == null || property.isEmpty()) {
                        throw new APIManagementException("Property for search type 'other' cannot be null or empty.");
                    }
                    searchResult = persistenceDAO.searchAPIsByOther(orgName, property, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching APIs by property: " + property + " with content: " + searchContent, e);
                }
        }

        return searchResult;
    }

    public static List<ContentSearchResult> searchContentForPublisher(SearchQuery searchQuery, String orgName, int start, int offset, String[] roles) throws APIManagementException {
        String searchContent;
        SearchType searchType;

        try {
            searchType = SearchType.valueOf(searchQuery.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            searchType = SearchType.OTHER;
        }
        searchContent = searchQuery.getContent();

        List<ContentSearchResult> contentSearchResults;

        switch (searchType) {
            case CONTENT:
                contentSearchResults = persistenceDAO.searchContentByContent(orgName, searchContent, start, offset, roles);
                break;
            case NAME:
                contentSearchResults = persistenceDAO.searchContentByName(orgName, searchContent, start, offset, roles);
                break;
            case PROVIDER:
                contentSearchResults = persistenceDAO.searchContentByProvider(orgName, searchContent, start, offset, roles);
                break;
            case VERSION:
                contentSearchResults = persistenceDAO.searchContentByVersion(orgName, searchContent, start, offset, roles);
                break;
            case CONTEXT:
                contentSearchResults = persistenceDAO.searchContentByContext(orgName, searchContent, start, offset, roles);
                break;
            case STATUS:
                contentSearchResults = persistenceDAO.searchContentByStatus(orgName, searchContent, start, offset, roles);
                break;
            case DESCRIPTION:
                contentSearchResults = persistenceDAO.searchContentByDescription(orgName, searchContent, start, offset, roles);
                break;
            case TAGS:
            case TAG:
                contentSearchResults = persistenceDAO.searchContentByTags(orgName, searchContent, start, offset, roles);
                break;
            case API_CATEGORY:
                contentSearchResults = persistenceDAO.searchContentByCategory(orgName, searchContent, start, offset, roles);
                break;
            default:
                contentSearchResults = persistenceDAO.searchContentByOther(orgName, searchQuery.getType(), searchContent, start, offset, roles);
        }

        return contentSearchResults;
    }

    public static List<ContentSearchResult> serachAPIsForDevPortal(SearchQuery searchQuery, String orgName, int start, int offset, String[] roles) throws APIManagementException {
        String searchContent;
        SearchType searchType;

        try {
            searchType = SearchType.valueOf(searchQuery.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            searchType = SearchType.OTHER;
        }
        searchContent = searchQuery.getContent();

        List<ContentSearchResult> searchResult;

        switch (searchType) {
            case CONTENT:
                searchResult = persistenceDAO.searchAPIsByContentForDevPortal(orgName, searchContent, start, offset, roles);
                break;
            case NAME:
                searchResult = persistenceDAO.searchAPIsByNameForDevPortal(orgName, searchContent, start, offset, roles);
                break;
            case PROVIDER:
                searchResult = persistenceDAO.searchAPIsByProviderForDevPortal(searchContent, orgName, start, offset, roles);
                break;
            case VERSION:
                searchResult = persistenceDAO.searchAPIsByVersionForDevPortal(orgName, searchContent, start, offset, roles);
                break;
            case CONTEXT:
                searchResult = persistenceDAO.searchAPIsByContextForDevPortal(orgName, searchContent, start, offset, roles);
                break;
            case STATUS:
                searchResult = persistenceDAO.searchAPIsByStatusForDevPortal(orgName, searchContent, start, offset, roles);
                break;
            case DESCRIPTION:
                searchResult = persistenceDAO.searchAPIsByDescriptionForDevPortal(orgName, searchContent, start, offset, roles);
                break;
            case TAGS:
            case TAG:
                searchResult = persistenceDAO.searchAPIsByTagsForDevPortal(orgName, searchContent, start, offset, roles);
                break;
            case API_CATEGORY:
                searchResult = persistenceDAO.searchAPIsByCategoryForDevPortal(orgName, searchContent, start, offset, roles);
                break;
            default:
                if (searchQuery.getType() == null || searchQuery.getType().isEmpty()) {
                    throw new APIManagementException("Property for 'other' type cannot be null or empty.");
                }
                searchResult = persistenceDAO.searchAPIsByOtherForDevPortal(orgName, searchQuery.getType(), searchContent, start, offset, roles);
        }

        return searchResult;
    }

    public static List<ContentSearchResult> searchContentForDevPortal(SearchQuery modifiedQuery, String requestedTenantDomain, int start, int offset, String[] roles) throws APIManagementException {
        String searchContent = modifiedQuery.getContent();
        SearchType searchType;

        try {
            searchType = SearchType.valueOf(modifiedQuery.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            searchType = SearchType.OTHER;
        }

        List<ContentSearchResult> contentSearchResults;

        switch (searchType) {
            case CONTENT:
                contentSearchResults = persistenceDAO.searchContentByContentForDevPortal(requestedTenantDomain, searchContent, start, offset, roles);
                break;
            case NAME:
                contentSearchResults = persistenceDAO.searchContentByNameForDevPortal(requestedTenantDomain, searchContent, start, offset, roles);
                break;
            case PROVIDER:
                contentSearchResults = persistenceDAO.searchContentByProviderForDevPortal(requestedTenantDomain, searchContent, start, offset, roles);
                break;
            case VERSION:
                contentSearchResults = persistenceDAO.searchContentByVersionForDevPortal(requestedTenantDomain, searchContent, start, offset, roles);
                break;
            case CONTEXT:
                contentSearchResults = persistenceDAO.searchContentByContextForDevPortal(requestedTenantDomain, searchContent, start, offset, roles);
                break;
            case STATUS:
                contentSearchResults = persistenceDAO.searchContentByStatusForDevPortal(requestedTenantDomain, searchContent, start, offset, roles);
                break;
            case DESCRIPTION:
                contentSearchResults = persistenceDAO.searchContentByDescriptionForDevPortal(requestedTenantDomain, searchContent, start, offset, roles);
                break;
            case TAGS:
            case TAG:
                contentSearchResults = persistenceDAO.searchContentByTagsForDevPortal(requestedTenantDomain, searchContent, start, offset, roles);
                break;
            case API_CATEGORY:
                contentSearchResults = persistenceDAO.searchContentByCategoryForDevPortal(requestedTenantDomain, searchContent, start, offset, roles);
                break;
            default:
                contentSearchResults = persistenceDAO.searchContentByOtherForDevPortal(requestedTenantDomain, modifiedQuery.getType(), searchContent, start, offset, roles);
        }

        return contentSearchResults;
    }

    public static List<String> searchAPIProductsForPublisher(SearchQuery searchQuery, String orgName, int start, int offset, String[] roles) throws APIManagementException {
        String searchContent;
        SearchType searchType;
        String property = null;

        try {
            searchType = SearchType.valueOf(searchQuery.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            searchType = SearchType.OTHER;
            property = searchQuery.getType();
        }
        searchContent = searchQuery.getContent();

        List<String> searchResult;

        switch (searchType) {
            case CONTENT:
                try {
                    searchResult = persistenceDAO.searchAPIProductsByContent(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by content: " + searchContent, e);
                }
                break;
            case NAME:
                try {
                    searchResult = persistenceDAO.searchAPIProductsByName(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by name: " + searchContent, e);
                }
                break;
            case PROVIDER:
                try {
                    searchResult = persistenceDAO.searchAPIProductsByProvider(searchContent, orgName, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by provider: " + searchContent, e);
                }
                break;
            case VERSION:
                try {
                    searchResult = persistenceDAO.searchAPIProductsByVersion(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by version: " + searchContent, e);
                }
                break;
            case CONTEXT:
                try {
                    searchResult = persistenceDAO.searchAPIProductsByContext(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by context: " + searchContent, e);
                }
                break;
            case STATUS:
                try {
                    searchResult = persistenceDAO.searchAPIProductsByStatus(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by status: " + searchContent, e);
                }
                break;
            case DESCRIPTION:
                try {
                    searchResult = persistenceDAO.searchAPIProductsByDescription(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by description: " + searchContent, e);
                }
                break;
            case TAGS:
                try {
                    searchResult = persistenceDAO.searchAPIProductsByTags(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by tags: " + searchContent, e);
                }
                break;
            case TAG:
                try {
                    searchResult = persistenceDAO.searchAPIProductsByTags(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by tag: " + searchContent, e);
                }
                break;
            case API_CATEGORY:
                try {
                    searchResult = persistenceDAO.searchAPIProductsByCategory(orgName, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by category: " + searchContent, e);
                }
                break;
            default:
                try {
                    if (property == null || property.isEmpty()) {
                        throw new APIManagementException("Property for search type 'other' cannot be null or empty.");
                    }
                    searchResult = persistenceDAO.searchAPIProductsByOther(orgName, property, searchContent, start, offset, roles);
                } catch (APIManagementException e) {
                    throw new APIManagementException("Error while searching API Products by property: " + property + " with content: " + searchContent, e);
                }
        }

        return searchResult;
    }

//    public static List<ContentSearchResult> searchContentForAdmin(SearchQuery modifiedQuery, String org, int start, int count) {
//        String searchContent = modifiedQuery.getContent();
//        SearchType searchType;
//
//        try {
//            searchType = SearchType.valueOf(modifiedQuery.getType().toUpperCase());
//        } catch (IllegalArgumentException e) {
//            searchType = SearchType.OTHER;
//        }
//
//        List<ContentSearchResult> contentSearchResults;
//
//        switch (searchType) {
//            case CONTENT:
//                contentSearchResults = persistenceDAO.searchContentForAdmin(org, searchContent, start, count);
//                break;
//            case NAME:
//                contentSearchResults = persistenceDAO.searchContentByNameForAdmin(org, searchContent, start, count);
//                break;
//            case PROVIDER:
//                contentSearchResults = persistenceDAO.searchContentByProviderForAdmin(org, searchContent, start, count);
//                break;
//            case VERSION:
//                contentSearchResults = persistenceDAO.searchContentByVersionForAdmin(org, searchContent, start, count);
//                break;
//            case CONTEXT:
//                contentSearchResults = persistenceDAO.searchContentByContextForAdmin(org, searchContent, start, count);
//                break;
//            case STATUS:
//                contentSearchResults = persistenceDAO.searchContentByStatusForAdmin(org, searchContent, start, count);
//                break;
//            case DESCRIPTION:
//                contentSearchResults = persistenceDAO.searchContentByDescriptionForAdmin(org, searchContent, start, count);
//                break;
//            case TAGS:
//            case TAG:
//                contentSearchResults = persistenceDAO.searchContentByTagsForAdmin(org, searchContent, start, count);
//                break;
//            case API_CATEGORY:
//                contentSearchResults = persistenceDAO.searchContentByCategoryForAdmin(org, searchContent, start, count);
//                break;
//            default:
//                contentSearchResults = persistenceDAO.searchContentByOtherForAdmin(org, modifiedQuery.getType(), searchContent, start, count);
//        }
//
//        return contentSearchResults;
//    }
}
