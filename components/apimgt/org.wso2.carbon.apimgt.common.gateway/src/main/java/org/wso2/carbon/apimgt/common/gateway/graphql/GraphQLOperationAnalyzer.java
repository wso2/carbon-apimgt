/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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


import graphql.analysis.FieldComplexityCalculator;
import graphql.analysis.FieldComplexityEnvironment;
import graphql.analysis.QueryTraverser;
import graphql.analysis.QueryVisitorFieldEnvironment;
import graphql.analysis.QueryVisitorStub;
import graphql.execution.CoercedVariables;
import graphql.language.Argument;
import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FieldDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ListType;
import graphql.language.ObjectField;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.language.VariableReference;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * analyze each operation in the payload and return operation depth, complexity, accessed fields,
 * used types and mutated fields
 */
public class GraphQLOperationAnalyzer {
    private static final Log log = LogFactory.getLog(GraphQLOperationAnalyzer.class);
    private static final String OPERATION_DEPTH = "operationDepth";
    private static final String OPERATION_COMPLEXITY = "operationComplexity";
    private static final String OPERATION_NAME = "operationName";
    private static final String RETURN_TYPE = "returnType";
    private static final String ACCESSED_TYPES = "accessedTypes";
    private static final String QUERY = "query";
    private static final String MUTATION = "mutation";
    private static final String SUBSCRIPTION = "subscription";

    /**
     * @param type Operation Type
     * @param selection Selection Set
     * @param subDocument Parsed document
     * @param graphQLSchema GraphQL Schema
     * @param typeDefinitionRegistry GraphQL Type Definition Registry
     * @param complexityInfoJson Complexity Info
     * @param variableMap External Variables Map
     * @return field usages, depth, complexities of the requested query
     */
    public static Map<String, Object> getOperationInfo(String type, Selection selection, Document subDocument,
                                                       GraphQLSchema graphQLSchema,
                                                       TypeDefinitionRegistry typeDefinitionRegistry,
                                                       String complexityInfoJson, Map<String, Object> variableMap) {

        Map<String, Object> operationInfo = new HashMap<>();
        operationInfo.put(OPERATION_DEPTH, calculateDepth(subDocument, graphQLSchema, variableMap));
        operationInfo.put(OPERATION_COMPLEXITY, calculateComplexity(subDocument, graphQLSchema,
                complexityInfoJson, variableMap));

        //add query name to custom properties
        String operationName;
        if (selection instanceof Field) {
            Field field = (Field) selection;
            operationName = field.getName();
            operationInfo.put(OPERATION_NAME, operationName);
        } else {
            throw new IllegalArgumentException("Expected a Field but got a different type of Selection");
        }

        //add return type to custom properties
        GraphQLObjectType objectType = null;
        if (type.equals(QUERY)) {
            objectType = graphQLSchema.getQueryType();
        } else if (type.equals(MUTATION)) {
            objectType = graphQLSchema.getMutationType();
        } else if (type.equals(SUBSCRIPTION)) {
            objectType = graphQLSchema.getSubscriptionType();
        }

        if (objectType != null) {
            String returnType = getOperationReturnType(objectType, operationName);
            operationInfo.put(RETURN_TYPE, returnType);
        }

        // Add all used types in a given query
        List<String> parentList = new ArrayList<>();
        for (Definition subDefinition : subDocument.getDefinitions()) {
            OperationDefinition subOperation =  (OperationDefinition) subDefinition;
            Map<String, Object> typesUsedByEachOperation = getTypeList(subOperation, typeDefinitionRegistry);
            List<Map<String, String>> usedList = (List<Map<String, String>>) typesUsedByEachOperation.get("used");
            for (Map<String, String> entry : usedList) {
                String parent = entry.get("parent");
                if (!parentList.contains(parent)) {
                    parentList.add(parent);
                }
            }
        }
        List<Map<String, String>> accessedTypesList = new ArrayList<>();
        for (String parent : parentList) {
            Map<String, String> typeMap = new HashMap<>();
            typeMap.put("type", parent);
            accessedTypesList.add(typeMap);
        }

        operationInfo.put(ACCESSED_TYPES, accessedTypesList);
        return operationInfo;
    }

