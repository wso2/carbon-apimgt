/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.impl.dao.constants.DevPortalProcessingConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.dto.devportal.ApiMetaDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.ServerConfiguration;

/**
 * This class used to handle newly introduced 2025 version of Developer Portal's configuration with APIM.
 */
public class NewDevPortalHandlerImpl implements NewDevPortalHandler {

    private static final Log log = LogFactory.getLog(NewDevPortalHandlerImpl.class);
    private static final String baseUrl = getNewPortalURL();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, String> orgIdCache = new ConcurrentHashMap<>();
    private static volatile NewDevPortalHandlerImpl instance;

    private NewDevPortalHandlerImpl() {}

    public static NewDevPortalHandlerImpl getInstance() {
        if (instance == null) {
            synchronized (NewDevPortalHandlerImpl.class) {
                if (instance == null) {
                    instance = new NewDevPortalHandlerImpl();
                }
            }
        }
        return instance;
    }


    private static class HttpResponseData {
        private final int statusCode;
        private final String responseBody;

        private HttpResponseData(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        private int getStatusCode() {
            return statusCode;
        }

        private String getResponseBody() {
            return responseBody;
        }
    }

    @Override
    public boolean isNewPortalEnabled() {
        return Boolean.parseBoolean(getConfigProperty(APIConstants.API_STORE_NEW_PORTAL_ENABLED, "false"));
    }

    @Override
    public void publish(String tenantName, ApiTypeWrapper apiTypeWrapper) {
        try {
            SSLConnectionSocketFactory sslsf = generateSSLSF();
            publishAPI(apiTypeWrapper,  tenantName, sslsf);
        } catch (APIManagementException e) {
            log.error("Error while publishing API to " + baseUrl + ". Error: " + e.getMessage());
        }
    }

    @Override
    public void update(String tenantName, ApiTypeWrapper apiTypeWrapper) {
        try {
            SSLConnectionSocketFactory sslsf = generateSSLSF();
            updateAPI(apiTypeWrapper,  tenantName, sslsf);
        } catch (APIManagementException e) {
            log.error("Error while updating API to " + baseUrl + ". Error: " + e.getMessage());
        }
    }

    @Override
    public void unpublish(String tenantName, API api) {
        try {
            SSLConnectionSocketFactory sslsf = generateSSLSF();
            unpublishAPI(api, tenantName, sslsf);
        } catch (APIManagementException e) {
            log.error("Error while un-publishing API from " + baseUrl + ". Error: " + e.getMessage());
        }
    }

    private static void publishAPI(ApiTypeWrapper apiTypeWrapper, String tenantName,
                                   SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        String orgId = getOrgId(tenantName, sslsf);
        if (orgId != null) {
            API api = apiTypeWrapper.getApi();
            String apiDefinition = getDefinitionForDevPortal(api, tenantName);
            String apiMetaData = getApiMetaData(apiTypeWrapper);

            HttpResponseData responseData = apiPostAction(orgId, apiMetaData, apiDefinition, sslsf);

            if (responseData.getStatusCode() == 201) {
                log.info("API " + apiTypeWrapper.getName() + " successfully published to " + baseUrl);
            } else {
                log.error("Failed to publish API " + apiTypeWrapper.getName() + " to " + baseUrl + ". " +
                        "Status code: " + responseData.getStatusCode());
            }
        } else {
            log.error("Unable to find an organization in " + baseUrl + " that matches the tenant." +
                    "Hence failed to publish in " + baseUrl);
        }
    }

    private static void updateAPI(ApiTypeWrapper apiTypeWrapper, String tenantName,
                                  SSLConnectionSocketFactory sslsf) throws APIManagementException {
        API api = apiTypeWrapper.getApi();
        String apiDefinition = getDefinitionForDevPortal(api, tenantName);
        String apiMetaData = getApiMetaData(apiTypeWrapper);
        String orgId = getOrgId(tenantName, sslsf);
        if (orgId != null) {
            String apiId = getApiId(orgId, apiTypeWrapper.getName(), api.getId().getVersion(), sslsf);
            if (apiId != null) {
                HttpResponseData apiPutResponseData = apiPutAction(orgId, apiId, apiDefinition, apiMetaData, sslsf);
                if (apiPutResponseData.getStatusCode() == 200) {
                    log.info("API " + apiTypeWrapper.getName() + " successfully updated in " + baseUrl);
                } else {
                    log.error("Failed to update API " + apiTypeWrapper.getName() + " in " + baseUrl +
                            ". Status code: " + apiPutResponseData.getStatusCode());
                }
            } else {
                log.error("API is not available in " + baseUrl + ". Hence failed to update in " + baseUrl);
            }
        } else {
            log.error("Unable to find an organization in " + baseUrl + " that matches the tenant." +
                    "Hence failed to update in " + baseUrl);
        }
    }

