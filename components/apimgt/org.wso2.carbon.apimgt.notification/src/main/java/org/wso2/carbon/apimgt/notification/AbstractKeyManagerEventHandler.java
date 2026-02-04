/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.notification;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.keymgt.ExpiredJWTCleaner;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerEventHandler;
import org.wso2.carbon.apimgt.impl.publishers.RevocationRequestPublisher;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.notification.event.APIKeyUsageEvent;
import org.wso2.carbon.apimgt.notification.event.ConsumerAppRevocationEvent;
import org.wso2.carbon.apimgt.notification.event.Event;
import org.wso2.carbon.apimgt.notification.event.SubjectEntityRevocationEvent;
import org.wso2.carbon.apimgt.notification.event.TokenRevocationEvent;
import org.wso2.carbon.apimgt.notification.internal.ServiceReferenceHolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

/**
 *  Abstract Implementation of KeyManagerEventHandler.
 */
public abstract class AbstractKeyManagerEventHandler implements KeyManagerEventHandler {

    private RevocationRequestPublisher revocationRequestPublisher;

    public AbstractKeyManagerEventHandler() {

        revocationRequestPublisher = RevocationRequestPublisher.getInstance();
    }

    public boolean handleTokenRevocationEvent(TokenRevocationEvent tokenRevocationEvent) throws APIManagementException {

        Properties properties = getRevocationEventProperties(tokenRevocationEvent);
        properties.setProperty(APIConstants.NotificationEvent.EXPIRY_TIME,
                Long.toString(tokenRevocationEvent.getExpiryTime()));
        properties.put(APIConstants.NotificationEvent.CONSUMER_KEY, tokenRevocationEvent.getConsumerKey());
        if (StringUtils.isBlank(tokenRevocationEvent.getTokenType())) {
            tokenRevocationEvent.setTokenType(APIConstants.NotificationEvent.APPLICATION_TOKEN_TYPE_OAUTH2);
        }
        properties.put(APIConstants.NotificationEvent.TOKEN_TYPE, tokenRevocationEvent.getTokenType());
        ApiMgtDAO.getInstance().addRevokedJWTSignature(tokenRevocationEvent.getEventId(),
                tokenRevocationEvent.getAccessToken(), tokenRevocationEvent.getTokenType(),
                tokenRevocationEvent.getExpiryTime(), tokenRevocationEvent.getTenantId());
        Application application = ApiMgtDAO.getInstance()
                .getApplicationByClientId(tokenRevocationEvent.getConsumerKey());
        if (application != null) {
            String orgId = application.getOrganization();
            properties.put(APIConstants.NotificationEvent.ORG_ID, orgId);
        }
        revocationRequestPublisher.publishRevocationEvents(tokenRevocationEvent.getAccessToken(), properties);
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        String isRevokeTokenCleanupEnabled = config.getFirstProperty(APIConstants.ENABLE_REVOKE_TOKEN_CLEANUP);
        if (Boolean.parseBoolean(isRevokeTokenCleanupEnabled)) {
            // Cleanup expired revoked tokens from db.
            Runnable expiredJWTCleaner = new ExpiredJWTCleaner();
            Thread cleanupThread = new Thread(expiredJWTCleaner);
            cleanupThread.start();
        }
        return true;
    }

    public boolean handleApiKeyUsageEvent(APIKeyUsageEvent apiKeyUsageEvent) throws APIManagementException {

        Properties properties = apiKeyUsageEventProperties(apiKeyUsageEvent);
        properties.setProperty(APIConstants.NotificationEvent.EXPIRY_TIME,
                Long.toString(apiKeyUsageEvent.getExpiryTime()));
        properties.put(APIConstants.NotificationEvent.API_KEY, apiKeyUsageEvent.getApiKey());
        properties.put(APIConstants.NotificationEvent.LAST_USED_TIME, apiKeyUsageEvent.getLastUsedTime());
        generateApiKeyHashFromPlainKeyAndUpdateLastUsedTime(apiKeyUsageEvent.getApiKey(),
                formattedTime(apiKeyUsageEvent.getLastUsedTime()));
        return true;
    }

