/*
 *
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.LoggingMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APILogInfoDTO;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;

import java.util.List;

/**
 * API logging implementation.
 */
public class PerAPILoggingImpl {
    private static final String PER_API_LOGGING_PERMISSION_PATH = "/permission/protected/configure/logging";

    public void addUpdateAPILogger(String tenantId, String apiId, String logLevel) throws APIManagementException {
        if (!APIUtil.hasPermission(RestApiCommonUtil.getLoggedInUsername(), PER_API_LOGGING_PERMISSION_PATH)) {
            throw new APIManagementException("Invalid logging permission",
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        LoggingMgtDAO.getInstance().addAPILogger(tenantId, apiId, logLevel);
        publishLogAPIData(tenantId, apiId, logLevel);
    }
    private void publishLogAPIData(String tenant, String apiId, String value) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API existingAPI = apiProvider.getAPIbyUUID(apiId, tenant);
        APIEvent apiEvent = new APIEvent(apiId, value, APIConstants.EventType.UDATE_API_LOG_LEVEL.name(), existingAPI.getContext());
        APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());
    }
    public List<APILogInfoDTO> getAPILoggerList(String tenantId, boolean loggingEnabled) throws APIManagementException {
        if (!APIUtil.hasPermission(RestApiCommonUtil.getLoggedInUsername(), PER_API_LOGGING_PERMISSION_PATH)) {
            throw new APIManagementException("Invalid logging permission",
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        return LoggingMgtDAO.getInstance().retrieveAPILoggerList(tenantId, loggingEnabled);
    }

    public List<APILogInfoDTO> getAPILoggerListByApiId(String tenantId, String apiId) throws APIManagementException {
        if (!APIUtil.hasPermission(RestApiCommonUtil.getLoggedInUsername(), PER_API_LOGGING_PERMISSION_PATH)) {
            throw new APIManagementException("Invalid logging permission",
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        return LoggingMgtDAO.getInstance().retrieveAPILoggerByAPIID(tenantId, apiId);
    }
}
