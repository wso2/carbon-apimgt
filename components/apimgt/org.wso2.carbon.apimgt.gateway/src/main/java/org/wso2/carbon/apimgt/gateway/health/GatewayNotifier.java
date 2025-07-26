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
package org.wso2.carbon.apimgt.gateway.health;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for gateway health monitoring, including scheduling
 * heartbeat notifications and handling gateway registration/re-registration.
 */
public class GatewayNotifier {
    private static final Log log = LogFactory.getLog(GatewayNotifier.class);
    private static int notifyIntervalSeconds = 30; // Default, will be overridden by config
    private static boolean gatewayNotificationEnabled = true;
    private static String gatewayID = ""; // Start with empty, will be set at runtime or config
    private static GatewayNotifier instance;
    private final GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;
    private final GatewayNotificationConfiguration gatewayNotificationConfiguration;
    private final GatewayNotificationConfiguration.HeartbeatConfiguration heartbeatConfiguration;
    private final GatewayNotificationConfiguration.RegistrationConfiguration registrationConfiguration;
    private final ScheduledExecutorService scheduler;
    int maxRetryCount;
    double retryProgressionFactor;
    long retryDuration;

    /**
     * Constructs a new GatewayHealthMonitor instance.
     * Initializes the scheduler, loads configuration settings, and sets up heartbeat parameters
     */
    public GatewayNotifier() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        gatewayNotificationConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getGatewayNotificationConfiguration();
        gatewayArtifactSynchronizerProperties = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration()
                .getGatewayArtifactSynchronizerProperties();

        gatewayNotificationEnabled = gatewayNotificationConfiguration.isEnabled();
        heartbeatConfiguration = gatewayNotificationConfiguration.getHeartbeat();
        notifyIntervalSeconds = heartbeatConfiguration.getNotifyIntervalSeconds();
        if (heartbeatConfiguration.getGatewayID() != null && !heartbeatConfiguration.getGatewayID().isEmpty()) {
            gatewayID = heartbeatConfiguration.getGatewayID();
        }
        if (gatewayID == null || gatewayID.isEmpty()) {
            gatewayID = UUID.randomUUID().toString();
        }
        registrationConfiguration = gatewayNotificationConfiguration.getRegistration();
        maxRetryCount = registrationConfiguration.getMaxRetryCount();
        retryProgressionFactor = registrationConfiguration.getRetryProgressionFactor();
        retryDuration = registrationConfiguration.getRetryDuration();
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
        new Thread(new registerGateway()).start();
    }

    /**
     * Starts the gateway health monitoring system.
     * If heartbeat is disabled by configuration, this method returns immediately.
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
        if (log.isDebugEnabled()) {
            log.debug("Stopping Gateway Health Monitor");
        }

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
    }

    private String buildRegistrationPayload(String gatewayID) {
        long millis = Instant.now().toEpochMilli();
        if (gatewayID == null || gatewayID.isEmpty()) {
            gatewayID = UUID.randomUUID().toString();
        }
        String ipAddress = APIUtil.getHostAddress();

        com.google.gson.JsonObject node = new com.google.gson.JsonObject();
        node.addProperty(APIConstants.GatewayNotification.PAYLOAD_TYPE,
                         APIConstants.GatewayNotification.PAYLOAD_TYPE_REGISTER);
        node.addProperty(APIConstants.GatewayNotification.GATEWAY_ID, gatewayID);
        node.addProperty(APIConstants.GatewayNotification.TIMESTAMP, millis);

        com.google.gson.JsonObject gatewayProperties = new com.google.gson.JsonObject();
        gatewayProperties.addProperty("ipAddress", ipAddress);
        node.add(APIConstants.GatewayNotification.GATEWAY_PROPERTIES, gatewayProperties);

        com.google.gson.JsonArray environmentLabels = new com.google.gson.JsonArray();
        for (String label : gatewayArtifactSynchronizerProperties.getGatewayLabels()) {
            environmentLabels.add(label);
        }
        node.add(APIConstants.GatewayNotification.ENVIRONMENT_LABELS, environmentLabels);

        return node.toString();
    }

    private String buildHeartbeatPayload(String gatewayID) {
        long millis = java.time.Instant.now().toEpochMilli();
        com.google.gson.JsonObject node = new com.google.gson.JsonObject();
        node.addProperty(APIConstants.GatewayNotification.PAYLOAD_TYPE,
                         APIConstants.GatewayNotification.PAYLOAD_TYPE_HEARTBEAT);
        node.addProperty(APIConstants.GatewayNotification.GATEWAY_ID, gatewayID);
        node.addProperty(APIConstants.GatewayNotification.TIMESTAMP, millis);
        return node.toString();
    }

    /**
     * Registers the gateway with the API Manager.
     * This method sends a registration payload to the API Manager's notification endpoint.
     */
    private class registerGateway implements Runnable {
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Registering Gateway with ID: " + gatewayID);
            }
            String registrationPayload = buildRegistrationPayload(gatewayID);

            try {
                EventHubConfigurationDto config =
                        ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
                String serviceURLStr = config.getServiceUrl().concat(APIConstants.GatewayNotification.GATEWAY_NOTIFICATION_ENDPOINT);
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
                    String responseBody = new String(response.getEntity().getContent().readAllBytes(),
                                                     StandardCharsets.UTF_8);

                    if ("REGISTERED".equals(responseBody.trim())) {
                        DataHolder.setGatewayRegistrationResponse(DataHolder.GatewayRegistrationResponse.REGISTERED);
                    } else if ("ACKNOWLEDGED".equals(responseBody.trim())) {
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
            if (log.isDebugEnabled()) {
                log.debug("Executing Gateway heartbeat");
            }
            String heartbeatPayload = buildHeartbeatPayload(gatewayID);
            try {
                EventHubConfigurationDto config =
                        ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
                String serviceURLStr = config.getServiceUrl().concat(APIConstants.GatewayNotification.GATEWAY_NOTIFICATION_ENDPOINT);
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
                if (log.isDebugEnabled()) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = new String(response.getEntity().getContent().readAllBytes(),
                                                  StandardCharsets.UTF_8);
                    log.debug("/notify-gateway called. Status: " + statusCode + ", Response: " + responseBody);
                }
            } catch (IOException e) {
                log.error("Error occurred while executing Gateway HealthCheck/Registration logic", e);
            }
        }
    }

}

