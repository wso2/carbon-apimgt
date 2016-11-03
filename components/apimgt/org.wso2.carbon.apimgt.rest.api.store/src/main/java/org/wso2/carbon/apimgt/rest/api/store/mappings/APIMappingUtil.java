/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.APISummaryResults;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;

import java.util.ArrayList;
import java.util.List;

public class APIMappingUtil {

    /**
     * Converts {@link APISummaryResults} to {@link APIListDTO} DTO.
     * 
     * @param apisResult
     * @return
     */
    public static APIListDTO toAPIListDTO(APISummaryResults apisResult) {
        APIListDTO apiListDTO = new APIListDTO();
        apiListDTO.setCount(apisResult.getApiSummaryList().size());
        // apiListDTO.setNext(next);
        // apiListDTO.setPrevious(previous);
        apiListDTO.setList(toAPIInfo(apisResult.getApiSummaryList()));
        return apiListDTO;
    }

    /**
     * Converts {@link APISummary} List to an {@link APIInfoDTO} List.
     * 
     * @param apiSummaryList
     * @return
     */
    private static List<APIInfoDTO> toAPIInfo(List<APISummary> apiSummaryList) {
        List<APIInfoDTO> apiInfoList = new ArrayList<APIInfoDTO>();
        for (APISummary apiSummary : apiSummaryList) {
            APIInfoDTO apiInfo = new APIInfoDTO();
            apiInfo.setId(apiSummary.getId());
            apiInfo.setContext(apiSummary.getContext());
            apiInfo.setDescription(apiSummary.getDescription());
            apiInfo.setName(apiSummary.getName());
            apiInfo.setProvider(apiSummary.getProvider());
            apiInfo.setStatus(apiSummary.getStatus());
            apiInfo.setVersion(apiSummary.getVersion());
            apiInfoList.add(apiInfo);
        }
        return apiInfoList;
    }

    /**
     * Converts {@link APIDTO} to a {@link API}.
     * 
     * @param api
     * @return API DTO
     */
    public static APIDTO toAPIDTO(API api) {
        APIDTO apiDTO = new APIDTO();
        apiDTO.setId(api.getId());
        apiDTO.setName(api.getName());
        apiDTO.setProvider(api.getProvider());
        apiDTO.setStatus(api.getStatus());
        apiDTO.setVersion(api.getVersion());
        apiDTO.setContext(api.getContext());
        apiDTO.setDescription(api.getDescription());

        return apiDTO;
    }

}
