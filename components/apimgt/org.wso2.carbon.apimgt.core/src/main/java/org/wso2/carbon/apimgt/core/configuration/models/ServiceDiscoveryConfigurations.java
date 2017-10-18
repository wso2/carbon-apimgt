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

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold Service Discovery configurations and Generate yaml file
 */
@Configuration(namespace = "wso2.carbon.serviceDiscovery", description = "Service Discovery configurations")
public class ServiceDiscoveryConfigurations {

    @Element(description = "enable service discovery")
    private boolean enabled = false;
    @Element(description = "service discovery implementation configurations")
    private List<ServiceDiscoveryImplConfig> implementationConfigs = new ArrayList<>();


    public ServiceDiscoveryConfigurations() {
        implementationConfigs.add(new ServiceDiscoveryImplConfig());
    }

    public boolean isServiceDiscoveryEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<ServiceDiscoveryImplConfig> getImplementationConfigs() {
        return implementationConfigs;
    }

    public void setImplementationConfigs(List<ServiceDiscoveryImplConfig> implementationConfigs) {
        this.implementationConfigs = implementationConfigs;
    }
}
