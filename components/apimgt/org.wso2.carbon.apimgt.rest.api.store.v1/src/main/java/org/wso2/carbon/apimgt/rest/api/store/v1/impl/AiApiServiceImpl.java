package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatExecuteRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatExecuteResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatPreparationRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatPreparationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.MalformedURLException;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


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

    public Response apiChatPrepare(ApiChatPreparationRequestDTO apiChatPreparationRequestDTO, String apiChatRequestId, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response getApiChatHealth(MessageContext messageContext) {
        try {
            if (APIUtil.isApiChatEnabled()) {
                CloseableHttpResponse response = APIUtil.getAIServiceHealth(APIConstants.API_CHAT_ENDPOINT, APIConstants.API_CHAT_AUTH_TOKEN);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return Response.ok().build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }
        } catch (MalformedURLException e) {
            String errorMessage = "Malformed URL detected when attempting to perform health check against API Chat service";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (APIManagementException e) {
            String errorMessage = "Error encountered while connecting to the API Chat service";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