    private static void unpublishAPI(API api, String tenantName,
                                   SSLConnectionSocketFactory sslsf) throws APIManagementException {
        String orgId = getOrgId(tenantName, sslsf);
        if (orgId != null) {
            String apiId = getApiId(orgId, api.getId().getApiName(), api.getId().getVersion(), sslsf);
            if (apiId != null) {
                HttpResponseData responseData = apiDeleteAction(orgId, apiId, sslsf);
                if (responseData.getStatusCode() == 200) {
                    log.info("API " + api.getId().getApiName() + " successfully unpublished from " + baseUrl);
                } else if (responseData.getStatusCode() == 404) {
                    log.warn("API " + api.getId().getApiName() + " is not available in "
                            + baseUrl + " .Hence API cannot get unpublished from " + baseUrl);
                } else {
                    log.error("Failed to delete API " + api.getId().getApiName() + " from " + baseUrl +
                            ". Status code: " + responseData.getStatusCode());
                }
            }
        } else {
            log.error("Unable to find an organization in " + baseUrl + " that matches the tenant." +
                    "Hence fails to un-publish from " + baseUrl);
        }
    }

    private static String getDefinitionForDevPortal (API api, String tenantName) throws APIManagementException {
        APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(api.getId().getProviderName());
        String type = getType(api.getType());
        if (type.equals("REST")) {
            return apiConsumer.getOpenAPIDefinitionForEnvironment(api, "Default");
        } else if (type.equals("AsyncAPI")) {
            return apiConsumer.getAsyncAPIDefinition(api.getUuid(), tenantName);
        } else {
            // TODD: Handle other types of definitions
            return null;
        }
    }

    private static String getOrgId(String tenantName, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        if (orgIdCache.containsKey(tenantName)) {
            // OrgID cache hit
            return orgIdCache.get(tenantName);
        }
        HttpResponseData responseData = orgGetAction(tenantName, sslsf);
        String orgId;
        try {
            if (responseData.getStatusCode() == 200) {
                Map<?, ?> jsonMap = objectMapper.readValue(responseData.getResponseBody(), Map.class);
                orgId = (String) jsonMap.get("orgId");
                if (orgId == null || orgId.isEmpty()) {
                    return null;
                }
            } else {
                return null;
            }
            orgIdCache.put(tenantName, orgId);
            return orgId;
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error while processing Json response: " + e.getMessage(), e);
        }
    }

    private static String getApiId(String orgId, String apiName, String apiVersion, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        HttpResponseData responseData = apiGetAction(orgId, apiName, apiVersion, sslsf);

        if (responseData.getStatusCode() == 200) {
            try {
                List<Map<String, Object>> apiList = objectMapper.readValue(responseData.getResponseBody(),
                        new TypeReference<List<Map<String, Object>>>() {});
                if (apiList.size() == 1) {
                    Map<String, Object> apiDetails = apiList.get(0);
                    return (String) apiDetails.get("apiID");
                } else if (apiList.size() > 1) {
                    log.error("There are multiple APIs for the API name: " + apiName + " & version: " + apiVersion);
                    return null;
                } else {
                    log.error("No API found for the API name: " + apiName + " & version: " + apiVersion);
                    return null;
                }
            } catch (JsonProcessingException e) {
                throw new APIManagementException("Error reading API response: " + e.getMessage(), e);
            }
        } else if (responseData.getStatusCode() == 404) {
            log.error("API is not available in " + baseUrl + " for the name: " + apiName +
                    " and version: " + apiVersion);
            return null;
        } else {
            log.error("Failed to retrieve API ID. Status code: " + responseData.getStatusCode());
            return null;
        }
    }

