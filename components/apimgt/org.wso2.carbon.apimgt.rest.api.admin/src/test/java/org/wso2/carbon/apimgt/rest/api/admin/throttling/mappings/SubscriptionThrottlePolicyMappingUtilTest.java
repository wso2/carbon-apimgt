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
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.RequestCountLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.SubscriptionThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.throttling.common.SampleTestObjectCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.apimgt.core.models.policy.PolicyConstants.REQUEST_COUNT_TYPE;

public class SubscriptionThrottlePolicyMappingUtilTest {
    String uuid = UUID.randomUUID().toString();
    String name = "SampleSubscriptionPolicy";


    @Test(description = "Convert Subscription Throttle Policy to DTO")
    public void fromSubscriptionThrottlePolicyToDTOTest() throws Exception  {

        SubscriptionPolicy policy = new SubscriptionPolicy(uuid, name);
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        RequestCountLimit requestCountLimit = new RequestCountLimit("s", 1000, 10000);
        quotaPolicy.setLimit(requestCountLimit);
        quotaPolicy.setType(REQUEST_COUNT_TYPE);
        policy.setDefaultQuotaPolicy(quotaPolicy);
        policy.setCustomAttributes("[{\"name\":\"dwd\",\"value\":\"wdw\"},{\"name\":\"dwdw\",\"value\":\"dwdw\"}]".getBytes());
        SubscriptionThrottlePolicyDTO dto = SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyToDTO(policy);

        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getPolicyName(), name);
        Assert.assertEquals(dto.getId(), uuid);
        Assert.assertEquals(dto.getDefaultLimit().getRequestCountLimit().getRequestCount().intValue(),
                requestCountLimit.getRequestCount());
        Assert.assertEquals(dto.getCustomAttributes().get(0).getName(), "dwd");
        Assert.assertEquals(dto.getCustomAttributes().get(1).getName(), "dwdw");

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
        List<CustomAttributeDTO> customAttributeDTOs = new ArrayList<>();
        CustomAttributeDTO customAttributeDTO1 = new CustomAttributeDTO();
        customAttributeDTO1.setName("ABC");
        customAttributeDTO1.setValue("ABCVALUE");
        CustomAttributeDTO customAttributeDTO2 = new CustomAttributeDTO();
        customAttributeDTO2.setName("CDE");
        customAttributeDTO2.setValue("CDEVALUE");
        customAttributeDTOs.add(customAttributeDTO1);
        customAttributeDTOs.add(customAttributeDTO2);
        dto.setCustomAttributes(customAttributeDTOs);
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
        Assert.assertTrue(new String(policy.getCustomAttributes()).contains("ABC"));
        Assert.assertTrue(new String(policy.getCustomAttributes()).contains("ABCVALUE"));
        Assert.assertTrue(new String(policy.getCustomAttributes()).contains("CDE"));
        Assert.assertTrue(new String(policy.getCustomAttributes()).contains("CDEVALUE"));

    }

    @Test(description = "Convert Subscription Throttle Policy List to List DTO")
    public void fromSubscriptionPolicyArrayToListDTOTest() throws Exception {
        List<SubscriptionPolicy> subscriptionPolicies = new ArrayList<>();
        SubscriptionPolicy policy1  = SampleTestObjectCreator.createSubscriptionPolicyWithRequestLimit("Gold1");
        SubscriptionPolicy policy2  = SampleTestObjectCreator.createSubscriptionPolicyWithRequestLimit("Gold2");
        subscriptionPolicies.add(policy1);
        subscriptionPolicies.add(policy2);
        SubscriptionThrottlePolicyListDTO listDTO = SubscriptionThrottlePolicyMappingUtil
                                                        .fromSubscriptionPolicyArrayToListDTO(subscriptionPolicies);
        assertEquals(listDTO.getCount(), (Integer) subscriptionPolicies.size());
        assertEquals(listDTO.getList().get(0).getPolicyName(), policy1.getPolicyName());
        assertEquals(listDTO.getList().get(0).getDescription(), policy1.getDescription());
        assertEquals(listDTO.getList().get(0).getDefaultLimit().getUnitTime(), (Integer) policy1.
                getDefaultQuotaPolicy().getLimit().getUnitTime());
        assertEquals(listDTO.getList().get(0).getDefaultLimit().getRequestCountLimit().getRequestCount(),
                (Integer) ((RequestCountLimit) policy1.getDefaultQuotaPolicy().getLimit()).getRequestCount());

        assertEquals(listDTO.getList().get(1).getPolicyName(), policy2.getPolicyName());
        assertEquals(listDTO.getList().get(1).getDescription(), policy2.getDescription());
        assertEquals(listDTO.getList().get(1).getDefaultLimit().getUnitTime(), (Integer) policy2.
                getDefaultQuotaPolicy().getLimit().getUnitTime());
        assertEquals(listDTO.getList().get(1).getDefaultLimit().getRequestCountLimit().getRequestCount(),
                (Integer) ((RequestCountLimit) policy2.getDefaultQuotaPolicy().getLimit()).getRequestCount());
    }
}
