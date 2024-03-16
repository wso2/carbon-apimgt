package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatExecuteRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatExecuteResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatPreparationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.IOException;

import javax.ws.rs.core.Response;

public class AiApiServiceImpl implements AiApiService {
    private static final Log log = LogFactory.getLog(AiApiServiceImpl.class);

    public Response apiChatExecute(String apiChatRequestId, ApiChatExecuteRequestDTO apiChatExecuteRequestDTO, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response apiChatPrepare(String apiChatRequestId, String apiId, MessageContext messageContext) {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            String swaggerDefinition = apiConsumer.getOpenAPIDefinition(apiId, organization);
            String payload = "{\"openapi\": " + swaggerDefinition + "}";

            if (APIUtil.isApiChatEnabled()) {
                CloseableHttpResponse response = APIUtil.invokeAIService(APIConstants.API_CHAT_ENDPOINT,
                        APIConstants.API_CHAT_AUTH_TOKEN, APIConstants.API_CHAT_PREPARE_RESOURCE, payload,
                        apiChatRequestId);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_CREATED) {
                    HttpEntity entity = response.getEntity();
                    String responseStr = EntityUtils.toString(entity);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully executed the API Chat preparation with status code: "
                                + statusCode);
                    }
                    log.info("201 PREPARE CALL SUCCEEDED >>>>> ");
                    ObjectMapper objectMapper = new ObjectMapper();
                    log.info(responseStr);
//                    ApiChatPreparationResponseDTO preparationResponseDTO =
                    return Response.status(Response.Status.CREATED).entity(responseStr).build();
//                    return Response.created().entity(responseStr).build();
                } else {
                    String errorMessage = "Error encountered while executing the prepare statement of API Chat service";
                    log.error(errorMessage);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(errorMessage)
                            .build();
                }
            }
        } catch (APIManagementException | IOException e) {
            String errorMessage = "Error encountered while executing the prepare statement of API Chat service";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response getApiChatHealth(MessageContext messageContext) {
        try {
            if (APIUtil.isApiChatEnabled()) {
                CloseableHttpResponse response = APIUtil.getAIServiceHealth(APIConstants.API_CHAT_ENDPOINT,
                        APIConstants.API_CHAT_HEALTH_RESOURCE);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return Response.ok().build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error encountered while connecting to the API Chat service";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
