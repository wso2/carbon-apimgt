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
 * Class to hold Throttling configurations
 */
@Configuration(description = "Throttling configurations")
public class ThrottlingConfigurations {

    @Element(description = "Date Publisher configurations")
    private DataPublisherConfigurations dataPublisherConfigurations = new DataPublisherConfigurations();

    @Element(description = "Policy Deployer configurations")
    private PolicyDeployerConfiguration policyDeployerConfiguration = new PolicyDeployerConfiguration();

    @Element(description = "JMS Connection configurations")
    private JMSConnectionConfiguration jmsConnectionConfiguration = new JMSConnectionConfiguration();

    public DataPublisherConfigurations getDataPublisherConfigurations() {
        return dataPublisherConfigurations;
    }

    public void setDataPublisherConfigurations(DataPublisherConfigurations dataPublisherConfigurations) {
        this.dataPublisherConfigurations = dataPublisherConfigurations;
    }

    public PolicyDeployerConfiguration getPolicyDeployerConfiguration() {
        return policyDeployerConfiguration;
    }

    public void setPolicyDeployerConfiguration(PolicyDeployerConfiguration policyDeployerConfiguration) {
        this.policyDeployerConfiguration = policyDeployerConfiguration;
    }

    public JMSConnectionConfiguration getJmsConnectionConfiguration() {
        return jmsConnectionConfiguration;
    }

    public void setJmsConnectionConfiguration(JMSConnectionConfiguration jmsConnectionConfiguration) {
        this.jmsConnectionConfiguration = jmsConnectionConfiguration;
    }
}
