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

import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.languages.StaticDocCodegen;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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