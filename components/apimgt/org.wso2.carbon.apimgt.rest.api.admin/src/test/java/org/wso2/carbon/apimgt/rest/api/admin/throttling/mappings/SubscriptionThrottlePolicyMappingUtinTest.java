/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.throttling.mappings;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.SubscriptionThrottlePolicyMappingUtil;

import java.util.UUID;

public class SubscriptionThrottlePolicyMappingUtinTest {

    @Test(description = "Convert Subscription Throttle Policy to DTO")
    public void fromSubscriptionThrottlePolicyToDTOTest() throws Exception  {
        SubscriptionPolicy policy = new SubscriptionPolicy(UUID.randomUUID().toString(),"SampleSubscriptionPolicy");

        SubscriptionThrottlePolicyDTO dto = SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyToDTO(policy);

        Assert.assertNotNull(dto);
    }

    @Test(description = "Convert Subscription Throttle Policy DTO to Model")
    public void fromSubscriptionThrottlePolicyDTOToModelTest() throws Exception {
        SubscriptionThrottlePolicyDTO dto = new SubscriptionThrottlePolicyDTO();
        dto.setRateLimitTimeUnit("m");
        dto.setRateLimitCount(1);
        dto.setStopOnQuotaReach(true);
        SubscriptionPolicy policy = SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyDTOToModel(dto);

        Assert.assertNotNull(policy);
    }

}
