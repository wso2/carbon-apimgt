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


import org.wso2.carbon.apimgt.core.impl.ServiceDiscovererKubernetes;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to hold Implementation specific Service Discovery configurations
 */
@Configuration(description = "Implementation specific Service Discovery configurations")
public class ServiceDiscoveryImplConfig {

    @Element(description = "implementation class")
    private String implClass = APIMgtConstants.ServiceDiscoveryConstants.KUBERNETES_SERVICE_DISCOVERER;
    @Element(description = "implementation specific parameters")
    private Map<String, String> implParameters = new LinkedHashMap<>();

    public ServiceDiscoveryImplConfig() {
        implParameters.put(ServiceDiscovererKubernetes.MASTER_URL, "");
        implParameters.put(ServiceDiscovererKubernetes.CA_CERT_PATH,
                "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt");
        implParameters.put(ServiceDiscovererKubernetes.INCLUDE_CLUSTER_IPS, "true");
        implParameters.put(ServiceDiscovererKubernetes.INCLUDE_EXTERNAL_NAME_SERVICES, "true");
        implParameters.put(ServiceDiscovererKubernetes.EXTERNAL_SA_TOKEN_FILE_NAME, "");
        implParameters.put(ServiceDiscovererKubernetes.POD_MOUNTED_SA_TOKEN_FILE_PATH,
                "/var/run/secrets/kubernetes.io/serviceaccount/token");
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