    private static int calculateDepth(Document document, GraphQLSchema graphQLSchema,
                                      Map<String, Object> variableMap) {
        int depth = 0;
        try {
            if (variableMap == null) {
                variableMap = new HashMap<>();
            }
            QueryTraverser queryTraverser = QueryTraverser.newQueryTraverser()
                    .schema(graphQLSchema)
                    .document(document)
                    .operationName(null)
                    .coercedVariables(CoercedVariables.of(variableMap))
                    .build();
            depth = queryTraverser.reducePreOrder((env, acc) -> {
                return Math.max(getPathLength(env.getParentEnvironment()), acc);
            }, 0);
        } catch (Exception e) {
            log.error("Error Occurred when collecting data", e);
        }
        return depth;
    }

    private static int getPathLength(QueryVisitorFieldEnvironment path) {
        int length;
        for (length = 1; path != null; ++length) {
            path = path.getParentEnvironment();
        }
        return length;
    }

    private static int calculateComplexity(Document document, GraphQLSchema graphQLSchema, String complexityInfo,
                                           Map<String, Object> variableMap) {
        int totalComplexity;
        FieldComplexityCalculatorImpl fieldComplexityCalculator = new FieldComplexityCalculatorImpl();
        try {
            fieldComplexityCalculator.parseAccessControlPolicy(complexityInfo);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if (variableMap == null) {
            variableMap = new HashMap<>();
        }
        QueryTraverser queryTraverser = QueryTraverser.newQueryTraverser()
                .schema(graphQLSchema)
                .document(document)
                .operationName(null)
                .coercedVariables(CoercedVariables.of(variableMap))
                .build();
        final Map<QueryVisitorFieldEnvironment, Integer> valuesByParent = new LinkedHashMap();
        queryTraverser.visitPostOrder(new QueryVisitorStub() {
            public void visitField(QueryVisitorFieldEnvironment env) {
                int childComplexity = (Integer) valuesByParent.getOrDefault(env, 0);
                int value = calculateComplexity(env, childComplexity, fieldComplexityCalculator);
                valuesByParent.compute(env.getParentEnvironment(), (key, oldValue) -> {
                    return (Integer) Optional.ofNullable(oldValue).orElse(0) + value;
                });
            }
        });
        totalComplexity = (Integer) valuesByParent.getOrDefault((Object) null, 0);
        return totalComplexity;
    }

    private static int calculateComplexity(QueryVisitorFieldEnvironment queryVisitorFieldEnvironment,
                                           int childsComplexity,
                                           FieldComplexityCalculator fieldComplexityCalculator) {
        if (queryVisitorFieldEnvironment.isTypeNameIntrospectionField()) {
            return 0;
        } else {
            FieldComplexityEnvironment fieldComplexityEnvironment = convertEnv(queryVisitorFieldEnvironment);
            return fieldComplexityCalculator.calculate(fieldComplexityEnvironment, childsComplexity);
        }
    }

    private static FieldComplexityEnvironment convertEnv(QueryVisitorFieldEnvironment queryVisitorFieldEnvironment) {
        FieldComplexityEnvironment parentEnv = null;
        if (queryVisitorFieldEnvironment.getParentEnvironment() != null) {
            parentEnv = convertEnv(queryVisitorFieldEnvironment.getParentEnvironment());
        }

        return new FieldComplexityEnvironment(queryVisitorFieldEnvironment.getField(),
                queryVisitorFieldEnvironment.getFieldDefinition(),
                queryVisitorFieldEnvironment.getFieldsContainer(),
                queryVisitorFieldEnvironment.getArguments(),
                parentEnv);
    }

    private static String getOperationReturnType(GraphQLObjectType objectType, String operationName) {
        String returnType = null;
        for (GraphQLFieldDefinition fieldDefinition : objectType.getFieldDefinitions()) {
            if (fieldDefinition.getName().equals(operationName)) {
                GraphQLType fieldType = fieldDefinition.getType();
                if (fieldType instanceof GraphQLInterfaceType || fieldType instanceof GraphQLObjectType) {
                    returnType = ((GraphQLNamedType) fieldDefinition.getType()).getName();
                } else if (fieldType instanceof GraphQLList){
                    GraphQLType wrappedType = ((GraphQLList) fieldType).getOriginalWrappedType();
                    if (wrappedType instanceof GraphQLInterfaceType || wrappedType instanceof GraphQLObjectType){
                        returnType = ((GraphQLNamedType) wrappedType).getName();
                    }
                }
            }
        }
        return returnType;
    }

    private static Map<String, Object> getTypeList(OperationDefinition operation,
                                                  TypeDefinitionRegistry typeDefinitionRegistry) {
        Map<String, Object> definedTypes = new HashMap<>();
        String type = operation.getOperation().toString();
        Map<String, List<String>> result = getFieldUsage(operation);
        definedTypes = getSchemaUsage(result, definedTypes, typeDefinitionRegistry, type);
        return definedTypes;
    }

    private static Map<String, List<String>> getFieldUsage(OperationDefinition operation) {
        Map<String, Integer> fieldUsage = new HashMap<>();
        List<Selection> subDictionary = operation.getSelectionSet().getSelections();

        for (Selection selection : subDictionary) {
            Field levelField = (Field) selection;
            fieldUsage = analyzeFieldUsage(levelField.getName(), selection,
                    null, fieldUsage);
        }

        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : fieldUsage.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("\\.", 2);
            if (parts.length == 2) {
                String parentKey = parts[0];
                String childKey = parts[1];
                result.computeIfAbsent(parentKey, k -> new ArrayList<>()).add(childKey);
            }
        }
        return result;
    }

