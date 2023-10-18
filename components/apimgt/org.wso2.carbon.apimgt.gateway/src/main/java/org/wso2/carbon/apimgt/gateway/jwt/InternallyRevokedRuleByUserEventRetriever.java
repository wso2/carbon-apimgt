package org.wso2.carbon.apimgt.gateway.jwt;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.gateway.dto.RevokedJWTUserDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class will fetch users of revoked JWTs via webservice database during startup
 */
public class InternallyRevokedRuleByUserEventRetriever extends TimerTask {
    private static final Log log = LogFactory.getLog(InternallyRevokedRuleByUserEventRetriever.class);

    private static final int revokedJWTUserRetrievalTimeoutInSeconds = 15;

    private static final int revokedJWTUserRetrievalRetries = 15;

    @Override
    public void run() {
        loadRevokedJWTUsersFromWebService();
    }

    private void loadRevokedJWTUsersFromWebService() {
        RevokedJWTUserDTO[] revokedJWTUserDTOs = retrieveRevokedJWTUsersData();
        if (revokedJWTUserDTOs != null) {
            for (RevokedJWTUserDTO revokedJWTUserDTO : revokedJWTUserDTOs) {
                if ("USER_ID".equals(revokedJWTUserDTO.getSubjectIdType())) {
                    InternalRevokedJWTDataHolder.getInstance().
                            addInternalRevokedJWTUserIDToMap(revokedJWTUserDTO.getSubjectId(),
                                    revokedJWTUserDTO.getRevocationTime());
                } else if ("CLIENT_ID".equals(revokedJWTUserDTO.getSubjectIdType())) {
                    InternalRevokedJWTDataHolder.getInstance().
                            addInternalRevokedJWTClientIDToAppOnlyMap(revokedJWTUserDTO.getSubjectId(),
                                    revokedJWTUserDTO.getRevocationTime());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Subject Id : " + revokedJWTUserDTO.getSubjectId()
                            + " added to the user event revoke map.");
                }
            }
        } else {
            log.debug("No revoked JWT users are retrieved via web service");
        }
    }

    private RevokedJWTUserDTO[] retrieveRevokedJWTUsersData() {
        try {
            // The resource resides in the throttle web app. Hence, reading throttle configs
            String url = getEventHubConfiguration().getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP).concat(
                    "/revokedUsers");
            HttpGet method = new HttpGet(url);
            byte[] credentials = Base64.encodeBase64((getEventHubConfiguration().getUsername() + ":" +
                    getEventHubConfiguration().getPassword()).getBytes(StandardCharsets.UTF_8));
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            URL keyMgtURL = new URL(url);
            int keyMgtPort = keyMgtURL.getPort();
            String keyMgtProtocol = keyMgtURL.getProtocol();
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            HttpResponse httpResponse = null;
            int retryCount = 0;
            boolean retry;
            do {
                try {
                    httpResponse = httpClient.execute(method);
                    retry = false;
                } catch (IOException ex) {
                    retryCount++;
                    if (retryCount < revokedJWTUserRetrievalRetries) {
                        retry = true;
                        log.warn("Failed retrieving revoked JWT users from remote endpoint: " +
                                ex.getMessage() + ". Retrying after " + revokedJWTUserRetrievalTimeoutInSeconds +
                                " seconds...");
                        Thread.sleep(revokedJWTUserRetrievalTimeoutInSeconds * 1000);
                    } else {
                        throw ex;
                    }
                }
            } while (retry);

            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (responseString != null && !responseString.isEmpty()) {
                return new Gson().fromJson(responseString, RevokedJWTUserDTO[].class);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Exception when retrieving revoked JWT Users from remote endpoint ", e);
        }
        return null;
    }


    /**
     * Initiates the timer task to fetch data from the web service.
     * Timer task will not run after the retry count is completed.
     */
    public void startRevokedJWTUsersRetrievalTask() {
        //using same initDelay as in keytemplates,blocking conditions retriever
        new Timer().schedule(this, getEventHubConfiguration().getInitDelay());
    }

    protected EventHubConfigurationDto getEventHubConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
    }
}
