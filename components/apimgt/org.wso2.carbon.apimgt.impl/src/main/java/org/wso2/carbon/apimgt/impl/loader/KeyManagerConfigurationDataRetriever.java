package org.wso2.carbon.apimgt.impl.loader;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
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
                    do {
                        try {
                            httpResponse = httpClient.execute(method);
                            retry = false;
                        } catch (IOException ex) {
                            retryCount++;
                            int maxRetries = 15;
                            if (retryCount < maxRetries) {
                                retry = true;
                                long retryTimeout = 15l;
                                log.warn("Failed retrieving Key Manager Configurations from remote " +
                                        "endpoint: " + ex.getMessage()
                                        + ". Retrying after " + retryTimeout + " seconds...");
                                Thread.sleep(retryTimeout * 1000);
                            } else {
                                throw ex;
                            }
                        }
                    } while (retry);
                    String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                    KeyManagerConfiguration[] keyManagerConfigurations =
                            new Gson().fromJson(responseString, KeyManagerConfiguration[].class);
                    if (responseString != null && !responseString.isEmpty()) {
                    }
                    for (KeyManagerConfiguration keyManagerConfiguration : keyManagerConfigurations) {
                        if (keyManagerConfiguration.isEnabled()){
                            try {
                                ServiceReferenceHolder.getInstance().getKeyManagerConfigurationService()
                                        .addKeyManagerConfiguration(keyManagerConfiguration.getTenantDomain(),
                                                keyManagerConfiguration.getName(), keyManagerConfiguration.getType(),
                                                keyManagerConfiguration);
                            } catch (APIManagementException e) {
                                log.error("Error while configuring Key Manager "+ keyManagerConfiguration.getName() +
                                        " in tenant " + keyManagerConfiguration.getTenantDomain(), e);
                            }
                        }
                    }

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
