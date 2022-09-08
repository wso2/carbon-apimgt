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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.model.APISearchResult;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.restapi.publisher.EndpointCertificatesApiServiceImplUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.EndpointCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.CertificateMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.CertificateRestApiUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMetadataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class EndpointCertificatesApiServiceImpl implements EndpointCertificatesApiService {

    private static Log log = LogFactory.getLog(EndpointCertificatesApiServiceImpl.class);

    public Response getEndpointCertificateContentByAlias(String alias, MessageContext messageContext)
            throws APIManagementException {
        String certFileName = alias + ".crt";
        Object certificate = EndpointCertificatesApiServiceImplUtils.getEndpointCertificateContentByAlias(alias);
        if (certificate != null) {
            Response.ResponseBuilder responseBuilder = Response.ok().entity(certificate);
            responseBuilder.header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\""
                    + certFileName + "\"");
            responseBuilder.header(RestApiConstants.HEADER_CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
            return responseBuilder.build();
        }
        return null;
    }

    public Response deleteEndpointCertificateByAlias(String alias, MessageContext messageContext)
            throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);

        int responseCode = EndpointCertificatesApiServiceImplUtils.deleteEndpointCertificateByAlias(alias);

        if (responseCode == ResponseCode.SUCCESS.getResponseCode()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("The certificate which belongs to tenant : %d represented by the alias : " +
                        "%s is deleted successfully", tenantId, alias));
            }
            return Response.ok().build();
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Failed to delete the certificate which belongs to tenant : %d " +
                        "represented by the alias : %s.", tenantId, alias));
            }
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.DELETE_CERT, alias));
        }
    }

    public Response getEndpointCertificateByAlias(String alias, MessageContext messageContext)
            throws APIManagementException {

        CertificateInformationDTO certificateInformationDTO = EndpointCertificatesApiServiceImplUtils
                .getEndpointCertificateByAlias(alias);
        CertificateInfoDTO certificateInfoDTO =
                CertificateMappingUtil.fromCertificateInformationToDTO(certificateInformationDTO);
        return Response.ok().entity(certificateInfoDTO).build();
    }

    public Response updateEndpointCertificateByAlias(String alias, InputStream certificateInputStream,
                                                     Attachment certificateDetail, MessageContext messageContext)
            throws APIManagementException {
        try {
            ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
            String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String userName = RestApiCommonUtil.getLoggedInUsername();
            int tenantId = APIUtil.getTenantId(userName);

            String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
            int responseCode = EndpointCertificatesApiServiceImplUtils
                    .updateEndpointCertificateByAlias(alias, fileName, base64EncodedCert);
            List<CertificateMetadataDTO> updatedCertificate = apiProvider.searchCertificates(tenantId, alias, null);

            if (ResponseCode.SUCCESS.getResponseCode() == responseCode && !updatedCertificate.isEmpty()) {
                CertificateMetadataDTO certificateMetadata = updatedCertificate.get(0);
                CertMetadataDTO certificateDTO = CertificateMappingUtil.fromCertificateMetadataToDTO(certificateMetadata);

                URI updatedCertUri = new URI(RestApiConstants.CERTS_BASE_PATH + "?alias=" + alias);

                return Response.ok(updatedCertUri).entity(certificateDTO).build();
            }
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while generating the resource location URI for alias " + alias,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

    @Override
    public Response getCertificateUsageByAlias(String alias, Integer limit, Integer offset, MessageContext messageContext)
            throws APIManagementException {

        limit = (limit != null) ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = (offset != null) ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        APIMetadataListDTO apiMetadataListDTO;
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APISearchResult searchResult = EndpointCertificatesApiServiceImplUtils
                .getCertificateUsageByAlias(alias, organization, offset, limit);

        apiMetadataListDTO = APIMappingUtil.fromAPIListToAPIMetadataListDTO(searchResult.getApis());
        APIMappingUtil.setPaginationParamsForAPIMetadataListDTO(apiMetadataListDTO, alias, offset, limit, searchResult.getApiCount());

        return Response.status(Response.Status.OK).entity(apiMetadataListDTO).build();
    }

    public Response getEndpointCertificates(Integer limit, Integer offset, String alias, String endpoint,
                                            MessageContext messageContext) throws APIManagementException {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        List<CertificateMetadataDTO> certificates;
        String query = CertificateRestApiUtils.buildQueryString("alias", alias, "endpoint", endpoint);

        certificates = EndpointCertificatesApiServiceImplUtils.getEndpointCertificates(alias, endpoint);

        CertificatesDTO certificatesDTO = CertificateRestApiUtils.getPaginatedCertificates(certificates, limit,
                offset, query);
        return Response.status(Response.Status.OK).entity(certificatesDTO).build();
    }

    public Response addEndpointCertificate(InputStream certificateInputStream, Attachment certificateDetail,
                                           String alias, String endpoint, MessageContext messageContext)
            throws APIManagementException {
        try {
            ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
            String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);

            String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
            int responseCode = EndpointCertificatesApiServiceImplUtils
                    .addEndpointCertificate(base64EncodedCert, alias, endpoint, fileName);

            if (ResponseCode.SUCCESS.getResponseCode() == responseCode) {
                CertMetadataDTO certificateDTO = new CertMetadataDTO();
                certificateDTO.setEndpoint(endpoint);
                certificateDTO.setAlias(alias);

                URI createdCertUri = new URI(RestApiConstants.CERTS_BASE_PATH + "?alias=" + alias);
                return Response.created(createdCertUri).entity(certificateDTO).build();
            }
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while generating the resource location URI for alias " + alias,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }
}
