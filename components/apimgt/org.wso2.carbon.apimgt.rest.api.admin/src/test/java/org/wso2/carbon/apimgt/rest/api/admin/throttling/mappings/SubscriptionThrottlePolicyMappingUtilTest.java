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
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.RequestCountLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.SubscriptionThrottlePolicyMappingUtil;

import java.util.UUID;

public class SubscriptionThrottlePolicyMappingUtilTest {
    String uuid = UUID.randomUUID().toString();
    String name = "SampleSubscriptionPolicy";


    @Test(description = "Convert Subscription Throttle Policy to DTO")
    public void fromSubscriptionThrottlePolicyToDTOTest() throws Exception  {

        SubscriptionPolicy policy = new SubscriptionPolicy(uuid, name);
        SubscriptionThrottlePolicyDTO dto = SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyToDTO(policy);

        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getPolicyName(), name);
        Assert.assertEquals(dto.getPolicyId(), uuid);
    }

    @Test(description = "Convert Subscription Throttle Policy DTO to Model")
    public void fromSubscriptionThrottlePolicyDTOToModelTest() throws Exception {
        SubscriptionThrottlePolicyDTO dto = new SubscriptionThrottlePolicyDTO();
        dto.setRateLimitTimeUnit("m");
        dto.setRateLimitCount(1);
        dto.setStopOnQuotaReach(true);
        ThrottleLimitDTO throttleLimitDTO = new ThrottleLimitDTO();
        throttleLimitDTO.setType("RequestCountLimit");
        throttleLimitDTO.setTimeUnit("s");
        throttleLimitDTO.setUnitTime(1);
        RequestCountLimitDTO requestCountLimitDTO = new RequestCountLimitDTO();
        requestCountLimitDTO.setRequestCount(2);
        throttleLimitDTO.setRequestCountLimit(requestCountLimitDTO);
        dto.setDefaultLimit(throttleLimitDTO);
        SubscriptionPolicy policy = SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyDTOToModel(dto);
        Assert.assertNotNull(policy);
        Assert.assertEquals(policy.getRateLimitCount(), 1);
        Assert.assertEquals(policy.getRateLimitTimeUnit(), "m");
        Assert.assertEquals(policy.isStopOnQuotaReach(), true);
        Assert.assertEquals(policy.getDefaultQuotaPolicy().getType(), "requestCount");
        Assert.assertEquals(policy.getDefaultQuotaPolicy().getLimit().getTimeUnit(), dto.getDefaultLimit().getTimeUnit());
        Assert.assertEquals((Integer) policy.getDefaultQuotaPolicy().getLimit().getUnitTime(),
                dto.getDefaultLimit().getUnitTime());
        Assert.assertEquals((Integer)((RequestCountLimit)policy.getDefaultQuotaPolicy().getLimit()).getRequestCount(),
                dto.getDefaultLimit().getRequestCountLimit().getRequestCount());
    }

}
