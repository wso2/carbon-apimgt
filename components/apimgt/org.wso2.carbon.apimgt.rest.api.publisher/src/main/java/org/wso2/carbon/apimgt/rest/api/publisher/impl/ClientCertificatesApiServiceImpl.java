/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.ClientCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListPaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ClientCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.CertificateRestApiUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.CertificateMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a concrete implementation ClientCertificatesManagement API.
 */
public class ClientCertificatesApiServiceImpl extends ClientCertificatesApiService {
    private static final Log log = LogFactory.getLog(ClientCertificatesApiServiceImpl.class);
    private CertificateMgtUtils certificateMgtUtils = CertificateMgtUtils.getInstance();

    @Override
    public Response clientCertificatesAliasContentGet(String alias){
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String certFileName = alias + ".crt";
        try {
            ClientCertificateDTO clientCertificateDTO = preValidateClientCertificate(alias);
            if (clientCertificateDTO != null) {
                Object certificate = CertificateRestApiUtils
                        .getDecodedCertificate(clientCertificateDTO.getCertificate());
                Response.ResponseBuilder responseBuilder = Response.ok().entity(certificate);
                responseBuilder.header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                        "attachment; filename=\"" + certFileName + "\"");
                responseBuilder.header(RestApiConstants.HEADER_CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
                return responseBuilder.build();
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while retrieving the client certificate with alias " + alias + " for the tenant "
                            + tenantDomain, e, log);
        }
        return null;
    }

