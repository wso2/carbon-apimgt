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
package org.wso2.carbon.apimgt.ballerina.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Ballerina function to convert ip into Long value
 * <br>
 * org.wso2.carbon.apimgt.ballerina.util:convertIpToLong
 */
@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.util",
        functionName = "convertIpToLong",
        args = {@Argument(name = "value", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Convert ip into long value")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "value",
        value = "String value of ip")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int",
        value = "long value of ip")})
public class IPToLongConversion extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String value = getStringArgument(context, 0);
        long longValue = Util.ipToLong(value);
        return getBValues(new BInteger(longValue));
    }

}
