/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.rest.api.publisher.utils;

import com.nimbusds.jose.util.StandardCharset;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ClientCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

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

/**
 * Utility operations for Dynamic Certificate Management REST API.
 */
public class CertificateRestApiUtils {

    private static final Log log = LogFactory.getLog(CertificateRestApiUtils.class);

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
     * Get the paginated list of certificates based on the limit and offset values provided.
     *
     * @param certificateMetadataList : The list of certificate metadata.
     * @param limit                   : The number of items per page.
     * @param offset                  : Page number
     * @param query                   : The query parameters.
     * @return : CertificatesDTO Object with the parameters set.
     */
    public static CertificatesDTO getPaginatedCertificates(
            List<CertificateMetadataDTO> certificateMetadataList, int limit, int offset, String query) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Filter the certificates based on the pagination parameters, limit = %d and " +
                    "offset = %d", limit, offset));
        }

        int certCount = certificateMetadataList.size();
        List<CertMetadataDTO> certificateList = new ArrayList<>();

        CertificatesDTO certificatesDTO = new CertificatesDTO();
        certificatesDTO.setCount(certCount > limit ? limit : certCount);

        // If the provided offset value exceeds the offset, reset the offset to default.
        if (offset > certCount) {
            offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        }

        // Select only the set of Certificates which matches the given limit and offset values.
        int start = offset;
        int end = certCount > start + limit ? start + limit : certCount;
        for (int i = start; i < end; i++) {
            CertMetadataDTO certMetadataDTO = new CertMetadataDTO();
            CertificateMetadataDTO certificateMetadataDTO = certificateMetadataList.get(i);
            certMetadataDTO.setAlias(certificateMetadataDTO.getAlias());
            certMetadataDTO.setEndpoint(certificateMetadataDTO.getEndpoint());
            certificateList.add(certMetadataDTO);
        }

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, certCount);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = getCertificatesPaginatedURL(RestApiConstants.CERTS_GET_PAGINATED_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = getCertificatesPaginatedURL(RestApiConstants.CERTS_GET_PAGINATED_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        certificatesDTO.setNext(paginatedNext);
        certificatesDTO.setPrevious(paginatedPrevious);
        certificatesDTO.setCount(certificateList.size());
        certificatesDTO.setCertificates(certificateList);
        return certificatesDTO;
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
            List<ClientCertificateDTO> clientCertificateDTOList, int limit, int offset, String query) {
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
            clientCertMetadataDTO.setAlias(clientCertificateDTO.getAlias());
            clientCertMetadataDTO.setApiId(clientCertificateDTO.getApiIdentifier().toString());
            clientCertMetadataDTO.setTier(clientCertificateDTO.getTierName());
            clientCertificateList.add(clientCertMetadataDTO);
        }
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, certCount);
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
        certificatesDTO.setNext(paginatedNext);
        certificatesDTO.setPrevious(paginatedPrevious);
        certificatesDTO.setCount(clientCertificateList.size());
        certificatesDTO.setCertificates(clientCertificateList);
        return certificatesDTO;
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
     * Generates the query string from the provided params.
     *
     * @param firstParamName   : Name of the first param.
     * @param firstParamValue  : Value of the first param.
     * @param secondParamName  : Name of the second param.
     * @param secondParamValue : Value of the second param.
     * @return : The generated query string.
     */
    public static String buildQueryString(String firstParamName, String firstParamValue,
            String secondParamName, String secondParamValue) {
        String query = "";
        if (StringUtils.isNotBlank(firstParamValue)) {
            query = query + "&" + firstParamName + "=" + firstParamValue;
        }

        if (StringUtils.isNotBlank(secondParamValue)) {
            query = query + "&" + secondParamName + "=" + secondParamValue;
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("The query string for the api : %s", query));
        }
        return query;
    }
}
