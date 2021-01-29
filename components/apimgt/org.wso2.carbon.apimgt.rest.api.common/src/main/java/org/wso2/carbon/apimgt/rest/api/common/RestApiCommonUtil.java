package org.wso2.carbon.apimgt.rest.api.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestApiCommonUtil {

    public static final ThreadLocal userThreadLocal = new ThreadLocal();
    private static final Log log = LogFactory.getLog(RestApiCommonUtil.class);

    public static void setThreadLocalRequestedTenant(String user) {

        userThreadLocal.set(user);
    }

    public static void unsetThreadLocalRequestedTenant() {

        userThreadLocal.remove();
    }

    public static String getThreadLocalRequestedTenant() {

        return (String) userThreadLocal.get();
    }

    public static APIProvider getLoggedInUserProvider() throws APIManagementException {

        return APIManagerFactory.getInstance().getAPIProvider(getLoggedInUsername());
    }

    /** Returns an APIConsumer which is corresponding to the current logged in user taken from the carbon context
     *
     * @return an APIConsumer which is corresponding to the current logged in user
     * @throws APIManagementException
     */
    public static APIConsumer getLoggedInUserConsumer() throws APIManagementException {

        return APIManagerFactory.getInstance().getAPIConsumer(getLoggedInUsername());
    }

    public static String getLoggedInUsername() {

        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    public static String getLoggedInUserTenantDomain() {

        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    /**
     * Returns date in RFC3339 format.
     * Example: 2008-11-13T12:23:30-08:00
     *
     * @param date Date object
     * @return date string in RFC3339 format.
     */
    public static String getRFC3339Date(Date date) {

        DateTimeFormatter jodaDateTimeFormatter = ISODateTimeFormat.dateTime();
        DateTime dateTime = new DateTime(date);
        return jodaDateTimeFormatter.print(dateTime);
    }

    /**
     * Returns the next/previous offset/limit parameters properly when current offset, limit and size parameters are
     * specified
     *
     * @param offset current starting index
     * @param limit current max records
     * @param size maximum index possible
     * @return the next/previous offset/limit parameters as a hash-map
     */
    public static Map<String, Integer> getPaginationParams(Integer offset, Integer limit, Integer size) {

        Map<String, Integer> result = new HashMap<>();
        if (offset >= size || offset < 0)
            return result;

        int start = offset;
        int end = offset + limit - 1;

        int nextStart = end + 1;
        if (nextStart < size) {
            result.put(RestApiConstants.PAGINATION_NEXT_OFFSET, nextStart);
            result.put(RestApiConstants.PAGINATION_NEXT_LIMIT, limit);
        }

        int previousEnd = start - 1;
        int previousStart = previousEnd - limit + 1;

        if (previousEnd >= 0) {
            if (previousStart < 0) {
                result.put(RestApiConstants.PAGINATION_PREVIOUS_OFFSET, 0);
                result.put(RestApiConstants.PAGINATION_PREVIOUS_LIMIT, limit);
            } else {
                result.put(RestApiConstants.PAGINATION_PREVIOUS_OFFSET, previousStart);
                result.put(RestApiConstants.PAGINATION_PREVIOUS_LIMIT, limit);
            }
        }
        return result;
    }

    /** Returns the paginated url for APIs API
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param query search query value
     * @return constructed paginated url
     */
    public static String getAPIPaginatedURL(Integer offset, Integer limit, String query) {

        String paginatedURL = RestApiConstants.APIS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.QUERY_PARAM, query);
        return paginatedURL;
    }

    /** Returns the paginated url for Applications API
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param groupId groupId of the Application
     * @return constructed paginated url
     */
    public static String getApplicationPaginatedURL(Integer offset, Integer limit, String groupId) {

        groupId = groupId == null ? "" : groupId;
        String paginatedURL = RestApiConstants.APPLICATIONS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.GROUPID_PARAM, groupId);
        return paginatedURL;
    }

    /** Returns the paginated url for admin  /Applications API
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getApplicationPaginatedURL(Integer offset, Integer limit) {

        String paginatedURL = RestApiConstants.APPLICATIONS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /** Returns the paginated url for subscriptions for a particular API identifier
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param apiId API Identifier
     * @param groupId groupId of the Application
     * @return constructed paginated url
     */
    public static String getSubscriptionPaginatedURLForAPIId(Integer offset, Integer limit, String apiId,
                                                             String groupId) {

        groupId = groupId == null ? "" : groupId;
        String paginatedURL = RestApiConstants.SUBSCRIPTIONS_GET_PAGINATION_URL_APIID;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APIID_PARAM, apiId);
        paginatedURL = paginatedURL.replace(RestApiConstants.GROUPID_PARAM, groupId);
        return paginatedURL;
    }

    /** Returns the paginated url for subscriptions for a particular application
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param applicationId application id
     * @return constructed paginated url
     */
    public static String getSubscriptionPaginatedURLForApplicationId(Integer offset, Integer limit,
                                                                     String applicationId) {

        String paginatedURL = RestApiConstants.SUBSCRIPTIONS_GET_PAGINATION_URL_APPLICATIONID;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APPLICATIONID_PARAM, applicationId);
        return paginatedURL;
    }

    /** Returns the paginated url for documentations
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getDocumentationPaginatedURL(Integer offset, Integer limit, String apiId) {

        String paginatedURL = RestApiConstants.DOCUMENTS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APIID_PARAM, apiId);
        return paginatedURL;
    }

    /** Returns the paginated url for API ratings
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getRatingPaginatedURL(Integer offset, Integer limit, String apiId) {

        String paginatedURL = RestApiConstants.RATINGS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APIID_PARAM, apiId);
        return paginatedURL;
    }

    /** Returns the paginated url for tiers
     *
     * @param tierLevel tier level (api/application or resource)
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getTiersPaginatedURL(String tierLevel, Integer offset, Integer limit) {

        String paginatedURL = RestApiConstants.TIERS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.TIER_LEVEL_PARAM, tierLevel);
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /** Returns the paginated url for tags
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getTagsPaginatedURL(Integer offset, Integer limit) {

        String paginatedURL = RestApiConstants.TAGS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /**
     * Returns the paginated URL for scopes.
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getScopesPaginatedURL(Integer offset, Integer limit) {

        String paginatedURL = RestApiConstants.SCOPES_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /** Returns the paginated url for tags
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getResourcePathPaginatedURL(Integer offset, Integer limit) {

        String paginatedURL = RestApiConstants.RESOURCE_PATH_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /** Returns the paginated url for APIProducts API
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param query search query value
     * @return constructed paginated url
     */
    public static String getAPIProductPaginatedURL(Integer offset, Integer limit, String query) {

        String paginatedURL = RestApiConstants.APIS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.QUERY_PARAM, query);
        return paginatedURL;
    }

    /** Returns the paginated url for product documentations
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getProductDocumentationPaginatedURL(Integer offset, Integer limit, String apiId) {

        String paginatedURL = RestApiConstants.PRODUCT_DOCUMENTS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APIID_PARAM, apiId);
        return paginatedURL;
    }

    public static APIProvider getProvider(String username) throws APIManagementException {

        return APIManagerFactory.getInstance().getAPIProvider(username);
    }

    /**
     * Check whether the specified apiId is of type UUID
     *
     * @param apiId api identifier
     * @return true if apiId is of type UUID, false otherwise
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isUUID(String apiId) {

        try {
            UUID.fromString(apiId);
            return true;
        } catch (IllegalArgumentException e) {
            if (log.isDebugEnabled()) {
                log.debug(apiId + " is not a valid UUID");
            }
            return false;
        }

    }

    public static APIConsumer getConsumer(String subscriberName) throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
    }

    /**
     * This method retrieves the Swagger Definition for an API to be displayed
     * @param api API
     * @return String
     * */
    public static String retrieveSwaggerDefinition(API api, APIProvider apiProvider)
            throws APIManagementException {
        String apiSwagger = null;
        String providerName = APIUtil.replaceEmailDomainBack(api.getId().getProviderName());
        String providerTenantDomain = MultitenantUtils.getTenantDomain(providerName);
        if (api.getUuid() != null) {
            apiSwagger = apiProvider.getOpenAPIDefinition(api.getUuid(), providerTenantDomain);
        } else {
            apiSwagger = apiProvider.getOpenAPIDefinition(api.getId(), providerTenantDomain);
        }
         
        APIDefinition parser = OASParserUtil.getOASParser(apiSwagger);
        return parser.getOASDefinitionForPublisher(api, apiSwagger);
    }

    /**
     * Check if the user's tenant and the API's tenant is equal. If it is not this will throw an
     * APIMgtAuthorizationFailedException
     *
     * @param apiIdentifier API Identifier
     * @throws APIMgtAuthorizationFailedException
     */
    public static void validateUserTenantWithAPIIdentifier(APIIdentifier apiIdentifier)
            throws APIMgtAuthorizationFailedException {
        String username = getLoggedInUsername();
        String providerName = APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName());
        String providerTenantDomain = MultitenantUtils.getTenantDomain(providerName);
        String loggedInUserTenantDomain = getLoggedInUserTenantDomain();
        if (!providerTenantDomain.equals(loggedInUserTenantDomain)) {
            String errorMsg = "User " + username + " is not allowed to access " + apiIdentifier.toString()
                    + " as it belongs to a different tenant : " + providerTenantDomain;
            throw new APIMgtAuthorizationFailedException(errorMsg);
        }
    }

    /**
     * Url validator, Allow any url with https and http.
     * Allow any url without fully qualified domain
     *
     * @param url Url as string
     * @return boolean type stating validated or not
     */
    public static boolean isURL(String url) {

        Pattern pattern = Pattern.compile("^(http|https)://(.)+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();

    }
}
