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
    private static final String PER_API_LOGGING_PERMISSION_PATH = "/permission/protected/configure/logging";
    private static final String INVALID_LOGGING_PERMISSION = "Invalid logging permission";
    private static final String INCORRECT_LOGGING_PER_API_RESOURCE_REQUEST = "Resource Method and Resource Path both " +
            "should be included";
    private static final String REQUIRED_API_RESOURCE_IS_NOT_AVAILABLE = "Requested resource is not available";
    private final ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    public void addUpdateAPILogger(String tenantId, String apiId, String logLevel, String resourceMethod,
                                   String resourcePath) throws APIManagementException {
        if (apiMgtDAO.getAPIInfoByUUID(apiId) == null) {
            throw new APIManagementException("API not found.",
                    ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
        }
        if (!APIUtil.hasPermission(RestApiCommonUtil.getLoggedInUsername(), PER_API_LOGGING_PERMISSION_PATH)) {
            throw new APIManagementException(INVALID_LOGGING_PERMISSION,
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        if (resourceMethod != null && resourcePath != null) {
            boolean isAPIResourceExists = LoggingMgtDAO.getInstance().checkAPILoggerPerResourceAvailable(tenantId,
                    apiId, resourceMethod.toUpperCase(), resourcePath);
            if (isAPIResourceExists) {
                LoggingMgtDAO.getInstance().addAPILoggerPerResource(tenantId, apiId, logLevel,
                        resourceMethod.toUpperCase(), resourcePath);
            } else {
                throw new APIManagementException(REQUIRED_API_RESOURCE_IS_NOT_AVAILABLE,
                        ExceptionCodes.from(ExceptionCodes.LOGGING_API_RESOURCE_NOT_FOUND));
            }
        } else if (resourceMethod == null && resourcePath == null) {
            LoggingMgtDAO.getInstance().addAPILogger(tenantId, apiId, logLevel);
        } else {
            throw new APIManagementException(INCORRECT_LOGGING_PER_API_RESOURCE_REQUEST,
                    ExceptionCodes.from(ExceptionCodes.LOGGING_API_MISSING_DATA));
        }

        publishLogAPIData(tenantId, apiId, logLevel, resourceMethod, resourcePath);
    }

    private void publishLogAPIData(String tenantId, String apiId, String logLevel, String resourceMethod,
            String resourcePath) throws APIManagementException {
        APIEvent apiEvent = new APIEvent(apiId, logLevel, APIConstants.EventType.UDATE_API_LOG_LEVEL.name(),
                apiMgtDAO.getAPIContext(apiId), resourceMethod, resourcePath);
        APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());
    }

    public List<APILogInfoDTO> getAPILoggerList(String tenantId, String logLevel) throws APIManagementException {
        if (!APIUtil.hasPermission(RestApiCommonUtil.getLoggedInUsername(), PER_API_LOGGING_PERMISSION_PATH)) {
            throw new APIManagementException(INVALID_LOGGING_PERMISSION,
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        return LoggingMgtDAO.getInstance().retrieveAPILoggerList(tenantId, logLevel);
    }

    public List<APILogInfoDTO> getAPILoggerListByApiId(String tenantId, String apiId) throws APIManagementException {
        if (apiMgtDAO.getAPIInfoByUUID(apiId) == null) {
            throw new APIManagementException("API not found.",
                    ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
        }
        if (!APIUtil.hasPermission(RestApiCommonUtil.getLoggedInUsername(), PER_API_LOGGING_PERMISSION_PATH)) {
            throw new APIManagementException(INVALID_LOGGING_PERMISSION,
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        return LoggingMgtDAO.getInstance().retrieveAPILoggerByAPIID(tenantId, apiId);
    }
}
