/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.threatprotection.APIMThreatAnalyzerException;
import org.wso2.carbon.apimgt.gateway.threatprotection.AnalyzerHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.analyzer.APIMThreatAnalyzer;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.ConfigurationHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.JSONConfig;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatExceptionHandler;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * This mediator would protect the backend resources from the JSON threat vulnerabilities by validating the
 * JSON schema.
 */
public class JsonSchemaValidator extends AbstractMediator {

    private static final Log logger = LogFactory.getLog(JsonSchemaValidator.class);

    /**
     * This mediate method validates the message body.
     *
     * @param messageContext This message context contains the request message properties of the relevant
     *                       API which was enabled the JSON_Validator message mediation in flow.
     * @return a boolean true if the message content is passed the json schema criteria.
     */
    public boolean mediate(MessageContext messageContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("JSON schema validation mediator is activated...");
        }
        Map<String, InputStream> inputStreams = null;
        org.apache.axis2.context.MessageContext axis2MC;
        Boolean validRequest = true;
        String apiContext;
        String requestMethod;
        String contentType;
        axis2MC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object contentTypeObject = axis2MC.getProperty(ThreatProtectorConstants.CONTENT_TYPE).toString();
        if (contentTypeObject != null) {
            contentType = contentTypeObject.toString();
        } else {
            contentType = axis2MC.getProperty(ThreatProtectorConstants.SOAP_CONTENT_TYPE).toString();
        }
        apiContext = messageContext.getProperty(ThreatProtectorConstants.API_CONTEXT).toString();
        requestMethod = axis2MC.getProperty(ThreatProtectorConstants.HTTP_REQUEST_METHOD).toString();

        if (!APIConstants.SupportedHTTPVerbs.GET.name().equalsIgnoreCase(requestMethod) &&
                (ThreatProtectorConstants.APPLICATION_JSON.equals(contentType) ||
                        ThreatProtectorConstants.TEXT_JSON.equals(contentType))) {
            JSONConfig jsonConfig = configureSchemaProperties(messageContext);
            ConfigurationHolder.addJsonConfig(jsonConfig);
            APIMThreatAnalyzer apimThreatAnalyzer = AnalyzerHolder.getAnalyzer(contentType);
            try {
                inputStreams = GatewayUtils.cloneRequestMessage(messageContext);
                if (inputStreams != null) {
                    InputStream inputStreamJson = inputStreams.get(ThreatProtectorConstants.JSON);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamJson);
                    apimThreatAnalyzer.analyze(bufferedInputStream, apiContext);
                }
            } catch (APIMThreatAnalyzerException e) {
                validRequest = false;
                String message = "Request is failed due to a JSON schema validation failure: ";
                logger.error(message, e);
                GatewayUtils.handleThreat(messageContext, ThreatProtectorConstants.HTTP_SC_CODE,
                        message + e.getMessage());
            } catch (IOException e) {
                String message = "Error occurred while building the request: ";
                logger.error(message, e);
                GatewayUtils.handleThreat(messageContext, ThreatProtectorConstants.HTTP_SC_CODE,
                        message + e.getMessage());
            } finally {
                // return analyzer to the pool
                AnalyzerHolder.returnObject(apimThreatAnalyzer);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("JSON Schema Validator: " + APIMgtGatewayConstants.REQUEST_TYPE_FAIL_MSG);
            }
        }
        GatewayUtils.setOriginalInputStream(inputStreams, axis2MC);
        if (validRequest) {
            try {
                RelayUtils.buildMessage(axis2MC);
            } catch (IOException | XMLStreamException e) {
                GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE, e.getMessage());
            }
        }
        return true;
    }

    /**
     * This method binds the properties of the json validator sequence with the JsonConfig object.
     *
     * @param messageContext This message context contains the request message properties of the relevant
     *                       API which was enabled the JSON_Validator message mediation in flow.
     * @return JSONConfig contains the json schema properties need to be validated.
     */
    public JSONConfig configureSchemaProperties(MessageContext messageContext) {
        Object messageProperty;
        int propertyCount = 0;
        int stringLength = 0;
        int arrayElementCount = 0;
        int keyLength = 0;
        int maxJSONDepth = 0;

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_PROPERTY_COUNT);
        if (messageProperty != null) {
            propertyCount = Integer.parseInt(messageProperty.toString());
        } else {
            String errorMessage = "Json schema maxProperty count is missing.";
            ThreatExceptionHandler.handleException(messageContext, errorMessage);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_STRING_LENGTH);
        if (messageProperty != null) {
            stringLength = Integer.parseInt(messageProperty.toString());
        } else {
            String errorMessage = "Json schema Max String length is missing";
            ThreatExceptionHandler.handleException(messageContext, errorMessage);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_ARRAY_ELEMENT_COUNT);
        if (messageProperty != null) {
            arrayElementCount = Integer.parseInt(messageProperty.toString());
        } else {
            String errorMessage = "Json schema max array element count is missing";
            ThreatExceptionHandler.handleException(messageContext, errorMessage);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_KEY_LENGTH);
        if (messageProperty != null) {
            keyLength = Integer.parseInt(messageProperty.toString());
        } else {
            String errorMessage = "Json schema maximum key length is missing";
            ThreatExceptionHandler.handleException(messageContext, errorMessage);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_JSON_DEPTH);
        if (messageProperty != null) {
            maxJSONDepth = Integer.parseInt(messageProperty.toString());
        } else {
            String errorMessage = "Json schema maximum JSON depth is missing";
            ThreatExceptionHandler.handleException(messageContext, errorMessage);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(("Max Priority count is:" + propertyCount) + ", " + "Max String length is: "
                    + stringLength + ", " + "Max Array element count: " + arrayElementCount + ", "
                    + "Max Key Length: " + keyLength + ", " + "Max JSON depth is:" + maxJSONDepth + ", ");
        }
        JSONConfig jsonConfig = new JSONConfig();
        jsonConfig.setMaxPropertyCount(propertyCount);
        jsonConfig.setMaxStringLength(stringLength);
        jsonConfig.setMaxArrayElementCount(arrayElementCount);
        jsonConfig.setMaxKeyLength(keyLength);
        jsonConfig.setMaxJsonDepth(maxJSONDepth);
        return jsonConfig;
    }

    /**
     * If the isContentAware method returns false, The request message payload wont be build.
     *
     * @return this method always returns false to avoid building the message.
     */
    @Override
    public boolean isContentAware() {
        return false;
    }

}
