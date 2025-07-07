/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.GuardrailProviderService;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.api.dto.GuardrailProviderConfigurationDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * AWS Bedrock Guardrail Provider Service.
 * This service interacts with the AWS Bedrock Guardrails API to perform guardrail checks.
 */
public class AWSBedrockGuardrailProviderServiceImpl implements GuardrailProviderService {

    private String accessKey;
    private String secretKey;
    private String sessionToken;
    private String roleArn;
    private String roleRegion;
    private String roleExternalId;

    private long retrievalTimeout;
    private int maxRetryCount;
    private double retryProgressionFactor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(GuardrailProviderConfigurationDTO providerConfig) throws APIManagementException {
        if (providerConfig == null || providerConfig.getProperties() == null) {
            throw new APIManagementException("AWS Bedrock guardrail provider configuration not found");
        }

        accessKey = providerConfig.getProperties()
                .get(APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_ACCESS_KEY);
        secretKey = providerConfig.getProperties()
                .get(APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_SECRET_KEY);

        if (accessKey == null || secretKey == null) {
            throw new APIManagementException(
                    "Missing required AWS bedrock configuration: 'access_key', 'secret_key'");
        }

        sessionToken = providerConfig.getProperties()
                .get(APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_SESSION_TOKEN);
        roleArn = providerConfig.getProperties()
                .get(APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_ROLE_ARN);
        roleRegion = providerConfig.getProperties()
                .get(APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_ROLE_REGION);
        roleExternalId = providerConfig.getProperties()
                .get(APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_ROLE_EXTERNAL_ID);

        // Retry parameters
        try {
            retrievalTimeout = Long.parseLong(providerConfig.getProperties()
                    .getOrDefault(APIConstants.AI.RETRIEVAL_TIMEOUT, APIConstants.AI.DEFAULT_RETRIEVAL_TIMEOUT));
            maxRetryCount = Integer.parseInt(providerConfig.getProperties()
                    .getOrDefault(APIConstants.AI.RETRY_COUNT, APIConstants.AI.DEFAULT_RETRY_COUNT));
            retryProgressionFactor = Double.parseDouble(providerConfig.getProperties()
                    .getOrDefault(APIConstants.AI.RETRY_PROGRESSION_FACTOR,
                            APIConstants.AI.DEFAULT_RETRY_PROGRESSION_FACTOR));
        } catch (NumberFormatException e) {
            throw new APIManagementException("Invalid retry configuration provided: " +
                    "'retrieval_timeout', 'retry_count', 'retry_progression_factor'");
        }
    }

    @Override
    public String getType() {
        return APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_TYPE;
    }

    @Override
    public String callOut(Map<String, Object> callOutConfig) throws APIManagementException {
        String service = callOutConfig.getOrDefault(
                APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_CALLOUT_SERVICE, "").toString().trim();
        String region = callOutConfig.getOrDefault(
                APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_CALLOUT_SERVICE_REGION, "").toString().trim();
        String host = callOutConfig.getOrDefault(
                APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_CALLOUT_HOST, "").toString().trim();
        String uri = callOutConfig.getOrDefault(
                APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_CALLOUT_URI, "").toString().trim();
        String url = callOutConfig.getOrDefault(
                APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_CALLOUT_URL, "").toString().trim();
        String body = callOutConfig.getOrDefault(
                APIConstants.AI.GUARDRAIL_PROVIDER_AWSBEDROCK_CALLOUT_PAYLOAD, "").toString().trim();

        if ((StringUtils.isBlank(service) || StringUtils.isBlank(region) || StringUtils.isBlank(host)
                || StringUtils.isBlank(uri) || StringUtils.isBlank(url) || StringUtils.isBlank(body))) {
            throw new APIManagementException("Missing or invalid AWS Bedrock callout parameters: " +
                    "'service', 'guardrail_region', 'request_host', 'request_uri', 'request_url', 'request_payload'");
        }
        HttpClient httpClient = APIUtil.getHttpClient(url);
        HttpPost post = new HttpPost(url);
        post.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);

        try {
            // Generate AWS authentication headers
            Map<String, String> authHeaders;
            if (roleArn != null && !roleArn.isEmpty() && roleRegion != null && !roleRegion.isEmpty()) {
                // Generate AWS authentication headers using AssumeRole
                authHeaders = APIUtil.generateAWSSignatureUsingAssumeRole(
                        host, APIConstants.HTTP_POST, service, uri, "", body, accessKey,
                        secretKey, region, sessionToken, roleArn, roleRegion, roleExternalId
                );
            } else {
                authHeaders = APIUtil.generateAWSSignature(host, APIConstants.HTTP_POST, service, uri,
                        "", body, accessKey, secretKey, region, sessionToken
                );
            }
            for (Map.Entry<String, String> header : authHeaders.entrySet()) {
                post.setHeader(header.getKey(), header.getValue());
            }
            post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(
                    post, httpClient, retrievalTimeout, maxRetryCount, retryProgressionFactor)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode == HttpStatus.SC_OK) {
                    JsonNode root = objectMapper.readTree(responseBody);
                    return root.toString();
                } else {
                    throw new APIManagementException("Unexpected status code " + statusCode + ": " + responseBody);
                }
            }
        } catch (IOException e) {
            throw new APIManagementException("Error occurred while calling out to AWS Bedrock", e);
        }
    }
}
