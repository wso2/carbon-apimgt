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
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.AdvancedThrottlePolicyMappingUtil;

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
        AdvancedThrottlePolicyDTO dto = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyToDTO(apiPolicy);
        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getPolicyName(), APIMgtConstants.DEFAULT_API_POLICY);
        Assert.assertEquals(dto.getDisplayName(), displayName);
        Assert.assertEquals(dto.getDescription(), description);
    }

    @Test(description = "Convert Policy DTO to Policy object")
    public void fromAdvancedPolicyDTOToPolicyTest() throws Exception    {
        AdvancedThrottlePolicyDTO dto = new AdvancedThrottlePolicyDTO();
        dto.setDisplayName(APIMgtConstants.DEFAULT_API_POLICY);
        String uuid = UUID.randomUUID().toString();
        String description = "Sample Description";
        dto.setDescription(description);
        dto.setPolicyId(uuid);
        APIPolicy policy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(dto);
        Assert.assertNotNull(policy);
        Assert.assertEquals(policy.getDisplayName(), APIMgtConstants.DEFAULT_API_POLICY);
        Assert.assertEquals(policy.getDescription(), description);
    }


}
