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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.wso2.carbon.apimgt.hostobjects.util.Json;

import java.io.IOException;
/*This is a temporary solution to convert swagger object to json object. This class will be deleted
 once a fixed swagger parser version is released */
public class SecurityDefinitionDeserializer extends JsonDeserializer<SecuritySchemeDefinition> {
    @Override public SecuritySchemeDefinition deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        SecuritySchemeDefinition result = null;

        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode inNode = node.get("type");

        if (inNode != null) {
            String type = inNode.asText();
            if ("basic".equals(type)) {
                result = Json.mapper().convertValue(node, BasicAuthDefinition.class);
            } else if ("apiKey".equals(type)) {
                result = Json.mapper().convertValue(node, ApiKeyAuthDefinition.class);
            } else if ("oauth2".equals(type)) {
                result = Json.mapper().convertValue(node, OAuth2Definition.class);
            }
        }

        return result;
    }
}