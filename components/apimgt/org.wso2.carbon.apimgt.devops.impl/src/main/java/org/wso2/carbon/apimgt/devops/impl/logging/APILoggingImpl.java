/*
 *
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.devops.impl.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.LoggingMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APILogInfoDTO;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;

import java.util.List;

/**
 * API logging implementation.
 */
public class APILoggingImpl {
    private static final Log log = LogFactory.getLog(APILoggingImpl.class);
    private static final String PER_API_LOGGING_PERMISSION_PATH = "/permission/protected/configure/logging";
    private static final String INVALID_LOGGING_PERMISSION = "Invalid logging permission";
    private static final String INCORRECT_LOGGING_PER_API_RESOURCE_REQUEST = "Resource Method and Resource Path both " +
            "should be included";
    private static final String LOGGING_API_MISSING_DATA = "Requested API is not available";
    private static final String REQUIRED_API_RESOURCE_IS_NOT_AVAILABLE = "Requested resource is not available";
    private final ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    public void addUpdateAPILogger(String tenantId, String apiId, String logLevel, String resourceMethod,
                                   String resourcePath) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Adding/updating API logger for tenantId: " + tenantId + ", apiId: " + apiId + 
                     ", logLevel: " + logLevel);
        }
        if (apiMgtDAO.getAPIInfoByUUID(apiId) == null) {
            log.warn("API not found for UUID: " + apiId);
            throw new APIManagementException("API not found.",
                    ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
        }
        String username = RestApiCommonUtil.getLoggedInUsername();
        if (!APIUtil.hasPermission(username, PER_API_LOGGING_PERMISSION_PATH)) {
            log.warn("User " + username + " does not have permission to configure API logging");
            throw new APIManagementException(INVALID_LOGGING_PERMISSION,
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        if (resourceMethod != null && resourcePath != null) {
            if (log.isDebugEnabled()) {
                log.debug("Configuring logging for specific resource - Method: " + resourceMethod + 
                         ", Path: " + resourcePath);
            }
            boolean isAPIResourceExists = LoggingMgtDAO.getInstance().checkAPILoggerPerResourceAvailable(tenantId,
                    apiId, resourceMethod.toUpperCase(), resourcePath);
            if (isAPIResourceExists) {
                LoggingMgtDAO.getInstance().addAPILoggerPerResource(tenantId, apiId, logLevel,
                        resourceMethod.toUpperCase(), resourcePath);
                log.info("API logger configured for resource " + resourceMethod + " " + resourcePath + 
                        " in API " + apiId);
            } else {
                log.warn("API resource not available: " + resourceMethod + " " + resourcePath + " for API " + apiId);
                throw new APIManagementException(REQUIRED_API_RESOURCE_IS_NOT_AVAILABLE,
                        ExceptionCodes.from(ExceptionCodes.LOGGING_API_RESOURCE_NOT_FOUND));
            }
        } else if (resourceMethod == null && resourcePath == null) {
            if (log.isDebugEnabled()) {
                log.debug("Configuring logging for entire API");
            }
            boolean isAPIExists = LoggingMgtDAO.getInstance().checkIfAPIExists(tenantId, apiId);
            if (isAPIExists) {
                LoggingMgtDAO.getInstance().addAPILogger(tenantId, apiId, logLevel);
                log.info("API logger configured for entire API " + apiId + " with log level " + logLevel);
            } else {
                log.warn("API not found in tenant " + tenantId + " for logging configuration: " + apiId);
                throw new APIManagementException(LOGGING_API_MISSING_DATA,
                        ExceptionCodes.from(ExceptionCodes.LOGGING_API_NOT_FOUND_IN_TENANT));
            }
        } else {
            log.warn("Invalid logging request - both resource method and path must be provided or both must be null");
            throw new APIManagementException(INCORRECT_LOGGING_PER_API_RESOURCE_REQUEST,
                    ExceptionCodes.from(ExceptionCodes.LOGGING_API_MISSING_DATA));
        }

        publishLogAPIData(tenantId, apiId, logLevel, resourceMethod, resourcePath);
    }

    private void publishLogAPIData(String tenantId, String apiId, String logLevel, String resourceMethod,
            String resourcePath) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Publishing API logging configuration notification for API: " + apiId);
        }
        APIEvent apiEvent = new APIEvent(apiId, logLevel, APIConstants.EventType.UDATE_API_LOG_LEVEL.name(),
                apiMgtDAO.getAPIContext(apiId), resourceMethod, resourcePath);
        APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());
    }

    public List<APILogInfoDTO> getAPILoggerList(String tenantId, String logLevel) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving API logger list for tenantId: " + tenantId + ", logLevel: " + logLevel);
        }
        String username = RestApiCommonUtil.getLoggedInUsername();
        if (!APIUtil.hasPermission(username, PER_API_LOGGING_PERMISSION_PATH)) {
            log.warn("User " + username + " does not have permission to retrieve API logger list");
            throw new APIManagementException(INVALID_LOGGING_PERMISSION,
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        List<APILogInfoDTO> loggerList = LoggingMgtDAO.getInstance().retrieveAPILoggerList(tenantId, logLevel);
        if (log.isDebugEnabled()) {
            log.debug("Retrieved " + (loggerList != null ? loggerList.size() : 0) + " API loggers");
        }
        return loggerList;
    }

    public List<APILogInfoDTO> getAPILoggerListByApiId(String tenantId, String apiId) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving API logger list for tenantId: " + tenantId + ", apiId: " + apiId);
        }
        if (apiMgtDAO.getAPIInfoByUUID(apiId) == null) {
            log.warn("API not found for UUID: " + apiId);
            throw new APIManagementException("API not found.",
                    ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
        }
        String username = RestApiCommonUtil.getLoggedInUsername();
        if (!APIUtil.hasPermission(username, PER_API_LOGGING_PERMISSION_PATH)) {
            log.warn("User " + username + " does not have permission to retrieve API logger by API ID");
            throw new APIManagementException(INVALID_LOGGING_PERMISSION,
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        List<APILogInfoDTO> loggerList = LoggingMgtDAO.getInstance().retrieveAPILoggerByAPIID(tenantId, apiId);
        if (log.isDebugEnabled()) {
            log.debug("Retrieved " + (loggerList != null ? loggerList.size() : 0) + " API loggers for API " + apiId);
        }
        return loggerList;
    }
}
