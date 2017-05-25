/***********************************************************************************************************************
 *
 *  *
 *  *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *   WSO2 Inc. licenses this file to you under the Apache License,
 *  *   Version 2.0 (the "License"); you may not use this file except
 *  *   in compliance with the License.
 *  *   You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */

package org.wso2.carbon.apimgt.rest.api.core.utils;


import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.rest.api.core.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.APISummaryDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.APISummaryListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.UriTemplateDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MappingUtil {

    /**
     * This method converts List of SubscriptionValidationData into SubscriptionDTO list.
     *
     * @param subscriptionValidationData List of Subscription Validation Data
     * @return SubscriptionDTO list
     */
    public static List<SubscriptionDTO> convertToSubscriptionListDto(
            List<SubscriptionValidationData> subscriptionValidationData) {
        List<SubscriptionDTO> subscriptionDTOList = new ArrayList<>();
        for (SubscriptionValidationData subscriptionData : subscriptionValidationData) {
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
            subscriptionDTO.setApiName(subscriptionData.getApiName());
            subscriptionDTO.setApiContext(subscriptionData.getApiContext());
            subscriptionDTO.setApiVersion(subscriptionData.getApiVersion());
            subscriptionDTO.setApiProvider(subscriptionData.getApiProvider());
            subscriptionDTO.setConsumerKey(subscriptionData.getConsumerKey());
            subscriptionDTO.setSubscriptionPolicy(subscriptionData.getSubscriptionPolicy());
            subscriptionDTO.setApplicationName(subscriptionData.getApplicationName());
            subscriptionDTO.setApplicationOwner(subscriptionData.getApplicationOwner());
            subscriptionDTO.setKeyEnvType(subscriptionData.getKeyEnvType());
            subscriptionDTO.setApplicationId(subscriptionData.getApplicationId());
            subscriptionDTO.setApplicationTier(subscriptionData.getApplicationTier());
            subscriptionDTOList.add(subscriptionDTO);
        }
        return subscriptionDTOList;
    }

    /**
     * Converts labelDTOs into labels
     *
     * @param labelDTOs List of LabelDTOs
     * @return List of Labels
     */
    public static List<Label> convertToLabels(List<LabelDTO> labelDTOs) {
        List<Label> labels = new ArrayList<>();
        for (LabelDTO labelDTO : labelDTOs) {
            String labelId = UUID.randomUUID().toString();
            Label label = new Label.Builder().id(labelId).name(labelDTO.getName()).accessUrls(labelDTO.getAccessUrls())
                    .build();
            labels.add(label);
        }
        return labels;
    }

    /**
     * Convert Uritemplate list to UriTemplateDTO list
     * @param resourcesOfApi list of uriTemplates
     * @return ResourcesListDTO
     */
    public static List<UriTemplateDTO> convertToResourceListDto(List<UriTemplate> resourcesOfApi){
        List<UriTemplateDTO> uriTemplateDTOArrayList = new ArrayList<>();
        resourcesOfApi.forEach((v)->{
            UriTemplateDTO uriTemplateDTO = new UriTemplateDTO();
            uriTemplateDTO.setUriTemplate(v.getUriTemplate());
            uriTemplateDTO.setAuthType(v.getAuthType());
            uriTemplateDTO.setPolicy(v.getPolicy());
            uriTemplateDTO.setHttpVerb(v.getHttpVerb());
            uriTemplateDTO.setScope("");
            uriTemplateDTOArrayList.add(uriTemplateDTO);
        });
        return uriTemplateDTOArrayList;
    }

    /**
     * Converts API list to APIListDTO list.
     *
     * @param apisList List of APIs
     * @return APIListDTO object
     */
    public static APIListDTO toAPIListDTO(List<API> apisList) {
        APIListDTO apiListDTO = new APIListDTO();
        apiListDTO.setCount(apisList.size());
        apiListDTO.setList(toAPIInfo(apisList));
        return apiListDTO;
    }

    /**
     * Converts {@link API} List to an {@link APIInfoDTO} List.
     *
     * @param apiList
     * @return
     */
    private static List<APIInfoDTO> toAPIInfo(List<API> apiList) {
        List<APIInfoDTO> apiInfoList = new ArrayList<APIInfoDTO>();
        for (API api : apiList) {
            APIInfoDTO apiInfo = new APIInfoDTO();
            apiInfo.setId(api.getId());
            apiInfo.setContext(api.getContext());
            apiInfo.setName(api.getName());
            apiInfo.setLifeCycleStatus(api.getLifeCycleStatus());
            apiInfo.setVersion(api.getVersion());
            apiInfoList.add(apiInfo);
        }
        return apiInfoList;
    }

    /**
     * Convert uritemplates+ subscription validation to ApiSummaryList
     * @param uriTemplates list of resources
     * @param subscriptionValidationDataList list of subscription data
     * @return APISummaryListDTO
     */
    public static APISummaryListDTO toApiSummaryListDto(List<UriTemplate> uriTemplates, List<SubscriptionValidationData>
            subscriptionValidationDataList) {
        APISummaryListDTO apiSummaryListDTO = new APISummaryListDTO();
        APISummaryDTO apiSummaryDTO = new APISummaryDTO();
        apiSummaryDTO.setResources(convertToResourceListDto(uriTemplates));
        apiSummaryDTO.setSubscriptions(convertToSubscriptionListDto(subscriptionValidationDataList));
        apiSummaryListDTO.addListItem(apiSummaryDTO);
        return apiSummaryListDTO;
    }
}
