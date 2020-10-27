/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.apimgt.throttle.policy.deployer.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApiPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApplicationPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.GlobalPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.internal.ServiceReferenceHolder;

import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class ThrottlingPolicyTemplateBuilderTest {

    private ThrottlePolicyTemplateBuilder templateBuilder;

    @Before
    public void setUp() {
        System.setProperty("carbon.home", ThrottlingPolicyTemplateBuilderTest.class.getResource("/").getFile());
        templateBuilder = new ThrottlePolicyTemplateBuilder();

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.VELOCITY_LOGGER)).
                thenReturn("not-defined");
        Mockito.when(serviceReferenceHolder.getAPIMConfiguration()).
                thenReturn(apiManagerConfiguration);
    }

    @Test
    public void testGetThrottlePolicyForAPILevel() throws Exception {
        ApiPolicy policy = TestUtil.getPolicyAPILevel();

        Map<String, String> policyStringArray = templateBuilder.getThrottlePolicyForAPILevel(policy);
        Assert.assertEquals(1, policyStringArray.size());

        String defaultPolicyString = templateBuilder.getThrottlePolicyForAPILevelDefault(policy);
        Assert.assertNotNull(defaultPolicyString);
    }

    @Test
    public void testGetThrottlePolicyForGlobalLevel() throws Exception {
        GlobalPolicy policy = TestUtil.getPolicyGlobalLevel();
        String policyString = templateBuilder.getThrottlePolicyForGlobalLevel(policy);
        Assert.assertNotNull(policyString);
    }

    @Test
    public void testGetThrottlePolicyForAppLevel() throws Exception {
        ApplicationPolicy policy = TestUtil.getPolicyAppLevel();
        String policyString = templateBuilder.getThrottlePolicyForAppLevel(policy);
        Assert.assertNotNull(policyString);
    }

    @Test
    public void testGetThrottlePolicyForSubscriptionLevel() throws Exception {
        SubscriptionPolicy policy = TestUtil.getPolicySubLevel();
        String policyString = templateBuilder.getThrottlePolicyForSubscriptionLevel(policy);
        Assert.assertNotNull(policyString);
    }

}
