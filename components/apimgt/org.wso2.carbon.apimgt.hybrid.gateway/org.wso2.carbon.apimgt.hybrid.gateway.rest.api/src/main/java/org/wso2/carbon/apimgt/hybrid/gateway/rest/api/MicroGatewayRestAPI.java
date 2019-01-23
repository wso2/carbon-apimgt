/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.hybrid.gateway.rest.api;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dao.OnPremiseGatewayDAO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.utils.UploadServiceConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.exceptions.AuthenticationException;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.utils.AuthDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.utils.AuthenticatorUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.utils.UploadServiceUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dao.UploadedUsageFileInfoDAO;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dto.UploadedFileInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsagePublisherException;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * This class provides JAX-RS services for uploading API usage data and publishing api lifecycle events.
 */
public class MicroGatewayRestAPI {
    private static final Log log = LogFactory.getLog(MicroGatewayRestAPI.class);

    /**
     * This is the service which is used to upload the usage data file.
     *
     * @param uploadedInputStream uploadedInputStream input stream from the REST request
     * @param httpHeaders         HTTP headers for the authentication mechanism
     * @return response for the Upload process
     */
    @POST
    @Path("/usage/upload-file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@Multipart("file") InputStream uploadedInputStream, @Context HttpHeaders httpHeaders) {

        String uploadedFileName = null;
        String tenantDomain = null;
        String tenantAwareUsername = null;
        try {
            AuthDTO authDTO = AuthenticatorUtil.authorizeUser(httpHeaders);

            //Process continues only if the user is authorized
            if (authDTO.isAuthenticated()) {

                tenantDomain = authDTO.getTenantDomain();
                tenantAwareUsername = authDTO.getUsername();

                List<String> fileNameHeader = httpHeaders.getRequestHeader(UploadServiceConstants.FILE_NAME_HEADER);

                //If no File Name, stop the process
                if (fileNameHeader == null || fileNameHeader.isEmpty()) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("FileName Header is missing.\n").build();
                }
                uploadedFileName = fileNameHeader.get(0);

                if (!uploadedFileName.matches(UploadServiceConstants.FILE_NAME_REGEX)) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("FileName Header is in incorrect format.\n").build();
                }
                //Add the uploaded file info into the database
                long timeStamp = Long.parseLong(uploadedFileName.split("\\.")[2]);
                UploadedFileInfoDTO dto = new UploadedFileInfoDTO(tenantDomain, uploadedFileName, timeStamp);
                UploadedUsageFileInfoDAO.persistFileUpload(dto, uploadedInputStream);
                log.info("Successfully uploaded the API Usage file [" + uploadedFileName + "] for Tenant : "
                        + tenantDomain + " By : " + tenantAwareUsername);
                return Response.status(Response.Status.CREATED).entity("File uploaded successfully.\n").build();
            } else {
                log.warn("Un Authorized access for API Usage Upload Service. " + authDTO.getMessage());
                return UploadServiceUtil.getJsonResponse(authDTO.getResponseStatus(), authDTO.getMessage(),
                        authDTO.getDescription());
            }
        } catch (UsagePublisherException | AuthenticationException e) {
            String msg = "Error occurred while uploading API Usage file : " + uploadedFileName
                    + " for tenant : " + tenantDomain;
            log.error(msg, e);
            return Response.serverError().entity("Error occurred while uploading usage file. "
                    + e.getMessage()).build();
        }
    }

    /**
     * This is the service which is used to retrieve identifiers of updated APIs.
     *
     * @param httpHeaders         HTTP headers for the authentication mechanism
     * @return response for the Upload process
     */
    @GET
    @Path("/updated-apis")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUpdatedApis(@Context HttpHeaders httpHeaders) {
        String tenantAwareUsername;
        try {
            AuthDTO authDTO = AuthenticatorUtil.authorizeUser(httpHeaders);
            //Process continues only if the user is authorized
            if (authDTO.isAuthenticated()) {
                OnPremiseGatewayDAO onPremiseGatewayDAO = OnPremiseGatewayDAO.getInstance();
                tenantAwareUsername = authDTO.getUsername();
                String tenantDomain = authDTO.getTenantDomain();
                JSONArray apiIds = onPremiseGatewayDAO.getAPIPublishEvents(tenantDomain);
                JSONObject obj = new JSONObject();
                obj.put("API_IDs", apiIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully retrieved identifiers of updated APIs for Tenant : "
                            + tenantDomain + " By : " + tenantAwareUsername);
                }
                return Response.ok().entity(obj.toString()).build();
            } else {
                log.warn("Unauthorized access for API publish/re-publish event publishing service. "
                        + authDTO.getMessage());
                JsonObject responseObj = new JsonObject();
                responseObj.addProperty(UploadServiceConstants.MESSAGE, authDTO.getMessage());
                responseObj.addProperty(UploadServiceConstants.DESCRIPTION, authDTO.getDescription());
                return Response.status(authDTO.getResponseStatus())
                        .entity(responseObj.toString())
                        .build();
            }
        } catch (AuthenticationException | OnPremiseGatewayException e) {
            String msg = "Error occurred while retrieving identifiers of updated APIs";
            log.error(msg, e);
            return Response.serverError().entity(msg + e.getMessage()).build();
        }
    }

}
