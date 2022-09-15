/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.impl.restapi.publisher;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.google.gson.Gson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.*;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.dto.APIEndpointValidationDTO;
import org.wso2.carbon.apimgt.api.dto.EnvironmentPropertiesDTO;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.OAS2Parser;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.restapi.CommonUtils;
import org.wso2.carbon.apimgt.impl.restapi.Constants;
import org.wso2.carbon.apimgt.impl.restapi.PublisherUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.wsdl.util.SequenceUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import static org.wso2.carbon.apimgt.impl.restapi.CommonUtils.constructEndpointConfigForService;
import static org.wso2.carbon.apimgt.impl.restapi.CommonUtils.validateScopes;

public class ApisApiServiceImplUtils {

    private ApisApiServiceImplUtils() {
    }

    private static final Log log = LogFactory.getLog(ApisApiServiceImplUtils.class);
    private static final String HTTP_STATUS_LOG = "HTTP status ";
    private static final String AUDIT_ERROR = "Error while parsing the audit response";

    /**
     * @param content  Comment content
     * @param category Category
     * @param replyTo  Parent comment ID
     * @param username User commenting
     * @param apiId    API UUID
     * @return Comment
     */
    public static Comment createComment(String content, String category, String replyTo, String username, String apiId) {
        Comment comment = new Comment();
        comment.setText(content);
        comment.setCategory(category);
        comment.setParentCommentID(replyTo);
        comment.setEntryPoint("PUBLISHER");
        comment.setUser(username);
        comment.setApiId(apiId);

        return comment;
    }

