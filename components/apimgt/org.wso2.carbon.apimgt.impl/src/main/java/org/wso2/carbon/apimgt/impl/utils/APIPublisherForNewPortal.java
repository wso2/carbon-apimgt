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

package org.wso2.carbon.apimgt.impl.utils;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.impl.dao.constants.DevPortalProcessingConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.restapi.publisher.ApisApiServiceImplUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

public class APIPublisherForNewPortal {

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

    private static final Log log = LogFactory.getLog(APIPublisherForNewPortal.class);
    private static final String baseUrl = APIUtil.getNewPortalURL();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, String> orgIdCache = new HashMap<>();

    public static void publish(String tenantName, ApiTypeWrapper apiTypeWrapper) {
        try {
            SSLConnectionSocketFactory sslsf = generateSSLSF();
            publishAPI(apiTypeWrapper,  tenantName, sslsf);
        } catch (APIManagementException e) {
            log.error("Error while publishing API to " + baseUrl + ". Error: " + e.getMessage());
        }
    }

    public static void update(String tenantName, ApiTypeWrapper apiTypeWrapper) {
        try {
            SSLConnectionSocketFactory sslsf = generateSSLSF();
            updateAPI(apiTypeWrapper,  tenantName, sslsf);
        } catch (APIManagementException e) {
            log.error("Error while updating API to " + baseUrl + ". Error: " + e.getMessage());
        }
    }

    public static void unpublish(String tenantName, API api) {
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
            String apiDefinition = ApisApiServiceImplUtils.getApiDefinition(api);
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
        String apiDefinition = ApisApiServiceImplUtils.getApiDefinition(api);
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

    // Data Structuring Related Methods

    private static String getApiMetaData(ApiTypeWrapper apiTypeWrapper) {
        API api = apiTypeWrapper.getApi();

        JSONObject apiInfo = new JSONObject();
        apiInfo.put("referenceID", defaultString(apiTypeWrapper.getUuid()));
        apiInfo.put("provider", defaultString(api.getId().getProviderName()));
        apiInfo.put("apiName", defaultString(apiTypeWrapper.getName()));
        apiInfo.put("apiDescription", defaultString(api.getDescription()));
        if (defaultString(api.getVisibility()).equals("public")){
            apiInfo.put("visibility", "PUBLIC");
        } else {
            apiInfo.put("visibility", defaultString(api.getVisibility()));
            apiInfo.put("visibleGroups", generateVisibleGroupsArray(api));
        }
        apiInfo.put("owners", generateOwnersObject(api));
        apiInfo.put("apiVersion", defaultString(api.getId().getVersion()));
        apiInfo.put("apiType", defaultString(apiTypeWrapper.getType()));

        JSONObject endPoints = new JSONObject();
        endPoints.put("sandboxURL", defaultString(api.getApiExternalSandboxEndpoint()));
        endPoints.put("productionURL", defaultString(api.getApiExternalProductionEndpoint()));

        JSONObject response = new JSONObject();
        response.put("apiInfo", apiInfo);
        response.put("subscriptionPolicies", convertToSubscriptionPolicies(api.getAvailableTiers().toArray()));
        response.put("endPoints", endPoints);

        return response.toJSONString();
    }

    private static List<Map<String, String>> convertToSubscriptionPolicies(Object[] tiers) {
        List<Map<String, String>> subscriptionPolicies = new ArrayList<>();
        for (Object tier : tiers) {
            if (tier instanceof Tier) {
                Tier tierObject = (Tier) tier;
                String name = tierObject.getName();
                if (name != null) {
                    subscriptionPolicies.add(Collections.singletonMap("policyName", name));
                }
            }
        }
        return subscriptionPolicies;
    }

    private static String defaultString(String value) {
        return value != null ? value : "";
    }

    private static JSONArray generateVisibleGroupsArray(API api) {
        JSONArray visibleGroupsArray = new JSONArray();
        if (api.getVisibleRoles() != null) {
            for (String role : api.getVisibleRoles().split(",")) {
                visibleGroupsArray.add(role);
            }
        }
        return visibleGroupsArray;
    }

    private static JSONObject generateOwnersObject(API api) {
        JSONObject owners = new JSONObject();
        // TODO: verify below data
        owners.put("technicalOwner", defaultString(api.getTechnicalOwner()));
        owners.put("technicalOwnerEmail", defaultString(api.getTechnicalOwnerEmail()));
        owners.put("businessOwner", defaultString(api.getBusinessOwner()));
        owners.put("businessOwnerEmail", defaultString(api.getBusinessOwnerEmail()));
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
        Map<String, String> serverSecurityStores = APIUtil.getServerSecurityStores();
        char[] keyStorePassword = serverSecurityStores.get("keyStorePassword").toCharArray();
        char[] keyPassword = serverSecurityStores.get("keyPassword").toCharArray();
        char[] trustStorePassword = serverSecurityStores.get("trustStorePassword").toCharArray();
        try {
            KeyStore keyStore;
            String keyStoreType = serverSecurityStores.get("keyStoreType");
            if (keyStoreType != null) {
                keyStore = KeyStore.getInstance(keyStoreType);
            } else {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            }
            try (FileInputStream keyStoreStream = new FileInputStream(serverSecurityStores.get("keyStoreLocation"))) {
                keyStore.load(keyStoreStream, keyStorePassword);
            }
            return new SSLConnectionSocketFactory(
                    SSLContexts.custom()
                            .loadKeyMaterial(
                                    keyStore,
                                    keyPassword,
                                    (aliases, socket) -> serverSecurityStores.get("keyAlias"))
                            .loadTrustMaterial(
                                    new File(serverSecurityStores.get("trustStoreLocation")),
                                    trustStorePassword)
                            .build()
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new APIManagementException("Error while certification processing: " + e.getMessage(), e);
        } finally {
            // Clear Sensitive Data From Memory
            Arrays.fill(keyStorePassword, ' ');
            Arrays.fill(keyPassword, ' ');
            Arrays.fill(trustStorePassword, ' ');
        }
    }
}
