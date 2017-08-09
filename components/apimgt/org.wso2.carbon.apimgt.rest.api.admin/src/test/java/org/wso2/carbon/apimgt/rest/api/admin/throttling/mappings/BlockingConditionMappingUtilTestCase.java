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
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.IPConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.BlockingConditionMappingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.wso2.carbon.apimgt.core.util.APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE;

public class BlockingConditionMappingUtilTestCase {

    @Test(description = "Convert Blocking Condition to DTO")
    public void fromBlockingConditionToDTOTest()   throws  Exception   {
        BlockConditions conditions = new BlockConditions();
        String uuid = UUID.randomUUID().toString();
        conditions.setUuid(uuid);
        conditions.setConditionType(BLOCKING_CONDITION_IP_RANGE);
        BlockingConditionDTO dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(conditions);
        Assert.assertNotNull(dto);
        Assert.assertEquals(uuid, dto.getConditionId());
        Assert.assertEquals(dto.getConditionType(), BLOCKING_CONDITION_IP_RANGE);

    }

    @Test(description = "From Blocking Condition DTO to Model")
    public void fromBlockingConditionDTOToBlockConditionTest() throws Exception {
        BlockingConditionDTO dto = new BlockingConditionDTO();
        dto.setConditionId(UUID.randomUUID().toString());
        dto.setConditionType(BLOCKING_CONDITION_IP_RANGE);
        IPConditionDTO ipCondition = new IPConditionDTO();
        ipCondition.setStartingIP("12.32.45.3");
        ipCondition.setEndingIP("12.32.45.31");
        dto.setIpCondition(ipCondition);
        dto.setStatus(true);
        BlockConditions conditions = BlockingConditionMappingUtil.fromBlockingConditionDTOToBlockCondition(dto);
        Assert.assertNotNull(conditions);
        Assert.assertEquals(BLOCKING_CONDITION_IP_RANGE, conditions.getConditionType());
        Assert.assertEquals(conditions.getStartingIP(), dto.getIpCondition().getStartingIP());
        Assert.assertEquals(conditions.getEndingIP(), dto.getIpCondition().getEndingIP());
    }

    @Test(description = "From Blocking Condition to DTO")
    public void fromBlockConditionToIpConditionDTOTest() throws Exception   {
        BlockConditions conditions = new BlockConditions();
        conditions.setUuid(UUID.randomUUID().toString());
        conditions.setConditionType(BLOCKING_CONDITION_IP_RANGE);
        conditions.setStartingIP("12.23.45.3");
        conditions.setEndingIP("23.45.2.1");
        BlockingConditionDTO dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(conditions);
        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getConditionType(), BLOCKING_CONDITION_IP_RANGE);
        Assert.assertEquals(dto.getIpCondition().getStartingIP(), "12.23.45.3");
        Assert.assertEquals(dto.getIpCondition().getEndingIP(), "23.45.2.1");

    }

    @Test(description = "From Blocking Condition List to List DTO")
    public void fromBlockConditionListToListDTODTOTest() throws Exception   {
        List<BlockConditions> blockConditionList = new ArrayList<>();
        BlockConditions conditions1 = new BlockConditions();
        conditions1.setUuid(UUID.randomUUID().toString());
        conditions1.setConditionType(BLOCKING_CONDITION_IP_RANGE);
        conditions1.setStartingIP("12.23.45.3");
        conditions1.setEndingIP("23.45.2.1");

        BlockConditions conditions2 = new BlockConditions();
        conditions2.setUuid(UUID.randomUUID().toString());
        conditions2.setConditionType("API");
        conditions2.setConditionValue("DummyAPI");

        blockConditionList.add(conditions1);
        blockConditionList.add(conditions2);
        BlockingConditionListDTO dto = BlockingConditionMappingUtil.fromBlockConditionListToListDTO(blockConditionList);
        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getCount(), (Integer) blockConditionList.size());
        Assert.assertEquals(dto.getList().get(0).getIpCondition().getStartingIP(), conditions1.getStartingIP());
        Assert.assertEquals(dto.getList().get(0).getIpCondition().getEndingIP(), conditions1.getEndingIP());
        Assert.assertEquals(dto.getList().get(0).getConditionType(), conditions1.getConditionType());
        Assert.assertEquals(dto.getList().get(0).getConditionId(), conditions1.getUuid());

        Assert.assertEquals(dto.getList().get(1).getConditionType(), conditions2.getConditionType());
        Assert.assertEquals(dto.getList().get(1).getConditionId(), conditions2.getUuid());

    }
}
