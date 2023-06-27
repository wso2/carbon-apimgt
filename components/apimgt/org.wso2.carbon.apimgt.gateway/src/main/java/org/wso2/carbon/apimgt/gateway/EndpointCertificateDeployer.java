/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

/**
 * This class used to deploy Certificates into Gateway.
 */
public class EndpointCertificateDeployer {

    private static final Log log = LogFactory.getLog(EndpointCertificateDeployer.class);
    private String tenantDomain;
    private final EventHubConfigurationDto eventHubConfigurationDto =
            ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
    private String baseURL = eventHubConfigurationDto.getServiceUrl() + APIConstants.INTERNAL_WEB_APP_EP;

    public EndpointCertificateDeployer() {
    }

    public EndpointCertificateDeployer(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public void deployCertificatesAtStartup() throws APIManagementException {

        String endpoint = baseURL + APIConstants.CERTIFICATE_RETRIEVAL_ENDPOINT;

        try (CloseableHttpResponse closeableHttpResponse = invokeService(endpoint, tenantDomain)) {
            retrieveCertificatesAndDeploy(closeableHttpResponse);

        } catch (IOException | ArtifactSynchronizerException e) {
            throw new APIManagementException("Error while inserting certificates into truststore", e);
        }
    }

    public void deployCertificate(String alias) throws APIManagementException {

        String endpoint = baseURL + APIConstants.CERTIFICATE_RETRIEVAL_ENDPOINT.concat("?alias=").concat(alias);

        try (CloseableHttpResponse closeableHttpResponse = invokeService(endpoint, tenantDomain)) {
            retrieveCertificatesAndDeploy(closeableHttpResponse);

        } catch (IOException | ArtifactSynchronizerException e) {
            throw new APIManagementException("Error while inserting certificates into truststore", e);
        }
    }

    private void retrieveCertificatesAndDeploy(CloseableHttpResponse closeableHttpResponse) throws IOException {

        boolean tenantFlowStarted = false;
        if (closeableHttpResponse.getStatusLine().getStatusCode() == 200) {
            String content = EntityUtils.toString(closeableHttpResponse.getEntity());
            List<CertificateMetadataDTO> certificateMetadataDTOList;
            Type listType = new TypeToken<List<CertificateMetadataDTO>>() {
            }.getType();
            certificateMetadataDTOList = new Gson().fromJson(content, listType);

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                tenantFlowStarted = true;
                for (CertificateMetadataDTO certificateMetadataDTO : certificateMetadataDTOList) {
                    CertificateManagerImpl.getInstance()
                            .addCertificateToGateway(certificateMetadataDTO.getCertificate(),
                                    certificateMetadataDTO.getAlias());
                }
            } finally {
                if (tenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

        }
    }

    private CloseableHttpResponse invokeService(String endpoint, String tenantDomain) throws IOException,
            ArtifactSynchronizerException {

        HttpGet method = new HttpGet(endpoint);
        URL url = new URL(endpoint);
        String username = eventHubConfigurationDto.getUsername();
        String password = eventHubConfigurationDto.getPassword();
        byte[] credentials = Base64.encodeBase64((username + APIConstants.DELEM_COLON + password).
                getBytes(APIConstants.DigestAuthConstants.CHARSET));
        int port = url.getPort();
        String protocol = url.getProtocol();
        method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC
                + new String(credentials, APIConstants.DigestAuthConstants.CHARSET));
        if (tenantDomain != null) {
            method.setHeader(APIConstants.HEADER_TENANT, tenantDomain);
        }

        HttpClient httpClient = APIUtil.getHttpClient(port, protocol);
        try {
            return APIUtil.executeHTTPRequestWithRetries(method, httpClient);
        } catch (APIManagementException e) {
            throw new ArtifactSynchronizerException(e);
        }
    }

    public void deployAllCertificatesAtStartup() throws APIManagementException {

        String endpoint = baseURL + APIConstants.CERTIFICATE_RETRIEVAL_ENDPOINT;
        try (CloseableHttpResponse closeableHttpResponse = invokeService(endpoint, APIConstants.ORG_ALL_QUERY_PARAM)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() == 200) {
                retrieveAllCertificatesAndDeploy(closeableHttpResponse.getEntity());
            } else {
                log.error("Error while retrieving certificates from the endpoint : " + endpoint
                        + "with the status code : " + closeableHttpResponse.getStatusLine().getStatusCode()
                        + "and error : " + closeableHttpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (IOException | ArtifactSynchronizerException e) {
            throw new APIManagementException("Error while inserting certificates into truststore", e);
        }
    }

    private void retrieveAllCertificatesAndDeploy(HttpEntity certContent) throws IOException {

        String content = EntityUtils.toString(certContent);
        List<CertificateMetadataDTO> certificateMetadataDTOList;
        Type listType = new TypeToken<List<CertificateMetadataDTO>>() {
        }.getType();
        certificateMetadataDTOList = new Gson().fromJson(content, listType);
        for (CertificateMetadataDTO certificateMetadataDTO : certificateMetadataDTOList) {
            CertificateManagerImpl.getInstance()
                    .addAllCertificateToGateway(certificateMetadataDTO.getCertificate(),
                            certificateMetadataDTO.getAlias(), certificateMetadataDTO.getTenantId());
        }
    }
}
