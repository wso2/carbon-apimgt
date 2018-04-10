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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.wso2.carbon.apimgt.ballerina.threatprotection.analyzer.APIMThreatAnalyzer;

/**
 * Native Function org.wso2.carbon.apimgt.ballerina.threatprotection:analyze
 * This function is used to analyze xml/json payloads for malicious content.
 */

@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.threatprotection",
        functionName = "analyze",
        args = { @Argument(name = "payloadType", type = TypeEnum.STRING),
                 @Argument(name = "payload", type = TypeEnum.STRING),
                 @Argument(name = "apiContext", type = TypeEnum.STRING),
                 @Argument(name = "policyId", type = TypeEnum.STRING)},
        returnType = { @ReturnType(type = TypeEnum.BOOLEAN),
                       @ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Analyzes json/xml payloads for threats")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "payloadType",
        value = "Type of the payload (xml/json)")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "payload",
        value = "Payload string")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "apiContext",
        value = "API Context")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "apiId",
        value = "API ID")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "boolean",
        value = "true if no threats detected, false otherwise")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "error information if found")})
public class Analyze extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {

        String payloadType = getStringArgument(context, 0);
        String payload = getStringArgument(context, 1);
        String apiContext = getStringArgument(context, 2);
        String policyId = getStringArgument(context, 3);

        APIMThreatAnalyzer analyzer = AnalyzerHolder.getAnalyzer(payloadType, policyId);
        if (analyzer == null) {
            return getBValues(new BBoolean(true), new BString(""));
        }

        boolean noThreatsDetected = true;
        String errMessage = null;
        try {
            analyzer.analyze(payload, apiContext);
        } catch (APIMThreatAnalyzerException e) {
            noThreatsDetected = false;
            errMessage = e.getMessage();
        }

        AnalyzerHolder.returnObject(analyzer);
        return getBValues(new BBoolean(noThreatsDetected), new BString(errMessage));
    }
}
