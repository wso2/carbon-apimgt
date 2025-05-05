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
import org.wso2.carbon.apimgt.impl.definitions.APISpecParserConstants;

/**
 * Factory class for creating and returning the appropriate {@link SchemaProcessor} based on the OpenAPI specification
 * version of the provided {@link Schema} object. This is needed since there are some breaking changes introduced
 * from OpenAPI 3.0 to 3.1 in the Schema object.
 * This class is used to centralize the logic for determining which schema processor to use. It supports both
 * OpenAPI 3.0 and OpenAPI 3.1 schema processing. The factory ensures that only a single instance of each schema
 * processor is used.
 * Note: This class is not intended to be instantiated directly.
 */
public class SchemaProcessorFactory {

    private static final OpenAPI30SchemaProcessor OPEN_API_30_PROCESSOR = new OpenAPI30SchemaProcessor();
    private static final OpenAPI31To30SchemaProcessor OPEN_API_31_PROCESSOR = new OpenAPI31To30SchemaProcessor();

    private SchemaProcessorFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * This returns the correct schema processor according to the OpenAPI Spec version of the Schema object.
     *
     * @param schema Schema
     * @return SchemaProcessor
     */
    public static SchemaProcessor getProcessor(Schema<?> schema) {
        if (schema == null || schema.getSpecVersion() == null) {
            throw new IllegalArgumentException("Invalid schema or spec version");
        }
        if (APISpecParserConstants.OAS_V31.equalsIgnoreCase(schema.getSpecVersion().name())) {
            return OPEN_API_31_PROCESSOR;
        } else if (APISpecParserConstants.OAS_V30.equalsIgnoreCase(schema.getSpecVersion().name()))  {
            return OPEN_API_30_PROCESSOR;
        } else {
            throw new IllegalArgumentException("Invalid spec version. Only supported 3.0 and 3.1.");
        }
    }
}
