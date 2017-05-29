package org.wso2.carbon.apimgt.core.configuration.models;
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

import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold DataPublisher configurations
 */
public class DataPublisherConfigurations {

    @Element(description = "Reciever URL")
    private String receiverURL = "tcp://localhost:7611";
    @Element(description = "Data publisher credentials")
    private CredentialConfigurations dataPublisherCredentials = new CredentialConfigurations();

    public String getReceiverURL() {
        return receiverURL;
    }

    public CredentialConfigurations getDataPublisherCredentials() {
        return dataPublisherCredentials;
    }
}
