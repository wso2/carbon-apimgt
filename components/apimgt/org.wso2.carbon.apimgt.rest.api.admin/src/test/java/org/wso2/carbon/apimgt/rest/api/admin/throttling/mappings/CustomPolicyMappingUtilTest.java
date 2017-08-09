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

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.CustomPolicyMappingUtil;

import java.util.UUID;

public class CustomPolicyMappingUtilTest {
    String name = "SampleCustomPolicy";
    String uuid = UUID.randomUUID().toString();

    @Test()
    public void fromCustomPolicyToDTOTest() throws Exception    {

        CustomPolicy policy = new CustomPolicy(name);
        policy.setUuid(uuid);
        CustomRuleDTO dto = CustomPolicyMappingUtil.fromCustomPolicyToDTO(policy);
        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getPolicyName(), name);
        Assert.assertEquals(dto.getPolicyId(), uuid);
    }

    @Test()
    public void fromCustomPolicyDTOToModelTest() throws Exception   {
        CustomRuleDTO dto = new CustomRuleDTO();
        dto.setPolicyName(name);
        CustomPolicy policy = CustomPolicyMappingUtil.fromCustomPolicyDTOToModel(dto);
        Assert.assertNotNull(policy);
        Assert.assertEquals(policy.getPolicyName(), name);
    }


}
