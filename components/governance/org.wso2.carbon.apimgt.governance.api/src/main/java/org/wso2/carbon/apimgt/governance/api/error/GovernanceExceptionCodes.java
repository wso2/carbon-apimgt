/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.api.error;

import org.wso2.carbon.apimgt.governance.api.error.ErrorHandler;
import org.wso2.carbon.apimgt.governance.api.error.ErrorItem;

import java.util.Arrays;

public enum GovernanceExceptionCodes implements ErrorHandler {

    // TODO: Clean up unused codes after implementation
    // General exceptions
    INTERNAL_SERVER_ERROR(100000, "Internal server error.", 500, "Internal server error occurred while processing the request.", true),
    ORGANIZATION_NOT_FOUND(900357, "Organization Not Found", 400, "Organization is not found in the request"),

    // Unauthorized
    JWT_ASSERTION_NOT_FOUND(100001, "Unauthorized request", 401, "X-JWT-Assertion not found in the request header."),
    INVALID_JWT_ASSERTION(100002, "Invalid JWT assertion", 401, "Invalid JWT assertion found in the request header."),
    ORGANIZATION_ID_HEADER_NOT_FOUND(100003, "Required header not found", 400, "Organization ID header not found."),

    // Configuration related codes
    ERROR_READING_CONFIG_FILE(100011, "Error reading configuration file.", 500, "Error reading configuration file.", true),

    // Database connection related codes
    DATASOURCE_CONNECTION_CREATION_FAILED(101000, "Internal server error.", 500, "Internal server error occurred while processing the request.", true),
    DATASOURCE_CONNECTION_VALIDATION_FAILED(101001, "Internal server error.", 500, "Internal server error occurred while processing the request.", true),

    // Database query execution related codes
    INVALID_RESULT_FROM_DATABASE(102001, "Internal server error.", 500, "Internal server error occurred while processing the request.", true),

    // Ruleset related codes
    RULESET_ALREADY_EXIST(301001, "Ruleset already exists", 409, "Ruleset with name: '%s' in the organization: '%s' already exists."),
    RULESET_CREATION_FAILED(301002, "Ruleset creation failed", 500, "Ruleset creation failed with name: '%s' in the organization: '%s'."),
    ERROR_WHILE_RETRIEVING_RULESETS(301003, "Retrieving rulesets failed", 500, "Error while retrieving rulesets for the organization: %s"),
    ERROR_WHILE_RETRIEVING_RULESET_BY_ID(301004, "Retrieving ruleset by id failed", 500, "Error while retrieving ruleset for the organization: %s"),
    ERROR_WHILE_UPDATING_RULESET(301005, "Updating ruleset failed", 500, "Error while updating ruleset: %s in the organization: %s"),
    ERROR_WHILE_DELETING_RULESET(301006, "Deleting ruleset failed", 500, "Error while deleting ruleset with ID: %s in the organization: %s"),
    ERROR_WHILE_DELETING_RULES(301007, "Deleting rules failed", 500, "Error while deleting rules for the ruleset with ID: %s", true),
    RULESET_NOT_FOUND(301007, "Ruleset not found", 404, "Ruleset with ID: %s not found in the organization: %s"),
    RULESET_CONTENT_CONVERSION_ERROR(301008, "Ruleset content conversion failed", 500, "Error while converting input stream to a string for the ruleset with id: %s in the organization: %s"),
    ERROR_WHILE_RETRIEVING_RULESET_CONTENT(301009, "Retrieving ruleset content failed", 500, "Error while retrieving the content of the ruleset with id: %s in the organization: %s"),
    ERROR_WHILE_RETRIEVING_RULESET_CONTENT_ASSOCIATED_WITH_POLICIES(301010, "Retrieving ruleset contents associated with policies failed", 500, "Retrieving ruleset contents associated with policies failed for the organization %s", true),
    ERROR_WHILE_INSERTING_RULES(301011, "Rule insertion failed", 500, "Rule insertion failed for the ruleset with id: '%s'"),
    INVALID_RULESET_CONTENT(301012, "Ruleset content is invalid", 400, "Ruleset content is invalid with id: '%s'"),
    INVALID_RULESET_CONTENT_WITH_INFO(301013, "Invalid Ruleset content", 400, "Invalid Ruleset content, message: '%s'"),
    ERROR_WHILE_LOADING_DEFAULT_RULESET_CONTENT(301014, "Error while loading default ruleset content", 500, "Error while loading default ruleset provided by WSO2.", true),
    ERROR_WHILE_RETRIEVING_POLICY_IDS_BY_RULESET_ID(301015, "Error while retrieving policy ids for the ruleset.", 500, "Error while retrieving policy ids for ruleset id: %s", true),

