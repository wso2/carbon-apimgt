/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.apimgt.gateway;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.dto.TenantInfo;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.TenantUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.LoadingTenants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.stratos.common.exception.TenantMgtException;
import org.wso2.carbon.tenant.mgt.services.TenantMgtService;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for loading tenant information from the Event Hub to Gateway.
 */
public class TenancyLoader {
    private static final Log log = LogFactory.getLog(TenancyLoader.class);

    public void retrieveAndLoadAllTenants() throws APIManagementException {
        EventHubConfigurationDto eventHubConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
        String baseURL = eventHubConfigurationDto.getServiceUrl() + APIConstants.INTERNAL_WEB_APP_EP;

        String base64EncodedTenants;
        try {
            base64EncodedTenants = Base64.encodeBase64URLSafeString(
                    retrieveTenantFilter().getBytes(APIConstants.DigestAuthConstants.CHARSET));
            String endpoint = baseURL + APIConstants.TENANT_INFO_ENDPOINT + "?filter=" + base64EncodedTenants;
            String response = invokeService(endpoint);
            TenantInfoList tenantInfoList = new Gson().fromJson(response, TenantInfoList.class);
            TenantMgtService tenantMgtService = ServiceReferenceHolder.getInstance().getTenantMgtService();
            if (tenantMgtService != null) {
                for (TenantInfo tenantInfo : tenantInfoList.getTenants()) {
                    try {
                        if (tenantMgtService.isDomainAvailable(tenantInfo.getDomain())) {
                            TenantUtils.addTenant(tenantInfo);
                            APIUtil.loadTenantConfigBlockingMode(tenantInfo.getDomain());
                        } else {
                            TenantUtils.updateTenant(tenantInfo);
                        }
                    } catch (TenantMgtException | UserStoreException e) {
                        log.error("Error occurred while registering tenant " + tenantInfo.getDomain(), e);
                    }
                }
            }

        } catch (IOException e) {
            throw new APIManagementException("Error while Loading tenants", e);
        }
    }


    /**
     * This method is used to call the eventhub rest API to retrieve subscribers.
     *
     * @return the response body.
     */
    private String invokeService(String endpoint) throws APIManagementException {
        EventHubConfigurationDto eventHubConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
        int retrievalRetries = 15;

        try {
            HttpGet method = new HttpGet(endpoint);
            String username = eventHubConfigurationDto.getUsername();
            String password = eventHubConfigurationDto.getPassword();
            byte[] credentials = Base64.encodeBase64((username + APIConstants.DELEM_COLON + password).getBytes(
                    APIConstants.DigestAuthConstants.CHARSET));
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            URL eventHubURL = new URL(endpoint);
            int eventHubPort = eventHubURL.getPort();
            String eventHubProtocol = eventHubURL.getProtocol();
            HttpClient httpClient = APIUtil.getHttpClient(eventHubPort, eventHubProtocol);
            HttpResponse httpResponse = null;
            int retryCount = 0;
            boolean retry = true;
            do {
                try {
                    httpResponse = httpClient.execute(method);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        retry = false;
                    } else {
                        if (retryCount >= retrievalRetries) {
                            retry = false;
                            throw new APIManagementException(
                                    "Failed to retrieve tenants-info data from remote endpoint. " +
                                            "HTTP response code: " + httpResponse.getStatusLine().getStatusCode());
                        }
                        retryCount++;
                    }
                } catch (IOException ex) {
                    retryCount++;
                    if (retryCount < retrievalRetries) {
                        log.warn("Failed retrieving tenants-info data from remote endpoint: " + ex.getMessage() +
                                ". Retrying after " + retrievalRetries + " seconds...");
                        Thread.sleep(retrievalRetries * 1000);
                    } else {
                        log.error("Failed retrieving tenants-info data from remote endpoint: " + ex.getMessage());
                        throw ex;
                    }
                }
            } while (retry);
            return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        } catch (IOException | InterruptedException e) {
            log.error("Exception when retrieving tenants-info data from remote endpoint ", e);
            throw new APIManagementException("Error occurred retrieving tenant-info", e);
        }
    }

    public static class TenantInfoList {
        private final List<TenantInfo> tenants = new ArrayList<>();

        public List<TenantInfo> getTenants() {
            return tenants;
        }

    }

    private String retrieveTenantFilter() {
        String tenantFilter = "*";
        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
                    apiManagerConfiguration.getGatewayArtifactSynchronizerProperties();
            if (gatewayArtifactSynchronizerProperties != null) {
                LoadingTenants loadingTenants = gatewayArtifactSynchronizerProperties.getLoadingTenants();
                if (loadingTenants != null) {
                    if (loadingTenants.isIncludeAllTenants()) {
                        tenantFilter = "*";
                    } else if (!loadingTenants.getIncludingTenants().isEmpty()) {
                        tenantFilter = StringUtils.join(loadingTenants.getIncludingTenants(), "|");
                    }
                    if (!loadingTenants.getExcludingTenants().isEmpty()) {
                        String excludingFilter = StringUtils.join(loadingTenants.getExcludingTenants(), "|");
                        tenantFilter = tenantFilter.concat("&!").concat(excludingFilter);
                    }
                }
            }
        }
        return tenantFilter;
    }
}
