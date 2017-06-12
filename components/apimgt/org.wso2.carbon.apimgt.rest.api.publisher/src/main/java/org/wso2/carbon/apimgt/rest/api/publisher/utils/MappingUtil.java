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
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.models.WSDLInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDefinitionValidationResponse_wsdlInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDefinitionValidationResponse_wsdlInfo_endpointsDTO;
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
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for mapping rest api Models to Core
 */
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
        apidto.setSecurityScheme(mapSecuritySchemeIntToList(api.getSecurityScheme()));
        for (Policy policy : api.getPolicies()) {
            apidto.addPoliciesItem(policy.getPolicyName());
        }
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
            apiOperationsDTO.setPolicy(uriTemplate.getPolicy().getPolicyName());
            apidto.addOperationsItem(apiOperationsDTO);
        }
        if (api.getApiPolicy() != null) {
            apidto.setApiPolicy(api.getApiPolicy().getPolicyName());
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
            uriTemplateBuilder.policy(new APIPolicy(operationsDTO.getPolicy()));
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
        Set<Policy> subscriptionPolicies = new HashSet<>();
        apidto.getPolicies().forEach(v-> subscriptionPolicies.add(new SubscriptionPolicy(v)));
        API.APIBuilder apiBuilder = new API.APIBuilder(apidto.getProvider(), apidto.getName(), apidto.getVersion()).
                id(apidto.getId()).
                context(apidto.getContext()).
                description(apidto.getDescription()).
                lifeCycleStatus(apidto.getLifeCycleStatus()).
                endpoint(fromEndpointListToMap(apidto.getEndpoint())).
                visibleRoles(new HashSet<>(apidto.getVisibleRoles())).
                policies(subscriptionPolicies).
                apiPermission(apidto.getPermission()).
                tags(new HashSet<>(apidto.getTags())).
                labels(new HashSet<>(apidto.getLabels())).
                transport(new HashSet<>(apidto.getTransport())).
                isResponseCachingEnabled(Boolean.valueOf(apidto.getResponseCaching())).
                businessInformation(businessInformation).
                uriTemplates(uriTemplateList).
                corsConfiguration(corsConfiguration).
                wsdlUri(apidto.getWsdlUri()).
                securityScheme(mapSecuritySchemeListToInt(apidto.getSecurityScheme()));

        if (apidto.getIsDefaultVersion() != null) {
            apiBuilder.isDefaultVersion(apidto.getIsDefaultVersion());
        }
        if (apidto.getVisibility() != null) {
            apiBuilder.visibility(API.Visibility.valueOf(apidto.getVisibility().toString()));
        }
        if (apidto.getCacheTimeout() != null) {
            apiBuilder.cacheTimeout(apidto.getCacheTimeout());
        }
        if (apidto.getApiPolicy() != null) {
            Policy policy = new APIPolicy(apidto.getApiPolicy());
            apiBuilder.apiPolicy(policy);
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
            }
            if (endpointDTO.getInline() != null) {
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
            apiInfo.setSecurityScheme(mapSecuritySchemeIntToList(apiSummary.getSecurityScheme()));
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
                fileName(documentDTO.getFileName()).
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
        documentListDTO.setCount(documentInfoResults.size());
        return documentListDTO;
    }


    /**
     * This method convert Application model to ApplicationEvent
     * @param application Contains application data
     * @return DTO containing application data
     */
    public static ApplicationDTO toApplicationDto(Application application) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(application.getId());
        applicationDTO.setDescription(application.getDescription());
        applicationDTO.setName(application.getName());
        applicationDTO.setSubscriber(application.getCreatedUser());
        if (application.getPolicy() != null) {
            applicationDTO.setThrottlingTier(application.getPolicy().getPolicyName());
        }
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
        //TODO need to change when pagination implementation goes on
        subscriptionListDTO.count(subscriptionList.size());
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
        subscriptionDTO.setPolicy(subscription.getPolicy().getPolicyName());
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
        if (endpointSecurityDTO.getEnabled()) {
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

    /**
     * Map WSDLInfo to APIDefinitionValidationResponseDTO
     * 
     * @param info WSDLInfo object
     * @return {@link APIDefinitionValidationResponseDTO} based on provided {@link WSDLInfo} object
     */
    public static APIDefinitionValidationResponseDTO toWSDLValidationResponseDTO(WSDLInfo info) {
        APIDefinitionValidationResponseDTO wsdlValidationResponseDTO = new APIDefinitionValidationResponseDTO();
        wsdlValidationResponseDTO.setIsValid(info.getVersion() != null);

        APIDefinitionValidationResponse_wsdlInfoDTO infoDTO = new APIDefinitionValidationResponse_wsdlInfoDTO();
        infoDTO.setVersion(info.getVersion());
        APIDefinitionValidationResponse_wsdlInfo_endpointsDTO endpointsDTO;

        if (info.getEndpoints() != null) {
            for (String endpointName : info.getEndpoints().keySet()) {
                endpointsDTO = new APIDefinitionValidationResponse_wsdlInfo_endpointsDTO();
                endpointsDTO.setName(endpointName);
                endpointsDTO.setLocation(info.getEndpoints().get(endpointName));
                infoDTO.addEndpointsItem(endpointsDTO);
            }
        }

        //currently operations are supported only in WSDL 1.1
        if (APIMgtConstants.WSDLConstants.WSDL_VERSION_11.equals(info.getVersion())) {
            APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO bindingInfoDTO
                    = new APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO();
            bindingInfoDTO.setHasHttpBinding(info.hasHttpBindingOperations());
            bindingInfoDTO.setHasSoapBinding(info.hasSoapBindingOperations());
            infoDTO.setBindingInfo(bindingInfoDTO);
        }

        wsdlValidationResponseDTO.setWsdlInfo(infoDTO);
        return wsdlValidationResponseDTO;
    }

    /**
     * This method maps the security scheme list in to an integer values
     *
     * @param securityScheme security schemes list
     * @return security scheme integer value
     */
    public static int mapSecuritySchemeListToInt(List<String> securityScheme) {
        int securitySchemeValue = 0;
        for(String scheme : securityScheme) {
            switch (scheme) {
                case "Oauth" : securitySchemeValue = securitySchemeValue | 2;
                    break;
                case "apikey" : securitySchemeValue = securitySchemeValue | 1;
                    break;
                default:break;
            }
        }

        if (securityScheme.isEmpty()) {
            securitySchemeValue = 1;
        }

        return securitySchemeValue;
    }

    /**
     * This method maps the security scheme int in to a string list
     *
     * @param securityScheme security schemes int value
     * @return security scheme list
     */
    public static List<String> mapSecuritySchemeIntToList(int securityScheme) {
        List<String> securitySchemesList = new ArrayList<String>();
        if ((securityScheme & 1) == 1) { //Oauth
            securitySchemesList.add("Oauth");
        }
        if ((securityScheme & 2) == 2) { //apikey
            securitySchemesList.add("apikey");
        }
        return securitySchemesList;
    }

}
