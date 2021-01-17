package org.wso2.carbon.apimgt.gateway;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EndpointCertificateDeployer {
    private static final Log log = LogFactory.getLog(EndpointCertificateDeployer.class);
    private String tenantDomain;
    private final EventHubConfigurationDto eventHubConfigurationDto = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfigurationService().getAPIManagerConfiguration().getEventHubConfigurationDto();
    private String baseURL = eventHubConfigurationDto.getServiceUrl() + APIConstants.INTERNAL_WEB_APP_EP;

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

        if (closeableHttpResponse.getStatusLine().getStatusCode() == 200) {
            String content = EntityUtils.toString(closeableHttpResponse.getEntity());
            List<CertificateMetadataDTO> certificateMetadataDTOList;
            Type listType = new TypeToken<List<CertificateMetadataDTO>>() {
            }.getType();
            certificateMetadataDTOList = new Gson().fromJson(content, listType);
            for (CertificateMetadataDTO certificateMetadataDTO : certificateMetadataDTOList) {
                ResponseCode responseCode = CertificateMgtUtils.getInstance()
                        .addCertificateToSenderTrustStore(certificateMetadataDTO.getCertificate(),
                                certificateMetadataDTO.getAlias());
                if (responseCode.getResponseCode() == ResponseCode.SUCCESS.getResponseCode()) {
                    log.debug("Endpoint Certificate " + certificateMetadataDTO.getAlias() + " Deployed Successfully");
                }else if (responseCode.getResponseCode() == ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE.getResponseCode()){
                    log.error("Endpoint Certificate alias " + certificateMetadataDTO.getAlias() + " already exists");
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
            return APIUtil.executeHTTPRequest(method, httpClient);
        } catch (APIManagementException e) {
            throw new ArtifactSynchronizerException(e);
        }
    }
}
