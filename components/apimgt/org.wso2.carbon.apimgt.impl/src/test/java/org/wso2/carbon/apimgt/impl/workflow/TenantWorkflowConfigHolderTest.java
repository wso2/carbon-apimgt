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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.dto.WorkflowConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.IOException;
import java.util.List;

/**
 * TenantWorkflowConfigHolder test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, TenantWorkflowConfigHolder.class})
public class TenantWorkflowConfigHolderTest {
    private static final Log log = LogFactory.getLog(TenantWorkflowConfigHolderTest.class);

    private int tenantID = -1234;
    private String tenantDomain = "carbon.super";
    private APIMConfigService apimConfigService = Mockito.mock(APIMConfigService.class);

    @Before
    public void init() throws RegistryException {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getApimConfigService()).thenReturn(apimConfigService);
    }

    @Test
    public void testLoadingDefaultTenantWorkflowConfig() throws APIManagementException {
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(apimConfigService.getWorkFlowConfig(tenantDomain)).thenReturn(new WorkflowConfigDTO());
        try {
            tenantWorkflowConfigHolder.load();
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_APPLICATION_CREATION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_APPLICATION_DELETION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor
                    ("AM_APPLICATION_REGISTRATION_PRODUCTION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor
                    ("AM_APPLICATION_REGISTRATION_SANDBOX"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_USER_SIGNUP"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_SUBSCRIPTION_CREATION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_SUBSCRIPTION_UPDATE"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_SUBSCRIPTION_DELETION"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_API_STATE"));
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_API_PRODUCT_STATE"));

        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while loading default tenant workflow configuration");
        }
    }

    @Test
    public void testLoadingApprovalTenantWorkflowConfig() throws IOException, APIManagementException {
        JsonObject WFConfig = JsonParser.parseString("{\n" +
                "  \"Workflows\": {\n" +
                "    \"ApplicationCreation\": {},\n" +
                "    \"ProductionApplicationRegistration\": {},\n" +
                "    \"SandboxApplicationRegistration\": {},\n" +
                "    \"SubscriptionCreation\": {},\n" +
                "    \"SubscriptionUpdate\": {},\n" +
                "    \"UserSignUp\": {},\n" +
                "    \"SubscriptionDeletion\": {},\n" +
                "    \"ApplicationDeletion\": {},\n" +
                "    \"APIStateChange\": {},\n" +
                "    \"APIProductStateChange\": {}\n" +
                "  }\n" +
                "}").getAsJsonObject();

        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(apimConfigService.getWorkFlowConfig(tenantDomain))
                .thenReturn(WorkflowTestUtils.getWorkFlowConfigDTOFromJsonConfig(WFConfig));
        try {
            tenantWorkflowConfigHolder.load();
            Assert.assertEquals("org.wso2.carbon.apimgt.impl.workflow.ApplicationCreationApprovalWorkflowExecutor",
                    tenantWorkflowConfigHolder.getWorkflowExecutor("AM_APPLICATION_CREATION").getClass().getName());
            Assert.assertEquals("org.wso2.carbon.apimgt.impl.workflow.ApplicationDeletionApprovalWorkflowExecutor",
                    tenantWorkflowConfigHolder.getWorkflowExecutor("AM_APPLICATION_DELETION").getClass().getName());
            Assert.assertEquals("org.wso2.carbon.apimgt.impl.workflow.ApplicationRegistrationApprovalWorkflowExecutor",
                    tenantWorkflowConfigHolder.getWorkflowExecutor("AM_APPLICATION_REGISTRATION_PRODUCTION").getClass().getName());
            Assert.assertEquals("org.wso2.carbon.apimgt.impl.workflow.ApplicationRegistrationApprovalWorkflowExecutor",
                    tenantWorkflowConfigHolder.getWorkflowExecutor("AM_APPLICATION_REGISTRATION_SANDBOX").getClass().getName());
            Assert.assertEquals("org.wso2.carbon.apimgt.impl.workflow.UserSignUpApprovalWorkflowExecutor",
                    tenantWorkflowConfigHolder.getWorkflowExecutor("AM_USER_SIGNUP").getClass().getName());
            Assert.assertEquals("org.wso2.carbon.apimgt.impl.workflow.SubscriptionCreationApprovalWorkflowExecutor",
                    tenantWorkflowConfigHolder.getWorkflowExecutor("AM_SUBSCRIPTION_CREATION").getClass().getName());
            Assert.assertEquals("org.wso2.carbon.apimgt.impl.workflow.SubscriptionUpdateApprovalWorkflowExecutor",
                    tenantWorkflowConfigHolder.getWorkflowExecutor("AM_SUBSCRIPTION_UPDATE").getClass().getName());
            Assert.assertEquals("org.wso2.carbon.apimgt.impl.workflow.SubscriptionDeletionApprovalWorkflowExecutor",
                    tenantWorkflowConfigHolder.getWorkflowExecutor("AM_SUBSCRIPTION_DELETION").getClass().getName());
            Assert.assertEquals("org.wso2.carbon.apimgt.impl.workflow.APIStateChangeApprovalWorkflowExecutor",
                    tenantWorkflowConfigHolder.getWorkflowExecutor("AM_API_STATE").getClass().getName());
            Assert.assertEquals("org.wso2.carbon.apimgt.impl.workflow.APIProductStateChangeApprovalWorkflowExecutor",
                    tenantWorkflowConfigHolder.getWorkflowExecutor("AM_API_PRODUCT_STATE").getClass().getName());
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while loading approval tenant workflow configuration");
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorClassNotFound() throws Exception {
        //For signup workflow we set TestUserSignUpSimpleWorkflowExecutor. since it is not there, default signup executor should be used.
        JsonObject invalidWFExecutor = JsonParser.parseString("{\n" +
                "    \"Workflows\": {\n" +
                "        \"UserSignUp\": {\n" +
                "            \"Class\": \"TestUserSignUpSimpleWorkflowExecutor\"\n" +
                "        }\n" +
                "    }\n" +
                "}").getAsJsonObject();
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(apimConfigService.getWorkFlowConfig(tenantDomain))
                .thenReturn(WorkflowTestUtils.getWorkFlowConfigDTOFromJsonConfig(invalidWFExecutor));
        tenantWorkflowConfigHolder.load();
        WorkflowExecutor executor = tenantWorkflowConfigHolder.getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);
        Assert.assertEquals("Default class is not loaded for missing class",
                "org.wso2.carbon.apimgt.impl.workflow.UserSignUpSimpleWorkflowExecutor", executor.getClass().getName());

    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorClassCannotBeInstantiated() throws Exception {
        //Workflow executor is an abstract class so that InstantiationException will be thrown
        JsonObject invalidWFExecutor = JsonParser.parseString("{\n" +
                "    \"Workflows\": {\n" +
                "        \"ApplicationCreation\": {\n" +
                "            \"Class\": \"org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor\"\n" +
                "        }\n" +
                "    }\n" +
                "}").getAsJsonObject();
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(apimConfigService.getWorkFlowConfig(tenantDomain))
                .thenReturn(WorkflowTestUtils.getWorkFlowConfigDTOFromJsonConfig(invalidWFExecutor));
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor class cannot be " +
                    "instantiate");
        } catch (WorkflowException e) {
            Assert.assertEquals("Unable to instantiate class", e.getMessage());
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorClassCannotAccessible() throws Exception {
        //Workflow executor class is a singleton class with private constructor, so that IllegalAccessException will
        // be thrown while instantiation
        JsonObject invalidWFExecutor = JsonParser.parseString("{\n" +
                "    \"Workflows\": {\n" +
                "        \"ApplicationCreation\": {\n" +
                "            \"Class\": \"org.wso2.carbon.apimgt.impl.workflow.InvalidWorkFlowExecutor1\"\n" +
                "        }\n" +
                "    }\n" +
                "}").getAsJsonObject();
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(apimConfigService.getWorkFlowConfig(tenantDomain))
                .thenReturn(WorkflowTestUtils.getWorkFlowConfigDTOFromJsonConfig(invalidWFExecutor));
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor class cannot be " +
                    "accessible");
        } catch (WorkflowException e) {
            Assert.assertEquals("Illegal attempt to invoke class methods", e.getMessage());
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorPropertyNameEmpty() throws Exception {
        // One of the property names is empty, this will throw WorkflowException
        JsonObject invalidWFExecutor = JsonParser.parseString("{\n" +
                "    \"Workflows\": {\n" +
                "        \"UserSignUp\": {\n" +
                "            \"Class\": \"org.wso2.carbon.apimgt.impl.workflow.ApplicationCreationApprovalWorkflowExecutor\",\n" +
                "            \"Properties\": {\n" +
                "                \"\": \"xxx\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}").getAsJsonObject();
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(apimConfigService.getWorkFlowConfig(tenantDomain))
                .thenReturn(WorkflowTestUtils.getWorkFlowConfigDTOFromJsonConfig(invalidWFExecutor));
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor property name " +
                    "is empty");
        } catch (WorkflowException e) {
            Assert.assertEquals("An Executor class property must specify a name.", e.getMessage());
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorPropertySetterNotDefined() throws Exception {
        //Workflow executor class does not have setter method for 'TestParam'
        JsonObject invalidWFExecutor = JsonParser.parseString("{\n" +
                "    \"Workflows\": {\n" +
                "        \"UserSignUp\": {\n" +
                "            \"Class\": \"org.wso2.carbon.apimgt.impl.workflow.ApplicationCreationApprovalWorkflowExecutor\",\n" +
                "            \"Properties\": {\n" +
                "                \"TestParam\": \"xxx\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}").getAsJsonObject();
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(apimConfigService.getWorkFlowConfig(tenantDomain))
                .thenReturn(WorkflowTestUtils.getWorkFlowConfigDTOFromJsonConfig(invalidWFExecutor));
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor property setter method" +
                    " cannot be found");
        } catch (WorkflowException e) {
            Assert.assertEquals("Error invoking setter method named : setTestParam() that takes a single String, " +
                    "int, long, float, double or boolean parameter", e.getMessage());
            Assert.assertEquals("Did not find a setter method named : setTestParam() that takes a single String, " +
                    "int, long, float, double or boolean parameter", e.getCause().getMessage());
        }
    }

    @Test
    public void testFailureToLoadTenantWFConfigWhenWFExecutorPropertySetterInInvalid() throws Exception {
        //Workflow executor class setter method is invalid since it has multiple parameter types
        JsonObject invalidWFExecutor = JsonParser.parseString("{\n" +
                "    \"Workflows\": {\n" +
                "        \"UserSignUp\": {\n" +
                "            \"Class\": \"org.wso2.carbon.apimgt.impl.workflow.InvalidWorkFlowExecutor2\",\n" +
                "            \"Properties\": {\n" +
                "                \"Username\": \"admin\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}").getAsJsonObject();
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(apimConfigService.getWorkFlowConfig(tenantDomain))
                .thenReturn(WorkflowTestUtils.getWorkFlowConfigDTOFromJsonConfig(invalidWFExecutor));
        try {
            tenantWorkflowConfigHolder.load();
            Assert.fail("Expected WorkflowException has not been thrown when workflow executor property setter method" +
                    " is invalid");
        } catch (WorkflowException e) {
            Assert.assertEquals("Error invoking setter method named : setUsername() " +
                    "that takes a single String, int, long, float, double or boolean parameter", e.getMessage());
        }
    }

    /**
     * This method tests the correct loading of properties where property values can have
     * different types, such as string, int, double, etc.
     */
    @Test
    public void testLoadingTenantWFConfigWhenWFExecutorHasMultipleParamTypes() throws Exception {
        //Workflow executor class setter methods are available for different parameter types
        JsonObject WFExecutor = JsonParser.parseString("{\n" +
                "    \"Workflows\": {\n" +
                "        \"UserSignUp\": {\n" +
                "            \"Class\": \"org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorWithMultipleParamTypes\",\n" +
                "            \"Properties\": {\n" +
                "                \"StringParam\": \"admin\",\n" +
                "                \"IntParam\": 1,\n" +
                "                \"BooleanParam\": true,\n" +
                "                \"LongParam\": 10000000,\n" +
                "                \"DoubleParam\": 10.1000000000,\n" +
                "                \"FloatParam\": 10.1\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}").getAsJsonObject();
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(apimConfigService.getWorkFlowConfig(tenantDomain))
                .thenReturn(WorkflowTestUtils.getWorkFlowConfigDTOFromJsonConfig(WFExecutor));
        try {
            tenantWorkflowConfigHolder.load();
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_USER_SIGNUP"));
            WorkflowExecutorWithMultipleParamTypes executor = (WorkflowExecutorWithMultipleParamTypes) tenantWorkflowConfigHolder.getWorkflowExecutor("AM_USER_SIGNUP");
            Assert.assertEquals("admin", executor.stringParam);
            Assert.assertEquals((Integer) 1, executor.intParam);
            Assert.assertEquals(true, executor.boolParam);
            Assert.assertEquals(10000000, executor.longParam);
            Assert.assertEquals((Double) 10.1000000000, executor.doubleParam);
            Assert.assertEquals((Float) 10.1f, executor.floatParam);


        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException has been thrown while loading workflow executor for different " +
                    "param types");
        }
    }

    /**
     * This method tests the correct loading of properties when all property values are specified as strings.
     * This supports proper migration of workflow-extensions properties
     */
    @Test
    public void testLoadingTenantWFExecutorWithMultipleParamTypesWhenAllPropValuesAreSpecifiedAsStrings() throws Exception {
        //Workflow executor class setter methods are available for different parameter types
        JsonObject WFExecutor = JsonParser.parseString("{\n" +
                "    \"Workflows\": {\n" +
                "        \"UserSignUp\": {\n" +
                "            \"Class\": \"org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorWithMultipleParamTypes\",\n" +
                "            \"Properties\": {\n" +
                "                \"StringParam\": \"admin\",\n" +
                "                \"IntParam\": \"1\",\n" +
                "                \"BooleanParam\": \"true\",\n" +
                "                \"LongParam\": \"10000000\",\n" +
                "                \"DoubleParam\": \"10.1000000000\",\n" +
                "                \"FloatParam\": \"10.1\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}").getAsJsonObject();
        TenantWorkflowConfigHolder tenantWorkflowConfigHolder = new TenantWorkflowConfigHolder(tenantDomain, tenantID);
        Mockito.when(apimConfigService.getWorkFlowConfig(tenantDomain))
                .thenReturn(WorkflowTestUtils.getWorkFlowConfigDTOFromJsonConfig(WFExecutor));
        try {
            tenantWorkflowConfigHolder.load();
            Assert.assertNotNull(tenantWorkflowConfigHolder.getWorkflowExecutor("AM_USER_SIGNUP"));
            WorkflowExecutorWithMultipleParamTypes executor = (WorkflowExecutorWithMultipleParamTypes) tenantWorkflowConfigHolder.getWorkflowExecutor("AM_USER_SIGNUP");
            Assert.assertEquals("admin", executor.stringParam);
            Assert.assertEquals((Integer) 1, executor.intParam);
            Assert.assertEquals(true, executor.boolParam);
            Assert.assertEquals(10000000, executor.longParam);
            Assert.assertEquals((Double) 10.1000000000, executor.doubleParam);
            Assert.assertEquals((Float) 10.1f, executor.floatParam);
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

class WorkflowExecutorWithMultipleParamTypes extends WorkflowExecutor {
    public String stringParam;
    public Integer intParam;
    public Float floatParam;
    public Double doubleParam;
    public Boolean boolParam;
    public long longParam;
    public OMElement omElement;
    public JsonElement jsonElement;


    @Override
    public String getWorkflowType() {
        return null;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    public void setStringParam(String stringParam) {
        this.stringParam = stringParam;
    }

    public void setIntParam(int intParam) {
        this.intParam = intParam;
    }

    public void setLongParam(long longParam) {
        this.longParam = longParam;
    }

    public void setFloatParam(float floatParam) {
        this.floatParam = floatParam;
    }

    public void setDoubleParam(double doubleParam) {
        this.doubleParam = doubleParam;
    }

    public void setBooleanParam(boolean boolParam) {
        this.boolParam = boolParam;
    }

    public void setOMElementParam(OMElement omElement) {
        this.omElement = omElement;
    }

    public void setJsonElement(JsonElement jsonElement) {
        this.jsonElement = jsonElement;
    }
}
