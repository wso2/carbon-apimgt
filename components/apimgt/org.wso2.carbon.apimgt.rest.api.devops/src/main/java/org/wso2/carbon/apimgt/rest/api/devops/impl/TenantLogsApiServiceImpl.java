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
package org.wso2.carbon.apimgt.rest.api.devops.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.devops.impl.logging.APILoggingImpl;
import org.wso2.carbon.apimgt.impl.dto.APILogInfoDTO;
import org.wso2.carbon.apimgt.rest.api.devops.DevopsAPIUtils;
import org.wso2.carbon.apimgt.rest.api.devops.TenantLogsApiService;
import org.wso2.carbon.apimgt.rest.api.devops.dto.LoggingApiInputDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.LoggingApiOutputListDTO;

import java.util.List;
import javax.ws.rs.core.Response;

/**
 * Devops API implementation.
 */
public class TenantLogsApiServiceImpl implements TenantLogsApiService {

    private static final Log log = LogFactory.getLog(TenantLogsApiServiceImpl.class);

    public Response tenantLogsTenantApisApiIdGet(String tenant, String apiId, MessageContext messageContext)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving API loggers for tenant: " + tenant + ", apiId: " + apiId);
        }
        
        try {
            APILoggingImpl apiLoggingImpl = new APILoggingImpl();
            List<APILogInfoDTO> apiLogInfoDTOList = apiLoggingImpl.getAPILoggerListByApiId(tenant, apiId);
            LoggingApiOutputListDTO loggingApiOutputListDT = DevopsAPIUtils.getLoggingAPIList(apiLogInfoDTOList);
            
            log.info("Successfully retrieved API loggers for tenant: " + tenant + ", apiId: " + apiId);
            return Response.ok().entity(loggingApiOutputListDT).build();
        } catch (Exception e) {
            log.error("Error occurred while retrieving API loggers for tenant: " + tenant + ", apiId: " + apiId, e);
            throw e;
        }
    }

    public Response tenantLogsTenantApisApiIdPut(String tenant, String apiId, LoggingApiInputDTO loggingApiInputDTO,
            MessageContext messageContext) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Updating API logger for tenant: " + tenant + ", apiId: " + apiId + 
                    ", logLevel: " + (loggingApiInputDTO != null ? loggingApiInputDTO.getLogLevel() : "null"));
        }
        
        if (apiId != null) {
            if (DevopsAPIUtils.validateLogLevel(loggingApiInputDTO.getLogLevel())) {
                try {
                    APILoggingImpl apiLoggingImpl = new APILoggingImpl();
                    apiLoggingImpl.addUpdateAPILogger(tenant, apiId, loggingApiInputDTO.getLogLevel().toUpperCase(),
                            loggingApiInputDTO.getResourceMethod(), loggingApiInputDTO.getResourcePath());
                    
                    log.info("Successfully updated API logger for tenant: " + tenant + ", apiId: " + apiId + 
                            ", logLevel: " + loggingApiInputDTO.getLogLevel());
                    return Response.ok().entity(loggingApiInputDTO).build();
                } catch (Exception e) {
                    log.error("Error occurred while updating API logger for tenant: " + tenant + ", apiId: " + apiId, e);
                    throw e;
                }
            } else {
                log.warn("Invalid log level provided for tenant: " + tenant + ", apiId: " + apiId + 
                        ", logLevel: " + loggingApiInputDTO.getLogLevel());
                throw new APIManagementException("The input log level is incorrect: Input log level : " +
                        loggingApiInputDTO.getLogLevel(),
                        ExceptionCodes.from(ExceptionCodes.LOGGING_API_INCORRECT_LOG_LEVEL));
            }
        } else {
            log.error("API ID is missing for tenant: " + tenant);
            throw new APIManagementException("API ID is missing",
                    ExceptionCodes.from(ExceptionCodes.LOGGING_API_MISSING_DATA));
        }
    }

    public Response tenantLogsTenantApisGet(String tenant, String logLevel, MessageContext messageContext)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving API loggers for tenant: " + tenant + ", logLevel: " + logLevel);
        }
        
        try {
            APILoggingImpl apiLoggingImpl = new APILoggingImpl();
            List<APILogInfoDTO> apiLogInfoDTOList = apiLoggingImpl.getAPILoggerList(tenant, logLevel);
            LoggingApiOutputListDTO loggingApiOutputListDTO = DevopsAPIUtils.getLoggingAPIList(apiLogInfoDTOList);
            
            log.info("Successfully retrieved API loggers for tenant: " + tenant + 
                    (logLevel != null ? ", logLevel: " + logLevel : ""));
            return Response.ok().entity(loggingApiOutputListDTO).build();
        } catch (Exception e) {
            log.error("Error occurred while retrieving API loggers for tenant: " + tenant + 
                    (logLevel != null ? ", logLevel: " + logLevel : ""), e);
            throw e;
        }
    }
}
