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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDataHolder;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.SharedOperationPolicyApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.OperationPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.Response;

public class SharedOperationPolicyApiServiceImpl implements SharedOperationPolicyApiService {

    private static final Log log = LogFactory.getLog(SharedOperationPolicyApiServiceImpl.class);

    /**
     * Add a shared operation policy
     *
     * @param sharedPolicySpecFileInputStream       Input stream of the shared policy specification file
     * @param sharedPolicySpecFileDetail            Shared policy specification
     * @param sharedPolicyDefinitionFileInputStream Input stream of the shared policy definition file
     * @param sharedPolicyDefinitionFileDetail      Definition of the shared policy
     * @param messageContext                        message context
     * @return Added shared operation policy DTO as response
     */
    @Override
    public Response addSharedOperationPolicy(InputStream sharedPolicySpecFileInputStream,
                                             Attachment sharedPolicySpecFileDetail,
                                             InputStream sharedPolicyDefinitionFileInputStream,
                                             Attachment sharedPolicyDefinitionFileDetail,
                                             MessageContext messageContext) throws APIManagementException {

        try {

            String sharedPolicySpec = "";
            String jsonContent = "";
            String sharedPolicyDefinition = "";
            OperationPolicySpecification policySpecification;
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            if (sharedPolicySpecFileInputStream != null) {
                sharedPolicySpec = RestApiPublisherUtils.readInputStream(sharedPolicySpecFileInputStream,
                        sharedPolicySpecFileDetail);
                jsonContent = CommonUtil.yamlToJson(sharedPolicySpec);
                policySpecification = new Gson().fromJson(jsonContent, OperationPolicySpecification.class);

                RestApiPublisherUtils.validateOperationPolicySpecification(policySpecification);

                if (sharedPolicyDefinitionFileInputStream != null) {
                    sharedPolicyDefinition =
                            RestApiPublisherUtils.readInputStream(sharedPolicyDefinitionFileInputStream,
                                    sharedPolicyDefinitionFileDetail);
                }

                OperationPolicyDataHolder operationPolicyData = new OperationPolicyDataHolder();
                operationPolicyData.setSpecification(policySpecification);
                operationPolicyData.setDefinition(sharedPolicyDefinition);
                String sharedPolicyID = apiProvider.addSharedOperationalPolicy(operationPolicyData, organization);
                operationPolicyData.setPolicyId(sharedPolicyID);

                if (log.isDebugEnabled()) {
                    log.debug("A Shared Operation policy has been added with name " +
                            policySpecification.getPolicyName() + " and uuid " + sharedPolicyID);
                }
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
     * Delete shared operation policy by providing the policy ID
     *
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return A zip file containing both (if exists) operation policy specification and policy definition
     */
    @Override
    public Response deleteSharedOperationPolicyByPolicyId(String operationPolicyId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            boolean isDeleted = apiProvider.deleteSharedOperationPolicyByPolicyId(operationPolicyId, organization);
            if (!isDeleted) {
                throw new APIManagementException("Error while deleting shared operation policy : " + operationPolicyId
                        + " on organization " + organization);
            }

            if (log.isDebugEnabled()) {
                log.debug("The shared operation policy " + operationPolicyId + " has been deleted");
            }
            return Response.ok().entity("The shared operation policy with ID " + operationPolicyId
                    + " has been deleted successfully").build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_SHARED_OPERATION_POLICY, e, log);
            } else {
                throw e;
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while deleting shared operation policy",
                    e, log);
        }
        return null;
    }

    /**
     * Get the list of all shared operation policies for a given tenant domain
     *
     * @param limit          max number of records returned
     * @param offset         starting index
     * @param messageContext message context
     * @return A list of operation policies available for the API
     */
    @Override
    public Response getAllSharedOperationPolicies(Integer limit, Integer offset, String query,
                                                  MessageContext messageContext) throws APIManagementException {

        try {
            limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
            offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

            // Lightweight shared operation policy includes the policy ID and the policy specification.
            // Since policy definition is bit bulky, we don't query the definition unnecessarily.
            List<OperationPolicyDataHolder>
                    sharedOperationPolicyLIst = apiProvider.getLightWeightSharedOperationPolicies(tenantDomain);
            OperationPolicyDataListDTO policyListDTO = OperationPolicyMappingUtil
                    .fromOperationPolicyDataListToDTO(sharedOperationPolicyLIst, offset, limit);
            return Response.ok().entity(policyListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_SHARED_OPERATION_POLICY, e, log);
            } else {
                throw e;
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An error has occurred while getting the list of all shared " +
                    " operation policies", e, log);
        }
        return null;
    }

    /**
     * Get the shared operation policy specification by providing the policy ID
     *
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return Operation policy DTO as response
     */
    @Override
    public Response getSharedOperationPolicyByPolicyId(String operationPolicyId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            OperationPolicyDataHolder policyDataHolder =
                    apiProvider.getSharedOperationPolicyByPolicyId(operationPolicyId,
                            organization, false);
            OperationPolicyDataDTO policyDataDTO =
                    OperationPolicyMappingUtil.fromOperationPolicyDataToDTO(policyDataHolder);
            return Response.ok().entity(policyDataDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_SHARED_OPERATION_POLICY, e, log);
            } else {
                throw e;
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An error has occurred while getting the shared operation " +
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
    public Response getSharedOperationPolicyContentByPolicyId(String operationPolicyId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            OperationPolicyDataHolder policyData = apiProvider.getSharedOperationPolicyByPolicyId(operationPolicyId,
                    organization, true);

            File file = RestApiPublisherUtils.exportOperationPolicyData(policyData);
            return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getName() + "\"").build();
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
