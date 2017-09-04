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
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.CustomPolicyMappingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomPolicyMappingUtilTest {
    String name = "SampleCustomPolicy";
    String uuid = UUID.randomUUID().toString();

    @Test(description = "Convert custom Policy to DTO object")
    public void fromCustomPolicyToDTOTest() throws Exception    {

        CustomPolicy policy = new CustomPolicy(name);
        policy.setUuid(uuid);
        CustomRuleDTO dto = CustomPolicyMappingUtil.fromCustomPolicyToDTO(policy);
        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getPolicyName(), name);
        Assert.assertEquals(dto.getId(), uuid);
    }

    @Test(description = "Convert DTO to Model")
    public void fromCustomPolicyDTOToModelTest() throws Exception   {
        CustomRuleDTO dto = new CustomRuleDTO();
        dto.setPolicyName(name);
        CustomPolicy policy = CustomPolicyMappingUtil.fromCustomPolicyDTOToModel(dto);
        Assert.assertNotNull(policy);
        Assert.assertEquals(policy.getPolicyName(), name);
    }

    @Test(description = "Convert List<CustomPolicy> to CustomRuleListDTO")
    public void fromCustomPolicyArrayToListDTOTest() throws Exception   {
        CustomPolicy customPolicy1 = new CustomPolicy("namw1");
        customPolicy1.setDescription("custom policy desc1");
        customPolicy1.setKeyTemplate("keytemplate1");
        customPolicy1.setSiddhiQuery("sample query1");

        CustomPolicy customPolicy2 = new CustomPolicy("namw2");
        customPolicy2.setDescription("custom policy desc2");
        customPolicy2.setKeyTemplate("keytemplate2");
        customPolicy2.setSiddhiQuery("sample query2");
        List<CustomPolicy> customPolicyList = new ArrayList<>();
        customPolicyList.add(customPolicy1);
        customPolicyList.add(customPolicy2);
        CustomRuleListDTO listDTO = CustomPolicyMappingUtil.fromCustomPolicyArrayToListDTO(customPolicyList);

        Assert.assertEquals((Integer) customPolicyList.size(), listDTO.getCount());
        Assert.assertEquals(customPolicy1.getPolicyName(), listDTO.getList().get(0).getPolicyName());
        Assert.assertEquals(customPolicy1.getDisplayName(), listDTO.getList().get(0).getDisplayName());
        Assert.assertEquals(customPolicy1.getKeyTemplate(), listDTO.getList().get(0).getKeyTemplate());
        Assert.assertEquals(customPolicy1.getSiddhiQuery(), listDTO.getList().get(0).getSiddhiQuery());
        Assert.assertEquals(customPolicy1.getDescription(), listDTO.getList().get(0).getDescription());

        Assert.assertEquals(customPolicy2.getPolicyName(), listDTO.getList().get(1).getPolicyName());
        Assert.assertEquals(customPolicy2.getDisplayName(), listDTO.getList().get(1).getDisplayName());
        Assert.assertEquals(customPolicy2.getKeyTemplate(), listDTO.getList().get(1).getKeyTemplate());
        Assert.assertEquals(customPolicy2.getSiddhiQuery(), listDTO.getList().get(1).getSiddhiQuery());
        Assert.assertEquals(customPolicy2.getDescription(), listDTO.getList().get(1).getDescription());

    }
}
