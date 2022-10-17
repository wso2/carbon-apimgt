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
package org.wso2.carbon.apimgt.common.gateway.graphql;

import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class to handle graphQL specific API request processing.
 */
public class GraphQLProcessorUtil {

    private static final Log log = LogFactory.getLog(GraphQLProcessorUtil.class);

    /**
     * This method used to extract operation List.
     *
     * @param operation              operation
     * @param typeDefinitionRegistry TypeDefinitionRegistry
     * @return operationList
     */
    public static ArrayList<String> getOperationList(OperationDefinition operation,
                                                     TypeDefinitionRegistry typeDefinitionRegistry) {
        ArrayList<String> operationArray = new ArrayList<>();
        ArrayList<String> supportedFields = GraphQLSchemaDefinitionUtil.getSupportedFields(typeDefinitionRegistry,
                operation.getOperation().toString());
        getNestedLevelOperations(operation.getSelectionSet().getSelections(), supportedFields, operationArray);
        return operationArray;
    }

    /**
     * This method used to extract operation List as a comma seperated string.
     *
     * @param operation              operation
     * @param typeDefinitionRegistry TypeDefinitionRegistry
     * @return operationList
     */
    public static String getOperationListAsString(OperationDefinition operation,
                                                  TypeDefinitionRegistry typeDefinitionRegistry) {
        ArrayList<String> operationArray = getOperationList(operation, typeDefinitionRegistry);
        return String.join(",", operationArray);
    }

    /**
     * This method support to extracted nested level operations.
     *
     * @param selectionList   selection List
     * @param supportedFields supportedFields
     * @param operationArray  operationArray
     */
    public static void getNestedLevelOperations(List<Selection> selectionList, ArrayList<String> supportedFields,
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
}
