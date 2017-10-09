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
package org.wso2.carbon.apimgt.impl.observers;

import org.apache.axis2.context.ConfigurationContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, PrivilegedCarbonContext.class })
public class SignupObserverTestCase {

    @Test
    public void testCreateConfigurationContext() throws APIManagementException {
        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("foo.com");
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(1234);

        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        PowerMockito.mockStatic(APIUtil.class);

        SignupObserver signupObserver = new SignupObserver();
        signupObserver.createdConfigurationContext(configurationContext);

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.createSelfSignUpRoles(1234);
    }

    @Test
    public void testCreateConfigurationContextException() throws Exception {
        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("foo.com");
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(1234);

        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.doThrow(new APIManagementException("error"))
                .when(APIUtil.class, "createSelfSignUpRoles", Matchers.eq(1234));
        SignupObserver signupObserver = new SignupObserver();
        signupObserver.createdConfigurationContext(configurationContext);
    }
}