    //counts parent child usage
    private static Map<String, Integer> analyzeFieldUsage(String currentKey, Selection selection,
                                                String immediateParent, Map<String, Integer> fieldUsage) {
        Field levelField = (Field) selection;
        if (levelField.getSelectionSet() == null) {
            if (immediateParent != null) {
                String key = immediateParent + "." + currentKey;
                fieldUsage.put(key, fieldUsage.getOrDefault(key, 0) + 1);
            }
            return fieldUsage;
        } else {
            Map<String, Integer> tempFieldUsage = fieldUsage;
            List<Selection> subDictionary = levelField.getSelectionSet().getSelections();
            for (Selection child : subDictionary) {
                Field childLevel = (Field) child;
                tempFieldUsage = analyzeFieldUsage(childLevel.getName(), child, levelField.getName(),
                        tempFieldUsage);
            }
        }
        if (immediateParent != null) {
            String key = immediateParent + "." + currentKey;
            fieldUsage.put(key, fieldUsage.getOrDefault(key, 0) + 1);
        }
        return fieldUsage;
    }

    private static Map<String, Object> getSchemaUsage(Map<String, List<String>> fieldUsage, Map<String,
            Object> definedTypes, TypeDefinitionRegistry typeDefinitionRegistry, String type) {

        Map<String, TypeDefinition> operationList = typeDefinitionRegistry.types();
        for (Map.Entry<String, TypeDefinition> entry : operationList.entrySet()) {
            if (type.equals(entry.getValue().getName().toUpperCase(Locale.ROOT))) {
                for (FieldDefinition fieldDef : ((ObjectTypeDefinition) entry.getValue()).getFieldDefinitions()) {
                    String fieldName = fieldDef.getName();
                    if (fieldUsage.containsKey(fieldName)) {
                        String typeObject = getFieldType(fieldDef.getType());
                        List<String> leaves = fieldUsage.get(fieldName);
                        Map<String, Boolean> definedFieldUsage = getDefinedLeafUsage(typeObject, operationList);
                        makeTrue(leaves, definedFieldUsage);
                        definedTypes.put(typeObject, definedFieldUsage);
                    }
                }
            }
        }
        Map<String, Object> tempHashMap = new HashMap<>();
        for (String key : definedTypes.keySet()) {
            List<FieldDefinition> fieldDefinition = getFieldDef(key, operationList);
            for (FieldDefinition fieldDef : fieldDefinition) {
                if (operationList.containsKey(getFieldType(fieldDef.getType())) && !definedTypes
                        .containsKey(getFieldType(fieldDef.getType()))) {
                    String typeObject = getFieldType(fieldDef.getType());
                    if (fieldUsage.containsKey(fieldDef.getName())) {
                        List<String> leaves = fieldUsage.get(fieldDef.getName());
                        Map<String, Boolean> definedFieldUsage = getDefinedLeafUsage(typeObject, operationList);
                        makeTrue(leaves, definedFieldUsage);
                        tempHashMap.put(getFieldType(fieldDef.getType()), definedFieldUsage);
                    }
                }
            }
        }
        definedTypes.putAll(tempHashMap);
        return analyzeUsage(definedTypes);
    }

