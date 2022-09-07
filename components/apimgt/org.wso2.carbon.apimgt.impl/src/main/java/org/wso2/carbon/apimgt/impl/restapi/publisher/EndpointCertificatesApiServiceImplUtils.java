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

package org.wso2.carbon.apimgt.impl.restapi.publisher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.model.APISearchResult;
import org.wso2.carbon.apimgt.impl.restapi.CommonUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.List;

public class EndpointCertificatesApiServiceImplUtils {

    private EndpointCertificatesApiServiceImplUtils() {
    }

    private static final Log log = LogFactory.getLog(EndpointCertificatesApiServiceImplUtils.class);


    /**
     * @param alias Alias of the certificate
     * @return Certificate
     * @throws APIManagementException when certificate is not found
     */
    public static Object getEndpointCertificateContentByAlias(String alias) throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        int tenantId = APIUtil.getTenantId(CommonUtils.getLoggedInUsername());
        validateCertOperations(alias, tenantId);

        return apiProvider.getCertificateContent(alias);

    }

    /**
     * @param alias Certificate alias
     * @return Certificate management response code
     * @throws APIManagementException when an internal error occurs while deleting a certificate
     */
    public static int deleteEndpointCertificateByAlias(String alias) throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        String username = CommonUtils.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(username);
        validateCertOperations(alias, tenantId);

        return apiProvider.deleteCertificate(username, alias, null);
    }

    /**
     * @param alias Certificate alias
     * @return Certificate information
     * @throws APIManagementException when an internal error occurs while getting a certificate
     */
    public static CertificateInformationDTO getEndpointCertificateByAlias(String alias) throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        int tenantId = APIUtil.getTenantId(CommonUtils.getLoggedInUsername());
        validateCertOperations(alias, tenantId);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Retrieving the common information of the certificate which is represented by the" +
                    " alias : %s", alias));
        }
        return apiProvider.getCertificateStatus(alias);
    }

    /**
     * @param alias             Certificate alias
     * @param fileName          Certificate file name
     * @param base64EncodedCert base64 encoded certificate
     * @return Certificate management response code
     * @throws APIManagementException when an internal error occurs while updating a certificate
     */
    public static int updateEndpointCertificateByAlias(String alias, String fileName,
                                                       String base64EncodedCert)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        int tenantId = APIUtil.getTenantId(CommonUtils.getLoggedInUsername());
        validateCertOperations(alias, tenantId, fileName);
        return apiProvider.updateCertificate(base64EncodedCert, alias);
    }

    /**
     * @param alias        Certificate alias
     * @param organization Organization the user belong to
     * @param offset       Query offset
     * @param limit        Query limit
     * @return API search result
     * @throws APIManagementException when an internal error occurs while getting a certificate
     */
    public static APISearchResult getCertificateUsageByAlias(String alias, String organization, Integer offset, Integer limit)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        CertificateMetadataDTO certificateMetadataDTO = apiProvider.getCertificate(alias);
        APISearchResult searchResult;

        if (certificateMetadataDTO != null) {
            String endpoint = certificateMetadataDTO.getEndpoint();
            searchResult = apiProvider.searchPaginatedAPIsByFQDN(endpoint, organization, offset, limit);
        } else {
            searchResult = new APISearchResult();
        }

        return searchResult;
    }

    /**
     * @param alias    Certificate alias
     * @param endpoint Certificate endpoint
     * @return List of certificate metadata
     * @throws APIManagementException when an internal error occurs while getting an endpoint certificate
     */
    public static List<CertificateMetadataDTO> getEndpointCertificates(String alias, String endpoint)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        String username = CommonUtils.getLoggedInUsername();
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
     * @param base64EncodedCert base64 encoded certificate
     * @param alias             Certificate alias
     * @param endpoint          Certificate endpoint
     * @param fileName          Certificate file name
     * @return Certificate management response code
     * @throws APIManagementException when an internal error occurs while adding an endpoint certificate
     */
    public static int addEndpointCertificate(String base64EncodedCert, String alias, String endpoint, String fileName)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        String username = CommonUtils.getLoggedInUsername();
        validateCertOperations(alias, endpoint, fileName);
        int responseCode = apiProvider.addCertificate(username, base64EncodedCert, alias, endpoint);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Add certificate operation response code : %d", responseCode));
        }
        return responseCode;
    }

    /**
     * @param alias    Certificate alias
     * @param tenantId Tenant in which the certificate should be available
     * @throws APIManagementException when certificate is not found or required parameters are missing
     */
    private static void validateCertOperations(String alias, int tenantId) throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
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
    private static void validateCertOperations(String alias, int tenantId, String fileName) throws APIManagementException {
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
    private static void validateCertOperations(String alias, String endpoint, String fileName) throws APIManagementException {
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
