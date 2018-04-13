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
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.BaseAPIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.BaseAPIInfoDTO;

import java.util.ArrayList;
import java.util.List;

public class APIMappingUtil {

    /**
     * Converts {@code List<API>} to {@link APIListDTO} DTO.
     *
     * @param apisResult List of APIs
     * @return APIListDTO
     */
    public static APIListDTO toAPIListDTO(List<API> apisResult) {
        APIListDTO apiListDTO = new APIListDTO();
        apiListDTO.setCount(apisResult.size());
        // apiListDTO.setNext(next);
        // apiListDTO.setPrevious(previous);
        apiListDTO.setList(toAPIInfo(apisResult));
        return apiListDTO;
    }

    /**
     * Converts {@link API} List to an {@link APIInfoDTO} List.
     *
     * @param apiSummaryList List of APIs
     * @return List of APIInfoDTO
     */
    private static List<APIInfoDTO> toAPIInfo(List<API> apiSummaryList) {
        List<APIInfoDTO> apiInfoList = new ArrayList<APIInfoDTO>();
        for (API apiSummary : apiSummaryList) {
            APIInfoDTO apiInfo = new APIInfoDTO();
            apiInfo.setId(apiSummary.getId());
            apiInfo.setContext(apiSummary.getContext());
            apiInfo.setDescription(apiSummary.getDescription());
            apiInfo.setName(apiSummary.getName());
            apiInfo.setProvider(apiSummary.getProvider());
            apiInfo.setLifeCycleStatus(apiSummary.getLifeCycleStatus());
            apiInfo.setVersion(apiSummary.getVersion());
            apiInfo.setType(BaseAPIInfoDTO.TypeEnum.APIINFO);
            apiInfoList.add(apiInfo);
        }
        return apiInfoList;
    }

    /**
     * Converts {@link APIDTO} to a {@link API}.
     *
     * @param api API
     * @return API DTO
     */
    public static APIDTO toAPIDTO(API api) {
        APIDTO apiDTO = new APIDTO();
        apiDTO.setId(api.getId());
        apiDTO.setName(api.getName());
        apiDTO.setProvider(api.getProvider());
        apiDTO.setLifeCycleStatus(api.getLifeCycleStatus());
        apiDTO.setVersion(api.getVersion());
        apiDTO.setContext(api.getContext());
        apiDTO.setDescription(api.getDescription());
        api.getPolicies().forEach(policy -> apiDTO.addPoliciesItem(policy.getPolicyName()));
        apiDTO.setLabels(new ArrayList<>(api.getLabels()));
        apiDTO.setType(BaseAPIDTO.TypeEnum.API);
        return apiDTO;
    }

}
