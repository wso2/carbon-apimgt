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

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONTokener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.apk.apimgt.api.APIDefinition;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.FaultGatewaysException;
import org.wso2.apk.apimgt.api.MonetizationException;
import org.wso2.apk.apimgt.api.dto.EnvironmentPropertiesDTO;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.APIResourceMediationPolicy;
import org.wso2.apk.apimgt.api.model.APIRevision;
import org.wso2.apk.apimgt.api.model.APIRevisionDeployment;
import org.wso2.apk.apimgt.api.model.APIStateChangeResponse;
import org.wso2.apk.apimgt.api.model.APIStore;
import org.wso2.apk.apimgt.api.model.ApiTypeWrapper;
import org.wso2.apk.apimgt.api.model.Environment;
import org.wso2.apk.apimgt.api.model.Monetization;
import org.wso2.apk.apimgt.api.model.ResourceFile;
import org.wso2.apk.apimgt.api.model.ResourcePath;
import org.wso2.apk.apimgt.api.model.ServiceEntry;
import org.wso2.apk.apimgt.api.model.SwaggerData;
import org.wso2.apk.apimgt.api.model.Tier;
import org.wso2.apk.apimgt.api.model.URITemplate;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.definitions.OAS2Parser;
import org.wso2.apk.apimgt.impl.definitions.OAS3Parser;
import org.wso2.apk.apimgt.impl.definitions.OASParserUtil;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.impl.wsdl.util.SOAPOperationBindingUtils;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings.APIMappingUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings.ExternalStoreMappingUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings.PublisherCommonUtils;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIExternalStoreListDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIMonetizationInfoDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIRevenueDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIRevisionListDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.FileInfoDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.LifecycleHistoryDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.LifecycleStateDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.MockResponsePayloadListDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.ResourcePathListDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.ThrottlingPolicyDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.WorkflowResponseDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Util class for ApisApiService related operations
 */
public class ApisApiCommonImpl {

    public static final String MESSAGE = "message";
    public static final String ERROR_WHILE_UPDATING_API = "Error while updating API : ";

    private ApisApiCommonImpl() {

    }

    private static final Log log = LogFactory.getLog(ApisApiCommonImpl.class);
    private static final String HTTP_STATUS_LOG = "HTTP status ";
    private static final String AUDIT_ERROR = "Error while parsing the audit response";

