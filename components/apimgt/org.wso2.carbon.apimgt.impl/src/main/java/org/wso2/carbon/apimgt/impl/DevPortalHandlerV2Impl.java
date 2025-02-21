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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.impl.dao.constants.DevPortalConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.DevPortalProcessingConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.dto.devportal.ApiMetaDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.ServerConfiguration;

/**
 * This class used to handle newly introduced 2025 version of Developer Portal's configuration with APIM.
 */
public class DevPortalHandlerV2Impl implements DevPortalHandler {

    private static final Log log = LogFactory.getLog(DevPortalHandlerV2Impl.class);
    private static final String baseUrl = getPortalURL();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, String> orgIdMap = new ConcurrentHashMap<>();

    private static final SSLConnectionSocketFactory sslsf;
    static {
        try {
            sslsf = generateSSLSF();
        } catch (APIManagementException e) {
            throw new RuntimeException("Failed to initialize SSLConnectionSocketFactory", e);
        }
    }

    private DevPortalHandlerV2Impl() {}

    private static class Holder {
        private static final DevPortalHandlerV2Impl INSTANCE = new DevPortalHandlerV2Impl();
    }

    public static DevPortalHandlerV2Impl getInstance() {
        return Holder.INSTANCE;
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
    public String publishAPIMetadata(String organization, API api) throws APIManagementException {
        String orgId = getOrgId(organization);
        String apiDefinition = getDefinitionForDevPortal(api);
        String apiMetaData = getApiMetaData(api);

        HttpResponseData responseData = apiPostAction(orgId, apiMetaData, apiDefinition);

        if (responseData.getStatusCode() == 201) {
            try {
                log.debug("API " + api.getId().getApiName() + " successfully published to " + baseUrl);
                Map<?, ?> jsonMap = objectMapper.readValue(responseData.getResponseBody(), Map.class);
                String apiId = (String) jsonMap.get("apiID");
                if (apiId == null || apiId.isEmpty()) {
                    throw new APIManagementException("Reference ID was not found in publication response");
                } else {
                    return apiId;
                }
            } catch (JsonProcessingException e) {
                throw new APIManagementException("Error while processing Json response: " + e.getMessage(), e);
            }
        } else {
            throw new APIManagementException("Failed to publish API " + api.getId().getApiName() + " to " + baseUrl
                    + ". " + "Status code: " + responseData.getStatusCode());
        }
    }

    @Override
    public void updateAPIMetadata(String organization, API api, String refId) throws APIManagementException {
        String apiDefinition = getDefinitionForDevPortal(api);
        String apiMetaData = getApiMetaData(api);
        String orgId = getOrgId(organization);
        HttpResponseData apiPutResponseData = apiPutAction(orgId, refId, apiDefinition, apiMetaData);
        if (apiPutResponseData.getStatusCode() == 200) {
            log.debug("API " + api.getId().getApiName() + " successfully updated in " + baseUrl);
        } else {
            throw new APIManagementException("Failed to update API " + api.getId().getApiName() + " in " + baseUrl +
                    ". Status code: " + apiPutResponseData.getStatusCode());
        }
    }

    @Override
    public void unpublishAPIMetadata(String organization, API api, String refId) throws APIManagementException {
        HttpResponseData responseData = apiDeleteAction(getOrgId(organization), refId);
        if (responseData.getStatusCode() == 200) {
            log.debug("API " + api.getId().getApiName() + " successfully unpublished from " + baseUrl);
        } else if (responseData.getStatusCode() == 404) {
            throw new APIManagementException("API " + api.getId().getApiName() + " is not available in "
                    + baseUrl + " .Hence API cannot get unpublished from " + baseUrl);
        } else {
            throw new APIManagementException("Failed to delete API " + api.getId().getApiName() + " from "
                    + baseUrl + ". Status code: " + responseData.getStatusCode());
        }
    }

    @Override
    public void publishAPIContent(String organization, String refId, InputStream content, String apiName)
            throws APIManagementException {
        String tempPath =
                System.getProperty(DevPortalConstants.JAVA_IO_TMPDIR) + File.separator + "publishing-api-themes";
        String tempFile = apiName + APIConstants.ZIP_FILE_EXTENSION;
        File apiThemeArchive = new File(tempPath, tempFile);
        try {
            FileUtils.copyInputStreamToFile(content, apiThemeArchive);
            String imageMetadata = imageMetadataGenerator(apiThemeArchive);
            String orgId = getOrgId(organization);
            HttpResponseData responseData = apiContentPutAction(orgId, refId, apiThemeArchive, imageMetadata);
            if (responseData.getStatusCode() == 201) {
                log.debug("Successfully published API content for API: " + apiName + " and organization: "
                        + organization);
            } else {
                throw new APIManagementException("Failed to publish API content for API: " + apiName
                        + " and organization: " + organization);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while processing Input stream: " + e.getMessage(), e);
        }
    }

    @Override
    public void unpublishAPIContent(String organization, String refId) throws APIManagementException {
        HttpResponseData responseData = apiContentDeleteAction(getOrgId(organization), refId);
        if (responseData.getStatusCode() == 200) {
            log.debug("Successfully un-published API content for API Reference ID: " + refId
                    + " and organization: " + organization);
        } else {
            throw new APIManagementException("Failed to un-publish API content for API Reference ID: "
                    + refId + " and organization: " + organization);
        }
    }

    @Override
    public void publishOrgContent(String organization, InputStream content) throws APIManagementException {
        String tempPath =
                System.getProperty(DevPortalConstants.JAVA_IO_TMPDIR) + File.separator + "publishing-org-themes";
        String tempFile = organization + APIConstants.ZIP_FILE_EXTENSION;
        File apiThemeArchive = new File(tempPath, tempFile);
        try {
            FileUtils.copyInputStreamToFile(content, apiThemeArchive);
            HttpResponseData responseData = orgContentPutAction(getOrgId(organization), apiThemeArchive);
            if (responseData.getStatusCode() == 201) {
                log.debug("Successfully published Organization content for organization: " + organization);
            } else {
                throw new APIManagementException("Failed to publish Organization content for organization: "
                        + organization);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while processing Input stream: " + e.getMessage(), e);
        }
    }

    @Override
    public void unpublishOrgContent(String organization) throws APIManagementException {
        HttpResponseData responseData = orgContentDeleteAction(getOrgId(organization));
        if (responseData.getStatusCode() == 200) {
            log.debug("Successfully un-published Organization content for organization: " + organization);
        } else {
            throw new APIManagementException("Failed to un-publish Organization content for organization: "
                    + organization);
        }
    }

    private static String getPortalURL() {
        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String propertyValue = apiManagerConfiguration.getFirstProperty(APIConstants.API_STORE_ACCESS_URL);
        return StringUtils.isNotEmpty(propertyValue) ? propertyValue : "";
    }

    private static String getDefinitionForDevPortal (API api) throws APIManagementException {
        String type = getType(api.getType());
        switch (type) {
            case "REST":
                return api.getSwaggerDefinition();
            case "AsyncAPI":
                return api.getAsyncApiDefinition();
            case "GraphQL":
                return api.getGraphQLSchema();
            case "SOAP":
                return "TODO: SOAP";
            default:
                throw new APIManagementException("Cannot find a definition for the given type");
        }
    }

    private static String getOrgId(String tenantName)
            throws APIManagementException {
        String orgId;
        orgId = orgIdMap.get(tenantName);
        if (orgId != null) {
            return orgId;
        }
        HttpResponseData responseData = orgGetAction(tenantName);
        try {
            if (responseData.getStatusCode() == 200) {
                Map<?, ?> jsonMap = objectMapper.readValue(responseData.getResponseBody(), Map.class);
                orgId = (String) jsonMap.get("orgId");
                if (orgId == null || orgId.isEmpty()) {
                    throw new APIManagementException("Org reference ID is not available for organization");
                }
            } else if (responseData.getStatusCode() == 404) {
                throw new APIManagementException("Organization ID not found for the specified organization. Please " +
                        "verify the organization name in the DevPortal. If it is not available, create the " +
                        "organization there with the specified name and retry.");

            } else {
                throw new APIManagementException("Something went wrong when retrieving Org reference ID");
            }
            orgIdMap.put(tenantName, orgId);
            return orgId;
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error while processing Json response: " + e.getMessage(), e);
        }
    }

    private static String getApiMetaData(API api) throws APIManagementException {
        ApiMetaDataDTO apiMetaDataDTO = new ApiMetaDataDTO();

        ApiMetaDataDTO.ApiInfo apiInfo = new ApiMetaDataDTO.ApiInfo();
        apiInfo.setReferenceID(Objects.toString(api.getId(), ""));
        apiInfo.setProvider("WSO2"); // DEV PORTAL expects WSO2 as Provider when API coming from WSO2 API Manager
        apiInfo.setApiName(Objects.toString(api.getId().getApiName(), ""));
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

    private static List<Map<String, String>> convertToSubscriptionPolicies(Object[] tiers) {
        List<Map<String, String>> subscriptionPolicies = new ArrayList<>();
        for (Object tier : tiers) {
            if (tier instanceof Tier) {
                Tier tierObject = (Tier) tier;
                String name = tierObject.getName();
                if (name != null) {
                    Map<String, String> policy = new HashMap<>();
                    policy.put("policyName", name);
                    subscriptionPolicies.add(policy);
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

    private static String imageMetadataGenerator(File zipFile) throws APIManagementException, IOException {
        File tempDirectory = null;
        try {
            tempDirectory = CommonUtil.createTempDirectory(null);
            String extractedFolderName = CommonUtil.extractArchive(zipFile, tempDirectory.getAbsolutePath());
            File imagesFolder = new File(tempDirectory, extractedFolderName + "/images");
            if (!imagesFolder.exists() || !imagesFolder.isDirectory()) {
                throw new APIManagementException("Images folder not found in ZIP");
            }

            String apiIconName = null;
            String apiHeroName = null;
            for (File file : Objects.requireNonNull(imagesFolder.listFiles())) {
                String fileName = file.getName().toLowerCase();
                if (fileName.startsWith("api-icon")) {
                    apiIconName = file.getName();
                } else if (fileName.startsWith("api-hero")) {
                    apiHeroName = file.getName();
                }
            }

            JSONObject jsonResponse = new JSONObject();
            if (apiIconName != null) {
                jsonResponse.put("api-icon", apiIconName);
            }
            if (apiHeroName != null) {
                jsonResponse.put("api-hero", apiHeroName);
            }

            return jsonResponse.toString();
        } catch (APIImportExportException e) {
            throw new APIManagementException(e);
        } finally {
            if (tempDirectory != null) {
                FileUtils.deleteDirectory(tempDirectory);
            }
        }
    }

    // HTTPS Request Related Methods

    private static synchronized CloseableHttpClient getHttpClient() {
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }

    private static HttpResponseData orgGetAction(String organization)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.ORG_URI + "/" + organization;
        try (CloseableHttpClient httpClient = getHttpClient()) {
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

    private static HttpResponseData apiDeleteAction(String orgId, String refId)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.API_URI + "/" + refId;

        try (CloseableHttpClient httpClient = getHttpClient()) {
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


    private static HttpResponseData apiPostAction(String orgId, String apiMetadata, String apiDefinition)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.API_URI;
        try (CloseableHttpClient httpClient = getHttpClient()) {
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

    private static HttpResponseData apiPutAction(String orgId, String refId, String apiDefinition,
                                                 String apiMetadata)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.API_URI + "/" + refId;
        try (CloseableHttpClient httpClient = getHttpClient()) {
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

    private static HttpResponseData orgContentPutAction(String orgId, File orgContent)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.LAYOUT_URI;
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpPut httpPut = new HttpPut(apiUrl);
            httpPut.setHeader("organization", orgId);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("file", new FileBody(orgContent, ContentType.create("application/zip"),
                    orgContent.getName()));
            httpPut.setEntity(builder.build());

            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while uploading organization content in " + baseUrl + ": "
                    + e.getMessage(), e);
        }
    }

    private static HttpResponseData orgContentDeleteAction(String orgId) throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.LAYOUT_URI;
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpDelete httpDelete = new HttpDelete(apiUrl);
            httpDelete.setHeader("organization", orgId);

            try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while deleting organization content in " + baseUrl + ": "
                    + e.getMessage(), e);
        }
    }

    private static HttpResponseData apiContentPutAction(String orgId, String refId, File apiContent, String imageMetadata)
            throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.API_URI + "/" + refId + DevPortalProcessingConstants.TEMPLATE_URI;
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpPut httpPut = new HttpPut(apiUrl);
            httpPut.setHeader("organization", orgId);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("apiContent", new FileBody(apiContent, ContentType.create("application/zip"),
                    apiContent.getName()));
            builder.addTextBody("imageMetadata", imageMetadata, ContentType.APPLICATION_JSON);

            httpPut.setEntity(builder.build());

            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while updating API content in " + baseUrl + ": "
                    + e.getMessage(), e);
        }
    }

    private static HttpResponseData apiContentDeleteAction(String orgId, String refId) throws APIManagementException {
        String apiUrl = baseUrl + DevPortalProcessingConstants.API_URI + "/" + refId
                + DevPortalProcessingConstants.TEMPLATE_URI;
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpDelete httpDelete = new HttpDelete(apiUrl);
            httpDelete.setHeader("organization", orgId);

            try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return new HttpResponseData(statusCode, responseBody);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while deleting API content in " + baseUrl + ": " + e.getMessage(), e);
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
}
