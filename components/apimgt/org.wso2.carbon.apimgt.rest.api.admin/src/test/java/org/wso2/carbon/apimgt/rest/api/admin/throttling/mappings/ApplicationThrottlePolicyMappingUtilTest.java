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
import org.wso2.carbon.apimgt.rest.api.admin.mappings.ApplicationThrottlePolicyMappingUtil;

import java.util.UUID;

import static org.wso2.carbon.apimgt.core.models.policy.PolicyConstants.REQUEST_COUNT_TYPE;

public class ApplicationThrottlePolicyMappingUtilTest {
    String policyName = "SampleAppPolicy";
    String uuid = UUID.randomUUID().toString();
    String displayName = "Sample Display Name";

    @Test
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
        Assert.assertEquals(dto.getPolicyId(), uuid);
        Assert.assertEquals(dto.getDefaultLimit().getRequestCountLimit().getRequestCount().intValue(),
                            requestCountLimit.getRequestCount());
        Assert.assertEquals(dto.getDisplayName(), displayName);
    }

    @Test
    public void fromApplicationThrottlePolicyDTOToModelTest() throws    Exception   {
        ApplicationThrottlePolicyDTO dto = new ApplicationThrottlePolicyDTO();
        dto.setDisplayName(displayName);
        dto.setPolicyName(policyName);
        dto.setPolicyId(uuid);
        ApplicationPolicy policy = ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyDTOToModel(dto);
        Assert.assertNotNull(policy);
        Assert.assertEquals(policy.getDisplayName(), displayName);
        Assert.assertEquals(policy.getPolicyName(), policyName);
    }




}