    private static List<FieldDefinition> getFieldDef(String key, Map<String, TypeDefinition> operationList) {
        List<FieldDefinition> fieldDefinition = new ArrayList<>();
        if (operationList.get(key) instanceof ObjectTypeDefinition) {
            fieldDefinition = ((ObjectTypeDefinition) operationList.get(key)).getFieldDefinitions();
        } else if (operationList.get(key) instanceof InterfaceTypeDefinition) {
            fieldDefinition = ((InterfaceTypeDefinition) operationList.get(key)).getFieldDefinitions();
        }
        return fieldDefinition;
    }

    private static void makeTrue(List<String> leaves, Map<String, Boolean> definedFieldUsage) {
        for (String leaf : leaves) {
            if (definedFieldUsage.containsKey(leaf)) {
                definedFieldUsage.put(leaf, true);
            }
        }
    }

    private static Map<String, Boolean> getDefinedLeafUsage(String typeObject, Map<String,
                                                            TypeDefinition> operationList) {
        Map<String, Boolean> definedFieldUsage = new HashMap<>();
        List<FieldDefinition> fieldList = getFieldDef(typeObject, operationList);
        for (FieldDefinition field : fieldList) {
            definedFieldUsage.put(field.getName(), false);
        }
        return definedFieldUsage;
    }

    private static String getFieldType(Type type) {
        if (type instanceof TypeName) {
            return ((TypeName) type).getName();
        } else if (type instanceof ListType) {
            return ((TypeName) ((ListType) type).getType()).getName();
        }
        return "";
    }

    private static Map<String, Object> analyzeUsage(Map<String, Object> data) {
        Map<String, Object> jsonMap = new HashMap<>();
        List<Map<String, String>> usedList = new ArrayList<>();
        List<Map<String, String>> unusedList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String parent = entry.getKey();
            Map<String, Boolean> children = (Map<String, Boolean>) entry.getValue();

            for (Map.Entry<String, Boolean> childEntry : children.entrySet()) {
                String child = childEntry.getKey();
                boolean value = childEntry.getValue();

                Map<String, String> item = new HashMap<>();
                item.put("parent", parent);
                item.put("child", child);

                if (value) {
                    usedList.add(item);
                } else {
                    unusedList.add(item);
                }
            }
        }

        jsonMap.put("used", usedList);
        jsonMap.put("unused", unusedList);

