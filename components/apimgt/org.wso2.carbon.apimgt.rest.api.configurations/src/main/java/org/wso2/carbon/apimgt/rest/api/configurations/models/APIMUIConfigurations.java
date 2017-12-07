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


import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Collections;
import java.util.List;

/**
 * Class to hold EnvironmentConfigurations configuration parameters
 */
@Configuration(namespace = "wso2.carbon.apimgt.ui.configs", description = "APIM UI Configuration Parameters")
public class APIMUIConfigurations {

    @Element(description = "list of environments\n" +
            "  ◆ host           - host of an environment (eg 127.0.0.1:9292, use '<empty-string>' for the current host)\n" +
            "  ◆ loginTokenPath - token endpoint URL\n" +
            "  ◆ label          - environment label to uniquely identify an environment")
    private List<EnvironmentConfigurations> environments = Collections.singletonList(new EnvironmentConfigurations());

    public List<EnvironmentConfigurations> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<EnvironmentConfigurations> environments) {
        this.environments = environments;
    }
}