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

import io.swagger.v3.oas.models.media.Schema;

/**
 * Interface for processing OpenAPI schema objects when constructing API product OpenAPI definition.
 * Implementations of this interface handle the extraction and processing of references from schema definitions
 * in OpenAPI 3.0 and 3.1 specifications while performing the conversion from source Schema OpenAPI version to resulting
 * API Product's OpenAPI version.
 * The primary use case is to parse complex schema structures such as:
 * - {allOf}, {anyOf}, {oneOf} composed schemas
 * - Mapped schemas with additional properties
 * - Object and array schemas
 * - Schema references within/without properties
 */
public interface SchemaProcessor {

    /**
     * Extract references from a given schema object in OpenAPI 3.x version. This method traverses the schema structure
     * and extracts all references ($ref) it contains, storing them in the provided SwaggerUpdateContext. The context
     * maintains state across multiple extraction calls, accumulating all unique references found in the API definition.
     *
     * @param schema  Schema object
     * @param context Swagger update context to hold the references
     */
    void extractReferenceFromSchema(Schema<?> schema, OASParserUtil.SwaggerUpdateContext context);

    /**
     * Convert the given schema from a given OpenAPI version to API product's openAPI version. As of now, the default
     * API Product version in 3.0.1. This method modifies the schema in-place to ensure compatibility with OpenAPI 3.0.1
     * specification. Conversion may include transforming schema properties that are valid in newer versions but not in
     * 3.0.1, such as converting the 'examples' array to a single 'example' value or handling the 'types' array.
     *
     * @param schema Schema object
     */
    void convertSchema(Schema<?> schema);
}