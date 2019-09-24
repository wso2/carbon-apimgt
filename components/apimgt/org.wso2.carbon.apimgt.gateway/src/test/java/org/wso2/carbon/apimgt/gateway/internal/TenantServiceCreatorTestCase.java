/*
 *
 *    Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.synapse.SynapseConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.mediation.initializer.configurations.ConfigurationManager;
import org.wso2.carbon.mediation.initializer.configurations.ConfigurationTracker;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.cache.Cache;
import javax.cache.Caching;

import java.io.File;
import java.net.URL;

/**
 * Test class for TenantServiceCreator
 */
@RunWith(PowerMockRunner.class)

@PrepareForTest({PrivilegedCarbonContext.class, TenantServiceCreator.class, FileUtils.class, CacheProvider.class,
        ServiceReferenceHolder.class, Caching.class, Cache.class, APIManagerConfigurationService.class})

public class TenantServiceCreatorTestCase {

    @Before
    public void setup() {
        System.setProperty("carbon.home", "");
    }

    @Test
    public void testCreatedConfigurationContext() throws Exception {
        TenantServiceCreator tenantServiceCreator = new TenantServiceCreator();
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        // Failed to create Tenant's synapse sequences Error
        PowerMockito.mockStatic(Cache.class);
        Cache cache = Mockito.mock(Cache.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(APIManagerConfigurationService.class);
        PowerMockito.mockStatic(CacheProvider.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        final APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apiManagerConfigurationService =
                Mockito.mock(APIManagerConfigurationService.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).
                thenReturn(apiManagerConfigurationService);
        PowerMockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).
                thenReturn(apiManagerConfiguration);
        CacheProvider cacheProvider = Mockito.mock(CacheProvider.class);
        PowerMockito.when(cacheProvider.getDefaultCacheTimeout()).thenReturn((long) 900);

        Mockito.when(CacheProvider.getGatewayKeyCache()).thenReturn(cache);
        Mockito.when(CacheProvider.getResourceCache()).thenReturn(cache);
        Mockito.when(CacheProvider.getGatewayTokenCache()).thenReturn(cache);
        Mockito.when(CacheProvider.getInvalidTokenCache()).thenReturn(cache);
        tenantServiceCreator.createdConfigurationContext(configurationContext);

        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(FileUtils.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("abc.com");
        AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
        Mockito.when(configurationContext.getAxisConfiguration()).thenReturn(axisConfiguration);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        URL url = new URL("http", "localhost", 5000, "/fle/");
        Mockito.when(axisConfiguration.getRepository()).thenReturn(url);
        File tenantAxis2Repo = Mockito.mock(File.class);
        File synapseConfigsDir = Mockito.mock(File.class);

        //Couldn't create the synapse-config root on the file system error is logged.
        tenantServiceCreator.createdConfigurationContext(configurationContext);


        PowerMockito.whenNew(File.class).withArguments("/file/").thenReturn(tenantAxis2Repo);
        PowerMockito.whenNew(File.class).withAnyArguments()
                .thenReturn(synapseConfigsDir);
        Mockito.when(synapseConfigsDir.mkdir()).thenReturn(true);
        String synapseConfigsDirLocation = "/file/synapse-confgs";
        Mockito.when(synapseConfigsDir.getAbsolutePath()).thenReturn(synapseConfigsDirLocation);
        Mockito.doNothing().when(axisConfiguration).addParameter(SynapseConstants.Axis2Param.SYNAPSE_CONFIG_LOCATION,
                synapseConfigsDirLocation);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
        Mockito.when(privilegedCarbonContext.getRegistry(RegistryType.SYSTEM_CONFIGURATION)).thenReturn(userRegistry);
        PowerMockito.whenNew(ConfigurationManager.class).withArguments(userRegistry, configurationContext)
                .thenReturn(configurationManager);
        ConfigurationTracker tracker = Mockito.mock(ConfigurationTracker.class);
        Mockito.when(configurationManager.getTracker()).thenReturn(tracker);

        Mockito.when(tracker.getCurrentConfigurationName()).thenReturn("config-name");
        Mockito.when(synapseConfigsDir.exists()).thenReturn(false, false, false, true);

        copyFile("/repository/resources/apim-synapse-config/main.xml",
                "/file/synapse-confgs" + File.separator + "sequences" + File.separator + "main.xml");

        copyFile("/repository/resources/apim-synapse-config/fault.xml",
                "/file/synapse-confgs" + File.separator + "sequences" + File.separator + "falut.xml");

        copyFile("/repository/resources/apim-synapse-config/_auth_failure_handler_.xml",
                "/file/synapse-confgs" + File.separator + "sequences" + File.separator + "_auth_failure_handler_.xml");

        copyFile("/repository/resources/apim-synapse-config/_resource_mismatch_handler_.xml",
                "/file/synapse-confgs" + File.separator + "sequences" + File.separator + "_resource_mismatch_handler_.xml");

        copyFile("/repository/resources/apim-synapse-config/_throttle_out_handler_.xml",
                "/file/synapse-confgs" + File.separator + "sequences" + File.separator + "_throttle_out_handler_.xml");

        copyFile("/repository/resources/apim-synapse-config/_sandbox_key_error_.xml",
                "/file/synapse-confgs" + File.separator + "sequences" + File.separator + "_sandbox_key_error_.xml");

        copyFile("/repository/resources/apim-synapse-config/_production_key_error_.xml",
                "/file/synapse-confgs" + File.separator + "sequences" + File.separator + "_production_key_error_.xml");

        copyFile("/repository/resources/apim-synapse-config/_cors_request_handler_.xml",
                "/file/synapse-confgs" + File.separator + "sequences" + File.separator + "_cors_request_handler_.xml");
        copyFile("/repository/resources/apim-synapse-config/_threat_fault.xml",
                "/file/synapse-confgs" + File.separator + "sequences" + File.separator + "_threat_fault.xml");
        // test IOException Error while copying API manager specific synapse sequences
        tenantServiceCreator.createdConfigurationContext(configurationContext);
    }

    private void copyFile(String sourceFile, String destinationFile) throws Exception {
        File fileMain = Mockito.mock(File.class);
        File fileMainTo = Mockito.mock(File.class);
        Mockito.when(fileMain.exists()).thenReturn(true);
        PowerMockito.whenNew(File.class).withArguments(sourceFile).thenReturn(fileMain);
        PowerMockito.whenNew(File.class).withArguments(destinationFile).thenReturn(fileMainTo);
        PowerMockito.doNothing().when(FileUtils.class, "copyFile", fileMain, fileMainTo);
    }

    @Test
    public void testIsRunningSamplesMode() {
        Assert.assertTrue(TenantServiceCreator.isRunningSamplesMode());
    }
}
