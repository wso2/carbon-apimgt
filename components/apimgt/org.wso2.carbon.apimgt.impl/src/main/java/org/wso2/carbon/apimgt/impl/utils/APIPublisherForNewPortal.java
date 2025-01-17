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
import org.apache.http.HttpEntity;
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
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.PrivateKey;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.util.Map;

public class APIPublisherForNewPortal {
    private static final Log log = LogFactory.getLog(LifeCycleUtils.class);

    public static void publish(String tenantName, ApiTypeWrapper apiTypeWrapper)
            throws APIManagementException {
        String baseUrl = APIUtil.getNewPortalURL();
        // int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantName);
        // TODO: Finalised the right cert
        Certificate cert = SigningUtil.getPublicCertificate(-1234);
        PrivateKey privateKey = SigningUtil.getSigningKey(-1234);

        SSLContext sslContext = configureSSLContext(cert, privateKey);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

        String orgId = getNewPortalOrgId(baseUrl + "/devportal/b2b/organizations/" + tenantName, sslsf);

        if (orgId.isEmpty()){
            // Create a new org for the current tenant since no org in developer portal matches tenant
            String newOrgInfo = generateNewOrgInfoForDeveloperPortal(tenantName);
            orgId = createNewOrg(baseUrl + "/devportal/b2b/organizations", newOrgInfo, sslsf);
            if (orgId.isEmpty()) {
                log.error("Something went wrong while creating an org in new developer portal");
                return;
            }
        }
        API api = apiTypeWrapper.getApi();
        String apiDefinition = ApisApiServiceImplUtils.getApiDefinition(api);
        String apiMetaData = getApiMetaData(tenantName, apiTypeWrapper);

        int statusCode = publishApiMetadata(baseUrl + "/devportal/b2b/organizations/" + orgId + "/apis",
                apiMetaData, apiDefinition, sslsf);

        if (statusCode == 201) {
            log.info("The API " + apiTypeWrapper.getName() +
                    " has been successfully published to the NextGen Developer Portal at " + baseUrl + ".");
        } else {
            log.error("Failed to publish the API " + apiTypeWrapper.getName() +
                    " to the NextGen Developer Portal at " + baseUrl + ". Status code: " + statusCode);
        }
    }

    private static String getApiMetaData(String orgName, ApiTypeWrapper apiTypeWrapper) {
        API api = apiTypeWrapper.getApi();

        JSONObject apiInfo = new JSONObject();
        apiInfo.put("referenceID", apiTypeWrapper.getUuid() != null ? apiTypeWrapper.getUuid() : "");
        apiInfo.put("apiName", apiTypeWrapper.getName() != null ? apiTypeWrapper.getName() : "");
        apiInfo.put("orgName", orgName != null ? orgName : "");
        apiInfo.put("provider", api.getId().getProviderName() != null ? api.getId().getProviderName() : "");
        // TODO: Find What is the single category
        apiInfo.put("apiCategory", "");
        apiInfo.put("apiDescription", api.getDescription() != null ? api.getDescription() : "");
        apiInfo.put("visibility", api.getVisibility() != null ? api.getVisibility() : "");

        JSONArray visibleGroupsArray = new JSONArray();
        if (api.getVisibleRoles() != null) {
            String[] visibleRoles = api.getVisibleRoles().split(",");
            for (String role : visibleRoles) {
                visibleGroupsArray.add(role);
            }
        }
        apiInfo.put("visibleGroups", visibleGroupsArray);

        JSONObject owners = new JSONObject();
        owners.put("technicalOwner", api.getTechnicalOwner() != null ? api.getTechnicalOwner() : "");
        owners.put("technicalOwnerEmail", api.getTechnicalOwnerEmail() != null ? api.getTechnicalOwnerEmail() : "");
        owners.put("businessOwner", api.getBusinessOwner() != null ? api.getBusinessOwner() : "");
        owners.put("businessOwnerEmail", api.getBusinessOwnerEmail() != null ? api.getBusinessOwnerEmail() : "");

        apiInfo.put("owners", owners);
        apiInfo.put("apiVersion", api.getId().getVersion() != null ? api.getId().getVersion() : "");
        apiInfo.put("apiType", apiTypeWrapper.getType() != null ? apiTypeWrapper.getType() : "");

        JSONObject endPoints = new JSONObject();
        endPoints.put("sandboxURL", api.getApiExternalSandboxEndpoint() != null ?
                api.getApiExternalSandboxEndpoint() : "");
        endPoints.put("productionURL", api.getApiExternalProductionEndpoint() != null ?
                api.getApiExternalProductionEndpoint() : "");

        JSONObject response = new JSONObject();
        response.put("apiInfo", apiInfo);
        // TODO: Find where we can get policies for Apps
        response.put("subscriptionPolicies", new JSONArray());
        response.put("endPoints", endPoints);

        return response.toJSONString();
    }

    private static String generateNewOrgInfoForDeveloperPortal(String tenantName) {
        JSONObject orgInfo = new JSONObject();
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
                ObjectMapper objectMapper = new ObjectMapper();
                Map jsonMap = objectMapper.readValue(responseBody, Map.class);
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

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return response.getStatusLine().getStatusCode();
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while sending API metadata and definition: " + e.getMessage(), e);
        }
    }

    public static String createNewOrg(String apiUrl, String orgInfo, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("Content-Type", "application/json");
            StringEntity entity = new StringEntity(orgInfo, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                if (statusCode == 201) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<?, ?> jsonMap = objectMapper.readValue(responseBody, Map.class);
                    return (String) jsonMap.get("orgId");
                } else {
                    return "";
                }
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while creating an organization for tenant in new developer portal: "
                    + e.getMessage(), e);
        }
    }

    private static SSLContext configureSSLContext(Certificate cert, PrivateKey privateKey) throws APIManagementException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setKeyEntry("key", privateKey, null, new Certificate[]{cert});

            return SSLContexts.custom()
                    .loadKeyMaterial(keyStore, null)
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException
                 | UnrecoverableKeyException | KeyManagementException e) {
            throw new APIManagementException(e);
        }
    }
}
