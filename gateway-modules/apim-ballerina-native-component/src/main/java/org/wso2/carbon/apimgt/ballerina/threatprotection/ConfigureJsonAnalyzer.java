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
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.JSONConfig;

/**
 * Native Function org.wso2.carbon.apimgt.ballerina.threatprotection:configureJsonAnalyzer
 * This function is used to configure the json analyzers.
 */
@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.threatprotection",
        functionName = "configureJsonAnalyzer",
        args = { @Argument(name = "jsonInfo", type = TypeEnum.STRUCT, structType = "JSONThreatProtectionInfoDTO"),
                @Argument(name = "event", type = TypeEnum.STRING)},
        returnType = { @ReturnType(type = TypeEnum.BOOLEAN)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Configures the json analyzers")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "jsonInfo",
        value = "JSONThreatProtectionInfoDTO struct")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "event",
        value = "Threat Protection Policy Event")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "boolean",
        value = "true if success, false otherwise")})
public class ConfigureJsonAnalyzer extends AbstractNativeFunction {
    private static final String THREAT_PROTECTION_POLICY_ADD = "THREAT_PROTECTION_POLICY_ADD";
    private static final String THREAT_PROTECTION_POLICY_DELETE = "THREAT_PROTECTION_POLICY_DELETE";
    private static final String THREAT_PROTECTION_POLICY_UPDATE = "THREAT_PROTECTION_POLICY_UPDATE";

    private static Logger log = LoggerFactory.getLogger(ConfigureJsonAnalyzer.class);

    @Override
    public BValue[] execute(Context context) {
        String event = getStringArgument(context, 0);
        //configure json analyzer
        BStruct jsonInfo = ((BStruct) getRefArgument(context, 0));
        if (jsonInfo != null) {
            String jsonPolicyId = jsonInfo.getStringField(0);
            switch (event) {
                case THREAT_PROTECTION_POLICY_ADD:
                case THREAT_PROTECTION_POLICY_UPDATE:
                    String name = jsonInfo.getStringField(1);
                    int propertyCount = (int) jsonInfo.getIntField(0);
                    int stringLength = (int) jsonInfo.getIntField(1);
                    int arrayElementCount = (int) jsonInfo.getIntField(2);
                    int keyLength = (int) jsonInfo.getIntField(3);
                    int maxJSONDepth = (int) jsonInfo.getIntField(4);

                    JSONConfig jsonConfig = new JSONConfig();
                    jsonConfig.setName(name);
                    jsonConfig.setMaxPropertyCount(propertyCount);
                    jsonConfig.setMaxStringLength(stringLength);
                    jsonConfig.setMaxArrayElementCount(arrayElementCount);
                    jsonConfig.setMaxKeyLength(keyLength);
                    jsonConfig.setMaxJsonDepth(maxJSONDepth);
                    //put into ConfigurationHolder
                    ConfigurationHolder.addJsonConfig(jsonPolicyId, jsonConfig);
                    break;

                case THREAT_PROTECTION_POLICY_DELETE:
                    ConfigurationHolder.removeJsonConfig(jsonPolicyId);
                    break;

                default:
                    log.warn("Unknown event type for Threat Protection Policy. Event: " + event);
                    break;
            }
        }

        return getBValues(new BBoolean(true));
    }
}
