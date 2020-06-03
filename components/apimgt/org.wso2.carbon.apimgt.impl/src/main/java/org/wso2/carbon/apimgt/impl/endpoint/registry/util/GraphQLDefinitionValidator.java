/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.endpoint.registry.util;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import graphql.schema.idl.errors.SchemaProblem;
import graphql.schema.validation.SchemaValidationError;
import graphql.schema.validation.SchemaValidator;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.api.endpoint.registry.api.DefinitionValidationException;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * This class provides the functionality of validating GraphQL definitions
 */
public class GraphQLDefinitionValidator implements DefinitionValidator {

    @Override
    public boolean validate(URL definitionUrl) throws DefinitionValidationException {
        boolean isValid;
        try {
            String definitionContent = IOUtils.toString(definitionUrl.openStream());
            isValid = validate(definitionContent);
        } catch (IOException e) {
            throw new DefinitionValidationException("Error in reading content in the definition URL: "
                    + definitionUrl, e);
        }
        return isValid;
    }

    @Override
    public boolean validate(byte[] definition) throws DefinitionValidationException {
        String definitionContent = new String(definition);
        return validate(definitionContent);
    }

    @Override
    public boolean validate(String definition) throws DefinitionValidationException {
        boolean isValid;
        try {
            SchemaParser schemaParser = new SchemaParser();
            TypeDefinitionRegistry typeRegistry = schemaParser.parse(definition);
            GraphQLSchema graphQLSchema = UnExecutableSchemaGenerator.makeUnExecutableSchema(typeRegistry);
            SchemaValidator schemaValidation = new SchemaValidator();
            Set<SchemaValidationError> validationErrors = schemaValidation.validateSchema(graphQLSchema);
            isValid = (validationErrors.toArray().length == 0);
        } catch (SchemaProblem e) {
            throw new DefinitionValidationException("Unable to parse the GraphQL definition", e);
        }
        return isValid;
    }
}