    public static void checkCommentOwner(Comment comment, String username) throws APIManagementException {
        if (!comment.getUser().equals(username)) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.COMMENT_NO_PERMISSION, username, comment.getId()));
        }
    }

    /**
     * @param name Name of URI Template
     * @param verb HTTP verb
     * @return URITemplate
     */
    public static URITemplate createUriTemplate(String name, String verb) {
        URITemplate uriTemplate = new URITemplate();
        uriTemplate.setUriTemplate(name);
        uriTemplate.setHTTPVerb(verb.toUpperCase());
        uriTemplate.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
        uriTemplate.setThrottlingTier(APIConstants.UNLIMITED_TIER);

        return uriTemplate;
    }

    /**
     * @param api API
     * @return JSONObject with arns
     * @throws SdkClientException if AWSLambda SDK throws an error
     */
    public static JSONObject getAmazonResourceNames(API api) throws SdkClientException, APIManagementException {
        JSONObject arns = new JSONObject();
        String endpointConfigString = api.getEndpointConfig();
        if (StringUtils.isNotEmpty(endpointConfigString)) {
            JSONParser jsonParser = new JSONParser();
            JSONObject endpointConfig;
            try {
                endpointConfig = (JSONObject) jsonParser.parse(endpointConfigString);
            } catch (ParseException e) {
                throw new APIManagementException("Error while parsing endpoint config",
                        ExceptionCodes.JSON_PARSE_ERROR);
            }
            if (endpointConfig != null
                    && endpointConfig.containsKey(APIConstants.AMZN_ACCESS_KEY)
                    && endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY)
                    && endpointConfig.containsKey(APIConstants.AMZN_REGION)
                    && endpointConfig.containsKey(APIConstants.AMZN_ROLE_ARN)
                    && endpointConfig.containsKey(APIConstants.AMZN_ROLE_SESSION_NAME)
                    && endpointConfig.containsKey(APIConstants.AMZN_ROLE_REGION)) {
                String accessKey = (String) endpointConfig.get(APIConstants.AMZN_ACCESS_KEY);
                String secretKey = (String) endpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                String region = (String) endpointConfig.get(APIConstants.AMZN_REGION);
                String roleArn = (String) endpointConfig.get(APIConstants.AMZN_ROLE_ARN);
                String roleSessionName = (String) endpointConfig.get(APIConstants.AMZN_ROLE_SESSION_NAME);
                String roleRegion = (String) endpointConfig.get(APIConstants.AMZN_ROLE_REGION);
                try {
                    AWSLambda awsLambdaClient = getAWSLambdaClient(accessKey, secretKey, region,
                            roleArn, roleSessionName, roleRegion);
                    if (awsLambdaClient == null) {
                        return (JSONObject) Collections.emptyMap();
                    }
                    ListFunctionsResult listFunctionsResult = awsLambdaClient.listFunctions();
                    List<FunctionConfiguration> functionConfigurations = listFunctionsResult.getFunctions();
                    arns.put("count", functionConfigurations.size());
                    JSONArray list = new JSONArray();
                    for (FunctionConfiguration functionConfiguration : functionConfigurations) {
                        list.put(functionConfiguration.getFunctionArn());
                    }
                    arns.put("list", list);
                    return arns;
                } catch (CryptoException e) {
                    throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.ENDPOINT_CRYPTO_ERROR,
                            "Error while decrypting AWS Lambda secret key"));
                }
            }
        }
        return (JSONObject) Collections.emptyMap();
    }

    /**
     * @param accessKey       AWS access key
     * @param secretKey       AWS secret key
     * @param region          AWS region
     * @param roleArn         AWS role ARN
     * @param roleSessionName AWS role session name
     * @param roleRegion      AWS role region
     * @return AWS Lambda Client
     * @throws CryptoException when decoding secrets fail
     */
    private static AWSLambda getAWSLambdaClient(String accessKey, String secretKey, String region,
                                                String roleArn, String roleSessionName, String roleRegion) throws CryptoException {
        AWSLambda awsLambdaClient;
        if (StringUtils.isEmpty(accessKey) && StringUtils.isEmpty(secretKey)) {
            awsLambdaClient = getARNsWithIAMRole(roleArn, roleSessionName, roleRegion);
            return awsLambdaClient;
        } else if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey) &&
                StringUtils.isNotEmpty(region)) {
            awsLambdaClient = getARNsWithStoredCredentials(accessKey, secretKey, region,
                    roleArn, roleSessionName, roleRegion);
            return awsLambdaClient;
        } else {
            log.error("Missing AWS Credentials");
            return null;
        }
    }

    /**
     * @param roleArn         AWS role ARN
     * @param roleSessionName AWS role session name
     * @param roleRegion      AWS role region
     * @return AWS Lambda Client
     */
    private static AWSLambda getARNsWithIAMRole(String roleArn, String roleSessionName, String roleRegion) {
        AWSLambda awsLambdaClient;
        if (log.isDebugEnabled()) {
            log.debug("Using temporary credentials supplied by the IAM role attached to AWS " +
                    "instance");
        }
        if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                && StringUtils.isEmpty(roleRegion)) {
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .build();
            return awsLambdaClient;
        } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                && StringUtils.isNotEmpty(roleRegion)) {
            String stsRegion = String.valueOf(Regions.getCurrentRegion());
            AWSSecurityTokenService awsSTSClient;
            if (StringUtils.isEmpty(stsRegion)) {
                awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                        .build();
            } else {
                awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                        .withEndpointConfiguration(new EndpointConfiguration("https://sts."
                                + stsRegion + ".amazonaws.com", stsRegion))
                        .build();
            }
            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleArn)
                    .withRoleSessionName(roleSessionName);
            AssumeRoleResult assumeRoleResult = awsSTSClient.assumeRole(roleRequest);
            Credentials sessionCredentials = assumeRoleResult.getCredentials();
            BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                    sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
                    .withRegion(roleRegion)
                    .build();
            return awsLambdaClient;
        } else {
            log.error("Missing AWS STS configurations");
            return null;
        }
    }

    /**
     * @param accessKey       AWS access key
     * @param secretKey       AWS secret key
     * @param region          AWS region
     * @param roleArn         AWS role ARN
     * @param roleSessionName AWS role session name
     * @param roleRegion      AWS role region
     * @return AWS Lambda Client
     * @throws CryptoException when decoding secrets fail
     */
    private static AWSLambda getARNsWithStoredCredentials(String accessKey, String secretKey, String region,
                                                          String roleArn, String roleSessionName, String roleRegion)
            throws CryptoException {
        AWSLambda awsLambdaClient;
        if (log.isDebugEnabled()) {
            log.debug("Using user given stored credentials");
        }
        if (secretKey.length() == APIConstants.AWS_ENCRYPTED_SECRET_KEY_LENGTH) {
            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            secretKey = new String(cryptoUtil.base64DecodeAndDecrypt(secretKey),
                    StandardCharsets.UTF_8);
        }
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                && StringUtils.isEmpty(roleRegion)) {
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(region)
                    .build();
            return awsLambdaClient;
        } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                && StringUtils.isNotEmpty(roleRegion)) {
            AWSSecurityTokenService awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withEndpointConfiguration(new EndpointConfiguration("https://sts."
                            + region + ".amazonaws.com", region))
                    .build();
            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleArn)
                    .withRoleSessionName(roleSessionName);
            AssumeRoleResult assumeRoleResult = awsSTSClient.assumeRole(roleRequest);
            Credentials sessionCredentials = assumeRoleResult.getCredentials();
            BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                    sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
                    .withRegion(roleRegion)
                    .build();
            return awsLambdaClient;
        } else {
            log.error("Missing AWS STS configurations");
            return null;
        }
    }

    /**
     * @param api                         API
     * @param securityAuditPropertyObject audit security properties
     * @param apiDefinition               API definition
     * @param organization                user organization
     * @return JSONObject containing audit response
     * @throws APIManagementException when there's an unexpected response
     * @throws IOException            when http client fails
     */
    public static JSONObject getAuditReport(API api, JSONObject securityAuditPropertyObject,
                                            String apiDefinition, String organization)
            throws APIManagementException {
        boolean isDebugEnabled = log.isDebugEnabled();
        APIIdentifier apiIdentifier = api.getId();
        String apiToken = (String) securityAuditPropertyObject.get("apiToken");
        String collectionId = (String) securityAuditPropertyObject.get("collectionId");
        String baseUrl = (String) securityAuditPropertyObject.get("baseUrl");

        if (baseUrl == null) {
            baseUrl = APIConstants.BASE_AUDIT_URL;
        }
        // Retrieve the uuid from the database
        String auditUuid = ApiMgtDAO.getInstance().getAuditApiId(api.getUuid());
        if (auditUuid != null) {
            updateAuditApi(apiDefinition, apiToken, auditUuid, baseUrl, isDebugEnabled);
        } else {
            auditUuid = createAuditApi(collectionId, apiToken, apiIdentifier, apiDefinition, baseUrl,
                    isDebugEnabled, organization);
        }
        String getUrl = baseUrl + "/" + auditUuid + APIConstants.ASSESSMENT_REPORT;

        try {
            URL getReportUrl = new URL(getUrl);
            CloseableHttpClient getHttpClient = (CloseableHttpClient) APIUtil
                    .getHttpClient(getReportUrl.getPort(), getReportUrl.getProtocol());
            HttpGet httpGet = new HttpGet(getUrl);
            // Set the header properties of the request
            httpGet.setHeader(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            httpGet.setHeader(APIConstants.HEADER_API_TOKEN, apiToken);
            httpGet.setHeader(APIConstants.HEADER_USER_AGENT, APIConstants.USER_AGENT_APIM);
            // Code block for the processing of the response
            try (CloseableHttpResponse response = getHttpClient.execute(httpGet)) {
                if (isDebugEnabled) {
                    log.debug(HTTP_STATUS_LOG + response.getStatusLine().getStatusCode());
                }
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                    String inputLine;
                    StringBuilder responseString = new StringBuilder();

                    while ((inputLine = reader.readLine()) != null) {
                        responseString.append(inputLine);
                    }
                    reader.close();
                    JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
                    String report = responseJson.get(APIConstants.DATA).toString();
                    String grade = (String) ((JSONObject) ((JSONObject) responseJson.get(APIConstants.ATTR))
                            .get(APIConstants.DATA)).get(APIConstants.GRADE);
                    Integer numErrors = Integer.valueOf(
                            (String) ((JSONObject) ((JSONObject) responseJson.get(APIConstants.ATTR))
                                    .get(APIConstants.DATA)).get(APIConstants.NUM_ERRORS));
                    String decodedReport = new String(Base64Utils.decode(report), StandardCharsets.UTF_8);
                    JSONObject output = new JSONObject();
                    output.put("decodedReport", decodedReport);
                    output.put("grade", grade);
                    output.put("numErrors", numErrors);
                    output.put("auditUuid", auditUuid);
                    return output;
                }
            }
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), ExceptionCodes.INTERNAL_ERROR);
        } catch (ParseException e) {
            throw new APIManagementException(AUDIT_ERROR, ExceptionCodes.JSON_PARSE_ERROR);
        }
        return (JSONObject) Collections.emptyMap();
    }

    /**
     * Update API Definition before retrieving Security Audit Report
     *
     * @param apiDefinition  API Definition of API
     * @param apiToken       API Token to access Security Audit
     * @param auditUuid      Respective UUID of API in Security Audit
     * @param baseUrl        Base URL to communicate with Security Audit
     * @param isDebugEnabled Boolean whether debug is enabled
     * @throws APIManagementException In the event of unexpected response
     */
    private static void updateAuditApi(String apiDefinition, String apiToken, String auditUuid, String baseUrl,
                                       boolean isDebugEnabled)
            throws APIManagementException {
        // Set the property to be attached in the body of the request
        // Attach API Definition to property called specfile to be sent in the request
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("specfile", Base64Utils.encode(apiDefinition.getBytes(StandardCharsets.UTF_8)));
        // Logic for HTTP Request
        String putUrl = baseUrl + "/" + auditUuid;
        try {
            URL updateApiUrl = new URL(putUrl);
            try (CloseableHttpClient httpClient = (CloseableHttpClient) APIUtil
                    .getHttpClient(updateApiUrl.getPort(), updateApiUrl.getProtocol())) {
                HttpPut httpPut = new HttpPut(putUrl);
                // Set the header properties of the request
                httpPut.setHeader(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                httpPut.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                httpPut.setHeader(APIConstants.HEADER_API_TOKEN, apiToken);
                httpPut.setHeader(APIConstants.HEADER_USER_AGENT, APIConstants.USER_AGENT_APIM);
                httpPut.setEntity(new StringEntity(jsonBody.toJSONString()));
                // Code block for processing the response
                try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                    if (isDebugEnabled) {
                        log.debug(HTTP_STATUS_LOG + response.getStatusLine().getStatusCode());
                    }
                    if ((response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)) {
                        throw new APIManagementException(
                                "Error while sending data to the API Security Audit Feature. Found http status " +
                                        response.getStatusLine(),
                                ExceptionCodes.from(ExceptionCodes.AUDIT_SEND_FAILED,
                                        String.valueOf(response.getStatusLine())));
                    }
                } finally {
                    httpPut.releaseConnection();
                }
            }
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), ExceptionCodes.INTERNAL_ERROR);
        }

    }

    /**
     * Send API Definition to Security Audit for the first time
     *
     * @param collectionId   Collection ID in which the Definition should be sent to
     * @param apiToken       API Token to access Security Audit
     * @param apiIdentifier  API Identifier object
     * @param apiDefinition  API Definition of API
     * @param baseUrl        Base URL to communicate with Security Audit
     * @param isDebugEnabled Boolean whether debug is enabled
     * @param organization   Organization
     * @return String UUID of API in Security Audit
     * @throws APIManagementException In the event of unexpected response
     */
    private static String createAuditApi(String collectionId, String apiToken, APIIdentifier apiIdentifier,
                                         String apiDefinition, String baseUrl, boolean isDebugEnabled, String organization)
            throws APIManagementException {
        HttpURLConnection httpConn;
        OutputStream outputStream;
        String auditUuid = null;
        try {
            URL url = new URL(baseUrl);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true); // indicates POST method
            httpConn.setDoInput(true);
            httpConn.setRequestProperty(APIConstants.HEADER_CONTENT_TYPE,
                    APIConstants.MULTIPART_CONTENT_TYPE + APIConstants.MULTIPART_FORM_BOUNDARY);
            httpConn.setRequestProperty(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            httpConn.setRequestProperty(APIConstants.HEADER_API_TOKEN, apiToken);
            httpConn.setRequestProperty(APIConstants.HEADER_USER_AGENT, APIConstants.USER_AGENT_APIM);
            outputStream = httpConn.getOutputStream();
            writeAuditResponse(outputStream, apiIdentifier, apiDefinition, collectionId);
            // Checks server's status code first
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                if (isDebugEnabled) {
                    log.debug(HTTP_STATUS_LOG + status);
                }
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(httpConn.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder responseString = new StringBuilder();

                while ((inputLine = reader.readLine()) != null) {
                    responseString.append(inputLine);
                }
                reader.close();
                httpConn.disconnect();
                JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
                auditUuid = (String) ((JSONObject) responseJson.get(APIConstants.DESC)).get(APIConstants.ID);
                ApiMgtDAO.getInstance().addAuditApiMapping(apiIdentifier, auditUuid, organization);
            } else {
                handleAuditCreateError(httpConn);
            }
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), ExceptionCodes.INTERNAL_ERROR);
        } catch (ParseException e) {
            throw new APIManagementException(AUDIT_ERROR, ExceptionCodes.JSON_PARSE_ERROR);
        }
        return auditUuid;
    }

    /**
     * @param outputStream  HTTP output stream
     * @param apiIdentifier API Identifier object
     * @param apiDefinition API Definition of API
     * @param collectionId  Collection ID in which the Definition should be sent to
     */
    private static void writeAuditResponse(OutputStream outputStream, APIIdentifier apiIdentifier,
                                           String apiDefinition, String collectionId) {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
        // Name property
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY).append(APIConstants.MULTIPART_LINE_FEED)
                .append("Content-Disposition: form-data; name=\"name\"")
                .append(APIConstants.MULTIPART_LINE_FEED).append(APIConstants.MULTIPART_LINE_FEED)
                .append(apiIdentifier.getApiName()).append(APIConstants.MULTIPART_LINE_FEED);
        writer.flush();
        // Specfile property
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY).append(APIConstants.MULTIPART_LINE_FEED)
                .append("Content-Disposition: form-data; name=\"specfile\"; filename=\"swagger.json\"")
                .append(APIConstants.MULTIPART_LINE_FEED)
                .append(APIConstants.HEADER_CONTENT_TYPE + ": " + APIConstants.APPLICATION_JSON_MEDIA_TYPE)
                .append(APIConstants.MULTIPART_LINE_FEED).append(APIConstants.MULTIPART_LINE_FEED)
                .append(apiDefinition).append(APIConstants.MULTIPART_LINE_FEED);
        writer.flush();
        // CollectionID property
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY).append(APIConstants.MULTIPART_LINE_FEED)
                .append("Content-Disposition: form-data; name=\"cid\"").append(APIConstants.MULTIPART_LINE_FEED)
                .append(APIConstants.MULTIPART_LINE_FEED).append(collectionId)
                .append(APIConstants.MULTIPART_LINE_FEED);
        writer.flush();
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY + "--")
                .append(APIConstants.MULTIPART_LINE_FEED);
        writer.close();
    }

    private static void handleAuditCreateError(HttpURLConnection httpConn)
            throws IOException, ParseException, APIManagementException {
        if (httpConn.getErrorStream() != null) {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder responseString = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                responseString.append(inputLine);
            }
            reader.close();
            httpConn.disconnect();
            JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
            String errorMessage = httpConn.getResponseMessage();
            if (responseJson.containsKey("message")) {
                errorMessage = (String) responseJson.get("message");
            }
            throw new APIManagementException(
                    "Error while retrieving data for the API Security Audit Report. Found http status: " +
                            httpConn.getResponseCode() + " - " + errorMessage,
                    ExceptionCodes.AUDIT_RETRIEVE_FAILED);
        } else {
            throw new APIManagementException(
                    "Error while retrieving data for the API Security Audit Report. Found http status: " +
                            httpConn.getResponseCode() + " - " + httpConn.getResponseMessage(),
                    ExceptionCodes.AUDIT_RETRIEVE_FAILED);
        }
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @throws APIManagementException when prerequisites for API delete are not met
     */
    public static void deleteAPI(String apiId, String organization) throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        //check if the API has subscriptions
        //Todo : need to optimize this check. This method seems too costly to check if subscription exists
        List<SubscribedAPI> apiUsages = apiProvider.getAPIUsageByAPIId(apiId, organization);
        if (apiUsages != null && !apiUsages.isEmpty()) {
            throw new APIManagementException("Cannot remove the API "
                    + apiId + " as active subscriptions exist", ExceptionCodes.API_DELETE_FAILED_SUBSCRIPTIONS);
        }
        List<APIResource> usedProductResources = apiProvider.getUsedProductResources(apiId);

        if (!usedProductResources.isEmpty()) {
            throw new APIManagementException("Cannot remove the API because following resource paths " +
                    usedProductResources.toString() + " are used by one or more API Products",
                    ExceptionCodes.API_DELETE_API_PRODUCT_USED_RESOURCES);
        }

        // Delete the API
        apiProvider.deleteAPI(apiId, organization);
    }

    /**
     * Send HTTP HEAD request to test the endpoint url
     *
     * @param urlVal url for which the HEAD request is sent
     * @return APIEndpointValidationDTO Response DTO containing validity information of the HEAD request made
     * to test the endpoint url
     */
    public static APIEndpointValidationDTO sendHttpHEADRequest(String urlVal)
            throws APIManagementException, MalformedURLException {
        URL url;
        try {
            url = new URL(urlVal);
        } catch (MalformedURLException e) {
            throw new APIManagementException("URL is malformed",
                    e, ExceptionCodes.from(ExceptionCodes.URI_PARSE_ERROR, "Malformed url"));
        }
        if (url.getProtocol().matches("https")) {
            ServerConfiguration serverConfig = CarbonUtils.getServerConfiguration();
            String trustStorePath = serverConfig.getFirstProperty("Security.TrustStore.Location");
            String trustStorePassword = serverConfig.getFirstProperty("Security.TrustStore.Password");
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

            String keyStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
            String keyStoreType = serverConfig.getFirstProperty("Security.KeyStore.Type");
            String keyStorePassword = serverConfig.getFirstProperty("Security.KeyStore.Password");
            System.setProperty("javax.net.ssl.keyStoreType", keyStoreType);
            System.setProperty("javax.net.ssl.keyStore", keyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        }

        APIEndpointValidationDTO apiEndpointValidationDTO = new APIEndpointValidationDTO();
        org.apache.http.client.HttpClient client = APIUtil.getHttpClient(urlVal);
        HttpHead method = new HttpHead(urlVal);

        try {
            HttpResponse response = client.execute(method);
            apiEndpointValidationDTO.setStatusCode(response.getStatusLine().getStatusCode());
            apiEndpointValidationDTO.setStatusMessage(HttpStatus.getStatusText(response.getStatusLine().getStatusCode()));
        } catch (UnknownHostException e) {
            log.error("UnknownHostException occurred while sending the HEAD request to the given endpoint url:", e);
            apiEndpointValidationDTO.setError("Unknown Host");
        } catch (IOException e) {
            log.error("Error occurred while sending the HEAD request to the given endpoint url:", e);
            apiEndpointValidationDTO.setError("Connection error");
        } finally {
            method.releaseConnection();
        }
        return apiEndpointValidationDTO;
    }

    /**
     * @param url           URL of the OpenAPI definition
     * @param inputStream   OpenAPI definition file
     * @param apiDefinition OpenAPI definition
     * @param fileName      Filename of the definition file
     * @param returnContent Whether to return json or not
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException when file parsing fails
     */
    public static APIDefinitionValidationResponse validateOpenAPIDefinition(String url, InputStream inputStream,
                                                                            String apiDefinition, String fileName,
                                                                            boolean returnContent)
            throws APIManagementException {
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (url != null) {
            validationResponse = OASParserUtil.validateAPIDefinitionByURL(url, returnContent);
        } else if (inputStream != null) {
            try {
                if (fileName != null) {
                    if (fileName.endsWith(".zip")) {
                        validationResponse =
                                OASParserUtil.extractAndValidateOpenAPIArchive(inputStream, returnContent);
                    } else {
                        String openAPIContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                    }
                } else {
                    String openAPIContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                }
            } catch (IOException e) {
                throw new APIManagementException("Error while processing the file input",
                        e, ExceptionCodes.from(ExceptionCodes.OPENAPI_PARSE_EXCEPTION));
            }
        } else if (apiDefinition != null) {
            validationResponse = OASParserUtil.validateAPIDefinition(apiDefinition, returnContent);
        }

        return validationResponse;
    }

    /**
     * @param apiToAdd           API which will be added
     * @param apiProvider        API Provider Impl
     * @param service            ServiceCatalog service
     * @param validationResponse OpenAPI defnition validation response
     * @param isServiceAPI       whether the API is created from a service
     * @param syncOperations     sync all API operations
     * @throws APIManagementException when scope validation or OpenAPI parsing fails
     */
    public static API importAPIDefinition(API apiToAdd, APIProvider apiProvider, String organization,
                                          ServiceEntry service, APIDefinitionValidationResponse validationResponse,
                                          boolean isServiceAPI, boolean syncOperations)
            throws APIManagementException {
        String username = CommonUtils.getLoggedInUsername();
        if (isServiceAPI) {
            apiToAdd.setServiceInfo("key", service.getServiceKey());
            apiToAdd.setServiceInfo("md5", service.getMd5());
            apiToAdd.setEndpointConfig(constructEndpointConfigForService(service
                    .getServiceUrl(), null));
        }
        APIDefinition apiDefinition = validationResponse.getParser();
        SwaggerData swaggerData;
        String definitionToAdd = validationResponse.getJsonContent();
        if (syncOperations) {
            validateScopes(apiToAdd, apiProvider, username);
            swaggerData = new SwaggerData(apiToAdd);
            definitionToAdd = apiDefinition.populateCustomManagementInfo(definitionToAdd, swaggerData);
        }
        definitionToAdd = OASParserUtil.preProcess(definitionToAdd);
        Set<URITemplate> uriTemplates = apiDefinition.getURITemplates(definitionToAdd);
        Set<Scope> scopes = apiDefinition.getScopes(definitionToAdd);
        apiToAdd.setUriTemplates(uriTemplates);
        apiToAdd.setScopes(scopes);
        //Set extensions from API definition to API object
        apiToAdd = OASParserUtil.setExtensionsToAPI(definitionToAdd, apiToAdd);
        if (!syncOperations) {
            validateScopes(apiToAdd, apiProvider, username);
            swaggerData = new SwaggerData(apiToAdd);
            definitionToAdd = apiDefinition
                    .populateCustomManagementInfo(validationResponse.getJsonContent(), swaggerData);
        }

        // adding the definition
        apiToAdd.setSwaggerDefinition(definitionToAdd);

        API addedAPI = apiProvider.addAPI(apiToAdd);
        // retrieving the added API for returning as the response
        // this would provide the updated templates
        addedAPI = apiProvider.getAPIbyUUID(addedAPI.getUuid(), organization);

        return addedAPI;
    }

    /**
     * @param api API
     * @param soapOperation SOAP Operation
     * @return SOAP API Definition
     * @throws APIManagementException if an error occurred while parsing string to JSON Object
     */
    public static String generateSOAPAPIDefinition(API api, String soapOperation) throws APIManagementException {

        APIDefinition oasParser = new OAS2Parser();
        SwaggerData swaggerData = new SwaggerData(api);
        String apiDefinition = oasParser.generateAPIDefinition(swaggerData);
        JSONParser jsonParser = new JSONParser();
        JSONObject apiJson;
        JSONObject paths;
        try {
            apiJson = (JSONObject) jsonParser.parse(apiDefinition);
            paths = (JSONObject) jsonParser.parse(soapOperation);
            apiJson.replace("paths", paths);
            return apiJson.toJSONString();
        } catch (ParseException e) {
            throw new APIManagementException("Error while parsing the api definition.", e);
        }
    }

    /**
     * @param fileInputStream File input stream for the WSDL file
     * @param url URL
     * @param wsdlArchiveExtractedPath Path to WSDL extracted directory
     * @param filename File Name
     * @return Swagger string
     * @throws APIManagementException If the WSDL file not supported
     * @throws IOException If error occurred in converting InputStream to a byte array
     */
    public static String getSwaggerString(InputStream fileInputStream, String url, String wsdlArchiveExtractedPath,
                                          String filename) throws APIManagementException, IOException {

        String swaggerStr = "";
        if (StringUtils.isNotBlank(url)) {
            swaggerStr = SOAPOperationBindingUtils.getSoapOperationMappingForUrl(url);
        } else if (fileInputStream != null) {
            if (filename.endsWith(".zip")) {
                swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(wsdlArchiveExtractedPath);
            } else if (filename.endsWith(".wsdl")) {
                byte[] wsdlContent = APIUtil.toByteArray(fileInputStream);
                swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(wsdlContent);
            } else {
                throw new APIManagementException(ExceptionCodes.UNSUPPORTED_WSDL_FILE_EXTENSION);
            }
        }
        return swaggerStr;
    }

    /**
     * @param wsdlInputStream WSDL file input stream
     * @param contentType content type of the wsdl
     * @return Resource file WSDL
     */
    public static ResourceFile getWSDLResource(InputStream wsdlInputStream, String contentType) {

        ResourceFile wsdlResource;
        if (org.wso2.carbon.apimgt.impl.APIConstants.APPLICATION_ZIP.equals(contentType) ||
                org.wso2.carbon.apimgt.impl.APIConstants.APPLICATION_X_ZIP_COMPRESSED.equals(contentType)) {
            wsdlResource = new ResourceFile(wsdlInputStream, APIConstants.APPLICATION_ZIP);
        } else {
            wsdlResource = new ResourceFile(wsdlInputStream, contentType);
        }
        return wsdlResource;
    }

    /**
     * @param api API
     * @return API definition
     * @throws APIManagementException If any error occurred in generating API definition from swagger data
     */
    public static String getApiDefinition(API api) throws APIManagementException {

        APIDefinition parser = new OAS3Parser();
        SwaggerData swaggerData = new SwaggerData(api);
        return parser.generateAPIDefinition(swaggerData);
    }

    /**
     * @param apiPolicies Policy names applied to the API
     * @param availableThrottlingPolicyList All available policies
     * @return Filtered API policy list which are applied to the API
     */
    public static List<Tier> filterAPIThrottlingPolicies(List<String> apiPolicies, List<Tier> availableThrottlingPolicyList) {

        List<Tier> apiThrottlingPolicies = new ArrayList<>();
        if (apiPolicies != null && !apiPolicies.isEmpty()) {
            for (Tier tier : availableThrottlingPolicyList) {
                if (apiPolicies.contains(tier.getName())) {
                    apiThrottlingPolicies.add(tier);
                }
            }
        }
        return apiThrottlingPolicies;
    }

    /**
     * @param deploymentStatus Deployment status [deployed:true / deployed:false]
     * @param apiRevisions API revisions list
     * @return Filtered API revisions according to the deploymentStatus
     */
    public static List<APIRevision> filterAPIRevisionsByDeploymentStatus(String deploymentStatus, List<APIRevision> apiRevisions) {

        if ("deployed:true".equalsIgnoreCase(deploymentStatus)) {
            List<APIRevision> apiDeployedRevisions = new ArrayList<>();
            for (APIRevision apiRevision : apiRevisions) {
                if (!apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiDeployedRevisions.add(apiRevision);
                }
            }
            return apiDeployedRevisions;
        } else if ("deployed:false".equalsIgnoreCase(deploymentStatus)) {
            List<APIRevision> apiNotDeployedRevisions = new ArrayList<>();
            for (APIRevision apiRevision : apiRevisions) {
                if (apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiNotDeployedRevisions.add(apiRevision);
                }
            }
            return apiNotDeployedRevisions;
        }
        return apiRevisions;
    }

    /**
     * @param revisionId Revision ID
     * @param environments Environments of the organization
     * @param environment Selected environment
     * @param displayOnDevportal Enable display on Developer Portal
     * @param vhost Virtual Host of the revision deployment
     * @param mandatoryVHOST Is vhost mandatory in this validation
     * @return Created {@link APIRevisionDeployment} after validations
     * @throws APIManagementException if any validation fails
     */
    public static APIRevisionDeployment mapAPIRevisionDeploymentWithValidation(String revisionId, Map<String, Environment> environments,
                                                                               String environment, Boolean displayOnDevportal,
                                                                               String vhost, boolean mandatoryVHOST)
            throws APIManagementException {

        if (environments.get(environment) == null) {
            final String errorMessage = "Gateway environment not found: " + environment;
            throw new APIManagementException(errorMessage, ExceptionCodes.from(
                    ExceptionCodes.INVALID_GATEWAY_ENVIRONMENT, String.format("name '%s'", environment)));

        }
        if (mandatoryVHOST && StringUtils.isEmpty(vhost)) {
            // vhost is only required when deploying a revision, not required when un-deploying a revision
            // since the same scheme 'APIRevisionDeployment' is used for deploy and undeploy, handle it here.
            throw new APIManagementException("Required field 'vhost' not found in deployment",
                    ExceptionCodes.GATEWAY_ENVIRONMENT_VHOST_NOT_PROVIDED);
        }
        return mapApiRevisionDeployment(revisionId, vhost, displayOnDevportal, environment);
    }

    /**
     * @param revisionId Revision ID
     * @param vhost Virtual Host
     * @param displayOnDevportal Enable displaying on Developer Portal
     * @param deployment Deployment
     * @return Mapped {@link APIRevisionDeployment}
     */
    public static APIRevisionDeployment mapApiRevisionDeployment(String revisionId, String vhost, Boolean displayOnDevportal,
                                                                 String deployment) {

        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
        apiRevisionDeployment.setRevisionUUID(revisionId);
        apiRevisionDeployment.setDeployment(deployment);
        apiRevisionDeployment.setVhost(vhost);
        apiRevisionDeployment.setDisplayOnDevportal(displayOnDevportal);
        return apiRevisionDeployment;
    }

    /**
     * @param deploymentId Deployment ID
     * @return Deployment name decoded from the deploymentId
     * @throws APIMgtResourceNotFoundException If invalid or null deploymentId
     */
    public static String getDecodedDeploymentName(String deploymentId) throws APIMgtResourceNotFoundException {

        String decodedDeploymentName;
        if (deploymentId != null) {
            try {
                decodedDeploymentName = new String(Base64.getUrlDecoder().decode(deploymentId),
                        StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new APIMgtResourceNotFoundException("deployment with " + deploymentId +
                        " not found", ExceptionCodes.from(ExceptionCodes.EXISTING_DEPLOYMENT_NOT_FOUND,
                        deploymentId));
            }
        } else {
            throw new APIMgtResourceNotFoundException("deployment id not found",
                    ExceptionCodes.from(ExceptionCodes.DEPLOYMENT_ID_NOT_FOUND));
        }
        return decodedDeploymentName;
    }

    /**
     * @param fileInputStream API spec file input stream
     * @param isServiceAPI Is service API
     * @param fileName File name
     * @return Schema
     * @throws APIManagementException if error while reading the spec file contents
     */
    public static String getSchemaToBeValidated(InputStream fileInputStream, Boolean isServiceAPI, String fileName)
            throws APIManagementException {

        String schemaToBeValidated = null;
        if (Boolean.TRUE.equals(isServiceAPI) || fileName.endsWith(APIConstants.YAML_FILE_EXTENSION) || fileName
                .endsWith(APIConstants.YML_FILE_EXTENSION)) {
            //convert .yml or .yaml to JSON for validation
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            try {
                Object obj = yamlReader.readValue(fileInputStream, Object.class);
                ObjectMapper jsonWriter = new ObjectMapper();
                schemaToBeValidated = jsonWriter.writeValueAsString(obj);
            } catch (IOException e) {
                throw new APIManagementException("Error while reading file content", e,
                        ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
            }
        } else if (fileName.endsWith(APIConstants.JSON_FILE_EXTENSION)) {
            //continue with .json
            JSONTokener jsonDataFile = new JSONTokener(fileInputStream);
            schemaToBeValidated = new org.json.JSONObject(jsonDataFile).toString();
        }
        return schemaToBeValidated;
    }

    /**
     * @param environmentPropertiesMap Environment Properties Map
     * @return {@link EnvironmentPropertiesDTO} mapped from the properties in the environmentPropertiesMap
     * @throws APIManagementException If error in converting environmentPropertiesMap to {@link EnvironmentPropertiesDTO}
     */
    public static EnvironmentPropertiesDTO generateEnvironmentPropertiesDTO(Map<String, String> environmentPropertiesMap)
            throws APIManagementException {

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.convertValue(environmentPropertiesMap, new TypeReference<EnvironmentPropertiesDTO>() {
            });
        } catch (IllegalArgumentException e) {
            String errorMessage = "Possible keys are productionEndpoint,sandboxEndpoint";
            throw new APIManagementException(e.getMessage(),
                    ExceptionCodes.from(ExceptionCodes.INVALID_ENV_API_PROP_CONFIG, errorMessage));
        }
    }

    /**
     * @param service service entry
     * @return service info JSON Object
     */
    public static JSONObject getServiceInfo(ServiceEntry service) {

        JSONObject serviceInfo = new JSONObject();
        serviceInfo.put("name", service.getName());
        serviceInfo.put("version", service.getVersion());
        serviceInfo.put("key", service.getServiceKey());
        serviceInfo.put("md5", service.getMd5());
        return serviceInfo;
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return A list of API resource mediation policies with mock scripts
     * @throws APIManagementException when an internal errors occurs
     */
    public static List<APIResourceMediationPolicy> generateMockScripts(String apiId, String organization)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        CommonUtils.validateAPIExistence(apiId);
        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        Map<String, Object> examples = OASParserUtil.generateExamples(apiDefinition);

        return (List<APIResourceMediationPolicy>) examples.get(APIConstants.MOCK_GEN_POLICY_LIST);
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return Monetized policies to plan mapping
     * @throws APIManagementException when an internal error occurs
     */
    public static Map<String, String> getAPIMonetization(String apiId, String organization) throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        CommonUtils.validateAPIExistence(apiId);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        try {
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            return monetizationImplementation.getMonetizedPoliciesToPlanMapping(api);
        } catch (MonetizationException e) {
            throw new APIManagementException("Error occurred while getting the Monetization mappings for API "
                    + api.getId().getApiName(), e, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * @param apiId                  API UUID
     * @param organization           Tenant organization
     * @param monetizationEnabled    Whether to enable or disable monetization
     * @param monetizationProperties Monetization properties map
     * @return true if monetization state change is successful
     * @throws APIManagementException when a monetization related error occurs
     */
    public static boolean addAPIMonetization(String apiId, String organization,
                                             boolean monetizationEnabled, Map<String, String> monetizationProperties)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        if (!APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus())) {
            String errorMessage = "API " + api.getId().getApiName() +
                    " should be in published state to configure monetization.";
            throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_API_STATE_MONETIZATION);
        }
        //set the monetization status
        api.setMonetizationEnabled(monetizationEnabled);
        //clear the existing properties related to monetization
        api.getMonetizationProperties().clear();
        for (Map.Entry<String, String> currentEntry : monetizationProperties.entrySet()) {
            api.addMonetizationProperty(currentEntry.getKey(), currentEntry.getValue());
        }

        Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
        HashMap<String, String> monetizationDataMap = new Gson().fromJson(api.getMonetizationProperties().toString(),
                HashMap.class);
        if (MapUtils.isEmpty(monetizationDataMap)) {
            String errorMessage = "Monetization is not configured. Monetization data is empty for "
                    + api.getId().getApiName();
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        boolean isMonetizationStateChangeSuccessful = false;
        try {
            if (monetizationEnabled) {
                isMonetizationStateChangeSuccessful = monetizationImplementation.enableMonetization
                        (organization, api, monetizationDataMap);
            } else {
                isMonetizationStateChangeSuccessful = monetizationImplementation.disableMonetization
                        (organization, api, monetizationDataMap);
            }
        } catch (MonetizationException e) {
            String errorMessage = "Error while changing monetization status for API ID : " + apiId;
            throw new APIManagementException(errorMessage, e, ExceptionCodes.INTERNAL_ERROR);
        }
        if (isMonetizationStateChangeSuccessful) {
            apiProvider.configureMonetizationInAPIArtifact(api);
            return true;
        } else {
            throw new APIManagementException("Unable to change monetization status for API : " + apiId,
                    ExceptionCodes.from(ExceptionCodes.MONETIZATION_STATE_CHANGE_FAILED,
                            String.valueOf(monetizationEnabled)));
        }
    }

    /**
     * @param policySpecification Operation policy specification
     * @param operationPolicyData Operation policy metadata
     * @param apiId               API UUID
     * @param organization        Tenant organization
     * @return Created policy ID
     * @throws APIManagementException when adding an operation policy fails
     */
    public static String addAPISpecificOperationPolicy(OperationPolicySpecification policySpecification,
                                                       OperationPolicyData operationPolicyData,
                                                       String apiId, String organization)
            throws APIManagementException {
        String policyId;
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        OperationPolicyData existingPolicy =
                apiProvider.getAPISpecificOperationPolicyByPolicyName(policySpecification.getName(),
                        policySpecification.getVersion(), apiId, null, organization, false);
        if (existingPolicy == null) {
            policyId = apiProvider.addAPISpecificOperationPolicy(apiId, operationPolicyData, organization);
            if (log.isDebugEnabled()) {
                log.debug("An API specific operation policy has been added for the API " + apiId);
            }
        } else {
            throw new APIManagementException("An API specific operation policy found for the same name.",
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_ALREADY_EXISTS,
                            policySpecification.getName(), policySpecification.getVersion()));
        }
        return policyId;
    }

    /**
     * @param operationPolicyId Operation policy ID
     * @param apiId             API UUID
     * @param organization      Tenant organization
     * @throws APIManagementException when deleting API specific operation policy fails
     */
    public static void deleteAPISpecificOperationPolicyByPolicyId(String operationPolicyId, String apiId,
                                                                  String organization)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        OperationPolicyData existingPolicy = apiProvider
                .getAPISpecificOperationPolicyByPolicyId(operationPolicyId, apiId, organization, false);
        if (existingPolicy != null) {
            apiProvider.deleteOperationPolicyById(operationPolicyId, organization);

            if (log.isDebugEnabled()) {
                log.debug("The operation policy " + operationPolicyId + " has been deleted from the the API "
                        + apiId);
            }
        } else {
            throw new APIManagementException("Couldn't retrieve an existing operation policy with ID: "
                    + operationPolicyId + " for API " + apiId,
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, operationPolicyId));
        }
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @param sequenceType Sequence type
     * @param resourcePath Resource path
     * @param verb         HTTP verb
     * @return Resource policy
     * @throws APIManagementException when getting resource policy fails
     */
    public static String getAPIResourcePolicies(String apiId, String organization,
                                                String sequenceType, String resourcePath, String verb)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(apiId, organization);
        CommonUtils.checkAPIType(APIConstants.API_TYPE_SOAPTOREST, api.getType());
        if (StringUtils.isEmpty(sequenceType) || !(Constants.IN_SEQUENCE.equals(sequenceType)
                || Constants.OUT_SEQUENCE.equals(sequenceType))) {
            throw new APIManagementException("Sequence type should be either of the values from 'in' or 'out'",
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        String resourcePolicy = SequenceUtils.getRestToSoapConvertedSequence(api, sequenceType);
        if (StringUtils.isEmpty(resourcePath) && StringUtils.isEmpty(verb)) {
            return resourcePolicy;
        }
        if (StringUtils.isNotEmpty(resourcePath) && StringUtils.isNotEmpty(verb)) {
            try {
                JSONObject sequenceObj = (JSONObject) new JSONParser().parse(resourcePolicy);
                JSONObject resultJson = new JSONObject();
                String key = resourcePath + "_" + verb;
                JSONObject sequenceContent = (JSONObject) sequenceObj.get(key);
                if (sequenceContent == null) {
                    String errorMessage = "Cannot find any resource policy for Resource path : " + resourcePath +
                            " with type: " + verb;
                    throw new APIManagementException(errorMessage, ExceptionCodes.RESOURCE_NOT_FOUND);
                }
                resultJson.put(key, sequenceObj.get(key));
                return resultJson.toJSONString();
            } catch (ParseException e) {
                throw new APIManagementException("Error while retrieving the resource policies for the API : " + apiId,
                        ExceptionCodes.JSON_PARSE_ERROR);
            }
        } else if (StringUtils.isEmpty(resourcePath)) {
            throw new APIManagementException("Resource path cannot be empty for the defined verb: " + verb,
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        } else if (StringUtils.isEmpty(verb)) {
            throw new APIManagementException("HTTP verb cannot be empty for the defined resource path: " + resourcePath,
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        return "";
    }

    /**
     * @param apiId            API UUID
     * @param organization     Tenant organization
     * @param resourcePolicyId Resource policy ID
     * @param xmlContent       Policy xml content
     * @return Updates resource policy
     * @throws APIManagementException when updating a resource policy fails
     */
    public static String updateAPIResourcePoliciesByPolicyId(String apiId, String organization,
                                                             String resourcePolicyId, String xmlContent)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        CommonUtils.checkAPIType(APIConstants.API_TYPE_SOAPTOREST, api.getType());
        if (StringUtils.isEmpty(resourcePolicyId)) {
            String errorMessage = "Resource id should not be empty to update a resource policy.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        boolean isValidSchema = PublisherUtils.validateXMLSchema(xmlContent);
        if (isValidSchema) {
            List<SOAPToRestSequence> sequence = api.getSoapToRestSequences();
            for (SOAPToRestSequence soapToRestSequence : sequence) {
                if (soapToRestSequence.getUuid().equals(resourcePolicyId)) {
                    soapToRestSequence.setContent(xmlContent);
                    break;
                }
            }
            API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
            try {
                apiProvider.updateAPI(api, originalAPI);
            } catch (FaultGatewaysException e) {
                throw new APIManagementException("Error while updating the API with resource policies",
                        ExceptionCodes.INTERNAL_ERROR);
            }
            SequenceUtils.updateResourcePolicyFromRegistryResourceId(api.getId(), resourcePolicyId, xmlContent);
            return SequenceUtils.getResourcePolicyFromRegistryResourceId(api, resourcePolicyId);
        }
        return "";
    }

}