    @Override
    public Response clientCertificatesAliasDelete(String alias) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        try {
            ClientCertificateDTO clientCertificateDTO = preValidateClientCertificate(alias);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            int responseCode = apiProvider
                    .deleteClientCertificate(RestApiUtil.getLoggedInUsername(), clientCertificateDTO.getApiIdentifier(),
                            alias);
            if (responseCode == ResponseCode.SUCCESS.getResponseCode()) {
                API api = apiProvider.getAPI(clientCertificateDTO.getApiIdentifier());
                apiProvider.updateAPI(api);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("The client certificate which belongs to tenant : %s represented by the "
                            + "alias : %s is deleted successfully", tenantDomain, alias));
                }
                return Response.ok().entity("The certificate for alias '" + alias + "' deleted successfully.").build();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Failed to delete the client certificate which belongs to tenant : %s "
                            + "represented by the alias : %s.", tenantDomain, alias));
                }
                RestApiUtil.handleInternalServerError(
                        "Error while deleting the client certificate for alias '" + alias + "'.", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while deleting the client certificate with alias " + alias + " for the tenant "
                            + tenantDomain, e, log);
        } catch (FaultGatewaysException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while publishing the certificate change to gateways for the alias " + alias, e, log);
        }
        return null;
    }

    @Override
    public Response clientCertificatesAliasGet(String alias){
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        try {
            ClientCertificateDTO clientCertificateDTO = preValidateClientCertificate(alias);
            CertificateInformationDTO certificateInformationDTO = certificateMgtUtils
                    .getCertificateInfo(clientCertificateDTO.getCertificate());
            if (certificateInformationDTO != null) {
                CertificateInfoDTO certificateInfoDTO = CertificateMappingUtil
                        .fromCertificateInformationToDTO(certificateInformationDTO);
                return Response.ok().entity(certificateInfoDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError("Certificate is empty for alias " + alias, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while retrieving the client certificate with alias " + alias + " for the tenant "
                            + tenantDomain, e, log);
        }
        return null;
    }

    @Override
    public Response clientCertificatesAliasPut(String alias, InputStream certificateInputStream,
            Attachment certificateDetail, String tier) {
        try {
            ContentDisposition contentDisposition;
            String fileName;
            String base64EncodedCert = null;
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            int tenantId = APIUtil.getTenantId(userName);
            ClientCertificateDTO clientCertificateDTO = preValidateClientCertificate(alias);
            if (certificateDetail != null) {
                contentDisposition = certificateDetail.getContentDisposition();
                fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
                if (StringUtils.isNotBlank(fileName)) {
                    base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
                }
            }
            if (StringUtils.isEmpty(base64EncodedCert) && StringUtils.isEmpty(tier)) {
                return Response.ok().entity("Client Certificate is not updated for alias " + alias).build();
            }
            int responseCode = apiProvider
                    .updateClientCertificate(base64EncodedCert, alias, clientCertificateDTO.getApiIdentifier(), tier,
                            tenantId);

            if (ResponseCode.SUCCESS.getResponseCode() == responseCode) {
                API api = apiProvider.getAPI(clientCertificateDTO.getApiIdentifier());
                apiProvider.updateAPI(api);
                ClientCertMetadataDTO clientCertMetadataDTO = new ClientCertMetadataDTO();
                clientCertMetadataDTO.setAlias(alias);
                clientCertMetadataDTO.setApiId(api.getUUID());
                clientCertMetadataDTO.setTier(clientCertificateDTO.getTierName());
                URI updatedCertUri = new URI(RestApiConstants.CLIENT_CERTS_BASE_PATH + "?alias=" + alias);

                return Response.ok(updatedCertUri).entity(clientCertMetadataDTO).build();
            } else if (ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode() == responseCode) {
                RestApiUtil.handleInternalServerError(
                        "Error while updating the client certificate for the alias " + alias + " due to an internal "
                                + "server error", log);
            } else if (ResponseCode.CERTIFICATE_NOT_FOUND.getResponseCode() == responseCode) {
                RestApiUtil.handleResourceNotFoundError("", log);
            } else if (ResponseCode.CERTIFICATE_EXPIRED.getResponseCode() == responseCode) {
                RestApiUtil.handleBadRequest(
                        "Error while updating the client certificate for the alias " + alias + " Certificate Expired.",
                        log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while updating the client certificate for the alias " + alias + " due to an internal "
                            + "server error", e, log);
        } catch (IOException e) {
            RestApiUtil
                    .handleInternalServerError("Error while encoding client certificate for the alias " + alias, e,
                            log);
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while generating the resource location URI for alias '" + alias + "'", e, log);
        }  catch (FaultGatewaysException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while publishing the certificate change to gateways for the alias " + alias, e, log);
        }
        return null;
    }

    @Override
    public Response clientCertificatesGet(Integer limit,Integer offset,String alias, String apiId){
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        List<ClientCertificateDTO> certificates = new ArrayList<>();
        String userName = RestApiUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String query = CertificateRestApiUtils.buildQueryString("alias", alias, "apiId", apiId);

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            if (!apiProvider.isClientCertificateBasedAuthenticationConfigured()) {
                RestApiUtil.handleBadRequest(
                        "The client certificate based authentication is not configured for this " + "server", log);
            }
            int totalCount = apiProvider.getClientCertificateCount(tenantId);
            if (totalCount > 0) {
                APIIdentifier apiIdentifier = null;
                if (StringUtils.isNotEmpty(apiId)) {
                    API api = apiProvider.getAPIbyUUID(apiId, RestApiUtil.getLoggedInUserTenantDomain());
                    apiIdentifier = api.getId();
                }
                certificates = apiProvider.searchClientCertificates(tenantId, alias, apiIdentifier);
            }

            ClientCertificatesDTO certificatesDTO = CertificateRestApiUtils
                    .getPaginatedClientCertificates(certificates, limit, offset, query);
            APIListPaginationDTO paginationDTO = new APIListPaginationDTO();
            paginationDTO.setLimit(limit);
            paginationDTO.setOffset(offset);
            paginationDTO.setTotal(totalCount);
            certificatesDTO.setPagination(paginationDTO);
            return Response.status(Response.Status.OK).entity(certificatesDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving the client certificates.", e, log);
        }
        return null;
    }

    @Override
    public Response clientCertificatesPost(InputStream certificateInputStream,Attachment certificateDetail,
            String alias,String apiId,String tier){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
            String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
            if (StringUtils.isEmpty(alias) || StringUtils.isEmpty(apiId)) {
                RestApiUtil.handleBadRequest("The alias and/ or apiId should not be empty", log);
            }
            if (StringUtils.isBlank(fileName)) {
                RestApiUtil.handleBadRequest("Certificate addition failed. Proper Certificate file should be provided",
                        log);
            }
            if (!apiProvider.isClientCertificateBasedAuthenticationConfigured()) {
                RestApiUtil.handleBadRequest(
                        "The client certificate based authentication is not configured for this " + "server", log);
            }
            API api = apiProvider.getAPIbyUUID(apiId, RestApiUtil.getLoggedInUserTenantDomain());
            String userName = RestApiUtil.getLoggedInUsername();
            String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
            int responseCode = apiProvider.addClientCertificate(userName, api.getId(), base64EncodedCert, alias, tier);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Add certificate operation response code : %d", responseCode));
            }
            if (ResponseCode.SUCCESS.getResponseCode() == responseCode) {
                apiProvider.updateAPI(api);
                ClientCertMetadataDTO certificateDTO = new ClientCertMetadataDTO();
                certificateDTO.setAlias(alias);
                certificateDTO.setApiId(apiId);
                certificateDTO.setTier(tier);
                URI createdCertUri = new URI(RestApiConstants.CLIENT_CERTS_BASE_PATH + "?alias=" + alias);
                return Response.created(createdCertUri).entity(certificateDTO).build();
            } else if (ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode() == responseCode) {
                RestApiUtil.handleInternalServerError(
                        "Internal server error while adding the client certificate to " + "API " + apiId, log);
            } else if (ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE.getResponseCode() == responseCode) {
                RestApiUtil.handleResourceAlreadyExistsError(
                        "The alias '" + alias + "' already exists in the trust store.", log);
            } else if (ResponseCode.CERTIFICATE_EXPIRED.getResponseCode() == responseCode) {
                RestApiUtil.handleBadRequest(
                        "Error while adding the certificate to the API " + apiId + ". " + "Certificate Expired.", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "APIManagement exception while adding the certificate to the API " + apiId + " due to an internal "
                            + "server error", e, log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError(
                    "IOException while generating the encoded certificate for the API " + apiId, e, log);
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while generating the resource location URI for alias '" + alias + "'", e, log);
        } catch (FaultGatewaysException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while publishing the certificate change to gateways for the alias " + alias, e, log);
        }
        return null;
    }

    /**
     * To pre validate client certificate given for an alias
     *
     * @param alias Alias of the certificate.
     * @return Client certificate
     * @throws APIManagementException API Management Exception.
     */
    private ClientCertificateDTO preValidateClientCertificate(String alias) throws APIManagementException {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        if (StringUtils.isEmpty(alias)) {
            RestApiUtil.handleBadRequest("The alias cannot be empty", log);
        }
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        if (!apiProvider.isClientCertificateBasedAuthenticationConfigured()) {
            RestApiUtil.handleBadRequest(
                    "The client certificate based authentication is not configured for this server", log);
        }
        ClientCertificateDTO clientCertificate = apiProvider.getClientCertificate(tenantId, alias);
        if (clientCertificate == null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Could not find a client certificate in truststore which belongs to "
                        + "tenant : %d and with alias : %s. Hence the operation is terminated.", tenantId, alias));
            }
            String message = "Certificate for alias '" + alias + "' is not found.";
            RestApiUtil.handleResourceNotFoundError(message, log);
        }
        return clientCertificate;
    }

}
