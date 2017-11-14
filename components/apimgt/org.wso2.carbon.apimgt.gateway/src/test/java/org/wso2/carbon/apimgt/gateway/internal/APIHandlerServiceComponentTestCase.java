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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.APIKeyValidatorClientPool;
import org.wso2.carbon.apimgt.gateway.handlers.security.thrift.ThriftKeyValidatorClientPool;
import org.wso2.carbon.apimgt.gateway.service.APIThrottleDataService;
import org.wso2.carbon.apimgt.gateway.service.APIThrottleDataServiceImpl;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.util.BlockingConditionRetriever;
import org.wso2.carbon.apimgt.gateway.throttling.util.KeyTemplateRetriever;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * Test class for APIHandlerServiceComponent
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIKeyValidatorClientPool.class, ThriftKeyValidatorClientPool.class, ServiceReferenceHolder.class,
        APIManagerConfiguration.class, APIHandlerServiceComponent.class})
public class APIHandlerServiceComponentTestCase {
    private APIHandlerServiceComponent apiHandlerServiceComponent;
    private APIKeyValidatorClientPool apiKeyValidatorClientPool;
    private ComponentContext context;
    private ServiceReferenceHolder serviceReferenceHolder;
    private ConfigurationContextService configurationContextService;
    private APIManagerConfigurationService apiManagerConfigurationService;
    private APIManagerConfiguration apiManagerConfiguration;
    private BundleContext bundleContext;
    private TenantServiceCreator tenantServiceCreator;
    private ServiceRegistration registration;

    @Before
    public void setup() throws Exception {
        System.setProperty("carbon.home", "");
        apiHandlerServiceComponent = new APIHandlerServiceComponent();
        context = Mockito.mock(ComponentContext.class);
        registration = Mockito.mock(ServiceRegistration.class);
        bundleContext = Mockito.mock(BundleContext.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        tenantServiceCreator = Mockito.mock(TenantServiceCreator.class);
        apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.whenNew(TenantServiceCreator.class).withAnyArguments().thenReturn(tenantServiceCreator);
        apiHandlerServiceComponent.setConfiguration(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_GATEWAY_TYPE)).thenReturn("synapse");
        Mockito.when(context.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(APIManagerConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.doNothing().when(apiManagerConfiguration).load("/repository/conf/api-manager.xml");
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        configurationContextService = Mockito.mock(ConfigurationContextService.class);

        PowerMockito.mockStatic(ThriftKeyValidatorClientPool.class);
        PowerMockito.mockStatic(APIKeyValidatorClientPool.class);
        apiKeyValidatorClientPool = Mockito.mock(APIKeyValidatorClientPool.class);
        ThriftKeyValidatorClientPool thriftKeyValidatorClientPool = Mockito.mock(ThriftKeyValidatorClientPool.class);
        PowerMockito.when(APIKeyValidatorClientPool.getInstance()).thenReturn(apiKeyValidatorClientPool);
        PowerMockito.when(ThriftKeyValidatorClientPool.getInstance()).thenReturn(thriftKeyValidatorClientPool);

        Mockito.when(bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                tenantServiceCreator, null)).thenReturn(null);
    }

    @Test
    public void testActivate() throws Exception {
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);
        Mockito.when(apiManagerConfiguration.getThrottleProperties()).thenReturn(throttleProperties);
        Mockito.when(throttleProperties.isEnabled()).thenReturn(true);
        ThrottleDataHolder throttleDataHolder = Mockito.mock(ThrottleDataHolder.class);
        APIThrottleDataServiceImpl apiThrottleDataServiceImpl = Mockito.mock(APIThrottleDataServiceImpl.class);

        PowerMockito.whenNew(ThrottleDataHolder.class).withAnyArguments().thenReturn(throttleDataHolder);
        PowerMockito.whenNew(APIThrottleDataServiceImpl.class).withAnyArguments().thenReturn(apiThrottleDataServiceImpl);


        Mockito.when(bundleContext.registerService(APIThrottleDataService.class.getName(),
                apiThrottleDataServiceImpl, null)).thenReturn(registration);

        ThrottleProperties.BlockCondition blockCondition = Mockito.mock(ThrottleProperties.BlockCondition.class);
        Mockito.when(throttleProperties.getBlockCondition()).thenReturn(blockCondition);
        Mockito.when(blockCondition.isEnabled()).thenReturn(true);
        BlockingConditionRetriever blockingConditionRetriever = Mockito.mock(BlockingConditionRetriever.class);
        KeyTemplateRetriever keyTemplateRetriever = Mockito.mock(KeyTemplateRetriever.class);
        PowerMockito.whenNew(BlockingConditionRetriever.class).withAnyArguments().thenReturn(blockingConditionRetriever);
        PowerMockito.whenNew(KeyTemplateRetriever.class).withAnyArguments().thenReturn(keyTemplateRetriever);
        PowerMockito.doNothing().when(blockingConditionRetriever).startWebServiceThrottleDataRetriever();
        PowerMockito.doNothing().when(keyTemplateRetriever).startKeyTemplateDataRetriever();
        apiHandlerServiceComponent.activate(context);
        //test deactivate
        Mockito.doNothing().when(apiKeyValidatorClientPool).cleanup();
        apiHandlerServiceComponent.deactivate(context);
    }

    @Test
    public void testSetConfigurationContextService() {
        Mockito.when(serviceReferenceHolder.getConfigurationContextService()).thenReturn(configurationContextService);
        apiHandlerServiceComponent.setConfigurationContextService(configurationContextService);
        Assert.assertEquals(configurationContextService, ServiceReferenceHolder.getInstance().getConfigurationContextService());
    }

    @Test
    public void testUnsetConfigurationContextService() {
        Mockito.when(serviceReferenceHolder.getConfigurationContextService()).thenReturn(null);
        apiHandlerServiceComponent.unsetConfigurationContextService(null);
        Assert.assertNull(ServiceReferenceHolder.getInstance().getConfigurationContextService());
    }

    @Test
    public void testSetAPIManagerConfigurationService() {
        apiHandlerServiceComponent.setAPIManagerConfigurationService(Mockito.mock(APIManagerConfigurationService.class));
        Assert.assertEquals(apiManagerConfigurationService, ServiceReferenceHolder.getInstance().getApiManagerConfigurationService());
    }

    @Test
    public void testUnsetAPIManagerConfigurationService() {
        Mockito.when(serviceReferenceHolder.getApiManagerConfigurationService()).thenReturn(null);
        apiHandlerServiceComponent.unsetAPIManagerConfigurationService(null);
        Assert.assertNull(ServiceReferenceHolder.getInstance().getApiManagerConfigurationService());
    }


}
