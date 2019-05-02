/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.soaptorest.SequenceGenerator;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class ApisApiServiceImpl extends ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    @Override
    public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query,
            String ifNoneMatch, Boolean expand, String tenantDomain) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisPost(APIDTO body) {
        URI createdApiUri;
        APIDTO createdApiDTO;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String username = RestApiUtil.getLoggedInUsername();
            boolean isWSAPI = APIDTO.TypeEnum.WS == body.getType();
            boolean isSoapToRestConvertedApi = APIDTO.TypeEnum.SOAPTOREST == body.getType();

            // validate web socket api endpoint configurations
            if (isWSAPI) {
                if (!RestApiPublisherUtils.isValidWSAPI(body)) {
                    RestApiUtil.handleBadRequest("Endpoint URLs should be valid web socket URLs", log);
                }
            } else {
//                if (body.getApiDefinition() == null) {todo
//                    RestApiUtil.handleBadRequest("Parameter: \"apiDefinition\" cannot be null", log);
//                }
            }

//            String apiSecurity = body.getApiSecurity();todo
           /* if (!apiProvider.isClientCertificateBasedAuthenticationConfigured() && apiSecurity != null && apiSecurity
                    .contains(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                RestApiUtil.handleBadRequest("Mutual SSL Based authentication is not supported in this server", log);
            }*/
            if (body.getAccessControlRoles() != null) {
                String errorMessage = RestApiPublisherUtils.validateUserRoles(body.getAccessControlRoles());

                if (!errorMessage.isEmpty()) {
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            }
            if (body.getAdditionalProperties() != null) {
                String errorMessage = RestApiPublisherUtils
                        .validateAdditionalProperties(body.getAdditionalProperties());
                if (!errorMessage.isEmpty()) {
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            }
            if (body.getContext() == null) {
                RestApiUtil.handleBadRequest("Parameter: \"context\" cannot be null", log);
            } else if (body.getContext().endsWith("/")) {
                RestApiUtil.handleBadRequest("Context cannot end with '/' character", log);
            }
            if (apiProvider.isApiNameWithDifferentCaseExist(body.getName())) {
                RestApiUtil.handleBadRequest("Error occurred while adding API. API with name " + body.getName()
                        + " already exists.", log);
            }

            //Get all existing versions of  api been adding
            List<String> apiVersions = apiProvider.getApiVersionsMatchingApiName(body.getName(), username);
            if (apiVersions.size() > 0) {
                //If any previous version exists
                for (String version : apiVersions) {
                    if (version.equalsIgnoreCase(body.getVersion())) {
                        //If version already exists
                        if (apiProvider.isDuplicateContextTemplate(body.getContext())) {
                            RestApiUtil.handleResourceAlreadyExistsError("Error occurred while " +
                                    "adding the API. A duplicate API already exists for "
                                    + body.getName() + "-" + body.getVersion(), log);
                        } else {
                            RestApiUtil.handleBadRequest("Error occurred while adding API. API with name " +
                                    body.getName() + " already exists with different " +
                                    "context", log);
                        }
                    }
                }
            } else {
                //If no any previous version exists
                if (apiProvider.isDuplicateContextTemplate(body.getContext())) {
                    RestApiUtil.handleBadRequest("Error occurred while adding the API. A duplicate API context " +
                            "already exists for " + body.getContext(), log);
                }
            }

            //Check if the user has admin permission before applying a different provider than the current user
            String provider = body.getProvider();
            if (!StringUtils.isBlank(provider) && !provider.equals(username)) {
                if (!APIUtil.hasPermission(username, APIConstants.Permissions.APIM_ADMIN)) {
                    if (log.isDebugEnabled()) {
                        log.debug("User " + username + " does not have admin permission ("
                                + APIConstants.Permissions.APIM_ADMIN + ") hence provider (" +
                                provider + ") overridden with current user (" + username + ")");
                    }
                    provider = username;
                }
            } else {
                //Set username in case provider is null or empty
                provider = username;
            }

            List<String> tiersFromDTO = body.getPolicies();
            //If tiers are not defined, the api should be a PROTOTYPED one,
            if (!APIConstants.PROTOTYPED.equals(body.getLifeCycleStatus()) &&
                    (tiersFromDTO == null || tiersFromDTO.isEmpty())) {
                RestApiUtil.handleBadRequest("No tier defined for the API", log);
            }
            //check whether the added API's tiers are all valid
            Set<Tier> definedTiers = apiProvider.getTiers();
            List<String> invalidTiers = RestApiUtil.getInvalidTierNames(definedTiers, tiersFromDTO);
            if (invalidTiers.size() > 0) {
                RestApiUtil.handleBadRequest(
                        "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid", log);
            }
            APIPolicy apiPolicy = apiProvider.getAPIPolicy(username, body.getApiPolicy());
            if (apiPolicy == null && body.getApiPolicy() != null) {
                RestApiUtil.handleBadRequest(
                        "Specified policy " + body.getApiPolicy() + " is invalid", log);
            }
//            if (isSoapToRestConvertedApi && StringUtils.isNotBlank(body.getWsdlUri())) {
//                String swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(body.getWsdlUri());
//                body.setApiDefinition(swaggerStr);
//            }
            API apiToAdd = APIMappingUtil.fromDTOtoAPI(body, provider);
            //Overriding some properties:
            //only allow CREATED as the stating state for the new api if not status is PROTOTYPED
            if (!APIConstants.PROTOTYPED.equals(apiToAdd.getStatus())) {
                apiToAdd.setStatus(APIConstants.CREATED);
            }
            //we are setting the api owner as the logged in user until we support checking admin privileges and assigning
            //  the owner as a different user
            apiToAdd.setApiOwner(provider);

            //attach micro-geteway labels
            apiToAdd = assignLabelsToDTO(body,apiToAdd);

            //adding the api
            apiProvider.addAPI(apiToAdd);
            if (isSoapToRestConvertedApi) {
                if (StringUtils.isNotBlank(apiToAdd.getWsdlUrl())) {
                    String swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(body.getWsdlUri());
                    apiProvider.saveSwagger20Definition(apiToAdd.getId(), swaggerStr);
                    SequenceGenerator.generateSequencesFromSwagger(swaggerStr, new Gson().toJson(body));
                } else {
                    String errorMessage =
                            "Error while generating the swagger since the wsdl url is null for: " + body.getProvider()
                                    + "-" + body.getName() + "-" + body.getVersion();
                    RestApiUtil.handleInternalServerError(errorMessage, log);
                }
            } /*else if (!isWSAPI) {todo
                apiProvider.saveSwagger20Definition(apiToAdd.getId(), body.getApiDefinition());
            }*/
            APIIdentifier createdApiId = apiToAdd.getId();
            //Retrieve the newly added API to send in the response payload
            API createdApi = apiProvider.getAPI(createdApiId);
            createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);
            //This URI used to set the location header of the POST response
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (JSONException e) {
            String errorMessage = "Error while validating endpoint configurations : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion() + "-" /*+ body.getEndpointConfig()*/;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
//        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDelete(String apiId, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId,
            String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdContentPost(String apiId, String documentId,
            InputStream fileInputStream, Attachment fileDetail, String inlineContent, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdDelete(String apiId, String documentId, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdPut(String apiId, String documentId, DocumentDTO body,
            String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDocumentsPost(String apiId, DocumentDTO body, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdGatewayConfigGet(String apiId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdGatewayConfigPut(String apiId, String gatewayConfig, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdGet(String apiId, String xWSO2Tenant, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdLifecycleGet(String apiId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdLifecycleHistoryGet(String apiId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdLifecycleLifecyclePendingTaskDelete(String apiId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdPoliciesMediationGet(String apiId, Integer limit, Integer offset, String query,
            String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdPoliciesMediationMediationPolicyIdDelete(String apiId, String mediationPolicyId,
            String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdPoliciesMediationMediationPolicyIdGet(String apiId, String mediationPolicyId,
            String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdPoliciesMediationMediationPolicyIdPut(String apiId, String mediationPolicyId,
            MediationDTO body, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdPoliciesMediationPost(MediationDTO body, String apiId, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdPut(String apiId, APIDTO body, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdResourcePoliciesGet(String apiId, String sequenceType, String resourcePath,
            String verb, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdResourcePoliciesResourcePolicyIdGet(String apiId, String resourcePolicyId,
            String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdResourcePoliciesResourcePolicyIdPut(String apiId, String resourcePolicyId,
            ResourcePolicyInfoDTO body, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdScopesGet(String apiId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdScopesNameDelete(String apiId, String name, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdScopesNameGet(String apiId, String name, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdScopesNamePut(String apiId, String name, ScopeDTO body, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdScopesPost(String apiId, ScopeDTO body, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdSwaggerGet(String apiId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdSwaggerPut(String apiId, String apiDefinition, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdThreatProtectionPoliciesDelete(String apiId, String policyId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdThreatProtectionPoliciesGet(String apiId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdThreatProtectionPoliciesPost(String apiId, String policyId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdThumbnailGet(String apiId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdThumbnailPost(String apiId, InputStream fileInputStream, Attachment fileDetail,
            String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdWsdlGet(String apiId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdWsdlPut(String apiId, InputStream fileInputStream, Attachment fileDetail,
            String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisChangeLifecyclePost(String action, String apiId, String lifecycleChecklist,
            String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisCopyApiPost(String newVersion, String apiId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisHead(String query, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisImportDefinitionPost(String type, InputStream fileInputStream, Attachment fileDetail,
            String url, String additionalProperties, String implementationType, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisValidateDefinitionPost(String type, String url, InputStream fileInputStream,
            Attachment fileDetail) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    /**
     * This method is used to assign micro gateway labels to the DTO
     *
     * @param apiDTO API DTO
     * @param api the API object
     * @return the API object with labels
     */
    private API assignLabelsToDTO(APIDTO apiDTO, API api) {

        if (apiDTO.getLabels() != null) {
            List<LabelDTO> dtoLabels = apiDTO.getLabels();
            List<Label> labelList = new ArrayList<>();
            for (LabelDTO labelDTO : dtoLabels) {
                Label label = new Label();
                label.setName(labelDTO.getName());
//                label.setDescription(labelDTO.getDescription()); todo add description
                labelList.add(label);
            }
            api.setGatewayLabels(labelList);
        }
        return api;
    }
}
