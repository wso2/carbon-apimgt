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
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Collections;
import java.util.List;

/**
 * Class to hold Environment configurations
 */
@Configuration(namespace = "wso2.carbon.apimgt.environments",
        description = "APIM Environment Configuration Parameters")
public class EnvironmentConfigurations {
    //Unique name for environment to set cookies by backend
    @Element(description = "current environment's label from the list of environments")
    private String environmentLabel = "Default";

    @Element(description = "list of web clients (eg: 127.0.0.1:9443) to allow make requests" +
            " to current environment\n(use '" + APIMgtConstants.CORSAllowOriginConstants.ALLOW_ALL_ORIGINS +
            "' to allow any web client)\nthe first host is used as UI-Service")
    //If the first host is an empty string, use "wso2.carbon.apimgt.application: apimBaseUrl" as UI-Service
    private List<String> allowedHosts = Collections.singletonList("");

    @Element(description = "Multi-Environment Overview Configurations")
    private MultiEnvironmentOverview multiEnvironmentOverview = new MultiEnvironmentOverview();

    public String getEnvironmentLabel() {
        return environmentLabel;
    }

    public void setEnvironmentLabel(String environmentLabel) {
        this.environmentLabel = environmentLabel;
    }

    public List<String> getAllowedHosts() {
        return allowedHosts;
    }

    public void setAllowedHosts(List<String> allowedHosts) {
        this.allowedHosts = allowedHosts;
    }

    public MultiEnvironmentOverview getMultiEnvironmentOverview() {
        return multiEnvironmentOverview;
    }

    public void setMultiEnvironmentOverview(MultiEnvironmentOverview multiEnvironmentOverview) {
        this.multiEnvironmentOverview = multiEnvironmentOverview;
    }
}
