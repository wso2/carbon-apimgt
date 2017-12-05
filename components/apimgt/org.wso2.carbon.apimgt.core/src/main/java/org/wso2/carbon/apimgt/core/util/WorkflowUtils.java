/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



/**
 * Utility class for workflow related tasks.
 */
public class WorkflowUtils {

    private static final Logger log = LoggerFactory.getLogger(WorkflowUtils.class);

    private WorkflowUtils() {
    }

    /**
     * Convert json string to a map object
     * 
     * @param json json string
     * @return Map Map with key value pairs in the json string
     * @throws ParseException if provided string cannot be parsed
     */
    public static Map<String, String> jsonStringToMap(String json) throws ParseException {
        if (json == null || json.isEmpty()) {
            return null;
        } else {
            ContainerFactory containerFactory = new ContainerFactory() {
                @Override
                public List creatArrayContainer() {
                    return new LinkedList();
                }

                @Override
                public Map createObjectContainer() {
                    return new HashMap();
                }
            };
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json, containerFactory);
            return (HashMap) obj;
        }
    }

    /**
     * Convert java map to json string
     * 
     * @param map map object
     * @return String json string
     */
    public static String mapTojsonString(Map<String, String> map) {

        JSONObject jsonObj = new JSONObject();
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                jsonObj.put(entry.getKey(), entry.getValue());
            }

            return jsonObj.toJSONString();
        }
        return null;
    }    
}
