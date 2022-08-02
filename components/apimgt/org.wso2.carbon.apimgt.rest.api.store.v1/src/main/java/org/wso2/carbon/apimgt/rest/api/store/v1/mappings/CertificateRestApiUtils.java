/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import com.nimbusds.jose.util.StandardCharset;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ClientCertificatesDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CertificateRestApiUtils {

    private static final Log log = LogFactory.getLog(CertificateRestApiUtils.class);

    /**
     * Generates the query string from the provided params.
     *
     * @param firstParamName   : Name of the first param.
     * @param firstParamValue  : Value of the first param.
     * @param secondParamName  : Name of the second param.
     * @param secondParamValue : Value of the second param.
     * @return : The generated query string.
     */
    public static String buildQueryString(String firstParamName, String firstParamValue, String secondParamName,
            String secondParamValue) {
        String query = "";
        if (StringUtils.isNotBlank(firstParamValue)) {
            query = query + "&" + firstParamName + "=" + firstParamValue;
        }

        if (StringUtils.isNotBlank(secondParamValue)) {
            query = query + "&" + secondParamName + "=" + secondParamValue;
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("The query string for the application : %s", query));
        }
        return query;
    }

    /**
     * Generates a base64 encoded string of the certificate that is being uploaded.
     *
     * @param certificateInputStream : The input stream for the certificate.
     * @return : Base64 encoded certificate string.
     * @throws IOException :
     */
    public static String generateEncodedCertificate(InputStream certificateInputStream) throws IOException {

        byte[] certificateBytes = IOUtils.toByteArray(certificateInputStream);
        byte[] encodedCert = Base64.encodeBase64(certificateBytes);
        return new String(encodedCert, StandardCharset.UTF_8);
    }

    /**
     * To get the decoded certificate input stream.
     *
     * @param certificate Relevant encoded certificate
     * @return Input stream of the certificate.
     * @throws APIManagementException API Management Exception.
     */
    public static ByteArrayInputStream getDecodedCertificate(String certificate) throws APIManagementException {
        byte[] cert = (Base64.decodeBase64(certificate.getBytes(StandardCharsets.UTF_8)));
        ByteArrayInputStream serverCert = new ByteArrayInputStream(cert);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            if (serverCert.available() > 0) {
                Certificate generatedCertificate = cf.generateCertificate(serverCert);
                X509Certificate x509Certificate = (X509Certificate) generatedCertificate;
                return new ByteArrayInputStream(x509Certificate.getEncoded());
            }
        } catch (CertificateException e) {
            throw new APIManagementException("Error while decoding the certificate", e);
        }
        return null;
    }

    /**
     * Get the paginated certificate urls.
     *
     * @param offset : The offset
     * @param limit  : The limit parameter.
     * @param query  : The provided query string
     * @return : Certificates paginated URL
     */
    private static String getCertificatesPaginatedURL(String paginatedURL, Integer offset, Integer limit,
            String query) {
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.QUERY_PARAM, query);
        return paginatedURL;
    }

    /**
     * To get the paginated list of client certificates.
     *
     * @param clientCertificateDTOList Client certificate list.
     * @param limit                    Limit
     * @param offset                   Offset
     * @param query                    query
     * @return paginated list of client certificates.
     */
    public static ClientCertificatesDTO getPaginatedClientCertificates(
            List<ClientCertificateDTO> clientCertificateDTOList, int limit, int offset, String query,
            String applicationId) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "Filter the client certificates based on the pagination parameters, limit = %d and" + "offset = %d",
                    limit, offset));
        }

        int certCount = clientCertificateDTOList.size();
        List<ClientCertMetadataDTO> clientCertificateList = new ArrayList<>();
        ClientCertificatesDTO certificatesDTO = new ClientCertificatesDTO();
        certificatesDTO.setCount(certCount > limit ? limit : certCount);

        // If the provided offset value exceeds the offset, reset the offset to default.
        if (offset > certCount) {
            offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        }

        // Select only the set of Certificates which matches the given limit and offset values.
        int start = offset;
        int end = certCount > start + limit ? start + limit : certCount;
        for (int i = start; i < end; i++) {

            ClientCertMetadataDTO clientCertMetadataDTO = new ClientCertMetadataDTO();
            ClientCertificateDTO clientCertificateDTO = clientCertificateDTOList.get(i);
            clientCertMetadataDTO.setName(clientCertificateDTO.getAlias());
            clientCertMetadataDTO.setApplicationId(applicationId);
            clientCertMetadataDTO.setType(clientCertificateDTO.getGatewayType());
            clientCertMetadataDTO.setUUID(clientCertificateDTO.getUUID());
            clientCertificateList.add(clientCertMetadataDTO);
        }
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, certCount);
        String paginatedPrevious = "";
        String paginatedNext = "";
        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = getCertificatesPaginatedURL(RestApiConstants.CLIENT_CERTS_GET_PAGINATED_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = getCertificatesPaginatedURL(RestApiConstants.CLIENT_CERTS_GET_PAGINATED_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }
        certificatesDTO.setCount(clientCertificateList.size());
        certificatesDTO.setCertificates(clientCertificateList);
        return certificatesDTO;
    }

    /**
     * To pre validate client certificate given for an alias
     *
     * @param UUID Unique Identifier of the certificate.
     * @return Client certificate
     * @throws APIManagementException API Management Exception.
     */
    public static ClientCertificateDTO preValidateClientCertificate(String UUID, int applicationId)
            throws APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        if (StringUtils.isEmpty(UUID)) {
            throw new APIManagementException("The UUID cannot be empty", ExceptionCodes.ALIAS_CANNOT_BE_EMPTY);
        }

        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        ClientCertificateDTO clientCertificate = apiConsumer.getClientCertificate(UUID, null, applicationId);
        if (clientCertificate == null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Could not find a client certificate in truststore which belongs to "
                        + "application : %d. Hence the operation is terminated.", applicationId));
            }
            String message = "Certificate for UUID '" + UUID + "' is not found.";
            throw new APIMgtResourceNotFoundException(message);
        }
        return clientCertificate;
    }

}