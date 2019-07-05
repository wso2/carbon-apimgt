/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.util.codegen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.languages.StaticDocCodegen;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.util.DeserializationUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DynamicHtmlGen extends StaticDocCodegen {
    
    private Map<String, String> tagToSanitizedMap;
    
    public DynamicHtmlGen () {
        tagToSanitizedMap = new HashMap<>();
    }
    
    @Override
    public String toApiName(String name) {
        if (tagToSanitizedMap.containsKey(name)) {
            return tagToSanitizedMap.get(name);   
        } else {
            return name;
        }
    }
    
    @Override
    public String sanitizeTag(String tag) {
        String sanitizedTag = super.sanitizeTag(tag);
        tagToSanitizedMap.put(sanitizedTag, tag);
        return sanitizedTag;
    }

    @Override
    public String toApiFilename(String name) {
        return sanitizeTag(name);
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation,
            Map<String, Model> definitions, Swagger swagger) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, definitions, swagger);
        op.summary = operation.getSummary();
        op.notes = operation.getDescription();

        String resourcesPath = new File(inputSpec).getParent();
        try {
            LinkedHashMap xExamples = (LinkedHashMap)operation.getVendorExtensions().get("x-examples");
            if (xExamples != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode exampleNode;
                String ref = (String)xExamples.get("$ref");
                String[] segments = ref.split("#/");
                if (segments.length >= 2) {
                    File exampleFile = new File(resourcesPath + File.separator + segments[0]);
                    String content = FileUtils.readFileToString(exampleFile);
                    JsonNode rootNode = DeserializationUtils.readYamlTree(content);
                    exampleNode = rootNode.get(segments[1]);
                    if (exampleNode == null) {
                        throw new RuntimeException("Could not find element '" + segments[1] + "' in " + exampleFile);
                    }
                } else {
                    File exampleFile = new File(resourcesPath + File.separator + ref);
                    String content = FileUtils.readFileToString(exampleFile);
                    exampleNode = DeserializationUtils.readYamlTree(content);
                }

                ArrayList result = mapper.convertValue(exampleNode, ArrayList.class);

                for (Object o: result) {
                    LinkedHashMap example = (LinkedHashMap)o;
                    LinkedHashMap request = (LinkedHashMap)example.get("request");
                    LinkedHashMap response = (LinkedHashMap)example.get("response");
                    if (request != null) {
                        StringBuilder builder = new StringBuilder();
                        String method = (String)request.get("method");
                        String url = (String)request.get("url");
                        builder.append(method);
                        builder.append(" ");
                        builder.append(url);
                        builder.append(" HTTP 1/1\n");

                        StringBuilder curlBuilder = new StringBuilder();
                        boolean hasNoCurl = false;
                        if (example.get("curl") == null) {
                            hasNoCurl = true;
                            curlBuilder.append("curl -k -v -X ");
                            curlBuilder.append(method);
                            curlBuilder.append(" '");
                            curlBuilder.append(url);
                            curlBuilder.append("' ");

                        }
                        String headers = (String) request.get("headers");
                        if (headers != null) {
                            builder.append(headers);
                            String[] headerArray = headers.split("\n");
                            if (hasNoCurl) {
                                for (String header : headerArray) {
                                    curlBuilder.append("-H '");
                                    curlBuilder.append(header);
                                    curlBuilder.append("' ");
                                }
                            }
                        }

                        LinkedHashMap body = (LinkedHashMap) request.get("body");
                        if (body != null) {
                            String jsonBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
                            builder.append("\n");
                            builder.append(jsonBody);

                            if (hasNoCurl) {
                                curlBuilder.append("-d @payload.json");
                            }
                        }

                        if (hasNoCurl) {
                            example.put("curl", curlBuilder.toString());
                        }

                        example.put("rawRequest", builder.toString());
                    }

                    if (response != null) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("HTTP 1/1 ");
                        LinkedHashMap status = (LinkedHashMap)response.get("status");
                        builder.append(status.get("code"));
                        builder.append(" ");
                        builder.append(status.get("msg"));
                        builder.append("\n");
                        String headers = (String) response.get("headers");
                        if (headers != null) {
                            builder.append(headers);
                        }

                        LinkedHashMap body = (LinkedHashMap) response.get("body");
                        if (body != null) {
                            String jsonBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
                            builder.append("\n");
                            builder.append(jsonBody);
                        }
                        example.put("rawResponse", builder.toString());
                    }
                }
                operation.getVendorExtensions().put("x-examples", result);
            }

            List<Map<String, List<String>>> security = operation.getSecurity();
            if (security != null) {
                List<String> scopes = security.get(0).get("OAuth2Security");
                if (scopes.size() > 0) {
                    operation.getVendorExtensions().put("x-scopes", scopes);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading example file", e);
        }
        return op;
    }

    @Override
    public CodegenParameter fromParameter(Parameter param, Set<String> imports) {
        CodegenParameter parameter = super.fromParameter(param, imports);
        parameter.description = param.getDescription();
        return parameter;
    }

    /* Uncomment once swagger-code-gen version updated to 2.2.0.wso2v1
    @Override
    public String escapeUnsafeCharacters(String input) {
        return input;
    }

    @Override
    public String escapeQuotationMark(String input) {
        return input;
    }*/
}