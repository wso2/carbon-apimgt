/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.configurations;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.rest.api.configurations.internal.ConfigurationActivator;
import org.wso2.carbon.apimgt.rest.api.configurations.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.configurations.models.EnvironmentConfigurations;

public class ConfigurationServiceTestCase {

    @Test(description = "Test ConfigurationService instance")
    public void testGetInstance() {
        ConfigurationService configurationService = ConfigurationService.getInstance();
        Assert.assertNotNull(configurationService);
    }

    @Test(description = "Test whether environment name is empty")
    public void testGetEnvironmentName() {
        String environmentName = ConfigurationService.getEnvironmentName();
        Assert.assertFalse(environmentName.isEmpty());
    }

    @Test(description = "Test for Environment Configurations")
    public void testGetEnvironmentConfigurations() {
        ////Happy Path
        EnvironmentConfigurations environmentConfigurations = ConfigurationService.getInstance()
                .getEnvironmentConfigurations();
        Assert.assertNotNull(environmentConfigurations);
    }
}
