/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.model.OpenAPIRequest;
import org.wso2.carbon.apimgt.gateway.handlers.security.model.OpenAPIResponse;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;

import java.util.ArrayList;

/**
 * This SchemaValidator handler validates the request/response messages against schema defined in the swagger.
 */
public class SchemaValidator extends AbstractHandler {

    private static final String INTERNAL_ERROR_CODE = "500";
    private static final Log logger = LogFactory.getLog(SchemaValidator.class);
    private static final String HTTP_SC_CODE = "400";

    /**
     * Method to generate OpenApiInteractionValidator when the swagger is provided.
     *
     * @param swagger Swagger definition.
     * @return OpenApiInteractionValidator object for the provided swagger.
     */
    private static OpenApiInteractionValidator getOpenAPIValidator(String swagger) {

        OpenAPIParser openAPIParser = new OpenAPIParser();
        ParseOptions options = new ParseOptions();
        options.setResolveFully(true);
        SwaggerParseResult swaggerParseResult =
                openAPIParser.readContents(swagger, new ArrayList<>(), options);
        OpenAPI openAPI = swaggerParseResult.getOpenAPI();
        return OpenApiInteractionValidator
                .createFor(openAPI)
                .withLevelResolver(
                        LevelResolver.create()
                                .withLevel("validation.schema.required", ValidationReport.Level.INFO)
                                .withLevel("validation.response.body.missing", ValidationReport.Level.INFO)
                                .build())
                .build();
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        logger.debug("Validating the API request Body content..");
        String swagger = messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_STRING).toString();
        if (swagger == null) {
            return true;
        }
        OpenApiInteractionValidator validator = getOpenAPIValidator(swagger);
        OpenAPIRequest request = new OpenAPIRequest(messageContext);

        ValidationReport validationReport = validator.validateRequest(request);
        if (validationReport.hasErrors()) {
            StringBuilder finalMessage = new StringBuilder();
            for (ValidationReport.Message message : validationReport.getMessages()) {
                finalMessage.append(message.getMessage()).append(", ");
            }
            String errMessage = "Schema validation failed in the Request: ";
            logger.error(errMessage);
            GatewayUtils.handleThreat(messageContext, HTTP_SC_CODE, errMessage + finalMessage);
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {

        String swagger = messageContext.getProperty("OPEN_API_STRING").toString();
        OpenApiInteractionValidator validator = getOpenAPIValidator(swagger);
        OpenAPIResponse response = new OpenAPIResponse(messageContext);

        ValidationReport validationReport = validator.validateResponse(response.getPath(), response.getMethod(),
                response);
        if (validationReport.hasErrors()) {
            StringBuilder finalMessage = new StringBuilder();
            for (ValidationReport.Message message : validationReport.getMessages()) {
                finalMessage.append(message.getMessage()).append(", ");
            }
            String errMessage = "Schema validation failed in the Response: ";
            logger.error(errMessage);
            GatewayUtils.handleThreat(messageContext, INTERNAL_ERROR_CODE, errMessage + finalMessage);
        }
        return true;
    }
}
