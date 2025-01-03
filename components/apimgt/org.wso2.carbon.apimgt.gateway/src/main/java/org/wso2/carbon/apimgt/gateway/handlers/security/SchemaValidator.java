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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.model.OpenAPIRequest;
import org.wso2.carbon.apimgt.gateway.handlers.security.model.OpenAPIResponse;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;

/**
 * This SchemaValidator handler validates the request/response messages against schema defined in the swagger.
 */
public class SchemaValidator extends AbstractHandler {

    private static final String INTERNAL_ERROR_CODE = "500";
    private static final Log logger = LogFactory.getLog(SchemaValidator.class);
    private static final String HTTP_SC_CODE = "400";
    public static final String REG_TIME_MODULE = "register.timeModule";

    /**
     * Method to generate OpenApiInteractionValidator when the openAPI is provided.
     *
     * @param openAPI openAPI
     * @return OpenApiInteractionValidator object for the provided swagger.
     */
    private static OpenApiInteractionValidator getOpenAPIValidator(OpenAPI openAPI) {

        return OpenApiInteractionValidator
                .createFor(openAPI)
                .withLevelResolver(
                        LevelResolver.create()
                                .withLevel("validation.schema.required", ValidationReport.Level.INFO)
                                .withLevel("validation.response.body.missing", ValidationReport.Level.INFO)
                                .withLevel("validation.schema.additionalProperties", ValidationReport.Level.IGNORE)
                                .build())
                .build();
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        boolean timeModuleRegisterEnabled = Boolean.parseBoolean(System.getProperty(REG_TIME_MODULE, "false"));
        if (timeModuleRegisterEnabled) {
            Json.mapper().registerModule(new JavaTimeModule());
        }
        logger.debug("Validating the API request Body content..");
        OpenAPI openAPI = (OpenAPI) messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT);
        if (openAPI != null) {
            OpenApiInteractionValidator validator = getOpenAPIValidator(openAPI);
            OpenAPIRequest request = new OpenAPIRequest(messageContext);

            ValidationReport validationReport = validator.validateRequest(request);
            messageContext.setProperty(APIMgtGatewayConstants.SCHEMA_VALIDATION_REPORT, validationReport);
            if (validationReport.hasErrors()) {
                StringBuilder finalMessage = new StringBuilder();
                for (ValidationReport.Message message : validationReport.getMessages()) {
                    finalMessage.append(message.getMessage()).append(", ");
                }
                String errMessage = "Schema validation failed in the Request: ";
                logger.error(errMessage);
                GatewayUtils.handleThreat(messageContext, HTTP_SC_CODE, errMessage + finalMessage);
            }
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {

        OpenAPI openAPI = (OpenAPI) messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT);
        if (openAPI != null) {
            OpenApiInteractionValidator validator = getOpenAPIValidator(openAPI);
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
        }
        return true;
    }
}
