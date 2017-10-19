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
 * Class to hold Analytics configurations.
 */
@Configuration(description = "Analytics configurations")
public class AnalyticsConfigurations {

    @Element(description = "enable analytics")
    private boolean enabled = false;
    @Element(description = "DAS server URL")
    private String dasServerURL = "http://localhost:9091";
    @Element(description = "DAS server credentials")
    private CredentialConfigurations dasServerCredentials = new CredentialConfigurations();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDasServerURL() {
        return dasServerURL;
    }

    public void setDasServerURL(String dasServerURL) {
        this.dasServerURL = dasServerURL;
    }

    public CredentialConfigurations getDasServerCredentials() {
        return dasServerCredentials;
    }

    public void setDasServerCredentials(CredentialConfigurations dasServerCredentials) {
        this.dasServerCredentials = dasServerCredentials;
    }
}
