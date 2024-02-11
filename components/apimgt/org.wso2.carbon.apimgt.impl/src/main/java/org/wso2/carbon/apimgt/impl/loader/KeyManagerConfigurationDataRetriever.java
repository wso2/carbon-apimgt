package org.wso2.carbon.apimgt.impl.loader;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class KeyManagerConfigurationDataRetriever extends TimerTask {

    public KeyManagerConfigurationDataRetriever(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    private Log log = LogFactory.getLog(KeyManagerConfigurationDataRetriever.class);
    private String tenantDomain;
    @Override
    public void run() {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            EventHubConfigurationDto eventHubConfigurationDto =
                    apiManagerConfiguration.getEventHubConfigurationDto();
            if (eventHubConfigurationDto != null && eventHubConfigurationDto.isEnabled()) {
                try {
                    String url = eventHubConfigurationDto.getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP)
                            .concat("/keymanagers");
                    byte[] credentials = Base64.encodeBase64((eventHubConfigurationDto.getUsername() + ":" +
                            eventHubConfigurationDto.getPassword()).getBytes());
                    HttpGet method = new HttpGet(url);
                    method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
                    method.setHeader(APIConstants.HEADER_TENANT, tenantDomain);
                    URL configUrl = new URL(url);
                    int port = configUrl.getPort();
                    String protocol = configUrl.getProtocol();
                    HttpClient httpClient = APIUtil.getHttpClient(port, protocol);
                    HttpResponse httpResponse = null;
                    int retryCount = 0;
                    boolean retry;
                    int maxRetries = 15;
                    do {
                        try {
                            httpResponse = httpClient.execute(method);
                            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                                KeyManagerConfigurationDTO[] keyManagerConfigurationDTOS =
                                        new Gson().fromJson(responseString, KeyManagerConfigurationDTO[].class);
                                for (KeyManagerConfigurationDTO keyManagerConfiguration : keyManagerConfigurationDTOS) {
                                    if (keyManagerConfiguration.isEnabled()) {
                                        KeyManagerConfiguration resolvedKeyManagerConfiguration = APIUtil.toKeyManagerConfiguration(keyManagerConfiguration);
                                        try {
                                            ServiceReferenceHolder.getInstance().getKeyManagerConfigurationService()
                                                    .addKeyManagerConfiguration(
                                                            resolvedKeyManagerConfiguration.getTenantDomain(),
                                                            resolvedKeyManagerConfiguration.getName(),
                                                            resolvedKeyManagerConfiguration.getType(),
                                                            resolvedKeyManagerConfiguration);
                                        } catch (APIManagementException e) {
                                            log.error("Error while configuring Key Manager " +
                                                    keyManagerConfiguration.getName() +
                                                    " in tenant " + resolvedKeyManagerConfiguration.getTenantDomain(), e);
                                        }
                                    }
                                }
                                retry = false;
                            } else {
                                retry = true;
                                retryCount++;
                            }
                        } catch (IOException ex) {
                            if (retryCount < maxRetries) {
                                retry = true;
                            } else {
                                throw ex;
                            }
                        }
                        if (retry) {
                            if (retryCount < maxRetries) {
                                retryCount++;
                                long retryTimeout = (long) Math.min(Math.pow(2, retryCount), 300);
                                log.warn("Failed retrieving Key Manager Configurations from remote " +
                                        "endpoint. Retrying after " + retryTimeout + " seconds...");
                                Thread.sleep(retryTimeout * 1000);
                            } else {
                                log.error("Failed to retrieve Key Manager configuration with " + retryCount + " times.");
                            }
                        }
                    } while (retry);
                } catch (InterruptedException | IOException  e) {
                    log.error("Error while retrieving key manager configurations", e);
                }
            }
        }
    }

    public void startLoadKeyManagerConfigurations() {
        new Timer().schedule(this, 5);
    }
}
