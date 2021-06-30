/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.handlers.DataPublisherUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import javax.cache.Cache;
import java.util.Arrays;
import java.util.Map;

/**
 * This @BotDetectionMediator mediator refers at the OpenService api
 * If invoked this OpenService api by a bot, it will detect this meditor
 * take headers, pseed body and client IP and log it in seperate log
 * If enabled alert through analytics, will triger an email to admins of the system
 */
public class BotDetectionMediator extends AbstractMediator {
    private static final Log log = LogFactory.getLog(BotDetectionMediator.class);
    private static final String BOT_ACCESS_COUNT_CACHE = "BOT_ACCESS_CACHE";
    private static final Cache botAccessCountCache = APIUtil
            .getCache(APIConstants.API_MANAGER_CACHE_MANAGER, BOT_ACCESS_COUNT_CACHE, 60, 60);
    private int throttleLimit = 2;

    /**
     * initiated a cache to keep the bot access count
     */
    public BotDetectionMediator() {
        super();
    }

    @Override
    public boolean mediate(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        String clientIP = DataPublisherUtil.getEndUserIP(messageContext);
        if (isThrottledOut(clientIP)) {
            messageContext.setProperty("BOT_THROTTLED_OUT", true);
            messageContext.setProperty("BOT_IP", clientIP);
            return true;
        }
        String messageBody;
        long currentTime = System.currentTimeMillis();
        String messageId = messageContext.getMessageID();
        String apiMethod = (String) msgContext.getProperty("HTTP_METHOD");
        try {
            RelayUtils.buildMessage(msgContext);
            SOAPEnvelope messageEnvelop = msgContext.getEnvelope();
            if (messageEnvelop != null && messageEnvelop.getBody() != null) {
                messageBody = String.valueOf(messageEnvelop.getBody());
            } else {
                messageBody = "Empty Message";
            }
        } catch (Exception e) {
            messageBody = "Malformed Message";
        }

        String headerSet = getPassedHeaderSet(msgContext);

        log.info(String.format(
                "MessageId : %s | Request Method : %s | Message Body : %s | client Ip : %s | " + "Headers set : %s",
                messageId, apiMethod, messageBody, clientIP, headerSet));
        return true;
    }

    /**
     * @param msgContext get message context to get the header set
     * @return passed header set
     */
    private String getPassedHeaderSet(org.apache.axis2.context.MessageContext msgContext) {
        Map headers = (Map) (msgContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Object[] headersArray = headers.entrySet().toArray();
        return Arrays.toString(headersArray);
    }

    /**
     * @param clientIp get to count the bot access
     * @return increse count and if throttle limit passed will trottle out
     */
    private boolean isThrottledOut(String clientIp) {
        synchronized ("BOT_DETECTION_".concat(clientIp).intern()) {
            int counter = 0;
            Object counterObject = botAccessCountCache.get(clientIp);
            if (counterObject != null) {
                counter = (int) counterObject;
                if (counter > throttleLimit) {
                    return true;
                }
            }
            botAccessCountCache.put(clientIp, ++counter);
            return false;
        }
    }
}
