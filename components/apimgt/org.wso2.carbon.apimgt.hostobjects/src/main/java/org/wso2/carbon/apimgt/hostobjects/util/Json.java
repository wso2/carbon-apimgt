/*
*  Copyright (c) 2005-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*This is a temporary solution to convert swagger object to json object. This class will be deleted
 once a fixed swagger parser version is released */
public class Json {

    private static ObjectMapper mapper;

    private static final Log log = LogFactory.getLog(Json.class);

    public static ObjectMapper mapper() {
        if (mapper == null) {
            mapper = ObjectMapperFactory.createJson();
        }
        return mapper;
    }

    public static ObjectWriter pretty() {
        return mapper().writer(new DefaultPrettyPrinter());
    }

    public static String pretty(Object o) {
        try {
            return pretty().writeValueAsString(o);
        } catch (Exception e) {
            log.error("Error occurred while attempting to pretty the object ", e);
            return null;
        }
    }

    /*
        TODO the following code is a hack to get past the fact that Path and Response are not interfaces, it can be deleted as part of the refactor to make Path and Response interfaces
        pathMapper and responseMapper are ObjectMappers that are only going to be used during deserialization of Paths and Responses.
        We need them because:
         1) RefPath extends Path
         2) RefResponse extends Response
         And when we detect we are deserializing a "normal" Path or Response (e.g. its not a ref) we need skip
         the PathDeserializer and ResponseDeserializer logic, lest we get into a stack overflow problem
     */
    private static ObjectMapper pathMapper;
    private static ObjectMapper responseMapper;

    protected static ObjectMapper pathMapper() {
        if (pathMapper == null) {
            pathMapper = ObjectMapperFactory.createJson(false, true);
        }

        return pathMapper;
    }

    protected static ObjectMapper responseMapper() {
        if (responseMapper == null) {
            responseMapper = ObjectMapperFactory.createJson(false, false);
        }

        return responseMapper;
    }
}