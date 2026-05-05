/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.gateway.mediators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

import javax.activation.DataHandler;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This @AWSLambdaMediator mediator invokes AWS Lambda functions when
 * calling resources in APIs with AWS Lambda endpoint type.
 */
public class AWSLambdaMediator extends AbstractMediator implements ManagedLifecycle {
    private static final Log log = LogFactory.getLog(AWSLambdaMediator.class);
    private String accessKey = "";
    private String secretKey = "";
    private String region = "";
    private String resourceName = "";
    private String roleArn = "";
    private String roleSessionName = "";
    private String roleRegion = "";
    private Duration resourceTimeout = Duration.ofMillis(APIConstants.AWS_DEFAULT_CONNECTION_TIMEOUT);
    private boolean isContentEncodingEnabled = false;
    private static final String PATH_PARAMETERS = "pathParameters";
    private static final String QUERY_STRING_PARAMETERS = "queryStringParameters";
    private static final String BODY_PARAMETER = "body";
    private static final String IS_BASE64_ENCODED_PARAMETER = "isBase64Encoded";
    private static final String PATH = "path";
    private static final String HTTP_METHOD = "httpMethod";
    private LambdaClient awsLambdaClient;
    private StsClient stsClient;
    private StsAssumeRoleCredentialsProvider assumeRoleCredentialsProvider;
    private static final String STATUS_CODE = "statusCode";
    private static final String MULTI_VALUE_HEADERS = "multiValueHeaders";
    private boolean proxyResponseMappingEnabled = false;

    public AWSLambdaMediator() {

    }

    /**
     * Initializes the mediator.
     *
     * @param synapseEnvironment Synapse environment context
     */
    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        if (log.isDebugEnabled()) {
            log.debug("AWSLambdaMediator initialized.");
        }

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                .getApiManagerConfigurationService().getAPIManagerConfiguration();
        String proxyMappingConfig = config.getFirstProperty(APIConstants.AWS_LAMBDA_PROXY_RESPONSE_ENABLED);
        proxyResponseMappingEnabled = Boolean.parseBoolean(proxyMappingConfig);
        if (log.isDebugEnabled()) {
            log.debug("AWS Lambda proxy response mapping enabled: " + proxyResponseMappingEnabled);
        }

        // Validate resource timeout and set client configuration
        if (resourceTimeout.toMillis() < 1000 || resourceTimeout.toMillis() > 900000) {
            setResourceTimeout(APIConstants.AWS_DEFAULT_CONNECTION_TIMEOUT);
        }
        ClientOverrideConfiguration clientConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(resourceTimeout)
                .build();

