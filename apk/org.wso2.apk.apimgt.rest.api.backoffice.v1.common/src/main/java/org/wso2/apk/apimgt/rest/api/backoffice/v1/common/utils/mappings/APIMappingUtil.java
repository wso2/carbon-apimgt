/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.apk.apimgt.api.APIDefinition;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.WorkflowStatus;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.model.APICategory;
import org.wso2.apk.apimgt.api.model.APIDeploymentInfo;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.APIStateChangeResponse;
import org.wso2.apk.apimgt.api.model.LifeCycleEvent;
import org.wso2.apk.apimgt.api.model.ResourcePath;
import org.wso2.apk.apimgt.api.model.URITemplate;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.definitions.OASParserUtil;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIAdditionalPropertiesDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIAdditionalPropertiesMapDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIBusinessInformationDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIDeploymentDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIInfoDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIListDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIMonetizationInfoDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIOperationsDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIRevisionDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.LifecycleHistoryDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.LifecycleHistoryItemDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.LifecycleStateAvailableTransitionsDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.LifecycleStateDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.PaginationDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.ResourcePathDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.ResourcePathListDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.WorkflowResponseDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.apk.apimgt.impl.utils.APIUtil.handleExceptionWithCode;

/**
 * This class used for mapping utility to API.
 */
public class APIMappingUtil {

    private static final Log log = LogFactory.getLog(APIMappingUtil.class);

    public static API fromDTOtoAPI(APIDTO dto, String provider) throws APIManagementException {

        String providerEmailDomainReplaced = APIUtil.replaceEmailDomain(provider);

        // The provider name that is coming from the body is not honored for now.
        // Later we can use it by checking admin privileges of the user.
        APIIdentifier apiId = new APIIdentifier(providerEmailDomainReplaced, dto.getName(), dto.getVersion());
        API model = new API(apiId);

        String context = dto.getContext();
        final String originalContext = context;

        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }

