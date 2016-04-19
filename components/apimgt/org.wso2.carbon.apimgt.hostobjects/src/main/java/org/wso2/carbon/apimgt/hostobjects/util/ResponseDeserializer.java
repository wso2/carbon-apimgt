/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.models.RefResponse;
import io.swagger.models.Response;
import org.wso2.carbon.apimgt.hostobjects.util.Json;

import java.io.IOException;
/*This is a temporary solution to convert swagger object to json object. This class will be deleted
 once a fixed swagger parser version is released */
public class ResponseDeserializer extends JsonDeserializer<Response> {
    @Override public Response deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode sub = node.get("$ref");

        if (sub != null) {
            return Json.mapper().convertValue(node, RefResponse.class);
        } else {
            Response response = Json.responseMapper().convertValue(node, Response.class);
            return response;
        }
    }
}