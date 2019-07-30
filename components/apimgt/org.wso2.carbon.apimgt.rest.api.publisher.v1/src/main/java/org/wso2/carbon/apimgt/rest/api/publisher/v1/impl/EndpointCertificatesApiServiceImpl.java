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
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.EndpointCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateValidityDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.CertificateRestApiUtils;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class EndpointCertificatesApiServiceImpl implements EndpointCertificatesApiService {

    private static Log log = LogFactory.getLog(EndpointCertificatesApiServiceImpl.class);
    public Response endpointCertificatesAliasContentGet(String alias, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        String certFileName = alias + ".crt";

        if (!StringUtils.isNotEmpty(alias)) {
            RestApiUtil.handleBadRequest("The alias cannot be empty", log);
        }

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            if (!apiProvider.isCertificatePresent(tenantId, alias)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Could not find a certificate in truststore which belongs to tenant : %d " +
                            "and with alias : %s. Hence the operation is terminated.", tenantId, alias));
                }
                String message = "Certificate for Alias '" + alias + "' is not found.";
                RestApiUtil.handleResourceNotFoundError(message, log);
            }

            Object certificate = apiProvider.getCertificateContent(alias);
            if (certificate != null) {
                Response.ResponseBuilder responseBuilder = Response.ok().entity(certificate);
                responseBuilder.header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\""
                        + certFileName + "\"");
                responseBuilder.header(RestApiConstants.HEADER_CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
                return responseBuilder.build();
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving the certificate status.", e, log);
        }
        return null;
    }

    public Response endpointCertificatesAliasDelete(String alias, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        String userName = RestApiUtil.getLoggedInUsername();

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            if (!apiProvider.isCertificatePresent(tenantId, alias)) {
                String message = "Certificate for alias '" + alias + "' is not found.";
                RestApiUtil.handleResourceNotFoundError(message, log);
            }

            int responseCode = apiProvider.deleteCertificate(userName, alias, null);

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
                RestApiUtil.handleInternalServerError("Error while deleting the certificate for alias '" +
                        alias + "'.", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while deleting the certificate for alias '" +
                    alias + "'.", e, log);
        }
        return null;
    }

    public Response endpointCertificatesAliasGet(String alias, MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);

        if (!StringUtils.isNotEmpty(alias)) {
            RestApiUtil.handleBadRequest("The alias cannot be empty", log);
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Retrieving the common information of the certificate which is represented by the" +
                    " alias : %s", alias));
        }

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            if (!apiProvider.isCertificatePresent(tenantId, alias)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Could not find a certificate in truststore which belongs to tenant %d " +
                            "and with alias %s. Hence the operation is terminated.", tenantId, alias));
                }
                String message = "Certificate for Alias '" + alias + "' is not found.";
                RestApiUtil.handleResourceNotFoundError(message, log);
            }

            CertificateInformationDTO certificateInformationDTO = apiProvider.getCertificateStatus(alias);

            CertificateValidityDTO certificateValidityDTO = new CertificateValidityDTO();
            certificateValidityDTO.setFrom(certificateInformationDTO.getFrom());
            certificateValidityDTO.setTo(certificateInformationDTO.getTo());

            CertificateInfoDTO certificateInfoDTO = new CertificateInfoDTO();
            certificateInfoDTO.setValidity(certificateValidityDTO);
            certificateInfoDTO.setStatus(certificateInformationDTO.getStatus());
            certificateInfoDTO.setSubject(certificateInformationDTO.getSubject());
            certificateInfoDTO.setVersion(certificateInformationDTO.getVersion());

            return Response.ok().entity(certificateInfoDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving the certificate status.", e, log);
        }
        return null;
    }

    public Response endpointCertificatesAliasPut(InputStream certificateInputStream, Attachment certificateDetail,
            String alias, MessageContext messageContext) {
        try {
            if (StringUtils.isEmpty(alias)) {
                RestApiUtil.handleBadRequest("The alias should not be empty", log);
            }

            ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
            String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);

            if (StringUtils.isBlank(fileName)) {
                RestApiUtil.handleBadRequest("Certificate update failed. The Certificate should not be empty", log);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            int tenantId = APIUtil.getTenantId(userName);

            if (!apiProvider.isCertificatePresent(tenantId, alias)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Could not find a certificate in truststore which belongs to tenant : %d " +
                            "and with alias : %s. Hence the operation is terminated.", tenantId, alias));
                }
                RestApiUtil.handleResourceNotFoundError("Could not update the certificate. " +
                        "The alias '" + alias + "' not found.", log);
            }

            String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
            int responseCode = apiProvider.updateCertificate(base64EncodedCert, alias);
            List<CertificateMetadataDTO> updatedCertificate = apiProvider.searchCertificates(tenantId, alias, null);

            if (ResponseCode.SUCCESS.getResponseCode() == responseCode && updatedCertificate.size() > 0) {

                CertificateMetadataDTO certificateMetadata = updatedCertificate.get(0);

                CertMetadataDTO certificateDTO = new CertMetadataDTO();
                certificateDTO.setAlias(certificateMetadata.getAlias());
                certificateDTO.setEndpoint(certificateMetadata.getEndpoint());

                URI updatedCertUri = new URI(RestApiConstants.CERTS_BASE_PATH + "?alias=" + alias);

                return Response.ok(updatedCertUri).entity(certificateDTO).build();
            }

            if (ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode() == responseCode) {
                RestApiUtil.handleInternalServerError("Error while updating the certificate due to an internal " +
                        "server error", log);
            } else if (ResponseCode.CERTIFICATE_NOT_FOUND.getResponseCode() == responseCode) {
                RestApiUtil.handleResourceNotFoundError("", log);
            } else if (ResponseCode.CERTIFICATE_EXPIRED.getResponseCode() == responseCode) {
                RestApiUtil.handleBadRequest("Error while updating the certificate. Certificate Expired.", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while adding the certificate due to an internal server " +
                    "error", log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error while encoding certificate", log);
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError("Error while generating the resource location URI for alias '" +
                    alias + "'", log);
        }
        return null;
    }

    public Response endpointCertificatesGet(Integer limit, Integer offset, String alias, String endpoint,
            MessageContext messageContext) {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        List<CertificateMetadataDTO> certificates;
        String userName = RestApiUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String query = CertificateRestApiUtils.buildQueryString("alias", alias, "endpoint", endpoint);

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            if (StringUtils.isNotEmpty(alias) || StringUtils.isNotEmpty(endpoint)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Call the search certificate api to get the filtered certificates for " +
                            "tenant id : %d, alias : %s, and endpoint : %s", tenantId, alias, endpoint));
                }
                certificates = apiProvider.searchCertificates(tenantId, alias, endpoint);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("There is no query parameters provided. So, retrieve all the certificates" +
                            " belongs to the tenantId : %d", tenantId));
                }
                certificates = apiProvider.getCertificates(userName);
            }

            CertificatesDTO certificatesDTO = CertificateRestApiUtils.getPaginatedCertificates(certificates, limit,
                    offset, query);
            return Response.status(Response.Status.OK).entity(certificatesDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving the certificates.", e, log);
        }
        return null;
    }

    public Response endpointCertificatesPost(InputStream certificateInputStream, Attachment certificateDetail,
            String alias, String endpoint, MessageContext messageContext) {
        try {
            if (StringUtils.isEmpty(alias) || StringUtils.isEmpty(endpoint)) {
                RestApiUtil.handleBadRequest("The alias and/ or endpoint should not be empty", log);
            }

            ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
            String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);

            if (StringUtils.isBlank(fileName)) {
                RestApiUtil.handleBadRequest("Certificate update failed. Proper Certificate file should be provided",
                        log);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
            int responseCode = apiProvider.addCertificate(userName, base64EncodedCert, alias, endpoint);

            if (log.isDebugEnabled()) {
                log.debug(String.format("Add certificate operation response code : %d", responseCode));
            }

            if (ResponseCode.SUCCESS.getResponseCode() == responseCode) {
                CertMetadataDTO certificateDTO = new CertMetadataDTO();
                certificateDTO.setEndpoint(endpoint);
                certificateDTO.setAlias(alias);

                URI createdCertUri = new URI(RestApiConstants.CERTS_BASE_PATH + "?alias=" + alias);
                return Response.created(createdCertUri).entity(certificateDTO).build();
            } else if (ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode() == responseCode) {
                RestApiUtil.handleInternalServerError("Error while adding the certificate due to an" +
                        " internal server error", log);
            } else if (ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE.getResponseCode() == responseCode) {
                RestApiUtil.handleResourceAlreadyExistsError("The alias '" + alias +
                        "' already exists in the trust store.", log);
            } else if (ResponseCode.CERTIFICATE_EXPIRED.getResponseCode() == responseCode) {
                RestApiUtil.handleBadRequest("Error while adding the certificate. Certificate Expired.", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while adding the certificate due to an internal server " +
                    "error", log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error while generating the encoded certificate", log);
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError("Error while generating the resource location URI for alias '" +
                    alias + "'", log);
        }
        return null;
    }
}