        context = context.startsWith("/") ? context : ("/" + context);
        String providerDomain = APIUtil.getTenantDomain(provider);
        if (!APIConstants.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain) && dto.getId() == null
                && !context.contains("/t/" + providerDomain)) {
            //Create tenant aware context for API
            context = "/t/" + providerDomain + context;
        }

        // This is to support the pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        context = checkAndSetVersionParam(context);
        model.setContextTemplate(context);

        context = updateContextWithVersion(dto.getVersion(), originalContext, context);
        model.setContext(context);
        model.setDescription(dto.getDescription());

        model.setType(dto.getType().toString());
        if (dto.getState() != null) {
            model.setStatus((dto.getState() != null) ? dto.getState().value().toUpperCase() : null);
        }

        // URI Templates
        // No default topics for AsyncAPIs. Therefore set URITemplates only for non-AsyncAPIs.
        Set<URITemplate> uriTemplates = getURITemplates(model, dto.getOperations());
        model.setUriTemplates(uriTemplates);

        if (dto.getTags() != null) {
            Set<String> apiTags = new HashSet<>(dto.getTags());
            model.setTags(apiTags);
        }

        model.setApiLevelPolicy(dto.getApiUsagePolicy());

        String transports = StringUtils.join(dto.getTransport(), ',');
        model.setTransports(transports);

        List<APIAdditionalPropertiesDTO> additionalProperties = dto.getAdditionalProperties();
        if (additionalProperties != null) {
            for (APIAdditionalPropertiesDTO property : additionalProperties) {
                if (property.isDisplay()) {
                    model.addProperty(property.getName() + APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX, property
                            .getValue());
                } else {
                    model.addProperty(property.getName(), property.getValue());
                }
            }
        }

        Map<String, APIAdditionalPropertiesMapDTO> additionalPropertiesMap = dto.getAdditionalPropertiesMap();
        if (additionalPropertiesMap != null && !additionalPropertiesMap.isEmpty()) {
            for (Map.Entry<String, APIAdditionalPropertiesMapDTO> entry : additionalPropertiesMap.entrySet()) {
                if (entry.getValue().isDisplay()) {
                    model.addProperty(entry.getKey() + APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX,
                            entry.getValue().getValue());
                } else {
                    model.addProperty(entry.getKey(), entry.getValue().getValue());
                }
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        APIBusinessInformationDTO apiBusinessInformationDTO = objectMapper.convertValue(dto.getBusinessInformation(),
                APIBusinessInformationDTO.class);
        if (apiBusinessInformationDTO != null) {
            model.setBusinessOwner(apiBusinessInformationDTO.getBusinessOwner());
            model.setBusinessOwnerEmail(apiBusinessInformationDTO.getBusinessOwnerEmail());
            model.setTechnicalOwner(apiBusinessInformationDTO.getTechnicalOwner());
            model.setTechnicalOwnerEmail(apiBusinessInformationDTO.getTechnicalOwnerEmail());
        }
        setAPICategoriesToModel(dto, model);
        setRevisionToModel(dto, model);
        setDeploymentsToModel(dto, model);
        return model;
    }

    /**
     * This method populate the API Model with Deployments defined in the APIDTO
     *
     * @param dto   APIDTO
     * @param model API Model
     */
    private static void setDeploymentsToModel(APIDTO dto, API model) {
        List<APIDeploymentInfo> apiDeploymentInfos = new ArrayList<>();
        for (APIDeploymentDTO apiDeploymentDTO : dto.getDeployments()) {
            apiDeploymentInfos.add(new APIDeploymentInfo(apiDeploymentDTO.getName(),
                    apiDeploymentDTO.getDeployedTime().toString()));
        }
        model.setDeploymentInfoList(apiDeploymentInfos);
    }

    /**
     * This method populate the API Model with Revision defined in the APIDTO
     *
     * @param dto   APIDTO
     * @param model API Model
     */
    private static void setRevisionToModel(APIDTO dto, API model) {
        model.setRevisionName(dto.getRevision().getDisplayName());
        model.setRevisionDescription(dto.getRevision().getDescription());
        model.setRevisionCreatedTime(String.valueOf(dto.getRevision().getCreatedTime()));
    }

    /**
     * This method creates the API monetization information DTO.
     *
     * @param apiId        API apiid
     * @param organization identifier of the organization
     * @return monetization information DTO
     * @throws APIManagementException if failed to construct the DTO
     */
    public static APIMonetizationInfoDTO getMonetizationInfoDTO(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(apiId, organization);
        APIMonetizationInfoDTO apiMonetizationInfoDTO = new APIMonetizationInfoDTO();
        //set the information relatated to monetization to the DTO
        apiMonetizationInfoDTO.setEnabled(api.getMonetizationStatus());
        Map<String, String> monetizationPropertiesMap = new HashMap<>();

        if (api.getMonetizationProperties() != null) {
            JSONObject monetizationProperties = api.getMonetizationProperties();
            for (Object propertyKey : monetizationProperties.keySet()) {
                String key = (String) propertyKey;
                monetizationPropertiesMap.put(key, (String) monetizationProperties.get(key));
            }
        }
        apiMonetizationInfoDTO.setProperties(monetizationPropertiesMap);
        return apiMonetizationInfoDTO;
    }

    /**
     * Get map of monetized policies to plan mapping.
     *
     * @param uuid                           apiuuid
     * @param organization                   organization
     * @param monetizedPoliciesToPlanMapping map of monetized policies to plan mapping
     * @return DTO of map of monetized policies to plan mapping
     * @throws APIManagementException if failed to construct the DTO
     */
    public static APIMonetizationInfoDTO getMonetizedTiersDTO(String uuid, String organization,
                                                              Map<String, String> monetizedPoliciesToPlanMapping)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(uuid, organization);
        APIMonetizationInfoDTO apiMonetizationInfoDTO = new APIMonetizationInfoDTO();
        apiMonetizationInfoDTO.setEnabled(api.getMonetizationStatus());
        apiMonetizationInfoDTO.setProperties(monetizedPoliciesToPlanMapping);
        return apiMonetizationInfoDTO;
    }

    /**
     * Returns the APIIdentifier given the uuid.
     *
     * @param apiId API uuid
     * @return APIIdentifier which represents the given id
     * @throws APIManagementException
     */
    public static APIIdentifier getAPIIdentifierFromUUID(String apiId)
            throws APIManagementException {

        return APIUtil.getAPIIdentifierFromUUID(apiId);
    }

    /**
     * Returns an API with minimal info given the uuid.
     *
     * @param apiUUID      API uuid
     * @param organization organization of the API
     * @return API which represents the given id
     * @throws APIManagementException
     */
    public static API getAPIInfoFromUUID(String apiUUID, String organization)
            throws APIManagementException {

        API api;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        api = apiProvider.getLightweightAPIByUUID(apiUUID, organization);
        return api;
    }

    /**
     * Converts a List object of APIs into a DTO.
     *
     * @param apiList List of APIs
     * @return APIListDTO object containing APIDTOs
     */
    public static Object fromAPIListToDTO(List<API> apiList) throws APIManagementException {

        return fromAPIListToInfoDTO(apiList);
    }

    /**
     * Converts a List object of APIs into Info DTO List.
     *
     * @param apiList List of APIs
     * @return APIListDTO object containing APIDTOs
     */
    public static APIListDTO fromAPIListToInfoDTO(List<API> apiList) throws APIManagementException {

        APIListDTO apiListDTO = new APIListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiListDTO.getList();
        for (API api : apiList) {
            apiInfoDTOs.add(fromAPIToInfoDTO(api));
        }
        apiListDTO.setCount(apiInfoDTOs.size());
        return apiListDTO;
    }

    /**
     * Converts a List object of URITemplates into APIOperations DTO List.
     *
     * @param uriTemplateList uriTemplateList
     * @return List of APIOperationsDTO object
     */
    public static List<APIOperationsDTO> fromURITemplateListToOprationList(List<URITemplate> uriTemplateList) {

        int index = 0;
        List<APIOperationsDTO> operations = new ArrayList<>();
        for (URITemplate uriTemplate : uriTemplateList) {
            uriTemplate.setId((index++));
            operations.add(fromURITemplateToOperationList(uriTemplate));
        }
        return operations;
    }

    /**
     * Converts a uriTemplate to APIOperations DTO.
     *
     * @param uriTemplate uriTemplate
     * @return APIOperationsDTO object
     */
    private static APIOperationsDTO fromURITemplateToOperationList(URITemplate uriTemplate) {

        APIOperationsDTO operation = new APIOperationsDTO();
        operation.setId(Integer.toString(uriTemplate.getId()));
        operation.setVerb(uriTemplate.getHTTPVerb());
        operation.setTarget(uriTemplate.getUriTemplate());
        return operation;
    }

    /**
     * Creates a minimal DTO representation of an API object.
     *
     * @param api API object
     * @return a minimal representation DTO
     */
    public static APIInfoDTO fromAPIToInfoDTO(API api) {

        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setDescription(api.getDescription());
        String context = api.getContextTemplate();
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }
        apiInfoDTO.setContext(context);
        apiInfoDTO.setId(api.getUuid());
        APIIdentifier apiId = api.getId();
        apiInfoDTO.setName(apiId.getApiName());
        apiInfoDTO.setVersion(apiId.getVersion());
        apiInfoDTO.setType(api.getType());
        apiInfoDTO.setHasThumbnail(!StringUtils.isBlank(api.getThumbnailUrl()));
        apiInfoDTO.setState(APIInfoDTO.StateEnum.valueOf(api.getStatus()));

        if (api.getCreatedTime() != null) {
            Date createdTime = new Date(Long.parseLong(api.getCreatedTime()));
            apiInfoDTO.setCreatedTime(String.valueOf(createdTime.getTime()));
        }
        if (api.getLastUpdated() != null) {
            Date lastUpdatedTime = api.getLastUpdated();
            apiInfoDTO.setUpdatedTime(String.valueOf(lastUpdatedTime.getTime()));
        }
        return apiInfoDTO;
    }

    /**
     * Sets pagination urls for a APIListDTO object given pagination parameters and url parameters.
     *
     * @param apiListDTO a APIListDTO object
     * @param query      search condition
     * @param limit      max number of objects returned
     * @param offset     starting index
     * @param size       max offset
     */
    public static void setPaginationParams(Object apiListDTO, String query, int offset, int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        ((APIListDTO) apiListDTO).setPagination(paginationDTO);
    }

    private static String checkAndSetVersionParam(String context) {
        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        if (!context.contains(RestApiConstants.API_VERSION_PARAM)) {
            if (!context.endsWith("/")) {
                context = context + "/";
            }
            context = context + RestApiConstants.API_VERSION_PARAM;
        }
        return context;
    }

    private static String getThumbnailUri(String uuid) {

        return RestApiConstants.RESOURCE_PATH_THUMBNAIL.replace(RestApiConstants.APIID_PARAM, uuid);
    }

    private static String updateContextWithVersion(String version, String contextVal, String context) {
        // This condition should not be true for any occasion but we keep it so that there are no loopholes in
        // the flow.
        if (version == null) {
            // context template patterns - /{version}/foo or /foo/{version}
            // if the version is null, then we remove the /{version} part from the context
            context = contextVal.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        } else {
            context = context.replace(RestApiConstants.API_VERSION_PARAM, version);
        }
        return context;
    }

    public static APIDTO fromAPItoDTO(API model) throws APIManagementException {

        return fromAPItoDTO(model, false, null);
    }

    public static APIDTO fromAPItoDTO(API model, APIProvider apiProvider)
            throws APIManagementException {

        return fromAPItoDTO(model, false, apiProvider);
    }

    public static APIDTO fromAPItoDTO(API model, boolean preserveCredentials,
                                      APIProvider apiProviderParam)
            throws APIManagementException {

        APIProvider apiProvider;
        if (apiProviderParam != null) {
            apiProvider = apiProviderParam;
        } else {
            apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        }
        APIDTO dto = new APIDTO();
        dto.setName(model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        dto.setId(model.getUuid());
        String context = model.getContextTemplate();
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }
        dto.setContext(context);
        dto.setCreatedTime(model.getCreatedTime());
        if (model.getLastUpdated() != null) {
            dto.setLastUpdatedTime(Long.toString(model.getLastUpdated().getTime()));
        }
        dto.setDescription(model.getDescription());
        dto.setHasThumbnail(!StringUtils.isBlank(model.getThumbnailUrl()));

        boolean isAsyncAPI = APIDTO.TypeEnum.WS.toString().equals(model.getType())
                || APIDTO.TypeEnum.WEBSUB.toString().equals(model.getType())
                || APIDTO.TypeEnum.SSE.toString().equals(model.getType())
                || APIDTO.TypeEnum.ASYNC.toString().equals(model.getType());

        model.getId().setUuid(model.getUuid());

        String organization = APIUtil.getTenantDomain(APIUtil.replaceEmailDomainBack(model.getId().getProviderName()));

        if (!isAsyncAPI) {
            // Get from swagger definition
            List<APIOperationsDTO> apiOperationsDTO;
            String apiSwaggerDefinition;
            if (model.getSwaggerDefinition() != null) {
                apiSwaggerDefinition = model.getSwaggerDefinition();
            } else {
                apiSwaggerDefinition = apiProvider.getOpenAPIDefinition(model.getUuid(), organization);
            }

            //We will fetch operations from the swagger definition and not from the AM_API_URL_MAPPING table: table
            //entries may have API level throttling tiers listed in case API level throttling is selected for the API.
            //This will lead the x-throttling-tiers of API definition to get overwritten. (wso2/product-apim#11240)
            apiOperationsDTO = getOperationsFromSwaggerDef(model, apiSwaggerDefinition);
            dto.setOperations(apiOperationsDTO);
        } else {
            // Get from asyncapi definition
            List<APIOperationsDTO> apiOperationsDTO = getOperationsFromAPI(model);
            dto.setOperations(apiOperationsDTO);
        }

        Set<String> apiTags = model.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);

        dto.setApiUsagePolicy(model.getApiLevelPolicy());

        //APIs created with type set to "NULL" will be considered as "HTTP"
        if (model.getType() == null || model.getType().toLowerCase().equals("null")) {
            dto.setType(APIDTO.TypeEnum.HTTP);
        } else {
            dto.setType(APIDTO.TypeEnum.fromValue(model.getType()));
        }

        if (!APIConstants.APITransportType.WS.toString().equals(model.getType())) {
            if (StringUtils.isEmpty(model.getTransports())) {
                List<String> transports = new ArrayList<>();
                transports.add(APIConstants.HTTPS_PROTOCOL);

                dto.setTransport(transports);
            }
            dto.setTransport(Arrays.asList(model.getTransports().split(",")));
        }

        if (model.getAdditionalProperties() != null) {
            JSONObject additionalProperties = model.getAdditionalProperties();
            List<APIAdditionalPropertiesDTO> additionalPropertiesList = new ArrayList<>();
            Map<String, APIAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<>();
            for (Object propertyKey : additionalProperties.keySet()) {
                APIAdditionalPropertiesDTO additionalPropertiesDTO = new APIAdditionalPropertiesDTO();
                APIAdditionalPropertiesMapDTO apiInfoAdditionalPropertiesMapDTO =
                        new APIAdditionalPropertiesMapDTO();
                String key = (String) propertyKey;
                int index = key.lastIndexOf(APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX);
                additionalPropertiesDTO.setValue((String) additionalProperties.get(key));
                apiInfoAdditionalPropertiesMapDTO.setValue((String) additionalProperties.get(key));
                if (index > 0) {
                    additionalPropertiesDTO.setName(key.substring(0, index));
                    apiInfoAdditionalPropertiesMapDTO.setName(key.substring(0, index));
                    additionalPropertiesDTO.setDisplay(true);
                } else {
                    additionalPropertiesDTO.setName(key);
                    apiInfoAdditionalPropertiesMapDTO.setName(key);
                    additionalPropertiesDTO.setDisplay(false);
                }
                apiInfoAdditionalPropertiesMapDTO.setDisplay(false);
                additionalPropertiesMap.put(key, apiInfoAdditionalPropertiesMapDTO);
                additionalPropertiesList.add(additionalPropertiesDTO);
            }
            dto.setAdditionalProperties(additionalPropertiesList);
            dto.setAdditionalPropertiesMap(additionalPropertiesMap);
        }

        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(model.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(model.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(model.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(model.getTechnicalOwnerEmail());
        dto.setBusinessInformation(apiBusinessInformationDTO);

        APIMonetizationInfoDTO apiMonetizationInfoDTO = new APIMonetizationInfoDTO();
        apiMonetizationInfoDTO.setEnabled(model.isMonetizationEnabled());
        Map<String, String> monetizationPropertiesMap = new HashMap<>();

        if (model.getMonetizationProperties() != null) {
            JSONObject monetizationProperties = model.getMonetizationProperties();
            for (Object propertyKey : monetizationProperties.keySet()) {
                String key = (String) propertyKey;
                monetizationPropertiesMap.put(key, (String) monetizationProperties.get(key));
            }
        }
        apiMonetizationInfoDTO.setProperties(monetizationPropertiesMap);
        dto.setMonetization(apiMonetizationInfoDTO);

        if (null != model.getLastUpdated()) {
            Date lastUpdateDate = model.getLastUpdated();
            Timestamp timeStamp = new Timestamp(lastUpdateDate.getTime());
            dto.setLastUpdatedTime(String.valueOf(timeStamp));
        }
        if (null != model.getCreatedTime()) {
            Date created = new Date(Long.parseLong(model.getCreatedTime()));
            Timestamp timeStamp = new Timestamp(created.getTime());
            dto.setCreatedTime(String.valueOf(timeStamp.getTime()));
        }
        dto.setState(APIDTO.StateEnum.valueOf(model.getStatus()));
        List<APICategory> apiCategories = model.getApiCategories();
        List<String> categoryNameList = new ArrayList<>();
        if (apiCategories != null && !apiCategories.isEmpty()) {
            for (APICategory category : apiCategories) {
                categoryNameList.add(category.getName());
            }
        }
        dto.setCategories(categoryNameList);

        APIRevisionDTO apiRevisionDTO = new APIRevisionDTO();
        apiRevisionDTO.setId(String.valueOf(model.getRevisionId()));
        apiRevisionDTO.setDisplayName(model.getRevisionName());
        apiRevisionDTO.setDescription(model.getRevisionDescription());
        try {
            apiRevisionDTO.setCreatedTime(parseStringToDate(model.getRevisionCreatedTime()));
        } catch (java.text.ParseException e) {
            String errorMessage = "Error while parsing the revision created time:" + model.getCreatedTime();
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        dto.setRevision(apiRevisionDTO);

        List<APIDeploymentDTO> apiDeploymentDTOList = new ArrayList<>();
        if (apiProvider.getAPIRevisionDeploymentList(model.getRevisionedApiId()) != null) {
            for (APIDeploymentInfo apiDeploymentInfo : model.getDeploymentInfoList()) {
                apiDeploymentDTOList.add(fromAPIDeploymentInfotoDTO(apiDeploymentInfo));
            }
        }
        dto.setDeployments(apiDeploymentDTOList);
        return dto;
    }

    /**
     * Return the REST API DTO representation of API Lifecycle state information.
     *
     * @param apiLCData API lifecycle state information
     * @return REST API DTO representation of API Lifecycle state information
     */
    public static LifecycleStateDTO fromLifecycleModelToDTO(Map<String, Object> apiLCData,
                                                            boolean apiOlderVersionExist) {

        LifecycleStateDTO lifecycleStateDTO = new LifecycleStateDTO();

        String currentState = (String) apiLCData.get(APIConstants.LC_STATUS);
        lifecycleStateDTO.setState(currentState);

        String[] nextStates = (String[]) apiLCData.get(APIConstants.LC_NEXT_STATES);
        if (nextStates != null) {
            List<LifecycleStateAvailableTransitionsDTO> transitionDTOList = new ArrayList<>();
            for (String state : nextStates) {
                LifecycleStateAvailableTransitionsDTO transitionDTO = new LifecycleStateAvailableTransitionsDTO();
                transitionDTO.setEvent(state);
                //todo: Set target state properly
                transitionDTO.setTargetState("");
                transitionDTOList.add(transitionDTO);
            }
            lifecycleStateDTO.setAvailableTransitions(transitionDTOList);
        }
        return lifecycleStateDTO;
    }

    /**
     * Return the REST API DTO representation of API Lifecycle history information.
     *
     * @param lifeCycleEvents API lifecycle history information
     * @return REST API DTO representation of API Lifecycle history information
     */
    public static LifecycleHistoryDTO fromLifecycleHistoryModelToDTO(List<LifeCycleEvent> lifeCycleEvents) {

        LifecycleHistoryDTO historyDTO = new LifecycleHistoryDTO();
        historyDTO.setCount(lifeCycleEvents.size());
        for (LifeCycleEvent event : lifeCycleEvents) {
            LifecycleHistoryItemDTO historyItemDTO = new LifecycleHistoryItemDTO();
            historyItemDTO.setPostState(event.getNewStatus());
            historyItemDTO.setPreviousState(event.getOldStatus());
            historyItemDTO.setUser(event.getUserId());

            String updatedTime = RestApiCommonUtil.getRFC3339Date(event.getDate());
            historyItemDTO.setUpdatedTime(updatedTime);
            historyDTO.getList().add(historyItemDTO);
        }
        return historyDTO;
    }

    /**
     * This method returns URI templates according to the given list of operations.
     *
     * @param operations List operations
     * @return URI Templates
     * @throws APIManagementException
     */
    public static Set<URITemplate> getURITemplates(API model, List<APIOperationsDTO> operations)
            throws APIManagementException {

        boolean isHttpVerbDefined = false;
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();

        if (operations == null || operations.isEmpty()) {
            operations = getDefaultOperationsList(model.getType());
        }

        for (APIOperationsDTO operation : operations) {
            URITemplate template = new URITemplate();

            String uriTempVal = operation.getTarget();

            String httpVerb = operation.getVerb();

            //Only continue for supported operations
            if (APIConstants.SUPPORTED_METHODS.contains(httpVerb.toLowerCase())
                    || (APIConstants.GRAPHQL_SUPPORTED_METHOD_LIST.contains(httpVerb.toUpperCase()))
                    || (APIConstants.WEBSUB_SUPPORTED_METHOD_LIST.contains(httpVerb.toUpperCase()))
                    || (APIConstants.SSE_SUPPORTED_METHOD_LIST.contains(httpVerb.toUpperCase()))
                    || (APIConstants.WS_SUPPORTED_METHOD_LIST.contains(httpVerb.toUpperCase()))) {
                isHttpVerbDefined = true;
                template.setThrottlingTier(operation.getUsagePlan());
                template.setThrottlingTiers(operation.getUsagePlan());
                template.setUriTemplate(uriTempVal);
                template.setHTTPVerb(httpVerb.toUpperCase());
                template.setHttpVerbs(httpVerb.toUpperCase());
                uriTemplates.add(template);
            } else {
                if (APIConstants.GRAPHQL_API.equals(model.getType())) {
                    handleExceptionWithCode(String
                                    .format("The GRAPHQL operation Type '%s' provided for operation '%s' is invalid",
                                            httpVerb, uriTemplates),
                            ExceptionCodes.from(ExceptionCodes.INVALID_OPERATION_TYPE,
                                    "GraphQL", "GraphQL", httpVerb));
                } else if (APIConstants.API_TYPE_WEBSUB.equals(model.getType())) {
                    handleExceptionWithCode(String
                                    .format("The WEBSUB operation Type '%s' provided for operation '%s' is invalid",
                                            httpVerb, uriTemplates),
                            ExceptionCodes.from(ExceptionCodes.INVALID_OPERATION_TYPE,
                                    "WEBSUB", "WEBSUB", httpVerb));
                } else if (APIConstants.API_TYPE_SSE.equals(model.getType())) {
                    handleExceptionWithCode(String
                                    .format("The SSE operation Type '%s' provided for operation '%s' is invalid",
                                            httpVerb, uriTemplates),
                            ExceptionCodes.from(ExceptionCodes.INVALID_OPERATION_TYPE,
                                    "SSE", "SSE", httpVerb));
                } else if (APIConstants.API_TYPE_WS.equals(model.getType())) {
                    handleExceptionWithCode(String
                                    .format("The WEBSOCKET operation Type '%s' provided for operation '%s' is invalid",
                                            httpVerb, uriTemplates),
                            ExceptionCodes.from(ExceptionCodes.INVALID_OPERATION_TYPE,
                                    "WEBSOCKET", "WEBSOCKET", httpVerb));
                } else {
                    handleExceptionWithCode(String
                                    .format("The HTTP operation Type '%s' provided for operation '%s' is invalid",
                                            httpVerb, uriTemplates),
                            ExceptionCodes.from(ExceptionCodes.INVALID_OPERATION_TYPE,
                                    "HTTP", "HTTP", httpVerb));
                }
            }

            if (!isHttpVerbDefined) {
                if (APIConstants.GRAPHQL_API.equals(model.getType())) {
                    handleExceptionWithCode("Operation '" + uriTempVal + "' has global parameters without " +
                            "Operation Type", ExceptionCodes.from(ExceptionCodes.VERB_NOT_FOUND,
                            "operation", "operation"));
                } else if (APIConstants.API_TYPE_WEBSUB.equals(model.getType()) ||
                        APIConstants.API_TYPE_SSE.equals(model.getType())) {
                    handleExceptionWithCode("Topic '" + uriTempVal + "' has global parameters without " +
                            "topic Type", ExceptionCodes.from(ExceptionCodes.VERB_NOT_FOUND,
                            "topic", "topic"));
                } else {
                    handleExceptionWithCode("Resource '" + uriTempVal + "' has global parameters without " +
                            "HTTP methods", ExceptionCodes.from(ExceptionCodes.VERB_NOT_FOUND,
                            "method", "method"));
                }
            }
        }

        return uriTemplates;
    }

    /**
     * Returns a set of operations from a API.
     *
     * @param api API object
     * @return a set of operations from a given swagger definition
     */
    private static List<APIOperationsDTO> getOperationsFromAPI(API api) {

        Set<URITemplate> uriTemplates = api.getUriTemplates();
        List<APIOperationsDTO> operationsDTOList = new ArrayList<>();
        for (URITemplate uriTemplate : uriTemplates) {
            APIOperationsDTO operationsDTO = getOperationFromURITemplate(uriTemplate);
            operationsDTOList.add(operationsDTO);
        }
        return operationsDTOList;
    }

    /**
     * Returns a set of operations from a API
     * Returns a set of operations from a given swagger definition
     *
     * @param api               API object
     * @param swaggerDefinition Swagger definition
     * @return a set of operations from a given swagger definition
     * @throws APIManagementException error while trying to retrieve URI templates of the given API
     */

    private static List<APIOperationsDTO> getOperationsFromSwaggerDef(API api, String swaggerDefinition)
            throws APIManagementException {

        APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerDefinition);
        Set<URITemplate> uriTemplates;
        if (APIConstants.GRAPHQL_API.equals(api.getType())) {
            uriTemplates = api.getUriTemplates();
        } else {
            uriTemplates = apiDefinition.getURITemplates(swaggerDefinition);
        }

        List<APIOperationsDTO> operationsDTOList = new ArrayList<>();
        if (!StringUtils.isEmpty(swaggerDefinition)) {
            for (URITemplate uriTemplate : uriTemplates) {
                APIOperationsDTO operationsDTO = getOperationFromURITemplate(uriTemplate);
                operationsDTOList.add(operationsDTO);
            }
        }
        return operationsDTOList;
    }

    /**
     * Reads the operationPolicies from the API object passed in, and sets them back to the API Operations DTO
     *
     * @param api              API object
     * @param apiOperationsDTO List of API Operations DTO
     */
    private static void setOperationPoliciesToOperationsDTO(API api, List<APIOperationsDTO> apiOperationsDTO) {

        Set<URITemplate> uriTemplates = api.getUriTemplates();

        Map<String, URITemplate> uriTemplateMap = new HashMap<>();
        for (URITemplate uriTemplate : uriTemplates) {
            String key = uriTemplate.getUriTemplate() + ":" + uriTemplate.getHTTPVerb();
            uriTemplateMap.put(key, uriTemplate);
        }
    }

    /**
     * Converts a URI template object to a REST API DTO.
     *
     * @param uriTemplate URI Template object
     * @return REST API DTO representing URI template object
     */
    private static APIOperationsDTO getOperationFromURITemplate(URITemplate uriTemplate) {

        APIOperationsDTO operationsDTO = new APIOperationsDTO();
        operationsDTO.setId(""); //todo: Set ID properly
        operationsDTO.setVerb(uriTemplate.getHTTPVerb());
        operationsDTO.setTarget(uriTemplate.getUriTemplate());
        return operationsDTO;
    }

    /**
     * Returns a default operations list with wildcard resources and http verbs.
     *
     * @return a default operations list
     */
    private static List<APIOperationsDTO> getDefaultOperationsList(String apiType) {

        List<APIOperationsDTO> operationsDTOs = new ArrayList<>();
        String[] supportedMethods;

        if (apiType.equals(APIConstants.GRAPHQL_API)) {
            supportedMethods = APIConstants.GRAPHQL_SUPPORTED_METHODS;
        } else if (apiType.equals(APIConstants.API_TYPE_SOAP)) {
            supportedMethods = APIConstants.SOAP_DEFAULT_METHODS;
        } else if (apiType.equals(APIConstants.API_TYPE_WEBSUB)) {
            supportedMethods = APIConstants.WEBSUB_SUPPORTED_METHODS;
        } else if (apiType.equals(APIConstants.API_TYPE_SSE)) {
            supportedMethods = APIConstants.SSE_SUPPORTED_METHODS;
        } else if (apiType.equals(APIConstants.API_TYPE_WS)) {
            supportedMethods = APIConstants.WS_SUPPORTED_METHODS;
        } else {
            supportedMethods = APIConstants.HTTP_DEFAULT_METHODS;
        }

        for (String verb : supportedMethods) {
            APIOperationsDTO operationsDTO = new APIOperationsDTO();
            if (apiType.equals((APIConstants.API_TYPE_WEBSUB))) {
                operationsDTO.setTarget(APIConstants.WEBSUB_DEFAULT_TOPIC_NAME);
            } else {
                operationsDTO.setTarget("/*");
            }
            operationsDTO.setVerb(verb);
            operationsDTOs.add(operationsDTO);
        }
        return operationsDTOs;
    }

    /**
     * Converts a List object of API resource paths into a DTO.
     *
     * @param resourcePathList List of API resource paths
     * @param limit            maximum number of API resource paths to be returned
     * @param offset           starting index
     * @return ResourcePathListDTO object containing ResourcePathDTOs
     */
    public static ResourcePathListDTO fromResourcePathListToDTO(List<ResourcePath> resourcePathList, int limit,
                                                                int offset) {

        ResourcePathListDTO resourcePathListDTO = new ResourcePathListDTO();
        List<ResourcePathDTO> resourcePathDTOs = new ArrayList<>();

        //identifying the proper start and end indexes
        int size = resourcePathList.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = Math.min(offset + limit - 1, size - 1);

        for (int i = start; i <= end; i++) {
            ResourcePath path = resourcePathList.get(i);
            ResourcePathDTO dto = new ResourcePathDTO();
            dto.setId(path.getId());
            dto.setResourcePath(path.getResourcePath());
            dto.setHttpVerb(path.getHttpVerb());
            resourcePathDTOs.add(dto);
        }

        resourcePathListDTO.setCount(resourcePathDTOs.size());
        resourcePathListDTO.setList(resourcePathDTOs);
        return resourcePathListDTO;
    }

    /**
     * Sets pagination urls for a ResourcePathListDTO object.
     *
     * @param resourcePathListDTO ResourcePathListDTO object to which pagination urls need to be set
     * @param offset              starting index
     * @param limit               max number of returned objects
     * @param size                max offset
     */
    public static void setPaginationParamsForAPIResourcePathList(ResourcePathListDTO resourcePathListDTO, int offset,
                                                                 int limit, int size) {
        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getResourcePathPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getResourcePathPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        resourcePathListDTO.setPagination(paginationDTO);
    }

    /**
     * Returns workflow state DTO from the provided information.
     *
     * @param lifecycleStateDTO   Lifecycle state DTO
     * @param stateChangeResponse workflow response from API lifecycle change
     * @return workflow state DTO
     */
    public static WorkflowResponseDTO toWorkflowResponseDTO(LifecycleStateDTO lifecycleStateDTO,
                                                            APIStateChangeResponse stateChangeResponse) {

        WorkflowResponseDTO workflowResponseDTO = new WorkflowResponseDTO();

        if (WorkflowStatus.APPROVED.toString().equals(stateChangeResponse.getStateChangeStatus())) {
            workflowResponseDTO.setWorkflowStatus(WorkflowResponseDTO.WorkflowStatusEnum.APPROVED);
        } else if (WorkflowStatus.CREATED.toString().equals(stateChangeResponse.getStateChangeStatus())) {
            workflowResponseDTO.setWorkflowStatus(WorkflowResponseDTO.WorkflowStatusEnum.CREATED);
        } else if ((WorkflowStatus.REGISTERED.toString().equals(stateChangeResponse.getStateChangeStatus()))) {
            workflowResponseDTO.setWorkflowStatus(WorkflowResponseDTO.WorkflowStatusEnum.REGISTERED);
        } else if ((WorkflowStatus.REJECTED.toString().equals(stateChangeResponse.getStateChangeStatus()))) {
            workflowResponseDTO.setWorkflowStatus(WorkflowResponseDTO.WorkflowStatusEnum.REJECTED);
        } else {
            log.error("Unrecognized state : " + stateChangeResponse.getStateChangeStatus());
            workflowResponseDTO.setWorkflowStatus(WorkflowResponseDTO.WorkflowStatusEnum.CREATED);
        }

        workflowResponseDTO.setLifecycleState(lifecycleStateDTO);
        return workflowResponseDTO;
    }

    /**
     * Set API categories to API or APIProduct based on the instance type of the DTO object passes.
     *
     * @param dto   APIDTO or APIProductDTO
     * @param model API or APIProduct
     */
    private static void setAPICategoriesToModel(APIDTO dto, API model) {

        List<String> apiCategoryNames = dto.getCategories();
        List<APICategory> apiCategories = new ArrayList<>();
        for (String categoryName : apiCategoryNames) {
            APICategory category = new APICategory();
            category.setName(categoryName);
            apiCategories.add(category);
        }
        model.setApiCategories(apiCategories);
    }

    private static APIDeploymentDTO fromAPIDeploymentInfotoDTO(APIDeploymentInfo model)
            throws APIManagementException {

        APIDeploymentDTO apiRevisionDeploymentDTO = new APIDeploymentDTO();
        apiRevisionDeploymentDTO.setName(model.getName());
        if (model.getDeployedTime() != null) {
            try {
                apiRevisionDeploymentDTO.setDeployedTime(parseStringToDate(model.getDeployedTime()));
            } catch (java.text.ParseException e) {
                String errorMessage = "Error while parsing the deployed time:" + model.getDeployedTime();
                throw new APIManagementException(errorMessage, e,
                        ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
            }
        }
        return apiRevisionDeploymentDTO;
    }

    private static Date parseStringToDate(String time) throws java.text.ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.parse(time);
    }
}
