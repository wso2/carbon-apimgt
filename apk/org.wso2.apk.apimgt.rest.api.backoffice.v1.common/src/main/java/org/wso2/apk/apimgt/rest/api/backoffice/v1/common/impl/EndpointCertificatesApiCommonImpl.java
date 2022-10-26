/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.apk.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.apk.apimgt.api.model.APISearchResult;
import org.wso2.apk.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.mappings.CertificateRestApiUtils;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.mappings.APIMappingUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.mappings.CertificateMappingUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIMetadataListDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.CertMetadataDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.CertificateInfoDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.CertificatesDTO;

import java.io.InputStream;
import java.util.List;

/**
 * Utility class for operations related to EndpointCertificatesApiService
 */
public class EndpointCertificatesApiCommonImpl {

    private EndpointCertificatesApiCommonImpl() {

    }

    private static final Log log = LogFactory.getLog(EndpointCertificatesApiCommonImpl.class);

    /**
     * @param alias Alias of the certificate
     * @return Certificate
     * @throws APIManagementException when certificate is not found
     */
    public static Object getEndpointCertificateContentByAlias(String alias) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        int tenantId = APIUtil.getTenantId(RestApiCommonUtil.getLoggedInUsername());
        validateCertOperations(alias, tenantId);

        return apiProvider.getCertificateContent(alias);

    }

    /**
     * @param alias Certificate alias
     * @throws APIManagementException when an internal error occurs while deleting a certificate
     */
    public static void deleteEndpointCertificateByAlias(String alias) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String username = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(username);
        validateCertOperations(alias, tenantId);

        int responseCode = apiProvider.deleteCertificate(username, alias, null);
        if (responseCode != ResponseCode.SUCCESS.getResponseCode()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Failed to delete the certificate which belongs to tenant : %d " +
                        "represented by the alias : %s.", tenantId, alias));
            }
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.DELETE_CERT, alias));
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("The certificate which belongs to tenant : %d represented by the alias : " +
                    "%s is deleted successfully", tenantId, alias));
        }
    }

    /**
     * @param alias Certificate alias
     * @return Certificate information
     * @throws APIManagementException when an internal error occurs while getting a certificate
     */
    public static CertificateInfoDTO getEndpointCertificateByAlias(String alias) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        int tenantId = APIUtil.getTenantId(RestApiCommonUtil.getLoggedInUsername());
        validateCertOperations(alias, tenantId);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Retrieving the common information of the certificate which is represented by the" +
                    " alias : %s", alias));
        }
        CertificateInformationDTO certificateStatus = apiProvider.getCertificateStatus(alias);
        return CertificateMappingUtil.fromCertificateInformationToDTO(certificateStatus);
    }

    public static CertMetadataDTO updateEndpointCertificateByAlias(String alias, String fileName,
                                                                   InputStream certInputStream)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);

        String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certInputStream);
        validateCertOperations(alias, tenantId, fileName);

        int responseCode = apiProvider.updateCertificate(base64EncodedCert, alias);
        List<CertificateMetadataDTO> updatedCertificate = apiProvider.searchCertificates(tenantId, alias, null);

        if (ResponseCode.SUCCESS.getResponseCode() == responseCode && !updatedCertificate.isEmpty()) {
            CertificateMetadataDTO certificateMetadata = updatedCertificate.get(0);
            return CertificateMappingUtil.fromCertificateMetadataToDTO(certificateMetadata);
        }
        throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                "Endpoint Certificate Update Failed"));
    }

    /**
     * @param alias        Certificate alias
     * @param limit        Query limit
     * @param offset       Query offset
     * @param organization Organization the user belong to
     * @return API Meta Data List
     * @throws APIManagementException when an internal error occurs while getting a certificate
     */
    public static APIMetadataListDTO getCertificateUsageByAlias(String alias, Integer limit, Integer offset,
                                                                String organization) throws APIManagementException {

        limit = (limit != null) ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = (offset != null) ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        CertificateMetadataDTO certificateMetadataDTO = apiProvider.getCertificate(alias);
        APISearchResult searchResult;

        if (certificateMetadataDTO != null) {
            String endpoint = certificateMetadataDTO.getEndpoint();
            searchResult = apiProvider.searchPaginatedAPIsByFQDN(endpoint, organization, offset, limit);
        } else {
            searchResult = new APISearchResult();
        }

        APIMetadataListDTO apiMetadataListDTO = APIMappingUtil.fromAPIListToAPIMetadataListDTO(searchResult.getApis());
        APIMappingUtil.setPaginationParamsForAPIMetadataListDTO(apiMetadataListDTO, alias, offset, limit,
                searchResult.getApiCount());
        return apiMetadataListDTO;
    }

    public static CertificatesDTO getEndpointCertificates(Integer limit, Integer offset, String alias, String endpoint)
            throws APIManagementException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String query = CertificateRestApiUtils.buildQueryString("alias", alias, "endpoint",
                endpoint);
        List<CertificateMetadataDTO> certificates = getEndpointCertificates(alias, endpoint);
        return CertificateRestApiUtils.getPaginatedCertificates(certificates, limit, offset, query);
    }

    /**
     * @param alias    Certificate alias
     * @param endpoint Certificate endpoint
     * @return List of certificate metadata
     * @throws APIManagementException when an internal error occurs while getting an endpoint certificate
     */
    private static List<CertificateMetadataDTO> getEndpointCertificates(String alias, String endpoint)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String username = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(username);
        List<CertificateMetadataDTO> certificates;
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
            certificates = apiProvider.getCertificates(username);
        }
        return certificates;
    }

    /**
     * @param certificateInputStream Certificate InputStream
     * @param alias                  Certificate alias
     * @param endpoint               Certificate endpoint
     * @param fileName               Certificate file name
     * @return Certificate management response code
     * @throws APIManagementException when an internal error occurs while adding an endpoint certificate
     */
    public static CertMetadataDTO addEndpointCertificate(InputStream certificateInputStream, String alias,
                                                         String endpoint, String fileName)
            throws APIManagementException {

        String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String username = RestApiCommonUtil.getLoggedInUsername();
        validateCertOperations(alias, endpoint, fileName);
        int responseCode = apiProvider.addCertificate(username, base64EncodedCert, alias, endpoint);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Add certificate operation response code : %d", responseCode));
        }

        if (ResponseCode.SUCCESS.getResponseCode() == responseCode) {
            CertMetadataDTO certificateDTO = new CertMetadataDTO();
            certificateDTO.setEndpoint(endpoint);
            certificateDTO.setAlias(alias);
            return certificateDTO;
        }

        throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                "Endpoint Certificate Add Failed"));
    }

    /**
     * @param alias    Certificate alias
     * @param tenantId Tenant in which the certificate should be available
     * @throws APIManagementException when certificate is not found or required parameters are missing
     */
    private static void validateCertOperations(String alias, int tenantId) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (StringUtils.isEmpty(alias)) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.CERT_BAD_REQUEST, "The alias cannot be empty"));
        }

        if (!apiProvider.isCertificatePresent(tenantId, alias)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Could not find a certificate in truststore which belongs to tenant : %d " +
                        "and with alias : %s. Hence the operation is terminated.", tenantId, alias));
            }
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.CERT_NOT_FOUND, alias));
        }
    }

    /**
     * @param alias    Certificate alias
     * @param tenantId Tenant in which the certificate should be available
     * @param fileName Certificate file name
     * @throws APIManagementException when certificate is not found or required parameters are missing
     */
    private static void validateCertOperations(String alias, int tenantId, String fileName)
            throws APIManagementException {

        validateCertOperations(alias, tenantId);
        if (StringUtils.isBlank(fileName)) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.CERT_BAD_REQUEST, "The Certificate should not be empty"));
        }
    }

    /**
     * @param alias    Certificate alias
     * @param endpoint Certificate endpoint
     * @param fileName Certificate file name
     * @throws APIManagementException when required parameters are missing
     */
    private static void validateCertOperations(String alias, String endpoint, String fileName)
            throws APIManagementException {

        if (StringUtils.isEmpty(alias)) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.CERT_BAD_REQUEST, "The alias cannot be empty"));
        }

        if (StringUtils.isEmpty(endpoint)) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.CERT_BAD_REQUEST, "The endpoint cannot be empty"));
        }

        if (StringUtils.isBlank(fileName)) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.CERT_BAD_REQUEST, "The Certificate should not be empty"));
        }
    }
}
