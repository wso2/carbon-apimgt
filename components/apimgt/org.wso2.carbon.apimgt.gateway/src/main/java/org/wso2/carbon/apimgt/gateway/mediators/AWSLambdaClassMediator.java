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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import org.apache.axis2.AxisFault;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.json.XML;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class AWSLambdaClassMediator extends AbstractMediator {
    private static final Log log = LogFactory.getLog(AWSLambdaClassMediator.class);
    private String accessKey = "";
    private String secretKey = "";
    private String resourceName = "";

    public AWSLambdaClassMediator() {

    }

    /**
     * mediate to AWS Lambda
     * @param messageContext - contains the payload
     * @return true
     */
    public boolean mediate(MessageContext messageContext) {
        try {
            int httpSC;
            String payload;
            String jsonPayload;
            org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();

//            if (axis2MessageContext.getProperty("org.apache.synapse.commons.json.JsonInputStream.IsJsonObject")) {
//                payload = axis2MessageContext.getProperty("org.apache.synapse.commons.json.JsonInputStream");
//            } else {
//                payload = "{}";
//            }

            JSONObject soapBody = XML.toJSONObject(messageContext.getEnvelope().getBody().toString())
                    .getJSONObject("soapenv:Body");
            if (soapBody.has("jsonObject")) {
                payload = soapBody.getJSONObject("jsonObject").toString();
            } else {
                payload = "{}";
            }

            InvokeResult invokeResult = invokeLambda(payload);
            if (invokeResult != null) {
                httpSC = invokeResult.getStatusCode();
                jsonPayload = new String(invokeResult.getPayload().array(), Charset.forName(APIConstants
                        .DigestAuthConstants.CHARSET));
            } else {
                httpSC = 400;
                jsonPayload = "{statusCode: 400, message: 'Bad request'}";
            }
            JsonUtil.getNewJsonPayload(axis2MessageContext, jsonPayload, true, true);
            axis2MessageContext.setProperty("HTTP_SC", httpSC);
            axis2MessageContext.setProperty("messageType", APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.setProperty("ContentType", APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        } catch (AxisFault e) {
            log.error("Error while retrieving axis2MessageContext", e);
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
            AWSCredentialsProvider credentialsProvider;
            if ("".equals(accessKey) && "".equals(secretKey)) {
                credentialsProvider = InstanceProfileCredentialsProvider.getInstance();
            } else {
                if (APIConstants.AMZN_SECRET_KEY_PREFIX.equals(secretKey.substring(0,
                        APIConstants.AMZN_SECRET_KEY_PREFIX_LENGTH))) {
                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                    setSecretKey(new String(cryptoUtil.base64DecodeAndDecrypt(secretKey.substring(
                            APIConstants.AMZN_SECRET_KEY_PREFIX_LENGTH)), APIConstants.DigestAuthConstants.CHARSET));
                }
                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
                credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
            }
            AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .build();
            InvokeRequest invokeRequest = new InvokeRequest()
                    .withFunctionName(resourceName)
                    .withPayload(payload)
                    .withInvocationType(InvocationType.RequestResponse);
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
        return this.accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getResourceNameName() {
        return resourceName;
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

}
