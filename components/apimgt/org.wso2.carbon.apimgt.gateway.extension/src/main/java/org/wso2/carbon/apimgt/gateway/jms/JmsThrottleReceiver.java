package org.wso2.carbon.apimgt.gateway.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.andes.url.URLSyntaxException;

import javax.jms.JMSException;
import javax.jms.TopicSubscriber;
import javax.naming.NamingException;

/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * JMS receiver thread, try to fetch message from JMS topic periodic
 */
public class JmsThrottleReceiver extends Thread {
    private static final Logger log = LoggerFactory.getLogger(JmsReceiver.class);

    private ThrottleJMSListner listner;
    private TopicSubscriber subscriber;

    @Override
    public void run() {

        listner = new ThrottleJMSListner();
        try {
            subscriber = listner.subscribe();
        } catch (NamingException | JMSException | URLSyntaxException e) {
            log.error("Unable to subscribe to the topic", e);
        }

        while (true) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Starting to receive throttle messages..... ");
                }
                listner.receive(subscriber);
            } catch (NamingException | JMSException e) {
                log.error("Unable to subscribe to the topic", e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Unable to subscribe to the topic", e);
            }
        }

    }
}
