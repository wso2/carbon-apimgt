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

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold key manager configurations
 */
@Configuration(description = "Key Management Configurations")
public class EnvironmentConfigurations {

    @Element(description = "Default environment IS Host name")
    private String defaultEnvIsHost = "localhost";

    @Element(description = "Default environment IS port")
    private int defaultEnvIsPort = 0;

    @Element(description = "Default environment IS Token endpoint URL")
    private String defaultEnvIsEndPoint = "https://localhost:9443/oauth2/token";

    @Element(description = "Default environment IS Token endpoint URL")
    private String defaultEnvIsrevokeEndPoint = "";


    public String getDefaultEnvIsHost() {
        return defaultEnvIsHost;
    }

    public int getDefaultEnvIsPort() {
        return defaultEnvIsPort;
    }

    public String getDefaultEnvIsEndPoint() {
        return defaultEnvIsEndPoint;
    }

    public String getDefaultEnvIsrevokeEndPoint() {
        return defaultEnvIsrevokeEndPoint;
    }
}
