package org.wso2.carbon.apimgt.rest.api.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.OAuthTokenInfo;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.OASParserUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.uri.template.URITemplateException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.apimgt.impl.APIConstants.X_WSO2_AUTH_HEADER;
import static org.wso2.carbon.apimgt.impl.APIConstants.X_WSO2_BASEPATH;
import static org.wso2.carbon.apimgt.impl.APIConstants.X_WSO2_DISABLE_SECURITY;
import static org.wso2.carbon.apimgt.impl.APIConstants.X_WSO2_PRODUCTION_ENDPOINTS;
import static org.wso2.carbon.apimgt.impl.APIConstants.X_WSO2_SANDBOX_ENDPOINTS;
import static org.wso2.carbon.apimgt.rest.api.common.RestApiConstants.REST_API_GOVERNANCE_CONTEXT_FULL;
import static org.wso2.carbon.apimgt.rest.api.common.RestApiConstants.REST_API_GOVERNANCE_VERSION;

public class RestApiCommonUtil {

    public static final ThreadLocal userThreadLocal = new ThreadLocal();
    private static final Log log = LogFactory.getLog(RestApiCommonUtil.class);
    private static Set<URITemplate> dcrResourceMappings;
    private static Set<URITemplate> storeResourceMappings;
    private static Set<URITemplate> publisherResourceMappings;
    private static Set<URITemplate> adminAPIResourceMappings;
    private static Set<URITemplate> serviceCatalogAPIResourceMappings;
    private static Set<URITemplate> governanceResourceMapping;

    public static void unsetThreadLocalRequestedTenant() {

        userThreadLocal.remove();
    }

    public static String getThreadLocalRequestedTenant() {

        return (String) userThreadLocal.get();
    }

    public static void setThreadLocalRequestedTenant(String user) {

        userThreadLocal.set(user);
    }

    public static APIProvider getLoggedInUserProvider() throws APIManagementException {

        return APIManagerFactory.getInstance().getAPIProvider(getLoggedInUsername());
    }

