/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApiResultDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelUsageApisDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelUsageDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class used for mapping utility to LabelUsage related operations
 */
public class LabelsUsageMappingUtil {

    /**
     * Adds a list of ApiResult to LabelUsageDTO
     *
     * @param apiResults List of ApiResult
     * @return LabelUsageDTO label usage data
     */
    public static LabelUsageDTO fromApiResultListToLabelUsageDTO(List<ApiResult> apiResults) {
        LabelUsageDTO labelUsageDTO = new LabelUsageDTO();
        labelUsageDTO.setApis(fromApiResultListToLabelUsageApisDTO(apiResults));
        return labelUsageDTO;
    }

    /**
     * Converts a list of ApiResult to LabelUsageApisDTO
     *
     * @param apiResults List of ApiResult
     * @return LabelUsageDTO label usage data
     */
    private static LabelUsageApisDTO fromApiResultListToLabelUsageApisDTO(List<ApiResult> apiResults) {
        LabelUsageApisDTO labelUsageApisDTO = new LabelUsageApisDTO();
        labelUsageApisDTO.setCount(apiResults.size());
        labelUsageApisDTO.setList(fromApiResultListToLabelDTOList(apiResults));
        return labelUsageApisDTO;
    }

    /**
     * Converts a list of ApiResult to ApiResultDTO list
     *
     * @param apiResults List of ApiResult
     * @return List<ApiResultDTO> list containing api result data
     */
    private static List<ApiResultDTO> fromApiResultListToLabelDTOList(List<ApiResult> apiResults) {
        List<ApiResultDTO> apiResultDTOs = new ArrayList<>();
        for (ApiResult apiResult : apiResults) {
            ApiResultDTO apiResultDTO = new ApiResultDTO();
            apiResultDTO.setId(apiResult.getId());
            apiResultDTO.setName(apiResult.getName());
            apiResultDTO.setVersion(apiResult.getVersion());
            apiResultDTO.setProvider(apiResult.getProvider());
            apiResultDTOs.add(apiResultDTO);
        }
        return apiResultDTOs;
    }
}
