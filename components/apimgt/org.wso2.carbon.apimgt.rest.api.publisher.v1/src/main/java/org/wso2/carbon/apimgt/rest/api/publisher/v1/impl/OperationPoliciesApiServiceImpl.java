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

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.OperationPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.OperationPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl.OperationPoliciesApiCommonImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import javax.ws.rs.core.Response;

public class OperationPoliciesApiServiceImpl implements OperationPoliciesApiService {

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
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            if (policySpecFileInputStream != null) {
                String jsonContent = "";
                jsonContent = RestApiPublisherUtils.readInputStream(policySpecFileInputStream, policySpecFileDetail);

                String fileName = policySpecFileDetail.getDataHandler().getName();
                String fileContentType = URLConnection.guessContentTypeFromName(fileName);
                if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
                    fileContentType = policySpecFileDetail.getContentType().toString();
                }
                policySpecification = OperationPoliciesApiCommonImpl
                        .getPolicySpecification(fileContentType, jsonContent);

                OperationPolicyData operationPolicyData = OperationPoliciesApiCommonImpl
                        .prepareOperationPolicyData(policySpecification, organization);

                if (synapsePolicyDefinitionFileInputStream != null) {
                    String synapsePolicyDefinition =
                            RestApiPublisherUtils.readInputStream(synapsePolicyDefinitionFileInputStream,
                                    synapsePolicyDefinitionFileDetail);
                    synapseDefinition = new OperationPolicyDefinition();
                    OperationPoliciesApiCommonImpl
                            .preparePolicyDefinition(operationPolicyData, synapseDefinition,
                                    synapsePolicyDefinition, OperationPolicyDefinition.GatewayType.Synapse);
                }

                if (ccPolicyDefinitionFileInputStream != null) {
                    String choreoConnectPolicyDefinition = RestApiPublisherUtils
                            .readInputStream(ccPolicyDefinitionFileInputStream, ccPolicyDefinitionFileDetail);
                    ccPolicyDefinition = new OperationPolicyDefinition();
                    OperationPoliciesApiCommonImpl
                            .preparePolicyDefinition(operationPolicyData, ccPolicyDefinition,
                                    choreoConnectPolicyDefinition, OperationPolicyDefinition.GatewayType.ChoreoConnect);
                }

                if (policySpecification != null) {
                    String policyID = OperationPoliciesApiCommonImpl.addCommonOperationPolicy(policySpecification,
                            operationPolicyData, organization);
                    operationPolicyData.setPolicyId(policyID);
                    OperationPolicyDataDTO createdPolicy = OperationPolicyMappingUtil
                            .fromOperationPolicyDataToDTO(operationPolicyData);
                    URI createdPolicyUri = new URI(RestApiConstants.REST_API_PUBLISHER_VERSION
                            + RestApiConstants.PATH_DELIMITER + RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES
                            + RestApiConstants.PATH_DELIMITER + policyID);
                    return Response.created(createdPolicyUri).entity(createdPolicy).build();
                }
            }
        } catch (URISyntaxException e) {
            throw new APIManagementException("An Error has occurred while adding common operation policy", e,
                    ExceptionCodes.INTERNAL_ERROR);
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
    public Response deleteCommonOperationPolicyByPolicyId(String operationPolicyId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        OperationPoliciesApiCommonImpl.deleteCommonOperationPolicyByPolicyId(operationPolicyId, organization);
        return Response.ok().build();
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

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        OperationPolicyDataListDTO policyListDTO = OperationPoliciesApiCommonImpl.getAllCommonOperationPolicies(limit,
                offset, query, organization);
        return Response.ok().entity(policyListDTO).build();
    }

    /**
     * Get the common operation policy by providing the policy ID
     *
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return Operation policy DTO as response
     */
    @Override
    public Response getCommonOperationPolicyByPolicyId(String operationPolicyId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        OperationPolicyDataDTO policyDataDTO = OperationPoliciesApiCommonImpl.
                getCommonOperationPolicyByPolicyId(operationPolicyId, organization);
        return Response.ok().entity(policyDataDTO).build();
    }

    /**
     * Download the operation policy specification and definition for a given common operation policy
     *
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return A zip file containing both (if exists) operation policy specification and policy definition
     */
    @Override
    public Response getCommonOperationPolicyContentByPolicyId(String operationPolicyId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        OperationPolicyData policyData = OperationPoliciesApiCommonImpl
                .getCommonOperationPolicyContentByPolicyId(operationPolicyId, organization);
        File file = RestApiPublisherUtils.exportOperationPolicyData(policyData, ExportFormat.YAML.name());
        return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getName() + "\"").build();
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

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        OperationPolicyData policyData = OperationPoliciesApiCommonImpl.exportOperationPolicy(name, version,
                organization);
        ExportFormat exportFormat = StringUtils.isNotEmpty(format) ?
                ExportFormat.valueOf(format.toUpperCase()) :
                ExportFormat.YAML;
        File file = RestApiPublisherUtils.exportOperationPolicyData(policyData, exportFormat.name());
        return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getName() + "\"").build();
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
