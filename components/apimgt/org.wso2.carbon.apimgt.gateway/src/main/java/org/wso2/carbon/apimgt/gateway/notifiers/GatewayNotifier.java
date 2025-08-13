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

import com.google.gson.Gson;
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
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIConstants.GatewayNotification.GatewayRegistrationResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final ScheduledExecutorService heartbeatScheduler;
    private final ScheduledExecutorService registrationExecutor;
    private final Gson gson = new Gson();
    private final int maxRetryCount;
    private final double retryProgressionFactor;
    private final long retryDuration;
    private final String ipAddress;
    private final List<String> environmentLabels;
    private final List<String> loadingTenants;
    private int notifyIntervalSeconds;
    private boolean gatewayNotificationEnabled;
    private String gatewayID;

    /**
     * Initializes the scheduler, loads configuration settings, and sets up heartbeat parameters
     */
    private GatewayNotifier() {
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        registrationExecutor = Executors.newSingleThreadScheduledExecutor();
        gatewayNotificationConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getGatewayNotificationConfiguration();
        gatewayArtifactSynchronizerProperties = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration()
                .getGatewayArtifactSynchronizerProperties();

        gatewayNotificationEnabled = gatewayNotificationConfiguration.isEnabled();
        heartbeatConfiguration
                = gatewayNotificationConfiguration.getHeartbeat();
        notifyIntervalSeconds = heartbeatConfiguration.getNotifyIntervalSeconds();
        registrationConfiguration = gatewayNotificationConfiguration.getRegistration();
        maxRetryCount = registrationConfiguration.getMaxRetryCount();
        retryProgressionFactor = registrationConfiguration.getRetryProgressionFactor();
        retryDuration = registrationConfiguration.getRetryDuration();

        gatewayID = gatewayNotificationConfiguration.getGatewayID();

        if (StringUtils.isEmpty(gatewayID)) {
            gatewayID = UUID.randomUUID().toString();
        }
        DataHolder.getInstance().setGatewayID(gatewayID);

        ipAddress = getLocalIPAddress();

        environmentLabels = new ArrayList<>(gatewayArtifactSynchronizerProperties.getGatewayLabels());

        if (!gatewayArtifactSynchronizerProperties.isTenantLoading()) {
            loadingTenants = List.of(APIConstants.GatewayNotification.WSO2_ALL_TENANTS);
        } else {
            loadingTenants = gatewayArtifactSynchronizerProperties.getLoadingTenants().getIncludingTenants();
        }
    }

    public static synchronized GatewayNotifier getInstance() {
        if (instance == null) {
            synchronized (GatewayNotifier.class) {
                if (instance == null) {
                    instance = new GatewayNotifier();
                }
            }
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
        heartbeatScheduler.scheduleAtFixedRate(new Heartbeat(), 30, notifyIntervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * Stops the gateway health monitoring system.
     */
    public void stopHeartbeat() {
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdown();
            try {
                if (!heartbeatScheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    heartbeatScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatScheduler.shutdownNow();
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

    private class GatewayRegistrationWorker implements Runnable {
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Registering Gateway with ID: " + gatewayID);
            }
            
            GatewayProperties gatewayProperties = new GatewayProperties(ipAddress);
            GatewayRegistrationPayload payload = new GatewayRegistrationPayload(
                    APIConstants.GatewayNotification.PAYLOAD_TYPE_REGISTER,
                    gatewayID,
                    Instant.now().toEpochMilli(),
                    gatewayProperties,
                    environmentLabels,
                    loadingTenants
            );
            String registrationPayload = gson.toJson(payload);

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
                DataHolder.setGatewayRegistrationResponse(GatewayRegistrationResponse.NOT_RESPONDED);

                try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(request, httpClient,
                                                                                            retryDuration,
                                                                                            maxRetryCount,
                                                                                            retryProgressionFactor)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    String status = null;
                    try {
                        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                        status = jsonObject.get("status").getAsString();
                    } catch (RuntimeException ex) {
                        log.error("Invalid registration response payload: " + responseBody, ex);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("/notify-gateway called. Status: " + statusCode + ", Response: " + responseBody);
                    }

                    if (APIConstants.GatewayNotification.STATUS_REGISTERED.equals(status)) {
                        DataHolder.setGatewayRegistrationResponse(
                                GatewayRegistrationResponse.REGISTERED);
                        log.info("Gateway registered successfully with ID: " + gatewayID);
                    } else if (APIConstants.GatewayNotification.STATUS_ACKNOWLEDGED.equals(status)) {
                        DataHolder.setGatewayRegistrationResponse(GatewayRegistrationResponse.ACKNOWLEDGED);
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
            GatewayHeartbeatPayload payload = new GatewayHeartbeatPayload(
                    APIConstants.GatewayNotification.PAYLOAD_TYPE_HEARTBEAT,
                    gatewayID,
                    Instant.now().toEpochMilli(),
                    loadingTenants
            );
            String heartbeatPayload = gson.toJson(payload);
            
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
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (log.isDebugEnabled()) {
                    log.debug("/notify-gateway called. Status: " + statusCode + ", Response: " + responseBody);
                }
                if (statusCode != HttpStatus.SC_OK) {
                    log.error("Failed to send heartbeat notification. Status: " + statusCode + ", Response: "
                                      + responseBody);
                }
            } catch (IOException e) {
                log.error("Error occurred while executing Gateway Heartbeat notifier", e);
            }
        }
    }
    /**
     * Retrieves the local IP address of the gateway.
     *
     * @return The local IP address as a string, or "unknown" if it cannot be determined.
     */
    private String getLocalIPAddress() {
        try {
            byte[] addressBytes = Objects.requireNonNull(APIUtil.getLocalAddress()).getAddress();
            return Objects.requireNonNull(InetAddress.getByAddress(addressBytes).getHostAddress());
        } catch (UnknownHostException | NullPointerException e) {
            log.error("Failed to get IP address", e);
            return "unknown";
        }
    }

    /**
     * Represents the gateway properties structure for GSON serialization.
     */
    private static class GatewayProperties {
        private final String ipAddress;

        GatewayProperties(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }

    /**
     * Represents the gateway registration payload structure for GSON serialization.
     */
    private static class GatewayRegistrationPayload {
        private final String payloadType;
        private final String gatewayId;
        private final Long timeStamp;
        private final GatewayProperties gatewayProperties;
        private final List<String> environmentLabels;
        private final List<String> loadingTenants;

        GatewayRegistrationPayload(String payloadType, String gatewayId, Long timeStamp,
                                   GatewayProperties gatewayProperties, List<String> environmentLabels,
                                   List<String> loadingTenants) {
            this.payloadType = payloadType;
            this.gatewayId = gatewayId;
            this.timeStamp = timeStamp;
            this.gatewayProperties = gatewayProperties;
            this.environmentLabels = environmentLabels;
            this.loadingTenants = loadingTenants;
        }
    }

    /**
     * Represents the gateway heartbeat payload structure for GSON serialization.
     */
    private static class GatewayHeartbeatPayload {
        private final String payloadType;
        private final String gatewayId;
        private final Long timeStamp;
        private final List<String> loadingTenants;

        GatewayHeartbeatPayload(String payloadType, String gatewayId, Long timeStamp, List<String> loadingTenants) {
            this.payloadType = payloadType;
            this.gatewayId = gatewayId;
            this.timeStamp = timeStamp;
            this.loadingTenants = loadingTenants;
        }
    }

}

