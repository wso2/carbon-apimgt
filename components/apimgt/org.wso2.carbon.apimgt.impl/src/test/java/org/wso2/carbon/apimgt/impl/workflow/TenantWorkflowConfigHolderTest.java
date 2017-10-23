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

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * TenantWorkflowConfigHolder test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, TenantWorkflowConfigHolder.class})
public class TenantWorkflowConfigHolderTest {

    private int tenantID = -1234;
    private String tenantDomain = "carbon.super";
    private UserRegistry registry;

    @Before
    public void init() throws RegistryException {
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        registry = Mockito.mock(UserRegistry.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(Mockito.anyInt())).thenReturn(registry);
    }

    @Test
    public void testLoadingDefaultTenantWorkflowConfig() throws FileNotFoundException, XMLStreamException,
            RegistryException {
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        File defaultWFConfigFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("workflow-configs/default-workflow-extensions.xml").getFile());
        InputStream defaultWFConfigContent = new FileInputStream(defaultWFConfigFile);
        Resource defaultWFConfigResource = new ResourceImpl();
        defaultWFConfigResource.setContentStream(defaultWFConfigContent);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenReturn(defaultWFConfigResource);
        try {
            tenantWorkflowConfigHolder.load();
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_APPLICATION_CREATION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor
                    ("AM_APPLICATION_REGISTRATION_PRODUCTION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor
                    ("AM_APPLICATION_REGISTRATION_SANDBOX"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_USER_SIGNUP"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_SUBSCRIPTION_CREATION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_SUBSCRIPTION_DELETION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_API_STATE"));

        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while loading default tenant workflow configuration");
        }
    }

    @Test
    public void testLoadingExtendedTenantWorkflowConfig() throws FileNotFoundException, XMLStreamException,
            RegistryException {
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        File defaultWFConfigFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("workflow-configs/workflow-extensions.xml").getFile());
        InputStream defaultWFConfigContent = new FileInputStream(defaultWFConfigFile);
        Resource defaultWFConfigResource = new ResourceImpl();
        defaultWFConfigResource.setContentStream(defaultWFConfigContent);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenReturn(defaultWFConfigResource);
        try {
            tenantWorkflowConfigHolder.load();
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_APPLICATION_CREATION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor
                    ("AM_APPLICATION_REGISTRATION_PRODUCTION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor
                    ("AM_APPLICATION_REGISTRATION_SANDBOX"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_USER_SIGNUP"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_SUBSCRIPTION_CREATION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_SUBSCRIPTION_DELETION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_API_STATE"));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while loading extended tenant workflow configuration");
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenErrorWhileLoadingRegistryResource() throws FileNotFoundException,
            XMLStreamException, RegistryException {
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenThrow(new RegistryException("Error " +
                "loading Workflow Resource"));
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when registry resource loading failed");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error loading Resource from path" + APIConstants
                    .WORKFLOW_EXECUTOR_LOCATION);
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorClassNotFound() throws Exception {
        //Workflow executor is an non existing class so that ClassNotFoundException will be thrown
        String invalidWFExecutor =
                "<WorkFlowExtensions>\n" +
                "    <ApplicationCreation executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                ".TestExecutor\"/></WorkFlowExtensions>";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidWFExecutor.getBytes("UTF-8"));
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Resource defaultWFConfigResource = new ResourceImpl();
        defaultWFConfigResource.setContentStream(invalidInputStream);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenReturn(defaultWFConfigResource);
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor class not found");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Unable to find class");
        }
    }


    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorClassCannotBeInstantiated() throws Exception {
        //Workflow executor is an abstract class so that InstantiationException will be thrown
        String invalidWFExecutor =
                "<WorkFlowExtensions>\n" +
                "    <ApplicationCreation executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                ".WorkflowExecutor\"/></WorkFlowExtensions>";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidWFExecutor.getBytes("UTF-8"));
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Resource defaultWFConfigResource = new ResourceImpl();
        defaultWFConfigResource.setContentStream(invalidInputStream);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenReturn(defaultWFConfigResource);
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor class cannot be " +
                    "instantiate");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Unable to instantiate class");
        }
    }

}
