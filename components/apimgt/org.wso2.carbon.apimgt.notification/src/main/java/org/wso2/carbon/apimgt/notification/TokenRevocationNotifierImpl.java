/*
 *Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.apimgt.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.axis2.util.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.wso2.carbon.apimgt.eventing.EventPublisherEvent;
import org.wso2.carbon.apimgt.eventing.EventPublisherType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.token.TokenRevocationNotifier;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Implemented class for TokenRevocationNotifier interface
 * SendMessageOnRealtime() method is implemented
 * SendMessageToPersistentStorage() method is implemented
 */
public class TokenRevocationNotifierImpl implements TokenRevocationNotifier {

    private static final Log log = LogFactory.getLog(TokenRevocationNotifierImpl.class);
    protected static final String DEFAULT_TTL = "3600";
    protected Properties realTimeNotifierProperties;

    /**
     * Method to publish the revoked token on to the realtime message broker
     *
     * @param revokedToken requested revoked token JTI, user id or consumer key
     * @param properties realtime notifier properties read from the config
     */
    @Override
    public void sendMessageOnRealtime(String revokedToken, Properties properties) {

        if (APIConstants.NotificationEvent.CONSUMER_APP_REVOCATION_EVENT.equals(properties
                .getProperty(APIConstants.NotificationEvent.EVENT_TYPE))) {
            Map<String, String> consumerKeyRevocationMap = new HashMap<>();
            consumerKeyRevocationMap.put(APIConstants.NotificationEvent.CONSUMER_KEY,
                    properties.getProperty(APIConstants.NotificationEvent.CONSUMER_KEY));
            consumerKeyRevocationMap.put(APIConstants.NotificationEvent.REVOCATION_TIME,
                    properties.getProperty(APIConstants.NotificationEvent.REVOCATION_TIME));
            revokedToken = base64Encode(consumerKeyRevocationMap);
        } else if (APIConstants.NotificationEvent.SUBJECT_ENTITY_REVOCATION_EVENT.equals(properties
                .getProperty(APIConstants.NotificationEvent.EVENT_TYPE))) {
            Map<String, String> subjectEntityRevokeMap = new HashMap<>();
            subjectEntityRevokeMap.put(APIConstants.NotificationEvent.ENTITY_ID,
                    properties.getProperty(APIConstants.NotificationEvent.ENTITY_ID));
            subjectEntityRevokeMap.put(APIConstants.NotificationEvent.ENTITY_TYPE,
                    properties.getProperty(APIConstants.NotificationEvent.ENTITY_TYPE));
            subjectEntityRevokeMap.put(APIConstants.NotificationEvent.REVOCATION_TIME,
                    properties.getProperty(APIConstants.NotificationEvent.REVOCATION_TIME));
            revokedToken = base64Encode(subjectEntityRevokeMap);
        }
        sendRevokedTokenOnRealtime(revokedToken, properties);
    }

    private String base64Encode(Map<String, String> revokedTokenMap) {

        String encodedRevokedToken = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String revokedTokenJson = objectMapper.writeValueAsString(revokedTokenMap);
            byte[] encodedRevokedTokenStream =
                    Base64.encodeBase64(revokedTokenJson.getBytes(StandardCharsets.UTF_8));
            encodedRevokedToken = new String(encodedRevokedTokenStream, StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            log.error("Error while encoding revoked token event for consumer key revocation event.");
        }
        return encodedRevokedToken;
    }

    private void sendRevokedTokenOnRealtime(String revokedToken, Properties properties) {
        //Variables related to Realtime Notifier
        String type = null;
        long expiryTimeForJWT = 0L;
        if (properties.getProperty(APIConstants.NotificationEvent.EXPIRY_TIME) != null) {
            expiryTimeForJWT = Long.parseLong(properties.getProperty(APIConstants.NotificationEvent.EXPIRY_TIME));
        }
        String realtimeNotifierTTL = realTimeNotifierProperties.getProperty("ttl", DEFAULT_TTL);
        String eventId = properties.getProperty(APIConstants.NotificationEvent.EVENT_ID);
        if (APIConstants.NotificationEvent.TOKEN_REVOCATION_EVENT.equals(
                properties.getProperty(APIConstants.NotificationEvent.EVENT_TYPE))) {
            type = properties.getProperty(APIConstants.NotificationEvent.TOKEN_TYPE);
        } else {
            type = properties.getProperty(APIConstants.NotificationEvent.EVENT_TYPE);
        }
        String orgId = properties.getProperty(APIConstants.NotificationEvent.ORG_ID);
        int tenantId = (int) properties.get(APIConstants.NotificationEvent.TENANT_ID);
        Object[] objects =
                new Object[]{eventId, revokedToken, realtimeNotifierTTL, expiryTimeForJWT, type, tenantId};
        EventPublisherEvent tokenRevocationEvent = new EventPublisherEvent(APIConstants.TOKEN_REVOCATION_STREAM_ID,
                System.currentTimeMillis(), objects);
        tokenRevocationEvent.setOrgId(orgId);
        APIUtil.publishEvent(EventPublisherType.TOKEN_REVOCATION, tokenRevocationEvent,
                tokenRevocationEvent.toString());
    }

