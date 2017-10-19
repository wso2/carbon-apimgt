/*
*  Copyright (c) 2005-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.hostobjects.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class TestUtils {

    private static <T> T deserializeFileFromClasspath(String path, Class<T> type, ObjectMapper objectMapper) {
        final InputStream resource = TestUtils.class.getClassLoader().getResourceAsStream(path);

        String contents;

        if (resource == null) {
            throw new RuntimeException("Could not find file on the classpath: " + path);
        }

        try {
            contents = IOUtils.toString(resource);
        } catch (IOException e) {
            throw new RuntimeException("could not read from file " + path, e);
        }

        try {
            T result = objectMapper.readValue(contents, type);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Could not deserialize contents into type: " + type, e);
        }
    }

    public static <T> T deserializeJsonFileFromClasspath(String path, Class<T> type) {
        return deserializeFileFromClasspath(path, type, Json.mapper());
    }

    public static <T> T deserializeYamlFileFromClasspath(String path, Class<T> type) {
        return deserializeFileFromClasspath(path, type, Yaml.mapper());
    }
}