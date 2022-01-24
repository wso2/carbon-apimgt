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
import org.apache.commons.io.IOUtils;
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
import org.wso2.carbon.apimgt.rest.api.publisher.v1.OperationPolicyTemplatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDefinitionDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

public class OperationPolicyTemplatesApiServiceImpl implements OperationPolicyTemplatesApiService {

    private static final Log log = LogFactory.getLog(OperationPolicyTemplatesApiServiceImpl.class);

    @Override
    public Response addOperationPolicyTemplate(InputStream templateSpecFileInputStream,
                                               Attachment templateSpecFileDetail,
                                               InputStream templateDefinitionFileInputStream,
                                               Attachment templateDefinitionFileDetail, String templateName,
                                               String flow,
                                               MessageContext messageContext) throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String templateSpec = "";
            String jsonContent = "";
            String templateDefinition = "";
            OperationPolicySpecification policySpecification;
            if (templateSpecFileInputStream != null) {
                templateSpec = readInputStream(templateSpecFileInputStream, templateSpecFileDetail);
                jsonContent = CommonUtil.yamlToJson(templateSpec);
                policySpecification = new Gson().fromJson(jsonContent, OperationPolicySpecification.class);
            } else {
                // This flow will execute if a template specification is not found.
                if (log.isDebugEnabled()) {
                    log.debug("Operation policy template specification not found for the template " + templateName
                            + ". Default template spec used");
                }

                policySpecification = new OperationPolicySpecification();
                policySpecification.setPolicyName(templateName);
                List<String> policyFlow = new ArrayList<String>(Arrays.asList(flow.split(",")));
                policySpecification.setFlow(policyFlow);
            }

            if (templateDefinitionFileInputStream != null) {
                templateDefinition = readInputStream(templateDefinitionFileInputStream, templateDefinitionFileDetail);
            }

            OperationPolicyDataHolder operationPolicyDataHolder = new OperationPolicyDataHolder();
            operationPolicyDataHolder.setSpecification(policySpecification);
            operationPolicyDataHolder.setDefinition(templateDefinition);
            operationPolicyDataHolder.setName(templateName);
            operationPolicyDataHolder.setFlow(flow);
            apiProvider.addOperationalPolicyTemplate(operationPolicyDataHolder, organization);

            if (log.isDebugEnabled()) {
                log.debug("Operation policy template has been added with name " + templateName);
            }

            if (operationPolicyDataHolder != null) {
                String uriString = RestApiConstants.RESOURCE_PATH_API_MEDIATION
                        .replace(RestApiConstants.APIID_PARAM, "111") + "/" + "operational-policy";
                URI uri = new URI(uriString);
                OperationPolicyDefinitionDTO createdPolicy = new OperationPolicyDefinitionDTO();
                createdPolicy.setName(operationPolicyDataHolder.getName());
                return Response.created(uri).entity(createdPolicy).build();
            }

        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, e, log);
            } else {
                throw e;
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while adding operational policy template",
                    e, log);
        }
        return null;
    }

    public String readInputStream(InputStream fileInputStream, Attachment fileDetail) throws IOException {

        String content = null;
        if (fileInputStream != null) {
            String fileName = fileDetail.getDataHandler().getName();

            String fileContentType = URLConnection.guessContentTypeFromName(fileName);

            if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
                fileContentType = fileDetail.getContentType().toString();
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(fileInputStream, outputStream);
            byte[] sequenceBytes = outputStream.toByteArray();
            InputStream inSequenceStream = new ByteArrayInputStream(sequenceBytes);
            content = IOUtils.toString(inSequenceStream, StandardCharsets.UTF_8.name());
        }
        return content;
    }

    @Override
    public Response getAllOperationPolicyTemplates(Integer limit, Integer offset, String query,
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