    // Policy related codes
    ERROR_WHILE_CREATING_POLICY(401001, "Policy creation failed.", 500, "Error while creating governance policy in the organization %s", true),
    POLICY_NOT_FOUND(401002, "Policy not found", 404, "Policy with ID: %s not found in the organization: %s"),
    ERROR_WHILE_RETRIEVING_POLICIES(401003, "Retrieving policies failed", 500, "Error while retrieving policies for the organization: %s", true),
    ERROR_WHILE_RETRIEVING_POLICY_BY_ID(401004, "Retrieving policy by id failed", 500, "Error while retrieving policy with id: %s for the organization: %s"),
    ERROR_WHILE_UPDATING_POLICY(401005, "Updating policy failed", 500, "Error while updating policy with id: %s in the organization: %s"),
    ERROR_WHILE_DELETING_POLICY(401006, "Deleting policy failed", 500, "Error while deleting policy with ID: %s in the organization: %s"),
    POLICY_ALREADY_EXISTS(401007, "Policy already exists.", 409, "Policy with name: '%s' in the organization: '%s' already exists."),

    // Endpoint related codes
    ERROR_WHILE_INSERTING_ENDPOINT_DETAILS(501001, "Error while inserting endpoint details.", 500, "Error while inserting details for endpoint: %s in organization: %s", true),
    ERROR_WHILE_CHECKING_ENDPOINT_AVAILABILITY(501002, "Error while checking endpoint availability.", 500, "Error while checking endpoint availability for endpoint: %s in organization: %s", true),
    ERROR_WHILE_DELETING_ENDPOINT(501003, "Error while deleting endpoint.", 500, "Error while deleting endpoint with ID: %s in the organization: %s"),
    ENDPOINT_NOT_FOUND(501004, "Endpoint not found", 404, "Endpoint with ID: %s not found in the organization: %s"),
    ENDPOINT_NOT_FOUND_GENERAL(501004, "Endpoint not found", 404, "Endpoint with ID: %s is not found"),

    // Linting related codes
    ERROR_WHILE_SAVING_LINTING_RESULT(601001, "Error while saving linting result.", 500, "Error while saving linting result for endpoint: %s", true),
    ERROR_WHILE_ADDING_LINTING_RUN(601002, "Error while adding linting run.", 500, "Error while adding linting run for endpoint: %s policy: %s ruleset: %s", true),
    LINTING_RUN_IN_PROGRESS(601003, "There is currently a linting run in progress.", 503, "There is currently a linting run in progress for endpoint: %s policy: %s ruleset: %s"),
    ERROR_WHILE_UPDATING_LINTING_RUN(601004, "Error while updating linting run.", 500, "Error while updating linting run for endpoint: %s policy: %s ruleset: %s", true),
    ERROR_WHILE_CHECKING_PENDING_LINTING_RUNS(601004, "Error while checking pending linting runs.", 500, "Error while checking pending linting runs for endpoint: %s", true),
    MAX_RETRIES_EXCEEDED_FOR_LINTING_RUN_CHECK(601005, "Exceeded maximum retry attempts for checking pending linting runs.", 429, "Exceeded maximum retry attempts for checking pending linting runs for endpoint: %s"),
    ERROR_WHILE_DELETING_LINTING_RESULTS_FOR_ENDPOINT(601006, "Error while deleting linting results.", 500, "Error while deleting linting results for the endpoint: %s", true),

