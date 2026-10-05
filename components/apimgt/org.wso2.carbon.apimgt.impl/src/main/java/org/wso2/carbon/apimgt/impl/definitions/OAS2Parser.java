/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Yaml;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * @deprecated use org.wso2.carbon.apimgt.spec.parser.definitions.OAS2Parser instead
 */
@Deprecated
public class OAS2Parser extends org.wso2.carbon.apimgt.spec.parser.definitions.OAS2Parser {

    /**
     * Remove x-wso2-examples from all the paths from the swagger.
     *
     * @param swaggerString Swagger as String
     */
    @Deprecated
    public String removeExamplesFromSwagger(String swaggerString) throws APIManagementException {
        try {
            SwaggerParser swaggerParser = new SwaggerParser();
            Swagger swagger = swaggerParser.parse(swaggerString);
            swagger.getPaths().values().forEach(path -> {
                path.getOperations().forEach(operation -> {
                    if (operation.getVendorExtensions().keySet().contains(APIConstants.SWAGGER_X_EXAMPLES)) {
                        operation.getVendorExtensions().remove(APIConstants.SWAGGER_X_EXAMPLES);
                    }
                });
            });
            return Yaml.pretty().writeValueAsString(swagger);
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error while removing examples from OpenAPI definition", e,
                    ExceptionCodes.ERROR_REMOVING_EXAMPLES);
        }
    }
}
