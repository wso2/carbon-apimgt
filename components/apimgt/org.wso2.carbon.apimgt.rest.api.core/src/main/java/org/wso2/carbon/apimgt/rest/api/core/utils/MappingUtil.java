/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.core.utils;


import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.RegistrationSummary;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.rest.api.core.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.AnalyticsInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.CredentialsDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.GoogleAnalyticsTrackingInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.JWTInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.KeyManagerInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.PolicyDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationSummaryDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ThrottlingInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.UriTemplateDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for convert apimgt Core objects into rest api model.
 */
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
            subscriptionDTO.setKeyEnvType(subscriptionData.getKeyEnvType());
            subscriptionDTO.setApplicationId(subscriptionData.getApplicationId());
            subscriptionDTO.setStatus(subscriptionData.getStatus());
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
     *
     * @param resourcesOfApi list of uriTemplates
     * @return ResourcesListDTO
     */
    public static List<UriTemplateDTO> convertToResourceListDto(List<UriTemplate> resourcesOfApi) {
        List<UriTemplateDTO> uriTemplateDTOArrayList = new ArrayList<>();
        resourcesOfApi.forEach((v) -> {
            UriTemplateDTO uriTemplateDTO = new UriTemplateDTO();
            uriTemplateDTO.setUriTemplate(v.getUriTemplate());
            uriTemplateDTO.setAuthType(v.getAuthType());
            uriTemplateDTO.setPolicy(v.getPolicy().getUuid());
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
            apiInfo.setSecurityScheme(api.getSecurityScheme());
            apiInfoList.add(apiInfo);
        }
        return apiInfoList;
    }

    /**
     * convert {@link ApplicationDTO} to {@link Application}
     *
     * @param applicationList List of {@link Application}
     * @return ApplicationEvent list
     */
    public static List<ApplicationDTO> convertToApplicationDtoList(List<Application> applicationList) {
        List<ApplicationDTO> applicationDTOList = new ArrayList<>();
        for (Application application : applicationList) {
            ApplicationDTO applicationDTO = new ApplicationDTO();
            applicationDTO.setName(application.getName());
            applicationDTO.setApplicationId(application.getId());
            applicationDTO.setThrottlingTier(application.getPolicy().getUuid());
            applicationDTO.setSubscriber(application.getCreatedUser());
            applicationDTOList.add(applicationDTO);
        }
        return applicationDTOList;
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
        registrationSummaryDTO.setGoogleAnalyticsTrackingInfo(toGoogleAnalyticsTrackingInfoDTO(registrationSummary
                .getGoogleAnalyticsTrackingInfo()));
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
        analyticsInfoDTO.setEnabled(registrationSummary.getAnalyticsInfo().isEnabled());
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

    private static GoogleAnalyticsTrackingInfoDTO toGoogleAnalyticsTrackingInfoDTO(RegistrationSummary.GoogleAnalyticsTrackingInfo
                                                                                           googleAnalyticsTrackingInfo){
        GoogleAnalyticsTrackingInfoDTO googleAnalyticsTrackingInfoDTO = new GoogleAnalyticsTrackingInfoDTO();
        googleAnalyticsTrackingInfoDTO.setEnabled(googleAnalyticsTrackingInfo.isEnabled());
        googleAnalyticsTrackingInfoDTO.setTrackingID(googleAnalyticsTrackingInfo.getTrackingCode());
        return googleAnalyticsTrackingInfoDTO;
    }

    /**
     * Convert policy validation data list to policy dto list
     *
     * @param allPolicies all policies
     * @return PolicyDTO list
     */
    public static List<PolicyDTO> convertToPolicyDtoList(Set<PolicyValidationData> allPolicies) {
        List<PolicyDTO> policyDTOList = new ArrayList<>();
        allPolicies.forEach(v -> {
            PolicyDTO policyDTO = new PolicyDTO();
            policyDTO.setId(v.getId());
            policyDTO.setName(v.getName());
            policyDTO.setStopOnQuotaReach(v.isStopOnQuotaReach());
            policyDTOList.add(policyDTO);
        });
        return policyDTOList;
    }

    /**
     * Converts {@link Endpoint} list to {@link EndPointDTO} list
     *
     * @param endpointList {@link Endpoint} list
     * @return EndPointDTO list
     */
    public static List<EndPointDTO> toEndpointListDto(List<Endpoint> endpointList) {
        List<EndPointDTO> endPointDTOList = new ArrayList<>();
        endpointList.forEach(endpoint -> endPointDTOList.add(new EndPointDTO().endpointConfig(endpoint
                .getEndpointConfig()).id(endpoint.getId()).type(endpoint.getType()).name(endpoint.getName()).security
                (endpoint.getSecurity())));
        return endPointDTOList;
    }

    /**
     * Converts a List of Block Condition in to REST API LIST DTO Object.
     *
     * @param blockConditionList A List of Block Conditions
     * @return REST API List DTO object derived from Block Condition list
     */
    public static BlockingConditionListDTO fromBlockConditionListToListDTO(List<BlockConditions> blockConditionList) {
        BlockingConditionListDTO listDTO = new BlockingConditionListDTO();
        List<BlockingConditionDTO> blockingConditionDTOList = new ArrayList<>();
        if (blockConditionList != null) {
            for (BlockConditions blockCondition : blockConditionList) {
                BlockingConditionDTO dto = fromBlockingConditionToDTO(blockCondition);
                blockingConditionDTOList.add(dto);
            }
        }
        listDTO.setCount(blockingConditionDTOList.size());
        listDTO.setList(blockingConditionDTOList);
        return listDTO;
    }

    /**
     * Converts a single Block Condition model object into REST API DTO object.
     *
     * @param blockCondition Block condition model object
     * @return Block condition DTO object derived from block condition model object
     */
    public static BlockingConditionDTO fromBlockingConditionToDTO(BlockConditions blockCondition) {
        if (blockCondition.getUuid() == null) {
            return null;
        }
        BlockingConditionDTO dto = new BlockingConditionDTO();
        dto.setUuid(blockCondition.getUuid());
        dto.setConditionType(blockCondition.getConditionType());
        dto.setEnabled(blockCondition.isEnabled());
        if (blockCondition.getConditionType().equals(APIMgtConstants.ThrottlePolicyConstants
                .BLOCKING_CONDITION_IP_RANGE)) {
            dto.setStartingIP(APIUtils.ipToLong(blockCondition.getStartingIP()));
            dto.setEndingIP(APIUtils.ipToLong(blockCondition.getEndingIP()));
        }
        String conditionValue = blockCondition.getConditionValue();
        if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP.equals(blockCondition.getConditionType())) {
            dto.setFixedIp(APIUtils.ipToLong(conditionValue));
        }
        dto.setConditionValue(conditionValue);
        return dto;
    }

}
