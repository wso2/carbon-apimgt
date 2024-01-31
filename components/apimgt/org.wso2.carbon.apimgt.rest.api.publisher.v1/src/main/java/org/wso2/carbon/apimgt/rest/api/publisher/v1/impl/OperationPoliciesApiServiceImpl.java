/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.restapi.publisher.OperationPoliciesApiServiceImplUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.OperationPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.OperationPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class OperationPoliciesApiServiceImpl implements OperationPoliciesApiService {

    private static final Log log = LogFactory.getLog(OperationPoliciesApiServiceImpl.class);

    /**
     * Add a common operation policy that can be used by all the APIs
     *
     * @param policySpecFileInputStream              Input stream of the common policy specification file
     * @param policySpecFileDetail                   Common policy specification
     * @param synapsePolicyDefinitionFileInputStream Input stream of the synapse policy definition file
     * @param synapsePolicyDefinitionFileDetail      Synapse definition of the operation policy
     * @param ccPolicyDefinitionFileInputStream      Input stream of the choreo connect policy definition file
     * @param ccPolicyDefinitionFileDetail           Choreo connect definition of the operation policy
     * @param messageContext                         message context
     * @return Added common operation policy DTO as response
     */
    @Override
    public Response addCommonOperationPolicy(InputStream policySpecFileInputStream, Attachment policySpecFileDetail,
                                             InputStream synapsePolicyDefinitionFileInputStream,
                                             Attachment synapsePolicyDefinitionFileDetail,
                                             InputStream ccPolicyDefinitionFileInputStream,
                                             Attachment ccPolicyDefinitionFileDetail,
                                             MessageContext messageContext) throws APIManagementException {

        try {
            OperationPolicyDefinition ccPolicyDefinition = null;
            OperationPolicyDefinition synapseDefinition = null;
            OperationPolicySpecification policySpecification;
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            if (policySpecFileInputStream != null) {
                String jsonContent = "";
                jsonContent = RestApiPublisherUtils.readInputStream(policySpecFileInputStream, policySpecFileDetail);

                String fileName = policySpecFileDetail.getDataHandler().getName();
                String fileContentType = FilenameUtils.getExtension(fileName);
                if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
                    fileContentType = policySpecFileDetail.getContentType().toString();
                }
                if (APIConstants.YAML_CONTENT_TYPE.equals(fileContentType)) {
                    jsonContent = CommonUtil.yamlToJson(jsonContent);
                }
                policySpecification = APIUtil.getValidatedOperationPolicySpecification(jsonContent);

                OperationPolicyData operationPolicyData = OperationPoliciesApiServiceImplUtils
                        .prepareOperationPolicyData(policySpecification, organization);

                if (synapsePolicyDefinitionFileInputStream != null) {
                    String synapsePolicyDefinition =
                            RestApiPublisherUtils.readInputStream(synapsePolicyDefinitionFileInputStream,
                                    synapsePolicyDefinitionFileDetail);
                    synapseDefinition = new OperationPolicyDefinition();
                    OperationPoliciesApiServiceImplUtils
                            .preparePolicyDefinition(operationPolicyData, synapseDefinition,
                                    synapsePolicyDefinition, OperationPolicyDefinition.GatewayType.Synapse);
                }

                if (ccPolicyDefinitionFileInputStream != null) {
                    String choreoConnectPolicyDefinition = RestApiPublisherUtils
                            .readInputStream(ccPolicyDefinitionFileInputStream, ccPolicyDefinitionFileDetail);
                    ccPolicyDefinition = new OperationPolicyDefinition();
                    OperationPoliciesApiServiceImplUtils
                            .preparePolicyDefinition(operationPolicyData, ccPolicyDefinition,
                                    choreoConnectPolicyDefinition, OperationPolicyDefinition.GatewayType.ChoreoConnect);
                }
                OperationPolicyData existingPolicy =
                        apiProvider.getCommonOperationPolicyByPolicyName(policySpecification.getName(),
                                policySpecification.getVersion(), organization, false);
                String policyID;
                if (existingPolicy == null) {
                    policyID = apiProvider.addCommonOperationPolicy(operationPolicyData, organization);
                    if (log.isDebugEnabled()) {
                        log.debug("A common operation policy has been added with name "
                                + policySpecification.getName());
                    }
                } else {
                    throw new APIManagementException("Existing common operation policy found for the same name.");
                }
                operationPolicyData.setPolicyId(policyID);
                OperationPolicyDataDTO createdPolicy = OperationPolicyMappingUtil
                        .fromOperationPolicyDataToDTO(operationPolicyData);
                URI createdPolicyUri = new URI(RestApiConstants.REST_API_PUBLISHER_VERSION + "/"
                        + RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES + "/" + policyID);
                return Response.created(createdPolicyUri).entity(createdPolicy).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding a common operation policy." + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while adding common operation policy",
                    e, log);
        }
        return null;
    }

    /**
     * Delete a common operation policy by providing the policy ID
     *
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return ok if deleted successfully
     */
    @Override
    public Response deleteCommonOperationPolicyByPolicyId(String operationPolicyId, MessageContext messageContext) {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            OperationPolicyData existingPolicy =
                    apiProvider.getCommonOperationPolicyByPolicyId(operationPolicyId, organization, false);
            int policyUsageInGatewayPolicyMappings = apiProvider.getPolicyUsageByPolicyUUIDInGatewayPolicies(
                    operationPolicyId);
            if (policyUsageInGatewayPolicyMappings > 0) {
                RestApiUtil.handleConflict("Common operation policy: " + operationPolicyId
                        + " is already used in gateway policies. Cannot delete the policy", log);
            } else if (existingPolicy != null) {
                apiProvider.deleteOperationPolicyById(operationPolicyId, organization);
                if (log.isDebugEnabled()) {
                    log.debug("The common operation policy " + operationPolicyId + " has been deleted");
                }
                return Response.ok().build();
            } else {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve an existing API policy with ID: "
                        + operationPolicyId, ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND,
                        operationPolicyId));
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES,
                        operationPolicyId, e, log);
            } else {
                String errorMessage = "Error while deleting the common operation policy with ID: " + operationPolicyId
                        + " " + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while deleting the common operation policy + "
                    + operationPolicyId, e, log);
        }
        return null;
    }

    /**
     * Get the list of all common operation policies for a given organization
     *
     * @param limit          max number of records returned
     * @param offset         starting index
     * @param messageContext message context
     * @return A list of operation policies available for the API
     */
    @Override
    public Response getAllCommonOperationPolicies(Integer limit, Integer offset, String query,
                                                  MessageContext messageContext) throws APIManagementException {

        String apiManagementExceptionErrorMessage = "";
        OperationPolicyDataListDTO policyListDTO = null;
        String name = null;
        String version = null;
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            // If name & version are given, it returns the policy data
            if (query != null) {
                Map<String, String> queryParamMap = OperationPoliciesApiServiceImplUtils.getQueryParams(query);
                name = queryParamMap.get(ImportExportConstants.POLICY_NAME);
                version = queryParamMap.get(ImportExportConstants.VERSION_ELEMENT);

                apiManagementExceptionErrorMessage = "Error while retrieving the policy by name & version.";
                OperationPolicyData policyData = apiProvider.getCommonOperationPolicyByPolicyName(name, version,
                        organization, false);

                // if not found, throw not found error
                if (policyData != null) {
                    List<OperationPolicyData> commonOperationPolicyLIst = new ArrayList<>();
                    commonOperationPolicyLIst.add(policyData);
                    policyListDTO = OperationPolicyMappingUtil.fromOperationPolicyDataListToDTO(
                            commonOperationPolicyLIst, 0, 1);
                } else {
                    apiManagementExceptionErrorMessage =
                            "Couldn't retrieve an existing common policy with Name: " + name + " and Version: "
                                    + version;
                    throw new APIMgtResourceNotFoundException(apiManagementExceptionErrorMessage,
                            ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND_WITH_NAME_AND_VERSION, name,
                                    version));
                }
            } else {
                offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
                apiManagementExceptionErrorMessage = "Error while retrieving the list of all common operation policies.";

                // Since policy definition is bit bulky, we don't query the definition unnecessarily.
                List<OperationPolicyData> commonOperationPolicyLIst = apiProvider.getAllCommonOperationPolicies(
                        organization);

                // Set limit to the query param value or the count of all policies
                limit = limit != null ? limit : commonOperationPolicyLIst.size();
                policyListDTO = OperationPolicyMappingUtil.fromOperationPolicyDataListToDTO(commonOperationPolicyLIst,
                        offset, limit);
            }
            return Response.ok().entity(policyListDTO).build();

        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                throw new APIManagementException(apiManagementExceptionErrorMessage,
                        ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND_WITH_NAME_AND_VERSION, name,
                                version));
            } else {
                apiManagementExceptionErrorMessage += e.getMessage();
                RestApiUtil.handleInternalServerError(apiManagementExceptionErrorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Get the common operation policy by providing the policy ID
     *
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return Operation policy DTO as response
     */
    @Override
    public Response getCommonOperationPolicyByPolicyId(String operationPolicyId, MessageContext messageContext) {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            OperationPolicyData existingPolicy =
                    apiProvider.getCommonOperationPolicyByPolicyId(operationPolicyId, organization, false);
            if (existingPolicy != null) {
                OperationPolicyDataDTO policyDataDTO =
                        OperationPolicyMappingUtil.fromOperationPolicyDataToDTO(existingPolicy);
                return Response.ok().entity(policyDataDTO).build();
            } else {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve an existing common policy with ID: "
                        + operationPolicyId, ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND,
                        operationPolicyId));
            }

        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES,
                        operationPolicyId, e, log);
            } else {
                String errorMessage = "Error while getting the common operation policy with ID :" + operationPolicyId
                        + " " + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An error has occurred while getting the common operation " +
                    " policy with ID: " + operationPolicyId, e, log);
        }
        return null;
    }

    /**
     * Download the operation policy specification and definition for a given common operation policy
     *
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return A zip file containing both (if exists) operation policy specification and policy definition
     */
    @Override
    public Response getCommonOperationPolicyContentByPolicyId(String operationPolicyId, MessageContext messageContext) {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            OperationPolicyData policyData =
                    apiProvider.getCommonOperationPolicyByPolicyId(operationPolicyId, organization, true);
            if (policyData != null) {
                File file = RestApiPublisherUtils.exportOperationPolicyData(policyData, ExportFormat.YAML.name());
                return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"").build();
            } else {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve an existing common policy with ID: "
                        + operationPolicyId, ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND,
                        operationPolicyId));
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES,
                        operationPolicyId, e, log);
            } else {
                String errorMessage = "Error while getting the content of common operation policy with ID :"
                        + operationPolicyId + " " + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError(
                    "An error has occurred while getting the content of the common operation " +
                            " policy with ID " + operationPolicyId, e, log);
        }
        return null;
    }

    /**
     * Export Operation Policy as an archived file.
     *
     * @param name           Name of the operation policy
     * @param version        Version of the operation policy
     * @param messageContext message context
     * @throws APIManagementException If an error occurs while creating the directory, transferring files or
     *                                extracting the content
     * @returnA zip file containing both (if exists) operation policy specification and policy definition
     */
    @Override
    public Response exportOperationPolicy(String name, String version, String format, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        OperationPolicyData policyData = apiProvider.getCommonOperationPolicyByPolicyName(name, version, organization,
                true);
        if (policyData != null) {
            ExportFormat exportFormat = StringUtils.isNotEmpty(format) ?
                    ExportFormat.valueOf(format.toUpperCase()) :
                    ExportFormat.YAML;
            File file = RestApiPublisherUtils.exportOperationPolicyData(policyData, exportFormat.name());
            return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getName() + "\"").build();
        } else {
            // if not found, throw not found error
            throw new APIManagementException(
                    "Couldn't retrieve an existing common policy with Name: " + name + " and Version: " + version,
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND_WITH_NAME_AND_VERSION, name, version));
        }
    }

    /**
     * @param fileInputStream Archived file
     * @param fileDetail      file details
     * @param messageContext  message context
     * @return return a response with the corresponding status code
     * @throws APIManagementException If an error occurs while creating the directory, transferring files or
     *                                extracting the content
     */
    @Override
    public Response importOperationPolicy(InputStream fileInputStream, Attachment fileDetail,
                                          MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String extractedFolderPath = null;
        OperationPolicyDataDTO createdPolicy = null;
        URI createdPolicyUri = null;

        try {
            extractedFolderPath = ImportUtils.getArchivePathOfPolicyExtractedDirectory(fileInputStream);
            createdPolicy = ImportUtils.importPolicy(extractedFolderPath, organization, apiProvider);
            createdPolicyUri = new URI(RestApiConstants.REST_API_PUBLISHER_VERSION + File.separator
                    + RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES + File.separator + createdPolicy.getId());
            return Response.created(createdPolicyUri).entity(createdPolicy).build();
        } catch (URISyntaxException e) {
            String errorMessage = "An Error has occurred while adding common operation policy. " + e.getMessage();
            throw new APIManagementException(errorMessage, ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR));
        }
    }
}
