/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.workflow;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

import static org.wso2.carbon.h2.osgi.utils.CarbonConstants.CARBON_HOME;

/**
 * WorkflowExecutorFactory test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, Caching.class, WorkflowExecutorFactory.class})
public class WorkflowExecutorFactoryTest {

    private WorkflowExecutorFactory workflowExecutorFactory;
    private PrivilegedCarbonContext carbonContext;
    private Cache cache;
    private TenantWorkflowConfigHolder tenantWorkflowConfigHolder;
    private String tenantDomain = "carbon.super";
    private int tenantID = -1234;

    @Before
    public void init() {
        System.setProperty(CARBON_HOME, "");
        carbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        PowerMockito.when(carbonContext.getTenantDomain()).thenReturn(tenantDomain);
        PowerMockito.when(carbonContext.getTenantId()).thenReturn(tenantID);
        cache = Mockito.mock(Cache.class);
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        PowerMockito.mockStatic(Caching.class);
        Mockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(APIConstants.WORKFLOW_CACHE_NAME)).thenReturn(cache);
        tenantWorkflowConfigHolder = Mockito.mock(TenantWorkflowConfigHolder.class);
        workflowExecutorFactory = WorkflowExecutorFactory.getInstance();
    }

    @Test
    public void testRetrievingCachedWorkflowConfiguration() {
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(tenantWorkflowConfigHolder);
        try {
            Assert.assertNotNull(workflowExecutorFactory.getWorkflowConfigurations());
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException has occurred while retrieving workflow configuration");
        }
    }

    @Test
    public void testInitialisingWorkflowConfigCache() throws Exception {
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.whenNew(TenantWorkflowConfigHolder.class).withAnyArguments().thenReturn
                (tenantWorkflowConfigHolder);
        PowerMockito.doNothing().when(tenantWorkflowConfigHolder).load();
        try {
            Assert.assertNotNull(workflowExecutorFactory.getWorkflowConfigurations());
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException has occurred while retrieving workflow configuration");
        }
    }

    @Test
    public void testWorkflowExceptionWhileInitialisingWorkflowConfigCache() throws Exception {
        String errorMessage = "Error occurred while creating workflow configurations for tenant " + tenantDomain;
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.whenNew(TenantWorkflowConfigHolder.class).withAnyArguments().thenReturn
                (tenantWorkflowConfigHolder);
        PowerMockito.doThrow(new WorkflowException(errorMessage)).when(tenantWorkflowConfigHolder).load();
        try {
            workflowExecutorFactory.getWorkflowConfigurations();
            Assert.fail("Expected WorkflowException has not occurred while retrieving workflow configuration");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), errorMessage);
        }
    }

    @Test
    public void testCreatingWorkflowDTO() {

        //Create WorkflowDTO for AM_APPLICATION_CREATION workflow
        Assert.assertTrue(workflowExecutorFactory.createWorkflowDTO("AM_APPLICATION_CREATION") instanceof
                ApplicationWorkflowDTO);

        //Create WorkflowDTO for AM_APPLICATION_REGISTRATION_PRODUCTION workflow
        Assert.assertTrue(workflowExecutorFactory.createWorkflowDTO("AM_APPLICATION_REGISTRATION_PRODUCTION")
                instanceof ApplicationRegistrationWorkflowDTO);

        //Create WorkflowDTO for AM_APPLICATION_REGISTRATION_SANDBOX workflow
        Assert.assertTrue(workflowExecutorFactory.createWorkflowDTO("AM_APPLICATION_REGISTRATION_SANDBOX")
                instanceof ApplicationRegistrationWorkflowDTO);

        //Create WorkflowDTO for AM_SUBSCRIPTION_CREATION workflow
        Assert.assertTrue(workflowExecutorFactory.createWorkflowDTO("AM_SUBSCRIPTION_CREATION") instanceof
                SubscriptionWorkflowDTO);

        //Create WorkflowDTO for AM_USER_SIGNUP workflow
        Assert.assertTrue(workflowExecutorFactory.createWorkflowDTO("AM_USER_SIGNUP") instanceof WorkflowDTO);

        //Create WorkflowDTO for AM_API_STATE workflow
        Assert.assertTrue(workflowExecutorFactory.createWorkflowDTO("AM_API_STATE") instanceof
                APIStateWorkflowDTO);

        //When invalid workflow type provided
        Assert.assertNull(workflowExecutorFactory.createWorkflowDTO("INVALID_WF_TYPE"));
    }

    @Test
    public void testRetrievingWorkflowExecutor() {
        String workflowType = "AM_APPLICATION_CREATION";
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(tenantWorkflowConfigHolder);
        WorkflowExecutor workflowExecutor = Mockito.mock(WorkflowExecutor.class);
        Mockito.when(tenantWorkflowConfigHolder.getWorkflowExecutor(workflowType)).thenReturn(workflowExecutor);

        try {
            Assert.assertNotNull(workflowExecutorFactory.getWorkflowExecutor(workflowType));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException has occurred while retrieving workflow executor for :" +
                    workflowType);
        }
    }

    @Test
    public void testWorkflowExceptionWhileRetrievingWorkflowExecutor() throws Exception {
        String workflowType = "AM_APPLICATION_CREATION";
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.whenNew(TenantWorkflowConfigHolder.class).withAnyArguments().thenReturn
                (tenantWorkflowConfigHolder);
        PowerMockito.doThrow(new WorkflowException("")).when(tenantWorkflowConfigHolder).load();
        try {
            workflowExecutorFactory.getWorkflowExecutor(workflowType);
            Assert.fail("Expected WorkflowException has not occurred while retrieving workflow executor from config");
        } catch (WorkflowException e) {
            Assert.assertEquals("Error while creating WorkFlowDTO for " + workflowType, e.getMessage());
        }
    }

}
