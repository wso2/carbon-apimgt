/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.common.util;

import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.Analyzer;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.exception.BadRequestException;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.ServiceMethodInfo;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Utility class for all REST APIs.
 */
public class RestApiUtil {

    private static final Logger log = LoggerFactory.getLogger(RestApiUtil.class);
    private static final String LOGGED_IN_USER = "LOGGED_IN_USER";
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static String publisherRestAPIDefinition;
    private static String storeRestAPIDefinition;
    private static String adminRestAPIDefinition;
    private static String analyticsRestApiDefinition;
    private static Map<String, Swagger> swaggerRestAPIDefinitions = new HashMap<>();
    private static APIMConfigurations apimConfigurations = APIMConfigurationService.getInstance()
            .getApimConfigurations();

    static {
        try {
            swaggerRestAPIDefinitions.put(RestApiConstants.APPType.PUBLISHER, new SwaggerParser()
                    .parse(getPublisherRestAPIResource()));
            swaggerRestAPIDefinitions.put(RestApiConstants.APPType.STORE, new SwaggerParser()
                    .parse(getStoreRestAPIResource()));
            swaggerRestAPIDefinitions.put(RestApiConstants.APPType.ANALYTICS, new SwaggerParser()
                    .parse(getAnalyticsRestAPIResource()));
            swaggerRestAPIDefinitions.put(RestApiConstants.APPType.ADMIN, new SwaggerParser()
                    .parse(getAdminRestAPIResource()));
        } catch (APIManagementException e) {
            log.error("Error while parsing the swagger definition to " + Swagger.class.getName(), e);
        }
    }

    /**
     * Get the current logged in user's username
     *
     * @param request msf4j request
     * @return The current logged in user's username or null if user is not logged in.
     */
    public static String getLoggedInUsername(Request request) {
        //First retrieve real user name(display name) from message request.
        //If we decided to change this to ID then still flow will work without issue.
        String user =
                request.getProperty(LOGGED_IN_USER) != null ? request.getProperty(LOGGED_IN_USER).toString() : null;
        //Then get relevant pseudo name from local mapping.
        if (user != null) {
            try {
                return APIManagerFactory.getInstance().getUserNameMapper().getLoggedInPseudoNameFromUserID(user);
            } catch (APIManagementException e) {
                //Get username method should return null when there is error. Log and return null.
                log.error(e.getMessage(), e);
            }
        }
        return user;
    }

    /**
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param msg error message
     * @param log Log instance
     * @throws BadRequestException If 400 bad request comes.
     */
    public static void handleBadRequest(String msg, Logger log) throws BadRequestException {
        BadRequestException badRequestException = buildBadRequestException(msg);
        log.error(msg);
        throw badRequestException;
    }

    /**
     * Returns a new BadRequestException
     *
     * @param description description of the exception
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(String description) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400L, description);
        return new BadRequestException(errorDTO);
    }


    /**
     * Returns a generic errorDTO
     *
     * @param errorHandler The error handler object.
     * @param paramList    map of parameters specific to the error.
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(ErrorHandler errorHandler, Map<String, String> paramList) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(errorHandler.getErrorCode());
        errorDTO.setMoreInfo(paramList);
        errorDTO.setMessage(errorHandler.getErrorMessage());
        errorDTO.setDescription(errorHandler.getErrorDescription());
        return errorDTO;
    }

    /**
     * Returns a generic errorDTO
     *
     * @param errorHandler The error handler object.
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(ErrorHandler errorHandler) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(errorHandler.getErrorCode());
        errorDTO.setMessage(errorHandler.getErrorMessage());
        errorDTO.setDescription(errorHandler.getErrorDescription());
        return errorDTO;
    }

    /**
     * Return errorDTO object. This method accept APIMGTException as a parameter so we can set the e.getMessage
     * directly to the errorDTO.
     *
     * @param errorHandler Error Handler object.
     * @param paramList    Parameter list
     * @param e            APIMGTException object.
     * @return ErrorDTO Object.
     */
    public static ErrorDTO getErrorDTO(ErrorHandler errorHandler, HashMap<String, String> paramList,
                                       APIManagementException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(errorHandler.getErrorCode());
        errorDTO.setMoreInfo(paramList);
        if (e.getMessage() == null) {
            errorDTO.setMessage(errorHandler.getErrorMessage());
        } else {
            errorDTO.setMessage(e.getMessage());
        }
        errorDTO.setDescription(errorHandler.getErrorDescription());
        return errorDTO;
    }

