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

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Class to hold JMS connection details
 */
@Configuration(description = "JMS Connection configurations")
public class JMSConnectionConfiguration {

    @Element(description = "Java naming factory initial")
    private String javaNamingFactoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    @Element(description = "JMS Connectiontion factory JNDI name")
    private String jmsConnectionFactoryJNDIName = "TopicConnectionFactory";
    @Element(description = "Topic connection factory URL")
    private String topicConnectionFactoryURL = "tcp://localhost:5672";
    @Element(description = "Destination topic name")
    private String topic = "myTopic";
    @Element(description = "Broker connection username")
    private String username = "admin";
    @Element(description = "Broker connection password")
    private String password = "admin";

    public String getJavaNamingFactoryInitial() {
        return javaNamingFactoryInitial;
    }

    public void setJavaNamingFactoryInitial(String javaNamingFactoryInitial) {
        this.javaNamingFactoryInitial = javaNamingFactoryInitial;
    }

    public String getJmsConnectionFactoryJNDIName() {
        return jmsConnectionFactoryJNDIName;
    }

    public void setJmsConnectionFactoryJNDIName(String jmsConnectionFactoryJNDIName) {
        this.jmsConnectionFactoryJNDIName = jmsConnectionFactoryJNDIName;
    }

    public String getTopicConnectionFactoryURL() {
        return topicConnectionFactoryURL;
    }

    public void setTopicConnectionFactoryURL(String topicConnectionFactoryURL) {
        this.topicConnectionFactoryURL = topicConnectionFactoryURL;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
