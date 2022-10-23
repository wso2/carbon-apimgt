/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.apk.apimgt.impl.dao.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.dao.dto.UserContext;
import org.wso2.apk.apimgt.impl.dao.exceptions.APIPersistenceException;
import org.wso2.apk.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBSearchUtils {

    public static final String TAG_SEARCH_TYPE_PREFIX = "tag";
    public static final String TAG_COLON_SEARCH_TYPE_PREFIX = "tag:";
    public static final String CONTENT_SEARCH_TYPE_PREFIX = "content";
    public static final String DOCUMENTATION_SEARCH_TYPE_PREFIX = "doc";
    public static final String DOCUMENTATION_SEARCH_TYPE_PREFIX_WITH_EQUALS = "doc=";
    public static final String SEARCH_AND_TAG = "&";
    public static final String TAGS_SEARCH_TYPE_PREFIX = "tags";
    public static final String NAME_TYPE_PREFIX = "name";
    public static final String API_STATUS = "STATUS";
    public static final String API_PROVIDER = "Provider";
    public static final String DOCUMENT_INDEXER = "org.wso2.carbon.apimgt.impl.indexing.indexer.DocumentIndexer";
    public static final String STORE_VIEW_ROLES = "store_view_roles";
    public static final String PUBLISHER_ROLES = "publisher_roles";
    public static final String DOCUMENT_MEDIA_TYPE_KEY = "application/vnd.wso2-document\\+xml";
    public static final String DOCUMENT_INDEXER_INDICATOR = "document_indexed";
    public static final String DOCUMENTATION_SEARCH_MEDIA_TYPE_FIELD = "mediaType";
    public static final String DOCUMENTATION_INLINE_CONTENT_TYPE = "text/plain";
    public static final String API_RXT_MEDIA_TYPE = "application/vnd.wso2-api+xml";
    public static final String LCSTATE_SEARCH_KEY = "lcState";
    public static final String DOCUMENT_RXT_MEDIA_TYPE = "application/vnd.wso2-document+xml";
    public static final String API_OVERVIEW_STATUS = "overview_status";
    public static final String API_RELATED_CUSTOM_PROPERTIES_PREFIX = "api_meta.";
    public static final String API_RELATED_CUSTOM_PROPERTIES_DISPLAY_DEV = "__display";
    public static final String LABEL_SEARCH_TYPE_PREFIX = "label";
    public static final String API_LABELS_GATEWAY_LABELS = "labels_labelName";
    private static final String PROVIDER_SEARCH_TYPE_PREFIX = "provider";
    private static final String VERSION_SEARCH_TYPE_PREFIX = "version";
    private static final String CONTEXT_SEARCH_TYPE_PREFIX = "context";
    private static final String CONTEXT_TEMPLATE_SEARCH_TYPE_PREFIX = "contextTemplate";
    public static final String API_DESCRIPTION = "Description";
    public static final String TYPE_SEARCH_TYPE_PREFIX = "type";
    public static final String CATEGORY_SEARCH_TYPE_PREFIX = "api-category";
    public static final String ADVERTISE_ONLY_SEARCH_TYPE_PREFIX = "thirdParty";
    public static final String ADVERTISE_ONLY_ADVERTISED_PROPERTY = "advertiseOnly";
    public static final String ENABLE_STORE = "enableStore";
    public static final String API_CATEGORIES_CATEGORY_NAME = "apiCategories_categoryName";
    public static final String NULL_USER_ROLE_LIST = "null";
    public static final String GET_API_PRODUCT_QUERY  = "type=APIProduct";
    public static final String ENDPOINT_CONFIG_SEARCH_TYPE_PREFIX  = "endpointConfig";
    public static final String[] API_SEARCH_PREFIXES = { ENDPOINT_CONFIG_SEARCH_TYPE_PREFIX.toLowerCase(), DOCUMENTATION_SEARCH_TYPE_PREFIX, TAGS_SEARCH_TYPE_PREFIX,
            NAME_TYPE_PREFIX, PROVIDER_SEARCH_TYPE_PREFIX, CONTEXT_SEARCH_TYPE_PREFIX,
            CONTEXT_TEMPLATE_SEARCH_TYPE_PREFIX.toLowerCase(), VERSION_SEARCH_TYPE_PREFIX,
            LCSTATE_SEARCH_KEY.toLowerCase(), API_DESCRIPTION.toLowerCase(), API_STATUS.toLowerCase(),
            CONTENT_SEARCH_TYPE_PREFIX, TYPE_SEARCH_TYPE_PREFIX, LABEL_SEARCH_TYPE_PREFIX, CATEGORY_SEARCH_TYPE_PREFIX,
            ENABLE_STORE.toLowerCase() , ADVERTISE_ONLY_SEARCH_TYPE_PREFIX.toLowerCase(), "sort", "group", "group.sort"
            , "group.field", "group.ngroups", "group.format" };


    private static final Log log = LogFactory.getLog(DBSearchUtils.class);


    /**
     * @param inputSearchQuery search Query
     * @return Reconstructed new search query
     * @throws APIPersistenceException If there is an error in the search query
     */
    private static String constructQueryWithProvidedCriterias(String inputSearchQuery) throws APIPersistenceException {

        String newSearchQuery = "";
        // sub context and doc content doesn't support AND search
        if (inputSearchQuery != null && inputSearchQuery.contains(" ")
                && !inputSearchQuery.contains(TAG_COLON_SEARCH_TYPE_PREFIX)
                && (!inputSearchQuery.contains(CONTENT_SEARCH_TYPE_PREFIX)
                || inputSearchQuery.split(":").length > 2)) {
            if (inputSearchQuery.split(" ").length > 1) {
                String[] searchCriterias = inputSearchQuery.split(" ");
                for (int i = 0; i < searchCriterias.length; i++) {
                    if (searchCriterias[i].contains(":") && searchCriterias[i].split(":").length > 1) {
                        if (DOCUMENTATION_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchCriterias[i]
                                .split(":")[0])) {
                            throw new APIPersistenceException("Invalid query. AND based search is not supported for "
                                    + "doc prefix");
                        }
                    }
                    if (i == 0) {
                        newSearchQuery = getSingleSearchCriteria(searchCriterias[i]);
                    } else {
                        newSearchQuery = newSearchQuery + SEARCH_AND_TAG + getSingleSearchCriteria(searchCriterias[i]);
                    }
                }
            }
        } else {
            newSearchQuery = getSingleSearchCriteria(inputSearchQuery);
        }
        return newSearchQuery;
    }

    /**
     * Generates solr compatible search criteria syntax from user entered query criteria.
     * Ex: From version:1.0.0, this returns version=*1.0.0*
     *
     * @param criteria
     * @return solar compatible criteria
     * @throws APIPersistenceException
     */
    private static String getSingleSearchCriteria(String criteria) throws APIPersistenceException {

        criteria = criteria.trim();
        String searchValue = criteria;
        String searchKey = NAME_TYPE_PREFIX;

        if (criteria.contains(":")) {
            if (criteria.split(":").length > 1) {
                String[] splitValues = criteria.split(":");
                searchKey = splitValues[0].trim();
                searchValue = splitValues[1];
                // if search key is 'tag' instead of 'tags', allow it as well since rest api document says query
                // param to use for tag search is 'tag'

                if (TAG_SEARCH_TYPE_PREFIX.equals(searchKey)) {
                    searchKey = TAGS_SEARCH_TYPE_PREFIX;
                    searchValue = searchValue.replace(" ", "\\ ");
                }

                if (!DOCUMENTATION_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchKey)
                        && !TAGS_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchKey)) {
                    if (API_STATUS.equalsIgnoreCase(searchKey)) {
                        searchValue = searchValue.toLowerCase();
                    }
                    if (!(searchValue.endsWith("\"") && searchValue.startsWith("\""))) {
                        if (!searchValue.endsWith("*")) {
                            searchValue = searchValue + "*";
                        }
                        if (!searchValue.startsWith("*")) {
                            searchValue = "*" + searchValue;
                        }
                    }
                }

            } else {
                throw new APIPersistenceException("Search term is missing. Try again with valid search query.");
            }
        } else {
            if (!(searchValue.endsWith("\"") && searchValue.startsWith("\""))) {
                if (!searchValue.endsWith("*")) {
                    searchValue = searchValue + "*";
                }
                if (!searchValue.startsWith("*")) {
                    searchValue = "*" + searchValue;
                }
            }
        }
        if (API_PROVIDER.equalsIgnoreCase(searchKey)) {
            searchValue = searchValue.replaceAll("@", "-AT-");
        }
        return searchKey + "=" + searchValue;
    }

    private static String extractQuery(String searchQuery, boolean isDevPortal) {
        String[] searchQueries = searchQuery.split("&");
        StringBuilder filteredQuery = new StringBuilder();

        // Filtering the queries related with custom properties
        for (String query : searchQueries) {
            if (searchQuery.startsWith(DOCUMENTATION_SEARCH_TYPE_PREFIX)) {
                filteredQuery.append(query);
                break;
            }
            // If the query does not contain "=" then it is an erroneous scenario.
            if (query.contains("=")) {
                String[] searchKeys = query.split("=");

                if (searchKeys.length >= 2) {
                    if (!Arrays.asList(API_SEARCH_PREFIXES).contains(searchKeys[0].toLowerCase())) {
                        if (log.isDebugEnabled()) {
                            log.debug(searchKeys[0] + " does not match with any of the reserved key words. Hence"
                                    + " appending " + API_RELATED_CUSTOM_PROPERTIES_PREFIX + " as prefix");
                        }
                        if (isDevPortal) {
                            searchKeys[0] = (API_RELATED_CUSTOM_PROPERTIES_PREFIX + searchKeys[0]
                                    + API_RELATED_CUSTOM_PROPERTIES_DISPLAY_DEV);
                        } else {
                            searchKeys[0] = (API_RELATED_CUSTOM_PROPERTIES_PREFIX + searchKeys[0]);
                        }

                    }

                    // Ideally query keys for label and  category searchs are as below
                    //      label -> labels_labelName
                    //      category -> apiCategories_categoryName
                    // Since these are not user friendly we allow to use prefixes label and api-category. And label and
                    // category search should only return results that exactly match.
                    if (searchKeys[0].equals(LABEL_SEARCH_TYPE_PREFIX)) {
                        searchKeys[0] = API_LABELS_GATEWAY_LABELS;
                        searchKeys[1] = searchKeys[1].replace("*", "");
                    } else if (searchKeys[0].equals(CATEGORY_SEARCH_TYPE_PREFIX)) {
                        searchKeys[0] = API_CATEGORIES_CATEGORY_NAME;
                        searchKeys[1] = searchKeys[1].replace("*", "");
                    } else if (searchKeys[0].equals(ADVERTISE_ONLY_SEARCH_TYPE_PREFIX)) {
                        searchKeys[0] = ADVERTISE_ONLY_ADVERTISED_PROPERTY;
                    }

                    if (filteredQuery.length() == 0) {
                        filteredQuery.append(searchKeys[0]).append("=").append(searchKeys[1]);
                    } else {
                        filteredQuery.append("&").append(searchKeys[0]).append("=").append(searchKeys[1]);
                    }
                }
            } else {
                filteredQuery.append(query);
            }
        }
        return filteredQuery.toString();
    }

    private static String getDevPortalRolesWrappedQuery(String query, UserContext context) {
        if (APIUtil.isAdminUser(context)) {
            log.debug("Admin user. no modifications to the query");
            return query;
        }
        String criteria = STORE_VIEW_ROLES + "="
                + getUserRolesQuery(context.getRoles(), APIUtil.getSkipRoles(context));
        if (query != null && !query.trim().isEmpty()) {
            criteria = criteria + "&" + query;
        }
        if (log.isDebugEnabled()) {
            log.debug("User roles wrapped query : " + criteria);
        }

        return criteria;
    }

    private static String getUserRolesQuery(String[] userRoles, String skippedRoles) {

        StringBuilder rolesQuery = new StringBuilder();
        rolesQuery.append('(');
        rolesQuery.append(NULL_USER_ROLE_LIST);
        String skipRolesByRegex = skippedRoles;
        if (StringUtils.isNotEmpty(skipRolesByRegex)) {
            List<String> filteredUserRoles = new ArrayList<>(Arrays.asList(userRoles));
            String[] regexList = skipRolesByRegex.split(",");
            for (int i = 0; i < regexList.length; i++) {
                Pattern p = Pattern.compile(regexList[i]);
                Iterator<String> itr = filteredUserRoles.iterator();
                while(itr.hasNext()) {
                    String role = itr.next();
                    Matcher m = p.matcher(role);
                    if (m.matches()) {
                        itr.remove();
                    }
                }
            }
            userRoles = filteredUserRoles.toArray(new String[0]);
        }
        if (userRoles != null) {
            for (String userRole : userRoles) {
                rolesQuery.append(" OR ");
                rolesQuery.append(ClientUtils.escapeQueryChars(sanitizeUserRole(userRole.toLowerCase())));
            }
        }
        rolesQuery.append(")");
        return rolesQuery.toString();

    }

    /**
     * Convert special characters to encoded value.
     *
     * @param role
     * @return encoded value
     */
    private static String sanitizeUserRole(String role) {

        if (role.contains("&")) {
            return role.replaceAll("&", "%26");
        } else {
            return role;
        }
    }

    /**
     * Composes OR based search criteria from provided array of values
     *
     * @param values
     * @return
     */
    private static String getORBasedSearchCriteria(String[] values) {

        String criteria = "(";
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                criteria = criteria + values[i];
                if (i != values.length - 1) {
                    criteria = criteria + " OR ";
                } else {
                    criteria = criteria + ")";
                }
            }
            return criteria;
        }
        return null;
    }

    public static String getDevPortalSearchQuery(String searchQuery, UserContext ctx, boolean displayMultipleStatus,
                                                 boolean isAllowDisplayMultipleVersions) throws APIPersistenceException {
        //TODO: to update the search query to support postgresql
        String modifiedQuery = constructNewSearchQuery(searchQuery);
        if (!APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX_WITH_EQUALS.startsWith(modifiedQuery)) {
            String[] statusList = { APIConstants.PUBLISHED, APIConstants.PROTOTYPED };
            if (displayMultipleStatus) {
                statusList = new String[] { APIConstants.PUBLISHED, APIConstants.PROTOTYPED, APIConstants.DEPRECATED };
            }
            if (StringUtils.isEmpty(searchQuery)) { // normal listing
                String enableStoreCriteria = APIConstants.ENABLE_STORE_SEARCH_TYPE_KEY;
                if (isAllowDisplayMultipleVersions) {
                    modifiedQuery = modifiedQuery + APIConstants.SEARCH_AND_TAG + enableStoreCriteria;
                } else {
                    // solr result grouping is used when displayMultipleVersions is not enabled(default).
                    modifiedQuery = modifiedQuery + APIConstants.SEARCH_AND_TAG + enableStoreCriteria +
                            "&group=true&group.field=name&group.ngroups=true&group.sort=versionComparable desc";
                }
            }

            String apiOverviewStateCriteria = APIConstants.API_OVERVIEW_STATUS_SEARCH_TYPE_KEY;
            apiOverviewStateCriteria = apiOverviewStateCriteria + getORBasedSearchCriteria(statusList);

            modifiedQuery = modifiedQuery + APIConstants.SEARCH_AND_TAG + apiOverviewStateCriteria;
        }
        modifiedQuery = getDevPortalRolesWrappedQuery(extractQuery(modifiedQuery, true), ctx);
        return modifiedQuery;
    }

    /**
     * Used to reconstruct the input search query as sub context and doc content doesn't support AND search
     *
     * @param query Input search query
     * @return Reconstructed new search query
     * @throws APIPersistenceException If there is an error in the search query
     */
    private static String constructNewSearchQuery(String query) throws APIPersistenceException {

        return constructQueryWithProvidedCriterias(query.trim());
    }
}
