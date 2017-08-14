/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.analytics.mappings;

import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.core.models.analytics.APISubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionCount;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APICountDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APICountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APISubscriptionCountDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APISubscriptionCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.ApplicationCountDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.ApplicationCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.SubscriptionCountDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.SubscriptionCountListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping Utils for Analytics REST API.
 */
public class AnalyticsMappingUtil {

    /**
     * Converts and ApplicationCountList to ApplicationCountListDTO.
     *
     * @param applicationCountList list of ApplicationCount objects
     * @return corresponding ApplicationCountListDTO object
     */
    public static ApplicationCountListDTO fromApplicationCountToListDTO(List<ApplicationCount> applicationCountList) {
        ApplicationCountListDTO applicationCountListDTO = new ApplicationCountListDTO();
        List<ApplicationCountDTO> applicationCountDTOList = new ArrayList<>();
        applicationCountListDTO.setCount(applicationCountList.size());
        for (ApplicationCount applicationCount : applicationCountList) {
            applicationCountDTOList.add(fromApplicationCountToDTO(applicationCount));
        }

        applicationCountListDTO.setList(applicationCountDTOList);
        return applicationCountListDTO;
    }


    /**
     * Converts and APICountList to APICountDTO.
     *
     * @param apiCountList list of APICount objects
     * @return corresponding APICountListDTO object
     */
    public static APICountListDTO fromAPICountToListDTO(List<APICount> apiCountList) {
        APICountListDTO apiCountListDTO = new APICountListDTO();
        List<APICountDTO> apiCountDTOList = new ArrayList<>();
        apiCountListDTO.setCount(apiCountList.size());
        for (APICount apiInfo : apiCountList) {
            apiCountDTOList.add(fromAPICountToDTO(apiInfo));
        }
        apiCountListDTO.setList(apiCountDTOList);
        return apiCountListDTO;
    }

    /**
     * Converts and APIInfoList to APIInfoListDTO.
     *
     * @param apiInfoList list of ApiInfo objects
     * @return corresponding APIInfoListDTO object
     */
    public static APIInfoListDTO fromAPIInfoListToDTO(List<APIInfo> apiInfoList) {
        APIInfoListDTO apiInfoListDTO = new APIInfoListDTO();
        List<APIInfoDTO> apiInfoDTOList = new ArrayList<>();
        apiInfoListDTO.setCount(apiInfoList.size());
        for (APIInfo apiInfo : apiInfoList) {
            apiInfoDTOList.add(fromAPIInfoToDTO(apiInfo));
        }
        apiInfoListDTO.setList(apiInfoDTOList);
        return apiInfoListDTO;
    }

    /**
     * Converts and APISubscriptionInfoList to APIInfoListDTO.
     *
     * @param apiSubscriptionCountList list of ApiInfo objects
     * @return corresponding APIInfoListDTO object
     */
    public static APISubscriptionCountListDTO fromAPISubscriptionInfoListToDTO(List<APISubscriptionCount>
                                                                                       apiSubscriptionCountList) {
        APISubscriptionCountListDTO apiSubscriptionListDTO = new APISubscriptionCountListDTO();
        List<APISubscriptionCountDTO> apiSubscriptionDTOList = new ArrayList<>();
        apiSubscriptionListDTO.setCount(apiSubscriptionCountList.size());
        for (APISubscriptionCount apiSubscriptionCount : apiSubscriptionCountList) {
            APISubscriptionCountDTO apiSubscriptionDTO = new APISubscriptionCountDTO();
            apiSubscriptionDTO.setId(apiSubscriptionCount.getId());
            apiSubscriptionDTO.setName(apiSubscriptionCount.getName());
            apiSubscriptionDTO.setVersion(apiSubscriptionCount.getVersion());
            apiSubscriptionDTO.setProvider(apiSubscriptionCount.getProvider());
            apiSubscriptionDTO.setCount(apiSubscriptionCount.getCount());
            apiSubscriptionDTOList.add(apiSubscriptionDTO);
        }
        apiSubscriptionListDTO.setList(apiSubscriptionDTOList);
        return apiSubscriptionListDTO;
    }

    /**
     * Converts and SubscriptionInfoList to SubscriptionInfoListDTO.
     *
     * @param subscriptionCountList list of SubscriptionCount objects
     * @return corresponding APIInfoListDTO object
     */
    public static SubscriptionCountListDTO fromSubscriptionCountListToDTO(List<SubscriptionCount>
                                                                                  subscriptionCountList) {
        SubscriptionCountListDTO subscriptionCountListDTO = new SubscriptionCountListDTO();
        List<SubscriptionCountDTO> subscriptionCountDTOList = new ArrayList<>();
        subscriptionCountListDTO.setCount(subscriptionCountList.size());
        for (SubscriptionCount subscriptionCount : subscriptionCountList) {
            SubscriptionCountDTO subscriptionCountDTO = new SubscriptionCountDTO();
            subscriptionCountDTO.setTime(subscriptionCount.getTimestamp());
            subscriptionCountDTO.setCount(subscriptionCount.getCount());
            subscriptionCountDTOList.add(subscriptionCountDTO);
        }
        subscriptionCountListDTO.setList(subscriptionCountDTOList);
        return subscriptionCountListDTO;
    }

    private static APIInfoDTO fromAPIInfoToDTO(APIInfo apiInfo) {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setId(apiInfo.getId());
        apiInfoDTO.setName(apiInfo.getName());
        apiInfoDTO.setContext(apiInfo.getContext());
        apiInfoDTO.setDescription(apiInfo.getDescription());
        apiInfoDTO.setLifeCycleStatus(apiInfo.getLifeCycleStatus());
        apiInfoDTO.setProvider(apiInfo.getProvider());
        apiInfoDTO.setWorkflowStatus(apiInfo.getWorkflowStatus());
        apiInfoDTO.setTime(apiInfo.getCreatedTime());
        return apiInfoDTO;
    }

    private static APICountDTO fromAPICountToDTO(APICount apiCount) {
        APICountDTO apiCountDTO = new APICountDTO();
        apiCountDTO.setTime(apiCount.getTimestamp());
        apiCountDTO.setCount(apiCount.getCount());
        return apiCountDTO;
    }

    private static ApplicationCountDTO fromApplicationCountToDTO(ApplicationCount applicationCount) {
        ApplicationCountDTO applicationCountDTO = new ApplicationCountDTO();
        applicationCountDTO.setTime(applicationCount.getTimestamp());
        applicationCountDTO.setCount(applicationCount.getCount());
        return applicationCountDTO;
    }
}
