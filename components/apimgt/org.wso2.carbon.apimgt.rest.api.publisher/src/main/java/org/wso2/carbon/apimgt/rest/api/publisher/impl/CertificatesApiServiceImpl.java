package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.CertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListPaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.CertificateRestApiUtils;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.Response;

public class CertificatesApiServiceImpl extends CertificatesApiService {

    private static final Log log = LogFactory.getLog(CertificatesApiServiceImpl.class);

    @Override
    public Response certificatesAliasDelete(String alias) {

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
                return Response.ok().entity("The certificate for alias '" + alias + "' deleted successfully.").build();
            } else {
                RestApiUtil.handleInternalServerError("Error while deleting the certificate for alias '" +
                        alias + "'.", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while deleting the certificate for alias '" +
                    alias + "'.", e, log);
        }
        return null;
    }

    @Override
    public Response certificatesAliasGet(String alias) {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);

        if (!StringUtils.isNotEmpty(alias)) {
            RestApiUtil.handleBadRequest("The alias cannot be empty", log);
        }

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            if (!apiProvider.isCertificatePresent(tenantId, alias)) {
                String message = "Certificate for Alias '" + alias + "' is not found.";
                RestApiUtil.handleResourceNotFoundError(message, log);
            }

            CertificateInformationDTO certificateInformationDTO = apiProvider.getCertificateStatus(alias);
            CertificateInfoDTO certificateInfoDTO = new CertificateInfoDTO();

            certificateInfoDTO.setFrom(certificateInformationDTO.getFrom());
            certificateInfoDTO.setTo(certificateInformationDTO.getTo());
            certificateInfoDTO.setStatus(certificateInformationDTO.getStatus());
            certificateInfoDTO.setSubject(certificateInformationDTO.getSubject());
            certificateInfoDTO.setVersion(certificateInformationDTO.getVersion());

            return Response.ok().entity(certificateInfoDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving the certificate status.", e, log);
        }
        return null;
    }

    @Override
    public Response certificatesAliasPut(InputStream certificateInputStream, Attachment certificateDetail,
                                         String alias) {

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
            RestApiUtil.handleInternalServerError("Error while reading the certificate", log);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Response certificatesGet(Integer limit,Integer offset,String alias,String endpoint){

        limit = limit !=null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        List<CertificateMetadataDTO> certificates;
        String userName = RestApiUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String query = CertificateRestApiUtils.buildQueryString(alias, endpoint);

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            int totalCount = apiProvider.getCertificateCountPerTenant(tenantId);

            if (StringUtils.isNotEmpty(alias) || StringUtils.isNotEmpty(endpoint)) {
                certificates = apiProvider.searchCertificates(tenantId, alias, endpoint);
            } else {
                certificates = apiProvider.getCertificates(userName);
            }

            CertificatesDTO certificatesDTO = CertificateRestApiUtils.getPaginatedCertificates(certificates, limit,
                    offset, query);

            APIListPaginationDTO paginationDTO = new APIListPaginationDTO();
            paginationDTO.setLimit(limit);
            paginationDTO.setOffset(offset);
            paginationDTO.setTotal(totalCount);

            certificatesDTO.setPagination(paginationDTO);

            return Response.status(Response.Status.OK).entity(certificatesDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving the certificates.", e, log);
        }
        return null;
    }

    @Override
    public Response certificatesPost(InputStream certificateInputStream, Attachment certificateDetail, String alias,
                                     String endpoint) {

        try {
            if (StringUtils.isEmpty(alias) || StringUtils.isEmpty(endpoint)) {
                RestApiUtil.handleBadRequest("The alias and/ or endpoint should not be empty", log);
            }

            ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
            String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);

            if (StringUtils.isBlank(fileName)) {
                RestApiUtil.handleBadRequest("Certificate update failed. The Certificate should not be empty", log);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();

            String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
            int responseCode = apiProvider.addCertificate(userName, base64EncodedCert, alias, endpoint);

            if (ResponseCode.SUCCESS.getResponseCode() == responseCode) {
                CertMetadataDTO certificateDTO = new CertMetadataDTO();
                certificateDTO.setEndpoint(endpoint);
                certificateDTO.setAlias(alias);

                URI createdCertUri = new URI(RestApiConstants.CERTS_BASE_PATH + "?alias=" + alias);
                return Response.created(createdCertUri).entity(certificateDTO).build();
            }

            if (ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode() == responseCode) {
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
            RestApiUtil.handleInternalServerError("Error while reading the certificate", log);
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving the resource location of the certificate",
                    log);
        }
        return null;
    }

    @Override
    public Response certificatesAliasContentGet(String alias) {

        return null;
    }


}
