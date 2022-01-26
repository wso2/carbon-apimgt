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
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDefinitionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class SharedOperationPolicyApiServiceImpl implements SharedOperationPolicyApiService {

    private static final Log log = LogFactory.getLog(SharedOperationPolicyApiServiceImpl.class);

    @Override
    public Response addSharedOperationPolicy(InputStream sharedPolicySpecFileInputStream,
                                               Attachment sharedPolicySpecFileDetail,
                                               InputStream sharedPolicyDefinitionFileInputStream,
                                               Attachment sharedPolicyDefinitionFileDetail,
                                               MessageContext messageContext) throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String sharedPolicySpec = "";
            String jsonContent = "";
            String sharedPolicyDefinition = "";
            OperationPolicySpecification policySpecification;
            if (sharedPolicySpecFileInputStream != null) {
                sharedPolicySpec =
                        RestApiPublisherUtils.readInputStream(sharedPolicySpecFileInputStream, sharedPolicySpecFileDetail);
                jsonContent = CommonUtil.yamlToJson(sharedPolicySpec);
                policySpecification = new Gson().fromJson(jsonContent, OperationPolicySpecification.class);

                RestApiPublisherUtils.validateOperationPolicySpecification(policySpecification);

                if (sharedPolicyDefinitionFileInputStream != null) {
                    sharedPolicyDefinition =
                            RestApiPublisherUtils
                                    .readInputStream(sharedPolicyDefinitionFileInputStream, sharedPolicyDefinitionFileDetail);
                }

                OperationPolicyDataHolder operationPolicyData = new OperationPolicyDataHolder();
                operationPolicyData.setSpecification(policySpecification);
                operationPolicyData.setDefinition(sharedPolicyDefinition);
                String sharedPolicyID = apiProvider.addSharedOperationalPolicy(operationPolicyData);

                if (log.isDebugEnabled()) {
                    log.debug("Shared Operation policy has been added with name " +
                            policySpecification.getPolicyName());
                }

                if (operationPolicyData != null) {
                    OperationPolicyDefinitionDTO createdPolicy = new OperationPolicyDefinitionDTO();
                    createdPolicy.setName(policySpecification.getPolicyName());
                    createdPolicy.setPolicyId(sharedPolicyID);
                    createdPolicy.setApiTypes(policySpecification.getApiTypes());
                    createdPolicy.setFlows(policySpecification.getFlow());
                    createdPolicy.setGatewayTypes(policySpecification.getSupportedGatewayTypes());
                    return Response.ok().entity(createdPolicy).build();
                }
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, e, log);
            } else {
                throw e;
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while adding shared operational policy",
                    e, log);
        }
        return null;
    }

    @Override
    public Response getAllSharedOperationPolicies(Integer limit, Integer offset, String query,
                                                   MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }
}
