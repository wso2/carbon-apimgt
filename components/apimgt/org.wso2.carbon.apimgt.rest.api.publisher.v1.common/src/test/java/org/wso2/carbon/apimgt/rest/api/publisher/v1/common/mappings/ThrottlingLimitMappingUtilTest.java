/*
 *   Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.ThrottlingLimit;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingLimitDTO;

public class ThrottlingLimitMappingUtilTest {

    @Test
    public void fromDTOToThrottlingLimitTest() {
        ThrottlingLimitDTO throttlingLimitDTO = new ThrottlingLimitDTO();
        throttlingLimitDTO.setUnit(ThrottlingLimitDTO.UnitEnum.HOUR);
        throttlingLimitDTO.setRequestCount(100);
        ThrottlingLimit  throttlingLimit = ThrottlingLimitMappingUtil.fromDTOToThrottlingLimit(throttlingLimitDTO);
        Assert.assertNotNull("Throttling limit is null", throttlingLimit);
        Assert.assertEquals("Throttling limit unit is not equal", "HOUR", throttlingLimit.getUnit());
        Assert.assertEquals("Throttling limit request count is not equal", 100,
                throttlingLimit.getRequestCount());
    }

    @Test
    public void fromThrottlingLimitToDTOTest() {
        ThrottlingLimit throttlingLimit = new ThrottlingLimit();
        throttlingLimit.setUnit("DAY");
        throttlingLimit.setRequestCount(1000);
        ThrottlingLimitDTO throttlingLimitDTO = ThrottlingLimitMappingUtil.fromThrottlingLimitToDTO(throttlingLimit);
        Assert.assertNotNull("Throttling limit DTO is null", throttlingLimitDTO);
        Assert.assertEquals("Throttling limit DTO unit is not equal", ThrottlingLimitDTO.UnitEnum.DAY,
                throttlingLimitDTO.getUnit());
        Assert.assertEquals("Throttling limit DTO request count is not equal", 1000,
                throttlingLimitDTO.getRequestCount().intValue());
    }
}
