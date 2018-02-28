/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayConstants;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to hold Container Based Gateway configurations
 */
@Configuration(namespace = "wso2.carbon.apimgt.container.based.gateway",
        description = "Container Based Gateway Configurations")
public class ContainerBasedGatewayConfiguration {

    @Element(description = "enable container based gateway")
    private boolean enabled = false;

    @Element(description = "implementation class")
    private String implClass = ContainerBasedGatewayConstants.KUBERNETES_IMPL_CLASS;

    @Element(description = "implementation specific parameters")
    private Map<String, String> implParameters = new LinkedHashMap<>();

    public ContainerBasedGatewayConfiguration() {
        implParameters.put(ContainerBasedGatewayConstants.MASTER_URL, "");
        implParameters.put(ContainerBasedGatewayConstants.NAMESPACE, "default");
        implParameters.put(ContainerBasedGatewayConstants.API_CORE_URL, "https://localhost:9443");
        implParameters.put(ContainerBasedGatewayConstants.BROKER_HOST, "https://localhost:5672");
        implParameters.put(ContainerBasedGatewayConstants.CMS_TYPE, ContainerBasedGatewayConstants.KUBERNETES);
        implParameters.put(ContainerBasedGatewayConstants.SA_TOKEN_FILE_NAME, "");
        implParameters.put(ContainerBasedGatewayConstants.GATEWAY_HOSTNAME, "wso2.com");
    }

    public boolean isContainerBasedGatewayEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getImplClass() {
        return implClass;
    }

    public void setImplClass(String implClass) {
        this.implClass = implClass;
    }

    public Map<String, String> getImplParameters() {
        return implParameters;
    }

    public void setImplParameters(Map<String, String> implParameters) {
        this.implParameters = implParameters;
    }
}
