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
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


/**
 * Ballerina function to return a sorted string array of json file names in a given folder.
 * <br>
 * Returns an empty array if the folder does not exist or when there are no json files inside
 * org.wso2.carbon.apimgt.ballerina.util:listFiles
 */
@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.util",
        functionName = "listJSONFiles",
        args = {@Argument(name = "folderPath", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns an array of json file names")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "folderPath",
        value = "The path of the folder")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "list",
        value = "json file array")})

public class ListFiles extends AbstractNativeFunction  {

    @Override
    public BValue[] execute(Context context) {
        String directoryPath = getStringArgument(context, 0);
        File directory = new File(directoryPath);
        File[] fList = directory.listFiles();
        List<String> list = new ArrayList<String>();
        if (fList != null) {
            for (File file : fList) {
                if (file.isFile() && file.getName().toLowerCase(Locale.ENGLISH).endsWith(".json")) {
                    list.add(file.getName());
                }
            }
            Collections.sort(list, Comparator.naturalOrder());
            BStringArray balArray = new BStringArray();
            int i = 0;
            while (i < list.size()) {
                balArray.add(i, list.get(i));
                i = i + 1;
            }
            return getBValues(balArray);
        } else {
            return getBValues(new BStringArray());
        }
    }
}
