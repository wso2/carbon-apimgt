/*
 * Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.ThrottlingLimit;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingLimitDTO;

/**
 * This class is responsible for mapping APIOperationsThrottlingLimitDTOs into REST API throttling related manipulations
 * and vice versa.
 */
public class ThrottlingLimitMappingUtil {

    /**
     * Manipulates ThrottlingLimit object from APIOperationsThrottlingLimitDTO
     *
     * @param dto Holds throttling limits relevant to the operations
     * @return throttlingLimit object
     */
    public static ThrottlingLimit fromDTOToThrottlingLimit(ThrottlingLimitDTO dto) {
        ThrottlingLimit throttlingLimit = new ThrottlingLimit();
        throttlingLimit.setRequestCount(dto.getRequestCount());
        throttlingLimit.setUnit(dto.getUnit().toString());
        return throttlingLimit;
    }

    /**
     * Manipulates APIOperationsThrottlingLimitDTO object from ThrottlingLimit
     *
     * @param throttlingLimit Provides throttling limit details
     * @return APIOperationsThrottlingLimitDTO object with throttling details
     */
    public static ThrottlingLimitDTO fromThrottlingLimitToDTO(ThrottlingLimit throttlingLimit) {
        ThrottlingLimitDTO dto = new ThrottlingLimitDTO();
        if (throttlingLimit == null) {
            dto.setRequestCount(-1);
            dto.setUnit(ThrottlingLimitDTO.UnitEnum.MINUTE);
        } else {
            dto.setRequestCount(throttlingLimit.getRequestCount());
            dto.setUnit(ThrottlingLimitDTO.UnitEnum.valueOf(throttlingLimit.getUnit()));
        }
        return dto;
    }
}
