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

package org.wso2.carbon.apimgt.rest.api.publisher.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_businessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_corsConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_endpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_operationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPoint_endpointSecurityDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowResponseDTO.WorkflowStatusEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MappingUtil {

    /**
     * This method converts the API Object from models into APIDTO object.
     *
     * @param api API object
     * @return APIDTO object with provided API object
     */
    public static APIDTO toAPIDto(API api) throws IOException {
        APIDTO apidto = new APIDTO();
        apidto.setId(api.getId());
        apidto.setName(api.getName());
        apidto.version(api.getVersion());
        apidto.setContext(api.getContext());
        apidto.setDescription(api.getDescription());
        apidto.setIsDefaultVersion(api.isDefaultVersion());
        apidto.setVisibility(APIDTO.VisibilityEnum.valueOf(api.getVisibility().toString()));
        apidto.setResponseCaching(Boolean.toString(api.isResponseCachingEnabled()));
        apidto.setCacheTimeout(api.getCacheTimeout());
        apidto.setVisibleRoles(new ArrayList<>(api.getVisibleRoles()));
        apidto.setProvider(api.getProvider());
        apidto.setPermission(api.getApiPermission());
        apidto.setLifeCycleStatus(api.getLifeCycleStatus());
        apidto.setWorkflowStatus(api.getWorkflowStatus());
        apidto.setTags(new ArrayList<>(api.getTags()));
        apidto.setLabels(new ArrayList<>(api.getLabels()));
        apidto.setTransport(new ArrayList<>(api.getTransport()));
        apidto.setUserPermissionsForApi(api.getUserSpecificApiPermissions());
        api.getPolicies().forEach(apidto::addPoliciesItem);
        BusinessInformation businessInformation = api.getBusinessInformation();
        API_businessInformationDTO apiBusinessInformationDTO = new API_businessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(businessInformation.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(businessInformation.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(businessInformation.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(businessInformation.getTechnicalOwnerEmail());
        apidto.setBusinessInformation(apiBusinessInformationDTO);
        CorsConfiguration corsConfiguration = api.getCorsConfiguration();
        API_corsConfigurationDTO apiCorsConfigurationDTO = new API_corsConfigurationDTO();
        apiCorsConfigurationDTO.setAccessControlAllowCredentials(corsConfiguration.isAllowCredentials());
        apiCorsConfigurationDTO.setAccessControlAllowHeaders(corsConfiguration.getAllowHeaders());
        apiCorsConfigurationDTO.setAccessControlAllowMethods(corsConfiguration.getAllowMethods());
        apiCorsConfigurationDTO.setAccessControlAllowOrigins(corsConfiguration.getAllowOrigins());
        apiCorsConfigurationDTO.setCorsConfigurationEnabled(corsConfiguration.isEnabled());
        apidto.setCorsConfiguration(apiCorsConfigurationDTO);
        apidto.setEndpoint(fromEndpointToList(api.getEndpoint()));
        for (UriTemplate uriTemplate : api.getUriTemplates().values()) {
            API_operationsDTO apiOperationsDTO = new API_operationsDTO();
            apiOperationsDTO.setId(uriTemplate.getTemplateId());
            apiOperationsDTO.setUritemplate(uriTemplate.getUriTemplate());
            apiOperationsDTO.setAuthType(uriTemplate.getAuthType());
            apiOperationsDTO.setEndpoint(fromEndpointToList(uriTemplate.getEndpoint()));
            apiOperationsDTO.setHttpVerb(uriTemplate.getHttpVerb());
            apiOperationsDTO.setPolicy(uriTemplate.getPolicy());
            apidto.addOperationsItem(apiOperationsDTO);
        }
        apidto.setCreatedTime(api.getCreatedTime().toString());
        apidto.setLastUpdatedTime(api.getLastUpdatedTime().toString());
        return apidto;
    }

    private static List<API_endpointDTO> fromEndpointToList(Map<String, Endpoint> endpoint) throws IOException {
        List<API_endpointDTO> endpointDTOs = new ArrayList<>();
        if (endpoint != null) {
            for (Map.Entry<String, Endpoint> entry : endpoint.entrySet()) {
                API_endpointDTO endpointDTO = new API_endpointDTO();
                if (APIMgtConstants.API_SPECIFIC_ENDPOINT.equals(entry.getValue().getApplicableLevel())) {
                    endpointDTO.setInline(toEndPointDTO(entry.getValue()));
                } else {
                    endpointDTO.setKey(entry.getValue().getId());
                }
                endpointDTO.setType(entry.getKey());
                endpointDTOs.add(endpointDTO);
            }
        }
        return endpointDTOs;
    }

    /**
     * This method converts the API model object from the DTO object.
     *
     * @param apidto APIDTO object with API data
     * @return APIBuilder object
     */
    public static API.APIBuilder toAPI(APIDTO apidto) throws JsonProcessingException {
        BusinessInformation businessInformation = new BusinessInformation();
        API_businessInformationDTO apiBusinessInformationDTO = apidto.getBusinessInformation();
        if (apiBusinessInformationDTO != null) {
            businessInformation.setBusinessOwner(apiBusinessInformationDTO.getBusinessOwner());
            businessInformation.setBusinessOwnerEmail(apiBusinessInformationDTO.getBusinessOwnerEmail());
            businessInformation.setTechnicalOwner(apiBusinessInformationDTO.getTechnicalOwner());
            businessInformation.setTechnicalOwnerEmail(apiBusinessInformationDTO.getTechnicalOwnerEmail());
        }

        API_corsConfigurationDTO apiCorsConfigurationDTO = apidto.getCorsConfiguration();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        if (apiCorsConfigurationDTO != null) {
            corsConfiguration.setAllowCredentials(apiCorsConfigurationDTO.getAccessControlAllowCredentials());
            corsConfiguration.setAllowHeaders(apiCorsConfigurationDTO.getAccessControlAllowHeaders());
            corsConfiguration.setAllowMethods(apiCorsConfigurationDTO.getAccessControlAllowMethods());
            corsConfiguration.setAllowOrigins(apiCorsConfigurationDTO.getAccessControlAllowOrigins());
            corsConfiguration.setEnabled(apiCorsConfigurationDTO.getCorsConfigurationEnabled());
        }
        List<API_operationsDTO> operationList = apidto.getOperations();
        Map<String, UriTemplate> uriTemplateList = new HashMap<>();
        for (API_operationsDTO operationsDTO : operationList) {
            UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
            uriTemplateBuilder.uriTemplate(operationsDTO.getUritemplate());
            uriTemplateBuilder.authType(operationsDTO.getAuthType());
            uriTemplateBuilder.httpVerb(operationsDTO.getHttpVerb());
            uriTemplateBuilder.policy(operationsDTO.getPolicy());
            if (operationsDTO.getEndpoint() != null && !operationsDTO.getEndpoint().isEmpty()) {
                uriTemplateBuilder.endpoint(fromEndpointListToMap(operationsDTO.getEndpoint()));
            }
            if (operationsDTO.getId() != null) {
                uriTemplateBuilder.templateId(operationsDTO.getId());
            } else {
                uriTemplateBuilder.templateId(APIUtils.generateOperationIdFromPath(operationsDTO.getUritemplate(),
                        operationsDTO.getHttpVerb()));
            }
            uriTemplateList.put(uriTemplateBuilder.getTemplateId(), uriTemplateBuilder.build());
        }
        API.APIBuilder apiBuilder = new API.APIBuilder(apidto.getProvider(), apidto.getName(), apidto.getVersion()).
                id(apidto.getId()).
                context(apidto.getContext()).
                description(apidto.getDescription()).
                lifeCycleStatus(apidto.getLifeCycleStatus()).
                endpoint(fromEndpointListToMap(apidto.getEndpoint())).
                visibleRoles(new HashSet<>(apidto.getVisibleRoles())).
                policies(new HashSet<>(apidto.getPolicies())).
                permission(apidto.getPermission()).
                tags(new HashSet<>(apidto.getTags())).
                labels(new HashSet<>(apidto.getLabels())).
                transport(new HashSet<>(apidto.getTransport())).
                isResponseCachingEnabled(Boolean.valueOf(apidto.getResponseCaching())).
                policies(new HashSet<>(apidto.getPolicies())).
                businessInformation(businessInformation).
                uriTemplates(uriTemplateList).
                apiType(ApiType.STANDARD).  // Support Standard API creation from publisher
                corsConfiguration(corsConfiguration);
        if (apidto.getIsDefaultVersion() != null) {
            apiBuilder.isDefaultVersion(apidto.getIsDefaultVersion());
        }
        if (apidto.getVisibility() != null) {
            apiBuilder.visibility(API.Visibility.valueOf(apidto.getVisibility().toString()));
        }
        if (apidto.getCacheTimeout() != null) {
            apiBuilder.cacheTimeout(apidto.getCacheTimeout());
        }
        return apiBuilder;
    }

    private static Map<String, Endpoint> fromEndpointListToMap(List<API_endpointDTO> endpoint) throws
            JsonProcessingException {
        Map<String, Endpoint> endpointMap = new HashMap<>();
        for (API_endpointDTO endpointDTO : endpoint) {
            if (!StringUtils.isEmpty(endpointDTO.getKey())){
                endpointMap.put(endpointDTO.getType(), new Endpoint.Builder().id(endpointDTO.getKey())
                        .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build());
            }if (endpointDTO.getInline() != null){
                endpointMap.put(endpointDTO.getType(), toEndpoint(endpointDTO.getInline()));
            }
        }
        return endpointMap;
    }

    /**
     * Converts {@link API} List to an {@link APIInfoDTO} List.
     *
     * @param apiSummaryList
     * @return
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
            apiInfo.setWorkflowStatus(apiSummary.getWorkflowStatus());
            apiInfo.setUserPermissionsForApi(apiSummary.getUserSpecificApiPermissions());
            apiInfoList.add(apiInfo);
        }
        return apiInfoList;
    }

    /**
     * Converts API list to APIListDTO list.
     *
     * @param apisResult List of APIs
     * @return APIListDTO object
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
     * this  method convert Model object into Dto
     *
     * @param documentInfo object containing document information
     * @return DTO object containing document data
     */
    public static DocumentDTO toDocumentDTO(DocumentInfo documentInfo) {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setName(documentInfo.getName());
        documentDTO.setDocumentId(documentInfo.getId());
        documentDTO.setOtherTypeName(documentInfo.getOtherType());
        documentDTO.setSourceType(DocumentDTO.SourceTypeEnum.fromValue(documentInfo.getSourceType().getType()));
        documentDTO.setSourceUrl(documentInfo.getSourceURL());
        documentDTO.setFileName(documentInfo.getFileName());
        documentDTO.setSummary(documentInfo.getSummary());
        documentDTO.setVisibility(DocumentDTO.VisibilityEnum.fromValue(documentInfo.getVisibility().toString()));
        documentDTO.setType(DocumentDTO.TypeEnum.fromValue(documentInfo.getType().toString()));
        return documentDTO;
    }

    /**
     * This method convert the Dto object into Model
     *
     * @param documentDTO Contains data of a document
     * @return DocumentInfo model instance with document data
     */
    public static DocumentInfo toDocumentInfo(DocumentDTO documentDTO) {
        return new DocumentInfo.Builder().
                id(documentDTO.getDocumentId()).
                summary(documentDTO.getSummary()).
                name(documentDTO.getName()).
                otherType(documentDTO.getOtherTypeName()).
                sourceType(DocumentInfo.SourceType.valueOf(documentDTO.getSourceType().toString())).
                sourceURL(documentDTO.getSourceUrl()).
                type(DocumentInfo.DocType.valueOf(documentDTO.getType().toString())).
                permission(documentDTO.getPermission()).
                visibility(DocumentInfo.Visibility.valueOf(documentDTO.getVisibility().toString())).build();

    }

    /**
     * This method converts documentInfoResults to documentListDTO
     *
     * @param documentInfoResults list of document which return as results
     * @return DTO cotaning document list
     */
    public static DocumentListDTO toDocumentListDTO(List<DocumentInfo> documentInfoResults) {
        DocumentListDTO documentListDTO = new DocumentListDTO();
        for (DocumentInfo documentInfo : documentInfoResults) {
            documentListDTO.addListItem(toDocumentDTO(documentInfo));
        }
        return documentListDTO;
    }


    /**
     * This method convert Application model to ApplicationDTO
     * @param application Contains application data
     * @return DTO containing application data
     */
    public static ApplicationDTO toApplicationDto(Application application) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(application.getId());
        applicationDTO.setDescription(application.getDescription());
        applicationDTO.setGroupId(application.getGroupId());
        applicationDTO.setName(application.getName());
        applicationDTO.setSubscriber(application.getCreatedUser());
        applicationDTO.setThrottlingTier(application.getTier());
        return applicationDTO;
    }

    /**
     * Converts Subscription model into SubscriptionListDTO object
     *
     * @param subscriptionList list of subscriptions
     * @param limit            no of items to return
     * @param offset value to offset
     * @return SubscriptionListDTO containing subscriptions
     */
    public static SubscriptionListDTO fromSubscriptionListToDTO(List<Subscription> subscriptionList, Integer limit,
                                                                Integer offset) {
        SubscriptionListDTO subscriptionListDTO = new SubscriptionListDTO();
        for (Subscription subscription : subscriptionList) {
            subscriptionListDTO.addListItem(fromSubscription(subscription));
        }
        return subscriptionListDTO;
    }

    /**
     * Converts Subscription to SubscriptionDTO
     *
     * @param subscription subscription model containg subscription details
     * @return SubscriptionDTO containing subscription list
     */
    public static SubscriptionDTO fromSubscription(Subscription subscription) {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getId());
        subscriptionDTO.setSubscriptionStatus(
                SubscriptionDTO.SubscriptionStatusEnum.fromValue(subscription.getStatus().toString()));
        subscriptionDTO.setApplicationInfo(toApplicationDto(subscription.getApplication()));
        subscriptionDTO.setSubscriptionTier(subscription.getSubscriptionTier());
        return subscriptionDTO;
    }

    /**
     * Convert Endpoint to EndPointDTO
     *
     * @param endpoint endpoint model instance
     * @return EndPointDTO instance containing endpoint data
     */
    public static EndPointDTO toEndPointDTO(Endpoint endpoint) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        EndPointDTO endPointDTO = new EndPointDTO();
        endPointDTO.setId(endpoint.getId());
        endPointDTO.setName(endpoint.getName());
        endPointDTO.setEndpointConfig(endpoint.getEndpointConfig());
        EndPoint_endpointSecurityDTO endpointSecurityDTO = mapper.readValue(endpoint.getSecurity(),
                EndPoint_endpointSecurityDTO.class);
        if(endpointSecurityDTO.getEnabled()){
            endpointSecurityDTO.setPassword("");
        }
        endPointDTO.setEndpointSecurity(endpointSecurityDTO);
        endPointDTO.setMaxTps(endpoint.getMaxTps());
        endPointDTO.setType(endpoint.getType());
        return endPointDTO;
    }

    /**
     * Convert EndPointDTO to Endpoint
     *
     * @param endPointDTO Contains data of a endpoint
     * @return Endpoint model instance containing endpoint data
     */
    public static Endpoint toEndpoint(EndPointDTO endPointDTO) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Endpoint.Builder endPointBuilder = new Endpoint.Builder();
        endPointBuilder.endpointConfig(endPointDTO.getEndpointConfig());
        endPointBuilder.name(endPointDTO.getName());
        if (!StringUtils.isEmpty(endPointDTO.getId())) {
            endPointBuilder.id(endPointDTO.getId());
        } else {
            endPointBuilder.id(UUID.randomUUID().toString());
        }
        if (endPointDTO.getMaxTps() != null){
            endPointBuilder.maxTps(endPointDTO.getMaxTps());
        }
        endPointBuilder.security(mapper.writeValueAsString(endPointDTO.getEndpointSecurity()));
        endPointBuilder.type(endPointDTO.getType());
        endPointBuilder.applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT);
        return endPointBuilder.build();
    }

    /**
     * Convert list of Label to LabelListDTO
     *
     * @param labels List of labels
     * @return LabelListDTO list containing label data
     */
    public static LabelListDTO toLabelListDTO(List<Label> labels) {
        LabelListDTO labelListDTO = new LabelListDTO();
        labelListDTO.setCount(labels.size());
        labelListDTO.setList(toLabelDTO(labels));
        return labelListDTO;
    }

    /**
     * Converts label List to LabelListDTO} List.
     *
     * @param labels list of labels
     * @return LabelDTO list
     */
    private static List<LabelDTO> toLabelDTO(List<Label> labels) {
        List<LabelDTO> labelDTOs = new ArrayList<>();
        for (Label label : labels) {
            LabelDTO labelDTO = new LabelDTO();
            labelDTO.setLabelId(label.getId());
            labelDTO.setName(label.getName());
            labelDTO.setAccessUrls(label.getAccessUrls());
            labelDTOs.add(labelDTO);
        }
        return labelDTOs;
    }
    /**
     * Map WorkflowResponse to WorkflowResponseDTO
     * @param response WorkflowResponse object
     * @return WorkflowResponseDTO mapped WorkflowResponseDTO
     */
    public static WorkflowResponseDTO toWorkflowResponseDTO(WorkflowResponse response) {
        WorkflowResponseDTO responseDTO = new WorkflowResponseDTO();
        responseDTO.setWorkflowStatus(WorkflowStatusEnum.valueOf(response.getWorkflowStatus().toString()));
        responseDTO.setJsonPayload(response.getJSONPayload());
        return responseDTO;
    }

}
