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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.redis.RedisCacheUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * This @AWSLambdaMediator mediator invokes AWS Lambda functions when
 * calling resources in APIs with AWS Lambda endpoint type.
 */
public class AWSLambdaMediator extends AbstractMediator {
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

    public AWSLambdaMediator() {

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
            payload.addProperty(HTTP_METHOD, (String) messageContext.getProperty(APIConstants.REST_METHOD));
            payload.addProperty(PATH, (String) messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE));

            // Set lambda backend invocation start time for analytics
            messageContext.setProperty(Constants.BACKEND_START_TIME_PROPERTY, System.currentTimeMillis());

            String body = "{}";
            if (JsonUtil.hasAJsonPayload(axis2MessageContext)) {
                String jsonPayload = JsonUtil.jsonPayloadToString(axis2MessageContext);
                if (!isContentEncodingEnabled) {
                    payload.add(BODY_PARAMETER, new JsonParser().parse(body).getAsJsonObject());
                } else {
                    payload.addProperty(BODY_PARAMETER, Base64.encodeBase64String(jsonPayload.getBytes()));
                }
            } else {
                String multipartContent = extractFormDataContent(axis2MessageContext);
                if (StringUtils.isNotEmpty(multipartContent)) {
                    body = isContentEncodingEnabled ? Base64.encodeBase64String(multipartContent.getBytes()) :
                            multipartContent;
                    payload.addProperty(BODY_PARAMETER, body);
                } else {
                    // If the request does not have a payload(as either a json payload or multipart content),
                    // set an empty JSON object as the payload
                    payload.add(BODY_PARAMETER, new JsonParser().parse(body).getAsJsonObject());
                }
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
                JsonUtil.getNewJsonPayload(axis2MessageContext, new String(invokeResult.payload().asByteArray(), StandardCharsets.UTF_8),
                        true, true);
                axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, invokeResult.statusCode());
                axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_MESSAGE_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                axis2MessageContext.removeProperty(APIConstants.NO_ENTITY_BODY);
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
     * invoke AWS Lambda function
     *
     * @param payload - input parameters to pass to AWS Lambda function as a JSONString
     * @return InvokeResult
     */
    private InvokeResponse invokeLambda(String payload) {
        try {
            // Validate resource timeout and set client configuration
            if (resourceTimeout.toMillis() < 1000 || resourceTimeout.toMillis() > 900000) {
                setResourceTimeout(APIConstants.AWS_DEFAULT_CONNECTION_TIMEOUT);
            }
            ClientOverrideConfiguration clientConfig = ClientOverrideConfiguration.builder()
                    .apiCallTimeout(resourceTimeout).build();

            LambdaClient awsLambdaClient;
            if (StringUtils.isEmpty(accessKey) && StringUtils.isEmpty(secretKey)) {
                if (log.isDebugEnabled()) {
                    log.debug("Using temporary credentials supplied by the IAM role attached to AWS instance");
                }
                if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                        && StringUtils.isEmpty(roleRegion)) {
                    awsLambdaClient = LambdaClient.builder()
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .httpClientBuilder(ApacheHttpClient.builder())
                            .overrideConfiguration(clientConfig)
                            .build();
                } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                        && StringUtils.isNotEmpty(roleRegion)) {
                    Region region = new DefaultAwsRegionProviderChain().getRegion();
                    Credentials sessionCredentials = getSessionCredentials(
                            DefaultCredentialsProvider.create(), roleArn, roleSessionName,
                            String.valueOf(region));
                    AwsSessionCredentials basicSessionCredentials = AwsSessionCredentials.create(sessionCredentials.accessKeyId(), sessionCredentials.secretAccessKey(), sessionCredentials.sessionToken());
                    awsLambdaClient = LambdaClient.builder()
                                    .credentialsProvider(StaticCredentialsProvider.create(basicSessionCredentials))
                                    .httpClientBuilder(ApacheHttpClient.builder())
                                    .overrideConfiguration(clientConfig)
                            .region(Region.of(roleRegion))
                            .build();
                } else {
                    log.error("Missing AWS STS configurations");
                    return null;
                }
            } else if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey)
                    && StringUtils.isNotEmpty(region)) {
                if (log.isDebugEnabled()) {
                    log.debug("Using user given stored credentials");
                }
                AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
                if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                        && StringUtils.isEmpty(roleRegion)) {
                    awsLambdaClient = LambdaClient.builder()
                                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                                    .httpClientBuilder(ApacheHttpClient.builder())
                                    .overrideConfiguration(clientConfig)
                            .region(Region.of(region))
                            .build();
                } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                        && StringUtils.isNotEmpty(roleRegion)) {
                    Credentials sessionCredentials = getSessionCredentials(
                            StaticCredentialsProvider.create(awsCredentials), roleArn, roleSessionName, region);
                    AwsSessionCredentials basicSessionCredentials = AwsSessionCredentials.create(sessionCredentials.accessKeyId(), sessionCredentials.secretAccessKey(), sessionCredentials.sessionToken());
                    awsLambdaClient = LambdaClient.builder()
                                    .credentialsProvider(StaticCredentialsProvider.create(basicSessionCredentials))
                                    .httpClientBuilder(ApacheHttpClient.builder())
                                    .overrideConfiguration(clientConfig)
                            .region(Region.of(roleRegion))
                            .build();
                } else {
                    log.error("Missing AWS STS configurations");
                    return null;
                }
            } else {
                log.error("Missing AWS Credentials");
                return null;
            }

            SdkBytes payloadBytes = SdkBytes.fromUtf8String(payload);
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(resourceName)
                    .payload(payloadBytes)
                    .invocationType(InvocationType.REQUEST_RESPONSE)
                    .build();
            return awsLambdaClient.invoke(invokeRequest);
        } catch (SdkClientException | URISyntaxException e) {
            log.error("Error while invoking the lambda function", e);
        }
        return null;
    }

    private Credentials getSessionCredentials(AwsCredentialsProvider credentialsProvider, String roleArn,
                                              String roleSessionName, String region) throws URISyntaxException {
        Credentials sessionCredentials = null;
        if (ServiceReferenceHolder.getInstance().isRedisEnabled()) {
            Object previousCredentialsObject = new RedisCacheUtils(ServiceReferenceHolder.getInstance().getRedisPool())
                    .getObject(roleSessionName, Credentials.class);
            if (previousCredentialsObject != null) {
                sessionCredentials = (Credentials) previousCredentialsObject;
            }
        } else {
            sessionCredentials = CredentialsCache.getInstance().getCredentialsMap().get(roleSessionName);
        }
        if (sessionCredentials != null) {
            long expirationTime = sessionCredentials.expiration().toEpochMilli();
            long currentTime = System.currentTimeMillis();
            long timeDifference = expirationTime - currentTime;
            if (timeDifference > 1000) {
                return sessionCredentials;
            }
        }
        StsClient awsSTSClient;
        if (StringUtils.isEmpty(region)) {
            awsSTSClient = StsClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .build();
        } else {
            awsSTSClient = StsClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .endpointOverride(new URI("https://sts." + region + ".amazonaws.com"))
                    .build();
        }
        AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
                .roleArn(roleArn)
                .roleSessionName(roleSessionName)
                .build();
        AssumeRoleResponse assumeRoleResult = awsSTSClient.assumeRole(roleRequest);
        sessionCredentials = assumeRoleResult.credentials();
        if (ServiceReferenceHolder.getInstance().isRedisEnabled()) {
            new RedisCacheUtils(ServiceReferenceHolder.getInstance().getRedisPool())
                    .addObject(roleSessionName, sessionCredentials);
        } else {
            CredentialsCache.getInstance().getCredentialsMap().put(roleSessionName, sessionCredentials);
        }
        return sessionCredentials;
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