    // APIM related codes
    INVALID_APIM_CREDENTIALS(701001, "Invalid credentials", 500, "Invalid APIM admin credentials", true),
    ERROR_WHILE_GETTING_APIM_PROJECT(701002, "Error while getting APIM project.", 500, "Error while getting APIM project for endpoint: %s in the organization: %s", true),
    API_DEFINITION_NOT_FOUND(701003, "API definition not found.", 404, "API definition not found for endpoint: %s in the organization: %s"),
    API_DETAILS_NOT_FOUND(701004, "API details not found.", 404, "API details not found for endpoint: %s in the organization: %s"),
    ERROR_WHILE_EXTRACTING_SWAGGER_CONTENT(701005, "Error while extracting swagger content.", 500, "Error while extracting swagger content from zip for the endpoint : %s", true),
    ERROR_WHILE_EXTRACTING_API_DETAILS(701006, "Error while extracting api details.", 500, "Error while extracting api details from zip for the endpoint: %s", true),
    ERROR_WHILE_GETTING_API_LIST(701007, "Error while getting API list.", 500, "Error while getting API list in the " +
            "organization: %s", true);

    private final long errorCode;
    private final String errorMessage;
    private final int httpStatusCode;
    private final String errorDescription;
    private boolean stackTrace = false;

    /**
     * @param errorCode        This is unique error code that pass to upper level.
     * @param msg              The error message that you need to pass along with the error code.
     * @param httpErrorCode    This HTTP status code which should return from REST API layer. If you don't want to pass
     *                         a http status code keep it blank.
     * @param errorDescription The error description.
     */
    GovernanceExceptionCodes(long errorCode, String msg, int httpErrorCode, String errorDescription,
                             boolean stackTrace) {
        this.errorCode = errorCode;
        this.errorMessage = msg;
        this.httpStatusCode = httpErrorCode;
        this.errorDescription = errorDescription;
        this.stackTrace = stackTrace;
    }

    /**
     * @param errorCode        This is unique error code that pass to upper level.
     * @param msg              The error message that you need to pass along with the error code.
     * @param httpErrorCode    This HTTP status code which should return from REST API layer. If you don't want to pass
     *                         a http status code keep it blank.
     * @param errorDescription The error description.
     */
    GovernanceExceptionCodes(long errorCode, String msg, int httpErrorCode, String errorDescription) {
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

    public boolean printStackTrace() {

        return stackTrace;
    }

    /**
     * Create an ErrorHandler instance with the provided ExceptionCode filled with some dynamic input values
     *
     * @param errorHandler ErrorHandler or ExceptionCode object
     * @param params       dynamic values to be filled
     * @return ErrorHandler instance with the provided ExceptionCode filled with some dynamic input values
     */
    public static ErrorHandler from(ErrorHandler errorHandler, String... params) {
        String message = errorHandler.getErrorMessage();
        String description = errorHandler.getErrorDescription();

        if (params != null && params.length > 0) {
            int placesToFormatInMessage = message.length() - message.replace("%", "").length();
            int placesToFormatInDescription = description.length() - description.replace("%", "").length();

            String[] part1 = Arrays.copyOfRange(params, 0, placesToFormatInMessage);
            String[] part2 = Arrays.copyOfRange(params, placesToFormatInMessage,
                    placesToFormatInMessage + placesToFormatInDescription);

            if (placesToFormatInMessage > 0) {
                message = String.format(message, part1);
            }
            if (placesToFormatInDescription > 0) {
                description = String.format(description, part2);
            }
        }
        return new ErrorItem(message, description, errorHandler.getErrorCode(), errorHandler.getHttpStatusCode(),
                errorHandler.printStackTrace());
    }
}
