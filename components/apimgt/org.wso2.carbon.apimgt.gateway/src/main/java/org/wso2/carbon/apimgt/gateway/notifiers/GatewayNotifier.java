/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.notifiers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for heartbeat notifications and handling gateway registration.
 */
public class GatewayNotifier {
    private static final Log log = LogFactory.getLog(GatewayNotifier.class);
    private static volatile GatewayNotifier instance;
    private final GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;
    private final GatewayNotificationConfiguration gatewayNotificationConfiguration;
    private final GatewayNotificationConfiguration.HeartbeatConfiguration heartbeatConfiguration;
    private final GatewayNotificationConfiguration.RegistrationConfiguration registrationConfiguration;
    private final ScheduledExecutorService scheduler;
    private final ScheduledExecutorService registrationExecutor;
    private final int maxRetryCount;
    private final double retryProgressionFactor;
    private final long retryDuration;
    private int notifyIntervalSeconds;
    private boolean gatewayNotificationEnabled;
    private String gatewayID;

    /**
     * Constructs a new GatewayHealthMonitor instance.
     * Initializes the scheduler, loads configuration settings, and sets up heartbeat parameters
     */
    private GatewayNotifier() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        registrationExecutor = Executors.newSingleThreadScheduledExecutor();
        gatewayNotificationConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getGatewayNotificationConfiguration();
        gatewayArtifactSynchronizerProperties = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration()
                .getGatewayArtifactSynchronizerProperties();

        gatewayNotificationEnabled = gatewayNotificationConfiguration.isEnabled();
        heartbeatConfiguration = gatewayNotificationConfiguration.getHeartbeat();
        notifyIntervalSeconds = heartbeatConfiguration.getNotifyIntervalSeconds();
        registrationConfiguration = gatewayNotificationConfiguration.getRegistration();
        maxRetryCount = registrationConfiguration.getMaxRetryCount();
        retryProgressionFactor = registrationConfiguration.getRetryProgressionFactor();
        retryDuration = registrationConfiguration.getRetryDuration();

        gatewayID = gatewayNotificationConfiguration.getGatewayID();

