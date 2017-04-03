/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.exception;

/**
 * This enum class holds error codes that we need to pass to upper level. For example, to the UI.
 * You have to define your custom error codes here.
 */
public enum ExceptionCodes implements ErrorHandler {

    API_ALREADY_EXISTS(900300, "The API already exists.", 409, " The API already exists"),
    APPLICATION_ALREADY_EXISTS(900301, "The application already exists.", 409, " The application already exists"),
    APIMGT_DAO_EXCEPTION(900302, "Internal server error.", 500, " Error occurred while persisting data"),
    APIMGT_LIFECYCLE_EXCEPTION(900303, "Lifecycle exception occurred", 500, " Error occurred while changing " +
            "lifecycle state"),
    TIER_CANNOT_BE_NULL(900304, "The tier cannot be null.", 400, " The tier cannot be null"),
    TIER_NAME_INVALID(900305, "The tier name is invalid.", 400, " The tier name is invalid"),
    SWAGGER_PARSE_EXCEPTION(900306, "Error while parsing swagger json", 400, "Error while parsing swagger json"),
    APPLICATION_NOT_FOUND(900307, "Application not found", 404, "Application not found"),
    PARAMETER_NOT_PROVIDED(900308, "Parameter value missing", 400,
            "Some of the mandatory parameter values were missing"),
    API_NOT_FOUND(900309, "API not found", 404, "API could not be found"),
    SUBSCRIPTION_NOT_FOUND(900310, "Subscription not found", 404, "Couldn't retrieve Subscriptions for API"),
    UPDATE_STATE_CHANGE(900311, "API fields have state changes", 400, "Couldn't Update as API have changes can't be " +
            "done"),
    DOCUMENT_ALREADY_EXISTS(900312, "Document already exists", 409, "Document already exists"),
    COULD_NOT_UPDATE_API(900313, "Error has occurred. Could not update the API", 500, "Error has occurred. Could not "
            + "update the API"),
    AUTH_GENERAL_ERROR(900900, "Authorization Error", 403, " Error in authorization"),
    ACCESS_TOKEN_INACTIVE(900304, "Invalid Credentials", 401, " Access token is inactive."),
    ACCESS_TOKEN_EXPIRED(900903, "Invalid Credentials", 401, " Access token is expired."),
    MISSING_CREDENTIALS(900902, "Missing Credentials", 401, " Please provide an active access token to proceed"),
    INVALID_SCOPE(900910, "Invalid Scope", 403, " You are not authorized to access the resource."),
    INVALID_AUTHORIZATION_HEADER(900911, "Invalid Authorization header", 401,
            " Please provide the Authorization : Bearer <> token to proceed."),
    OAUTH2_APP_CREATION_FAILED(900401, "Keymanagement Error", 500, " Error while creating the consumer application."),
    OAUTH2_APP_ALREADY_EXISTS(900402, "Keymanagement Error", 409, " OAuth2 application already created."),
    OAUTH2_APP_DELETION_FAILED(900403, "Keymanagement Error", 500, " Error while deleting the consumer application."),
    OAUTH2_APP_UPDATE_FAILED(900404, "Keymanagement Error", 500, " Error while updating the consumer application."),
    OAUTH2_APP_RETRIEVAL_FAILED(900405, "Keymanagement Error", 500, " Error while retrieving the consumer application."
    ),
    OAUTH2_APP_MAP_FAILED(900406, "Keymanagement Error", 500, " Error while mapping an existing consumer application."),
    TOKEN_INTROSPECTION_FAILED(900407, "Keymanagement Error", 500, " Error while introspecting the access token."),
    APPLICATION_TOKEN_GENERATION_FAILED(900408, "Keymanagement Error", 500, " Error while generating the application" +
            "access token."),
    ACCESS_TOKEN_REVOKE_FAILED(900409, "Keymanagement Error", 500, " Error while revoking the access token."),
    DOCUMENT_CONTENT_NOT_FOUND(900314, "Document content not found", 404, "Document content not found"),
    DOCUMENT_NOT_FOUND(900315, "Document not found", 404, "Document not found"),
    API_EXPORT_ERROR(900316, "API export Error", 500, "Error while exporting the given APIs"),
    API_IMPORT_ERROR(900317, "API import Error", 500, "Error while importing the given APIs"),
    ENDPOINT_NOT_FOUND(900316, "Endpoint Not Found", 404, "Endpoint Not Found"),
    ENDPOINT_ALREADY_EXISTS(900322, "Endpoint already exists", 409, "Endpoint already exists"),
    ENDPOINT_ADD_FAILED(900323, "Endpoint adding failed", 400, "Endpoint adding failed"),
    API_DEFINITION_MALFORMED(900317, "ApiDefinition not found", 400, "Failed to retrieve API Definition"),
    TEMPLATE_EXCEPTION(900318, "Service configuration Error", 500, " Error generate service config"),
    GATEWAY_EXCEPTION(900319, "Gateway publishing Error", 500, " Error occurred while publishing to Gateway"),

    JSON_PARSE_ERROR(900320, "Json parse error", 500, "JSON parse error"),
    INVALID_CREDENTIALS(900321, "Invalid Credentials", 401, " Invalid username or password"),

    LOCATION_HEADER_INCORRECT(900322, "Error while obtaining URI for Location header", 500,
            "Error occurred while obtaining URI for Location header"),
    LAST_UPDATED_TIME_RETRIEVAL_ERROR(900323, "Error while retrieving last access time for the resource", 500,
            "Error while retrieving last access time for the resource"),
    WORKFLOW_EXCEPTION(900324, "Workflow error", 500,
            "Error occurred while executing workflow task"),
    WORKFLOW_NOT_FOUND(900325, "Workflow error", 500,
            "Error while retrieving worklow task for the reference id"),
    WORKFLOW_COMPLETED(900326, "Workflow error", 500,
            "Workflow is already completed"),
    WORKFLOW_PENDING(900327, "Workflow exception", 409,
            "Pending workflow task exists for the seleted API"),
    ROLES_CANNOT_BE_EMPTY(900328, "Role list cannot be empty", 400, " Role list cannot be empty"),
    ROLES_CANNOT_BE_NULL(900329, "Role list cannot be null", 400, " Role list cannot be null"),
    UNSUPPORTED_ROLE(900330, "Non existing roles cannot be added to an API", 400,
            " Non existing roles cannot be added to an API");


    private final long errorCode;
    private final String errorMessage;
    private final int httpStatusCode;
    private final String errorDescription;

    /**
     *
     * @param errorCode This is unique error code that pass to upper level.
     * @param msg The error message that you need to pass along with the error code.
     * @param httpErrorCode This HTTP status code which should return from REST API layer. If you don't want to pass a
     *                      http status code keep it blank.
     * @param errorDescription The error description.
     */
    ExceptionCodes(long errorCode, String msg, int httpErrorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorMessage = msg;
        this.httpStatusCode = httpErrorCode;
        this.errorDescription = errorDescription;
    }

    @Override
    public long getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    @Override
    public String getErrorDescription() {
        return this.errorDescription;
    }

}
