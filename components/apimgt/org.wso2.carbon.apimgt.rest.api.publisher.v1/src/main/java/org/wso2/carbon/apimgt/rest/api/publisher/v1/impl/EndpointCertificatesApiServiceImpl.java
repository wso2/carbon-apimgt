/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.EndpointCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl.EndpointCertificatesApiCommonImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMetadataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class EndpointCertificatesApiServiceImpl implements EndpointCertificatesApiService {

    @Override
    public Response getEndpointCertificateContentByAlias(String alias, MessageContext messageContext)
            throws APIManagementException {
        String certFileName = alias + ".crt";
        Object certificate = EndpointCertificatesApiCommonImpl.getEndpointCertificateContentByAlias(alias);
        if (certificate != null) {
            Response.ResponseBuilder responseBuilder = Response.ok().entity(certificate);
            responseBuilder.header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\""
                    + certFileName + "\"");
            responseBuilder.header(RestApiConstants.HEADER_CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
            return responseBuilder.build();
        }
        return null;
    }

    @Override
    public Response deleteEndpointCertificateByAlias(String alias, MessageContext messageContext)
            throws APIManagementException {

        EndpointCertificatesApiCommonImpl.deleteEndpointCertificateByAlias(alias);
        return Response.ok().build();
    }

    @Override
    public Response getEndpointCertificateByAlias(String alias, MessageContext messageContext)
            throws APIManagementException {

        CertificateInfoDTO certificateInfoDTO = EndpointCertificatesApiCommonImpl.getEndpointCertificateByAlias(alias);
        return Response.ok().entity(certificateInfoDTO).build();
    }

    public Response updateEndpointCertificateByAlias(String alias, InputStream certificateInputStream,
                                                     Attachment certificateDetail, MessageContext messageContext)
            throws APIManagementException {

        ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
        String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
        CertMetadataDTO certificateDTO = EndpointCertificatesApiCommonImpl.
                updateEndpointCertificateByAlias(alias, fileName, certificateInputStream);
        URI updatedCertUri;
        try {
            updatedCertUri = new URI(RestApiConstants.CERTS_BASE_PATH + "?alias=" + alias);
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while generating the resource location URI for alias " + alias,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return Response.ok(updatedCertUri).entity(certificateDTO).build();
    }

    @Override
    public Response getCertificateUsageByAlias(String alias, Integer limit, Integer offset, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        APIMetadataListDTO apiMetadataListDTO = EndpointCertificatesApiCommonImpl.getCertificateUsageByAlias(alias,
                limit, offset, organization);

        return Response.status(Response.Status.OK).entity(apiMetadataListDTO).build();
    }

    public Response getEndpointCertificates(Integer limit, Integer offset, String alias, String endpoint,
                                            MessageContext messageContext) throws APIManagementException {

        CertificatesDTO certificatesDTO = EndpointCertificatesApiCommonImpl.getEndpointCertificates(limit, offset,
                alias, endpoint);
        return Response.status(Response.Status.OK).entity(certificatesDTO).build();
    }

    public Response addEndpointCertificate(InputStream certificateInputStream, Attachment certificateDetail,
                                           String alias, String endpoint, MessageContext messageContext)
            throws APIManagementException {

        ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
        String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
        CertMetadataDTO certificateDTO = EndpointCertificatesApiCommonImpl.addEndpointCertificate(certificateInputStream,
                alias, endpoint, fileName);
        URI createdCertUri;
        try {
            createdCertUri = new URI(RestApiConstants.CERTS_BASE_PATH + "?alias=" + alias);
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while generating the resource location URI for alias " + alias,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return Response.created(createdCertUri).entity(certificateDTO).build();
    }
}