    private void generateApiKeyHashFromPlainKeyAndUpdateLastUsedTime(String apiKey, String lastUsedTime)
            throws APIManagementException {

        List<APIKeyInfo> records = ApiMgtDAO.getInstance().getAllAPIKeys();

        try {
            for (APIKeyInfo record : records) {

                String salt = extractSalt(record.getProperties());
                String hash = APIUtil.sha256HashWithSalt(apiKey, salt.getBytes(StandardCharsets.UTF_8));

                if (hash.equals(record.getApiKeyHash())) {
                    ApiMgtDAO.getInstance().updateAPIKeyUsage(record.getApiKeyHash(), lastUsedTime);
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new APIManagementException("Error while generating API key hash from the plain key:", e);
        }
    }

    private String extractSalt(byte[] propertiesBlob) throws IOException, ClassNotFoundException {

        try (ObjectInputStream ois =
                     new ObjectInputStream(new ByteArrayInputStream(propertiesBlob))) {
            Properties props = (Properties) ois.readObject();
            return props.getProperty("salt");
        }
    }

    public boolean handleConsumerAppRevocationEvent(ConsumerAppRevocationEvent consumerKeyEvent)
            throws APIManagementException {

        ApiMgtDAO.getInstance().addRevokedConsumerKey(consumerKeyEvent.getConsumerKey(),
                consumerKeyEvent.getRevocationTime(), consumerKeyEvent.getTenantDomain());
        // set properties
        Properties properties = getRevocationEventProperties(consumerKeyEvent);
        properties.setProperty(APIConstants.NotificationEvent.CONSUMER_KEY, consumerKeyEvent.getConsumerKey());
        properties.setProperty(APIConstants.NotificationEvent.REVOCATION_TIME,
                Long.toString(consumerKeyEvent.getRevocationTime()));

        // publish event
        revocationRequestPublisher.publishRevocationEvents(consumerKeyEvent.getConsumerKey(), properties);
        return true;
    }

    public void handleSubjectEntityRevocationEvent(SubjectEntityRevocationEvent userEvent)
            throws APIManagementException {

        ApiMgtDAO.getInstance().addRevokedSubjectEntity(userEvent.getEntityId(), userEvent.getEntityType(),
                userEvent.getRevocationTime(), userEvent.getTenantDomain());
        // set properties
        Properties properties = getRevocationEventProperties(userEvent);
        properties.setProperty(APIConstants.NotificationEvent.ENTITY_ID, userEvent.getEntityId());
        properties.setProperty(APIConstants.NotificationEvent.ENTITY_TYPE, userEvent.getEntityType());
        properties.setProperty(APIConstants.NotificationEvent.REVOCATION_TIME,
                Long.toString(userEvent.getRevocationTime()));

        // publish event
        revocationRequestPublisher.publishRevocationEvents(userEvent.getEntityId(), properties);
    }

    private Properties getRevocationEventProperties(Event event) {

        Properties properties = new Properties();
        properties.setProperty(APIConstants.NotificationEvent.EVENT_ID, event.getEventId());
        properties.put(APIConstants.NotificationEvent.TENANT_ID, event.getTenantId());
        properties.setProperty(APIConstants.NotificationEvent.TENANT_DOMAIN, event.getTenantDomain());
        properties.setProperty(APIConstants.NotificationEvent.EVENT_TYPE, event.getType());
        properties.setProperty(APIConstants.NotificationEvent.STREAM_ID,
                APIConstants.TOKEN_REVOCATION_STREAM_ID);
        return properties;
    }

    private Properties apiKeyUsageEventProperties(Event event) {

        Properties properties = new Properties();
        properties.setProperty(APIConstants.NotificationEvent.EVENT_ID, event.getEventId());
        properties.put(APIConstants.NotificationEvent.TENANT_ID, event.getTenantId());
        properties.setProperty(APIConstants.NotificationEvent.TENANT_DOMAIN, event.getTenantDomain());
        properties.setProperty(APIConstants.NotificationEvent.EVENT_TYPE, event.getType());
        properties.setProperty(APIConstants.NotificationEvent.STREAM_ID,
                APIConstants.API_KEY_USAGE_STREAM_ID);
        return properties;
    }

    private String formattedTime(long time) {
        String formattedTime = Instant.ofEpochMilli(time)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return formattedTime;
    }
}
