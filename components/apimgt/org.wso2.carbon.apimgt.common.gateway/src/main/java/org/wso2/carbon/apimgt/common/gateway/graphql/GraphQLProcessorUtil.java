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
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return getOperationList(operation, typeDefinitionRegistry, null);
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
        return getOperationListAsString(operation, typeDefinitionRegistry, null);
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
        getNestedLevelOperations(selectionList, supportedFields, operationArray, Collections.emptyMap(),
                new HashSet<>());
    }

    /**
     * This method used to extract operation List, expanding named and inline fragments so that every reachable
     * root field is included in the authorization check.
     *
     * @param operation              operation
     * @param typeDefinitionRegistry TypeDefinitionRegistry
     * @param fragmentDefinitions    map of fragment name to FragmentDefinition from the parsed document
     * @return operationList
     */
    public static ArrayList<String> getOperationList(OperationDefinition operation,
                                                     TypeDefinitionRegistry typeDefinitionRegistry,
                                                     Map<String, FragmentDefinition> fragmentDefinitions) {
        ArrayList<String> operationArray = new ArrayList<>();
        ArrayList<String> supportedFields = GraphQLSchemaDefinitionUtil.getSupportedFields(typeDefinitionRegistry,
                operation.getOperation().toString());
        Map<String, FragmentDefinition> fragments =
                (fragmentDefinitions != null) ? fragmentDefinitions : Collections.emptyMap();
        getNestedLevelOperations(operation.getSelectionSet().getSelections(), supportedFields, operationArray,
                fragments, new HashSet<>());
        return operationArray;
    }

    /**
     * This method used to extract operation List as a comma separated string, expanding named and inline fragments
     * so that every reachable root field is included in the authorization check.
     *
     * @param operation              operation
     * @param typeDefinitionRegistry TypeDefinitionRegistry
     * @param fragmentDefinitions    map of fragment name to FragmentDefinition from the parsed document
     * @return operationList
     */
    public static String getOperationListAsString(OperationDefinition operation,
                                                  TypeDefinitionRegistry typeDefinitionRegistry,
                                                  Map<String, FragmentDefinition> fragmentDefinitions) {
        ArrayList<String> operationArray = getOperationList(operation, typeDefinitionRegistry, fragmentDefinitions);
        return String.join(",", operationArray);
    }

    /**
     * This method supports extracting nested level operations, expanding fragment spreads and inline fragments
     * recursively so that all reachable root fields are included in the authorization check.
     * visitedFragments guards against cycles in recursive fragment references.
     *
     * @param selectionList       selection List
     * @param supportedFields     supportedFields
     * @param operationArray      operationArray
     * @param fragmentDefinitions map of fragment name to FragmentDefinition
     * @param visitedFragments    set of already-visited fragment names (cycle guard)
     */
    public static void getNestedLevelOperations(List<Selection> selectionList, ArrayList<String> supportedFields,
                                                ArrayList<String> operationArray,
                                                Map<String, FragmentDefinition> fragmentDefinitions,
                                                Set<String> visitedFragments) {
        for (Selection selection : selectionList) {
            if (selection instanceof Field) {
                Field levelField = (Field) selection;
                if (!operationArray.contains(levelField.getName()) &&
                        supportedFields.contains(levelField.getName())) {
                    operationArray.add(levelField.getName());
                    if (log.isDebugEnabled()) {
                        log.debug("Extracted operation: " + levelField.getName());
                    }
                }
                if (levelField.getSelectionSet() != null) {
                    getNestedLevelOperations(levelField.getSelectionSet().getSelections(), supportedFields,
                            operationArray, fragmentDefinitions, visitedFragments);
                }
            } else if (selection instanceof FragmentSpread) {
                String fragmentName = ((FragmentSpread) selection).getName();
                // visitedFragments.add returns false if already present — prevents infinite recursion on cycles
                if (visitedFragments.add(fragmentName)) {
                    FragmentDefinition fragment = fragmentDefinitions.get(fragmentName);
                    if (fragment != null) {
                        getNestedLevelOperations(fragment.getSelectionSet().getSelections(), supportedFields,
                                operationArray, fragmentDefinitions, visitedFragments);
                    } else {
                        log.warn("Fragment definition not found for spread: " + fragmentName + ". Failing closed.");
                    }
                }
            } else if (selection instanceof InlineFragment) {
                InlineFragment inlineFragment = (InlineFragment) selection;
                if (inlineFragment.getSelectionSet() != null) {
                    getNestedLevelOperations(inlineFragment.getSelectionSet().getSelections(), supportedFields,
                            operationArray, fragmentDefinitions, visitedFragments);
                }
            }
        }
    }
}
