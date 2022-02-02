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

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDataHolder;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.OperationPolicyApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.OperationPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;

import javax.ws.rs.core.Response;

public class OperationPolicyApiServiceImpl implements OperationPolicyApiService {

    private static final Log log = LogFactory.getLog(OperationPolicyApiServiceImpl.class);

    /**
     * Add a shared operation policy
     *
     * @param policySpecFileInputStream       Input stream of the shared policy specification file
     * @param policySpecFileDetail            Shared policy specification
     * @param policyDefinitionFileInputStream Input stream of the shared policy definition file
     * @param policyDefinitionFileDetail      Definition of the shared policy
     * @param messageContext                        message context
     * @return Added shared operation policy DTO as response
     */
    @Override
    public Response addCommonOperationPolicy(InputStream policySpecFileInputStream, Attachment policySpecFileDetail,
                                             InputStream policyDefinitionFileInputStream,
                                             Attachment policyDefinitionFileDetail, MessageContext messageContext
                                            ) throws APIManagementException {

        try {
            String policySpec = "";
            String jsonContent = "";
            String policyDefinition = "";
            OperationPolicySpecification policySpecification;
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            if (policySpecFileInputStream != null) {
                policySpec = RestApiPublisherUtils.readInputStream(policySpecFileInputStream, policySpecFileDetail);

                String fileName = policySpecFileDetail.getDataHandler().getName();
                String fileContentType = URLConnection.guessContentTypeFromName(fileName);
                if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
                    fileContentType = policySpecFileDetail.getContentType().toString();
                }
                log.info(fileName + fileContentType);

                jsonContent = CommonUtil.yamlToJson(policySpec);
                Schema schema = APIUtil.retrieveOperationPolicySpecificationJsonSchema();
                if (schema != null) {
                    try {
                        org.json.JSONObject uploadedConfig = new org.json.JSONObject(jsonContent);
                        schema.validate(uploadedConfig);
                    } catch (ValidationException e) {
                        List<String> errors = e.getAllMessages();
                        String errorMessage = errors.size() + " validation error(s) found. Error(s) :" + errors.toString();
                        throw new APIManagementException("Policy specification validation failure. "+ errorMessage,
                                ExceptionCodes.from(ExceptionCodes.INVALID_OPERATION_POLICY_SPECIFICATION,
                                        errorMessage));
                    }
                }

                policySpecification = new Gson().fromJson(jsonContent, OperationPolicySpecification.class);

                if (policyDefinitionFileInputStream != null) {
                    policyDefinition =
                            RestApiPublisherUtils.readInputStream(policyDefinitionFileInputStream,
                                    policyDefinitionFileDetail);
                }

                OperationPolicyDataHolder operationPolicyData = new OperationPolicyDataHolder();
                operationPolicyData.setTenantDomain(organization);
                operationPolicyData.setMd5Hash(APIUtil.getMd5OfOperationPolicy(jsonContent, policyDefinition));
                operationPolicyData.setSpecification(policySpecification);
                operationPolicyData.setDefinition(policyDefinition);

                OperationPolicyDataHolder existingPolicy =
                        apiProvider.getCommonOperationPolicyByPolicyName(policySpecification.getName(), organization, false);
                String policyID;
                if (existingPolicy != null) {
                    if (existingPolicy.isApiSpecificPolicy()) {
                        throw new APIManagementException("An API specific operation policy exists with the same " +
                                " policy name. Please use a different name.");
                    }
                    policyID = existingPolicy.getPolicyId();
                    apiProvider.updateOperationPolicy(policyID, operationPolicyData, organization);
                    if (log.isDebugEnabled()) {
                        log.debug("Existing common operation policy with name " + policySpecification.getName()
                                + " has been updated");
                    }
                } else {
                    policyID = apiProvider.addOperationPolicy(operationPolicyData, organization);
                    if (log.isDebugEnabled()) {
                        log.debug("A common operation policy has been added with name " + policySpecification.getName());
                    }
                }
                operationPolicyData.setPolicyId(policyID);
                OperationPolicyDataDTO createdPolicy = OperationPolicyMappingUtil
                        .fromOperationPolicyDataToDTO(operationPolicyData);
                return Response.ok().entity(createdPolicy).build();
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_SHARED_OPERATION_POLICY, e, log);
            } else {
                throw e;
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while adding shared operation policy",
                    e, log);
        }
        return null;
    }

    /**
     * Delete common operation policy by providing the policy ID
     *
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return ok if deleted successfully
     */
    @Override
    public Response deleteCommonOperationPolicyByPolicyId(String operationPolicyId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            OperationPolicyDataHolder existingPolicy =
                    apiProvider.getOperationPolicyByPolicyId(operationPolicyId, false);
            if (existingPolicy != null) {
                if (existingPolicy.isApiSpecificPolicy()) {
                    throw new APIManagementException("Cannot delete an API specific operation policy at the " +
                            " common policies resource.");
                }
                if (!organization.equals(existingPolicy.getTenantDomain())) {
                    throw new APIManagementException("Cannot delete the specified operation policy");
                }

                boolean isDeleted = apiProvider.deleteOperationPolicyById(operationPolicyId, organization);
                if (!isDeleted) {
                    throw new APIManagementException("Error while deleting common operation policy : " + operationPolicyId
                            + " on organization " + organization);
                }

                if (log.isDebugEnabled()) {
                    log.debug("The common operation policy " + operationPolicyId + " has been deleted");
                }
                return Response.ok().entity("The common operation policy with ID " + operationPolicyId
                        + " has been deleted successfully").build();
            } else {
                throw new APIManagementException("Cannot delete the operation policy " + operationPolicyId
                        + " on organization " + organization + " as it does not exists");
            }

        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_SHARED_OPERATION_POLICY, e, log);
            } else {
                throw e;
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while deleting common operation policy",
                    e, log);
        }
        return null;
    }

    /**
     * Get the list of all common operation policies for a given tenant domain
     *
     * @param limit          max number of records returned
     * @param offset         starting index
     * @param messageContext message context
     * @return A list of operation policies available for the API
     */
    @Override
    public Response getAllCommonOperationPolicies(Integer limit, Integer offset, String query,
                                                  MessageContext messageContext) throws APIManagementException {

        try {
            limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
            offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

            // Since policy definition is bit bulky, we don't query the definition unnecessarily.
            List<OperationPolicyDataHolder> commonOperationPolicyLIst =
                    apiProvider.getAllCommonOperationPolicies(tenantDomain);
            OperationPolicyDataListDTO policyListDTO = OperationPolicyMappingUtil
                    .fromOperationPolicyDataListToDTO(commonOperationPolicyLIst, offset, limit);
            return Response.ok().entity(policyListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_SHARED_OPERATION_POLICY, e, log);
            } else {
                throw e;
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An error has occurred while getting the list of all common " +
                    " operation policies", e, log);
        }
        return null;
    }

    /**
     * Get the common operation policy specification by providing the policy ID
     *
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return Operation policy DTO as response
     */
    @Override
    public Response getCommonOperationPolicyByPolicyId(String operationPolicyId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            OperationPolicyDataHolder existingPolicy =
                    apiProvider.getOperationPolicyByPolicyId(operationPolicyId, false);
            if (existingPolicy != null && !existingPolicy.isApiSpecificPolicy()) {
                OperationPolicyDataDTO policyDataDTO =
                        OperationPolicyMappingUtil.fromOperationPolicyDataToDTO(existingPolicy);
                return Response.ok().entity(policyDataDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_SHARED_OPERATION_POLICY, operationPolicyId, log);
            }

        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_SHARED_OPERATION_POLICY, e, log);
            } else {
                throw e;
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An error has occurred while getting the common operation " +
                    " policy", e, log);
        }
        return null;
    }

    /**
     * Download the operation policy specification and definition for a given shared operation policy
     *
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return A zip file containing both (if exists) operation policy specification and policy definition
     */
    @Override
    public Response getCommonOperationPolicyContentByPolicyId(String operationPolicyId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            OperationPolicyDataHolder policyData = apiProvider.getOperationPolicyByPolicyId(operationPolicyId, true);
            if (policyData != null && !policyData.isApiSpecificPolicy()) {
                File file = RestApiPublisherUtils.exportOperationPolicyData(policyData);
                return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"").build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_SHARED_OPERATION_POLICY, operationPolicyId, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_SHARED_OPERATION_POLICY, e, log);
            } else {
                throw e;
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An error has occurred while exporting the shared operation " +
                    " policy", e, log);
        }
        return null;
    }
}
