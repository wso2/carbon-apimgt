/*
 *   Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
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
package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.models.asyncapi.AsyncApiDocument;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServer;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServers;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.BaseAsyncApiV2Parser;

import java.util.ArrayList;
import java.util.List;

/**
 * New AsyncAPI v2 parser used to process AsyncAPI 2.x specifications.
 * This class extends BaseAsyncApiV2Parser and
 * provides version-specific  parsing capabilities for AsyncAPI 2.x definitions.
 */
public class AsyncApiV2Parser extends BaseAsyncApiV2Parser {

    private static final Log log = LogFactory.getLog(AsyncApiV2Parser.class);

    /**
     * Validates the given AsyncAPI definition against the AsyncAPI JSON HyperSchema.
     *
     * @param apiDefinition      the AsyncAPI definition to be validated, in JSON format
     * @param returnJsonContent  whether the validated content should be returned in JSON format
     * @return an APIDefinitionValidationResponse containing validation results and messages
     * @throws APIManagementException if an error occurs during validation or schema processing
     */
    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent)
            throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        String protocol = StringUtils.EMPTY;
        boolean validationSuccess = false;
        List<String> validationErrorMessages = new ArrayList<>();

        try {
            validationSuccess = AsyncApiParserUtil.validateAsyncApiContent(apiDefinition, validationErrorMessages);
        } catch (Exception e) {
            // unexpected problems during validation/parsing
            String msg = "Error occurred while validating AsyncAPI definition: " + e.getMessage();
            throw new APIManagementException(msg, e, ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        }

        if (validationSuccess) {
            AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(apiDefinition);
            ArrayList<String> endpoints = new ArrayList<>();
            AsyncApiServers servers = asyncApiDocument.getServers();
            if (servers != null && servers.getItems() != null && !servers.getItems().isEmpty()) {
                protocol = ((AsyncApiServer) asyncApiDocument.getServers().getItems().get(0)).getProtocol();
            }

            AsyncApiParserUtil.updateValidationResponseAsSuccess(
                    validationResponse,
                    apiDefinition,
                    asyncApiDocument.getAsyncapi(),
                    asyncApiDocument.getInfo().getTitle(),
                    asyncApiDocument.getInfo().getVersion(),
                    null,
                    asyncApiDocument.getInfo().getDescription(),
                    null
            );

            validationResponse.setParser(this);
            if (returnJsonContent) {
                validationResponse.setJsonContent(apiDefinition);
            }
            if (StringUtils.isNotEmpty(protocol)) {
                validationResponse.setProtocol(protocol);
            }
        } else {
            if (validationErrorMessages != null){
                validationResponse.setValid(false);
                for (String errorMessage: validationErrorMessages){
                    AsyncApiParserUtil.addErrorToValidationResponse(validationResponse, errorMessage);
                }
            }
        }
        return validationResponse;
    }
}
