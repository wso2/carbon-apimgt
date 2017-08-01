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
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.CustomPolicyMappingUtil;

import java.util.UUID;

public class CustomPolicyMappingUtilTest {

    @Test(description = "Convert custom Policy to DTO object")
    public void fromCustomPolicyToDTOTest() throws Exception    {
        CustomPolicy policy = new CustomPolicy("SampleCustomPolicy");
        policy.setUuid(UUID.randomUUID().toString());
        CustomRuleDTO dto = CustomPolicyMappingUtil.fromCustomPolicyToDTO(policy);
        Assert.assertNotNull(dto);
    }

    @Test(description = "Convert DTO to Model")
    public void fromCustomPolicyDTOToModelTest() throws Exception   {
        CustomRuleDTO dto = new CustomRuleDTO();
        CustomPolicyMappingUtil.fromCustomPolicyDTOToModel(dto);
        Assert.assertNotNull(dto);
    }


}
