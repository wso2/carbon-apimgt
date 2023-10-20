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
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.keymgt.ExpiredJWTCleaner;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerEventHandler;
import org.wso2.carbon.apimgt.impl.publishers.RevocationRequestPublisher;
import org.wso2.carbon.apimgt.notification.event.InternalTokenRevocationConKeyEvent;
import org.wso2.carbon.apimgt.notification.event.InternalTokenRevocationUserEvent;
import org.wso2.carbon.apimgt.notification.event.TokenRevocationEvent;

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

        Properties properties = new Properties();
        properties.setProperty(APIConstants.NotificationEvent.EVENT_ID, tokenRevocationEvent.getEventId());
        properties.put(APIConstants.NotificationEvent.CONSUMER_KEY, tokenRevocationEvent.getConsumerKey());
        if (StringUtils.isBlank(tokenRevocationEvent.getTokenType())) {
            tokenRevocationEvent.setTokenType(APIConstants.NotificationEvent.APPLICATION_TOKEN_TYPE_OAUTH2);
        }
        properties.put(APIConstants.NotificationEvent.TOKEN_TYPE, tokenRevocationEvent.getTokenType());
        properties.put(APIConstants.NotificationEvent.TENANT_ID, tokenRevocationEvent.getTenantId());
        properties.put(APIConstants.NotificationEvent.TENANT_DOMAIN, tokenRevocationEvent.getTenantDomain());
        properties.put(APIConstants.NotificationEvent.STREAM_ID, APIConstants.TOKEN_REVOCATION_STREAM_ID);
        properties.setProperty(APIConstants.NotificationEvent.EXPIRY_TIME,
                Long.toString(tokenRevocationEvent.getExpiryTime()));
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

        // Cleanup expired revoked tokens from db.
        Runnable expiredJWTCleaner = new ExpiredJWTCleaner();
        Thread cleanupThread = new Thread(expiredJWTCleaner);
        cleanupThread.start();
        return true;
    }

    public boolean handleInternalTokenRevocationByConsumerKeyEvent(InternalTokenRevocationConKeyEvent consumerKeyEvent)
            throws APIManagementException {

        // rule persistence
        ApiMgtDAO.getInstance().addRevokedConsumerKey(consumerKeyEvent.getConsumerKey(),
                consumerKeyEvent.getRevocationTime(), consumerKeyEvent.getOrganization());

        // set properties
        Properties properties = new Properties();
        properties.setProperty(APIConstants.NotificationEvent.EVENT_ID, consumerKeyEvent.getEventId());
        properties.setProperty(APIConstants.NotificationEvent.CONSUMER_KEY, consumerKeyEvent.getConsumerKey());
        properties.setProperty(APIConstants.NotificationEvent.REVOCATION_TIME,
                Long.toString(consumerKeyEvent.getRevocationTime()));
        properties.setProperty(APIConstants.NotificationEvent.ORGANIZATION, consumerKeyEvent.getOrganization());
        properties.setProperty(APIConstants.NotificationEvent.EVENT_TYPE, consumerKeyEvent.getType());
        properties.setProperty(APIConstants.NotificationEvent.STREAM_ID,
                APIConstants.TOKEN_REVOCATION_CONSUMER_KEY_EVENT_STREAM_ID);

        // publish event
        revocationRequestPublisher.publishRevocationEvents(consumerKeyEvent.getConsumerKey(), properties);
        return true;
    }

    public void handleInternalTokenRevocationByUserEvent(InternalTokenRevocationUserEvent userEvent)
            throws APIManagementException {
        // persistence
        ApiMgtDAO.getInstance().addRevokedRuleByUserEvent(userEvent.getSubjectId(),
                userEvent.getSubjectIdType(), userEvent.getRevocationTime(),
                userEvent.getOrganization());

        // set properties
        Properties properties = new Properties();
        properties.setProperty(APIConstants.NotificationEvent.EVENT_ID, userEvent.getEventId());
        properties.setProperty(APIConstants.NotificationEvent.SUBJECT_ID, userEvent.getSubjectId());
        properties.setProperty(APIConstants.NotificationEvent.SUBJECT_ID_TYPE, userEvent.getSubjectIdType());
        properties.setProperty(APIConstants.NotificationEvent.REVOCATION_TIME,
                Long.toString(userEvent.getRevocationTime()));
        properties.setProperty(APIConstants.NotificationEvent.ORGANIZATION, userEvent.getOrganization());
        properties.setProperty(APIConstants.NotificationEvent.EVENT_TYPE, userEvent.getType());
        properties.setProperty(APIConstants.NotificationEvent.STREAM_ID,
                APIConstants.TOKEN_REVOCATION_USER_EVENT_STREAM_ID);

        // publish event
        revocationRequestPublisher.publishRevocationEvents(userEvent.getSubjectId(), properties);
    }
}
