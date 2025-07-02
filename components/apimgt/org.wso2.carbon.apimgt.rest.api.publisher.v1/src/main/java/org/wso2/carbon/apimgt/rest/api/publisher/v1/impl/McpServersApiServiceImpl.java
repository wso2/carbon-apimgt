/*
 * Copyright (c) 2025, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIComplianceException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIInfo;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.BackendEndpoint;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.OrganizationInfo;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.restapi.CommonUtils;
import org.wso2.carbon.apimgt.impl.restapi.publisher.ApisApiServiceImplUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OrganizationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.impl.APIConstants.GOVERNANCE_COMPLIANCE_ERROR_MESSAGE;
import static org.wso2.carbon.apimgt.impl.APIConstants.GOVERNANCE_COMPLIANCE_KEY;

public class McpServersApiServiceImpl implements McpServersApiService {

    private static final Log log = LogFactory.getLog(McpServersApiServiceImpl.class);

    /**
     * Retrieves a paginated list of all MCP servers matching the given search criteria.
     * Handles query adjustments, pagination, and optional gzip compression of the response payload.
     *
     * @param limit          the maximum number of MCP servers to return
     * @param offset         the starting index for pagination
     * @param xWSO2Tenant    the tenant identifier from request header
     * @param query          the search query string to filter MCP servers
     * @param ifNoneMatch    the ETag header value for conditional requests
     * @param accept         the requested content type (e.g. gzip or JSON)
     * @param messageContext the message context of the request
     * @return a {@link Response} containing the list of MCP servers or an error response
     * @throws APIManagementException if an error occurs while retrieving MCP server data
     */
    @Override
    public Response getAllMCPServers(Integer limit, Integer offset, String xWSO2Tenant, String query,
                                     String ifNoneMatch, String accept, MessageContext messageContext)
            throws APIManagementException {

        List<API> allMatchedApis = new ArrayList<>();
        Object apiListDTO;

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "type:MCP" : "type:MCP " + query;
        try {
            if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
                query = query.replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":",
                        APIConstants.NAME_TYPE_PREFIX + ":");
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Map<String, Object> result;
            result = apiProvider.searchPaginatedAPIs(query, organization, offset, limit);
            Set<API> apis = (Set<API>) result.get("apis");
            allMatchedApis.addAll(apis);
            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis);
            Object totalLength = result.get("length");
            Integer length = 0;
            if (totalLength != null) {
                length = (Integer) totalLength;
            }
            APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, length);
            if (APIConstants.APPLICATION_GZIP.equals(accept)) {
                try {
                    File zippedResponse = GZIPUtils.constructZippedResponse(apiListDTO);
                    return Response.ok().entity(zippedResponse)
                            .header("Content-Disposition", "attachment").
                            header("Content-Encoding", "gzip").build();
                } catch (APIManagementException e) {
                    RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
                }
            } else {
                return Response.ok().entity(apiListDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving MCP servers with query : " + query;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves the MCP server API details for a given API UUID.
     * Validates that the API is of type MCP and applies organization-specific visibility and policy filters.
     *
     * @param apiId          the UUID of the API
     * @param xWSO2Tenant    the tenant identifier from request header
     * @param ifNoneMatch    the ETag header value for conditional requests
     * @param messageContext the message context of the request
     * @return a {@link Response} containing the MCP server API details
     * @throws APIManagementException if an error occurs during API retrieval or authorization
     */
    @Override
    public Response getMCPServer(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        OrganizationInfo organizationInfo = RestApiUtil.getOrganizationInfo(messageContext);
        APIDTO apiToReturn = null;
        try {
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            if (APIConstants.API_TYPE_MCP.equals(api.getType())) {
                api.setOrganization(organization);
                apiToReturn = APIMappingUtil.fromAPItoDTO(api, apiProvider);
            } else {
                String errorMessage = "Error while retrieving MCP server : " + apiId + ". API is not of type MCP";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to access the MCP server",
                        e, log);
            } else {
                String errorMessage = "Error while retrieving MCP server : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        if (apiToReturn.getVisibleOrganizations() != null && organizationInfo != null
                && organizationInfo.getOrganizationId() != null) {
            List<String> orglist = apiToReturn.getVisibleOrganizations();
            ArrayList<String> newOrgList = new ArrayList<String>(orglist);
            newOrgList.remove(organizationInfo.getOrganizationId());
            newOrgList.remove(organization);
            if (newOrgList.isEmpty()) {
                newOrgList.add(APIConstants.VISIBLE_ORG_NONE);
            }
            apiToReturn.setVisibleOrganizations(newOrgList);
            List<OrganizationPoliciesDTO> organizationPolicies = apiToReturn.getOrganizationPolicies();
            if (organizationPolicies != null) {
                organizationPolicies.removeIf(tier
                        -> tier.getOrganizationID().equals(organizationInfo.getOrganizationId()));
                apiToReturn.setOrganizationPolicies(organizationPolicies);
            }
        } else {
            apiToReturn.setVisibleOrganizations(Collections.singletonList(APIConstants.VISIBLE_ORG_NONE));
        }
        return Response.ok().entity(apiToReturn).build();
    }

    /**
     * @param apiId
     * @param endpointId
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerEndpoint(String apiId, String endpointId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            CommonUtils.validateAPIExistence(apiId);
            BackendEndpoint backendEndpoint = apiProvider.getMCPServerEndpoint(apiId, endpointId);
            BackendEndpointDTO backendEndpointDTO = APIMappingUtil.fromBackendEndpointToDTO(backendEndpoint, false);
//            removeAPIEndpointSecrets(apiEndpointDTO);
            return Response.ok().entity(backendEndpointDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving resource paths of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving endpoint of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * @param apiId
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerEndpoints(String apiId, MessageContext messageContext) throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            CommonUtils.validateAPIExistence(apiId);
            List<BackendEndpoint> backendEndpointList = apiProvider.getMCPServerEndpoints(apiId);
            List<BackendEndpointDTO> backendEndpointDTOList = backendEndpointList.stream()
                    .map(endpoint -> APIMappingUtil.fromBackendEndpointToDTO(endpoint, false))
                    .collect(Collectors.toList());
//            removeAPIEndpointSecrets(apiEndpointDTO);
            return Response.ok().entity(backendEndpointDTOList).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving resource paths of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving endpoint of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    private void removeAPIEndpointSecrets(APIEndpointDTO apiEndpointDTO) throws APIManagementException {

        Map endpointConfig = (Map) apiEndpointDTO.getEndpointConfig();
        if (endpointConfig.containsKey(APIConstants.ENDPOINT_SECURITY)) {
            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            Map endpointSecurity = (Map) endpointConfig.get(APIConstants.ENDPOINT_SECURITY);

            try {
                //decrypt if oath 2.0 endpoint security
                if (endpointSecurity.containsKey(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET)) {
                    endpointSecurity.put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET, cryptoUtil.
                            base64DecodeAndDecrypt((String) endpointSecurity.
                                    get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET)));
                }

                //remove password from endpoint security
                if (endpointSecurity.containsKey(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PASSWORD)) {
                    endpointSecurity.put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PASSWORD, "");
                }
            } catch (CryptoException e) {
                String errorMessage =
                        "Error while decrypting the secret key of API Endpoint ID : " + apiEndpointDTO.getId();
                throw new APIManagementException(errorMessage, e);
            }
            endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
            apiEndpointDTO.setEndpointConfig(endpointConfig);
        }

        //remove AMZN secret key
        if (endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY)) {
            endpointConfig.put(APIConstants.AMZN_SECRET_KEY, APIConstants.AWS_SECRET_KEY);
        }
    }

    /**
     * @param apiId
     * @param revisionId
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerRevision(String apiId, String revisionId, MessageContext messageContext) throws APIManagementException {

        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    /**
     * @param apiId
     * @param query
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerRevisions(String apiId, String query, MessageContext messageContext) throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIRevisionListDTO apiRevisionListDTO;
            List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
            List<APIRevision> apiRevisionsList = ApisApiServiceImplUtils.filterAPIRevisionsByDeploymentStatus(query, apiRevisions);
            apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisionsList);
            return Response.ok().entity(apiRevisionListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding retrieving API Revision for api id : " + apiId + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response importMCPServerDefinition(InputStream fileInputStream, Attachment fileDetail, String url,
                                              String additionalProperties, MessageContext messageContext)
            throws APIManagementException {

        if (StringUtils.isBlank(additionalProperties)) {
            throw new APIManagementException("'additionalProperties' is required and should not be null",
                    ExceptionCodes.ADDITIONAL_PROPERTIES_CANNOT_BE_NULL);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        APIDTO apiDTOFromProperties;
        try {
            apiDTOFromProperties = objectMapper.readValue(additionalProperties, APIDTO.class);
            APIUtil.validateCharacterLengthOfAPIParams(apiDTOFromProperties.getName(),
                    apiDTOFromProperties.getVersion(), apiDTOFromProperties.getContext(),
                    RestApiCommonUtil.getLoggedInUsername());
            try {
                APIUtil.validateAPIContext(apiDTOFromProperties.getContext(), apiDTOFromProperties.getName());
            } catch (APIManagementException e) {
                throw new APIManagementException(e.getMessage(),
                        ExceptionCodes.from(ExceptionCodes.API_CONTEXT_MALFORMED_EXCEPTION, e.getMessage()));
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while parsing 'additionalProperties'", e,
                    ExceptionCodes.ADDITIONAL_PROPERTIES_PARSE_ERROR);
        }
        apiDTOFromProperties.setType(APIDTO.TypeEnum.MCP);
        if (!PublisherCommonUtils.validateEndpoints(apiDTOFromProperties)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }
        try {
            LinkedHashMap endpointConfig = (LinkedHashMap) apiDTOFromProperties.getEndpointConfig();
            PublisherCommonUtils
                    .encryptEndpointSecurityOAuthCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                            apiDTOFromProperties);
            PublisherCommonUtils
                    .encryptEndpointSecurityApiKeyCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                            StringUtils.EMPTY, StringUtils.EMPTY, apiDTOFromProperties);

            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIDTO createdApiDTO = RestApiPublisherUtils.importOpenAPIDefinition(fileInputStream, url, null,
                    apiDTOFromProperties, fileDetail, null, organization);
            if (createdApiDTO != null) {
                URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
                return Response.created(createdApiUri).entity(createdApiDTO).build();
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + apiDTOFromProperties.getProvider() + "-" +
                    apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage =
                    "Error while encrypting the secret key of API : " + apiDTOFromProperties.getProvider() + "-"
                            + apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion();
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.ENDPOINT_SECURITY_CRYPTO_EXCEPTION, errorMessage));
        } catch (ParseException e) {
            String errorMessage = "Error while parsing the endpoint configuration of API : "
                    + apiDTOFromProperties.getProvider() + "-" + apiDTOFromProperties.getName() + "-"
                    + apiDTOFromProperties.getVersion();
            throw new APIManagementException(errorMessage, e);
        }
        return null;
    }

    /**
     * @param apiId
     * @param revisionId
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response restoreMCPServerRevision(String apiId, String revisionId, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        apiProvider.restoreAPIRevision(apiId, revisionId, organization);

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        APIDTO apiToReturn = APIMappingUtil.fromAPItoDTO(api, apiProvider);

        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiToReturn).build();
    }

    /**
     * @param apiId
     * @param revisionId
     * @param revisionNumber
     * @param allEnvironments
     * @param apIRevisionDeploymentDTO
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response undeployMCPServerRevision(String apiId, String revisionId, String revisionNumber,
                                              Boolean allEnvironments,
                                              List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                              MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //validate if api exists
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        if (revisionId == null && revisionNumber != null) {
            revisionId = apiProvider.getAPIRevisionUUID(revisionNumber, apiId);
            if (revisionId == null) {
                throw new APIManagementException(
                        "No revision found for revision number " + revisionNumber + " of API with UUID " + apiId,
                        ExceptionCodes.from(ExceptionCodes.REVISION_NOT_FOUND_FOR_REVISION_NUMBER, revisionNumber));
            }
        }

        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        if (allEnvironments) {
            apiRevisionDeployments = apiProvider.getAPIRevisionDeploymentList(revisionId);
        } else {
            for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : apIRevisionDeploymentDTOList) {
                Boolean displayOnDevportal = apiRevisionDeploymentDTO.isDisplayOnDevportal();
                String vhost = apiRevisionDeploymentDTO.getVhost();
                String environment = apiRevisionDeploymentDTO.getName();
                APIRevisionDeployment apiRevisionDeployment = ApisApiServiceImplUtils.mapAPIRevisionDeploymentWithValidation(revisionId,
                        environments, environment, displayOnDevportal, vhost, false);
                apiRevisionDeployments.add(apiRevisionDeployment);
            }
        }
        apiProvider.undeployAPIRevisionDeployment(apiId, revisionId, apiRevisionDeployments, organization);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = apiProvider.getAPIRevisionDeploymentList(revisionId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * @param apiId
     * @param apIRevisionDTO
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response createMCPServerRevision(String apiId, APIRevisionDTO apIRevisionDTO, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);

            //validate whether the API is advertise only
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            api.setOrganization(organization);
            APIDTO apiDto = APIMappingUtil.fromAPItoDTO(api, apiProvider);

            if (apiDto != null && apiDto.getAdvertiseInfo() != null && apiDto.getAdvertiseInfo().isAdvertised()) {
                throw new APIManagementException(
                        "Creating API Revisions is not supported for third party APIs: " + apiId,
                        ExceptionCodes.from(ExceptionCodes.THIRD_PARTY_API_REVISION_CREATION_UNSUPPORTED, apiId));
            }

            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            APIRevision apiRevision = new APIRevision();
            apiRevision.setApiUUID(apiId);
            apiRevision.setDescription(apIRevisionDTO.getDescription());
            Map<String, String> complianceResult = PublisherCommonUtils.checkGovernanceComplianceSync(apiId,
                    APIMGovernableState.API_DEPLOY, ArtifactType.API, organization, null, null);

            if (!complianceResult.isEmpty()
                    && complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY) != null
                    && !Boolean.parseBoolean(complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY))) {
                throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
            }

            //adding the api revision
            String revisionId = apiProvider.addAPIRevision(apiRevision, organization);

            //Retrieve the newly added APIRevision to send in the response payload
            APIRevision createdApiRevision = apiProvider.getAPIRevision(revisionId);
            APIRevisionDTO createdApiRevisionDTO = APIMappingUtil.fromAPIRevisiontoDTO(createdApiRevision);
            //This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS
                    + "/" + createdApiRevisionDTO.getApiInfo().getId() + "/"
                    + RestApiConstants.RESOURCE_PATH_REVISIONS + "/" + createdApiRevisionDTO.getId());
            PublisherCommonUtils.checkGovernanceComplianceAsync(apiId, APIMGovernableState.API_DEPLOY,
                    ArtifactType.API, organization);
            return Response.created(createdApiUri).entity(createdApiRevisionDTO).build();
        } catch (APIManagementException e) {
            if (e instanceof APIComplianceException) {
                throw e;
            }
            String errorMessage = "Error while adding new API Revision for API : " + apiId;
            if ((e.getErrorHandler()
                    .getErrorCode() == ExceptionCodes.THIRD_PARTY_API_REVISION_CREATION_UNSUPPORTED.getErrorCode())
                    || (e.getErrorHandler().getErrorCode() == ExceptionCodes.MAXIMUM_REVISIONS_REACHED.getErrorCode())) {
                throw e;
            } else {
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving created revision API location for API : "
                    + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response deleteMCPServer(String apiId, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);

            boolean isAPIExistDB = false;
            APIManagementException error = null;
            APIInfo apiInfo = null;
            try {
                apiInfo = CommonUtils.validateAPIExistence(apiId);
                if (APIConstants.API_TYPE_MCP.equals(apiInfo.getApiType())) {
                    isAPIExistDB = true;
                } else {
                    String errorMessage = "Error while validating API existence for deleting API : " + apiId + " on " +
                            "organization " + organization +
                            ". API is not of type MCP";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            } catch (APIManagementException e) {
                log.error("Error while validating API existence for deleting API " + apiId + " on organization "
                        + organization);
                error = e;
            }

            if (isAPIExistDB) {
                validateAPIOperationsPerLC(apiInfo.getStatus().toString());
                try {
                    List<SubscribedAPI> apiUsages = apiProvider.getAPIUsageByAPIId(apiId, organization);
                    if (apiUsages != null && !apiUsages.isEmpty()) {
                        List<SubscribedAPI> filteredUsages = new ArrayList<>();
                        for (SubscribedAPI usage : apiUsages) {
                            String subsCreatedStatus = usage.getSubCreatedStatus();
                            if (!APIConstants.SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subsCreatedStatus)) {
                                filteredUsages.add(usage);
                            }
                        }
                        if (!filteredUsages.isEmpty()) {
                            RestApiUtil.handleConflict("Cannot remove the API "
                                    + apiId + " as active subscriptions exist", log);
                        }
                    }
                } catch (APIManagementException e) {
                    log.error("Error while checking active subscriptions for deleting API "
                            + apiId + " on organization " + organization);
                    error = e;
                }
            }
            boolean isDeleted = false;
            try {
                apiProvider.deleteAPI(apiId, organization);
                isDeleted = true;
            } catch (APIManagementException e) {
                log.error("Error while deleting API " + apiId + "on organization " + organization, e);
            }

            if (error != null) {
                throw error;
            } else if (!isDeleted) {
                RestApiUtil.handleInternalServerError("Error while deleting API : " + apiId + " on organization "
                        + organization, log);
                return null;
            }
            PublisherCommonUtils.clearArtifactComplianceInfo(apiId, RestApiConstants.RESOURCE_API, organization);
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while deleting API : "
                        + apiId, e, log);
            } else {
                String errorMessage = "Error while deleting API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * @param apiId
     * @param revisionId
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response deleteMCPServerRevision(String apiId, String revisionId, MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        //validate if api exists
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        apiProvider.deleteAPIRevision(apiId, revisionId, organization);
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
        APIRevisionListDTO apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisions);
        return Response.ok().entity(apiRevisionListDTO).build();
    }

    /**
     * @param apiId
     * @param revisionId
     * @param apIRevisionDeploymentDTO
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response deployMCPServerRevision(String apiId, String revisionId,
                                            List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                            MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (revisionId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Revision Id is not provided").build();
        }

        //validate if api exists
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        APIDTO apiDto = APIMappingUtil.fromAPItoDTO(api, apiProvider);

        // Cannot deploy an API with custom sequence to the APK gateway
        Map endpointConfigMap = (Map) apiDto.getEndpointConfig();
        if (endpointConfigMap != null && !APIConstants.WSO2_SYNAPSE_GATEWAY.equals(apiDto.getGatewayType())
                && APIConstants.ENDPOINT_TYPE_SEQUENCE.equals(
                endpointConfigMap.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot Deploy an API with a Custom Sequence to APK Gateway: " + apiId).build();
        }

        if (apiDto.getLifeCycleStatus().equals(APIConstants.RETIRED)) {
            String errorMessage = "Deploying API Revisions is not supported for retired APIs. ApiId: " + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.RETIRED_API_REVISION_DEPLOYMENT_UNSUPPORTED, apiId));
        }
        if (apiDto != null && apiDto.getAdvertiseInfo() != null && Boolean.TRUE.equals(
                apiDto.getAdvertiseInfo().isAdvertised())) {
            String errorMessage = "Deploying API Revisions is not supported for third party APIs: " + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.THIRD_PARTY_API_REVISION_DEPLOYMENT_UNSUPPORTED, apiId));
        }

        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : apIRevisionDeploymentDTOList) {
            String environment = apiRevisionDeploymentDTO.getName();
            Boolean displayOnDevportal = apiRevisionDeploymentDTO.isDisplayOnDevportal();
            String vhost = apiRevisionDeploymentDTO.getVhost();
            APIRevisionDeployment apiRevisionDeployment = ApisApiServiceImplUtils.mapAPIRevisionDeploymentWithValidation(revisionId,
                    environments, environment, displayOnDevportal, vhost, true);
            apiRevisionDeployments.add(apiRevisionDeployment);
        }
        Map<String, String> complianceResult = PublisherCommonUtils.checkGovernanceComplianceSync(apiId,
                APIMGovernableState.API_DEPLOY, ArtifactType.API, organization, null, null);
        if (!complianceResult.isEmpty()
                && complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY) != null
                && !Boolean.parseBoolean(complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY))) {
            throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
        }
        apiProvider.deployAPIRevision(apiId, revisionId, apiRevisionDeployments, organization);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = apiProvider.getAPIRevisionsDeploymentList(apiId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        PublisherCommonUtils.checkGovernanceComplianceAsync(apiId, APIMGovernableState.API_DEPLOY,
                ArtifactType.API, organization);
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    @Override
    public Response updateMCPServer(String apiId, APIDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException {

        String[] tokenScopes =
                (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
        String username = RestApiCommonUtil.getLoggedInUsername();

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationInfo organizationInfo = RestApiUtil.getOrganizationInfo(messageContext);
            //validate if api exists
            CommonUtils.validateAPIExistence(apiId);
            if (!PublisherCommonUtils.validateEndpointConfigs(body)) {
                throw new APIManagementException("Invalid endpoint configs detected",
                        ExceptionCodes.INVALID_ENDPOINT_CONFIG);
            }

            // validate custom properties
            org.json.simple.JSONArray customProperties = APIUtil.getCustomProperties(organization);
            List<String> errorProperties = PublisherCommonUtils.validateMandatoryProperties(customProperties, body);
            if (!errorProperties.isEmpty()) {
                String errorString = " : " + String.join(", ", errorProperties);
                RestApiUtil.handleBadRequest(
                        ExceptionCodes.ERROR_WHILE_UPDATING_MANDATORY_PROPERTIES.getErrorMessage() + errorString,
                        ExceptionCodes.ERROR_WHILE_UPDATING_MANDATORY_PROPERTIES.getErrorCode(), log);
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
            validateAPIOperationsPerLC(originalAPI.getStatus());
            Map<String, String> complianceResult = PublisherCommonUtils
                    .checkGovernanceComplianceSync(originalAPI.getUuid(), APIMGovernableState.API_UPDATE,
                            ArtifactType.API, originalAPI.getOrganization(),
                            null, null);
            if (!complianceResult.isEmpty()
                    && complianceResult.get(GOVERNANCE_COMPLIANCE_KEY) != null
                    && !Boolean.parseBoolean(complianceResult.get(GOVERNANCE_COMPLIANCE_KEY))) {
                throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
            }

            API updatedApi = PublisherCommonUtils.updateApi(originalAPI, body, apiProvider, tokenScopes, organizationInfo);

            PublisherCommonUtils.checkGovernanceComplianceAsync(originalAPI.getUuid(), APIMGovernableState.API_UPDATE,
                    ArtifactType.API, originalAPI.getOrganization());
            return Response.ok().entity(APIMappingUtil.fromAPItoDTO(updatedApi)).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                if (e.getErrorHandler()
                        .getErrorCode() == ExceptionCodes.GLOBAL_MEDIATION_POLICIES_NOT_FOUND.getErrorCode()) {
                    RestApiUtil.handleResourceNotFoundError(e.getErrorHandler().getErrorDescription(), e, log);
                } else {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
                }
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while updating API : " + apiId, e, log);
            } else {
                throw e;
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage = "Error while encrypting the secret key of API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            String errorMessage = "Error while parsing endpoint config of API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * @param apiId
     * @param endpointId
     * @param apIEndpointDTO
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response updateMCPServerEndpoint(String apiId, String endpointId, APIEndpointDTO apIEndpointDTO, MessageContext messageContext) throws APIManagementException {

        return null;
    }

    /**
     * When the API is Published or Deprecated, only the users with scope "apim:api_import_export", "apim:api_publish",
     * "apim:admin" will be allowed for updating/deleting APIs or its sub-resources.
     *
     * @param status Status of the API which is currently created (current state)
     * @throws APIManagementException if update is not allowed
     */
    private void validateAPIOperationsPerLC(String status) throws APIManagementException {

        boolean updatePermittedForPublishedDeprecated = false;
        String[] tokenScopes =
                (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);

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

    /**
     * To check whether a particular exception is due to access control restriction.
     *
     * @param e Exception object.
     * @return true if the exception is caused due to authorization failure.
     */
    private boolean isAuthorizationFailure(Exception e) {

        String errorMessage = e.getMessage();
        return errorMessage != null && errorMessage.contains(APIConstants.UN_AUTHORIZED_ERROR_MESSAGE);
    }
}
