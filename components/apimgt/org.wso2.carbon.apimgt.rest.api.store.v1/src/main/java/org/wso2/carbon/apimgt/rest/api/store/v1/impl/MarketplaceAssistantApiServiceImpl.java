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
import org.wso2.carbon.apimgt.impl.ai.MarketplaceAssistantConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantApiCountResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantResponseDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.IOException;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class MarketplaceAssistantApiServiceImpl implements MarketplaceAssistantApiService {


    private static final Log log = LogFactory.getLog(MarketplaceAssistantApiServiceImpl.class);

    private static MarketplaceAssistantConfigurationDto marketplaceAssistantConfigurationDto;

    @Override
    public Response marketplaceAssistantExecute(MarketplaceAssistantRequestDTO marketplaceAssistantRequestDTO,
                                                MessageContext messageContext) throws APIManagementException {
        APIManagerConfiguration configuration = ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            marketplaceAssistantConfigurationDto = configuration.getMarketplaceAssistantConfigurationDto();
        }
        try {
            if (marketplaceAssistantConfigurationDto.isEnabled()) {

                boolean isChatQueryEmpty = StringUtils.isEmpty(marketplaceAssistantRequestDTO.getQuery());
                if (isChatQueryEmpty) {
                    String errorMessage = "Payload is badly formatted. Expected to have 'query'";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                    return null;
                }

                String organization = RestApiUtil.getValidatedOrganization(messageContext);

                JSONObject payload = new JSONObject();
                String history = new Gson().toJson(marketplaceAssistantRequestDTO.getHistory());

                payload.put(APIConstants.QUERY, marketplaceAssistantRequestDTO.getQuery());
                payload.put(APIConstants.HISTORY, history);
                payload.put(APIConstants.TENANT_DOMAIN, organization);

                String response = APIUtil.
                        MarketplaceAssistantPostService(marketplaceAssistantConfigurationDto.getEndpoint(),
                                marketplaceAssistantConfigurationDto.getAccessToken(),
                                marketplaceAssistantConfigurationDto.getChatResource(), payload.toString());

                ObjectMapper objectMapper = new ObjectMapper();
                MarketplaceAssistantResponseDTO executeResponseDTO = objectMapper.readValue(response,
                        MarketplaceAssistantResponseDTO.class);
                return Response.status(Response.Status.CREATED).entity(executeResponseDTO).build();
            }
        } catch (APIManagementException | IOException e) {
            String errorMessage = "Error encountered while executing the execute statement of Marketplace Assistant service";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
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
            marketplaceAssistantConfigurationDto = configuration.getMarketplaceAssistantConfigurationDto();
        }
        try {
            if (marketplaceAssistantConfigurationDto.isEnabled()) {

                CloseableHttpResponse response = APIUtil.
                        getMarketplaceChatApiCount(marketplaceAssistantConfigurationDto.getEndpoint(),
                                marketplaceAssistantConfigurationDto.getApiCountResource());
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    String responseStr = EntityUtils.toString(response.getEntity());
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully completed the Marketplace Assistant api count call with status code: " + statusCode);
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    MarketplaceAssistantApiCountResponseDTO executeResponseDTO = objectMapper.readValue(responseStr,
                            MarketplaceAssistantApiCountResponseDTO.class);
                    return Response.status(Response.Status.OK).entity(executeResponseDTO).build();
                } else {
                    String errorMessage = "Error encountered while executing the Marketplace Assistant service to accomodate the " +
                            "specified testing requirement";
                    log.error(errorMessage);
                    RestApiUtil.handleInternalServerError(errorMessage, log);
                }
            }
        } catch (APIManagementException | IOException e) {
            String errorMessage = "Error encountered while executing the execute statement of Marketplace Assistant service";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
