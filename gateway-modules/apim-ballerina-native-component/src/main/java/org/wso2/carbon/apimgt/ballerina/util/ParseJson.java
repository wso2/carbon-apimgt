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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Ballerina function to set a message property.
 * <br>
 * ballerina.model.messages:getProperty
 */
@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.util",
        functionName = "parse",
        args = {@Argument(name = "value", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.JSON)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Retrieve a message property")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "value",
        value = "The name of the property")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "json",
        value = "")})
public class ParseJson extends AbstractNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(ParseJson.class);

    @Override
    public BValue[] execute(Context context) {
        String jsonValue = getStringArgument(context, 0);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = null;
        try {
            actualObj = mapper.readTree(jsonValue);
        } catch (IOException e) {
            String msg = "Error while convert into json";
            log.error(msg, e);
            throw new BallerinaException(msg + e.getMessage());

        }
        return getBValues(new BJSON(actualObj));
    }
}
