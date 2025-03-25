/*
 *   Copyright (c) {2025}, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.impl.definitions;

import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OpenAPI31To30SchemaProcessor implements SchemaProcessor {
    private static final Log log = LogFactory.getLog(OpenAPI31To30SchemaProcessor.class);

    /**
     * Extract references from OpenAPI 3.1 schema object while converting the schema to 3.0 schema.
     *
     * @param schema  Schema object in OpenAPI 3.1
     * @param context SwaggerUpdateContext holding references.
     */
    @Override
    public void extractReferenceFromSchema(Schema<?> schema, OASParserUtil.SwaggerUpdateContext context) {
        if (schema == null) return;

        String ref = schema.get$ref();
        List<String> references = new ArrayList<>();

        if (ref == null) {
            if (schema.getItems() != null) { // if type: array and items array is not empty.
                OASParserUtil.processArraySchema(schema, context);
            } else if (schema.getAdditionalProperties() != null) { // if schema is a Mapped Schema
                Schema<?> additionalPropertiesSchema = (Schema<?>) schema.getAdditionalProperties();
                extractReferenceFromSchema(additionalPropertiesSchema, context);
            } else if (schema.getAllOf() != null || schema.getAnyOf() != null || schema.getOneOf() != null) {
                processComposedSchemas(schema, references, context);
            } else if (schema.getProperties() != null) { // if schema is an object with properties.
                references = extractReferenceFromNestedSchema(schema, context);
            }
        }
        if (ref != null) {
            OASParserUtil.addToReferenceObjectMap(ref, context);
        } else if (!references.isEmpty()) {
            for (String reference : references) {
                if (reference != null) {
                    OASParserUtil.addToReferenceObjectMap(reference, context);
                }
            }
        }
        // Eg: if schema has additionalProperties along with properties.
        OASParserUtil.processSchemaProperties(schema, context);
    }

    /**
     * Convert schema and process examples, types, and nullable properties from openapi 3.1 to 3.0.
     * Important: not changing the SpecVersion field as it will affect to processing of nested schemas.
     * This method will only cover the conversion of examples and types field version changes. This conversion
     * may result in loss of data in cases where there are multiple examples present in examples field of OpenAPI 3.1.0
     * Schema.
     *
     * @param schema The schema object that needs conversion.
     */
    @Override
    public void convertSchema(Schema<?> schema) {
        if (schema == null) {
            return;
        }
        if (schema.getExamples() != null && !schema.getExamples().isEmpty() && schema.getExample() == null) {
            Object firstExample = schema.getExamples().iterator().next();
            //even if there are multiple examples, those will be ignored and only the first one will be picked.
            schema.setExample(firstExample);
        }

        if (schema.getTypes() != null && schema.getTypes().contains(APIConstants.OPENAPIV31_SCHEMA_TYPE_NULLABLE)) {
            schema.setNullable(true);
            schema.getTypes().remove(APIConstants.OPENAPIV31_SCHEMA_TYPE_NULLABLE);
        }

        String type = schema.getType();
        Set<String> types = schema.getTypes();
        if (type == null && types != null && types.size() == 1) {
            // if there are multiple types other than "null", those will be ignored.
            schema.setType(types.iterator().next());
        }
    }

    /**
     * Extract references in OpenAPI 3.0 format from a nested schema object in OpenAPI 3.1.
     *
     * @param schema  Schema object in OpenAPI 3.0 format
     * @param context SchemaUpdateContext holding the references
     * @return References list
     */
    private static List<String> extractReferenceFromNestedSchema(Schema<?> schema,
                                                                 OASParserUtil.SwaggerUpdateContext context) {
        List<String> references = new ArrayList<>();
        OASParserUtil.convertSchema(schema);
        if (schema.getProperties() != null) {
            for (String propertyName : schema.getProperties().keySet()) {
                Schema<?> propertySchema = (Schema<?>) schema.getProperties().get(propertyName);
                OASParserUtil.convertSchema(propertySchema);
                if (propertySchema.getAllOf() != null || propertySchema.getAnyOf() != null
                        || propertySchema.getOneOf() != null) {
                    processComposedSchemas(propertySchema, references, context);
                } else if (propertySchema.getProperties() != null || propertySchema.getItems() != null) {
                    OASParserUtil.extractReferenceFromSchema(propertySchema, context);
                } else {
                    String schemaRef = propertySchema.get$ref();
                    if (schemaRef != null) {
                        references.add(schemaRef);
                    }
                }
            }
        }
        return references;
    }

    /**
     * Process composed schema object in OpenAPI 3.1 version to 3.0 format. This assumes the composed schema has no
     * a nested objects or arrays.
     *
     * @param schema     Composed schema object
     * @param references References list
     */
    private static void processComposedSchemas(Schema<?> schema, List<String> references,
                                               OASParserUtil.SwaggerUpdateContext updateContext) {
        if (schema.getAllOf() != null) {
            for (Schema<?> sc : schema.getAllOf()) {
                processComposedSchema(sc, updateContext, references);
            }
        }
        if (schema.getAnyOf() != null) {
            for (Schema<?> sc : schema.getAnyOf()) {
                processComposedSchema(sc, updateContext, references);
            }
        }
        if (schema.getOneOf() != null) {
            for (Schema<?> sc : schema.getOneOf()) {
                processComposedSchema(sc, updateContext, references);
            }
        }
        if (schema.getAllOf() == null && schema.getAnyOf() == null && schema.getOneOf() == null) {
            log.error("Unidentified schema. The schema is not available in the API definition.");
        }
    }

    /**
     * Process ComposedSchemas in OpenAPI31 while converting to OpenAPI 3.0 format. References list will be
     * populated while recursively traversing the composed schema object.
     *
     * @param sc         Schema
     * @param context    SwaggerUpdateContext
     * @param references References list
     */
    private static void processComposedSchema(Schema<?> sc, OASParserUtil.SwaggerUpdateContext context,
                                              List<String> references) {
        if (APIConstants.OPENAPI_OBJECT_DATA_TYPE.equalsIgnoreCase(sc.getType())
                || (sc.getTypes() != null && sc.getTypes().contains(APIConstants.OPENAPI_OBJECT_DATA_TYPE))) {
            references.addAll(extractReferenceFromNestedSchema(sc, context));
        } else if (sc.getItems() != null){
            OASParserUtil.processArraySchema(sc, context);
        } else {
            String schemaRef = sc.get$ref();
            if (schemaRef != null) {
                references.add(sc.get$ref());
            } else {
                OASParserUtil.processSchemaProperties(sc, context);
            }
        }
    }
}
