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
import org.wso2.carbon.apimgt.rest.api.admin.mappings.BlockingConditionMappingUtil;

import java.util.UUID;

import static org.wso2.carbon.apimgt.core.util.APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP;
import static org.wso2.carbon.apimgt.core.util.APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE;

public class BlockingConditionMappingUtilTest {

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
        dto.setConditionType(BLOCKING_CONDITIONS_IP);
        dto.setConditionValue("12.32.45.3");
        dto.setStatus(true);
        BlockConditions conditions = BlockingConditionMappingUtil.fromBlockingConditionDTOToBlockCondition(dto);
        Assert.assertNotNull(conditions);
        Assert.assertEquals(BLOCKING_CONDITIONS_IP, conditions.getConditionType());
        Assert.assertEquals(conditions.getConditionValue(), "12.32.45.3");
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
}