    private static String getApiMetaData(ApiTypeWrapper apiTypeWrapper) throws APIManagementException {
        API api = apiTypeWrapper.getApi();
        ApiMetaDataDTO apiMetaDataDTO = new ApiMetaDataDTO();

        ApiMetaDataDTO.ApiInfo apiInfo = new ApiMetaDataDTO.ApiInfo();
        apiInfo.setReferenceID(Objects.toString(apiTypeWrapper.getUuid(), ""));
        apiInfo.setProvider("WSO2"); // DEV PORTAL expects WSO2 as Provider when API coming from WSO2 API Manager
        apiInfo.setApiName(Objects.toString(apiTypeWrapper.getName(), ""));
        apiInfo.setApiDescription(Objects.toString(api.getDescription(), ""));
        if (Objects.toString(api.getVisibility(), "").equals("public")) {
            apiInfo.setVisibility("PUBLIC");
        } else {
            apiInfo.setVisibility(Objects.toString(api.getVisibility(), ""));
            apiInfo.setVisibleGroups(generateVisibleGroupsArray(api));
        }
        apiInfo.setOwners(generateOwnersObject(api));
        apiInfo.setApiVersion(Objects.toString(api.getId().getVersion(), ""));
        apiInfo.setApiType(getType(api.getType()));
        apiMetaDataDTO.setApiInfo(apiInfo);

        apiMetaDataDTO.setSubscriptionPolicies(convertToSubscriptionPolicies(api.getAvailableTiers().toArray()));

        ApiMetaDataDTO.EndPoints endPoints = new ApiMetaDataDTO.EndPoints();
        endPoints.setSandboxURL(getSandboxEndpoint(api.getEndpointConfig()));
        endPoints.setProductionURL(getProductionEndpoint(api.getEndpointConfig()));

        apiMetaDataDTO.setEndPoints(endPoints);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(apiMetaDataDTO);
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error while converting ApiMetaDataDTO to JSON: " + e.getMessage(), e);
        }
    }

    private static String getSandboxEndpoint(String jsonString) throws APIManagementException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(jsonString, Map.class);

