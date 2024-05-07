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
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
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
 *
 */
public class GraphQLOperationAnalyzer {
    private static final Log log = LogFactory.getLog(GraphQLOperationAnalyzer.class);

    /**
     * @param type
     * @param selection
     * @param subDocument
     * @param graphQLSchema
     * @param typeDefinitionRegistry
     * @param complexityInfoJson
     * @param variableMap
     * @return
     */
    //return field usages, depth, complexities of the requested query
    public static Map<String, Object> getOperationInfo(String type, Selection selection, Document subDocument,
                                                       GraphQLSchema graphQLSchema,
                                                       TypeDefinitionRegistry typeDefinitionRegistry,
                                                       String complexityInfoJson, HashMap<String, Object> variableMap) {

        Map<String, Object> operationalInfo = new HashMap<>();
        operationalInfo.put("operationDepth", calculateDepth(subDocument, graphQLSchema, variableMap));
        operationalInfo.put("operationComplexity", calculateComplexity(subDocument, graphQLSchema,
                complexityInfoJson, variableMap));

        //add query name to custom properties
        String operationName;
        if (selection instanceof Field) {
            Field field = (Field) selection;
            operationName = field.getName();
            operationalInfo.put("operationName", operationName);
        } else {
            throw new IllegalArgumentException("Expected a Field but got a different type of Selection");
        }

        //add return type to custom properties
        GraphQLObjectType objectType;
        if (type.equals("query")) {
            objectType = graphQLSchema.getQueryType();
        } else if (type.equals("mutation")) {
            objectType = graphQLSchema.getMutationType();
        } else if (type.equals("subscription")) {
            objectType = graphQLSchema.getSubscriptionType();
        } else {
            objectType = null;
        }
        assert objectType != null;
        String returnType = getOperationReturnType(objectType, operationName);
        operationalInfo.put("returnType", returnType);

        //add all used types in a given query
        List<String> parentList = new ArrayList<>();
        for (Definition subDefinition : subDocument.getDefinitions()) {
            OperationDefinition subOperation = (OperationDefinition) subDefinition;
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

        operationalInfo.put("accessedTypes", accessedTypesList);
        return operationalInfo;
    }

    private static int calculateDepth(Document document, GraphQLSchema graphQLSchema,
                                      HashMap<String, Object> variableMap) {
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
                                           HashMap<String, Object> variableMap) {
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

    /**
     * @param objectType
     * @param operationName
     * @return
     */
    public static String getOperationReturnType(GraphQLObjectType objectType, String operationName) {
        String returnType = null;
        for (GraphQLFieldDefinition fieldDefinition : objectType.getFieldDefinitions()) {
            if (fieldDefinition.getName().equals(operationName)) {
                if (fieldDefinition.getType() instanceof GraphQLInterfaceType) {
                    returnType = ((GraphQLInterfaceType) fieldDefinition.getType()).getName();
                } else if (fieldDefinition.getType() instanceof GraphQLObjectType) {
                    returnType = ((GraphQLObjectType) fieldDefinition.getType()).getName();
                } else if (fieldDefinition.getType() instanceof GraphQLList){
                    returnType = ((GraphQLObjectType)((GraphQLList)fieldDefinition.getType()).getOriginalWrappedType()).getName();
                }
            }
        }
        return returnType;
    }

    /**
     * @param operation
     * @param typeDefinitionRegistry
     * @return
     */
    public static Map<String, Object> getTypeList(OperationDefinition operation,
                                                  TypeDefinitionRegistry typeDefinitionRegistry) {
        Map<String, Object> definedTypes = new HashMap<>();
        String type = operation.getOperation().toString();
        Map<String, Integer> fieldUsage = analyzePayload(operation);
        Map<String, List<String>> result = processFieldUsage(fieldUsage);
        definedTypes = GraphQLOperationAnalyzer.getSchemaUsage(result, definedTypes, typeDefinitionRegistry, type);
        return definedTypes;
    }

    private static Map<String, Integer> analyzePayload(OperationDefinition operation) {
        Map<String, Integer> fieldUsage = new HashMap<>();
        List<Selection> subDictionary = operation.getSelectionSet().getSelections();

        for (Selection selection : subDictionary) {
            Field levelField = (Field) selection;
            fieldUsage = recFind(levelField.getName(), selection,
                    null, fieldUsage);
        }
        return fieldUsage;
    }

    //counts parent child usage
    private static Map<String, Integer> recFind(String currentKey, Selection selection,
                                                String immediateParent, Map<String, Integer> fieldUsage) {
        Field levelField = (Field) selection;
        if (levelField.getSelectionSet() == null) {
            if (immediateParent != null) {
                String key = immediateParent + "." + currentKey;
                fieldUsage.put(key, fieldUsage.getOrDefault(key, 0) + 1);
            }
            return fieldUsage;
        } else {
            Map<String, Integer> tmpFieldUsage = fieldUsage;
            List<Selection> subDictionary = levelField.getSelectionSet().getSelections();
            for (Selection child : subDictionary) {
                Field childLevel = (Field) child;
                tmpFieldUsage = recFind(childLevel.getName(), child, levelField.getName(),
                        tmpFieldUsage);
            }
        }
        if (immediateParent != null) {
            String key = immediateParent + "." + currentKey;
            fieldUsage.put(key, fieldUsage.getOrDefault(key, 0) + 1);
        }
        return fieldUsage;
    }

    //convert parent.child format into a hashmap
    public static Map<String, List<String>> processFieldUsage(Map<String, Integer> fieldUsage) {
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : fieldUsage.entrySet()) {
            String key = entry.getKey();
            int dotIndex = key.indexOf('.');
            if (dotIndex != -1) {
                String parentKey = key.substring(0, dotIndex);
                String childKey = key.substring(dotIndex + 1);
                result.computeIfAbsent(parentKey, k -> new ArrayList<>()).add(childKey);
            }
        }
        return result;
    }

    private static Map<String, Object> getSchemaUsage(Map<String, List<String>> fieldUsage, Map<String,
            Object> definedTypes, TypeDefinitionRegistry typeDefinitionRegistry, String type) {

        Map<String, TypeDefinition> operationList = typeDefinitionRegistry.types();
        for (Map.Entry<String, TypeDefinition> entry : operationList.entrySet()) {
            if (type.equals(entry.getValue().getName().toUpperCase(Locale.ROOT))) {
                for (FieldDefinition fieldDef : ((ObjectTypeDefinition) entry.getValue()).getFieldDefinitions()) {
                    String fieldName = fieldDef.getName();
                    if (isFieldSupported(fieldName, fieldUsage)) {
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

    private static boolean isFieldSupported(String fieldName, Map<String, List<String>> fieldUsage) {
        return fieldUsage.containsKey(fieldName);
    }

    /**
     * @param data
     * @return
     */
    public static Map<String, Object> analyzeUsage(Map<String, Object> data) {
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
     * @param type
     * @param selection
     * @param graphQLSchema
     * @param operationName
     * @param variableMap
     * @return
     */
    public static List<Map<String, Object>> getMutatedFields(String type, Selection selection,
                                                             GraphQLSchema graphQLSchema,
                                                             String operationName,
                                                             HashMap<String, Object> variableMap) {
        List<Map<String, Object>> transformedArray = null;
        if (type.equals("mutation")) {
            Map<String, Object> transformedMap;
            Map<String, Object> mutatedTypes;
            if (selection instanceof Field) {
                Field field = (Field) selection;
                mutatedTypes = getMutatedTypesFromOperation(field.getArguments());
            } else {
                throw new IllegalArgumentException("Expected a Field but got a different type");
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

    /**
     * @param arguments
     * @return
     */
    public static Map<String, String> getDefinedTypesFromSchema(List<GraphQLArgument> arguments) {
        Map<String, String> definedTypes = new HashMap<>();
        for (GraphQLArgument argument : arguments) {
            String type = null;
            if (argument.getType() instanceof GraphQLEnumType) {
                type = ((GraphQLEnumType) argument.getType()).getName();
            } else if (((GraphQLNonNull) argument.getType()).getOriginalWrappedType() instanceof
                    GraphQLInputObjectType) {
                type = ((GraphQLInputObjectType) ((GraphQLNonNull) argument.getType()).getOriginalWrappedType())
                        .getName();
            } else if (((GraphQLNonNull) argument.getType()).getOriginalWrappedType() instanceof GraphQLScalarType) {
                type = ((GraphQLScalarType) ((GraphQLNonNull) argument.getType()).getOriginalWrappedType()).getName();
            }
            if (type != null) {
                definedTypes.put(argument.getName(), type);
            }
        }
        return definedTypes;
    }

    /**
     * @param selection
     * @return
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
