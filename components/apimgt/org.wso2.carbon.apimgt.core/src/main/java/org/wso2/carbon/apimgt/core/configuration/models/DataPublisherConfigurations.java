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
 * Class to hold DataPublisher configurations
 */
@Configuration(description = "Data Publisher connection configurations")
public class DataPublisherConfigurations {

    @Element(description = "Reciever URL")
    private String receiverURL = "tcp://localhost:9612";
    @Element(description = "Data publisher credentials")
    private CredentialConfigurations dataPublisherCredentials = new CredentialConfigurations();

    public String getReceiverURL() {
        return receiverURL;
    }

    public void setReceiverURL(String receiverURL) {
        this.receiverURL = receiverURL;
    }

    public CredentialConfigurations getDataPublisherCredentials() {
        return dataPublisherCredentials;
    }

    public void setDataPublisherCredentials(CredentialConfigurations dataPublisherCredentials) {
        this.dataPublisherCredentials = dataPublisherCredentials;
    }
}
