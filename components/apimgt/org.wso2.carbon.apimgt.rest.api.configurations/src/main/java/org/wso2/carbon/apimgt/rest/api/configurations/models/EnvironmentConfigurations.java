/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.configurations.models;


import org.wso2.carbon.apimgt.rest.api.configurations.constants.ConfigurationConstants;
import org.wso2.carbon.apimgt.rest.api.configurations.models.elements.Environment;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Arrays;
import java.util.List;

/**
 * Class to hold Environment configuration parameters
 */
@Configuration(namespace = "wso2.carbon.apimgt.environments", description = "Environment Configurations")
public class EnvironmentConfigurations {

    @Element(description = "list of web clients (eg: 127.0.0.1:9292) to allow make requests to current environment\n" +
            "(use '" +ConfigurationConstants.ALL_ORIGINS + "' to allow any web client)")
    private List<String> clientHosts = Arrays.asList(new String[]{ConfigurationConstants.ALL_ORIGINS});

    //Unique name for environment to set cookies by backend
    @Element(description = "current environment's label from the list of environments")
    private String environmentLabel = "Default";

    @Element(description = "list of environments\n" +
            "  ◆ host           - host of a environment (eg 127.0.0.1:9292, use '<empty-string>' for current host)\n" +
            "  ◆ loginTokenPath - token endpoint URL\n" +
            "  ◆ label          - environment label to uniquely identify a environment")
    private List<Environment> environments = Arrays.asList(new Environment());

    public List<String> getClientHosts() {
        return clientHosts;
    }

    public void setClientHosts(List<String> clientHosts) {
        this.clientHosts = clientHosts;
    }

    public String getEnvironmentLabel() {
        return environmentLabel;
    }

    public void setEnvironmentLabel(String environmentLabel) {
        this.environmentLabel = environmentLabel;
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }
}