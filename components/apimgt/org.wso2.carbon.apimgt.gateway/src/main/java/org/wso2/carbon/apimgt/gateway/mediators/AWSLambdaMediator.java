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

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
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
    private int resourceTimeout = APIConstants.AWS_DEFAULT_CONNECTION_TIMEOUT;
    private static final String PATH_PARAMETERS = "pathParameters";
    private static final String QUERY_STRING_PARAMETERS = "queryStringParameters";
    private static final String HEADER_PARAMETER = "headers";
    private static final String BODY_PARAMETER = "body";

    public AWSLambdaMediator() {

    }

    /**
     * mediate to AWS Lambda
     * @param messageContext - contains the payload
     * @return true
     */
    public boolean mediate(MessageContext messageContext) {
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
        payload.add(HEADER_PARAMETER, headers);

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

        String body;
        if (JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            body = JsonUtil.jsonPayloadToString(axis2MessageContext);
        } else {
            body = "{}";
        }
        payload.addProperty(BODY_PARAMETER, body);

        if (log.isDebugEnabled()) {
            log.debug("Passing the payload " + payload.toString() + " to AWS Lambda function with resource name "
                    + resourceName);
        }
        InvokeResult invokeResult = invokeLambda(payload.toString());

        if (invokeResult != null) {
            if (log.isDebugEnabled()) {
                log.debug("AWS Lambda function: " + resourceName + " is invoked successfully.");
            }
            JsonUtil.setJsonStream(axis2MessageContext, new ByteArrayInputStream(invokeResult.getPayload().array()));
            axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, invokeResult.getStatusCode());
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

        return true;
    }

    /**
     * invoke AWS Lambda function
     * @param payload - input parameters to pass to AWS Lambda function as a JSONString
     * @return InvokeResult
     */
    private InvokeResult invokeLambda(String payload) {
        try {
            String[] resourceNameSplit = resourceName.split(":");
            region = resourceNameSplit[3];
            // set credential provider
            AWSCredentialsProvider credentialsProvider;
            if (StringUtils.isEmpty(accessKey) && StringUtils.isEmpty(secretKey)) {
                if (log.isDebugEnabled()) {
                    log.debug("Using temporary credentials supplied by the IAM role attached to the EC2 instance");
                }
                credentialsProvider = InstanceProfileCredentialsProvider.getInstance();
            } else if (!StringUtils.isEmpty(accessKey) && !StringUtils.isEmpty(secretKey)) {
                if (log.isDebugEnabled()) {
                    log.debug("Using user given stored credentials");
                }
                if (secretKey.length() == APIConstants.AWS_ENCRYPTED_SECRET_KEY_LENGTH) {
                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                    setSecretKey(new String(cryptoUtil.base64DecodeAndDecrypt(secretKey),
                            APIConstants.DigestAuthConstants.CHARSET));
                }
                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
                credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
            } else {
                log.error("Missing AWS Credentials");
                return null;
            }
            // set invoke request
            if (resourceTimeout < 1000 || resourceTimeout > 900000) {
                setResourceTimeout(APIConstants.AWS_DEFAULT_CONNECTION_TIMEOUT);
            }
            InvokeRequest invokeRequest = new InvokeRequest()
                    .withFunctionName(resourceName)
                    .withPayload(payload)
                    .withInvocationType(InvocationType.RequestResponse)
                    .withSdkClientExecutionTimeout(resourceTimeout);
            // set aws lambda client
            AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(region)
                    .build();
            return awsLambda.invoke(invokeRequest);
        } catch (SdkClientException e) {
            log.error("Error while invoking the lambda function", e);
        } catch (CryptoException | UnsupportedEncodingException e) {
            log.error("Error while decrypting the secret key", e);
        }
        return null;
    }

    public String getType() {
        return null;
    }

    public void setTraceState(int traceState) {
        traceState = 0;
    }

    public int getTraceState() {
        return 0;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getResourceName() {
        return resourceName;
    }

    public int getResourceTimeout() {
        return resourceTimeout;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public void setResourceTimeout(int resourceTimeout) {
        this.resourceTimeout = resourceTimeout;
    }
}
