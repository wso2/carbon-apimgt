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
import org.wso2.carbon.apimgt.eventing.EventPublisherEvent;
import org.wso2.carbon.apimgt.eventing.EventPublisherType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.keymgt.ExpiredJWTCleaner;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerEventHandler;
import org.wso2.carbon.apimgt.impl.publishers.RevocationRequestPublisher;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
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
        ApiMgtDAO.getInstance().addRevokedJWTSignature(tokenRevocationEvent.getEventId(),
                tokenRevocationEvent.getAccessToken(), tokenRevocationEvent.getTokenType(),
                tokenRevocationEvent.getExpiryTime(), tokenRevocationEvent.getTenantId());
        Application application = ApiMgtDAO.getInstance()
                .getApplicationByClientId(tokenRevocationEvent.getConsumerKey());
        if (application != null) {
            String orgId = application.getOrganization();
            properties.put(APIConstants.NotificationEvent.ORG_ID, orgId);
        }

        revocationRequestPublisher.publishRevocationEvents(tokenRevocationEvent.getAccessToken(),
                tokenRevocationEvent.getExpiryTime(), properties);

        // Cleanup expired revoked tokens from db.
        Runnable expiredJWTCleaner = new ExpiredJWTCleaner();
        Thread cleanupThread = new Thread(expiredJWTCleaner);
        cleanupThread.start();
        return true;
    }

    public boolean handleInternalTokenRevocationByConsumerKeyEvent(InternalTokenRevocationConKeyEvent consumerKeyEvent)
            throws APIManagementException {

        ApiMgtDAO.getInstance().addRevokedConsumerKey(consumerKeyEvent.getConsumerKey(),
                consumerKeyEvent.getRevocationTime(), consumerKeyEvent.getOrganization());

        // TODO: check whether we need to implement RevocationRequestPublisher based mechanism to send events
        // realtime or persistent storage as done in revocationRequestPublisher.publishRevocationEvents() method
        // in handleTokenRevocationEvent()
        Object[] objects = new Object[]{
                consumerKeyEvent.getEventId(),
                consumerKeyEvent.getConsumerKey(),
                consumerKeyEvent.getRevocationTime(),
                consumerKeyEvent.getOrganization(),
                consumerKeyEvent.getType()
        };
        EventPublisherEvent tokenRevocationEvent = new EventPublisherEvent(
                APIConstants.TOKEN_REVOCATION_CONSUMER_KEY_EVENT_STREAM_ID,
                System.currentTimeMillis(), objects);
        APIUtil.publishEvent(EventPublisherType.TOKEN_REVOKE_BY_CONSUMER_KEY_EVENT, tokenRevocationEvent,
                tokenRevocationEvent.toString());

        return true;
    }

    public void handleInternalTokenRevocationByUserEvent(InternalTokenRevocationUserEvent internalTokenRevocationEvent)
            throws APIManagementException {
        ApiMgtDAO.getInstance().addRevokedRuleByUserEvent(internalTokenRevocationEvent.getSubjectId(),
                internalTokenRevocationEvent.getSubjectIdType(), internalTokenRevocationEvent.getRevocationTime(),
                internalTokenRevocationEvent.getOrganization());

        Object[] objects = new Object[]{internalTokenRevocationEvent.getEventId(),
                internalTokenRevocationEvent.getSubjectId(),
                internalTokenRevocationEvent.getSubjectIdType(),
                internalTokenRevocationEvent.getRevocationTime(),
                internalTokenRevocationEvent.getOrganization(),
                internalTokenRevocationEvent.getType()
        };
        EventPublisherEvent tokenRevocationEvent = new EventPublisherEvent(
                APIConstants.TOKEN_REVOCATION_USER_EVENT_STREAM_ID,
                System.currentTimeMillis(), objects);
        APIUtil.publishEvent(EventPublisherType.TOKEN_REVOKE_BY_USER_EVENT, tokenRevocationEvent,
                tokenRevocationEvent.toString());
    }
}
