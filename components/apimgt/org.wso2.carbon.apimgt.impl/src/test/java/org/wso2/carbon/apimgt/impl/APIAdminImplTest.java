/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.io.FileUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.api.model.WorkflowTaskService;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.dto.WorkflowProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.DefaultWorkflowTaskService;
import org.wso2.carbon.context.CarbonContext;

import java.io.File;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, ApiMgtDAO.class, APIUtil.class, CarbonContext.class})
public class APIAdminImplTest {

    ServiceReferenceHolder serviceReferenceHolder;
    APIMConfigService apimConfigService;

    ApiMgtDAO apiMgtDAO;

    @Before
    public void setup() {

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(CarbonContext.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        apimConfigService = Mockito.mock(APIMConfigService.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getApimConfigService()).thenReturn(apimConfigService);
    }

    @Test
    public void getTenantConfig() throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        Mockito.when(apimConfigService.getTenantConfig("abc.com")).thenReturn("abcde");
        Assert.assertEquals(apiAdmin.getTenantConfig("abc.com"), "abcde");
        Mockito.verify(apimConfigService, Mockito.times(1)).getTenantConfig("abc.com");
    }

    @Test
    public void getTenantConfigException() throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        Mockito.when(apimConfigService.getTenantConfig("abc.com")).thenThrow(APIManagementException.class);
        try {
            apiAdmin.getTenantConfig("abc.com");
            Assert.fail("Method successfully invoked");
        } catch (APIManagementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void updateTenantConfig() throws Exception {

        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());
        String tenantConf = FileUtils.readFileToString(siteConfFile);
        PowerMockito.doNothing().when(APIUtil.class, "validateRestAPIScopes", tenantConf);
        Schema schema = Mockito.mock(Schema.class);
        PowerMockito.when(APIUtil.class, "retrieveTenantConfigJsonSchema").thenReturn(schema);
        Mockito.doNothing().when(schema).validate(Mockito.any());
        APIAdmin apiAdmin = new APIAdminImpl();
        Mockito.doNothing().when(apimConfigService).updateTenantConfig("abc.com", tenantConf);
        apiAdmin.updateTenantConfig("abc.com", tenantConf);
    }

