/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.ballerina.threatprotection;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.ConfigurationHolder;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.XMLConfig;

/**
 * Native Function org.wso2.carbon.apimgt.ballerina.threatprotection:configureXmlAnalyzer
 * This function is used to configure the xml analyzers.
 */
@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.threatprotection",
        functionName = "configureXmlAnalyzer",
        args = {@Argument(name = "xmlInfo", type = TypeEnum.STRUCT, structType = "XMLThreatProtectionInfoDTO"),
                @Argument(name = "event", type = TypeEnum.STRING)},
        returnType = { @ReturnType(type = TypeEnum.BOOLEAN)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Configures the xml analyzers ")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "xmlInfo",
        value = "XMLThreatProtectionInfoDTO struct")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "event",
        value = "Threat Protection Policy Event")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "boolean",
        value = "true if success, false otherwise")})
public class ConfigureXmlAnalyzer extends AbstractNativeFunction {
    private static final String THREAT_PROTECTION_POLICY_ADD = "THREAT_PROTECTION_POLICY_ADD";
    private static final String THREAT_PROTECTION_POLICY_DELETE = "THREAT_PROTECTION_POLICY_DELETE";
    private static final String THREAT_PROTECTION_POLICY_UPDATE = "THREAT_PROTECTION_POLICY_UPDATE";

    private static final Logger log = LoggerFactory.getLogger(ConfigureXmlAnalyzer.class);

    @Override
    public BValue[] execute(Context context) {
        String event = getStringArgument(context, 0);
        BStruct xmlInfo = ((BStruct) getRefArgument(context, 0));
        if (xmlInfo != null) {
            String xmlPolicyId = xmlInfo.getStringField(0);
            switch (event) {
                case THREAT_PROTECTION_POLICY_ADD:
                case THREAT_PROTECTION_POLICY_UPDATE:
                    String name = xmlInfo.getStringField(1);
                    boolean dtdEnabled = xmlInfo.getBooleanField(0) != 0;
                    boolean externalEntitiesEnabled = xmlInfo.getBooleanField(1) != 0;
                    int maxXMLDepth = (int) xmlInfo.getIntField(0);
                    int elementCount = (int) xmlInfo.getIntField(1);
                    int attributeCount = (int) xmlInfo.getIntField(2);
                    int attributeLength = (int) xmlInfo.getIntField(3);
                    int entityExpansionLimit = (int) xmlInfo.getIntField(4);
                    int childrenPerElement = (int) xmlInfo.getIntField(5);

                    XMLConfig xmlConfig = new XMLConfig();
                    xmlConfig.setName(name);
                    xmlConfig.setDtdEnabled(dtdEnabled);
                    xmlConfig.setExternalEntitiesEnabled(externalEntitiesEnabled);
                    xmlConfig.setMaxDepth(maxXMLDepth);
                    xmlConfig.setMaxElementCount(elementCount);
                    xmlConfig.setMaxAttributeCount(attributeCount);
                    xmlConfig.setMaxAttributeLength(attributeLength);
                    xmlConfig.setEntityExpansionLimit(entityExpansionLimit);
                    xmlConfig.setMaxChildrenPerElement(childrenPerElement);

                    //put into ConfigurationHolder
                    ConfigurationHolder.addXmlConfig(xmlPolicyId, xmlConfig);
                    break;

                case THREAT_PROTECTION_POLICY_DELETE:
                    ConfigurationHolder.removeXmlConfig(xmlPolicyId);
                    break;

                default:
                    log.warn("Unknown event type for XML Threat Protection Policy. Event: " + event);
                    break;
            }
        }

        return getBValues(new BBoolean(true));
    }
}
