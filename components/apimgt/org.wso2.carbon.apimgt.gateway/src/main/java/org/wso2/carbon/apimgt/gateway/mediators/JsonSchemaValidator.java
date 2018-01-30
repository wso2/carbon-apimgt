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
    /**
     * This mediate method performs JSONAnalyzer configurations and validating the message body.
     *
     * @param messageContext This message context contains the request message properties of the relevant API which was
     *                       enabled the JSON_Validator message mediation in flow.
     * @return a boolean true if the message content is passed the json schema criteria.
     */
    public boolean mediate(MessageContext messageContext) {
        Map<String, InputStream> inputStreams;
        org.apache.axis2.context.MessageContext axis2MC;
        Boolean validRequest = true;
        if (log.isDebugEnabled()) {
            log.debug("JSON schema validation mediator is activated...");
        }
        axis2MC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String contentType = axis2MC.getProperty(ThreatProtectorConstants.CONTENT_TYPE).toString();
        String apiContext = messageContext.getProperty(ThreatProtectorConstants.API_CONTEXT).toString();
        JSONConfig jsonConfig = configureSchemaProperties(messageContext);
        ConfigurationHolder.addJsonConfig(jsonConfig);
        APIMThreatAnalyzer apimThreatAnalyzer = AnalyzerHolder.getAnalyzer(contentType);
        try {
            inputStreams = GatewayUtils.cloneRequestMessage(messageContext);
            GatewayUtils.setOriginalInputStream(inputStreams, axis2MC);
            if (inputStreams != null) {
                InputStream inputStreamJson = inputStreams.get(ThreatProtectorConstants.JSON);
                BufferedInputStream input = new BufferedInputStream(inputStreamJson);
                apimThreatAnalyzer.analyze(input, apiContext);
            }

        } catch (APIMThreatAnalyzerException e) {
            validRequest = false;
            String message = "Request is failed due to JSON schema validation failure: ";
            GatewayUtils.handleThreat(messageContext, ThreatProtectorConstants.HTTP_SC_CODE, message
                    + e.getMessage());
        } catch (IOException e) {
            String message = "Error occurred while building the request: ";
            GatewayUtils.handleThreat(messageContext, ThreatProtectorConstants.HTTP_SC_CODE, message
                    + e.getMessage());
        } finally {
            // return analyzer to the pool
            AnalyzerHolder.returnObject(apimThreatAnalyzer);
        }
        if (validRequest) try {
            RelayUtils.buildMessage(axis2MC);
        } catch (IOException | XMLStreamException e) {
            GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE, e.getMessage());
        }
        return true;
    }

    /**
     * This configureSchemaProperties method bind the json_validator sequence properties for the JsonConfig object.
     *
     * @param messageContext This message context contains the request message properties of the relevant API which was
     *                       enabled the JSON_Validator message mediation in flow.
     * @return JSONConfig contains the json schema properties need to be validated.
     */
    private JSONConfig configureSchemaProperties(MessageContext messageContext) {
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
            String errorEMessage = "Json schema maxProperty count is missing";
            ThreatExceptionHandler.handleException(messageContext, errorEMessage);
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
            String errorMessage = "Json schema max array element count is missing.";
            ThreatExceptionHandler.handleException(messageContext, errorMessage);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_KEY_LENGTH);
        if (messageProperty != null) {
            keyLength = Integer.parseInt(messageProperty.toString());
        } else {
            String errorMessage = "Json schema maximum key length is missing.";
            ThreatExceptionHandler.handleException(messageContext, errorMessage);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_JSON_DEPTH);
        if (messageProperty != null) {
            maxJSONDepth = Integer.parseInt(messageProperty.toString());
        } else {
            String errorMessage = "Json schema maximum JSON depth is missing";
            ThreatExceptionHandler.handleException(messageContext, errorMessage);
        }
        if (log.isDebugEnabled()) {
            log.debug(("Max Priority count is:" + propertyCount) + ", " +
                    "Max String length is: " + stringLength + ", " +
                    "Max Array element count: " + arrayElementCount + ", " +
                    "Max Key Length: " + keyLength + ", " +
                    "Max JSON depth is:" + maxJSONDepth + ", ");
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
     * This method checks the status of the {enabledCheckBody} property which comes from the custom sequence.
     * If a client ask to check the message body,Method returns true else It will return false.
     * If the {isContentAware} method returns false, The request message payload wont be build.
     * Building a payload will directly affect to the performance.
     *
     * @return If enabledCheckBody is true,The method returns true else it returns false
     */
    public boolean isContentAware() {
        return false;
    }

}
