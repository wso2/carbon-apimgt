/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.json.XML;
import org.wso2.carbon.apimgt.gateway.threatprotection.APIMThreatAnalyzerException;
import org.wso2.carbon.apimgt.gateway.threatprotection.AnalyzerHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.gateway.threatprotection.analyzer.APIMThreatAnalyzer;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.ConfigurationHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.JSONConfig;

/**
 * This mediator would protect the backend resources from the JSON threat vulnerabilities by validating the
 * JSON schema.
 */
public class JsonSchemaValidator extends AbstractMediator {
    private static final Log log = LogFactory.getLog(DigestAuthMediator.class);
    org.apache.axis2.context.MessageContext axis2MC;

    /**
     * This mediate method performs JSONAnalyzer configurations and validating the message body.
     * @param messageContext This message context contains the request message properties of the relevant API which was
     *                       enabled the JSON_Validator message mediation in flow.
     * @return a boolean true if the message content is passed the json schema criticisers.
     */
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("JSON schema validation mediator is activated...");
        }

        axis2MC  = ((Axis2MessageContext)messageContext).getAxis2MessageContext();
        String apiPath = axis2MC.getProperty(ThreatProtectorConstants.TRANSPORT_URL).toString();
        String contentType = axis2MC.getProperty(ThreatProtectorConstants.CONTENT_TYPE).toString();
        String requestMethod = axis2MC.getProperty(ThreatProtectorConstants.HTTP_REQUEST_METHOD).toString();
        String apiContext =  messageContext.getProperty(ThreatProtectorConstants.API_CONTEXT).toString();

        // get the values from sequence and set to the config
        boolean enabled = Boolean.valueOf(messageContext.getProperty("enabled").toString());
        int propertyCount = Integer.valueOf(messageContext.getProperty("maxPropertyCount").toString());
        int stringLength = Integer.valueOf(messageContext.getProperty("maxStringLength").toString());
        int arrayElementCount = Integer.valueOf(messageContext.getProperty("maxArrayElementCount").toString());
        int keyLength = Integer.valueOf(messageContext.getProperty("maxKeyLength").toString());
        int maxJSONDepth = Integer.valueOf(messageContext.getProperty("maxJsonDepth").toString());

        JSONConfig jsonConfig = new JSONConfig();
        jsonConfig.setEnabled(enabled);
        jsonConfig.setMaxPropertyCount(propertyCount);
        jsonConfig.setMaxStringLength(stringLength);
        jsonConfig.setMaxArrayElementCount(arrayElementCount);
        jsonConfig.setMaxKeyLength(keyLength);
        jsonConfig.setMaxJsonDepth(maxJSONDepth);
        //put into ConfigurationHolder
        ConfigurationHolder.addJsonConfig(apiPath, jsonConfig);

        String jsonElements = axis2MC.getEnvelope().getBody().getFirstElement().toString();
        String jsonPayload = XML.toJSONObject(jsonElements).toString();

        if(!requestMethod.equals("GET")) {

            APIMThreatAnalyzer apimThreatAnalyzer = AnalyzerHolder.getAnalyzer(contentType,apiPath);
            try {
                apimThreatAnalyzer.analyze(jsonPayload, apiContext);
            } catch (APIMThreatAnalyzerException e) {
                log.error(e.getMessage());
            }
            //return analyzer to the pool
            AnalyzerHolder.returnObject(apimThreatAnalyzer);
        }
        return false;
    }
}
