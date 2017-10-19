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
 * Class to hold Version configuration parameters
 */
@Configuration(description = "Version Configurations")
public class NewVersionNotifierConfigurations {

    @Element(description = "notifiers")
    List<NotifierConfigurations> notifierConfigurations = new ArrayList<>();

    public NewVersionNotifierConfigurations() {
        notifierConfigurations.add(new NotifierConfigurations());
    }

    public List<NotifierConfigurations> getNotifierConfigurations() {
        return notifierConfigurations;
    }

    public void setNotifierConfigurations(List<NotifierConfigurations> notifierConfigurations) {
        this.notifierConfigurations = notifierConfigurations;
    }
}
