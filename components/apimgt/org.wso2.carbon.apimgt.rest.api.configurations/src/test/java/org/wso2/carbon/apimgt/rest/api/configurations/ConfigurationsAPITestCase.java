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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.WorkflowConfig;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExtensionsConfigBuilder;
import org.wso2.carbon.apimgt.rest.api.configurations.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.configurations.models.APIMUIConfigurations;
import org.wso2.carbon.apimgt.rest.api.configurations.models.EnvironmentConfigurations;
import org.wso2.carbon.apimgt.rest.api.configurations.utils.bean.EnvironmentConfigBean;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;

public class ConfigurationsAPITestCase {
    private List<EnvironmentConfigurations> sampleEnvironments;

    @BeforeTest
    public void setup() throws Exception {
        WorkflowExtensionsConfigBuilder.build(new ConfigProvider() {

            @Override
            public <T> T getConfigurationObject(Class<T> configClass) throws ConfigurationException {
                T workflowConfig = (T) new WorkflowConfig();
                return workflowConfig;
            }

            @Override
            public Object getConfigurationObject(String s) throws ConfigurationException {
                return null;
            }

            public <T> T getConfigurationObject(String s, Class<T> aClass) throws ConfigurationException {
                return null;
            }


        });

        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        ServiceReferenceHolder.getInstance().setConfigProvider(configProvider);
    }

    @Test
    public void testEnvironments() {
        //Happy Path - 200
        ////Default configurations
        ConfigurationsAPI configurationsAPI = new ConfigurationsAPI();
        Response response = configurationsAPI.environments();
        Assert.assertEquals(response.getStatus(), 200);

        EnvironmentConfigBean environmentConfigBean = (EnvironmentConfigBean) response.getEntity();
        Assert.assertFalse(environmentConfigBean.getEnvironments().isEmpty());
        Assert.assertNotNull(environmentConfigBean.getEnvironments().get(0).getHost());
        Assert.assertFalse(environmentConfigBean.getEnvironments().get(0).getLabel().isEmpty());
        Assert.assertFalse(environmentConfigBean.getEnvironments().get(0).getLoginTokenPath().isEmpty());

        ////Custom configurations
        APIMUIConfigurations apimUIConfigurations = ConfigurationService.getInstance().getApimUIConfigurations();
        apimUIConfigurations.setEnvironments(getSampleEnvironments());

        response = configurationsAPI.environments();
        environmentConfigBean = (EnvironmentConfigBean) response.getEntity();
        Assert.assertEquals(environmentConfigBean.getEnvironments().size(), getSampleEnvironments().size());
        for (int i = 0; i < environmentConfigBean.getEnvironments().size(); i++) {
            Assert.assertEquals(environmentConfigBean.getEnvironments().get(i).getLabel(),
                    getSampleEnvironments().get(i).getLabel());
            Assert.assertEquals(environmentConfigBean.getEnvironments().get(i).getHost(),
                    getSampleEnvironments().get(i).getHost());
            Assert.assertEquals(environmentConfigBean.getEnvironments().get(i).getLoginTokenPath(),
                    getSampleEnvironments().get(i).getLoginTokenPath());
        }
    }

    /**
     * Sample list of environments
     *
     * @return List of mocked environments
     */
    private List<EnvironmentConfigurations> getSampleEnvironments() {
        if (sampleEnvironments != null) {
            return sampleEnvironments;
        }

        //Sample values for environmentConfigurations
        String[] labels = {"Development", "Production", "Staged"};
        String[] hosts = {"dev.sample.com:9292", "prod.sample.com:9292", "staged.sample.com:9292"};
        String loginTokenPath = "/login/token";

        //Sample environmentConfigurations with sample values
        EnvironmentConfigurations[] environmentConfigurations = new EnvironmentConfigurations[labels.length];
        for (int i = 0; i < environmentConfigurations.length; i++) {
            EnvironmentConfigurations environmentConfiguration = new EnvironmentConfigurations();
            environmentConfiguration.setHost(hosts[i]);
            environmentConfiguration.setLabel(labels[i]);
            environmentConfiguration.setLoginTokenPath(loginTokenPath);
            environmentConfigurations[i] = environmentConfiguration;
        }

        sampleEnvironments = Arrays.asList(environmentConfigurations);
        return sampleEnvironments;
    }

    @Test
    public void testAvailableFeatures() {

        ConfigurationsAPI configurationsAPI = new ConfigurationsAPI();
        Response response = configurationsAPI.availableFeatures();
        Assert.assertEquals(response.getStatus(), 200);
    }
}
