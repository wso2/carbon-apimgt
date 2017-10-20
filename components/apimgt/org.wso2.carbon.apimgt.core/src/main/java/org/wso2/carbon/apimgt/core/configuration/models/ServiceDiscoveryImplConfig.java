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


import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.HashMap;

/**
 * Class to hold Container Management System specific Service Discovery configurations
 */
@Configuration(description = "Container Management System specific Service Discovery configurations")
public class ServiceDiscoveryImplConfig {

    @Element(description = "service discovery implementation class")
    private String implClass = APIMgtConstants.ServiceDiscoveryConstants.KUBERNETES_SERVICE_DISCOVERER;
    @Element(description = "container management system specific properties")
    private HashMap<String, String> cmsSpecificParameters = new HashMap<>();


    public ServiceDiscoveryImplConfig() {
        cmsSpecificParameters.put("masterUrl", "https://192.168.99.100:8443/");
        cmsSpecificParameters.put("includeClusterIPs", "false");
        cmsSpecificParameters.put("includeExternalNameServices", "false");
        cmsSpecificParameters.put("podMountedSATokenFile", "/var/run/secrets/kubernetes.io/serviceaccount/token");
        cmsSpecificParameters.put("externalSATokenFile", "KubernetesToken");
        cmsSpecificParameters.put("caCertLocation", "/resources/security/ca.crt");
    }

    public String getImplClass() {
        return implClass;
    }

    public void setImplClass(String implClass) {
        this.implClass = implClass;
    }

    public HashMap<String, String> getCmsSpecificParameters() {
        return cmsSpecificParameters;
    }

    public void setCmsSpecificParameters(HashMap<String, String> cmsSpecificParameters) {
        this.cmsSpecificParameters = cmsSpecificParameters;
    }
}