    /**
     * Method to send the revoked token to the persistent storage
     *
     * @param revokedToken token to be revoked, user id or consumer key
     * @param properties persistent notifier properties read from the config
     */
    @Override
    public void sendMessageToPersistentStorage(String revokedToken, Properties properties) {

        //Variables related to Persistent Notifier
        String defaultPersistentNotifierHostname = "https://localhost:2379/v2/keys/jti/";
        String persistentNotifierHostname = properties
                .getProperty("hostname", defaultPersistentNotifierHostname);
        String defaultPersistentNotifierUsername = "root";
        String persistentNotifierUsername = properties
                .getProperty("username", defaultPersistentNotifierUsername);
        String defaultPersistentNotifierPassword = "root";
        String persistentNotifierPassword = properties
                .getProperty("password", defaultPersistentNotifierPassword);
        String etcdEndpoint = persistentNotifierHostname + revokedToken;
        URL etcdEndpointURL = new URL(etcdEndpoint);
        String etcdEndpointProtocol = etcdEndpointURL.getProtocol();
        int etcdEndpointPort = etcdEndpointURL.getPort();
        HttpClient etcdEPClient = APIUtil.getHttpClient(etcdEndpointPort, etcdEndpointProtocol);
        HttpPut httpETCDPut = new HttpPut(etcdEndpoint);
        byte[] encodedAuth = Base64.encodeBase64((persistentNotifierUsername + ":" + persistentNotifierPassword).
                getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
        httpETCDPut.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        List<NameValuePair> etcdParams = getEtcdParams(properties);

        //Send the revoked token to the persistent storage Server
        httpETCDPut.setEntity(new UrlEncodedFormEntity(etcdParams, StandardCharsets.UTF_8));
        HttpResponse etcdResponse;
        try {
            etcdResponse = etcdEPClient.execute(httpETCDPut);
            if (etcdResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                    || etcdResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully submitted the request for revoked token. HTTP status :" + etcdResponse
                            .getStatusLine().getStatusCode());
                }
            } else {
                log.error("Sending revoked token to persistent storage failed. HTTP error code : " + etcdResponse
                        .getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            log.error("Error while sending revoked token to the persistent storage :", e);
        }
    }

    @NotNull
    private List<NameValuePair> getEtcdParams(Properties properties) {

        List<NameValuePair> etcdParams = new ArrayList<>();
        etcdParams.add(new BasicNameValuePair("value", "true"));
        etcdParams.add(new BasicNameValuePair("ttl", properties.getProperty("ttl", DEFAULT_TTL)));
        if (APIConstants.NotificationEvent.TOKEN_REVOCATION_EVENT.equals(properties
                .getProperty(APIConstants.NotificationEvent.EVENT_TYPE))) {
            etcdParams.add(new BasicNameValuePair(APIConstants.NotificationEvent.EXPIRY_TIME,
                    properties.getProperty(APIConstants.NotificationEvent.EXPIRY_TIME)));
        } else if (APIConstants.NotificationEvent.CONSUMER_APP_REVOCATION_EVENT.equals(properties
                .getProperty(APIConstants.NotificationEvent.EVENT_TYPE))) {
            etcdParams.add(new BasicNameValuePair(APIConstants.NotificationEvent.CONSUMER_KEY,
                    properties.getProperty(APIConstants.NotificationEvent.CONSUMER_KEY)));
            etcdParams.add(new BasicNameValuePair(APIConstants.NotificationEvent.REVOCATION_TIME,
                    properties.getProperty(APIConstants.NotificationEvent.REVOCATION_TIME)));
        } else if (APIConstants.NotificationEvent.SUBJECT_ENTITY_REVOCATION_EVENT.equals(properties
                .getProperty(APIConstants.NotificationEvent.EVENT_TYPE))) {
            etcdParams.add(new BasicNameValuePair(APIConstants.NotificationEvent.ENTITY_ID,
                    properties.getProperty(APIConstants.NotificationEvent.ENTITY_ID)));
            etcdParams.add(new BasicNameValuePair(APIConstants.NotificationEvent.ENTITY_TYPE,
                    properties.getProperty(APIConstants.NotificationEvent.ENTITY_TYPE)));
            etcdParams.add(new BasicNameValuePair(APIConstants.NotificationEvent.REVOCATION_TIME,
                    properties.getProperty(APIConstants.NotificationEvent.REVOCATION_TIME)));
        }
        return etcdParams;
    }

    @Override
    public void init(Properties realTimeNotifierProperties, Properties persistentNotifierProperties) {

        this.realTimeNotifierProperties = realTimeNotifierProperties;
    }

}
