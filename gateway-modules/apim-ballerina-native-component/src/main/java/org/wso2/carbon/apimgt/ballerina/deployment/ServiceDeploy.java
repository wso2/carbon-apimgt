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

import org.apache.commons.io.IOUtils;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Native function org.wso2.carbon.apimgt.ballerina.deployment.ServiceDeploy.{@link ServiceDeploy}
 * This function will create ballerina file in the FS.
 *
 * @since 0.10-SNAPSHOT
 */
@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.deployment",
        functionName = "deployService",
        args = {@Argument(name = "fileName", type = TypeEnum.STRING),
                @Argument(name = "config", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = " deployment service")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "fileName",
        value = "path to the service file")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "config",
        value = "ballerina source")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "status of the deployment")})
public class ServiceDeploy extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(ServiceDeploy.class);

    @Override
    public BValue[] execute(Context context) {
        String fileName = getArgument(context, 0).stringValue();
        String config = getArgument(context, 1).stringValue();
        String path = System.getProperty("ballerina.home") + "/org/wso2/carbon/apimgt/gateway/" + fileName;
        if (saveApi(path, config)) {
            log.info("write config to File system");
        } else {
            log.error("Error saving API configuration in " + path);
        }

        return new BValue[0];
    }

    /**
     * Save API into FS
     *
     * @param path    file path
     * @param content API config
     */
    private boolean saveApi(String path, String content) {

        try (OutputStream outputStream = new FileOutputStream(path)) {
            IOUtils.write(content, outputStream, "UTF-8");
            return true;
        } catch (IOException e) {
            log.error("Error saving API configuration in " + path, e);
        }
        return false;
    }
}

