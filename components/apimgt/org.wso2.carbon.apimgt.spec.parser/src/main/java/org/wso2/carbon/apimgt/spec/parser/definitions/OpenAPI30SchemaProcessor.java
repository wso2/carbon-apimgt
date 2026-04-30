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
package org.wso2.carbon.apimgt.spec.parser.definitions;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class OpenAPI30SchemaProcessor implements SchemaProcessor {
    private static final Log log = LogFactory.getLog(OpenAPI30SchemaProcessor.class);

    /**
     * Extract references from OpenAPI 3.0 schema object.
     *
     * @param schema  Schema object in OpenAPI 3.0
     * @param context SwaggerUpdateContext holding references.
     */
    @Override
    public void extractReferenceFromSchema(Schema<?> schema, OASParserUtil.SwaggerUpdateContext context) {
        if (schema == null) return;

        String ref = schema.get$ref();
        List<String> references = new ArrayList<>();

        if (ref == null) {
            if (schema instanceof ArraySchema) {
                OASParserUtil.processArraySchema(schema, context);
            } else if (schema instanceof MapSchema) {
                Object additionalProperties = schema.getAdditionalProperties();
                if (additionalProperties instanceof Schema) {
                    extractReferenceFromSchema((Schema<?>) additionalProperties, context);
                }
                // If additionalProperties is a boolean, no schema reference extraction is needed
            } else if (schema instanceof ComposedSchema) {
                processComposedSchemas((ComposedSchema) schema, references, context);
            } else if (schema instanceof ObjectSchema) {
                references = extractReferenceFromNestedSchema(schema, context);
            }
        }

        if (ref != null) { // if schema has $ref property
            OASParserUtil.addToReferenceObjectMap(ref, context);
        } else if (!references.isEmpty()) { //if schema has any references from nested schemas
            for (String reference : references) {
                if (reference != null) {
                    OASParserUtil.addToReferenceObjectMap(reference, context);
                }
            }
        }
        // Eg: if schema has additionalProperties along with properties
        OASParserUtil.processSchemaProperties(schema, context);
    }

    /**
     * Extract references from a nested schema object in OpenAPI 3.0.
     *
     * @param schema  Schema object in OpenAPI 3.0 format
     * @param context SchemaUpdateContext holding the references
     * @return References list
     */
    private static List<String> extractReferenceFromNestedSchema(Schema<?> schema,
                                                                 OASParserUtil.SwaggerUpdateContext context) {
        List<String> references = new ArrayList<>();
        if (schema.getProperties() != null) {
            for (String propertyName : schema.getProperties().keySet()) {
                Schema<?> propertySchema = (Schema<?>) schema.getProperties().get(propertyName);
                if (propertySchema instanceof ComposedSchema) {
                    processComposedSchemas((ComposedSchema) propertySchema, references, context);
                } else if (propertySchema instanceof ObjectSchema || propertySchema instanceof ArraySchema) {
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
     * Process the composed schema object for allOf, anyOf or oneOf fields and extract references.
     * This assumes the composed schemas has no nested objects or arrays.
     *
     * @param cs         Composed schema
     * @param references References list
     */
    private static void processComposedSchemas(ComposedSchema cs, List<String> references,
                                               OASParserUtil.SwaggerUpdateContext context) {
        if (cs.getAllOf() != null) {
            for (Schema<?> sc : cs.getAllOf()) {
                processComposedSchema(sc, context, references);
            }
        }
        if (cs.getAnyOf() != null) {
            for (Schema<?> sc : cs.getAnyOf()) {
                processComposedSchema(sc, context, references);
            }
        }
        if (cs.getOneOf() != null) {
            for (Schema<?> sc : cs.getOneOf()) {
                processComposedSchema(sc, context, references);
            }
        }
        if (cs.getAllOf() == null && cs.getAnyOf() == null && cs.getOneOf() == null) {
            log.warn("Unidentified schema. The schema is not available in the API definition.");
        }
    }

    /**
     * Process ComposedSchemas in OpenAPI30. References list will be populated while recursively traversing the
     * composed schema object.
     *
     * @param sc         Schema
     * @param context    SwaggerUpdateContext
     * @param references References list
     */
    private static void processComposedSchema(Schema<?> sc, OASParserUtil.SwaggerUpdateContext context,
                                              List<String> references) {
        if (APISpecParserConstants.OPENAPI_OBJECT_DATA_TYPE.equalsIgnoreCase(sc.getType())) {
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

    /**
     * There will be no conversion of schema object as the resulting API product will be in 3.0 same as original schema.
     *
     * @param schema Schema object
     */
    @Override
    public void convertSchema(Schema<?> schema) {
    }
}
