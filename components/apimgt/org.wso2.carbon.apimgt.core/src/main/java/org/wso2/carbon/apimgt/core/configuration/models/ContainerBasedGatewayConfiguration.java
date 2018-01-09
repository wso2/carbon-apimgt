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
 * Class to hold Container Based Gateway configurations
 */
@Configuration(description = "Container Based Gateway Configurations")
public class ContainerBasedGatewayConfiguration {

    @Element(description = "Container Based management System Type")
    private String cmsType = "kubernetes";

    @Element(description = "KubernetesGateway Configurations")
    private KubernetesGatewayConfigurations kubernetesGatewayConfigurations = new KubernetesGatewayConfigurations();

    @Element(description = "OpenshiftGateway Configurations")
    private OpenshiftGatewayConfigurations openshiftGatewayConfigurations  = new OpenshiftGatewayConfigurations();

    public KubernetesGatewayConfigurations getKubernetesGatewayConfigurations() {
        return kubernetesGatewayConfigurations;
    }
    public OpenshiftGatewayConfigurations getOpenshiftGatewayConfigurations() {
        return openshiftGatewayConfigurations;
    }

    public void setOpenshiftGatewayConfigurations(OpenshiftGatewayConfigurations openshiftGatewayConfigurations) {
        this.openshiftGatewayConfigurations = openshiftGatewayConfigurations;
    }
    public void setKubernetesGatewayConfigurations(KubernetesGatewayConfigurations kubernetesGatewayConfigurations) {
        this.kubernetesGatewayConfigurations = kubernetesGatewayConfigurations;
    }

    public String getCmsType() {
        return cmsType;
    }

    public void setCmsType(String cmsType) {
        this.cmsType = cmsType;
    }
}
