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

package org.wso2.carbon.apimgt.rest.api.store.utils.mappings;

import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.APISummaryResults;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIInfo;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIList;

public class APIMappingUtil {

    /**
     * Converts {@link APISummaryResults} to {@link APIList} DTO.
     * 
     * @param apisResult
     * @return
     */
    public static APIList toAPIListDTO(APISummaryResults apisResult) {
        APIList apiListDTO = new APIList();
        apiListDTO.setCount(apisResult.getApiSummaryList().size());
        // apiListDTO.setNext(next);
        // apiListDTO.setPrevious(previous);
        apiListDTO.setList(toAPIInfo(apisResult.getApiSummaryList()));
        return apiListDTO;
    }

    /**
     * Converts {@link APISummary} List to an {@link APIInfo} List.
     * 
     * @param apiSummaryList
     * @return
     */
    private static List<APIInfo> toAPIInfo(List<APISummary> apiSummaryList) {
        List<APIInfo> apiInfoList = new ArrayList<APIInfo>();
        for (APISummary apiSummary : apiSummaryList) {
            APIInfo apiInfo = new APIInfo();
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
     * Converts {@link API} to a {@link org.wso2.carbon.apimgt.rest.api.store.dto.API}.
     * 
     * @param api
     * @return API DTO
     */
    public static org.wso2.carbon.apimgt.rest.api.store.dto.API toAPIDTO(API api) {
        org.wso2.carbon.apimgt.rest.api.store.dto.API apiDTO = new org.wso2.carbon.apimgt.rest.api.store.dto.API();
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
