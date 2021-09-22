/*
 * Copyright (c) 2020, WSO2 Inc.(http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * */

package org.wso2.carbon.apimgt.rest.api.gateway.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.gateway.perlogging.PerAPILogger;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.perlog.PerAPILogService;
import org.wso2.carbon.apimgt.rest.api.gateway.ApiLoggingApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.util.GatewayAPIUtils;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;

import javax.ws.rs.core.Response;

public class ApiLoggingApiServiceImpl implements ApiLoggingApiService {

    private static final Log log = LogFactory.getLog(ApiLoggingApiServiceImpl.class);

    private static final String LOG_ALL = "all";
    private static final String LOG_HEADERS = "headers";
    private static final String LOG_BODY = "body";

    PerAPILogService perAPILogService = PerAPILogger.getInstance();

    public Response apiLoggingDelete(String context, MessageContext messageContext) throws APIManagementException {
        // check if the value exists
        if (context == null) {
            perAPILogService.publishLogAPIData("", APIConstants.APILogHandler.DELETE_ALL);
            return Response.status(204).build();
        }
        String logLevel = perAPILogService.getLogData(context);
        if (logLevel != null) {
            perAPILogService.publishLogAPIData(context, APIConstants.APILogHandler.DELETE);
            return Response.status(204).build();
        } else {
            throw new APIManagementException("The API data to be deleted not found for context : " + context,
                                             ExceptionCodes.from(ExceptionCodes.LOGGING_API_NOT_FOUND, context));
        }
    }

    public Response apiLoggingGet(String context, MessageContext messageContext) throws APIManagementException {

        APIListDTO apiListDTO = new APIListDTO();
        List<APIDTO> apidtos = new ArrayList<>();
        if (context != null) {
            //If only a single API detail is needed
            String logLevel = perAPILogService.getLogData(context);
            if (logLevel != null) {
                APIDTO apidto = new APIDTO();
                apidto.setContext(context);
                apidto.setLogLevel(logLevel);
                apidtos.add(apidto);
            } else {
                // If there is not entry for the given API context, return an error
                throw new APIManagementException("The API data to be retrieved not found for context :" + context,
                                                 ExceptionCodes.from(ExceptionCodes.LOGGING_API_NOT_FOUND, context));
            }
        } else {
            // Retrieving all the API details from the log data holder
            Map<String, String> logProperties = perAPILogService.getLogData();
            for (Map.Entry<String, String> entry : logProperties.entrySet()) {
                APIDTO apidto = new APIDTO();
                apidto.setContext(entry.getKey());
                apidto.setLogLevel(entry.getValue());
                apidtos.add(apidto);
            }
        }
        apiListDTO.setApis(apidtos);
        return Response.ok().entity(apiListDTO).build();
    }

    public Response apiLoggingPost(APIListDTO payload, String context, String logLevel, MessageContext messageContext)
            throws APIManagementException {
        if (payload != null) {
            for (int i = 0; i < payload.getApis().size(); i++) {
                APIDTO apidto = payload.getApis().get(i);
                logLevel = apidto.getLogLevel();
                context = apidto.getContext();
                apidto.setContext(GatewayAPIUtils.contextTemplateValidation(apidto.getContext()));
                if (GatewayAPIUtils.validateLogLevel(logLevel)) {
                    perAPILogService.publishLogAPIData(apidto.getContext(), apidto.getLogLevel());
                } else {
                    throw new APIManagementException("The input log level is incorrect: Input log level : " + logLevel,
                                                     ExceptionCodes.from(ExceptionCodes.LOGGING_API_INCORRECT_LOG_LEVEL));
                }
            }
            //Response would be the added payload
            return Response.ok().entity(payload).build();
        }
        // If both payload and the query params empty , consider it as a bad request
        if (StringUtils.isEmpty(context) || StringUtils.isEmpty(logLevel)) {
            throw new APIManagementException("Context or the log level is missing",
                                             ExceptionCodes.from(ExceptionCodes.LOGGING_API_MISSING_DATA));
        }
        if (GatewayAPIUtils.validateLogLevel(logLevel)) {
            // Response would be the added details as API details list object
            context = GatewayAPIUtils.contextTemplateValidation(context);
            APIListDTO apiListDTO = new APIListDTO();
            APIDTO apidto = new APIDTO();
            apidto.setContext(context);
            apidto.setLogLevel(logLevel.toLowerCase());
            List<APIDTO> apidtos = new ArrayList<>();
            apidtos.add(apidto);
            apiListDTO.setApis(apidtos);
            perAPILogService.publishLogAPIData(context, logLevel);
            return Response.ok().entity(apiListDTO).build();
        }
        // Invalid log level handle as a bad request
        throw new APIManagementException("The input log level is incorrect: Input log level : " + logLevel,
                                         ExceptionCodes.from(ExceptionCodes.LOGGING_API_INCORRECT_LOG_LEVEL));
    }
}
