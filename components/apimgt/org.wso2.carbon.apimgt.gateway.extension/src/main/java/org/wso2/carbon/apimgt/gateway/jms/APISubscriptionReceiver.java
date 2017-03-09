/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.gateway.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.andes.url.URLSyntaxException;
import org.wso2.carbon.apimgt.gateway.exception.APISubscriptionValidationException;

import javax.jms.JMSException;
import javax.jms.TopicSubscriber;
import javax.naming.NamingException;

/**
 * JMS receiver thread, try to fetch message from JMS topic periodic
 */
public class APISubscriptionReceiver extends Thread {
    private static final Logger log = LoggerFactory.getLogger(JmsReceiver.class);

    private APISubscriptionMessageJMSListener sus;
    private TopicSubscriber subscriber;

    @Override
    public void run() {
        sus = new APISubscriptionMessageJMSListener();
        try {
            subscriber = sus.subscribe();
        } catch (NamingException | JMSException | URLSyntaxException e) {
            log.error("Unable to subscribe to the topic", e);
            return;
        }

        while (true) {
            try {
                sus.receive(subscriber);
            } catch (APISubscriptionValidationException e) {
                log.error("Error occurred while receiving subscription message.", e);
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }

    }
}
