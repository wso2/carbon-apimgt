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
import org.wso2.carbon.apimgt.core.models.RegistrationSummary;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.rest.api.core.dto.AnalyticsInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.CredentialsDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.JWTInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.KeyManagerInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationSummaryDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ResourcesListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ThrottlingInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.UriTemplateDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.APIListDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MappingUtil {

    /**
     * This method converts List of SubscriptionValidationData into SubscriptionListDTO object.
     *
     * @param subscriptionValidationData List of Subscription Validation Data
     * @return subscriptionListDTO
     */
    public static SubscriptionListDTO convertToSubscriptionListDto(
            List<SubscriptionValidationData> subscriptionValidationData) {
        SubscriptionListDTO subscriptionListDTO = new SubscriptionListDTO();
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
            subscriptionListDTO.addListItem(subscriptionDTO);
        }
        return subscriptionListDTO;
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
     * Convert Uritemplate list to ResourceListDto
     * @param resourcesOfApi list of uriTemplates
     * @return ResourcesListDTO
     */
    public static ResourcesListDTO convertToResourceListDto(List<UriTemplate> resourcesOfApi){
        ResourcesListDTO resourcesListDTO = new ResourcesListDTO();
        List<UriTemplateDTO> uriTemplateDTOArrayList = new ArrayList<>();
        resourcesOfApi.forEach((v)->{
            UriTemplateDTO uriTemplateDTO = new UriTemplateDTO();
            uriTemplateDTO.setUriTemplate(v.getUriTemplate());
            uriTemplateDTO.setAuthType(v.getAuthType());
            uriTemplateDTO.setPolicy(v.getPolicy());
            uriTemplateDTO.setHttpVerb(v.getHttpVerb());
            uriTemplateDTO.setScopes(Collections.emptyList());
            uriTemplateDTOArrayList.add(uriTemplateDTO);
        });
        resourcesListDTO.setList(uriTemplateDTOArrayList);
        return resourcesListDTO;
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
     * Converts the Gateway registration summary into RegistrationSummaryDTO
     *
     * @param registrationSummary the registration summary required by gateway
     * @return RegistrationSummaryDTO
     */
    public static RegistrationSummaryDTO toRegistrationSummaryDTO(RegistrationSummary registrationSummary) {
        RegistrationSummaryDTO registrationSummaryDTO = new RegistrationSummaryDTO();
        registrationSummaryDTO.setKeyManagerInfo(toKeyManagerInfoDTO(registrationSummary));
        registrationSummaryDTO.setAnalyticsInfo(toAnalyticsDTO(registrationSummary));
        registrationSummaryDTO.setJwTInfo(toJWTInfoDTO(registrationSummary));
        registrationSummaryDTO.setThrottlingInfo(toThrottlingInfoDTO(registrationSummary));
        return registrationSummaryDTO;
    }

    /**
     * Converts RegistrationSummary key manager information into KeyManagerInfoDTO
     *
     * @param registrationSummary the registration summary required by gateway
     * @return KeyManagerInfoDTO
     */
    private static KeyManagerInfoDTO toKeyManagerInfoDTO(RegistrationSummary registrationSummary) {

        KeyManagerInfoDTO keyManagerInfoDTO = new KeyManagerInfoDTO();
        keyManagerInfoDTO.setDcrEndpoint(registrationSummary.getKeyManagerInfo().getDcrEndpoint());
        keyManagerInfoDTO.setIntrospectEndpoint(registrationSummary.getKeyManagerInfo().getIntrospectEndpoint());
        keyManagerInfoDTO.setRevokeEndpoint(registrationSummary.getKeyManagerInfo().getRevokeEndpoint());
        keyManagerInfoDTO.setTokenEndpoint(registrationSummary.getKeyManagerInfo().getTokenEndpoint());
        CredentialsDTO keyManagerCredentials = new CredentialsDTO();
        keyManagerCredentials.setUsername(registrationSummary.getKeyManagerInfo().getCredentials().getUsername());
        keyManagerCredentials.setPassword(registrationSummary.getKeyManagerInfo().getCredentials().getPassword());
        keyManagerInfoDTO.setCredentials(keyManagerCredentials);
        return keyManagerInfoDTO;
    }

    /**
     * Converts RegistrationSummary analytics information into AnalyticsInfoDTO
     *
     * @param registrationSummary the registration summary required by gateway
     * @return AnalyticsInfoDTO
     */
    private static AnalyticsInfoDTO toAnalyticsDTO(RegistrationSummary registrationSummary) {
        AnalyticsInfoDTO analyticsInfoDTO = new AnalyticsInfoDTO();
        analyticsInfoDTO.serverURL(registrationSummary.getAnalyticsInfo().getDasServerURL());
        CredentialsDTO analyticsServerCredentials = new CredentialsDTO();
        analyticsServerCredentials.setUsername(registrationSummary.getAnalyticsInfo().getDasServerCredentials()
                .getUsername());
        analyticsServerCredentials.setPassword(registrationSummary.getAnalyticsInfo().getDasServerCredentials()
                .getPassword());
        analyticsInfoDTO.setCredentials(analyticsServerCredentials);
        return analyticsInfoDTO;
    }

    /**
     * Converts RegistrationSummary JWT information into JWTInfoDTO
     *
     * @param registrationSummary the registration summary required by gateway
     * @return JWTInfoDTO
     */
    private static JWTInfoDTO toJWTInfoDTO(RegistrationSummary registrationSummary) {
        JWTInfoDTO jwtInfoDTO = new JWTInfoDTO();
        jwtInfoDTO.enableJWTGeneration(registrationSummary.getJwtInfo().isEnableJWTGeneration());
        jwtInfoDTO.jwtHeader(registrationSummary.getJwtInfo().getJwtHeader());
        return jwtInfoDTO;
    }

    /**
     * Converts RegistrationSummary Throttling information into ThrottlingInfoDTO
     *
     * @param registrationSummary the registration summary required by gateway
     * @return ThrottlingInfoDTO
     */
    private static ThrottlingInfoDTO toThrottlingInfoDTO(RegistrationSummary registrationSummary) {
        ThrottlingInfoDTO throttlingInfoDTO = new ThrottlingInfoDTO();
        throttlingInfoDTO.serverURL(registrationSummary.getThrottlingInfo().getDataPublisher().getReceiverURL());
        CredentialsDTO throttlingServerCredentials = new CredentialsDTO();
        throttlingServerCredentials.setUsername(registrationSummary.getThrottlingInfo().getDataPublisher()
                .getCredentials().getUsername());
        throttlingServerCredentials.setPassword(registrationSummary.getThrottlingInfo().getDataPublisher()
                .getCredentials().getPassword());
        throttlingInfoDTO.setCredentials(throttlingServerCredentials);
        return throttlingInfoDTO;
    }
}
