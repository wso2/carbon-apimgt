/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.analysis.FieldComplexityCalculator;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class GraphQLProcessorUtil {

    private static final Log log = LogFactory.getLog(GraphQLProcessorUtil.class);
    private static final Validator validator = new Validator();


    /**
     * This method used to extract operation List
     *
     * @param operation operation
     * @return operationList
     */
    public static String getOperationList(OperationDefinition operation, TypeDefinitionRegistry typeDefinitionRegistry) {
        String operationList;
        GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
        ArrayList<String> operationArray = new ArrayList<>();

        List<URITemplate> list = graphql.extractGraphQLOperationList(typeDefinitionRegistry,
                operation.getOperation().toString());
        ArrayList<String> supportedFields = getSupportedFields(list);

        getNestedLevelOperations(operation.getSelectionSet().getSelections(), supportedFields, operationArray);
        operationList = String.join(",", operationArray);
        return operationList;
    }

    /**
     * This method support to extracted nested level operations
     *
     * @param selectionList   selection List
     * @param supportedFields supportedFields
     * @param operationArray  operationArray
     */
    private static void getNestedLevelOperations(List<Selection> selectionList, ArrayList<String> supportedFields,
                                                 ArrayList<String> operationArray) {
        for (Selection selection : selectionList) {
            if (!(selection instanceof Field)) {
                continue;
            }
            Field levelField = (Field) selection;
            if (!operationArray.contains(levelField.getName()) &&
                    supportedFields.contains(levelField.getName())) {
                operationArray.add(levelField.getName());
                if (log.isDebugEnabled()) {
                    log.debug("Extracted operation: " + levelField.getName());
                }
            }
            if (levelField.getSelectionSet() != null) {
                getNestedLevelOperations(levelField.getSelectionSet().getSelections(), supportedFields, operationArray);
            }
        }
    }

    /**
     * This method helps to extract only supported operation names
     *
     * @param list URITemplates
     * @return supported Fields
     */
    private static ArrayList<String> getSupportedFields(List<URITemplate> list) {
        ArrayList<String> supportedFields = new ArrayList<>();
        for (URITemplate template : list) {
            supportedFields.add(template.getUriTemplate());
        }
        return supportedFields;
    }

    public static String validatePayload(GraphQLSchema schema, Document document) {

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
