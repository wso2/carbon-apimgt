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
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.RequestCountLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.ApplicationThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.throttling.common.SampleTestObjectCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.wso2.carbon.apimgt.core.models.policy.PolicyConstants.REQUEST_COUNT_TYPE;

public class ApplicationThrottlePolicyMappingUtilTest {
    String policyName = "SampleAppPolicy";
    String uuid = UUID.randomUUID().toString();
    String displayName = "Sample Display Name";

    @Test(description = "Convert from Policy to DTO")
    public void fromApplicationThrottlePolicyToDTOTest() throws Exception   {

        Policy policy = new ApplicationPolicy(uuid,policyName);
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        RequestCountLimit requestCountLimit = new RequestCountLimit("s", 1000, 10000);
        quotaPolicy.setLimit(requestCountLimit);
        quotaPolicy.setType(REQUEST_COUNT_TYPE);
        policy.setDefaultQuotaPolicy(quotaPolicy);
        policy.setDisplayName(displayName);
        ApplicationThrottlePolicyDTO dto = ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyToDTO(policy);
        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getDisplayName(), displayName);
        Assert.assertNotNull(dto.getPolicyName(), policyName);
        Assert.assertEquals(dto.getId(), uuid);
        Assert.assertEquals(dto.getDefaultLimit().getRequestCountLimit().getRequestCount().intValue(),
                            requestCountLimit.getRequestCount());
        Assert.assertEquals(dto.getDisplayName(), displayName);
    }

    @Test(description = "Convert from DTO to Policy")
    public void fromApplicationThrottlePolicyDTOToModelTest() throws    Exception   {
        ApplicationThrottlePolicyDTO dto = new ApplicationThrottlePolicyDTO();
        dto.setDisplayName(displayName);
        dto.setPolicyName(policyName);
        dto.setId(uuid);
        ThrottleLimitDTO throttleLimitDTO = new ThrottleLimitDTO();
        throttleLimitDTO.setType("RequestCountLimit");
        throttleLimitDTO.setTimeUnit("s");
        throttleLimitDTO.setUnitTime(1);
        RequestCountLimitDTO requestCountLimitDTO = new RequestCountLimitDTO();
        requestCountLimitDTO.setRequestCount(2);
        throttleLimitDTO.setRequestCountLimit(requestCountLimitDTO);
        dto.setDefaultLimit(throttleLimitDTO);
        ApplicationPolicy policy = ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyDTOToModel(dto);
        Assert.assertNotNull(policy);
        Assert.assertEquals(policy.getDisplayName(), displayName);
        Assert.assertEquals(policy.getPolicyName(), policyName);
        Assert.assertEquals(policy.getDefaultQuotaPolicy().getType(), "requestCount");
        Assert.assertEquals(policy.getDefaultQuotaPolicy().getLimit().getTimeUnit(), dto.getDefaultLimit().getTimeUnit());
        Assert.assertEquals((Integer) policy.getDefaultQuotaPolicy().getLimit().getUnitTime(),
                                                                                    dto.getDefaultLimit().getUnitTime());
        Assert.assertEquals((Integer)((RequestCountLimit)policy.getDefaultQuotaPolicy().getLimit()).getRequestCount(),
                                                    dto.getDefaultLimit().getRequestCountLimit().getRequestCount());

    }

    @Test(description = "Convert from Policy Array to DTO")
    public void testFromApplicationPolicyArrayToListDTO() throws Exception   {
        List<ApplicationPolicy> appPolicies = new ArrayList<>();
        ApplicationPolicy policy1 = SampleTestObjectCreator.createApplicationPolicyWithRequestLimit("Gold");
        ApplicationPolicy policy2 = SampleTestObjectCreator.createApplicationPolicyWithRequestLimit("Silver");
        appPolicies.add(policy1);
        appPolicies.add(policy2);
        ApplicationThrottlePolicyListDTO listDTO =
                                ApplicationThrottlePolicyMappingUtil.fromApplicationPolicyArrayToListDTO(appPolicies);

        Assert.assertEquals(listDTO.getCount(), (Integer) appPolicies.size());
        Assert.assertNotNull(listDTO.getList().get(0).getPolicyName(), policy1.getPolicyName());
        Assert.assertEquals(listDTO.getList().get(0).getId(), policy1.getUuid());
        Assert.assertEquals(listDTO.getList().get(0).getDefaultLimit().getRequestCountLimit()
                                                                        .getRequestCount().intValue(),
                                    ((RequestCountLimit) policy1.getDefaultQuotaPolicy().getLimit()).getRequestCount());
        Assert.assertEquals(listDTO.getList().get(0).getDisplayName(), policy1.getDisplayName());

        Assert.assertNotNull(listDTO.getList().get(1).getPolicyName(), policy2.getPolicyName());
        Assert.assertEquals(listDTO.getList().get(1).getId(), policy2.getUuid());
        Assert.assertEquals(listDTO.getList().get(1).getDefaultLimit().getRequestCountLimit()
                        .getRequestCount().intValue(),
                ((RequestCountLimit) policy2.getDefaultQuotaPolicy().getLimit()).getRequestCount());
        Assert.assertEquals(listDTO.getList().get(1).getDisplayName(), policy2.getDisplayName());
    }


}
