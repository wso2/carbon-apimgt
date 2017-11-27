/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.ballerina.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Returns an array of keys contained in the specified JSON.
 * If the JSON is not an object type element, then this method will return an empty array.
 *
 * @since 0.90
 */
@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.util",
        functionName = "getKeys",
        args = {@Argument(name = "json", type = TypeEnum.JSON)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns an array of keys contained in the specified JSON")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "json",
        value = "JSON object")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string[]",
        value = "string[] of keys")})

public class GetKeys extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(GetKeys.class);

    @Override
    public BValue[] execute(Context ctx) {

        List<String> keys = new ArrayList<String>();

        BJSON json = (BJSON) getRefArgument(ctx, 0);
        JsonNode node = json.value();

        if (node.getNodeType() != JsonNodeType.OBJECT) {
            return getBValues(new BStringArray());
        }

        Iterator<String> keysItr = ((ObjectNode) node).fieldNames();
        while (keysItr.hasNext()) {
            keys.add(keysItr.next());
        }

        return getBValues(new BStringArray(keys.toArray(new String[keys.size()])));
    }
}