    /**
     * Returns a generic errorDTO
     *
     * @param message     specifies the error message
     * @param code        error code.
     * @param description error description.
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(String message, Long code, String description) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        return errorDTO;
    }

    /**
     * Returns an APIStore.
     *
     * @param subscriberName Name of the subscriber.
     * @return {@code APIStore}
     * @throws APIManagementException if failed to get the consumers.
     */
    public static APIStore getConsumer(String subscriberName) throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
    }

    /**
     * Returns an APIStore for anonymous user.
     *
     * @return {@code APIStore}
     * @throws APIManagementException if failed to get the consumer object.
     */
    public static APIStore getConsumer() throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer();
    }

    /**
     * Returns an APIAnalytics for a specific user.
     *
     * @param username
     * @return {@code APIAnalytics}
     * @throws APIManagementException if failed to get APIAnalytics
     */
    public static Analyzer getAnalyzer(String username) throws APIManagementException {
        return APIManagerFactory.getInstance().getAnalyzer(username);
    }

    /**
     * Returns an APIMgtAdminService.
     *
     * @return API Management Admin Service
     * @throws APIManagementException If failed to retrieve admin service.
     */
    public static APIMgtAdminService getAPIMgtAdminService() throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIMgtAdminService();
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
        if (offset >= size || offset < 0) {
            return result;
        }
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
     * Returns the paginated url for Applications API
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
     * Returns the gateway config retrieve url
     *
     * @param uuid api id
     * @return constructed gateway config retrieve  url
     */
    public static String getGatewayConfigGetURL(String uuid) {
        String path = RestApiConstants.GATEWAY_CONFIG_GET_URL + "/" + uuid + "/gateway-config";
        return path;
    }

    /**
     * Returns the swagger retrieve url
     *
     * @param uuid api id
     * @return constructed gateway config retrieve  url
     */
    public static String getSwaggerGetURL(String uuid) {
        String path = RestApiConstants.SWAGGER_GET_URL + "/" + uuid + "/swagger";
        return path;
    }


    /**
     * Search the Policy in the given collection of Policies. Returns it if it is included there. Otherwise return null
     *
     * @param policies Policy Collection
     * @param tierName Policy to find
     * @return Matched Policy with its name
     */
    public static Policy findPolicy(Collection<Policy> policies, String tierName) {
        for (Policy policy : policies) {
            if (policy.getPolicyName() != null && tierName != null && policy.getPolicyName().equals(tierName)) {
                return policy;
            }
        }
        return null;
    }

    public static boolean isURL(String sourceUrl) {
        //TODO: to be implemented
        return true;
    }


    /**
     * This is static method to return API definition of API Publisher REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return String associated with API Manager publisher REST API
     * @throws APIManagementException if failed to get publisher api resource.
     */
    public static String getPublisherRestAPIResource() throws APIManagementException {

        if (publisherRestAPIDefinition == null) {
            //if(basePath.contains("/api/am/store/")){
            //this is store API and pick resources accordingly
            try {
                publisherRestAPIDefinition = IOUtils
                        .toString(RestApiUtil.class.getResourceAsStream(RestApiConstants.PUBLISHER_API_YAML), "UTF-8");
            } catch (IOException e) {
                String message = "Error while reading the swagger definition of Publisher Rest API";
                log.error(message, e);
                throw new APIMgtSecurityException(message, ExceptionCodes.API_NOT_FOUND);
            }

        }
        return publisherRestAPIDefinition;
    }

    /**
     * This is static method to return API definition of API Publisher REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return String associated with API Manager store REST API
     * @throws APIManagementException if failed to get store api resource
     */
    public static String getStoreRestAPIResource() throws APIManagementException {

        if (storeRestAPIDefinition == null) {
            //if(basePath.contains("/api/am/store/")){
            //this is store API and pick resources accordingly
            try {
                storeRestAPIDefinition = IOUtils
                        .toString(RestApiUtil.class.getResourceAsStream(RestApiConstants.STORE_API_YAML), "UTF-8");
            } catch (IOException e) {
                String message = "Error while reading the swagger definition of Store Rest API";
                log.error(message, e);
                throw new APIMgtSecurityException(message, ExceptionCodes.API_NOT_FOUND);
            }

        }
        return storeRestAPIDefinition;
    }

    /**
     * This method return API swagger definition of Admin REST API
     *
     * @return String associated with API Manager admin REST API
     * @throws APIManagementException if failed to get admin api resource
     */
    public static String getAdminRestAPIResource() throws APIManagementException {

        if (adminRestAPIDefinition == null) {
            // this is admin API and pick resources accordingly
            try {
                adminRestAPIDefinition = IOUtils
                        .toString(RestApiUtil.class.getResourceAsStream(RestApiConstants.ADMIN_API_YAML), "UTF-8");
            } catch (IOException e) {
                String message = "Error while reading the swagger definition of Admin Rest API";
                log.error(message, e);
                throw new APIMgtSecurityException(message, ExceptionCodes.API_NOT_FOUND);
            }

        }
        return adminRestAPIDefinition;
    }

    /**
     * used to convert yaml to json
     *
     * @param yamlString yaml String
     * @return Json string
     */
    public static String convertYmlToJson(String yamlString) {
        Yaml yaml = new Yaml();
        Map map = (Map) yaml.load(yamlString);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(map);
        return jsonObject.toJSONString();
    }

    public static String getContext(String appType) {
        APIMConfigurations apimConfigurations = APIMConfigurationService.getInstance().getApimConfigurations();
        if (RestApiConstants.APPType.PUBLISHER.equals(appType)) {
            return apimConfigurations.getPublisherContext();
        } else if (RestApiConstants.APPType.STORE.equals(appType)) {
            return apimConfigurations.getStoreContext();
        } else if (RestApiConstants.APPType.ADMIN.equals(appType)) {
            return apimConfigurations.getAdminContext();
        } else {
            return null;
        }
    }

    public static String getHost(String protocol) {
        TransportsConfiguration transportsConfiguration = YAMLTransportConfigurationBuilder.build();
        Set<ListenerConfiguration> listenerConfigurationSet = transportsConfiguration.getListenerConfigurations();
        String host = apimConfigurations.getHostname();
        if (!apimConfigurations.isReverseProxyEnabled()) {
            if (HTTP.equals(protocol)) {
                for (ListenerConfiguration listenerConfiguration : listenerConfigurationSet) {
                    if (HTTP.equals(listenerConfiguration.getScheme())) {
                        host = host.concat(":").concat(String.valueOf(listenerConfiguration.getPort()));
                        break;
                    }
                }
            } else {
                for (ListenerConfiguration listenerConfiguration : listenerConfigurationSet) {
                    if (HTTPS.equals(listenerConfiguration.getScheme())) {
                        host = host.concat(":").concat(String.valueOf(listenerConfiguration.getPort()));
                    }
                }
            }
        }
        return host;
    }

    /**
     * This method return API swagger definition of Analytics REST API
     *
     * @return swagger definition as a String
     * @throws APIMgtSecurityException if failed to get analytics api resource
     */
    public static String getAnalyticsRestAPIResource() throws APIMgtSecurityException {
        if (analyticsRestApiDefinition == null) {
            try {
                analyticsRestApiDefinition = IOUtils
                        .toString(RestApiUtil.class.getResourceAsStream(RestApiConstants.ANALYTICS_API_YAML), "UTF-8");
            } catch (IOException e) {
                String message = "Error while reading the swagger definition of Analytics Rest API";
                log.error(message, e);
                throw new APIMgtSecurityException(message, ExceptionCodes.API_NOT_FOUND);
            }

        }
        return analyticsRestApiDefinition;
    }

    /**
     * Util method for mapping from rest API policy level String to {@link APIMgtAdminService.PolicyLevel} enum
     */
    public static APIMgtAdminService.PolicyLevel mapRestApiPolicyLevelToPolicyLevelEnum(String level)
            throws APIManagementException {
        if (APIMgtAdminService.PolicyLevel.api.name().equals(level)) {
            return APIMgtAdminService.PolicyLevel.api;
        } else if (APIMgtAdminService.PolicyLevel.application.name().equals(level)) {
            return APIMgtAdminService.PolicyLevel.application;
        } else if (APIMgtAdminService.PolicyLevel.subscription.name().equals(level)) {
            return APIMgtAdminService.PolicyLevel.subscription;
        } else {
            throw new APIManagementException("Policy Level " + level + " not supported",
                    ExceptionCodes.POLICY_LEVEL_NOT_SUPPORTED);
        }
    }

    /**
     * Converts time in millis to ISO-8601 date time format
     *
     * @param epochMilliSeconds time in milli seconds
     * @return time in ISO 8601 format
     */
    public static String epochToISO8601DateTime(long epochMilliSeconds, ZoneId zoneId) {
        // returns ISO 8601 format, e.g. 2014-02-15T01:02:03Z
        return Instant.ofEpochMilli(epochMilliSeconds).atZone(zoneId).toString();
    }

    /**
     * Coverts given ISO8601 timestamp to in UTC time instant
     *
     * @param timestamp Timestamp in ISO8601 format
     * @return Instant  in UTC
     */
    public static Instant fromISO8601ToInstant(String timestamp) throws APIManagementException {
        // returns ISO 8601 format, e.g. 2014-02-15T01:02:03Z
        try {
            return Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(timestamp)).atZone(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException e) {
            throw new APIManagementException(e.getMessage(), ExceptionCodes.INVALID_DATE_TIME_STAMP);
        } catch (DateTimeException e) {
            throw new APIManagementException(e.getMessage(), ExceptionCodes.INVALID_DATE_TIME_STAMP);
        }
    }

    /**
     * Return the ZoneId of given ISO8601 timestamp
     *
     * @param timestamp Timestamp in ISO format
     * @return Zone ID extracted from timestamp
     */
    public static ZoneId getRequestTimeZone(String timestamp) {
        return ZonedDateTime.parse(timestamp).getZone();
    }

    /**
     * Get defined HTTP methods in the swagger definition as a comma separated string
     *
     * @param request           Request
     * @param serviceMethodInfo Method information for the request
     * @return Http Methods as a comma separated string
     * @throws APIManagementException if failed to get defined http methods
     */
    public static String getDefinedMethodHeadersInSwaggerContent(
            Request request, ServiceMethodInfo serviceMethodInfo) throws APIManagementException {
        String requestURI = request.getUri().toLowerCase(Locale.ENGLISH);
        Swagger swagger = null;
        Method resourceMethod;

        if (requestURI.contains("/api/am/publisher")) {
            swagger = swaggerRestAPIDefinitions.get(RestApiConstants.APPType.PUBLISHER);
        } else if (requestURI.contains("/api/am/store")) {
            swagger = swaggerRestAPIDefinitions.get(RestApiConstants.APPType.STORE);
        } else if (requestURI.contains("/api/am/analytics")) {
            swagger = swaggerRestAPIDefinitions.get(RestApiConstants.APPType.ANALYTICS);
        } else if (requestURI.contains("/api/am/admin")) {
            swagger = swaggerRestAPIDefinitions.get(RestApiConstants.APPType.ADMIN);
        } else {
            return null;
        }

        if (swagger == null) {
            throw new APIManagementException("Error while parsing the swagger definition",
                    ExceptionCodes.SWAGGER_URL_MALFORMED);
        }

        resourceMethod = serviceMethodInfo.getMethod();
        if (resourceMethod == null) {
            throw new APIManagementException("Could not read required properties from HTTP Request.",
                    ExceptionCodes.SWAGGER_URL_MALFORMED);
        }

        String apiPath = resourceMethod.getDeclaringClass().getAnnotation(javax.ws.rs.ApplicationPath.class).value();
        javax.ws.rs.Path apiPathAnnotation = resourceMethod.getAnnotation(javax.ws.rs.Path.class);
        if (apiPathAnnotation != null) {
            apiPath += apiPathAnnotation.value();
        }
        Path swaggerAPIPath = swagger.getPath(apiPath);
        if (swaggerAPIPath == null) {
            throw new APIManagementException("Could not read API path from the swagger definition",
                    ExceptionCodes.SWAGGER_URL_MALFORMED);
        }

        return swaggerAPIPath.getOperationMap().keySet().stream().map(Enum::toString)
                .collect(Collectors.joining(", "));
    }

}
