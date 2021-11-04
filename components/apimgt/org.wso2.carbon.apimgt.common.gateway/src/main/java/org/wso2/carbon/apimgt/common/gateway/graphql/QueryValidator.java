/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.common.gateway.graphql;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains methods using for Graphql query payload validation using schema.
 */
public class QueryValidator {

    private static final Log log = LogFactory.getLog(QueryValidator.class);
    private Validator validator;

    public QueryValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * Validate query payload.
     *
     * @param schema   GraphQL Schema
     * @param document GraphQL payload as a Document
     * @return Validation Error Message if any
     */
    public String validatePayload(GraphQLSchema schema, Document document) {

        String validationErrorMessage = null;
        ArrayList<String> validationErrorMessageList = new ArrayList<>();
        List<ValidationError> validationErrors = validator.validateDocument(schema, document);
        if (validationErrors != null && validationErrors.size() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Validation failed for " + document);
            }
            for (ValidationError error : validationErrors) {
                validationErrorMessageList.add(error.getDescription());
            }
            validationErrorMessage = String.join(",", validationErrorMessageList);
        }
        return validationErrorMessage;
    }
}
