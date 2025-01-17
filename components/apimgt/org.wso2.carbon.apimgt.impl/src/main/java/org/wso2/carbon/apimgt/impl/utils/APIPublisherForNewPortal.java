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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import org.wso2.carbon.apimgt.impl.restapi.publisher.ApisApiServiceImplUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIPublisherForNewPortal {

    private static final Log log = LogFactory.getLog(APIPublisherForNewPortal.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final char[] trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword").toCharArray();
    private static final String trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");

    public static void publish(String tenantName, ApiTypeWrapper apiTypeWrapper) throws APIManagementException {

        // TODO: This section is not finalized yet
        String baseUrl = APIUtil.getNewPortalURL();
        Certificate cert = SigningUtil.getPublicCertificate(-1234);
        PrivateKey privateKey = SigningUtil.getSigningKey(-1234);

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(configureSSLContext(cert, privateKey));

        String orgId = fetchOrgIdOrCreateNew(baseUrl, tenantName, sslsf);
        if (orgId.isEmpty()) {
            log.error("Failed to create or fetch organization ID.");
            return;
        }

        publishAPI(baseUrl, apiTypeWrapper, tenantName, orgId, sslsf);
    }

    private static void publishAPI(String baseUrl, ApiTypeWrapper apiTypeWrapper, String tenantName, String orgId,
                                   SSLConnectionSocketFactory sslsf) throws APIManagementException {
        API api = apiTypeWrapper.getApi();
        String apiDefinition = ApisApiServiceImplUtils.getApiDefinition(api);
        String apiMetaData = getApiMetaData(tenantName, apiTypeWrapper);

        int statusCode = publishApiMetadata(baseUrl + "/devportal/b2b/organizations/" + orgId + "/apis",
                apiMetaData, apiDefinition, sslsf);

        if (statusCode == 201) {
            log.info("API " + apiTypeWrapper.getName() + " successfully published to " + baseUrl);
        } else {
            log.error("Failed to publish API " + apiTypeWrapper.getName() + ". Status code: " + statusCode);
        }
    }

    private static String fetchOrgIdOrCreateNew(String baseUrl, String tenantName, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        String orgId = getNewPortalOrgId(baseUrl + "/devportal/b2b/organizations/" + tenantName, sslsf);

        if (orgId.isEmpty()) {
            String newOrgInfo = generateNewOrgInfoForDeveloperPortal(tenantName);
            orgId = createNewOrg(baseUrl + "/devportal/b2b/organizations", newOrgInfo, sslsf);
        }
        return orgId;
    }

    private static String getApiMetaData(String orgName, ApiTypeWrapper apiTypeWrapper) {
        API api = apiTypeWrapper.getApi();

        JSONObject apiInfo = new JSONObject();
        //TODO: Verify below data
        apiInfo.put("referenceID", defaultString(apiTypeWrapper.getUuid()));
        apiInfo.put("apiName", defaultString(apiTypeWrapper.getName()));
        apiInfo.put("orgName", defaultString(orgName));
        apiInfo.put("provider", defaultString(api.getId().getProviderName()));
        apiInfo.put("apiCategory", "");
        apiInfo.put("apiDescription", defaultString(api.getDescription()));
        apiInfo.put("visibility", defaultString(api.getVisibility()));
        apiInfo.put("visibleGroups", generateVisibleGroupsArray(api));
        apiInfo.put("owners", generateOwnersObject(api));
        apiInfo.put("apiVersion", defaultString(api.getId().getVersion()));
        apiInfo.put("apiType", defaultString(apiTypeWrapper.getType()));

        JSONObject endPoints = new JSONObject();
        endPoints.put("sandboxURL", defaultString(api.getApiExternalSandboxEndpoint()));
        endPoints.put("productionURL", defaultString(api.getApiExternalProductionEndpoint()));

        JSONObject response = new JSONObject();
        response.put("apiInfo", apiInfo);
        response.put("subscriptionPolicies", new JSONArray());
        response.put("endPoints", endPoints);

        return response.toJSONString();
    }

    private static String defaultString(String value) {
        return value != null ? value : "";
    }

    // TODO: Verify below data
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

    private static String getNewPortalOrgId(String apiUrl, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpGet httpGet = new HttpGet(apiUrl);
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            if (statusCode == 200) {
                Map<?, ?> jsonMap = objectMapper.readValue(responseBody, Map.class);
                return (String) jsonMap.get("orgId");
            } else if (statusCode == 404) {
                return "";
            } else {
                throw new APIManagementException("Unexpected response: " + statusCode + " - " + responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException(e);
        }
    }

    public static int publishApiMetadata(String apiUrl, String apiMetadata, String apiDefinition,
                                         SSLConnectionSocketFactory sslsf) throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("apiMetadata", apiMetadata, ContentType.APPLICATION_JSON);
            builder.addBinaryBody("apiDefinition", apiDefinition.getBytes(), ContentType.APPLICATION_JSON,
                    "apiDefinition.json");

            httpPost.setEntity(builder.build());

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return response.getStatusLine().getStatusCode();
            }
        } catch (IOException e) {
            throw new APIManagementException("Error sending API metadata and definition: " + e.getMessage(), e);
        }
    }

    public static String createNewOrg(String apiUrl, String orgInfo, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(orgInfo, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                if (statusCode == 201) {
                    Map<?, ?> jsonMap = objectMapper.readValue(responseBody, Map.class);
                    return (String) jsonMap.get("orgId");
                }
                return "";
            }
        } catch (IOException e) {
            throw new APIManagementException("Error creating organization: " + e.getMessage(), e);
        }
    }

    private static SSLContext configureSSLContext(Certificate cert, PrivateKey privateKey) throws APIManagementException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setKeyEntry("key", privateKey, null, new Certificate[]{cert});

            return SSLContexts.custom()
                    .loadKeyMaterial(keyStore, null)
//                  .loadTrustMaterial((chain, authType) -> true)
                    .loadTrustMaterial(new File(trustStoreLocation), trustStorePassword)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new APIManagementException(e);
        }
    }
}