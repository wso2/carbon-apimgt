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

package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold Broker configurations
 */
@Configuration(description = "Broker configurations")
public class BrokerConfigurations {

    @Element(description = "JMS connection configuration")
    private JMSConnectionConfiguration jmsConnectionConfiguration = new JMSConnectionConfiguration();
    @Element(description = "Store topic name")
    private String storeTopic = "StoreTopic";
    @Element(description = "Publisher topic name")
    private String publisherTopic = "PublisherTopic";
    @Element(description = "Throttle topic name")
    private String throttleTopic = "ThrottleTopic";
    public JMSConnectionConfiguration getJmsConnectionConfiguration() {
        return jmsConnectionConfiguration;
    }

    public void setJmsConnectionConfiguration(JMSConnectionConfiguration jmsConnectionConfiguration) {
        this.jmsConnectionConfiguration = jmsConnectionConfiguration;
    }

    public String getStoreTopic() {
        return storeTopic;
    }

    public void setStoreTopic(String storeTopic) {
        this.storeTopic = storeTopic;
    }

    public String getPublisherTopic() {
        return publisherTopic;
    }

    public void setPublisherTopic(String publisherTopic) {
        this.publisherTopic = publisherTopic;
    }

    public String getThrottleTopic() {
        return throttleTopic;
    }

    public void setThrottleTopic(String throttleTopic) {
        this.throttleTopic = throttleTopic;
    }
}
