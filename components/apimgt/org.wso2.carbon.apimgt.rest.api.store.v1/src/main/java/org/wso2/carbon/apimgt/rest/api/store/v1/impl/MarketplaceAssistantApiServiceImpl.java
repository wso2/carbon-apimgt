/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.MarketplaceAssistant;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantRequest;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantResponse;
import org.wso2.carbon.apimgt.impl.ai.MarketplaceAssistantServiceFactory;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantApiCountResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantResponseDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;

import javax.ws.rs.core.Response;


public class MarketplaceAssistantApiServiceImpl implements MarketplaceAssistantApiService {

    private static final Log log = LogFactory.getLog(MarketplaceAssistantApiServiceImpl.class);

    @Override
    public Response marketplaceAssistantExecute(MarketplaceAssistantRequestDTO marketplaceAssistantRequestDTO,
                                                MessageContext messageContext) throws APIManagementException {
        try {
            boolean isChatQueryEmpty = StringUtils.isEmpty(marketplaceAssistantRequestDTO.getQuery());
            if (isChatQueryEmpty) {
                String errorMessage = "Payload is badly formatted. Expected to have 'query'";
                RestApiUtil.handleBadRequest(errorMessage, log);
                return null;
            }

            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String history = new Gson().toJson(marketplaceAssistantRequestDTO.getHistory());

            MarketplaceAssistantRequest request = new MarketplaceAssistantRequest();
            request.setQuery(marketplaceAssistantRequestDTO.getQuery());
            request.setHistory(history);
            request.setOrganization(organization);
            request.setUsername(username);

            MarketplaceAssistant marketplaceAssistantService =
                    MarketplaceAssistantServiceFactory.getMarketplaceAssistantService();
            MarketplaceAssistantResponse response = marketplaceAssistantService.execute(request);
            if (response == null || response.getExecuteResponse() == null) {
                return null;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            MarketplaceAssistantResponseDTO executeResponseDTO = objectMapper.readValue(response.getExecuteResponse(),
                    MarketplaceAssistantResponseDTO.class);
            return Response.status(Response.Status.CREATED).entity(executeResponseDTO).build();
        } catch (APIManagementException | IOException e) {
            if (RestApiUtil.isDueToAIServiceNotAccessible(e)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
            } else if (RestApiUtil.isDueToAIServiceThrottled(e)) {
                return Response.status(Response.Status.TOO_MANY_REQUESTS).entity(e.getMessage()).build();
            } else {
                String errorMessage = "Error encountered while executing the execute statement of Marketplace " +
                        "Assistant service";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response getMarketplaceAssistantApiCount(MessageContext messageContext) throws APIManagementException {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            MarketplaceAssistantRequest request = new MarketplaceAssistantRequest();
            request.setOrganization(organization);

            MarketplaceAssistant marketplaceAssistantService =
                    MarketplaceAssistantServiceFactory.getMarketplaceAssistantService();
            MarketplaceAssistantResponse response = marketplaceAssistantService.getApiCount(request);
            if (response == null || response.getCount() == null) {
                return null;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            MarketplaceAssistantApiCountResponseDTO executeResponseDTO = objectMapper.readValue(response.getCount(),
                    MarketplaceAssistantApiCountResponseDTO.class);
            return Response.status(Response.Status.OK).entity(executeResponseDTO).build();
        } catch (APIManagementException | IOException e) {
            String errorMessage = "Error encountered while executing the execute statement of Marketplace " +
                    "Assistant service";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
