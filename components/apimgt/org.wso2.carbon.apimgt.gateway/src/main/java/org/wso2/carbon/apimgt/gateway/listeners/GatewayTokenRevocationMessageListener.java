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

package org.wso2.carbon.apimgt.gateway.listeners;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

public class GatewayTokenRevocationMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(GatewayTokenRevocationMessageListener.class);

    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) message;
                    Map<String, Object> map = new HashMap<String, Object>();
                    Enumeration enumeration = mapMessage.getMapNames();
                    while (enumeration.hasMoreElements()) {
                        String key = (String) enumeration.nextElement();
                        map.put(key, mapMessage.getObject(key));
                    }
                    if (APIConstants.TopicNames.TOPIC_TOKEN_REVOCATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (map.get(APIConstants.REVOKED_TOKEN_KEY) !=
                                null) {
                            /*
                             * This message contains revoked token data
                             * revokedToken - Revoked Token which should be removed from the cache
                             * expiryTime - ExpiryTime of the token if token is JWT, otherwise expiry is set to 0
                             */
                            handleRevokedTokenMessage((String) map.get(APIConstants.REVOKED_TOKEN_KEY),
                                    (Long) map.get(APIConstants.REVOKED_TOKEN_EXPIRY_TIME));
                        }

                    }
                } else {
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException e) {
            log.error("JMSException occurred when processing the received message ", e);
        }
    }

    private void handleRevokedTokenMessage(String revokedToken, long expiryTime) {

        boolean isJwtToken = false;
        if (StringUtils.isEmpty(revokedToken)) {
            return;
        }

        //handle JWT tokens
        if (revokedToken.contains(APIConstants.DOT) && APIUtil.isValidJWT(revokedToken)) {
            revokedToken = APIUtil.getSignatureIfJWT(revokedToken); //JWT signature is the cache key
            ServiceReferenceHolder.getInstance().getRevokedTokenService()
                    .addRevokedJWTIntoMap(revokedToken, expiryTime);  // Add revoked
            // token to
            // revoked JWT map
            isJwtToken = true;
        }
        ServiceReferenceHolder.getInstance().getRevokedTokenService()
                .removeTokenFromGatewayCache(revokedToken, isJwtToken);
    }
}
