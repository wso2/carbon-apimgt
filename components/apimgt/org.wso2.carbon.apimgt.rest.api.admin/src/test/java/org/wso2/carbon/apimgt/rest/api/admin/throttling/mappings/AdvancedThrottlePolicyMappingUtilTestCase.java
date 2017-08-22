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
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.RequestCountLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.AdvancedThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.throttling.common.SampleTestObjectCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdvancedThrottlePolicyMappingUtilTestCase {

    @Test(description = "Convert Policy to DTO")
    public void fromAdvancedPolicyToDTOTest() throws Exception  {
        APIPolicy apiPolicy = new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY);
        String uuid = UUID.randomUUID().toString();
        String displayName = "SampleAPIPolicy";
        String description = "Sample Description";
        apiPolicy.setUuid(uuid);
        apiPolicy.setDisplayName(displayName);
        apiPolicy.setDescription(description);
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit("s", 60 ,10);
        quotaPolicy.setLimit(requestCountLimit);
        apiPolicy.setDefaultQuotaPolicy(quotaPolicy);
        AdvancedThrottlePolicyDTO dto = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyToDTO(apiPolicy);
        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getPolicyName(), APIMgtConstants.DEFAULT_API_POLICY);
        Assert.assertEquals(dto.getDisplayName(), displayName);
        Assert.assertEquals(dto.getDescription(), description);
        Assert.assertEquals(apiPolicy.getDefaultQuotaPolicy().getLimit().getTimeUnit(), dto.getDefaultLimit().getTimeUnit());
        Assert.assertEquals((Integer) apiPolicy.getDefaultQuotaPolicy().getLimit().getUnitTime(),
                dto.getDefaultLimit().getUnitTime());
        Assert.assertEquals((Integer)((RequestCountLimit)apiPolicy.getDefaultQuotaPolicy().getLimit()).getRequestCount(),
                dto.getDefaultLimit().getRequestCountLimit().getRequestCount());
    }

    @Test(description = "Convert Policy DTO to Policy object")
    public void fromAdvancedPolicyDTOToPolicyTest() throws Exception    {
        AdvancedThrottlePolicyDTO dto = new AdvancedThrottlePolicyDTO();
        dto.setDisplayName(APIMgtConstants.DEFAULT_API_POLICY);
        String uuid = UUID.randomUUID().toString();
        String description = "Sample Description";
        dto.setDescription(description);
        dto.setId(uuid);
        ThrottleLimitDTO throttleLimitDTO = new ThrottleLimitDTO();
        throttleLimitDTO.setType("RequestCountLimit");
        throttleLimitDTO.setTimeUnit("s");
        throttleLimitDTO.setUnitTime(1);
        RequestCountLimitDTO requestCountLimitDTO = new RequestCountLimitDTO();
        requestCountLimitDTO.setRequestCount(2);
        throttleLimitDTO.setRequestCountLimit(requestCountLimitDTO);
        dto.setDefaultLimit(throttleLimitDTO);
        APIPolicy policy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(dto);
        Assert.assertNotNull(policy);
        Assert.assertEquals(policy.getDisplayName(), APIMgtConstants.DEFAULT_API_POLICY);
        Assert.assertEquals(policy.getDescription(), description);
        Assert.assertEquals(policy.getDefaultQuotaPolicy().getType(), "requestCount");
        Assert.assertEquals(policy.getDefaultQuotaPolicy().getLimit().getTimeUnit(), dto.getDefaultLimit().getTimeUnit());
        Assert.assertEquals((Integer) policy.getDefaultQuotaPolicy().getLimit().getUnitTime(),
                dto.getDefaultLimit().getUnitTime());
        Assert.assertEquals((Integer)((RequestCountLimit)policy.getDefaultQuotaPolicy().getLimit()).getRequestCount(),
                dto.getDefaultLimit().getRequestCountLimit().getRequestCount());
    }

    @Test(description = "Convert Policy list to Policy List DTO object")
    public void fromAPIPolicyArrayToListDTOTest() throws Exception    {
        APIPolicy policy1 = SampleTestObjectCreator.createAPIPolicyWithRequestLimit("Gold1");
        APIPolicy policy2 = SampleTestObjectCreator.createAPIPolicyWithRequestLimit("Gold2");
        List<APIPolicy> policyList = new ArrayList<>();
        policyList.add(policy1);
        policyList.add(policy2);
        AdvancedThrottlePolicyListDTO listDTO = AdvancedThrottlePolicyMappingUtil.fromAPIPolicyArrayToListDTO(policyList);
        Assert.assertEquals((Integer) policyList.size(), listDTO.getCount());
        Assert.assertEquals(policy1.getDisplayName(), listDTO.getList().get(0).getDisplayName());
        Assert.assertEquals(policy1.getDescription(), listDTO.getList().get(0).getDescription());
        Assert.assertEquals(policy1.getDefaultQuotaPolicy().getType(), "requestCount");
        Assert.assertEquals(policy1.getDefaultQuotaPolicy().getLimit().getTimeUnit(), listDTO.getList().get(0)
                                                    .getDefaultLimit().getTimeUnit());
        Assert.assertEquals((Integer) policy1.getDefaultQuotaPolicy().getLimit().getUnitTime(),
                listDTO.getList().get(0).getDefaultLimit().getUnitTime());
        Assert.assertEquals((Integer)((RequestCountLimit)policy1.getDefaultQuotaPolicy().getLimit()).getRequestCount(),
                listDTO.getList().get(0).getDefaultLimit().getRequestCountLimit().getRequestCount());

        Assert.assertEquals(policy2.getDisplayName(), listDTO.getList().get(1).getDisplayName());
        Assert.assertEquals(policy2.getDescription(), listDTO.getList().get(1).getDescription());
        Assert.assertEquals(policy2.getDefaultQuotaPolicy().getType(), "requestCount");
        Assert.assertEquals(policy2.getDefaultQuotaPolicy().getLimit().getTimeUnit(), listDTO.getList().get(1)
                .getDefaultLimit().getTimeUnit());
        Assert.assertEquals((Integer) policy2.getDefaultQuotaPolicy().getLimit().getUnitTime(),
                listDTO.getList().get(1).getDefaultLimit().getUnitTime());
        Assert.assertEquals((Integer)((RequestCountLimit)policy2.getDefaultQuotaPolicy().getLimit()).getRequestCount(),
                listDTO.getList().get(1).getDefaultLimit().getRequestCountLimit().getRequestCount());
    }

    @Test(description = "Convert Policy DTO to Policy object")
    public void fromAdvancedPolicyToInfoDTOTest() throws Exception    {
        APIPolicy apiPolicy = new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY);
        String uuid = UUID.randomUUID().toString();
        String displayName = "SampleAPIPolicy";
        String description = "Sample Description";
        apiPolicy.setUuid(uuid);
        apiPolicy.setDisplayName(displayName);
        apiPolicy.setDescription(description);
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit("s", 60 ,10);
        quotaPolicy.setLimit(requestCountLimit);
        apiPolicy.setDefaultQuotaPolicy(quotaPolicy);
        AdvancedThrottlePolicyDTO policyDTO = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyToInfoDTO(apiPolicy);
        Assert.assertNotNull(policyDTO);
        Assert.assertEquals(policyDTO.getDisplayName(), displayName);
        Assert.assertEquals(policyDTO.getDescription(), description);
        Assert.assertEquals(policyDTO.getDefaultLimit().getType(), "RequestCountLimit");
        Assert.assertEquals(policyDTO.getDefaultLimit().getTimeUnit(), apiPolicy.getDefaultQuotaPolicy()
                                                                                            .getLimit().getTimeUnit());
        Assert.assertEquals(policyDTO.getDefaultLimit().getUnitTime(),
                                            (Integer) apiPolicy.getDefaultQuotaPolicy().getLimit().getUnitTime());
        Assert.assertEquals(policyDTO.getDefaultLimit().getRequestCountLimit().getRequestCount(),
                          (Integer)((RequestCountLimit)apiPolicy.getDefaultQuotaPolicy().getLimit()).getRequestCount());
    }
}
