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

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.APIStore;
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
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.msf4j.Request;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Utility class for all REST APIS.
 */
public class RestApiUtil {

    private static final Logger log = LoggerFactory.getLogger(RestApiUtil.class);
    private static String publisherRestAPIDefinition;
    private static String storeRestAPIDefinition;
    private static String adminRestAPIDefinition;
    private static final String LOGGED_IN_USER = "LOGGED_IN_USER";
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static APIMConfigurations apimConfigurations = APIMConfigurationService.getInstance()
            .getApimConfigurations();

    /**
     * Get the current logged in user's username
     * @return The current logged in user.
     * @param request
     */
    public static String getLoggedInUsername(Request request) {
        String loggedInUser = null;
        try {
            loggedInUser = request.getProperty(LOGGED_IN_USER).toString();
       } catch (NullPointerException e) {
           String message = "Error while getting loggedInUser from request";
           log.error(message, e);
       }
        return loggedInUser;
    }

    /**
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param msg error message
     * @param log Log instance
     * @throws BadRequestException  If 400 bad request comes.
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
     * @param paramList map of parameters specific to the error.
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
     * @param errorHandler Error Handler object.
     * @param paramList Parameter list
     * @param e APIMGTException object.
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
     * @param message specifies the error message
     * @param code  error code.
     * @param description   error description.
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
     * @param subscriberName    Name of the subscriber.
     * @return  {@code APIStore}
     * @throws APIManagementException   if failed to get the consumers.
     */
    public static APIStore getConsumer(String subscriberName) throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
    }

    /**
     * Returns an APIStore for anonymous user.
     *
     * @return  {@code APIStore}
     * @throws APIManagementException if failed to get the consumer object.
     */
    public static APIStore getConsumer() throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer();
    }

    /**
     * Returns an APIMgtAdminService.
     *
     * @return API Management Admin Service
     * @throws APIManagementException   If failed to retrieve admin service.
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
     * @param offset  starting index
     * @param limit   max number of objects returned
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
     * @throws APIManagementException   if failed to get publisher api resource.
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
     * @throws  APIManagementException   if failed to get store api resource
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
     * @return  String associated with API Manager admin REST API
     * @throws APIManagementException   if failed to get admin api resource
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
     * @return  Json string
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
     * Util method for mapping from rest API policy level String to {@link APIMgtAdminService.PolicyLevel} enum
     */
    public static APIMgtAdminService.PolicyLevel mapRestApiPolicyLevelToPolicyLevelEnum (String level)
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
}