        return jsonMap;
    }

    /**
     * @param type Operation Type
     * @param selection Selection Set
     * @param graphQLSchema GraphQL Schema
     * @param operationName Name of the mutation
     * @param variableMap External Variable Map
     * @return mutated fields and types
     */
    public static List<Map<String, Object>> getMutatedFields(String type, Selection selection,
                                                             GraphQLSchema graphQLSchema,
                                                             String operationName,
                                                             Map<String, Object> variableMap) {
        List<Map<String, Object>> transformedArray = null;
        if (type.equals("mutation")) {
            Map<String, Object> transformedMap;
            Map<String, Object> mutatedTypes = new HashMap<>();
            if (selection instanceof Field) {
                Field field = (Field) selection;
                mutatedTypes = getMutatedTypesFromOperation(field.getArguments());
            } 
            if (variableMap != null) {
                mutatedTypes = formatVariableMap(mutatedTypes, variableMap);
            }
            Map<String, String> definedTypes = getDefinedTypesFromSchema(graphQLSchema.getMutationType()
                    .getFieldDefinition(operationName).getArguments());
            transformedMap = transformMaps(definedTypes, mutatedTypes);
            transformedArray = convertMapToListOfMaps(transformedMap);
        }
        return transformedArray;
    }

    private static Map<String, Object> formatVariableMap(Map<String, Object> mutatedTypes,
                                                         Map<String, Object> variableMap) {
        Map<String, Object> combinedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : mutatedTypes.entrySet()) {
            String keyFromFirstMap = entry.getKey();
            String valueFromFirstMap = (String) entry.getValue();
            if (variableMap.containsKey(valueFromFirstMap)) {
                Object valueFromSecondMap = variableMap.get(valueFromFirstMap);
                if (valueFromSecondMap instanceof Map) {
                    Map<?, ?> innerMap = (Map<?, ?>) valueFromSecondMap;
                    String[] keysArray = innerMap.keySet().toArray(new String[0]);
                    combinedMap.put(keyFromFirstMap, keysArray);
                } else {
                    combinedMap.put(keyFromFirstMap, valueFromSecondMap);
                }
            }
        }
        return combinedMap;
    }

    private static Map<String, Object> transformMaps(Map<String, String> definedTypes,
                                                     Map<String, Object> mutatedTypes) {
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, String> entry : definedTypes.entrySet()) {
            String newKey = entry.getValue();
            String oldKey = entry.getKey();
            Object newValue = mutatedTypes.get(oldKey);
            resultMap.put(newKey, newValue);
        }
        return resultMap;
    }

    private static List<Map<String, Object>> convertMapToListOfMaps(Map<String, Object> inputMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("type", entry.getKey());
            newMap.put("field", entry.getValue());
            result.add(newMap);
        }
        return result;
    }

    private static Map<String, Object> getMutatedTypesFromOperation(List<Argument> arguments) {
        Map<String, Object> mutatedTypes = new HashMap<>();
        for (Argument argument : arguments) {
            Object value = null;
            if (argument.getValue() instanceof ObjectValue) {
                List<String> fields = new ArrayList<>();
                for (ObjectField field : ((ObjectValue) argument.getValue()).getObjectFields()) {
                    value = field.getName();
                    fields.add((String) value);
                }
                mutatedTypes.put(argument.getName(), fields);
            } else if (argument.getValue() instanceof EnumValue) {
                mutatedTypes.put(argument.getName(), ((EnumValue) argument.getValue()).getName());
            } else if (argument.getValue() instanceof VariableReference) {
                mutatedTypes.put(argument.getName(), ((VariableReference) argument.getValue()).getName());
            }
        }
        return mutatedTypes;
    }

    private static Map<String, String> getDefinedTypesFromSchema(List<GraphQLArgument> arguments) {
        Map<String, String> definedTypes = new HashMap<>();
        for (GraphQLArgument argument : arguments) {
            String type = null;
            GraphQLType argumentType = argument.getType();
            if (argumentType instanceof GraphQLEnumType) {
                type = ((GraphQLEnumType) argumentType).getName();
            } else if (argumentType instanceof GraphQLNonNull) {
                GraphQLType wrappedType = ((GraphQLNonNull) argumentType).getOriginalWrappedType();
                if (wrappedType instanceof GraphQLInputObjectType) {
                    type = ((GraphQLInputObjectType) wrappedType).getName();
                } else if (wrappedType instanceof GraphQLScalarType) {
                    type = ((GraphQLScalarType) wrappedType).getName();
                }
            }
            if (type != null) {
                definedTypes.put(argument.getName(), type);
            }
        }
        return definedTypes;
    }

    /**
     * @param selection Selection Set
     * @return accessed fields in a given query
     */
    public static List<Map> getUsedFields(Selection selection) {
        List<Map> accessedFields = new ArrayList<>();
        if (selection instanceof Field) {
            Field field = (Field) selection;
            if (field.getSelectionSet() != null) {
                for (Selection sel : field.getSelectionSet().getSelections()) {
                    if (sel instanceof Field) {
                        Map<String, String> fieldName = new HashMap<>();
                        fieldName.put("fieldName", ((Field) sel).getName());
                        accessedFields.add(fieldName);
                    }
                }
            }
        }
        return accessedFields;
    }
}
