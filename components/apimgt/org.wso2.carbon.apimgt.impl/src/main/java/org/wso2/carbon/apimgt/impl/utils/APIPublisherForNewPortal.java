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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIPublisherForNewPortal {

    private static final Log log = LogFactory.getLog(APIPublisherForNewPortal.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final char[] trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword").toCharArray();
    private static final String trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");
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
            log.error("Error while publishing API for new developer portal. Error: " + e.getMessage());
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
            unpublishAPI(baseUrl, api, orgId, sslsf);
        } catch (APIManagementException e) {
            log.error("Error while unpublishing API from new developer portal. Error: " + e.getMessage());
        }
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
            log.error("Failed to publish API " + apiTypeWrapper.getName() + " to the new portal. Status code: "
                    + statusCode);
        }
    }

    private static void updateAPI(String baseUrl, ApiTypeWrapper apiTypeWrapper, String tenantName, String orgId,
                                   SSLConnectionSocketFactory sslsf) throws APIManagementException {
        API api = apiTypeWrapper.getApi();
        String apiDefinition = ApisApiServiceImplUtils.getApiDefinition(api);
        String apiMetaData = getApiMetaData(tenantName, apiTypeWrapper);

        // TODO: THIS REST IS NOT FINALISED. WE NEED TO COME UP WITH SIMILAR REST FROM DEV PORTAL
        // TODO: THAT SHOULD RETURN 200 IF API IS AVAILABLE AND 404 IF API IS NOT AVAILABLE FOR GIVEN REFERENCE_ID
        int statusCodeOfAPIAvailability = getAPIByReferenceId(baseUrl + "/devportal/b2b/organizations/"
                + orgId + "/apis/" + apiTypeWrapper.getUuid(), sslsf);

        if (statusCodeOfAPIAvailability == 200) {
            // API is available, hence update API
            int statusCodeOfUpdatingAPI = updateApiMetadata(baseUrl + "/devportal/b2b/organizations/"
                            + orgId + "/apis/" + apiTypeWrapper.getUuid(),
                    apiMetaData, apiDefinition, sslsf);
            if (statusCodeOfUpdatingAPI == 200) {
                log.info("API " + apiTypeWrapper.getName() + " successfully updated in " + baseUrl);
            } else {
                log.error("Failed to update API " + apiTypeWrapper.getName() + " in the new portal. Status code: "
                        + statusCodeOfUpdatingAPI);
            }
        } else if (statusCodeOfAPIAvailability == 404) {
            // API is not available, hence publish API
            int statusCodeOfPublishingAPI = publishApiMetadata(baseUrl +
                            "/devportal/b2b/organizations/" + orgId + "/apis", apiMetaData, apiDefinition, sslsf);
            if (statusCodeOfPublishingAPI == 201) {
                log.info("API " + apiTypeWrapper.getName() + " successfully published to " + baseUrl);
            } else {
                log.error("Failed to publish API " + apiTypeWrapper.getName() + " to the new portal. Status code: "
                        + statusCodeOfPublishingAPI);
            }
        } else {
            log.error("Failed to find to update or publish API " + apiTypeWrapper.getName() +
                    " to the new portal. Status code: " + statusCodeOfAPIAvailability);
        }
    }

    private static void unpublishAPI(String baseUrl, API api, String orgId,
                                   SSLConnectionSocketFactory sslsf) throws APIManagementException {
        int statusCode = unpublishApi(
                baseUrl + "/devportal/b2b/organizations/" + orgId + "/apis/" + api.getId().getUUID(), sslsf);

        if (statusCode == 200) {
            log.info("API " + api.getId().getApiName() + " successfully unpublished from " + baseUrl);
        } else if (statusCode == 404) {
            log.warn("API " + api.getId().getApiName() + " is not available in "
                    + baseUrl + " .Hence API cannot get unpublished from " + baseUrl);
        } else {
            log.error("Failed to unpublish API " + api.getId().getApiName() + " from new portal. Status code: "
                    + statusCode);
        }
    }

    private static String fetchOrgIdOrCreateNew(String baseUrl, String tenantName, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        String orgId = getOrgId(tenantName, baseUrl, sslsf);

        if (orgId.isEmpty()) {
            String newOrgInfo = generateNewOrgInfoForDeveloperPortal(tenantName);
            orgId = createNewOrg(baseUrl + "/devportal/b2b/organizations", newOrgInfo, sslsf);
        }
        return orgId;
    }

    private static String getOrgId(String tenantName, String baseUrl, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        if (orgIdCache.containsKey(tenantName)) {
            // OrgID cache hit
            return orgIdCache.get(tenantName);
        }

        String orgId = getNewPortalOrgId(baseUrl + "/devportal/b2b/organizations/" + tenantName, sslsf);

        if (orgId != null && !orgId.isEmpty()) {
            orgIdCache.put(tenantName, orgId);
        }

        return orgId;
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

    public static List<Map<String, String>> convertToSubscriptionPolicies(Object[] tiers) {
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

    private static int getAPIByReferenceId(String apiUrl, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(apiUrl))) {
            return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new APIManagementException("Error retrieving organization ID: " + e.getMessage(), e);
        }
    }

    private static String getNewPortalOrgId(String apiUrl, SSLConnectionSocketFactory sslsf)
            throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(apiUrl))) {

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
            throw new APIManagementException("Error retrieving organization ID: " + e.getMessage(), e);
        }
    }

    public static int publishApiMetadata(String apiUrl, String apiMetadata, String apiDefinition,
                                         SSLConnectionSocketFactory sslsf) throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("apiMetadata", apiMetadata, ContentType.APPLICATION_JSON);
            builder.addBinaryBody("apiDefinition", apiDefinition.getBytes(), ContentType.APPLICATION_JSON, "apiDefinition.json");

            httpPost.setEntity(builder.build());

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return response.getStatusLine().getStatusCode();
            }
        } catch (IOException e) {
            throw new APIManagementException("Error sending API metadata and definition: " + e.getMessage(), e);
        }
    }

    public static int updateApiMetadata(String apiUrl, String apiMetadata, String apiDefinition,
                                         SSLConnectionSocketFactory sslsf) throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPut httpPut = new HttpPut(apiUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("apiMetadata", apiMetadata, ContentType.APPLICATION_JSON);
            builder.addBinaryBody("apiDefinition", apiDefinition.getBytes(), ContentType.APPLICATION_JSON, "apiDefinition.json");

            httpPut.setEntity(builder.build());

            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                return response.getStatusLine().getStatusCode();
            }
        } catch (IOException e) {
            throw new APIManagementException("Error updating API metadata and definition: " + e.getMessage(), e);
        }
    }

    public static int unpublishApi(String apiUrl, SSLConnectionSocketFactory sslsf) throws APIManagementException {
        try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
             CloseableHttpResponse response = httpClient.execute(new HttpDelete(apiUrl))) {
            return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new APIManagementException("Error removing API from new portal: " + e.getMessage(), e);
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

    // Certificate Related Methods

    private static SSLConnectionSocketFactory generateSSLSF() throws APIManagementException {
        // TODO: This section is not finalized yet
        Certificate cert = SigningUtil.getPublicCertificate(-1234);
        PrivateKey privateKey = SigningUtil.getSigningKey(-1234);

        return new SSLConnectionSocketFactory(configureSSLContext(cert, privateKey));
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
