package org.wso2.carbon.apimgt.impl.restapi.publisher;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import org.apache.axiom.util.base64.Base64Utils;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.*;
import org.wso2.carbon.apimgt.api.dto.APIEndpointValidationDTO;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.restapi.Constants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.restapi.CommonUtils.constructEndpointConfigForService;
import static org.wso2.carbon.apimgt.impl.restapi.CommonUtils.validateScopes;
import static org.wso2.carbon.apimgt.impl.restapi.Constants.CHARSET;

public class ApisApiServiceImplUtils {

    private static final Log log = LogFactory.getLog(ApisApiServiceImplUtils.class);

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
     * @throws ParseException               when JSON parsing fails
     * @throws CryptoException              when encrypting secrets fail
     * @throws UnsupportedEncodingException when converting to String fails
     * @throws SdkClientException           if AWSLambda SDK throws an error
     */
    public static JSONObject getAmazonResourceNames(API api)
            throws ParseException, CryptoException, UnsupportedEncodingException, SdkClientException {
        JSONObject arns = new JSONObject();
        String endpointConfigString = api.getEndpointConfig();
        if (StringUtils.isNotEmpty(endpointConfigString)) {
            JSONParser jsonParser = new JSONParser();
            JSONObject endpointConfig = (JSONObject) jsonParser.parse(endpointConfigString);
            if (endpointConfig != null) {
                if (endpointConfig.containsKey(APIConstants.AMZN_ACCESS_KEY)
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
                    AWSLambda awsLambdaClient;
                    if (StringUtils.isEmpty(accessKey) && StringUtils.isEmpty(secretKey)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Using temporary credentials supplied by the IAM role attached to AWS " +
                                    "instance");
                        }
                        if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                                && StringUtils.isEmpty(roleRegion)) {
                            awsLambdaClient = AWSLambdaClientBuilder.standard()
                                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                                    .build();
                        } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                                && StringUtils.isNotEmpty(roleRegion)) {
                            AWSSecurityTokenService awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
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
                        } else {
                            log.error("Missing AWS STS configurations");
                            return null;
                        }
                    } else if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey) &&
                            StringUtils.isNotEmpty(region)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Using user given stored credentials");
                        }
                        if (secretKey.length() == APIConstants.AWS_ENCRYPTED_SECRET_KEY_LENGTH) {
                            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                            secretKey = new String(cryptoUtil.base64DecodeAndDecrypt(secretKey),
                                    APIConstants.DigestAuthConstants.CHARSET);
                        }
                        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
                        if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                                && StringUtils.isEmpty(roleRegion)) {
                            awsLambdaClient = AWSLambdaClientBuilder.standard()
                                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                                    .withRegion(region)
                                    .build();
                        } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                                && StringUtils.isNotEmpty(roleRegion)) {
                            AWSSecurityTokenService awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                                    .withRegion(region)
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
                        } else {
                            log.error("Missing AWS STS configurations");
                            return null;
                        }
                    } else {
                        log.error("Missing AWS Credentials");
                        return null;
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
                }
            }
        }
        return null;
    }

    /**
     * @param api                         API
     * @param securityAuditPropertyObject audit security properties
     * @param apiDefinition               API definition
     * @param organization                user organization
     * @return JSONObject containing audit response
     * @throws APIManagementException when there's an unexpected response
     * @throws IOException            when http client fails
     * @throws ParseException         when createAuditApi fails
     */
    public static JSONObject getAuditReport(API api, JSONObject securityAuditPropertyObject,
                                            String apiDefinition, String organization)
            throws APIManagementException, IOException, ParseException {
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
        URL getReportUrl = new URL(getUrl);

        try (CloseableHttpClient getHttpClient = (CloseableHttpClient) APIUtil
                .getHttpClient(getReportUrl.getPort(), getReportUrl.getProtocol())) {
            HttpGet httpGet = new HttpGet(getUrl);
            // Set the header properties of the request
            httpGet.setHeader(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            httpGet.setHeader(APIConstants.HEADER_API_TOKEN, apiToken);
            httpGet.setHeader(APIConstants.HEADER_USER_AGENT, APIConstants.USER_AGENT_APIM);
            // Code block for the processing of the response
            try (CloseableHttpResponse response = getHttpClient.execute(httpGet)) {
                if (isDebugEnabled) {
                    log.debug("HTTP status " + response.getStatusLine().getStatusCode());
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
        }
        return null;
    }

    /**
     * Update API Definition before retrieving Security Audit Report
     *
     * @param apiDefinition  API Definition of API
     * @param apiToken       API Token to access Security Audit
     * @param auditUuid      Respective UUID of API in Security Audit
     * @param baseUrl        Base URL to communicate with Security Audit
     * @param isDebugEnabled Boolean whether debug is enabled
     * @throws IOException            In the event of any problems with the request
     * @throws APIManagementException In the event of unexpected response
     */
    private static void updateAuditApi(String apiDefinition, String apiToken, String auditUuid, String baseUrl,
                                       boolean isDebugEnabled)
            throws IOException, APIManagementException {
        // Set the property to be attached in the body of the request
        // Attach API Definition to property called specfile to be sent in the request
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("specfile", Base64Utils.encode(apiDefinition.getBytes(StandardCharsets.UTF_8)));
        // Logic for HTTP Request
        String putUrl = baseUrl + "/" + auditUuid;
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
                    log.debug("HTTP status " + response.getStatusLine().getStatusCode());
                }
                if (!(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)) {
                    throw new APIManagementException(
                            "Error while sending data to the API Security Audit Feature. Found http status " +
                                    response.getStatusLine());
                }
            } finally {
                httpPut.releaseConnection();
            }
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
     * @throws IOException            In the event of any problems in the request
     * @throws APIManagementException In the event of unexpected response
     * @throws ParseException         In the event of any parse errors from the response
     */
    private static String createAuditApi(String collectionId, String apiToken, APIIdentifier apiIdentifier,
                                         String apiDefinition, String baseUrl, boolean isDebugEnabled, String organization)
            throws IOException, APIManagementException, ParseException {
        HttpURLConnection httpConn;
        OutputStream outputStream;
        PrintWriter writer;
        String auditUuid = null;
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
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
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
        // Checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            if (isDebugEnabled) {
                log.debug("HTTP status " + status);
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
                                httpConn.getResponseCode() + " - " + errorMessage);
            } else {
                throw new APIManagementException(
                        "Error while retrieving data for the API Security Audit Report. Found http status: " +
                                httpConn.getResponseCode() + " - " + httpConn.getResponseMessage());
            }
        }
        return auditUuid;
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
        URL url = new URL(urlVal);
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
                        String openAPIContent = IOUtils.toString(inputStream, CHARSET);
                        validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                    }
                } else {
                    String openAPIContent = IOUtils.toString(inputStream, CHARSET);
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
     * @param username           API Provider username
     * @param service            ServiceCatalog service
     * @param validationResponse OpenAPI defnition validation response
     * @param isServiceAPI       whether the API is created from a service
     * @param syncOperations     sync all API operations
     * @throws APIManagementException when scope validation or OpenAPI parsing fails
     */
    public static API importAPIDefinition(API apiToAdd, APIProvider apiProvider, String username, String organization,
                                           ServiceEntry service, APIDefinitionValidationResponse validationResponse,
                                           boolean isServiceAPI, boolean syncOperations)
            throws APIManagementException {
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


}