            Map<?, ?> sandboxEndpoints = (Map<?, ?>) jsonMap.get("sandbox_endpoints");
            return (String) sandboxEndpoints.get("url");
        } catch (Exception e){
            throw new APIManagementException("Error reading Endpoints: " + e.getMessage(), e);
        }
    }

    private static String getProductionEndpoint(String jsonString) throws APIManagementException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(jsonString, Map.class);

            Map<?, ?> productionEndpoints = (Map<?, ?>) jsonMap.get("production_endpoints");
            return (String) productionEndpoints.get("url");
        } catch (Exception e){
            throw new APIManagementException("Error reading Endpoints: " + e.getMessage(), e);
        }
    }

    private static String getType(String type) {
        if (APIConstants.API_TYPE_WS.equals(type) || APIConstants.API_TYPE_WEBSUB.equals(type) ||
                APIConstants.API_TYPE_SSE.equals(type) || APIConstants.API_TYPE_ASYNC.equals(type) ||
                APIConstants.API_TYPE_WEBHOOK.equals(type)) {
            return "AsyncAPI";
        } else if (APIConstants.API_TYPE_GRAPHQL.equals(type)) {
            return "GraphQL";
        } else if (APIConstants.API_TYPE_SOAP.equals(type)) {
            return "SOAP";
        } else {
            return "REST";
        }
    }

    private static List<String> convertToSubscriptionPolicies(Object[] tiers) {
        List<String> subscriptionPolicies = new ArrayList<>();
        for (Object tier : tiers) {
            if (tier instanceof Tier) {
                Tier tierObject = (Tier) tier;
                String name = tierObject.getName();
                if (name != null) {
                    subscriptionPolicies.add(name); // Add tier name directly
                }
            }
        }
        return subscriptionPolicies;
    }

    private static List<String> generateVisibleGroupsArray(API api) {
        List<String> visibleGroupsList = new ArrayList<>();
        if (api.getVisibleRoles() != null) {
            visibleGroupsList = Arrays.asList(api.getVisibleRoles().split(","));
        }
        return visibleGroupsList;
    }

    private static ApiMetaDataDTO.ApiInfo.Owners generateOwnersObject(API api) {
        ApiMetaDataDTO.ApiInfo.Owners owners = new ApiMetaDataDTO.ApiInfo.Owners();
        owners.setTechnicalOwner(Objects.toString(api.getTechnicalOwner(), ""));
        owners.setTechnicalOwnerEmail(Objects.toString(api.getTechnicalOwnerEmail(), ""));
        owners.setBusinessOwner(Objects.toString(api.getBusinessOwner(), ""));
        owners.setBusinessOwnerEmail(Objects.toString(api.getBusinessOwnerEmail(), ""));
        return owners;
    }

    // HTTPS Request Related Methods

    private static HttpResponseData orgGetAction(String orgRef, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.ORG_URI + "/" + orgRef;
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            URIBuilder uriBuilder = new URIBuilder(apiUrl);
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException | URISyntaxException e) {
            throw new APIManagementException("Error while organization search in " + baseUrl + ": " + e.getMessage(), e);
        }
    }

    private static HttpResponseData apiGetAction(String orgId, String name, String version,
                                                 SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.API_URI;
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            URIBuilder uriBuilder = new URIBuilder(apiUrl)
                    .addParameter("name", name)
                    .addParameter("version", version);
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.setHeader("organization", orgId);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException | URISyntaxException e) {
            throw new APIManagementException("Error while API search in " + baseUrl + ": " + e.getMessage(), e);
        }
    }

    private static HttpResponseData apiDeleteAction(String orgId, String apiId, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.API_URI + "/" + apiId;

        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpDelete httpDelete = new HttpDelete(apiUrl);
            httpDelete.setHeader("organization", orgId);

            try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while API delete in " + baseUrl + ": " + e.getMessage(), e);
        }
    }


    private static HttpResponseData apiPostAction(String orgId, String apiMetadata, String apiDefinition,
                                                  SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.API_URI;
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("organization", orgId);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("apiMetadata", apiMetadata, ContentType.APPLICATION_JSON);
            builder.addBinaryBody("apiDefinition", apiDefinition.getBytes(), ContentType.APPLICATION_JSON,
                    "apiDefinition.json");
            httpPost.setEntity(builder.build());
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while API publish in " + baseUrl + ": " + e.getMessage(), e);
        }
    }

    private static HttpResponseData apiPutAction(String orgId, String apiId, String apiDefinition,
                                                 String apiMetadata, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.API_URI + "/" + apiId;
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPut httpPut = new HttpPut(apiUrl);
            httpPut.setHeader("organization", orgId);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("apiMetadata", apiMetadata, ContentType.APPLICATION_JSON);
            builder.addBinaryBody("apiDefinition", apiDefinition.getBytes(), ContentType.APPLICATION_JSON,
                    "apiDefinition.json");
            httpPut.setEntity(builder.build());
            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while API update in " + baseUrl + ": " + e.getMessage(), e);
        }
    }

    private static SSLConnectionSocketFactory generateSSLSF() throws APIManagementException {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        char[] keyStorePassword = serverConfig.getFirstProperty("Security.KeyStore.Password").toCharArray();
        char[] keyPassword = serverConfig.getFirstProperty("Security.KeyStore.KeyPassword").toCharArray();
        char[] trustStorePassword = serverConfig.getFirstProperty("Security.TrustStore.Password").toCharArray();
        try {
            // Key Store
            KeyStore keyStore;
            String keyStoreType = serverConfig.getFirstProperty("Security.KeyStore.Type");
            if (keyStoreType != null) {
                keyStore = KeyStore.getInstance(keyStoreType);
            } else {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            }
            try (FileInputStream keyStoreStream = new FileInputStream(
                    serverConfig.getFirstProperty("Security.KeyStore.Location"))) {
                keyStore.load(keyStoreStream, keyStorePassword);
            }

            // Trust Store
            KeyStore trustStore;
            String trustStoreType = serverConfig.getFirstProperty("Security.TrustStore.Type");
            if (trustStoreType != null) {
                trustStore = KeyStore.getInstance(trustStoreType);
            } else {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            }
            try (FileInputStream trustStoreStream = new FileInputStream(
                    serverConfig.getFirstProperty("Security.TrustStore.Location"))) {
                trustStore.load(trustStoreStream, trustStorePassword);
            }

            return new SSLConnectionSocketFactory(
                    SSLContexts.custom()
                            .loadKeyMaterial(
                                    keyStore,
                                    keyPassword,
                                    (aliases, socket) -> serverConfig.getFirstProperty("Security.KeyStore.KeyAlias"))
                            .loadTrustMaterial(trustStore, null).build());
        } catch (GeneralSecurityException | IOException e) {
            throw new APIManagementException("Error while certification processing: " + e.getMessage(), e);
        } finally {
            // Clear Sensitive Data From Memory
            Arrays.fill(keyStorePassword, ' ');
            Arrays.fill(keyPassword, ' ');
            Arrays.fill(trustStorePassword, ' ');
        }
    }

    private static String getNewPortalURL() {
        return getConfigProperty(APIConstants.API_STORE_NEW_PORTAL_URL, "");
    }

    private static String getConfigProperty(String key, String defaultValue) {
        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String propertyValue = apiManagerConfiguration.getFirstProperty(key);
        return StringUtils.isNotEmpty(propertyValue) ? propertyValue : defaultValue;
    }
}
