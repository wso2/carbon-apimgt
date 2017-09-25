/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.common.codegen;

import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.languages.StaticDocCodegen;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The class responsible for manipulating swagger code gen StaticDocCodegen to cater APIM HTML doc gen requirements.
 */
public class DynamicHtmlGen extends StaticDocCodegen {

    private Map<String, String> tagToSanitizedMap;

    public DynamicHtmlGen() {
        tagToSanitizedMap = new HashMap<>();
    }

    @Override
    public void preprocessSwagger(Swagger swagger) {
        super.preprocessSwagger(swagger);
        for (Path path : swagger.getPaths().values()) {
            cleanExtraTags(path.getGet());
            cleanExtraTags(path.getPost());
            cleanExtraTags(path.getPut());
            cleanExtraTags(path.getHead());
            cleanExtraTags(path.getOptions());
            cleanExtraTags(path.getPatch());
            cleanExtraTags(path.getDelete());
        }
    }

    @Override
    public String toApiName(String name) {
        return tagToSanitizedMap.getOrDefault(name, name);
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

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input;
    }

    @Override
    public String escapeQuotationMark(String input) {
        return input;
    }

    /**
     * Keep a single tag for a given operation
     * 
     * @param operation swagger operation for a path
     */
    private void cleanExtraTags(Operation operation) {
        if (operation != null) {
            List<String> tags = operation.getTags();
            if (tags != null) {
                int size = tags.size();
                for (int i = 1; i < size; i++) {
                    tags.remove(1); //If we remove element 1, next element will become element 1
                }
            }
        }
    }

}
