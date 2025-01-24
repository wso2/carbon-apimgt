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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.restapi.publisher.ApisApiServiceImplUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, String> orgIdCache = new HashMap<>();

    public static void publish(String tenantName, ApiTypeWrapper apiTypeWrapper) {
        try {
            String baseUrl = APIUtil.getNewPortalURL();
            SSLConnectionSocketFactory sslsf = generateSSLSF();
            String orgId = fetchOrgIdOrCreateNew(baseUrl, tenantName, sslsf);
            if (orgId.isEmpty()) {
                log.error("Failed to create an organization for tenant: "
                        + tenantName + ". Unable to proceed with publishing API.");
                return;
            }
            publishAPI(baseUrl, apiTypeWrapper, tenantName, orgId, sslsf);
        } catch (APIManagementException e) {
            log.error("Error while publishing API for new developer portal. Error: " + e.getMessage());
        }
    }

    public static void update(String tenantName, ApiTypeWrapper apiTypeWrapper) {
        try {
            String baseUrl = APIUtil.getNewPortalURL();
            SSLConnectionSocketFactory sslsf = generateSSLSF();
            String orgId = fetchOrgIdOrCreateNew(baseUrl, tenantName, sslsf);
            if (orgId.isEmpty()) {
                log.error("Organization for tenant cannot be found and failed to create an organization for tenant: "
                        + tenantName + ". Unable to proceed with updating API.");
                return;
            }
            updateAPI(baseUrl, apiTypeWrapper, tenantName, orgId, sslsf);
        } catch (APIManagementException e) {
            log.error("Error while updating API for new developer portal. Error: " + e.getMessage());
        }
    }

    public static void unpublish(String tenantName, API api) {
        try {
            String baseUrl = APIUtil.getNewPortalURL();
            SSLConnectionSocketFactory sslsf = generateSSLSF();
            String orgId = getOrgId(tenantName, baseUrl, sslsf);
            if (orgId.isEmpty()) {
                log.warn("No organization found for tenant: "
                        + tenantName + ". Aborting unpublish operation in the new portal.");
                return;
            }
            unpublishAPI(baseUrl, api, tenantName, sslsf);
        } catch (APIManagementException e) {
            log.error("Error while unpublishing API from new developer portal. Error: " + e.getMessage());
        }
    }

    private static void publishAPI(String baseUrl, ApiTypeWrapper apiTypeWrapper, String tenantName, String orgId,
                                   SSLConnectionSocketFactory sslsf) throws APIManagementException {
        API api = apiTypeWrapper.getApi();
        String apiDefinition = ApisApiServiceImplUtils.getApiDefinition(api);
        String apiMetaData = getApiMetaData(tenantName, apiTypeWrapper);

        HttpResponseData responseData = apiPostAction(baseUrl + "/devportal/action/api", orgId,
                apiMetaData, apiDefinition, sslsf);

        if (responseData.getStatusCode() == 201) {
            log.info("API " + apiTypeWrapper.getName() + " successfully published to " + baseUrl);
        } else {
            log.error("Failed to publish API " + apiTypeWrapper.getName() + " to the new portal. Status code: "
                    + responseData.getStatusCode());
        }
    }

    private static void updateAPI(String baseUrl, ApiTypeWrapper apiTypeWrapper, String tenantName, String orgId,
                                   SSLConnectionSocketFactory sslsf) throws APIManagementException {
        API api = apiTypeWrapper.getApi();
        String apiDefinition = ApisApiServiceImplUtils.getApiDefinition(api);
        String apiMetaData = getApiMetaData(tenantName, apiTypeWrapper);

        HttpResponseData responseData = apiGetAction(baseUrl + "/devportal/action/api" ,apiTypeWrapper.getUuid(), sslsf);

        if (responseData.getStatusCode() == 200) {
            // API is available, hence update API
            try {
                Map<?, ?> jsonMap = objectMapper.readValue(responseData.getResponseBody(), Map.class);
                String apiId = (String) jsonMap.get("apiId");
                HttpResponseData apiPutResponseData = apiPutAction(baseUrl + "/devportal/action/api",
                        orgId, apiId, apiMetaData, apiDefinition, sslsf);
                if (apiPutResponseData.getStatusCode() == 200) {
                    log.info("API " + apiTypeWrapper.getName() + " successfully updated in " + baseUrl);
                } else {
                    log.error("Failed to update API " + apiTypeWrapper.getName() + " in the new portal. Status code: "
                            + apiPutResponseData.getStatusCode());
                }
            } catch (JsonProcessingException e) {
                throw new APIManagementException("Error reading response of API: " + e.getMessage(), e);
            }
        } else if (responseData.getStatusCode() == 404) {
            // API is not available, hence publish API
            HttpResponseData apiPostResponseData = apiPostAction(baseUrl + "/devportal/action/api", orgId, apiMetaData, apiDefinition, sslsf);
            if (apiPostResponseData.getStatusCode() == 201) {
                log.info("API " + apiTypeWrapper.getName() + " successfully published to " + baseUrl);
            } else {
                log.error("Failed to publish API " + apiTypeWrapper.getName() + " to the new portal. Status code: "
                        + apiPostResponseData.getStatusCode());
            }
        } else {
            log.error("Failed to find to update or publish API " + apiTypeWrapper.getName() +
                    " to the new portal. Status code: " + responseData.getStatusCode());
        }
    }

    private static void unpublishAPI(String baseUrl, API api, String tenantName,
                                   SSLConnectionSocketFactory sslsf) throws APIManagementException {
        HttpResponseData responseData = apiDeleteAction(
                baseUrl + "/devportal/action/api", tenantName ,api.getId().getUUID(), sslsf);

        if (responseData.getStatusCode() == 200) {
            log.info("API " + api.getId().getApiName() + " successfully unpublished from " + baseUrl);
        } else if (responseData.getStatusCode() == 404) {
            log.warn("API " + api.getId().getApiName() + " is not available in "
                    + baseUrl + " .Hence API cannot get unpublished from " + baseUrl);
        } else {
            log.error("Failed to unpublish API " + api.getId().getApiName() + " from new portal. Status code: "
                    + responseData.getStatusCode());
        }
    }

    private static String fetchOrgIdOrCreateNew(String baseUrl, String tenantName, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        try {
            String orgId = getOrgId(tenantName, baseUrl, sslsf);

            if (orgId.isEmpty()) {
                String newOrgInfo = generateNewOrgInfoForDeveloperPortal(tenantName);
                HttpResponseData responseData = orgPostAction(baseUrl + "/devportal/action/org", newOrgInfo, sslsf);
                if (responseData.getStatusCode() == 201) {
                    Map<?, ?> jsonMap = objectMapper.readValue(responseData.getResponseBody(), Map.class);
                    return (String) jsonMap.get("orgId");
                } else {
                    return "";
                }
            }
            return orgId;
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error retrieving organization ID: " + e.getMessage(), e);
        }
    }

    private static String getOrgId(String tenantName, String baseUrl, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        if (orgIdCache.containsKey(tenantName)) {
            // OrgID cache hit
            return orgIdCache.get(tenantName);
        }

        HttpResponseData responseData = orgGetAction(baseUrl + "/devportal/action/org", tenantName, sslsf);
        String orgId = null;

        try {
            if (responseData.getStatusCode() == 200) {
                Map<?, ?> jsonMap = objectMapper.readValue(responseData.getResponseBody(), Map.class);
                orgId = (String) jsonMap.get("orgId");
            } else if (responseData.getStatusCode() == 404) {
                orgId = "";
            }
            if (orgId != null && !orgId.isEmpty()) {
                orgIdCache.put(tenantName, orgId);
            }
            return orgId;
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error retrieving organization ID: " + e.getMessage(), e);
        }
    }

    // Data Structuring Related Methods

    private static String getApiMetaData(String orgName, ApiTypeWrapper apiTypeWrapper) {
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

    private static String generateNewOrgInfoForDeveloperPortal(String tenantName) {
        JSONObject orgInfo = new JSONObject();
        // TODO: verify below data
        orgInfo.put("orgName", tenantName);
        orgInfo.put("businessOwner", tenantName);
        orgInfo.put("businessOwnerContact", "none");
        orgInfo.put("businessOwnerEmail", "admin@none.com");
        orgInfo.put("devPortalURLIdentifier", tenantName);
        orgInfo.put("roleClaimName", "roles");
        orgInfo.put("groupsClaimName", "groups");
        orgInfo.put("organizationClaimName", "organizationID");
        orgInfo.put("organizationIdentifier", tenantName);
        orgInfo.put("adminRole", "admin");
        orgInfo.put("subscriberRole", "subscriber");
        orgInfo.put("superAdminRole", "superAdmin");
        return orgInfo.toJSONString();
    }

    // HTTPS Request Related Methods

    private static HttpResponseData orgGetAction(String apiUrl, String orgRef, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            String jsonPayload = String.format("{\"action\":\"GET\",\"orgRef\":\"%s\"}", orgRef);
            StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error retrieving organization ID: " + e.getMessage(), e);
        }
    }

    private static HttpResponseData orgPostAction(String apiUrl, String orgInfo, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            String jsonPayload = String.format("{\"action\":\"POST\",\"orgInfo\":\"%s\"}", orgInfo);
            StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error creating organization: " + e.getMessage(), e);
        }
    }

    private static HttpResponseData apiGetAction(String apiUrl, String apiRef, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            String jsonPayload = String.format("{\"action\":\"GET\",\"apiRef\":\"%s\"}", apiRef);
            StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error retrieving API information: " + e.getMessage(), e);
        }
    }

    private static HttpResponseData apiPostAction(String apiUrl, String orgId, String apiMetadata, String apiDefinition,
                                         SSLConnectionSocketFactory sslsf) throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("action", "POST", ContentType.APPLICATION_JSON);
            builder.addTextBody("orgId", orgId, ContentType.APPLICATION_JSON);
            builder.addTextBody("apiMetadata", apiMetadata, ContentType.APPLICATION_JSON);
            builder.addBinaryBody("apiDefinition", apiDefinition.getBytes(), ContentType.APPLICATION_JSON, "apiDefinition.json");
            httpPost.setEntity(builder.build());
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error sending API metadata and definition: " + e.getMessage(), e);
        }
    }

    private static HttpResponseData apiPutAction(String apiUrl, String orgId, String apiId, String apiMetadata, String apiDefinition,
                                                  SSLConnectionSocketFactory sslsf) throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("action", "PUT", ContentType.APPLICATION_JSON);
            builder.addTextBody("orgId", orgId, ContentType.APPLICATION_JSON);
            builder.addTextBody("apiId", apiId, ContentType.APPLICATION_JSON);
            builder.addTextBody("apiMetadata", apiMetadata, ContentType.APPLICATION_JSON);
            builder.addBinaryBody("apiDefinition", apiDefinition.getBytes(), ContentType.APPLICATION_JSON, "apiDefinition.json");
            httpPost.setEntity(builder.build());
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error creating API metadata and definition: " + e.getMessage(), e);
        }
    }

    private static HttpResponseData apiDeleteAction(String apiUrl, String orgRef, String apiRef, SSLConnectionSocketFactory sslsf) throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            String jsonPayload = String.format(
                    "{\"action\":\"DELETE\",\"orgRef\":\"%s\",\"apiRef\":\"%s\"}",
                    orgRef, apiRef
            );
            StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error removing API from new portal: " + e.getMessage(), e);
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
            throw new APIManagementException(e);
        } finally {
            // Clear sensitive data from memory
            Arrays.fill(keyStorePassword, ' ');
            Arrays.fill(keyPassword, ' ');
            Arrays.fill(trustStorePassword, ' ');
        }
    }
}
