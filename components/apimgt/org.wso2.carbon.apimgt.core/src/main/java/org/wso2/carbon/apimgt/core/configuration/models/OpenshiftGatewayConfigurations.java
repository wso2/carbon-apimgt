/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold Openshift Gateway configurations
 */
@Configuration(description = "OpenshiftGateway Configurations")
public class OpenshiftGatewayConfigurations {

    @Element(description = "Access URL of the Container Based Gateway")
    private String masterURL = "https://192.168.42.188:8443";

    @Element(description = "Namespace for the Service to be created")
    private String namespace = "mynamespace";

    @Element(description = "Name of the Docker Image of the Gateway")
    private String image = "wso2apim-gateway:v3.0.0";

    @Element(description = "URL of API Core")
    private String apiCoreURL = "https://localhost:9292";

    @Element(description = "Message Broker Host IP address")
    private String brokerHost = "localhost";

    @Element(description = "Client Secret of Service Account")
    private String serviceAccountSecret = "my-service-account-token-xpl9g";

    @Element(description = "Client Secret of Service Account")
    private String saTokenFile =
            "/home/../saToken.cert";

    @Element(description = "Location of ca.cert File of CMS")
    private String certFile = "../ca.crt";

    public void setMasterURL(String masterURL) {
        this.masterURL = masterURL;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setApiCoreURL(String apiCoreURL) {
        this.apiCoreURL = apiCoreURL;
    }

    public void setBrokerHost(String brokerHost) {
        this.brokerHost = brokerHost;
    }

    public void setServiceAccountSecret(String serviceAccountSecret) {
        this.serviceAccountSecret = serviceAccountSecret;
    }

    public void setSaTokenFile(String saTokenFile) {
        this.saTokenFile = saTokenFile;
    }

    public void setCertFile(String certFile) {
        this.certFile = certFile;
    }

    public String getMasterURL() {
        return masterURL;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getApiCoreURL() {
        return apiCoreURL;
    }

    public String getImage() {
        return image;
    }

    public String getBrokerHost() {
        return brokerHost;
    }

    public String getCertFile() {
        return certFile;
    }

    public String getServiceAccountSecret() {
        return serviceAccountSecret;
    }

    public String getSaTokenFile() {
        return saTokenFile;
    }
}
