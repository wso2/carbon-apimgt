package org.wso2.carbon.apimgt.gateway.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DeploymentStatusNotifierRunnable implements Runnable {
    private static final Log log = LogFactory.getLog(DeploymentStatusNotifierRunnable.class);
    private static final String NOTIFY_API_DEPLOYMENT_STATUS_PATH = "/notify-api-deployment-status";
    private static final String CONTENT_TYPE = "application/json";

    private final String apiId;
    private final String apiRevisionId;
    private final boolean success;
    private final String action;
    private final Long errorCode;
    private final String errorMessage;


    public DeploymentStatusNotifierRunnable(String apiId, String apiRevisionId,  boolean success, String action,
                                            Long errorCode, String errorMessage) {
        this.apiId = apiId;
        this.apiRevisionId = apiRevisionId;
        this.success = success;
        this.action = action;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public void run() {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("gatewayId", DataHolder.getInstance().getConfiguredGWID());
            payload.addProperty("apiId", apiId);
            payload.addProperty("deploymentStatus", success ? "SUCCESS" : "FAILURE");
            payload.addProperty("action", action);
            if (success){
                payload.addProperty("revisionId", apiRevisionId);
            } else {
                payload.addProperty("errorCode", errorCode);
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    payload.addProperty("errorMessage", errorMessage);
                }
            }

            String response = notifyApiDeploymentStatus(new Gson().toJson(payload));
            log.debug("/notify-api-deployment-status called. Response: " + response);
        } catch (Exception e) {
            log.error("Error notifying API deployment status", e);
        }
    }

    private String notifyApiDeploymentStatus(String payload) {
        String endpoint = getServiceURL() + NOTIFY_API_DEPLOYMENT_STATUS_PATH;
        try {
            HttpResponse response = executePost(endpoint, payload);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            log.debug("/notify-api-deployment-status called. Status: " + statusCode + ", Response: " + responseBody);
            return responseBody;
        } catch (Exception e) {
            log.error("Error occurred while calling /notify-api-deployment-status", e);
            return null;
        }
    }

    private HttpResponse executePost(String endpoint, String payload) throws Exception {
        URL url = new URL(endpoint);
        EventHubConfigurationDto config = getEventHubConfiguration();
        HttpClient httpClient = APIUtil.getHttpClient(url.getPort(), url.getProtocol());

        HttpPost request = new HttpPost(endpoint);
        request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC
                + new String(getServiceCredentials(config), StandardCharsets.UTF_8));
        request.setHeader("Content-Type", CONTENT_TYPE);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        return httpClient.execute(request);
    }

    private String getServiceURL() {
        return getEventHubConfiguration().getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP);
    }

    private EventHubConfigurationDto getEventHubConfiguration() {
        return ServiceReferenceHolder.getInstance().getApiManagerConfigurationService()
                .getAPIManagerConfiguration().getEventHubConfigurationDto();
    }

    private byte[] getServiceCredentials(EventHubConfigurationDto config) {
        String credentials = config.getUsername() + APIConstants.DELEM_COLON + config.getPassword();
        return org.apache.commons.codec.binary.Base64.encodeBase64(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
