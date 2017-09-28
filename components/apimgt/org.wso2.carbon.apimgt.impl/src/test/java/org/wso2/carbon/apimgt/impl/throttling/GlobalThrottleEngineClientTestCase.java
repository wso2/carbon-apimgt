/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.throttling;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationException;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;

import java.rmi.RemoteException;

public class GlobalThrottleEngineClientTestCase {

    private final String SESSION_COOKIE = "abcd-efgh";
    private final String USER_NAME = "john";
    private final String PASSWORD = "password";
    private final String HOST = "foo.com";
    private final String EX_PLAN = "ex-plan";
    private final String EX_PLAN_WRONG = "ex-plan-wrong";
    private final String EX_PLAN_NAME = "ex-plan-name";
    private ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
    private OperationContext operationContext = Mockito.mock(OperationContext.class);
    private ServiceContext serviceContext = Mockito.mock(ServiceContext.class);
    private AuthenticationAdminStub authenticationAdminStub = Mockito.mock(AuthenticationAdminStub.class);
    private EventProcessorAdminServiceStub eventProcessorAdminServiceStub =  Mockito.mock(EventProcessorAdminServiceStub.class);
    private Options options = Mockito.mock(Options.class);
    private ThrottleProperties.PolicyDeployer policyDeployer = Mockito.mock(ThrottleProperties.PolicyDeployer.class);


