/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.common.gateway.graphql;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.OperationTypeDefinition;
import graphql.language.SchemaDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import org.wso2.carbon.apimgt.common.gateway.constants.GraphQLConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Contains utils for processing GraphQLSchemaDefinition
 */
public class GraphQLSchemaDefinitionUtil {

    /**
     * Extract GraphQL Operations from given schema.
     *
     * @param typeRegistry graphQL Schema Type Registry
     * @param type         operation type string
     * @return the arrayList of APIOperationsDTO
     */
    public static ArrayList<String> getSupportedFields(TypeDefinitionRegistry typeRegistry, String type) {
        ArrayList<String> operationArray = new ArrayList<>();
        Map<String, TypeDefinition> operationList = typeRegistry.types();
        for (Map.Entry<String, TypeDefinition> entry : operationList.entrySet()) {
            Optional<SchemaDefinition> schemaDefinition = typeRegistry.schemaDefinition();
            if (schemaDefinition.isPresent()) {
                List<OperationTypeDefinition> operationTypeList = schemaDefinition.get().getOperationTypeDefinitions();
                for (OperationTypeDefinition operationTypeDefinition : operationTypeList) {
                    boolean canAddOperation = entry.getValue().getName()
                            .equalsIgnoreCase(operationTypeDefinition.getTypeName().getName()) &&
                            (type == null || type.equals(operationTypeDefinition.getName().toUpperCase(Locale.ROOT)));
                    if (canAddOperation) {
                        addOperations(entry, operationArray);
                        break;
                    }
                }
            } else {
                boolean canAddOperation = (entry.getValue().getName().equalsIgnoreCase(GraphQLConstants.GRAPHQL_QUERY)
                        || entry.getValue().getName().equalsIgnoreCase(GraphQLConstants.GRAPHQL_MUTATION)
                        || entry.getValue().getName().equalsIgnoreCase(GraphQLConstants.GRAPHQL_SUBSCRIPTION)) &&
                        (type == null || type.equals(entry.getValue().getName().toUpperCase(Locale.ROOT)));
                if (canAddOperation) {
                    addOperations(entry, operationArray);
                }
            }
        }
        return operationArray;
    }

    /**
     * Check subscription operation availability from given graphql schema.
     *
     * @param schema graphQL Schema
     * @return the boolean value of subscription operation availability
     */
    public static boolean isSubscriptionAvailable(String schema) {
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
        return UnExecutableSchemaGenerator.makeUnExecutableSchema(typeRegistry).getSubscriptionType() != null ?
                true : false;
    }

    /**
     * @param entry Entry
     */
    private static ArrayList<String> addOperations(Map.Entry<String, TypeDefinition> entry,
                                                   ArrayList<String> operationsArray) {
        for (FieldDefinition fieldDef : ((ObjectTypeDefinition) entry.getValue()).getFieldDefinitions()) {
            operationsArray.add(fieldDef.getName());
        }
        return operationsArray;
    }
}

