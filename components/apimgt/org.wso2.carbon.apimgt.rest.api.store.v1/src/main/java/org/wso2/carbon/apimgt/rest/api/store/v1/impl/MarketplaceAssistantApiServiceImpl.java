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
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantApiCountResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantResponseDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.OAuthUtil;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;

import java.io.IOException;

import javax.ws.rs.core.Response;


public class MarketplaceAssistantApiServiceImpl implements MarketplaceAssistantApiService {

    private static final Log log = LogFactory.getLog(MarketplaceAssistantApiServiceImpl.class);

    private static MarketplaceAssistantConfigurationDTO configDto;

    @Override
    public Response marketplaceAssistantExecute(MarketplaceAssistantRequestDTO marketplaceAssistantRequestDTO,
                                                MessageContext messageContext) throws APIManagementException {
        APIManagerConfiguration configuration = ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            configDto = configuration.getMarketplaceAssistantConfigurationDto();
        }
        try {
            if (configDto.isKeyProvided() || configDto.isAuthTokenProvided()) {

                boolean isChatQueryEmpty = StringUtils.isEmpty(marketplaceAssistantRequestDTO.getQuery());
                if (isChatQueryEmpty) {
                    String errorMessage = "Payload is badly formatted. Expected to have 'query'";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                    return null;
                }

                String organization = RestApiUtil.getValidatedOrganization(messageContext);

                JSONObject payload = new JSONObject();
                String history = new Gson().toJson(marketplaceAssistantRequestDTO.getHistory());

                String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
                String userRoles = new Gson().toJson(APIUtil.getListOfRoles(username));

                payload.put(APIConstants.QUERY, marketplaceAssistantRequestDTO.getQuery());
                payload.put(APIConstants.HISTORY, history);
                payload.put(APIConstants.TENANT_DOMAIN, organization);
                payload.put(APIConstants.USERROLES, userRoles.toLowerCase());
                payload.put(APIConstants.APIM_VERSION, APIUtil.getAPIMVersion());

                String response;
                if (configDto.isKeyProvided()) {
                    response = APIUtil.invokeAIService(configDto.getEndpoint(), configDto.getTokenEndpoint(),
                            configDto.getKey(), configDto.getChatResource(), payload.toString(), null);
                } else {
                    response = APIUtil.invokeAIService(configDto.getEndpoint(), null,
                            configDto.getAccessToken(), configDto.getChatResource(), payload.toString(), null);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                MarketplaceAssistantResponseDTO executeResponseDTO = objectMapper.readValue(response,
                        MarketplaceAssistantResponseDTO.class);
                return Response.status(Response.Status.CREATED).entity(executeResponseDTO).build();
            }
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
        APIManagerConfiguration configuration = ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            configDto = configuration.getMarketplaceAssistantConfigurationDto();
        }
        CloseableHttpResponse response = null;
        try {
            if (configDto.isKeyProvided() || configDto.isAuthTokenProvided()) {
                if (configDto.isKeyProvided()) {
                    response = APIUtil.
                            getMarketplaceChatApiCount(configDto.getEndpoint(),
                                    configDto.getTokenEndpoint(),
                                    configDto.getKey(),
                                    configDto.getApiCountResource());
                } else {
                    response = APIUtil.
                            getMarketplaceChatApiCount(configDto.getEndpoint(),
                                    null,
                                    configDto.getAccessToken(),
                                    configDto.getApiCountResource());
                }
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    String responseStr = EntityUtils.toString(response.getEntity());
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully completed the Marketplace Assistant api count call with status code: " +
                                statusCode);
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    MarketplaceAssistantApiCountResponseDTO executeResponseDTO = objectMapper.readValue(responseStr,
                            MarketplaceAssistantApiCountResponseDTO.class);
                    return Response.status(Response.Status.OK).entity(executeResponseDTO).build();
                } else {
                    String errorMessage = "Error encountered while executing the Marketplace Assistant service to " +
                            "accommodate the specified testing requirement";
                    log.error(errorMessage);
                    RestApiUtil.handleInternalServerError(errorMessage, log);
                }
            }
        } catch (APIManagementException | IOException e) {
            String errorMessage = "Error encountered while executing the execute statement of Marketplace " +
                    "Assistant service";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("Error while closing the CloseableHttpResponse", e);
                }
            }
        }
        return null;
    }
}