    /**
     * @param message   CXF message to be validate
     * @param tokenInfo Token information associated with incoming request
     * @return return true if we found matching scope in resource and token information
     * else false(means scope validation failed).
     */
    public static boolean validateScopes(Map<String, Object> message, OAuthTokenInfo tokenInfo) {
        String basePath = (String) message.get("org.apache.cxf.message.Message.BASE_PATH");
        // path is obtained from Message.REQUEST_URI instead of Message.PATH_INFO, as Message.PATH_INFO contains
        // decoded values of request parameters
        String path = (String) message.get("org.apache.cxf.request.uri");
        String verb = (String) message.get("org.apache.cxf.request.method");
        String resource = path.substring(basePath.length() - 1);
        String[] scopes = tokenInfo.getScopes();

        String version = (String) message.get(RestApiConstants.API_VERSION);

        //get all the URI templates of the REST API from the base path
        Set<URITemplate> uriTemplates = RestApiCommonUtil.getURITemplatesForBasePath(basePath + version);
        if (uriTemplates.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No matching scopes found for request with path: " + basePath
                        + ". Skipping scope validation.");
            }
            return true;
        }

        for (Object template : uriTemplates.toArray()) {
            org.wso2.uri.template.URITemplate templateToValidate = null;
            Map<String, String> var = new HashMap<>();
            //check scopes with what we have
            String templateString = ((URITemplate) template).getUriTemplate();
            try {
                templateToValidate = new org.wso2.uri.template.URITemplate(templateString);
            } catch (URITemplateException e) {
                log.error("Error while creating URI Template object to validate request. Template pattern: " +
                        templateString, e);
            }
            if (templateToValidate != null && templateToValidate.matches(resource, var) && scopes != null
                    && verb != null && verb.equalsIgnoreCase(((URITemplate) template).getHTTPVerb())) {
                for (String scope : scopes) {
                    Scope scp = ((URITemplate) template).getScope();
                    if (scp != null) {
                        if (scope.equalsIgnoreCase(scp.getKey())) {
                            //we found scopes matches
                            if (log.isDebugEnabled()) {
                                log.debug("Scope validation successful for access token: " +
                                        message.get(RestApiConstants.MASKED_TOKEN) + " with scope: " + scp.getKey() +
                                        " for resource path: " + path + " and verb " + verb);
                            }
                            return true;
                        }
                    } else if (!((URITemplate) template).retrieveAllScopes().isEmpty()) {
                        List<Scope> scopesList = ((URITemplate) template).retrieveAllScopes();
                        for (Scope scpObj : scopesList) {
                            if (scope.equalsIgnoreCase(scpObj.getKey())) {
                                //we found scopes matches
                                if (log.isDebugEnabled()) {
                                    log.debug("Scope validation successful for access token: " +
                                            message.get(RestApiConstants.MASKED_TOKEN) + " with scope: " + scpObj.getKey() +
                                            " for resource path: " + path + " and verb " + verb);
                                }
                                return true;
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Scope not defined in swagger for matching resource " + resource + " and verb "
                                    + verb + " . So consider as anonymous permission and let request to continue.");
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method is used to get the URI template set for the relevant REST API using the given base path.
     *
     * @param basePath Base path of the REST API
     * @return Set of URI templates for the REST API
     */
    public static Set<URITemplate> getURITemplatesForBasePath(String basePath) {
        Set<URITemplate> uriTemplates = new HashSet<>();
        //get URI templates using the base path in the request
        if (basePath.contains(RestApiConstants.REST_API_PUBLISHER_CONTEXT_FULL_0)) {
            uriTemplates = RestApiCommonUtil.getPublisherAppResourceMapping(RestApiConstants.REST_API_PUBLISHER_VERSION_0);
        } else if (basePath.contains(RestApiConstants.REST_API_PUBLISHER_CONTEXT_FULL)) {
            uriTemplates = RestApiCommonUtil.getPublisherAppResourceMapping(RestApiConstants.REST_API_PUBLISHER_VERSION);
        } else if (basePath.contains(RestApiConstants.REST_API_STORE_CONTEXT_FULL_0)) {
            uriTemplates = RestApiCommonUtil.getStoreAppResourceMapping(RestApiConstants.REST_API_STORE_VERSION_0);
        } else if (basePath.contains(RestApiConstants.REST_API_DEVELOPER_PORTAL_CONTEXT_FULL)) {
            uriTemplates = RestApiCommonUtil.getStoreAppResourceMapping(RestApiConstants.REST_API_DEVELOPER_PORTAL_VERSION);
        } else if (basePath.contains(RestApiConstants.REST_API_ADMIN_CONTEXT_FULL_0)) {
            uriTemplates = RestApiCommonUtil.getAdminAPIAppResourceMapping(RestApiConstants.REST_API_ADMIN_VERSION_0);
        } else if (basePath.contains(RestApiConstants.REST_API_ADMIN_CONTEXT_FULL)) {
            uriTemplates = RestApiCommonUtil.getAdminAPIAppResourceMapping(RestApiConstants.REST_API_ADMIN_VERSION);
        } else if (basePath.contains(RestApiConstants.REST_API_SERVICE_CATALOG_CONTEXT_FULL)) {
            uriTemplates = RestApiCommonUtil.getServiceCatalogAPIResourceMapping();
        } else if (basePath.contains(RestApiConstants.REST_API_DCR_CONTEXT_FULL)) {
            uriTemplates = RestApiCommonUtil.getDCRAppResourceMapping();
        } else if (basePath.contains(REST_API_GOVERNANCE_CONTEXT_FULL)) {
            uriTemplates = RestApiCommonUtil.getGovernanceResourceMapping(REST_API_GOVERNANCE_VERSION);
        }
        return uriTemplates;
    }

    /**
     * This is static method to return URI Templates map of API Store REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return URITemplate set associated with API Manager Store REST API
     */
    public static Set<URITemplate> getStoreAppResourceMapping(String version) {

        API api = new API(new APIIdentifier(RestApiConstants.REST_API_PROVIDER,
                RestApiConstants.REST_API_STORE_CONTEXT,
                RestApiConstants.REST_API_STORE_VERSION_0));

        if (storeResourceMappings != null) {
            return storeResourceMappings;
        } else {
            String defFileName = RestApiConstants.REST_API_STORE_VERSION_0.equals(version) ?
                    "/store-api.json" : "/devportal-api.yaml";
            try (InputStream defStream = RestApiCommonUtil.class.getResourceAsStream(defFileName)) {
                String definition = IOUtils.toString(defStream, RestApiConstants.CHARSET);
                APIDefinition oasParser = OASParserUtil.getOASParser(definition);
                //Get URL templates from swagger content w created
                storeResourceMappings = oasParser.getURITemplates(definition);
            } catch (APIManagementException e) {
                log.error("Error while reading resource mappings for Store API: " + api.getId().getApiName(), e);
            } catch (IOException e) {
                log.error("Error while reading the swagger definition for Store API: " + api.getId().getApiName(), e);
            }
            return storeResourceMappings;
        }
    }

    /**
     * This is static method to return URI Templates map of API Admin REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return URITemplate set associated with API Manager Admin REST API
     */
    public static Set<URITemplate> getAdminAPIAppResourceMapping(String version) {

        API api = new API(new APIIdentifier(RestApiConstants.REST_API_PROVIDER, RestApiConstants.REST_API_ADMIN_CONTEXT,
                RestApiConstants.REST_API_ADMIN_VERSION_0));

        if (adminAPIResourceMappings != null) {
            return adminAPIResourceMappings;
        } else {
            String defFileName = RestApiConstants.REST_API_ADMIN_VERSION_0.equals(version) ?
                    "/admin-api.json" : "/admin-api.yaml";
            try (InputStream defStream = RestApiCommonUtil.class.getResourceAsStream(defFileName)) {
                String definition = IOUtils.toString(defStream, RestApiConstants.CHARSET);
                APIDefinition oasParser = OASParserUtil.getOASParser(definition);
                //Get URL templates from swagger content we created
                adminAPIResourceMappings = oasParser.getURITemplates(definition);
            } catch (APIManagementException e) {
                log.error("Error while reading resource mappings for Admin API: " + api.getId().getApiName(), e);
            } catch (IOException e) {
                log.error("Error while reading the swagger definition for Admin API: " + api.getId().getApiName(), e);
            }
            return adminAPIResourceMappings;
        }
    }

    /**
     * This is static method to return URI Templates map of API Governance REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return URITemplate set associated with API Manager Governance REST API
     */
    public static Set<URITemplate> getGovernanceResourceMapping(String version) {

        API api = new API(new APIIdentifier(RestApiConstants.REST_API_PROVIDER,
                RestApiConstants.REST_API_GOVERNANCE_CONTEXT, RestApiConstants.REST_API_GOVERNANCE_VERSION));

        if (governanceResourceMapping == null) {
            try (InputStream defStream = RestApiCommonUtil.class.getResourceAsStream("/governance-api.yaml")) {
                String definition;
                definition = IOUtils.toString(defStream, RestApiConstants.CHARSET);
                APIDefinition oasParser = OASParserUtil.getOASParser(definition);
                //Get URL templates from swagger content we created
                governanceResourceMapping = oasParser.getURITemplates(definition);
            } catch (APIManagementException e) {
                log.error("Error while reading resource mappings for Governance API: " + api.getId().getApiName(), e);
            } catch (IOException e) {
                log.error("Error while reading the swagger definition for Governance API: "
                        + api.getId().getApiName(), e);
            }
        }
        return governanceResourceMapping;
    }

    /**
     * This is static method to return URI Templates map of API Publisher REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return URITemplate set associated with API Manager publisher REST API
     */
    public static Set<URITemplate> getPublisherAppResourceMapping(String version) {
        API api = new API(new APIIdentifier(RestApiConstants.REST_API_PROVIDER, RestApiConstants.REST_API_STORE_CONTEXT,
                RestApiConstants.REST_API_STORE_VERSION_0));

        if (publisherResourceMappings != null) {
            return publisherResourceMappings;
        } else {
            String defFileName = RestApiConstants.REST_API_PUBLISHER_VERSION_0.equals(version) ?
                    "/publisher-api.json" : "/publisher-api.yaml";
            try (InputStream defStream = RestApiCommonUtil.class.getResourceAsStream(defFileName)) {
                String definition = IOUtils.toString(defStream, RestApiConstants.CHARSET);
                APIDefinition oasParser = OASParserUtil.getOASParser(definition);
                //Get URL templates from swagger content we created
                publisherResourceMappings = oasParser.getURITemplates(definition);
            } catch (APIManagementException e) {
                log.error("Error while reading resource mappings for Publisher API: " + api.getId().getApiName(), e);
            } catch (IOException e) {
                log.error("Error while reading the swagger definition for Publisher API: " + api.getId().getApiName(), e);
            }
            return publisherResourceMappings;
        }
    }

    /**
     * This is static method to return URI Templates map of DCR REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return URITemplate set associated with API Manager DCR REST API
     */
    public static Set<URITemplate> getDCRAppResourceMapping() {
        API api = new API(new APIIdentifier(RestApiConstants.REST_API_PROVIDER, RestApiConstants.REST_API_DCR_CONTEXT,
                RestApiConstants.REST_API_DCR_VERSION));

        if (dcrResourceMappings == null) {
            try (InputStream resourceStream = RestApiCommonUtil.class.getResourceAsStream("/dcr.yaml")) {
                String definition = IOUtils.toString(resourceStream, "UTF-8");
                APIDefinition oasParser = OASParserUtil.getOASParser(definition);
                //Get URL templates from swagger content we created
                dcrResourceMappings = oasParser.getURITemplates(definition);
            } catch (APIManagementException e) {
                log.error("Error while reading resource mappings for API: " + api.getId().getApiName(), e);
            } catch (IOException e) {
                log.error("Error while reading the swagger definition for API: " + api.getId().getApiName(), e);
            }
        }
        return dcrResourceMappings;
    }

    /**
     * This is static method to return URI Templates map of Service Catalog REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return URITemplate set associated with Service Catalog REST API
     */
    public static Set<URITemplate> getServiceCatalogAPIResourceMapping() {
        API api = new API(new APIIdentifier(RestApiConstants.REST_API_PROVIDER,
                RestApiConstants.REST_API_SERVICE_CATALOG_CONTEXT_FULL, "v0"));

        if (serviceCatalogAPIResourceMappings != null) {
            return serviceCatalogAPIResourceMappings;
        } else {
            try (InputStream resourceStream = RestApiCommonUtil.class
                    .getResourceAsStream("/service-catalog-api.yaml")) {
                String definition = IOUtils.toString(resourceStream, RestApiConstants.CHARSET);
                APIDefinition oasParser = OASParserUtil.getOASParser(definition);
                //Get URL templates from swagger content we created
                serviceCatalogAPIResourceMappings = oasParser.getURITemplates(definition);
            } catch (APIManagementException e) {
                log.error("Error while reading resource mappings for Service catalog API: " + api.getId().getApiName(), e);
            } catch (IOException e) {
                log.error("Error while reading the swagger definition for Service catalog API: " + api.getId().getApiName(), e);
            }
            return serviceCatalogAPIResourceMappings;
        }
    }

    /**
     * This method is used to get the scope list from the yaml file.
     *
     * @return MAP of scope list for all portal
     */
    public static Map<String, List<String>> getScopesInfoFromAPIYamlDefinitions() throws APIManagementException {

        Map<String, List<String>> portalScopeList = new HashMap<>();
        String[] fileNameArray = {"/admin-api.yaml", "/publisher-api.yaml", "/devportal-api.yaml",
                "/service-catalog-api.yaml"};
        for (String fileName : fileNameArray) {
            String definition;
            try (InputStream resourceStream = RestApiCommonUtil.class.getResourceAsStream(fileName)) {
                definition = IOUtils.toString(resourceStream, "UTF-8");
            } catch (IOException e) {
                throw new APIManagementException("Error while reading the swagger definition ,",
                        ExceptionCodes.DEFINITION_EXCEPTION);
            }
            APIDefinition oasParser = OASParserUtil.getOASParser(definition);
            Set<Scope> scopeSet = oasParser.getScopes(definition);
            for (Scope entry : scopeSet) {
                List<String> list = new ArrayList<>();
                list.add(entry.getDescription());
                list.add((fileName.replaceAll("-api.yaml", "").replace("/", "")));
                if (("/service-catalog-api.yaml".equals(fileName))) {
                    if (!entry.getKey().contains("apim:api_view")) {
                        portalScopeList.put(entry.getName(), list);
                    }
                } else {
                    portalScopeList.put(entry.getName(), list);
                }
            }
        }
        return portalScopeList;
    }

    /**
     * Returns an APIConsumer which is corresponding to the current logged in user taken from the carbon context
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
     * @param limit  current max records
     * @param size   maximum index possible
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

    /**
     * Returns the paginated url for APIs API
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @param query  search query value
     * @return constructed paginated url
     */
    public static String getAPIPaginatedURL(Integer offset, Integer limit, String query) {

        String paginatedURL = RestApiConstants.APIS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.QUERY_PARAM, query);
        return paginatedURL;
    }

    /**
     * Returns the paginated url for Endpoint Certificate Usage API
     *
     * @param alias  alias of certificate
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getCertificateUsagePaginatedURL(String alias, Integer offset, Integer limit) {

        String paginatedURL = RestApiConstants.ENDPOINT_CERTIFICATE_USAGE_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.ALIAS_PARAM, alias);
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /**
     * Returns the paginated url for Applications API
     *
     * @param offset  starting index
     * @param limit   max number of objects returned
     * @param groupId groupId of the Application
     * @return constructed paginated url
     */
    public static String getApplicationPaginatedURL(Integer offset, Integer limit, String groupId) {

        return getApplicationPaginatedURLWithSortParams(offset, limit, groupId, null, null);
    }

    /**
     * Returns the paginated url for Applications API when it comes to sortOrder and sortBy
     *
     * @param offset    starting index
     * @param limit     max number of objects returned
     * @param groupId   group ID of the application
     * @param sortOrder specified sorting order ex: ASC
     * @param sortBy    specified parameter for the sort ex: name
     * @return constructed paginated url
     */
    public static String getApplicationPaginatedURLWithSortParams(Integer offset, Integer limit, String groupId,
                                                                  String sortOrder, String sortBy) {

        groupId = groupId == null ? "" : groupId;
        String paginatedURL = RestApiConstants.APPLICATIONS_GET_PAGINATION_URL;
        if (StringUtils.isNoneBlank(sortBy) || StringUtils.isNotBlank(sortOrder)) {
            sortOrder = sortOrder == null ? "" : sortOrder;
            sortBy = sortBy == null ? "" : sortBy;
            paginatedURL = RestApiConstants.APPLICATIONS_GET_PAGINATION_URL_WITH_SORTBY_SORTORDER;
            paginatedURL = paginatedURL.replace(RestApiConstants.SORTBY_PARAM, sortBy);
            paginatedURL = paginatedURL.replace(RestApiConstants.SORTORDER_PARAM, sortOrder);
        }
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.GROUPID_PARAM, groupId);
        return paginatedURL;
    }

    /**
     * Returns the paginated url for admin  /Applications API
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getApplicationPaginatedURL(Integer offset, Integer limit) {

        String paginatedURL = RestApiConstants.APPLICATIONS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /**
     * Returns the paginated url for subscriptions for a particular API identifier
     *
     * @param offset  starting index
     * @param limit   max number of objects returned
     * @param apiId   API Identifier
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

    /**
     * Returns the paginated url for subscriptions
     *
     * @param offset  starting index
     * @param limit   max number of objects returned
     * @param groupId groupId of the Application
     * @return constructed paginated url
     */
    public static String getSubscriptionPaginatedURL(Integer offset, Integer limit, String groupId) {

        groupId = groupId == null ? "" : groupId;
        String paginatedURL = RestApiConstants.SUBSCRIPTIONS_GET_PAGINATION_URL_APIID;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.GROUPID_PARAM, groupId);
        return paginatedURL;
    }

    /**
     * Returns the paginated url for subscriptions for a particular application
     *
     * @param offset        starting index
     * @param limit         max number of objects returned
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

    /**
     * Returns the paginated url for documentations
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getDocumentationPaginatedURL(Integer offset, Integer limit, String apiId) {

        String paginatedURL = RestApiConstants.DOCUMENTS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APIID_PARAM, apiId);
        return paginatedURL;
    }

    /**
     * Returns the paginated url for API ratings
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getRatingPaginatedURL(Integer offset, Integer limit, String apiId) {

        String paginatedURL = RestApiConstants.RATINGS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APIID_PARAM, apiId);
        return paginatedURL;
    }

    /**
     * Returns the paginated url for tiers
     *
     * @param tierLevel tier level (api/application or resource)
     * @param offset    starting index
     * @param limit     max number of objects returned
     * @return constructed paginated url
     */
    public static String getTiersPaginatedURL(String tierLevel, Integer offset, Integer limit) {

        String paginatedURL = RestApiConstants.TIERS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.TIER_LEVEL_PARAM, tierLevel);
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /**
     * Returns the paginated url for tags
     *
     * @param offset starting index
     * @param limit  max number of objects returned
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

    /**
     * Returns the paginated url for tags
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getResourcePathPaginatedURL(Integer offset, Integer limit) {

        String paginatedURL = RestApiConstants.RESOURCE_PATH_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /**
     * Returns the paginated url for APIProducts API
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @param query  search query value
     * @return constructed paginated url
     */
    public static String getAPIProductPaginatedURL(Integer offset, Integer limit, String query) {

        String paginatedURL = RestApiConstants.APIS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.QUERY_PARAM, query);
        return paginatedURL;
    }

    /**
     * Returns the paginated url for product documentations
     *
     * @param offset starting index
     * @param limit  max number of objects returned
     * @return constructed paginated url
     */
    public static String getProductDocumentPaginatedURL(Integer offset, Integer limit, String apiId) {

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

    public static APIConsumer getConsumer(String subscriberName, String organization) throws APIManagementException {

        return APIManagerFactory.getInstance().getAPIConsumer(subscriberName, organization);
    }

    /**
     * This method retrieves the Swagger Definition for an API to be displayed
     *
     * @param api         API
     * @param apiProvider API Provider
     * @return String with the swagger definition
     */
    public static String retrieveSwaggerDefinition(String uuid, API api, APIProvider apiProvider)
            throws APIManagementException {

        String apiSwagger = apiProvider.getOpenAPIDefinition(uuid, api.getOrganization());
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

    /**
     * This method retrieves the AsyncAPI Definition for an API to be displayed
     *
     * @param api API
     * @return String
     */
    public static String retrieveAsyncAPIDefinition(API api, APIProvider apiProvider)
            throws APIManagementException {

        return apiProvider.getAsyncAPIDefinition(api.getId().getUUID(), api.getOrganization());
    }

    public static String getValidateTenantDomain(String xWSO2Tenant) {

        String tenantDomain = getLoggedInUserTenantDomain();
        if (xWSO2Tenant == null) {
            return tenantDomain;
        } else {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return xWSO2Tenant;
            } else {
                return tenantDomain;
            }
        }

    }

    /**
     * Generate a basic OpenAPI definition with given details.
     *
     * @param name             name of the API.
     * @param version          version of the API.
     * @param context          context of the API.
     * @param callbackEndpoint callback URL of the async API.
     * @return OpenAPI definition as String.
     * @throws JsonProcessingException Error occurred while generating the OpenAPI.
     */
    public static String generateOpenAPIForAsync(String name, String version, String context, String callbackEndpoint)
            throws JsonProcessingException {

        OpenAPI openAPI = new OpenAPI();
        Info info = new Info();
        info.setTitle(name);
        info.setDescription("API Definition of " + name);
        info.setVersion(version);
        openAPI.setInfo(info);
        ArrayList<Server> servers = new ArrayList<>();
        Server server = new Server();
        server.setUrl("/");
        servers.add(server);
        openAPI.setServers(Arrays.asList(server));
        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        ApiResponses apiResponses = new ApiResponses();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Default response");
        apiResponses.addApiResponse("default", apiResponse);
        operation.setResponses(apiResponses);
        pathItem.setPost(operation);
        paths.addPathItem("/*", pathItem);
        openAPI.paths(paths);
        List<String> urls = new ArrayList<>();
        urls.add(callbackEndpoint);
        Map<String, Object> tempMap = new HashMap();
        tempMap.put("type", "http");
        tempMap.put("urls", urls);
        openAPI.addExtension(X_WSO2_PRODUCTION_ENDPOINTS, tempMap);
        openAPI.addExtension(X_WSO2_SANDBOX_ENDPOINTS, tempMap);
        openAPI.addExtension(X_WSO2_AUTH_HEADER, "Authorization");
        openAPI.addExtension(X_WSO2_BASEPATH, context + "/" + version);
        openAPI.addExtension(X_WSO2_DISABLE_SECURITY, true);
        return Json.mapper().writeValueAsString(openAPI);
    }

}
