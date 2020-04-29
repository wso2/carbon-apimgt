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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
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
import java.lang.reflect.Constructor;
import java.util.List;

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

    @Test
    public void testFailureToLoadTenantWFConfigWhenXMLStreamExceptionOccurredWhileParsingConfig() throws Exception {
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        File defaultWFConfigFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("workflow-configs/workflow-extensions.xml").getFile());
        InputStream defaultWFConfigContent = new FileInputStream(defaultWFConfigFile);
        Resource defaultWFConfigResource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenReturn(defaultWFConfigResource);
        Mockito.when(defaultWFConfigResource.getContentStream()).thenReturn(defaultWFConfigContent);
        //XMLStreamException will be thrown while building workflow config
        PowerMockito.whenNew(StAXOMBuilder.class).withArguments(defaultWFConfigContent).thenThrow(new
                XMLStreamException(""));
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when XMLStreamException occurred while " +
                    "processing workflow config");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Error building xml from Resource at " + APIConstants
                    .WORKFLOW_EXECUTOR_LOCATION);
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorClassCannotAccessible() throws Exception {
        //Workflow executor class is a singleton class with private constructor, so that IllegalAccessException will
        // be thrown while instantiation
        String invalidWFExecutor =
                "<WorkFlowExtensions>\n" +
                        "    <ApplicationCreation executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".InvalidWorkFlowExecutor1\"/></WorkFlowExtensions>";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidWFExecutor.getBytes("UTF-8"));
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Resource defaultWFConfigResource = new ResourceImpl();
        defaultWFConfigResource.setContentStream(invalidInputStream);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenReturn(defaultWFConfigResource);
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor class cannot be " +
                    "accessible");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Illegal attempt to invoke class methods");
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorPropertyNameNotFound() throws Exception {
        //Workflow executor class is a singleton class with private constructor, so that IllegalAccessException will
        // be thrown while instantiation
        String invalidWFExecutor =
                "<WorkFlowExtensions>\n" +
                        "     <ApplicationCreation executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".ApplicationCreationWSWorkflowExecutor\">\n" +
                        "         <Property/>\n" +
                        "    </ApplicationCreation>\n" +
                        "</WorkFlowExtensions>\n";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidWFExecutor.getBytes("UTF-8"));
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Resource defaultWFConfigResource = new ResourceImpl();
        defaultWFConfigResource.setContentStream(invalidInputStream);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenReturn(defaultWFConfigResource);
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor property 'name' " +
                    "attribute not found");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Unable to load workflow executor class");
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorPropertySetterNotDefined() throws Exception {
        //Workflow executor class does not have setter method for 'testParam'
        String invalidWFExecutor =
                "<WorkFlowExtensions>\n" +
                        "     <ApplicationCreation executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".ApplicationCreationWSWorkflowExecutor\">\n" +
                        "         <Property name=\"testParam\">test</Property>\n" +
                        "    </ApplicationCreation>\n" +
                        "</WorkFlowExtensions>\n";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidWFExecutor.getBytes("UTF-8"));
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Resource defaultWFConfigResource = new ResourceImpl();
        defaultWFConfigResource.setContentStream(invalidInputStream);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenReturn(defaultWFConfigResource);
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor property setter method" +
                    " cannot be found");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Unable to load workflow executor class");
            Assert.assertEquals(e.getCause().getMessage(), "Error invoking setter method named : setTestParam() " +
                    "that takes a single String, int, long, float, double or boolean parameter");
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorPropertySetterInInvalid() throws Exception {
        //Workflow executor class setter method is invalid since it has multiple parameter types
        String invalidWFExecutor =
                "<WorkFlowExtensions>\n" +
                        "     <ApplicationCreation executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".InvalidWorkFlowExecutor2\">\n" +
                        "         <Property name=\"username\">admin</Property>\n" +
                        "    </ApplicationCreation>\n" +
                        "</WorkFlowExtensions>\n";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidWFExecutor.getBytes("UTF-8"));
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Resource defaultWFConfigResource = new ResourceImpl();
        defaultWFConfigResource.setContentStream(invalidInputStream);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenReturn(defaultWFConfigResource);
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor property setter method" +
                    " is invalid");
        } catch (WorkflowException e) {
            Assert.assertEquals(e.getMessage(), "Unable to load workflow executor class");
            Assert.assertEquals(e.getCause().getMessage(), "Error invoking setter method named : setUsername() " +
                    "that takes a single String, int, long, float, double or boolean parameter");
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorHasMultipleParamTypes() throws Exception {
        //Workflow executor class setter methods are available for different parameter types
        String invalidWFExecutor =
                "<WorkFlowExtensions>\n" +
                        "     <ApplicationCreation executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".WorkflowExecutorWithMultipleParamTypes\">\n" +
                        "         <Property name=\"stringParam\">admin</Property>\n" +
                        "         <Property name=\"intParam\">1</Property>\n" +
                        "         <Property name=\"booleanParam\">true</Property>\n" +
                        "         <Property name=\"longParam\">10000000</Property>\n" +
                        "         <Property name=\"doubleParam\">10.1000000000</Property>\n" +
                        "         <Property name=\"floatParam\">10.1</Property>\n" +
                        "         <Property name=\"omElement\">" +
                        "            <omElement>test</omElement>" +
                        "         </Property>\n" +
                        "    </ApplicationCreation>\n" +
                        "    <ProductionApplicationRegistration executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".ApplicationRegistrationSimpleWorkflowExecutor\"/>" +
                        "    <SandboxApplicationRegistration executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".ApplicationRegistrationSimpleWorkflowExecutor\"/>\n" +
                        "    <SubscriptionCreation executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".SubscriptionCreationSimpleWorkflowExecutor\"/>\n"+
                        "   <SubscriptionUpdate executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".SubscriptionUpdateSimpleWorkflowExecutor\"/>\n"+
                        "    <UserSignUp executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".UserSignUpSimpleWorkflowExecutor\"/>\n"+
                        "    <SubscriptionDeletion executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".SubscriptionDeletionSimpleWorkflowExecutor\"/>\n"+
                        "    <ApplicationDeletion executor=\"org.wso2.carbon.apimgt.impl.workflow" +
                        ".ApplicationDeletionSimpleWorkflowExecutor\"/>\n"+
                        "</WorkFlowExtensions>\n";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidWFExecutor.getBytes("UTF-8"));
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Resource defaultWFConfigResource = new ResourceImpl();
        defaultWFConfigResource.setContentStream(invalidInputStream);
        Mockito.when(registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION)).thenReturn(defaultWFConfigResource);
        try {
            tenantWorkflowConfigHolder.load();
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_APPLICATION_CREATION"));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException has been thrown while loading workflow executor for different " +
                    "param types");
        }
    }
}

/**
 * This WorkflowExecutor is a singleton class and cannot be instantiated
 */
class InvalidWorkFlowExecutor1 {
    private InvalidWorkFlowExecutor1() {
    }
}

/**
 * This WorkflowExecutor has invalid setter method with multiple input types
 */
class InvalidWorkFlowExecutor2 extends WorkflowExecutor{

    public void setUsername(String username, int id){
    }

    @Override
    public String getWorkflowType() {
        return null;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }
}

class WorkflowExecutorWithMultipleParamTypes extends WorkflowExecutor{

    @Override
    public String getWorkflowType() {
        return null;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    public void setStringParam(String stringParam){}
    public void setIntParam(int intParam){}
    public void setLongParam(long longParam){}
    public void setFloatParam(float floatParam){}
    public void setDoubleParam(double doubleParam){}
    public void setBooleanParam(boolean booleanParam){}
    public void setOmElement(OMElement omElement){}
}