    public static Object getAllAPIs(Integer limit, Integer offset, String sortBy, String sortOrder, String query,
                                    String organization) throws APIManagementException {

        List<API> allMatchedApis = new ArrayList<>();
        Object apiListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        sortBy = sortBy != null ? sortBy : RestApiConstants.DEFAULT_SORT_CRITERION;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DESCENDING_SORT_ORDER;

        //revert content search back to normal search by name to avoid doc result complexity and to comply with
        // REST api practices
        if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
            query = query
                    .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":",
                            APIConstants.NAME_TYPE_PREFIX + ":");
        }

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        Map<String, Object> result;

        result = apiProvider.searchPaginatedAPIs(query, organization, offset, limit, sortBy, sortOrder);

        Set<API> apis = (Set<API>) result.get("apis");
        allMatchedApis.addAll(apis);

        apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis);

        //Add pagination section in the response
        Object totalLength = result.get("length");
        int length = 0;
        if (totalLength != null) {
            length = (Integer) totalLength;
        }

        APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, length);
        return apiListDTO;
    }

    public static APIDTO getAPI(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        return getAPIByID(apiId, apiProvider, organization);
    }

    /**
     * @param name Name of URI Template
     * @param verb HTTP verb
     * @return URITemplate
     */
    public static URITemplate createUriTemplate(String name, String verb) {

        URITemplate uriTemplate = new URITemplate();
        uriTemplate.setUriTemplate(name);
        uriTemplate.setHTTPVerb(verb.toUpperCase());
        uriTemplate.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
        uriTemplate.setThrottlingTier(APIConstants.UNLIMITED_TIER);

        return uriTemplate;
    }

    public static APIDTO updateAPI(String apiId, APIDTO body, String[] tokenScopes, String organization)
            throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        boolean isWSAPI = APIDTO.TypeEnum.WS.equals(body.getType());

        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);

        // validate web socket api endpoint configurations
        if (isWSAPI && !PublisherCommonUtils.isValidWSAPI(body)) {
            throw new APIManagementException("Endpoint URLs should be valid web socket URLs",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }

        // validate sandbox and production endpoints
        if (!PublisherCommonUtils.validateEndpoints(body)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }

        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
        originalAPI.setOrganization(organization);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(originalAPI.getStatus(), tokenScopes);
        API updatedApi;
        try {
            updatedApi = PublisherCommonUtils.updateApi(originalAPI, body, apiProvider, tokenScopes);
        } catch (FaultGatewaysException e) {
            String errorMessage = ERROR_WHILE_UPDATING_API + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        return APIMappingUtil.fromAPItoDTO(updatedApi);
    }

    private static void validateAPIOperationsPerLC(String status, String[] tokenScopes) throws APIManagementException {

        boolean updatePermittedForPublishedDeprecated = false;

        for (String scope : tokenScopes) {
            if (RestApiConstants.PUBLISHER_SCOPE.equals(scope)
                    || RestApiConstants.API_IMPORT_EXPORT_SCOPE.equals(scope)
                    || RestApiConstants.API_MANAGE_SCOPE.equals(scope)
                    || RestApiConstants.ADMIN_SCOPE.equals(scope)) {
                updatePermittedForPublishedDeprecated = true;
                break;
            }
        }
        if (!updatePermittedForPublishedDeprecated && (
                APIConstants.PUBLISHED.equals(status)
                        || APIConstants.DEPRECATED.equals(status))) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.API_UPDATE_FORBIDDEN_PER_LC, status));
        }
    }

    public static APIExternalStoreListDTO getAllPublishedExternalStoresByAPI(String apiId) throws
            APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Set<APIStore> publishedStores = apiProvider.getPublishedExternalAPIStores(apiId);
        return ExternalStoreMappingUtil.fromAPIExternalStoreCollectionToDTO(publishedStores);
    }

    public static LifecycleHistoryDTO getAPILifecycleHistory(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api;
        APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(apiId);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            api = apiProvider.getAPIbyUUID(apiRevision.getApiUUID(), organization);
        } else {
            api = apiProvider.getAPIbyUUID(apiId, organization);
        }
        return PublisherCommonUtils.getLifecycleHistoryDTO(api.getUuid(), apiProvider);
    }

    public static LifecycleStateDTO getAPILifecycleState(String apiId, String organization)
            throws APIManagementException {

        return getLifecycleState(apiId, organization);
    }

    /**
     * Retrieves API Lifecycle state information
     *
     * @param apiId        API Id
     * @param organization organization
     * @return API Lifecycle state information
     */
    private static LifecycleStateDTO getLifecycleState(String apiId, String organization)
            throws APIManagementException {

        APIIdentifier apiIdentifier;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (apiProvider.checkAPIUUIDIsARevisionUUID(apiId) != null) {
            apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId, organization).getId();
        } else {
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        }
        return PublisherCommonUtils.getLifecycleStateInformation(apiIdentifier, organization);
    }

    public static void deleteAPILifecycleStatePendingTasks(String apiId) throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        apiProvider.deleteWorkflowTask(apiIdentifierFromTable);
    }

    public static FileInfoDTO updateAPIThumbnail(String apiId, InputStream fileInputStream, String organization,
                                                 String fileName, String fileDetailContentType)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String extension = FilenameUtils.getExtension(fileName);
        if (!RestApiConstants.ALLOWED_THUMBNAIL_EXTENSIONS.contains(extension.toLowerCase())) {
            String errorMessage = "Unsupported Thumbnail File Extension. Supported extensions are .jpg, .png, "
                    + ".jpeg, .svg, and .gif";
            throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        String fileContentType = URLConnection.guessContentTypeFromName(fileName);
        if (StringUtils.isBlank(fileContentType)) {
            fileContentType = fileDetailContentType;
        }
        PublisherCommonUtils.updateThumbnail(fileInputStream, fileContentType, apiProvider, apiId, organization);
        FileInfoDTO infoDTO = new FileInfoDTO();
        infoDTO.setMediaType(fileContentType);
        return infoDTO;
    }

    public static String getAPISwagger(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return RestApiCommonUtil.retrieveSwaggerDefinition(apiId, api, apiProvider);
    }

    public static ResourceFile getAPIThumbnail(String apiId, APIProvider apiProvider, String organization)
            throws APIManagementException {

        //this will fail if user does not have access to the API or the API does not exist
        RestApiCommonUtil.validateAPIExistence(apiId);
        return apiProvider.getIcon(apiId, organization);
    }

    public static ResourcePathListDTO getAPIResourcePaths(String apiId, Integer limit, Integer offset)
            throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        List<ResourcePath> apiResourcePaths = apiProvider.getResourcePathsOfAPI(apiIdentifier);

        ResourcePathListDTO dto = APIMappingUtil.fromResourcePathListToDTO(apiResourcePaths, limit, offset);
        APIMappingUtil.setPaginationParamsForAPIResourcePathList(dto, offset, limit, apiResourcePaths.size());
        return dto;
    }

    public static ResourceFile getWSDLOfAPI(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist
        return apiProvider.getWSDL(apiId, organization);
    }

    public static WorkflowResponseDTO changeAPILifecycle(String action, String apiId, String lifecycleChecklist,
                                                         String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiWrapper = new ApiTypeWrapper(apiProvider.getAPIbyUUID(apiId, organization));
        APIStateChangeResponse stateChangeResponse = PublisherCommonUtils.changeApiOrApiProductLifecycle(action,
                apiWrapper, lifecycleChecklist, organization);

        //returns the current lifecycle state
        LifecycleStateDTO stateDTO = getLifecycleState(apiId, organization);

        return APIMappingUtil.toWorkflowResponseDTO(stateDTO, stateChangeResponse);
    }

    public static String generateMockScripts(String apiId, String organization) throws APIManagementException {

        APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        if (apiIdentifierFromTable == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                    apiId));
        }
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);

        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        apiDefinition = String.valueOf(OASParserUtil.generateExamples(apiDefinition).get(APIConstants.SWAGGER));
        apiProvider.saveSwaggerDefinition(originalAPI, apiDefinition, organization);
        return apiDefinition;
    }

    public static List<Tier> getAPISubscriptionPolicies(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIDTO apiInfo = getAPIByID(apiId, apiProvider, organization);
        List<Tier> availableThrottlingPolicyList = ThrottlingPoliciesApiCommonImpl
                .getThrottlingPolicyList(ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString(),
                        true);

        List<String> apiPolicies = apiInfo.getPolicies();
        return filterAPIThrottlingPolicies(apiPolicies, availableThrottlingPolicyList);
    }

    public static APIRevisionListDTO getAPIRevisions(String apiId, String query) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIRevisionListDTO apiRevisionListDTO;
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
        List<APIRevision> apiRevisionsList = filterAPIRevisionsByDeploymentStatus(query, apiRevisions);
        apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisionsList);
        return apiRevisionListDTO;
    }

    public static List<APIRevisionDeploymentDTO> getAPIRevisionDeployments(String apiId) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        List<APIRevisionDeployment> apiRevisionDeploymentsList = apiProvider.getAPIRevisionsDeploymentList(apiId);

        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsList) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        return apiRevisionDeploymentDTOS;
    }

    public static String getAsyncAPIDefinition(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return RestApiCommonUtil.retrieveAsyncAPIDefinition(api, apiProvider);
    }

    public static String getEnvironmentSpecificAPIProperties(String apiId, String envId)
            throws APIManagementException {

        // validate api UUID
        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // get properties
        EnvironmentPropertiesDTO properties = apiProvider.getEnvironmentSpecificAPIProperties(apiId, envId);
        // convert to string to remove null values
        return new Gson().toJson(properties);
    }

    /**
     * @param api           API
     * @param soapOperation SOAP Operation
     * @return SOAP API Definition
     * @throws APIManagementException if an error occurred while parsing string to JSON Object
     */
    public static String generateSOAPAPIDefinition(API api, String soapOperation) throws APIManagementException {

        APIDefinition oasParser = new OAS2Parser();
        SwaggerData swaggerData = new SwaggerData(api);
        String apiDefinition = oasParser.generateAPIDefinition(swaggerData);
        JSONParser jsonParser = new JSONParser();
        JSONObject apiJson;
        JSONObject paths;
        try {
            apiJson = (JSONObject) jsonParser.parse(apiDefinition);
            paths = (JSONObject) jsonParser.parse(soapOperation);
            apiJson.replace("paths", paths);
            return apiJson.toJSONString();
        } catch (ParseException e) {
            String errorMessage = "Error while parsing the api definition.";
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    /**
     * @param fileInputStream          File input stream for the WSDL file
     * @param url                      URL
     * @param wsdlArchiveExtractedPath Path to WSDL extracted directory
     * @param filename                 File Name
     * @return Swagger string
     * @throws APIManagementException If the WSDL file not supported
     * @throws IOException            If error occurred in converting InputStream to a byte array
     */
    public static String getSwaggerString(InputStream fileInputStream, String url, String wsdlArchiveExtractedPath,
                                          String filename) throws APIManagementException, IOException {

        String swaggerStr = "";
        if (StringUtils.isNotBlank(url)) {
            swaggerStr = SOAPOperationBindingUtils.getSoapOperationMappingForUrl(url);
        } else if (fileInputStream != null) {
            if (filename.endsWith(".zip")) {
                swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(wsdlArchiveExtractedPath);
            } else if (filename.endsWith(".wsdl")) {
                byte[] wsdlContent = APIUtil.toByteArray(fileInputStream);
                swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(wsdlContent);
            } else {
                throw new APIManagementException(ExceptionCodes.UNSUPPORTED_WSDL_FILE_EXTENSION);
            }
        }
        return swaggerStr;
    }

    /**
     * @param api API
     * @return API definition
     * @throws APIManagementException If any error occurred in generating API definition from swagger data
     */
    private static String getApiDefinition(API api) throws APIManagementException {

        APIDefinition parser = new OAS3Parser();
        SwaggerData swaggerData = new SwaggerData(api);
        return parser.generateAPIDefinition(swaggerData);
    }

    /**
     * @param apiPolicies                   Policy names applied to the API
     * @param availableThrottlingPolicyList All available policies
     * @return Filtered API policy list which are applied to the API
     */
    public static List<Tier> filterAPIThrottlingPolicies(List<String> apiPolicies,
                                                         List<Tier> availableThrottlingPolicyList) {

        List<Tier> apiThrottlingPolicies = new ArrayList<>();
        if (apiPolicies != null && !apiPolicies.isEmpty()) {
            for (Tier tier : availableThrottlingPolicyList) {
                if (apiPolicies.contains(tier.getName())) {
                    apiThrottlingPolicies.add(tier);
                }
            }
        }
        return apiThrottlingPolicies;
    }

    /**
     * @param deploymentStatus Deployment status [deployed:true / deployed:false]
     * @param apiRevisions     API revisions list
     * @return Filtered API revisions according to the deploymentStatus
     */
    public static List<APIRevision> filterAPIRevisionsByDeploymentStatus(String deploymentStatus,
                                                                         List<APIRevision> apiRevisions) {

        if ("deployed:true".equalsIgnoreCase(deploymentStatus)) {
            List<APIRevision> apiDeployedRevisions = new ArrayList<>();
            for (APIRevision apiRevision : apiRevisions) {
                if (!apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiDeployedRevisions.add(apiRevision);
                }
            }
            return apiDeployedRevisions;
        } else if ("deployed:false".equalsIgnoreCase(deploymentStatus)) {
            List<APIRevision> apiNotDeployedRevisions = new ArrayList<>();
            for (APIRevision apiRevision : apiRevisions) {
                if (apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiNotDeployedRevisions.add(apiRevision);
                }
            }
            return apiNotDeployedRevisions;
        }
        return apiRevisions;
    }

    /**
     * @param revisionId         Revision ID
     * @param environments       Environments of the organization
     * @param environment        Selected environment
     * @param displayOnDevportal Enable display on Developer Portal
     * @param vhost              Virtual Host of the revision deployment
     * @param mandatoryVHOST     Is vhost mandatory in this validation
     * @return Created {@link APIRevisionDeployment} after validations
     * @throws APIManagementException if any validation fails
     */
    public static APIRevisionDeployment mapAPIRevisionDeploymentWithValidation(String revisionId,
                                                                               Map<String, Environment> environments,
                                                                               String environment,
                                                                               Boolean displayOnDevportal,
                                                                               String vhost, boolean mandatoryVHOST)
            throws APIManagementException {

        if (environments.get(environment) == null) {
            final String errorMessage = "Gateway environment not found: " + environment;
            throw new APIManagementException(errorMessage, ExceptionCodes.from(
                    ExceptionCodes.INVALID_GATEWAY_ENVIRONMENT, String.format("name '%s'", environment)));

        }
        if (mandatoryVHOST && StringUtils.isEmpty(vhost)) {
            // vhost is only required when deploying a revision, not required when un-deploying a revision
            // since the same scheme 'APIRevisionDeployment' is used for deploy and undeploy, handle it here.
            throw new APIManagementException("Required field 'vhost' not found in deployment",
                    ExceptionCodes.GATEWAY_ENVIRONMENT_VHOST_NOT_PROVIDED);
        }
        return mapApiRevisionDeployment(revisionId, vhost, displayOnDevportal, environment);
    }

    /**
     * @param revisionId         Revision ID
     * @param vhost              Virtual Host
     * @param displayOnDevportal Enable displaying on Developer Portal
     * @param deployment         Deployment
     * @return Mapped {@link APIRevisionDeployment}
     */
    public static APIRevisionDeployment mapApiRevisionDeployment(String revisionId, String vhost,
                                                                 Boolean displayOnDevportal, String deployment) {

        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
        apiRevisionDeployment.setRevisionUUID(revisionId);
        apiRevisionDeployment.setDeployment(deployment);
        apiRevisionDeployment.setVhost(vhost);
        apiRevisionDeployment.setDisplayOnDevportal(displayOnDevportal);
        return apiRevisionDeployment;
    }

    /**
     * @param deploymentId Deployment ID
     * @return Deployment name decoded from the deploymentId
     * @throws APIMgtResourceNotFoundException If invalid or null deploymentId
     */
    public static String getDecodedDeploymentName(String deploymentId) throws APIMgtResourceNotFoundException {

        String decodedDeploymentName;
        if (deploymentId != null) {
            try {
                decodedDeploymentName = new String(Base64.getUrlDecoder().decode(deploymentId),
                        StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new APIMgtResourceNotFoundException("deployment with " + deploymentId +
                        " not found", ExceptionCodes.from(ExceptionCodes.EXISTING_DEPLOYMENT_NOT_FOUND,
                        deploymentId));
            }
        } else {
            throw new APIMgtResourceNotFoundException("deployment id not found",
                    ExceptionCodes.from(ExceptionCodes.DEPLOYMENT_ID_NOT_FOUND));
        }
        return decodedDeploymentName;
    }

    /**
     * @param fileInputStream API spec file input stream
     * @param isServiceAPI    Is service API
     * @param fileName        File name
     * @return Schema
     * @throws APIManagementException if error while reading the spec file contents
     */
    public static String getSchemaToBeValidated(InputStream fileInputStream, Boolean isServiceAPI, String fileName)
            throws APIManagementException {

        String schemaToBeValidated = null;
        if (Boolean.TRUE.equals(isServiceAPI) || fileName.endsWith(APIConstants.YAML_FILE_EXTENSION) || fileName
                .endsWith(APIConstants.YML_FILE_EXTENSION)) {
            //convert .yml or .yaml to JSON for validation
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            try {
                Object obj = yamlReader.readValue(fileInputStream, Object.class);
                ObjectMapper jsonWriter = new ObjectMapper();
                schemaToBeValidated = jsonWriter.writeValueAsString(obj);
            } catch (IOException e) {
                throw new APIManagementException("Error while reading file content", e,
                        ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
            }
        } else if (fileName.endsWith(APIConstants.JSON_FILE_EXTENSION)) {
            //continue with .json
            JSONTokener jsonDataFile = new JSONTokener(fileInputStream);
            schemaToBeValidated = new org.json.JSONObject(jsonDataFile).toString();
        }
        return schemaToBeValidated;
    }

    /**
     * @param environmentPropertiesMap Environment Properties Map
     * @return {@link EnvironmentPropertiesDTO} mapped from the properties in the environmentPropertiesMap
     * @throws APIManagementException If error converting environmentPropertiesMap to {@link EnvironmentPropertiesDTO}
     */
    public static EnvironmentPropertiesDTO generateEnvironmentPropertiesDTO(
            Map<String, String> environmentPropertiesMap)
            throws APIManagementException {

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.convertValue(environmentPropertiesMap, new TypeReference<EnvironmentPropertiesDTO>() {
            });
        } catch (IllegalArgumentException e) {
            String errorMessage = "Possible keys are productionEndpoint,sandboxEndpoint";
            throw new APIManagementException(e.getMessage(),
                    ExceptionCodes.from(ExceptionCodes.INVALID_ENV_API_PROP_CONFIG, errorMessage));
        }
    }

    /**
     * @param service service entry
     * @return service info JSON Object
     */
    public static JSONObject getServiceInfo(ServiceEntry service) {

        JSONObject serviceInfo = new JSONObject();
        serviceInfo.put("name", service.getName());
        serviceInfo.put("version", service.getVersion());
        serviceInfo.put("key", service.getServiceKey());
        serviceInfo.put("md5", service.getMd5());
        return serviceInfo;
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return A list of API resource mediation policies with mock scripts
     * @throws APIManagementException when an internal errors occurs
     */
    public static MockResponsePayloadListDTO getGeneratedMockScriptsOfAPI(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        Map<String, Object> examples = OASParserUtil.generateExamples(apiDefinition);

        List<APIResourceMediationPolicy> policiesList =
                (List<APIResourceMediationPolicy>) examples.get(APIConstants.MOCK_GEN_POLICY_LIST);
        return APIMappingUtil.fromMockPayloadsToListDTO(policiesList);
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return Monetized policies to plan mapping
     * @throws APIManagementException when an internal error occurs
     */
    public static APIMonetizationInfoDTO getAPIMonetization(String apiId, String organization)
            throws APIManagementException {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when retrieving monetized plans.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        String uuid = RestApiCommonUtil.getAPIUUID(apiId);

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        Map<String, String> monetizedPoliciesToPlanMapping;
        try {
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            monetizedPoliciesToPlanMapping = monetizationImplementation.getMonetizedPoliciesToPlanMapping(api);
        } catch (MonetizationException e) {
            throw new APIManagementException("Error occurred while getting the Monetization mappings for API "
                    + api.getId().getApiName(), e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                            "Error occurred while getting the Monetization mappings for API"));
        }
        return APIMappingUtil.getMonetizedTiersDTO(uuid, organization, monetizedPoliciesToPlanMapping);
    }

    public static APIMonetizationInfoDTO addAPIMonetization(String apiId, APIMonetizationInfoDTO body,
                                                            String organization)
            throws APIManagementException {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when configuring monetization.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        RestApiCommonUtil.validateAPIExistence(apiId);

        boolean monetizationEnabled = body.isEnabled();
        Map<String, String> monetizationProperties = body.getProperties();
        //set the monetization status
        addAPIMonetization(apiId, organization, monetizationEnabled, monetizationProperties);
        return APIMappingUtil.getMonetizationInfoDTO(apiId, organization);
    }

    /**
     * @param apiId                  API UUID
     * @param organization           Tenant organization
     * @param monetizationEnabled    Whether to enable or disable monetization
     * @param monetizationProperties Monetization properties map
     * @return true if monetization state change is successful
     * @throws APIManagementException when a monetization related error occurs
     */
    private static boolean addAPIMonetization(String apiId, String organization,
                                              boolean monetizationEnabled, Map<String, String> monetizationProperties)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        if (!APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus())) {
            String errorMessage = "API " + api.getId().getApiName() +
                    " should be in published state to configure monetization.";
            throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_API_STATE_MONETIZATION);
        }
        //set the monetization status
        api.setMonetizationEnabled(monetizationEnabled);
        //clear the existing properties related to monetization
        api.getMonetizationProperties().clear();
        for (Map.Entry<String, String> currentEntry : monetizationProperties.entrySet()) {
            api.addMonetizationProperty(currentEntry.getKey(), currentEntry.getValue());
        }

        Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
        HashMap<String, String> monetizationDataMap = new Gson().fromJson(api.getMonetizationProperties().toString(),
                HashMap.class);
        if (MapUtils.isEmpty(monetizationDataMap)) {
            String errorMessage = "Monetization is not configured. Monetization data is empty for "
                    + api.getId().getApiName();
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        boolean isMonetizationStateChangeSuccessful = false;
        try {
            if (monetizationEnabled) {
                isMonetizationStateChangeSuccessful = monetizationImplementation.enableMonetization
                        (organization, api, monetizationDataMap);
            } else {
                isMonetizationStateChangeSuccessful = monetizationImplementation.disableMonetization
                        (organization, api, monetizationDataMap);
            }
        } catch (MonetizationException e) {
            String errorMessage = "Error while changing monetization status for API ID : " + apiId;
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        if (isMonetizationStateChangeSuccessful) {
            apiProvider.configureMonetizationInAPIArtifact(api);
            return true;
        } else {
            throw new APIManagementException("Unable to change monetization status for API : " + apiId,
                    ExceptionCodes.from(ExceptionCodes.MONETIZATION_STATE_CHANGE_FAILED,
                            String.valueOf(monetizationEnabled)));
        }
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return A map of revenue details
     * @throws APIManagementException when retrieving monetization details fail
     */
    public static APIRevenueDTO getAPIRevenue(String apiId, String organization) throws APIManagementException {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when getting revenue details.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        if (!APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus())) {
            String errorMessage = "API " + api.getId().getApiName() +
                    " should be in published state to configure monetization.";
            throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_API_STATE_MONETIZATION);
        }

        try {
            Map<String, String> revenueUsageData = monetizationImplementation.getTotalRevenue(api, apiProvider);
            APIRevenueDTO apiRevenueDTO = new APIRevenueDTO();
            apiRevenueDTO.setProperties(revenueUsageData);
            return apiRevenueDTO;
        } catch (MonetizationException e) {
            String errorMessage = "Error while getting revenue information for API ID : " + apiId;
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    public static void validateDocument(String apiId, String name, String organization) throws APIManagementException {

        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(apiId)) {
            throw new APIManagementException("API Id and/ or document name should not be empty",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        boolean documentationExist = apiProvider.isDocumentationExist(apiId, name, organization);
        if (!documentationExist) {
            throw new APIManagementException(ExceptionCodes.RESOURCE_NOT_FOUND);
        }
    }

    private static void handleInvalidParams(InputStream fileInputStream, String fileName, String url,
                                            String apiDefinition, Boolean isServiceAPI) throws APIManagementException {

        String msg = "";
        boolean isFileSpecified = (fileInputStream != null && fileName != null)
                || (fileInputStream != null && isServiceAPI);
        if (url == null && !isFileSpecified && apiDefinition == null) {
            msg = "One out of 'file' or 'url' or 'inline definition' should be specified";
        }

        boolean isMultipleSpecificationGiven = (isFileSpecified && url != null) || (isFileSpecified &&
                apiDefinition != null) || (apiDefinition != null && url != null);
        if (isMultipleSpecificationGiven) {
            msg = "Only one of 'file', 'url', and 'inline definition' should be specified";
        }

        if (StringUtils.isNotBlank(msg)) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, msg));
        }
    }

    private static APIDTO getAPIByID(String apiId, APIProvider apiProvider, String organization)
            throws APIManagementException {

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return APIMappingUtil.fromAPItoDTO(api, apiProvider);
    }

    private static APIDTO createAPIDTO(API existingAPI, String newVersion) {

        APIDTO apidto = new APIDTO();
        apidto.setName(existingAPI.getId().getApiName());
        apidto.setContext(existingAPI.getContextTemplate());
        apidto.setVersion(newVersion);
        return apidto;
    }

    private static String getSOAPOperation() {

        return "{\"/*\":{\"post\":{\"parameters\":[{\"schema\":{\"type\":\"string\"},\"description\":\"SOAP request.\","
                + "\"name\":\"SOAP Request\",\"required\":true,\"in\":\"body\"},"
                + "{\"description\":\"SOAPAction header for soap 1.1\",\"name\":\"SOAPAction\",\"type\":\"string\","
                + "\"required\":false,\"in\":\"header\"}],\"responses\":{\"200\":{\"description\":\"OK\"}}," +
                "\"security\":[{\"default\":[]}],\"consumes\":[\"text/xml\",\"application/soap+xml\"]}}}";
    }

    private static String getOperationPolicyRetrieveErrorMessage(String apiId, String operationPolicyId) {

        return "Couldn't retrieve an existing operation policy with ID: "
                + operationPolicyId + " for API " + apiId;
    }
}