    // Schema not present
    @Test
    public void updateTenantConfigNegative1() throws Exception {

        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());
        String tenantConf = FileUtils.readFileToString(siteConfFile);
        PowerMockito.doNothing().when(APIUtil.class, "validateRestAPIScopes", tenantConf);
        Schema schema = Mockito.mock(Schema.class);
        PowerMockito.when(APIUtil.class, "retrieveTenantConfigJsonSchema").thenReturn(null);
        APIAdmin apiAdmin = new APIAdminImpl();
        Mockito.doNothing().when(apimConfigService).updateTenantConfig("abc.com", tenantConf);
        try {
            apiAdmin.updateTenantConfig("abc.com", tenantConf);
            Assert.fail("Method successfully invoked");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "tenant-config validation failure");
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.INTERNAL_ERROR);
        }
    }

    // invalid json
    @Test
    public void updateTenantConfigNegative2() throws Exception {

        String tenantConf = "{\"hello\"";
        PowerMockito.doNothing().when(APIUtil.class, "validateRestAPIScopes", tenantConf);
        APIAdmin apiAdmin = new APIAdminImpl();
        Schema schema = Mockito.mock(Schema.class);
        PowerMockito.when(APIUtil.class, "retrieveTenantConfigJsonSchema").thenReturn(schema);

        Mockito.doNothing().when(apimConfigService).updateTenantConfig("abc.com", tenantConf);
        try {
            apiAdmin.updateTenantConfig("abc.com", tenantConf);
            Assert.fail("Method successfully invoked");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "tenant-config validation failure");
        }
    }

    // valid json element missing
    @Test
    public void updateTenantConfigNegative3() throws Exception {

        String tenantConf = "{\"hello\":\"world\"}";
        PowerMockito.doNothing().when(APIUtil.class, "validateRestAPIScopes", tenantConf);
        APIAdmin apiAdmin = new APIAdminImpl();
        Schema schema = Mockito.mock(Schema.class);
        PowerMockito.when(APIUtil.class, "retrieveTenantConfigJsonSchema").thenReturn(schema);
        Mockito.doThrow(ValidationException.class).when(schema).validate(Mockito.any());
        Mockito.doNothing().when(apimConfigService).updateTenantConfig("abc.com", tenantConf);
        try {
            apiAdmin.updateTenantConfig("abc.com", tenantConf);
            Assert.fail("Method successfully invoked");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "tenant-config validation failure");
        }
    }

    @Test
    public void getTenantConfigSchema() throws Exception {

        APIAdmin apiAdmin = new APIAdminImpl();
        Schema schema = Mockito.mock(Schema.class);
        PowerMockito.when(APIUtil.class, "retrieveTenantConfigJsonSchema").thenReturn(schema);
        Assert.assertEquals(apiAdmin.getTenantConfigSchema("abc.com"), schema.toString());
    }

    @Test
    public void getPolicyByNameAndType() throws Exception {
        APIPolicy apiPolicy = Mockito.mock(APIPolicy.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);
        APIAdmin apiAdmin = new APIAdminImpl();
        Mockito.when(apiMgtDAO.getAPIPolicy(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenReturn(apiPolicy);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getThrottleProperties()).thenReturn(throttleProperties);
        Mockito.when(throttleProperties.isEnableUnlimitedTier()).thenReturn(true);

        try {
            Policy policy = apiAdmin.getPolicyByNameAndType(ArgumentMatchers.anyInt(), "api", ArgumentMatchers.anyString());
            Assert.assertNotNull(policy);
        } catch (APIManagementException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testWorkflowDefaultPendingList() throws Exception {
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        WorkflowProperties workflowProperties = Mockito.mock(WorkflowProperties.class);
        Mockito.when(apiManagerConfiguration.getWorkflowProperties()).thenReturn(workflowProperties);
        Mockito.when(workflowProperties.isListTasks()).thenReturn(true);

        WorkflowTaskService task = new DefaultWorkflowTaskService();
        Mockito.when(serviceReferenceHolder.getWorkflowTaskService()).thenReturn(task);
        String workflowType = "AM_APPLICATION_CREATION";
        String status = "CREATED";
        String tenantDomain = "carbon.super";
        String reference = "1234";

        Workflow[] pendingTasks = new Workflow[1];
        Workflow pendingTask = new Workflow();
        pendingTask.setExternalWorkflowReference(reference);
        pendingTasks[0] = pendingTask;
        Mockito.when(apiMgtDAO.getworkflows(workflowType, status, tenantDomain)).thenReturn(pendingTasks);


        CarbonContext context = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(context);

        APIAdmin apiAdmin = new APIAdminImpl();
        Workflow[] returnedWorkflows = apiAdmin.getworkflows(workflowType, status, tenantDomain);
        Assert.assertNotNull("Workflow array null", returnedWorkflows);
        Assert.assertEquals("Workflow array length mismatch", 1, returnedWorkflows.length);
        Assert.assertEquals("Invalid workflow", reference, returnedWorkflows[0].getExternalWorkflowReference());

    }
    
    @Test
    public void testWorkflowPendingListWithListingDisabled() throws Exception {
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        WorkflowProperties workflowProperties = Mockito.mock(WorkflowProperties.class);
        Mockito.when(apiManagerConfiguration.getWorkflowProperties()).thenReturn(workflowProperties);
        Mockito.when(workflowProperties.isListTasks()).thenReturn(false);

        String workflowType = "AM_APPLICATION_CREATION";
        String status = "CREATED";
        String tenantDomain = "carbon.super";

        APIAdmin apiAdmin = new APIAdminImpl();
        Workflow[] returnedWorkflows = apiAdmin.getworkflows(workflowType, status, tenantDomain);
        Assert.assertNotNull("Workflow array null", returnedWorkflows);
        Assert.assertEquals("Workflow array length mismatch", 0, returnedWorkflows.length);


    }
}