        if (StringUtils.isEmpty(accessKey) && StringUtils.isEmpty(secretKey)) {
            if (log.isDebugEnabled()) {
                log.debug("Using temporary credentials supplied by the IAM role attached to AWS instance");
            }

            if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                    && StringUtils.isEmpty(roleRegion)) {
                awsLambdaClient = LambdaClient.builder()
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .httpClientBuilder(getHttpClientBuilder())
                        .overrideConfiguration(clientConfig)
                        .build();

            } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                    && StringUtils.isNotEmpty(roleRegion)) {
                Region region = new DefaultAwsRegionProviderChain().getRegion();
                assumeRoleCredentialsProvider = getStsAssumeRoleCredentialsProvider(
                        DefaultCredentialsProvider.create(), region, roleArn, roleSessionName);
                awsLambdaClient = LambdaClient.builder()
                        .credentialsProvider(assumeRoleCredentialsProvider)
                        .httpClientBuilder(getHttpClientBuilder())
                        .overrideConfiguration(clientConfig)
                        .region(Region.of(roleRegion))
                        .build();
            } else {
                log.error("Missing AWS STS configurations");
            }

        } else if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey)
                && StringUtils.isNotEmpty(region)) {
            if (log.isDebugEnabled()) {
                log.debug("Using user given stored credentials");
            }

            StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));
            if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                    && StringUtils.isEmpty(roleRegion)) {
                awsLambdaClient = LambdaClient.builder()
                        .credentialsProvider(staticCredentialsProvider)
                        .httpClientBuilder(getHttpClientBuilder())
                        .overrideConfiguration(clientConfig)
                        .region(Region.of(region))
                        .build();

            } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                    && StringUtils.isNotEmpty(roleRegion)) {
                assumeRoleCredentialsProvider = getStsAssumeRoleCredentialsProvider(
                        staticCredentialsProvider, Region.of(region), roleArn, roleSessionName);
                awsLambdaClient = LambdaClient.builder()
                        .credentialsProvider(assumeRoleCredentialsProvider)
                        .httpClientBuilder(getHttpClientBuilder())
                        .overrideConfiguration(clientConfig)
                        .region(Region.of(roleRegion))
                        .build();
            } else {
                log.error("Missing AWS STS configurations");
            }
        } else {
            log.error("Missing AWS Credentials");
        }
    }

    /**
     * Destroys the mediator.
     */
    @Override
    public void destroy() {

        closeResource(awsLambdaClient);
        closeResource(assumeRoleCredentialsProvider);
        closeResource(stsClient);
    }

    /**
     * mediate to AWS Lambda
     *
     * @param messageContext - contains the payload
     * @return true
     */
    public boolean mediate(MessageContext messageContext) {
        try {
            org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            JsonObject payload = new JsonObject();

            // set headers
            JsonObject headers = new JsonObject();
            TreeMap transportHeaders =
                    (TreeMap) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            for (Object keyObj : transportHeaders.keySet()) {
                String key = (String) keyObj;
                String value = (String) transportHeaders.get(keyObj);
                headers.addProperty(key, value);
            }
            payload.add(APIConstants.PROPERTY_HEADERS_KEY, headers);

            // set path/query parameters
            JsonObject pathParameters = new JsonObject();
            JsonObject queryStringParameters = new JsonObject();
            Set propertySet = messageContext.getPropertyKeySet();
            for (Object key : propertySet) {
                if (key != null) {
                    String propertyKey = key.toString();
                    if (propertyKey.startsWith(RESTConstants.REST_URI_VARIABLE_PREFIX)) {
                        pathParameters.addProperty(propertyKey.substring(RESTConstants.REST_URI_VARIABLE_PREFIX.length()),
                                (String) messageContext.getProperty(propertyKey));
                    } else if (propertyKey.startsWith(RESTConstants.REST_QUERY_PARAM_PREFIX)) {
                        queryStringParameters.addProperty(propertyKey.substring(RESTConstants.REST_QUERY_PARAM_PREFIX.length()),
                                (String) messageContext.getProperty(propertyKey));
                    }
                }
            }
            payload.add(PATH_PARAMETERS, pathParameters);
            payload.add(QUERY_STRING_PARAMETERS, queryStringParameters);
            String httpMethod = (String) messageContext.getProperty(APIConstants.REST_METHOD);
            payload.addProperty(HTTP_METHOD, httpMethod);
            payload.addProperty(PATH, (String) messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE));

            // Set lambda backend invocation start time for analytics
            messageContext.setProperty(Constants.BACKEND_START_TIME_PROPERTY, System.currentTimeMillis());

            if (!APIConstants.HTTP_GET.equalsIgnoreCase(httpMethod) && !APIConstants.HTTP_HEAD.equalsIgnoreCase(httpMethod)) {
                String body = "{}";
                if (JsonUtil.hasAJsonPayload(axis2MessageContext)) {
                    body = JsonUtil.jsonPayloadToString(axis2MessageContext);
                    if (isContentEncodingEnabled) {
                        body = Base64.encodeBase64String(body.getBytes());
                    }
                } else {
                    String multipartContent = extractFormDataContent(axis2MessageContext);
                    if (StringUtils.isNotEmpty(multipartContent)) {
                        body = isContentEncodingEnabled ? Base64.encodeBase64String(multipartContent.getBytes()) :
                                multipartContent;
                    }
                }
                payload.addProperty(BODY_PARAMETER, body);
            }

            payload.addProperty(IS_BASE64_ENCODED_PARAMETER, isContentEncodingEnabled);

            if (log.isDebugEnabled()) {
                log.debug("Passing the payload " + payload.toString() + " to AWS Lambda function with resource name "
                        + resourceName);
            }
            InvokeResponse invokeResult = invokeLambda(payload.toString());

            if (invokeResult != null) {
                if (log.isDebugEnabled()) {
                    log.debug("AWS Lambda function: " + resourceName + " is invoked successfully.");
                }
                handleLambdaResponse(axis2MessageContext, invokeResult);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to invoke AWS Lambda function: " + resourceName);
                }
                axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, APIMgtGatewayConstants.HTTP_SC_CODE);
                axis2MessageContext.setProperty(APIConstants.NO_ENTITY_BODY, true);
            }
        } catch (AxisFault e) {
            log.error("Exception has occurred while performing AWS Lambda mediation : " + e.getMessage(), e);
            return false;
        }

        return true;
    }

    /**
     * Invoke AWS Lambda function
     *
     * @param payload - input parameters to pass to AWS Lambda function as a JSONString
     * @return InvokeResult
     */
    private InvokeResponse invokeLambda(String payload) {
        try {
            if (awsLambdaClient == null) {
                log.error("AWS Lambda client is null. Cannot invoke the lambda function.");
                return null;
            }
            SdkBytes payloadBytes = SdkBytes.fromUtf8String(payload);
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(resourceName)
                    .payload(payloadBytes)
                    .invocationType(InvocationType.REQUEST_RESPONSE)
                    .build();
            return awsLambdaClient.invoke(invokeRequest);
        } catch (SdkClientException | AwsServiceException e) {
            log.error("Error while invoking the lambda function", e);
            return null;
        }
    }

    /**
     * Creates the HTTP client builder based on the given configuration
     *
     * @return Configured ApacheHttpClient.Builder instance
     */
    private ApacheHttpClient.Builder getHttpClientBuilder() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                .getApiManagerConfigurationService().getAPIManagerConfiguration();

        ApacheHttpClient.Builder builder = ApacheHttpClient.builder();

        applyIntConfig(config, APIConstants.MAX_CONNECTIONS, builder::maxConnections);
        applyIntConfig(config,  APIConstants.CONNECTION_TIMEOUT, v -> builder.connectionTimeout(
                Duration.ofSeconds(v)));
        applyIntConfig(config, APIConstants.SOCKET_TIMEOUT, v -> builder.socketTimeout(Duration.ofSeconds(v)));
        applyIntConfig(config, APIConstants.ACQUISITION_TIMEOUT, v -> builder.connectionAcquisitionTimeout(
                Duration.ofSeconds(v)));

        return builder;
    }

    /**
     * Applies the given APIM configuration value to the AWS SDK HTTP client builder
     * Missing or invalid values are ignored, and AWS SDK defaults are used
     *
     * @param config APIM configuration source used to read the property
     * @param key property suffix
     * @param setter callback that applies the parsed integer value to the HTTP client builder
     */
    private void applyIntConfig(APIManagerConfiguration config, String key, java.util.function.IntConsumer setter) {

        String value = config.getFirstProperty(APIConstants.AWS_LAMBDA_HTTP_CLIENT + key);
        if (StringUtils.isNotEmpty(value)) {
            try {
                setter.accept(Integer.parseInt(value));
            } catch (NumberFormatException nfe) {
                log.warn("Invalid " + key + " for AWS Lambda HTTP Client: " + value + ". Falling back to default.");
            }
        } else {
            log.warn(key + " for AWS Lambda HTTP Client is not set. Falling back to default.");
        }
    }

    /**
     * Creates an STS AssumeRole credentials provider for the given role
     *
     * @param baseCredentialsProvider Credentials used to call STS
     * @param region AWS region for STS
     * @param roleArn Role ARN to assume
     * @param roleSessionName Session name used for AssumeRole
     * @return StsAssumeRoleCredentialsProvider instance
     */
    private StsAssumeRoleCredentialsProvider getStsAssumeRoleCredentialsProvider(AwsCredentialsProvider
                                  baseCredentialsProvider, Region region, String roleArn, String roleSessionName ) {

        if (region != null) {
            stsClient = StsClient.builder()
                    .credentialsProvider(baseCredentialsProvider)
                    .region(region)
                    .build();
        } else {
            stsClient = StsClient.builder()
                    .credentialsProvider(baseCredentialsProvider)
                    .build();
        }
        // Build a provider that assumes the role and refreshes session credentials automatically
        return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(stsClient)
                .refreshRequest(AssumeRoleRequest.builder()
                        .roleArn(roleArn)
                        .roleSessionName(roleSessionName)
                        .build())
                .build();
    }

    /**
     * Extract form data content from the message context
     *
     * @param axis2MessageContext - message context
     * @return form data content
     */
    private String extractFormDataContent(org.apache.axis2.context.MessageContext axis2MessageContext) {
        String content = "";
        try {
            String synapseContentType = (String) axis2MessageContext.getProperty("synapse.internal.rest.contentType");
            if (synapseContentType != null && synapseContentType.equals("multipart/form-data")) {
                SOAPEnvelope soapEnvelope = axis2MessageContext.getEnvelope();
                if (soapEnvelope != null) {
                    SOAPBody soapBody = soapEnvelope.getBody();
                    Iterator i = soapBody.getFirstElement().getChildren();

                    String contentType = (String) axis2MessageContext.getProperty("ContentType");
                    if (!StringUtils.isEmpty(contentType)) {
                        String boundary = contentType.split("boundary=")[1];
                        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
                        multipartEntityBuilder.setBoundary(boundary);
                        while (i.hasNext()) {
                            OMElement obj = (OMElement) i.next();
                            String encodedContent = obj.getText();
                            String localName = obj.getLocalName();
                            byte[] decodedContent = Base64.decodeBase64(encodedContent);
                            multipartEntityBuilder.addTextBody(localName, new String(decodedContent,
                                    StandardCharsets.UTF_8));
                        }

                        HttpEntity entity = multipartEntityBuilder.build();
                        try (ByteArrayOutputStream out = new ByteArrayOutputStream((int) entity.getContentLength())) {
                            entity.writeTo(out);
                            content = out.toString();
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error while extracting form data content", e);
        }
        return content;
    }

    /**
     * Helper method to safely close a resource
     * Automatically derives the component name for logging from the class type
     *
     * @param resource The AutoCloseable resource to close
     */
    private void closeResource(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                log.error("Error closing " + resource.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * Populates the Axis2 message context with HTTP status, headers, and body from AWS Lambda response.
     * Handles proxy response format {statusCode, headers, body, isBase64Encoded}.
     * Routes body by Content-Type: JSON via JsonUtil, XML via StAX, text via PlainTextFormatter,
     * binary via ExpandingMessageFormatter. Decodes base64 first if isBase64Encoded=true.
     * Falls back to JSON string primitive for unknown types.
     *
     * @param axis2MessageContext message context to populate
     * @param invokeResult        result of the AWS Lambda Invoke API call
     * @throws AxisFault if setting the JSON payload fails
     */
    private void handleLambdaResponse(org.apache.axis2.context.MessageContext axis2MessageContext,
            InvokeResponse invokeResult) throws AxisFault {

        // --- proxy response mapping disabled ---
        if (!proxyResponseMappingEnabled) {
            JsonUtil.getNewJsonPayload(axis2MessageContext,
                    new String(invokeResult.payload().asByteArray(), StandardCharsets.UTF_8), true, true);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, invokeResult.statusCode());
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_MESSAGE_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.removeProperty(APIConstants.NO_ENTITY_BODY);
            return;
        }

        // --- Proxy response mapping mode ---
        Map<String, String> transportHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (invokeResult.payload() == null) {
            log.warn("Lambda invocation returned a null payload; treating as empty response.");
            axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, transportHeaders);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, 502);
            axis2MessageContext.setProperty(APIConstants.NO_ENTITY_BODY, true);
            return;
        }
        String lambdaPayload = new String(invokeResult.payload().asByteArray(), StandardCharsets.UTF_8);
        if (log.isDebugEnabled()) {
            log.debug("Received Lambda response payload (length=" + lambdaPayload.length() + ").");
        }

        // If x-amz-function-error is set, Lambda itself crashed (not a user-thrown error).
        // The payload {errorMessage, errorType, stackTrace} is logged server-side only.
        // A generic 502 body is returned to avoid leaking backend internals to the client.
        if (StringUtils.isNotEmpty(invokeResult.functionError())) {
            log.error("Lambda function error [" + invokeResult.functionError() + "]");
            axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, transportHeaders);
            JsonUtil.getNewJsonPayload(axis2MessageContext, "{\"message\":\"Internal Server Error\"}", true, true);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, 502);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_MESSAGE_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.removeProperty(APIConstants.NO_ENTITY_BODY);
            return;
        }

        // Parse the Lambda proxy response envelope: {statusCode, headers, body, isBase64Encoded}
        LambdaResponse lambdaResponse = getLambdaResponse(lambdaPayload, invokeResult.statusCode());
        String responseBody = lambdaResponse.getBody();

        axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, lambdaResponse.getStatusCode());

        if (log.isDebugEnabled()) {
            log.debug("Parsed Lambda proxy response: statusCode=" + lambdaResponse.getStatusCode()
                    + ", contentType=" + lambdaResponse.getContentType()
                    + ", isBase64Encoded=" + lambdaResponse.isBase64Encoded()
                    + ", bodyLength=" + (responseBody != null ? responseBody.length() : 0));
        }

        // Forward Lambda response headers — always, even for empty-body responses (e.g. 204, 301).
        // Always create a fresh response header map to avoid leaking inbound request headers to the client.
        // Content-Type is excluded here; it is applied via the Axis2 message context properties below.
        axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, transportHeaders);
        for (Map.Entry<String, String> entry : lambdaResponse.getHeaders().entrySet()) {
            if (!APIConstants.HEADER_CONTENT_TYPE.equalsIgnoreCase(entry.getKey())) {
                transportHeaders.put(entry.getKey(), entry.getValue());
            }
        }

        // No body field in the proxy response (e.g. 204 No Content) — suppress the HTTP body entirely.
        if (StringUtils.isEmpty(responseBody)) {
            axis2MessageContext.setProperty(APIConstants.NO_ENTITY_BODY, true);
            return;
        }

        // Extract charset from Content-Type before stripping params; used for text/XML byte decoding.
        Charset responseCharset = StandardCharsets.UTF_8;
        String rawContentType = lambdaResponse.getContentType();
        if (rawContentType != null) {
            for (String param : rawContentType.split(";")) {
                String trimmed = param.trim();
                if (trimmed.toLowerCase(java.util.Locale.ROOT).startsWith("charset=")) {
                    String charsetName = trimmed.substring("charset=".length()).trim().replace("\"", "");
                    try {
                        responseCharset = Charset.forName(charsetName);
                    } catch (IllegalArgumentException e) {
                        log.warn("Unsupported charset '" + charsetName + "' in Content-Type; falling back to UTF-8.");
                    }
                    break;
                }
            }
        }

        // Strip content-type parameters (e.g. "; charset=UTF-8") so formatter lookup is unambiguous.
        String responseContentType = lambdaResponse.getContentType();
        if (responseContentType != null && responseContentType.contains(";")) {
            responseContentType = responseContentType.split(";")[0].trim();
        }
        // Normalise to lower-case once for all comparisons below.
        String contentTypeLower = responseContentType != null ? responseContentType.toLowerCase(java.util.Locale.ROOT) : "";

        // Pre-decode base64 once; null means the body is a plain string (not base64).
        // Use the strict JDK decoder (RFC 4648) so invalid base64 throws immediately rather than
        // silently producing garbage bytes, which Commons Codec's lenient decoder would not catch.
        byte[] decodedBytes = null;
        if (lambdaResponse.isBase64Encoded()) {
            try {
                decodedBytes = java.util.Base64.getDecoder().decode(responseBody);
            } catch (IllegalArgumentException e) {
                log.warn("Lambda response has isBase64Encoded=true but the body is not valid base64; returning 502.", e);
                transportHeaders.clear();
                axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, 502);
                axis2MessageContext.setProperty(APIConstants.NO_ENTITY_BODY, true);
                return;
            }
        }

        // Configure XMLInputFactory once — reused if the body turns out to be XML.
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        try {
            xmlInputFactory.setProperty(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (IllegalArgumentException ignored) {
            // Parser does not support FEATURE_SECURE_PROCESSING; XXE is still blocked above.
        }

        OMFactory omFactory = OMAbstractFactory.getOMFactory();

        if (contentTypeLower.contains("json")) {
            String src = decodedBytes != null ? new String(decodedBytes, responseCharset) : responseBody;
            String jsonBody;
            try {
                JsonParser.parseString(src);
                jsonBody = src;
            } catch (JsonSyntaxException e) {
                log.warn("Lambda response body is not valid JSON despite Content-Type claiming application/json;"
                        + " wrapping as JSON string primitive.", e);
                jsonBody = new JsonPrimitive(src).toString();
            }
            JsonUtil.getNewJsonPayload(axis2MessageContext, jsonBody, true, true);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_MESSAGE_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);

        } else if (contentTypeLower.endsWith("/xml") || contentTypeLower.contains("+xml")) {
            String src = decodedBytes != null ? new String(decodedBytes, responseCharset) : responseBody;
            OMElement existingBodyChild = axis2MessageContext.getEnvelope().getBody().getFirstElement();
            if (existingBodyChild != null) {
                existingBodyChild.detach();
            }
            boolean xmlParsed = false;
            try {
                XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(new StringReader(src));
                OMElement xmlElement = new StAXOMBuilder(xmlReader).getDocumentElement();
                axis2MessageContext.getEnvelope().getBody().addChild(xmlElement);
                xmlParsed = true;
            } catch (Exception e) {
                log.error("Failed to parse XML body; falling back to JSON string primitive.", e);
                JsonUtil.getNewJsonPayload(axis2MessageContext, new JsonPrimitive(src).toString(), true, true);
            }
            if (xmlParsed) {
                axis2MessageContext.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE,
                        responseContentType);
                axis2MessageContext.setProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE,
                        responseContentType);
                axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_MESSAGE_TYPE, responseContentType);
                axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE, responseContentType);
            } else {
                axis2MessageContext.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE,
                        APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                axis2MessageContext.setProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE,
                        APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_MESSAGE_TYPE,
                        APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE,
                        APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            }

        } else if (contentTypeLower.startsWith("text/")) {
            String src = decodedBytes != null ? new String(decodedBytes, responseCharset) : responseBody;
            OMElement existingBodyChild = axis2MessageContext.getEnvelope().getBody().getFirstElement();
            if (existingBodyChild != null) {
                existingBodyChild.detach();
            }
            OMElement textWrapper = omFactory.createOMElement(BaseConstants.DEFAULT_TEXT_WRAPPER);
            textWrapper.setText(src);
            axis2MessageContext.getEnvelope().getBody().addChild(textWrapper);
            axis2MessageContext.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE, responseContentType);
            axis2MessageContext.setProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE, responseContentType);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_MESSAGE_TYPE, responseContentType);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE, responseContentType);

        } else if (decodedBytes != null) {
            // Base64-encoded binary: wrap with DEFAULT_BINARY_WRAPPER for ExpandingMessageFormatter.
            OMElement existingBodyChild = axis2MessageContext.getEnvelope().getBody().getFirstElement();
            if (existingBodyChild != null) {
                existingBodyChild.detach();
            }
            String mimeType = StringUtils.isNotEmpty(responseContentType)
                    ? responseContentType
                    : APIConstants.APPLICATION_OCTET_STREAM_MEDIA_TYPE;
            DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(decodedBytes, mimeType));
            OMElement binaryWrapper = omFactory.createOMElement(BaseConstants.DEFAULT_BINARY_WRAPPER);
            binaryWrapper.addChild(omFactory.createOMText(dataHandler, true));
            axis2MessageContext.getEnvelope().getBody().addChild(binaryWrapper);
            axis2MessageContext.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE, mimeType);
            axis2MessageContext.setProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE, mimeType);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_MESSAGE_TYPE, mimeType);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE, mimeType);

        } else {
            if (isBinaryContentType(responseContentType)) {
                log.warn("Lambda response has binary Content-Type '" + responseContentType
                        + "' but isBase64Encoded is not true; returning 502.");
                transportHeaders.clear();
                axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, 502);
                axis2MessageContext.setProperty(APIConstants.NO_ENTITY_BODY, true);
                return;
            }
            // Unknown non-binary type without base64: treat as JSON string primitive.
            JsonUtil.getNewJsonPayload(axis2MessageContext,
                    new JsonPrimitive(responseBody).toString(), true, true);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_MESSAGE_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);
        }

        // Body has been set — ensure NO_ENTITY_BODY is cleared so the transport layer sends it.
        axis2MessageContext.removeProperty(APIConstants.NO_ENTITY_BODY);
    }

    /**
     * Parses an AWS Lambda proxy integration response payload into a {@link LambdaResponse}
     *
     * @param lambdaPayload raw UTF-8 payload from the Lambda Invoke response
     * @param defaultStatusCode HTTP status code to use if the payload does not contain a valid one
     * @return LambdaResponse populated from the payload
     */
    private LambdaResponse getLambdaResponse(String lambdaPayload, int defaultStatusCode) {
        // Body is initialized as null. It is only set if the proxy response contains a 'body' field.
        // If JSON parsing fails entirely, we fall back to the raw lambdaPayload in the catch block.
        LambdaResponse lambdaResponse = new LambdaResponse(defaultStatusCode, null);

        if (StringUtils.isEmpty(lambdaPayload)) {
            log.warn("Lambda proxy response payload is empty; treating as malformed proxy envelope and returning 502.");
            lambdaResponse.setStatusCode(502);
            return lambdaResponse;
        }

        try {
            JsonObject responseObject = JsonParser.parseString(lambdaPayload).getAsJsonObject();
            if (responseObject.has(STATUS_CODE) && !responseObject.get(STATUS_CODE).isJsonNull()) {
                int parsedStatusCode = responseObject.get(STATUS_CODE).getAsInt();
                if (parsedStatusCode >= 100 && parsedStatusCode <= 599) {
                    lambdaResponse.setStatusCode(parsedStatusCode);
                } else {
                    log.warn("Lambda proxy response contains an out-of-range statusCode (" + parsedStatusCode
                            + "); treating as malformed proxy envelope and returning 502.");
                    lambdaResponse.setStatusCode(502);
                    return lambdaResponse;
                }
            } else {
                log.warn("Lambda proxy response is missing the required statusCode field;"
                        + " treating as malformed proxy envelope and returning 502.");
                lambdaResponse.setStatusCode(502);
                return lambdaResponse;
            }
            if (responseObject.has(BODY_PARAMETER) && !responseObject.get(BODY_PARAMETER).isJsonNull()) {
                String responseBody = responseObject.get(BODY_PARAMETER).getAsString();
                if (responseObject.has(IS_BASE64_ENCODED_PARAMETER)
                        && !responseObject.get(IS_BASE64_ENCODED_PARAMETER).isJsonNull()
                        && responseObject.get(IS_BASE64_ENCODED_PARAMETER).getAsBoolean()) {
                    lambdaResponse.setBase64Encoded(true);
                    // Do not decode here; keep as base64 string for payload setting
                } else {
                    lambdaResponse.setBase64Encoded(false);
                }
                lambdaResponse.setBody(responseBody);
            }
            // Per AWS proxy integration spec: headers and multiValueHeaders are merged.
            // If the same key appears in both, only the values from multiValueHeaders are used.
            // Keys present only in headers are included normally.
            if (responseObject.has(MULTI_VALUE_HEADERS)
                    && responseObject.get(MULTI_VALUE_HEADERS).isJsonObject()) {
                JsonObject multiHeaders = responseObject.getAsJsonObject(MULTI_VALUE_HEADERS);
                for (String headerKey : multiHeaders.keySet()) {
                    if (multiHeaders.get(headerKey).isJsonArray()) {
                        JsonArray values = multiHeaders.getAsJsonArray(headerKey);
                        StringBuilder headerValue = new StringBuilder();
                        for (JsonElement element : values) {
                            if (element.isJsonNull()) {
                                continue;
                            }
                            if (headerValue.length() > 0) {
                                headerValue.append(", ");
                            }
                            headerValue.append(element.getAsString());
                        }
                        if (headerValue.length() > 0) {
                            lambdaResponse.addHeader(headerKey, headerValue.toString());
                        }
                    } else if (!multiHeaders.get(headerKey).isJsonNull()) {
                        lambdaResponse.addHeader(headerKey, multiHeaders.get(headerKey).getAsString());
                    }
                }
            }
            if (responseObject.has(APIConstants.PROPERTY_HEADERS_KEY)
                    && responseObject.get(APIConstants.PROPERTY_HEADERS_KEY).isJsonObject()) {
                JsonObject responseHeaders = responseObject.getAsJsonObject(APIConstants.PROPERTY_HEADERS_KEY);
                for (String headerKey : responseHeaders.keySet()) {
                    // Skip keys already populated from multiValueHeaders.
                    if (!lambdaResponse.getHeaders().containsKey(headerKey)) {
                        JsonElement headerValue = responseHeaders.get(headerKey);
                        if (!headerValue.isJsonNull()) {
                            lambdaResponse.addHeader(headerKey, headerValue.getAsString());
                        }
                    } else {
                        log.debug("Lambda response header '" + headerKey + "' from 'headers' map is being"
                                + " dropped because it is already present in 'multiValueHeaders'."
                                + " Per AWS proxy integration spec, multiValueHeaders takes precedence.");
                    }
                }
            }
        } catch (IllegalStateException | JsonSyntaxException | NumberFormatException
                | UnsupportedOperationException e) {
            log.warn("Failed to parse AWS Lambda proxy response payload as proxy integration envelope; returning 502.", e);
            lambdaResponse.setStatusCode(502);
            lambdaResponse.setBody(null);
            lambdaResponse.getHeaders().clear();
        }

        return lambdaResponse;
    }

    /**
     * Holds the parsed fields of an AWS Lambda proxy integration response
     */
    private static class LambdaResponse {
        private int statusCode;
        private String body;
        private String contentType;
        private boolean isBase64Encoded;
        private Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        LambdaResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        int getStatusCode() {
            return statusCode;
        }

        void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        String getBody() {
            return body;
        }

        void setBody(String body) {
            this.body = body;
        }

        Map<String, String> getHeaders() {
            return headers;
        }

        void addHeader(String name, String value) {
            this.headers.put(name, value);
            if (APIConstants.HEADER_CONTENT_TYPE.equalsIgnoreCase(name)) {
                this.contentType = value;
            }
        }

        String getContentType() {
            return contentType;
        }

        boolean isBase64Encoded() {
            return isBase64Encoded;
        }

        void setBase64Encoded(boolean base64Encoded) {
            isBase64Encoded = base64Encoded;
        }
    }

    private boolean isBinaryContentType(String contentType) {
        if (contentType == null) return false;
        String lower = contentType.toLowerCase();
        return lower.startsWith("image/")
                || lower.startsWith("audio/")
                || lower.startsWith("video/")
                || lower.equals("application/octet-stream")
                || lower.equals("application/pdf")
                || lower.equals("application/zip")
                || lower.equals("application/x-zip-compressed")
                || lower.equals("application/gzip")
                || lower.equals("application/x-gzip");
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void setTraceState(int traceState) {
        traceState = 0;
    }

    @Override
    public int getTraceState() {
        return 0;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region;
    }

    public String getRoleArn() {
        return roleArn;
    }

    public String getRoleSessionName() {
        return roleSessionName;
    }

    public String getRoleRegion() {
        return roleRegion;
    }

    public String getResourceName() {
        return resourceName;
    }

    public int getResourceTimeout() {
        return (int) resourceTimeout.toMillis();
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    public void setRoleSessionName(String roleSessionName) {
        this.roleSessionName = roleSessionName;
    }

    public void setRoleRegion(String roleRegion) {
        this.roleRegion = roleRegion;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public void setResourceTimeout(int resourceTimeout) {
        this.resourceTimeout = Duration.ofMillis(resourceTimeout);
    }

    public void setIsContentEncodingEnabled(boolean isContentEncodingEnabled) {
        this.isContentEncodingEnabled = isContentEncodingEnabled;
    }
}
