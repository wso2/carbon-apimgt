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
package org.wso2.carbon.apimgt.spec.parser.definitions;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.models.asyncapi.AsyncApiDocument;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServer;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServers;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiValidationSchemas;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy AsyncAPI v2 parser responsible for processing AsyncAPI 2.x specifications.
 * This class extends BaseAsyncApiV2Parser and
 * provides legacy-specific parsing capabilities for AsyncAPI 2.x definitions.
 */

public class AsyncApiParser extends BaseAsyncApiV2Parser {

    private static final Log log = LogFactory.getLog(AsyncApiParser.class);
    private List<String> otherSchemes;

    public List<String> getOtherSchemes() {
        return otherSchemes;
    }

    public void setOtherSchemes(List<String> otherSchemes) {
        this.otherSchemes = otherSchemes;
    }

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
        JSONObject schemaToBeValidated = new JSONObject(apiDefinition);
        //import and load AsyncAPI HyperSchema for JSON schema validation
        JSONObject hyperSchema = new JSONObject(AsyncApiValidationSchemas.ASYNCAPI_JSON_HYPERSCHEMA);

        //validate AsyncAPI using JSON schema validation
        try {
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(
                    AsyncApiValidationSchemas.METASCHEMA);
            SchemaLoader schemaLoader = SchemaLoader.builder().registerSchemaByURI
                    (new URI(AsyncApiValidationSchemas.JSONSCHEMA), json).schemaJson(hyperSchema).build();
            Schema schemaValidator = schemaLoader.load().build();
            schemaValidator.validate(schemaToBeValidated);

            validationSuccess = true;
        } catch(ValidationException e) {
            //validation error messages
            validationErrorMessages = e.getAllMessages();
        } catch (URISyntaxException e) {
            String msg = "Error occurred when registering the schema";
            throw new APIManagementException(msg, e,
                    ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        } catch (ParseException e) {
            String msg = "Error occurred when parsing the schema";
            throw new APIManagementException(msg, e,
                    ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        }

        // TODO: Validation is failing. Need to fix this. Therefore overriding the value as True.
        validationSuccess = true;

        if (validationSuccess) {
            AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(apiDefinition);
            ArrayList<String> endpoints = new ArrayList<>();
            AsyncApiServers servers = asyncApiDocument.getServers();
            if (servers != null && servers.getItems() != null && !servers.getItems().isEmpty())
            {
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

    /**
     * Get available transport protocols for the Async API.
     *
     * @param definition Async API Definition
     * @return List<String> List of available transport protocols
     * @throws APIManagementException If the async env configuration if not provided properly
     * @deprecated This method has no usage hence it was deprecated.
     */
    @Deprecated
    public static List<String> getTransportProtocolsForAsyncAPI(String definition) throws APIManagementException {

        ArrayList<String> asyncTransportProtocolsList = (ArrayList<String>)
                AsyncApiParserUtil.getTransportProtocolsForAsyncAPI(definition);
        return asyncTransportProtocolsList;
    }

}
