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

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.threatprotection.APIMThreatAnalyzerException;
import org.wso2.carbon.apimgt.gateway.threatprotection.AnalyzerHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.gateway.threatprotection.analyzer.APIMThreatAnalyzer;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.ConfigurationHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.XMLConfig;


public class XMLSchemaValidator extends AbstractMediator {
    org.apache.axis2.context.MessageContext axis2MC;

    public boolean mediate(MessageContext messageContext) {

        axis2MC  = ((Axis2MessageContext)messageContext).getAxis2MessageContext();
        String apiPath = axis2MC.getProperty(ThreatProtectorConstants.TRANSPORT_URL).toString();
        String payload = axis2MC.getEnvelope().getBody().getFirstElement().toString();
        String requestMethod = axis2MC.getProperty("HTTP_METHOD_OBJECT").toString();
        String contentType = axis2MC.getProperty("synapse.internal.rest.contentType").toString();
        String apiContext =  messageContext.getProperty("REST_API_CONTEXT").toString();

        boolean enabled = Boolean.valueOf(messageContext.getProperty("enabled").toString());
        boolean dtdEnabled = Boolean.valueOf(messageContext.getProperty("dtdEnabled").toString());
        boolean externalEntitiesEnabled = Boolean.valueOf(messageContext.
                getProperty("externalEntitiesEnabled").toString());
        int maxXMLDepth = Integer.valueOf(messageContext.getProperty("maxElementCount").toString());
        int elementCount = Integer.valueOf(messageContext.getProperty("maxAttributeCount").toString());
        int attributeCount = Integer.valueOf(messageContext.getProperty("maxAttributeLength").toString());
        int maxAttributeLength = Integer.valueOf(messageContext.getProperty("maxAttributeLength").toString());
        int entityExpansionLimit = Integer.valueOf(messageContext.getProperty("entityExpansionLimit").toString());
        int childrenPerElement = Integer.valueOf(messageContext.getProperty("maxChildrenPerElement").toString());

        XMLConfig xmlConfig = new XMLConfig();
        xmlConfig.setEnabled(enabled);
        xmlConfig.setDtdEnabled(dtdEnabled);
        xmlConfig.setExternalEntitiesEnabled(externalEntitiesEnabled);
        xmlConfig.setMaxDepth(maxXMLDepth);
        xmlConfig.setMaxElementCount(elementCount);
        xmlConfig.setMaxAttributeCount(attributeCount);
        xmlConfig.setMaxAttributeLength(maxAttributeLength);
        xmlConfig.setEntityExpansionLimit(entityExpansionLimit);
        xmlConfig.setMaxChildrenPerElement(childrenPerElement);

        //put into ConfigurationHolder
        ConfigurationHolder.addXmlConfig(apiPath, xmlConfig);

        if(!requestMethod.equals("GET")) {

            APIMThreatAnalyzer apimThreatAnalyzer = AnalyzerHolder.getAnalyzer(contentType, apiPath);
            try {
                apimThreatAnalyzer.analyze(payload, apiContext);
            } catch (APIMThreatAnalyzerException e) {
                log.error(""+ e.getMessage());
            }
            //return analyzer to the pool
            AnalyzerHolder.returnObject(apimThreatAnalyzer);
        }
        return false;
    }

}
