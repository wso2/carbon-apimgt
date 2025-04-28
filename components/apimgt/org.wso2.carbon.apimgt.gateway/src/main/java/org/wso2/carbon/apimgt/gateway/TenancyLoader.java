package org.wso2.carbon.apimgt.gateway;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.dto.TenantInfo;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.TenantUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.stratos.common.exception.TenantMgtException;
import org.wso2.carbon.tenant.mgt.services.TenantMgtService;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class TenancyLoader implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(TenancyLoader.class);
    private final EventHubConfigurationDto eventHubConfigurationDto = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
    private final String baseURL = eventHubConfigurationDto.getServiceUrl() + APIConstants.INTERNAL_WEB_APP_EP;

    public void retrieveAndLoadAllTenants() throws APIManagementException {
        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
        String encodedTenants = null;
        try {
            encodedTenants = URLEncoder.encode(StringUtils.join(gatewayArtifactSynchronizerProperties, "|"), APIConstants.DigestAuthConstants.CHARSET);
            encodedTenants = encodedTenants.replace("\\+", "%20");
            String endpoint = baseURL + APIConstants.TENANT_INFO_ENDPOINT + "?tenants=" + encodedTenants;
            try (CloseableHttpResponse closeableHttpResponse = invokeService(endpoint)) {
                if (closeableHttpResponse.getStatusLine().getStatusCode() == 200) {
                    try (InputStream inputStream = closeableHttpResponse.getEntity().getContent()) {
                        String response = IOUtils.toString(inputStream, APIConstants.DigestAuthConstants.CHARSET);
                        TenantInfoList tenantInfoList = new Gson().fromJson(response, TenantInfoList.class);
                        TenantMgtService tenantMgtService = ServiceReferenceHolder.getInstance().getTenantMgtService();
                        for (TenantInfo tenantInfo : tenantInfoList.getTenants()) {
                            try {
                                if (tenantMgtService.isDomainAvailable(tenantInfo.getDomain())) {
                                    TenantUtils.addTenant(tenantInfo);
                                } else {
                                    TenantUtils.updateTenant(tenantInfo);
                                }
                            } catch (TenantMgtException | UserStoreException e) {
                                log.error("Error occurred while registering tenant " + tenantInfo.getDomain(), e);
                            }
                        }
                    }
                } else {
                    throw new APIManagementException("Failed to retrieve tenant information");
                }
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while retrieving tenant info", e);
        }
    }

    private CloseableHttpResponse invokeService(String endpoint) throws APIManagementException, IOException {

        HttpGet method = new HttpGet(endpoint);
        URL url = new URL(endpoint);
        String username = eventHubConfigurationDto.getUsername();
        String password = eventHubConfigurationDto.getPassword();
        byte[] credentials = Base64.encodeBase64((username + APIConstants.DELEM_COLON + password).getBytes(APIConstants.DigestAuthConstants.CHARSET));
        int port = url.getPort();
        String protocol = url.getProtocol();
        method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC + new String(credentials, APIConstants.DigestAuthConstants.CHARSET));


        HttpClient httpClient = APIUtil.getHttpClient(port, protocol);
        return APIUtil.executeHTTPRequestWithRetries(method, httpClient);
    }

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        try {
            retrieveAndLoadAllTenants();
        } catch (APIManagementException e) {
            log.error("Error while loading tenants", e);
        }
    }

    public static class TenantInfoList {
        private List<TenantInfo> tenants = new ArrayList<>();

        public List<TenantInfo> getTenants() {
            return tenants;
        }

        public void setTenants(List<TenantInfo> tenants) {
            this.tenants = tenants;
        }
    }


}
