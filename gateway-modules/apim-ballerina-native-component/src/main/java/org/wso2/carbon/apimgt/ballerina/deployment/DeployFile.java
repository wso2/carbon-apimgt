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
package org.wso2.carbon.apimgt.ballerina.deployment;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.ballerina.util.Util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Native function org.wso2.carbon.apimgt.ballerina.deployment.DeployFile.{@link DeployFile}
 * This function will create ballerina file in the FS.
 *
 * @since 0.10-SNAPSHOT
 */
@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.deployment",
        functionName = "deploy",
        args = {@Argument(name = "fileName", type = TypeEnum.STRING),
                @Argument(name = "config", type = TypeEnum.STRING),
                @Argument(name = "path", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = " deployment service")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "fileName",
        value = "path to the service file")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "config",
        value = "ballerina source")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "path",
        value = "ballerina package")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "status of the deployment")})
public class DeployFile extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(DeployFile.class);
    private static Path programDirPath = Paths.get(System.getProperty("user.dir"));

    @Override
    public BValue[] execute(Context context) {
        String fileName = getStringArgument(context, 0);
        String config = getStringArgument(context, 1);
        String packageName = getStringArgument(context, 2);

        Path path = Paths.get(packageName);
        String filePath = path.toAbsolutePath() + File.separator + fileName;
        if (Util.saveFile(filePath, config)) {
            log.info("write config to File system");

        } else {
            log.error("Error saving API configuration in " + path);
        }
        return new BValue[0];
    }
}

