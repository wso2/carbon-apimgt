/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.common.config;

import net.consensys.cava.toml.Toml;
import net.consensys.cava.toml.TomlArray;
import net.consensys.cava.toml.TomlParseResult;
import net.consensys.cava.toml.TomlTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Toml parser class to parse the configuration toml
 */
public class TomlParser {

    private static final Log log = LogFactory.getLog(TomlParser.class);

    private TomlParser() {

    }

    static Map<String, Object> parse(String filePath) throws ConfigParserException {
        try {
            TomlParseResult parseResult = Toml.parse(Paths.get(filePath));
            if (parseResult.hasErrors()) {
                parseResult.errors().forEach(error -> log.error(error.toString()));
                throw new ConfigParserException("Error parsing deployment configuration");
            }
            return parseToml(parseResult);
        } catch (IOException e) {
            throw new ConfigParserException("Error parsing file " + filePath, e);
        }
    }

    private static Map<String, Object> parseToml(TomlParseResult result) {
        Map<String, Object> templateContext = new LinkedHashMap<>();
        Set<String> dottedKeySet = result.dottedKeySet();
        for (String dottedKey : dottedKeySet) {
            dottedKey = dottedKey.replaceAll("\"", "'");
            String undottedKey = dottedKey.replaceAll("\\.","_");
            templateContext.put(undottedKey, getValue(result.get(dottedKey)));
        }
        return templateContext;
    }

    private static Object getValue(Object value) {
        Object returnValue;
        if (value instanceof TomlArray) {
            returnValue = processTomlArray((TomlArray) value);
        } else if (value instanceof TomlTable) {
            returnValue = processTomlMap((TomlTable) value);
        } else {
            returnValue = value;
        }
        return returnValue;
    }

    private static Map<String, Object> processTomlMap(TomlTable tomlTable) {

        Map<String, Object> finalMap = new LinkedHashMap<>();
        Set<String> dottedKeySet = tomlTable.dottedKeySet();
        for (String key : dottedKeySet) {
            // To support single quoted keys in the toml inside an array.
            // Eg: [[a.b]]
            //     'c.d' = "value"
            key = key.replaceAll("\"", "'");
            Object value = tomlTable.get(key);
            if (value instanceof TomlArray) {
                finalMap.put(key, processTomlArray((TomlArray) value));
            } else {
                finalMap.put(key, tomlTable.get(key));
            }
        }

        return finalMap;
    }

    private static List<Object> processTomlArray(TomlArray value) {

        List<Object> finalList = new ArrayList<>();
        List<Object> tomlList = value.toList();
        for (Object obj : tomlList) {
            if (obj instanceof TomlArray) {
                finalList.add(processTomlArray((TomlArray) obj));
            } else if (obj instanceof TomlTable) {
                finalList.add(processTomlMap((TomlTable) obj));
            } else {
                finalList.add(obj);
            }
        }
        return finalList;
    }
}
