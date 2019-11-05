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
import org.wso2.carbon.core.util.CryptoUtil;
import org.json.XML;
import java.nio.ByteBuffer;
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
     * @param messageContext - should contain the payload
     * @return true
     */
    public boolean mediate(MessageContext messageContext) {
        // convert XML payload to JSON payload
        JSONObject soapBody = XML.toJSONObject(messageContext.getEnvelope().getBody().toString()).getJSONObject("soapenv:Body");

        String jsonPayload = "{}";
        if (soapBody.has("jsonObject")) {
            jsonPayload = soapBody.getJSONObject("jsonObject").toString();
        }

        // get response from Lambda
        ByteBuffer response = invokeLambda(jsonPayload);

        // byte buffer to string conversion
        String strResponse = new String(response.array(), Charset.forName("UTF-8"));

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        try {
            // set response to messageContext
            JsonUtil.getNewJsonPayload(axis2MessageContext, strResponse, true, true);
            axis2MessageContext.setProperty("HTTP_SC", 200);
            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
            axis2MessageContext.setProperty("messageType", "application/json");
            axis2MessageContext.setProperty("ContentType", "application/json");
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }

        return true;
    }

    /**
     * invoke AWS Lambda function
     *
     * @param payload - input parameters to pass to AWS Lambda function as a JSONString
     * @return ByteBuffer response
     *
     */
    public ByteBuffer invokeLambda(String payload) {
        ByteBuffer response = null;
        AWSCredentialsProvider credentialsProvider;
        try {
            if (accessKey.equals("") && secretKey.equals("")) {
                credentialsProvider = InstanceProfileCredentialsProvider.getInstance();
            } else {
                String decryptedSecretKey = decrypt(secretKey);
                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, decryptedSecretKey);
                credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
            }
            AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .build();
            InvokeRequest invokeRequest = new InvokeRequest()
                    .withFunctionName(resourceName)
                    .withPayload(payload)
                    .withInvocationType(InvocationType.RequestResponse);
            InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
            response = invokeResult.getPayload();
        } catch (com.amazonaws.SdkClientException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String decrypt (String cipherText) throws Exception {
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        String plainText = new String(cryptoUtil.base64DecodeAndDecrypt(secretKey), "UTF-8");
        return plainText;
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