        if (gatewayID == null || gatewayID.isEmpty()) {
            gatewayID = UUID.randomUUID().toString();
        }
        DataHolder.getInstance().setGatewayID(gatewayID);
    }

    public static synchronized GatewayNotifier getInstance() {
        if (instance == null) {
            instance = new GatewayNotifier();
        }
        return instance;
    }

    /**
     * Registers the gateway with the control plan.
     */
    public void registerGateway() {
        if (log.isDebugEnabled()) {
            log.debug("Registering Gateway with ID: " + gatewayID);
        }
        registrationExecutor.execute(new GatewayRegistrationWorker());
    }

    /**
     * Starts the gateway health monitoring system.
     */
    public void startHeartbeat() {
        if (!gatewayNotificationEnabled) {
            log.warn("Gateway heartbeat is disabled by configuration. CP will not get the heartbeat notifications.");
            return;
        }
        scheduler.scheduleAtFixedRate(new Heartbeat(), 30, notifyIntervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * Stops the gateway health monitoring system.
     */
    public void stopHeartbeat() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (registrationExecutor != null && !registrationExecutor.isShutdown()) {
            registrationExecutor.shutdown();
            try {
                if (!registrationExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    registrationExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                registrationExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private String buildRegistrationPayload(String gatewayID) {
        long millis = Instant.now().toEpochMilli();
        if (gatewayID == null || gatewayID.isEmpty()) {
            gatewayID = UUID.randomUUID().toString();
        }
        String ipAddress = null;
        try {
            byte[] addressBytes = APIUtil.getLocalAddress().getAddress();
            ipAddress = InetAddress.getByAddress(addressBytes).getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Failed to get IP address", e);
            ipAddress = "unknown";
        }

        JsonObject node = new JsonObject();
        node.addProperty(APIConstants.GatewayNotification.PAYLOAD_TYPE,
                         APIConstants.GatewayNotification.PAYLOAD_TYPE_REGISTER);
        node.addProperty(APIConstants.GatewayNotification.GATEWAY_ID, gatewayID);
        node.addProperty(APIConstants.GatewayNotification.TIMESTAMP, millis);

        JsonObject gatewayProperties = new JsonObject();
        gatewayProperties.addProperty("ipAddress", ipAddress);
        node.add(APIConstants.GatewayNotification.GATEWAY_PROPERTIES, gatewayProperties);

        JsonArray environmentLabels = new JsonArray();
        for (String label : gatewayArtifactSynchronizerProperties.getGatewayLabels()) {
            environmentLabels.add(label);
        }
        node.add(APIConstants.GatewayNotification.ENVIRONMENT_LABELS, environmentLabels);

        JsonArray loadingTenants = new JsonArray();
        if (gatewayArtifactSynchronizerProperties.getLoadingTenants().isIncludeAllTenants()) {
            loadingTenants.add(APIConstants.GatewayNotification.WSO2_ALL_TENANTS);
        } else {
            for (String tenant : gatewayArtifactSynchronizerProperties.getLoadingTenants().getIncludingTenants()) {
                loadingTenants.add(tenant);
            }
        }
        node.add(APIConstants.GatewayNotification.LOADING_TENANTS, loadingTenants);

        return node.toString();
    }

    private String buildHeartbeatPayload(String gatewayID) {
        long millis = Instant.now().toEpochMilli();
        JsonObject node = new JsonObject();
        node.addProperty(APIConstants.GatewayNotification.PAYLOAD_TYPE,
                         APIConstants.GatewayNotification.PAYLOAD_TYPE_HEARTBEAT);
        node.addProperty(APIConstants.GatewayNotification.GATEWAY_ID, gatewayID);
        node.addProperty(APIConstants.GatewayNotification.TIMESTAMP, millis);

        JsonArray loadingTenants = new JsonArray();
        if (gatewayArtifactSynchronizerProperties.getLoadingTenants().isIncludeAllTenants()) {
            loadingTenants.add(APIConstants.GatewayNotification.WSO2_ALL_TENANTS);
        } else {
            for (String tenant : gatewayArtifactSynchronizerProperties.getLoadingTenants().getIncludingTenants()) {
                loadingTenants.add(tenant);
            }
        }
        node.add(APIConstants.GatewayNotification.LOADING_TENANTS, loadingTenants);

        return node.toString();
    }

    private class GatewayRegistrationWorker implements Runnable {
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Registering Gateway with ID: " + gatewayID);
            }
            String registrationPayload = buildRegistrationPayload(gatewayID);

            try {
                EventHubConfigurationDto config =
                        ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
                String serviceURLStr = config.getServiceUrl().concat(
                        APIConstants.GatewayNotification.GATEWAY_NOTIFICATION_ENDPOINT);
                URL url = new URL(serviceURLStr);
                HttpClient httpClient = APIUtil.getHttpClient(url.getPort(), url.getProtocol());
                HttpPost request = new HttpPost(serviceURLStr);
                request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                                  APIConstants.AUTHORIZATION_BASIC + new String(Base64.encodeBase64(
                                          (config.getUsername() + APIConstants.DELEM_COLON
                                                  + config.getPassword()).getBytes(StandardCharsets.UTF_8)),
                                                                                StandardCharsets.UTF_8));
                request.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                request.setEntity(new StringEntity(registrationPayload, ContentType.APPLICATION_JSON));
                DataHolder.setGatewayRegistrationResponse(DataHolder.GatewayRegistrationResponse.NOT_RESPONDED);

                try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(request, httpClient,
                                                                                            retryDuration,
                                                                                            maxRetryCount,
                                                                                            retryProgressionFactor)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();
                    String status = jsonObject.get("status").getAsString();
                    if (APIConstants.GatewayNotification.STATUS_REGISTERED.equals(status.trim())) {
                        DataHolder.setGatewayRegistrationResponse(DataHolder.GatewayRegistrationResponse.REGISTERED);
                    } else if (APIConstants.GatewayNotification.STATUS_ACKNOWLEDGED.equals(status.trim())) {
                        DataHolder.setGatewayRegistrationResponse(DataHolder.GatewayRegistrationResponse.ACKNOWLEDGED);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("/notify-gateway called. Status: " + statusCode + ", Response: " + responseBody);
                    }
                }
            } catch (IOException | APIManagementException e) {
                log.error("Error occurred while executing Gateway Registration", e);
            }
        }
    }

    private class Heartbeat implements Runnable {
        @Override
        public void run() {
            String heartbeatPayload = buildHeartbeatPayload(gatewayID);
            try {
                EventHubConfigurationDto config =
                        ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
                String serviceURLStr = config.getServiceUrl().concat(
                        APIConstants.GatewayNotification.GATEWAY_NOTIFICATION_ENDPOINT);
                URL url = new URL(serviceURLStr);

                HttpClient httpClient = APIUtil.getHttpClient(url.getPort(), url.getProtocol());

                HttpPost request = new HttpPost(serviceURLStr);
                request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                                  APIConstants.AUTHORIZATION_BASIC + new String(Base64.encodeBase64(
                                          (config.getUsername() + APIConstants.DELEM_COLON
                                                  + config.getPassword()).getBytes(StandardCharsets.UTF_8)),
                                                                                StandardCharsets.UTF_8));
                request.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                request.setEntity(new StringEntity(heartbeatPayload, ContentType.APPLICATION_JSON));
                HttpResponse response = httpClient.execute(request);

                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                log.debug("/notify-gateway called. Status: " + statusCode + ", Response: " + responseBody);
                if (statusCode != HttpStatus.SC_OK) {
                    log.error("Failed to send heartbeat notification. Status: " + statusCode + ", Response: "
                                      + responseBody);

                }

            } catch (IOException e) {
                log.error("Error occurred while executing Gateway HealthCheck/Registration logic", e);
            }
        }
    }

}