    @Before
    public void init() throws Exception {
        Mockito.when(serviceClient.getLastOperationContext()).thenReturn(operationContext);
        Mockito.when(operationContext.getServiceContext()).thenReturn(serviceContext);
        Mockito.when(serviceContext.getProperty(HTTPConstants.COOKIE_STRING)).thenReturn(SESSION_COOKIE);
        Mockito.when(authenticationAdminStub.login(USER_NAME,PASSWORD,HOST)).thenReturn(true);
        Mockito.when(authenticationAdminStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(eventProcessorAdminServiceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceClient.getOptions()).thenReturn(options);
        Mockito.when(eventProcessorAdminServiceStub.validateExecutionPlan(EX_PLAN)).thenReturn("success");
        Mockito.when(eventProcessorAdminServiceStub.validateExecutionPlan(EX_PLAN_WRONG)).thenReturn("error");
        Mockito.when(policyDeployer.getUsername()).thenReturn(USER_NAME);
        Mockito.when(policyDeployer.getPassword()).thenReturn(PASSWORD);
    }

    @Test
    public void testValidateCorrectExecutionPlan() throws Exception {
        GlobalThrottleEngineClient globalThrottleEngineClient = new GlobalThrottleEngineClientWrapper(authenticationAdminStub, eventProcessorAdminServiceStub, policyDeployer);
        boolean validity= globalThrottleEngineClient.validateExecutionPlan(EX_PLAN);
        Mockito.verify(authenticationAdminStub, Mockito.atLeastOnce()).login(USER_NAME, PASSWORD, HOST);
        Assert.assertTrue(validity);
    }

    @Test
    public void testValidateWrongExecutionPlan() throws Exception {
        GlobalThrottleEngineClient globalThrottleEngineClient = new GlobalThrottleEngineClientWrapper(authenticationAdminStub, eventProcessorAdminServiceStub, policyDeployer);
        boolean validity= globalThrottleEngineClient.validateExecutionPlan(EX_PLAN_WRONG);
        Mockito.verify(authenticationAdminStub, Mockito.atLeastOnce()).login(USER_NAME, PASSWORD, HOST);
        Assert.assertFalse(validity);
    }

    @Test
    public void testRemoteExceptionWhenValidateExPlan() throws Exception{
        GlobalThrottleEngineClient globalThrottleEngineClient = new GlobalThrottleEngineClientWrapper(authenticationAdminStub, eventProcessorAdminServiceStub, policyDeployer);
        Mockito.when(eventProcessorAdminServiceStub.validateExecutionPlan(EX_PLAN_WRONG)).thenThrow(RemoteException.class);
        boolean validity= globalThrottleEngineClient.validateExecutionPlan(EX_PLAN_WRONG);
        Mockito.verify(authenticationAdminStub, Mockito.atLeastOnce()).login(USER_NAME, PASSWORD, HOST);
        Assert.assertFalse(validity);
    }

    @Test
    public void testLoginAuthenticationExceptionWhenLogin() throws Exception{
        GlobalThrottleEngineClient globalThrottleEngineClient = new GlobalThrottleEngineClientWrapper(authenticationAdminStub, eventProcessorAdminServiceStub, policyDeployer);
        Mockito.when(authenticationAdminStub.login(USER_NAME,PASSWORD,HOST)).thenThrow(LoginAuthenticationExceptionException.class);
        boolean validity= globalThrottleEngineClient.validateExecutionPlan(EX_PLAN_WRONG);
        Mockito.verify(authenticationAdminStub, Mockito.atLeastOnce()).login(USER_NAME, PASSWORD, HOST);
        Assert.assertFalse(validity);
    }

    @Test
    public void testDeployExecutionPlan() throws Exception {
        GlobalThrottleEngineClient globalThrottleEngineClient = new GlobalThrottleEngineClientWrapper(authenticationAdminStub, eventProcessorAdminServiceStub, policyDeployer);
        globalThrottleEngineClient.deployExecutionPlan(EX_PLAN);
        Mockito.verify(authenticationAdminStub, Mockito.atLeastOnce()).login(USER_NAME, PASSWORD, HOST);
        Mockito.verify(eventProcessorAdminServiceStub, Mockito.times(1)).deployExecutionPlan(EX_PLAN);

    }

    @Test
    public void testLogoutAuthenticationExceptionWhenDeployingExPlan() throws Exception{
        GlobalThrottleEngineClient globalThrottleEngineClient = new GlobalThrottleEngineClientWrapper(authenticationAdminStub, eventProcessorAdminServiceStub, policyDeployer);
        Mockito.doThrow(LogoutAuthenticationExceptionException.class).when(authenticationAdminStub).logout();
        globalThrottleEngineClient.deployExecutionPlan(EX_PLAN);
        Mockito.verify(authenticationAdminStub, Mockito.atLeastOnce()).login(USER_NAME, PASSWORD, HOST);
        Mockito.verify(eventProcessorAdminServiceStub, Mockito.times(1)).deployExecutionPlan(EX_PLAN);
    }

    @Test
    public void testLogoutAuthenticationExceptionWhenUpdatingExPlan() throws Exception{
        GlobalThrottleEngineClient globalThrottleEngineClient = new GlobalThrottleEngineClientWrapper(authenticationAdminStub, eventProcessorAdminServiceStub, policyDeployer);
        Mockito.doThrow(LogoutAuthenticationExceptionException.class).when(authenticationAdminStub).logout();
        globalThrottleEngineClient.updateExecutionPlan(EX_PLAN_NAME, EX_PLAN);
        Mockito.verify(authenticationAdminStub, Mockito.atLeastOnce()).login(USER_NAME, PASSWORD, HOST);
        Mockito.verify(eventProcessorAdminServiceStub, Mockito.times(1)).editActiveExecutionPlan(EX_PLAN, EX_PLAN_NAME);
    }

    @Test
    public void testUpdateExecutionPlan() throws Exception {
        GlobalThrottleEngineClient globalThrottleEngineClient = new GlobalThrottleEngineClientWrapper(authenticationAdminStub, eventProcessorAdminServiceStub, policyDeployer);
        globalThrottleEngineClient.updateExecutionPlan(EX_PLAN_NAME, EX_PLAN);
        Mockito.verify(authenticationAdminStub, Mockito.atLeastOnce()).login(USER_NAME, PASSWORD, HOST);
        Mockito.verify(eventProcessorAdminServiceStub, Mockito.times(1)).editActiveExecutionPlan(EX_PLAN, EX_PLAN_NAME);
    }

    @Test
    public void testDeleteExecutionPlan() throws Exception {
        GlobalThrottleEngineClient globalThrottleEngineClient = new GlobalThrottleEngineClientWrapper(authenticationAdminStub, eventProcessorAdminServiceStub, policyDeployer);
        globalThrottleEngineClient.deleteExecutionPlan(EX_PLAN_NAME);
        Mockito.verify(authenticationAdminStub, Mockito.atLeastOnce()).login(USER_NAME, PASSWORD, HOST);
        Mockito.verify(eventProcessorAdminServiceStub, Mockito.times(1)).undeployActiveExecutionPlan(EX_PLAN_NAME);
    }
}
