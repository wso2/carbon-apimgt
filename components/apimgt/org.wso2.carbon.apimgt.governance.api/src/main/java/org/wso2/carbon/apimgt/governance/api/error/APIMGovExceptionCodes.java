/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

/**
 * This enum class contains the error codes and error messages for the governance component.
 */
public enum APIMGovExceptionCodes implements ErrorHandler {

    // General exceptions
    INTERNAL_SERVER_ERROR(990001, "Internal server error.",
            500, "Internal server error occurred while processing the request.", true),
    ORGANIZATION_NOT_FOUND(990002, "Organization Not Found",
            400, "Organization is not found in the request"),
    DATASOURCE_INACCESSIBLE(990003, "Error while accessing the datasource.",
            500, "Error while accessing the datasource with name: %s", true),
    BAD_REQUEST(990004, "Bad request.", 400, "Bad request: %s"),

    // Ruleset related codes
    RULESET_ALREADY_EXIST(990100, "Ruleset already exists",
            409, "Ruleset with name: '%s' in the organization: '%s' already exists."),
    ERROR_RULESET_ASSOCIATED_WITH_POLICIES(990101, "Ruleset is currently " +
            "in use by some governance policies", 409,
            "The ruleset with ID: %s cannot be deleted because it " +
                    "is associated with some governance policies. " +
                    "Please update the associated governance policies before " +
                    "attempting to delete the ruleset."),

    RULESET_NOT_FOUND(990102, "Ruleset not found",
            404, "Ruleset with ID: %s not found"),
    RULESET_CREATION_FAILED(990103, "Ruleset creation failed",
            500, "Ruleset creation failed with name: '%s' in the " +
            "organization: '%s'.", true),

    ERROR_WHILE_GETTING_RULESETS(990104, "Retrieving rulesets failed",
            500, "Error while retrieving rulesets for " +
            "the organization: %s", true),
    ERROR_WHILE_GETTING_RULESET_BY_ID(990105, "Retrieving ruleset by id failed",
            500, "Error while retrieving ruleset with the provided ID", true),
    ERROR_WHILE_GETTING_RULESET_BY_NAME(990106, "Retrieving ruleset by name failed",
            500, "Error while retrieving " +
            "ruleset for the organization: %s", true),
    ERROR_WHILE_SEARCHING_RULESETS(990107, "Error while searching rulesets",
            500, "Error while searching rulesets for the organization: %s", true),

    ERROR_WHILE_UPDATING_RULESET(990108, "Updating ruleset failed",
            500, "Error while updating ruleset: %s", true),
    ERROR_WHILE_DELETING_RULESET(990109, "Deleting ruleset failed",
            500, "Error while deleting ruleset with ID: %s ", true),

    ERROR_WHILE_GETTING_RULESETS_ASSOCIATED_WITH_POLICY(990110,
            "Error while retrieving rulesets associated with " +
                    "policy", 500, "Error while retrieving " +
            "rulesets associated with policy with id: %s", true),
    ERROR_WHILE_GETTING_ASSOCIATED_POLICIES(990111, "Error while retrieving " +
            "associated policies for the ruleset.", 500,
            "Error while retrieving associated policies for the ruleset with ID: %s", true),

    ERROR_WHILE_GETTING_RULES_BY_RULESET_ID(990112, "Error while retrieving rules by ruleset id",
            500, "Error while " +
            "retrieving rules by ruleset id: %s", true),
    ERROR_WHILE_INSERTING_RULES(990113, "Rule insertion failed",
            500, "Rule insertion failed for the ruleset with id:" +
            " '%s'", true),

    ERROR_WHILE_GETTING_RULESET_CONTENT(990114, "Retrieving ruleset content failed",
            500, "Error while retrieving " +
            "the content of the ruleset with id: %s", true),
    ERROR_WHILE_EXTRACTING_RULE_CONTENT(990115, "Error while extracting rule content",
            500, "Error while extracting " +
            "rule content from the ruleset", true),
    ERROR_WHILE_LOADING_DEFAULT_RULESET_CONTENT(990116, "Error while loading default ruleset content",
            500, "Error while loading " +
            "default ruleset provided by WSO2.", true),

    ERROR_FAILED_TO_PARSE_RULESET_CONTENT(990117, "Failed to parse ruleset content",
            500, "Failed to parse ruleset " +
            "content for the ruleset.", true),
    ERROR_WHILE_COVERTING_RULESET_CONTENT_STREAM_TO_BYTE_ARRAY(990118, "Error while converting " +
            "ruleset content stream to byte array",
            500, "Error while converting ruleset content stream to byte array",
            true),

    INVALID_RULESET_CONTENT(990119, "Ruleset content is invalid",
            400, "Content of ruleset '%s' is invalid"),
    INVALID_RULESET_CONTENT_DETAILED(990120, "Invalid ruleset content",
            400, "Content of ruleset `%s` is invalid:\n %s"),

    // Policy related codes
    POLICY_ALREADY_EXISTS(990200, "Policy already exists.",
            409, "Policy with name: '%s' in the organization: '%s' already exists."),

    ERROR_WHILE_CREATING_POLICY(990201, "Policy creation failed.",
            500, "Error while creating governance policy in the organization %s", true),

    POLICY_NOT_FOUND(990202, "Policy not found",
            404, "Policy with ID: %s not found"),

    ERROR_WHILE_GETTING_POLICIES(990203, "Retrieving policies failed",
            500, "Error while retrieving policies for the organization: %s", true),

    ERROR_WHILE_GETTING_POLICY_BY_ID(990204, "Retrieving policy by id failed",
            500, "Error while retrieving policy with id: %s ", true),

    ERROR_WHILE_GETTING_POLICY_BY_NAME(990205, "Retrieving policy by name failed",
            500, "Error while retrieving policy with name: %s for the organization: %s", true),

    ERROR_WHILE_UPDATING_POLICY(990206, "Updating policy failed",
            500, "Error while updating policy with id: %s ", true),

    ERROR_WHILE_DELETING_POLICY(990207, "Deleting policy failed",
            500, "Error while deleting policy with ID: %s", true),

    ERROR_WHILE_GETTING_ACTIONS_BY_POLICY_ID(990208, "Retrieving actions by policy id failed",
            500, "Error while retrieving actions by policy id: %s", true),

    ERROR_WHILE_ASSIGNING_ACTION_TO_POLICY(990209, "Error while assigning action to policy",
            400, "Invalid action provided for the policy: %s"),

    ERROR_WHILE_SEARCHING_POLICIES(990210, "Error while searching policies",
            500, "Error while searching policies for the organization: %s", true),

    ERROR_WHILE_DELETING_LABEL_POLICY_MAPPINGS(990211, "Error while deleting label policy mappings",
            500, "Error while deleting label policy mappings for label with ID: %s", true),


    // Request related codes
    ERROR_WHILE_ADDING_NEW_GOV_EVAL_REQUEST(990300, "Error while adding new governance evaluation request.",
            500, "Error while adding new governance evaluation request for artifact: %s", true),

    ERROR_WHILE_GETTING_EVAL_PENDING_POLICIES_FOR_ARTIFACT(990301,
            "Error while retrieving  evaluation pending policies.",
            500, "Error while retrieving  evaluation pending policies for artifact: %s", true),

    ERROR_WHILE_GETTING_GOV_EVAL_REQUESTS(990302, "Error while retrieving  governance evaluation requests.",
            500, "Error while retrieving  governance evaluation requests"),

    ERROR_WHILE_UPDATING_GOV_EVAL_REQUEST(990303, "Error while updating governance evaluation request.",
            500, "Error while updating governance evaluation request with ID: %s", true),

    ERROR_WHILE_DELETING_GOVERNANCE_EVAL_REQUEST(990304, "Error while deleting governance evaluation request.",
            500, "Error while deleting governance evaluation request with ID: %s"),

    ERROR_WHILE_DELETING_GOVERNANCE_EVAL_REQUESTS(990305, "Error while deleting governance evaluation requests.",
            500, "Error while deleting governance evaluation requests."),

    ERROR_WHILE_CHANGING_PROCESSING_REQ_TO_PENDING(990306, "Error while changing processing requests to pending.",
            500, "Error while changing processing requests to pending", true),

    ERROR_WHILE_GETTING_COMPLIANCE_PENDING_ARTIFACTS(990307, "Error while retrieving  compliance pending artifacts.",
            500, "Error while retrieving  compliance pending artifacts", true),


    // Result related codes
    ERROR_WHILE_SAVING_GOVERNANCE_RESULT(990400, "Error while saving governance result.",
            500, "Error while saving governance result for artifact: %s", true),

    ERROR_WHILE_DELETING_GOVERNANCE_RESULT(990401, "Error while deleting governance result.",
            500, "Error while deleting governance result with ID: %s", true),

    ERROR_WHILE_SAVING_RULE_VIOLATIONS(990402, "Error while saving rule violations.",
            500, "Error while saving rule violations", true),

    ERROR_WHILE_CLEARING_RULE_VIOLATIONS(990403, "Error while clearing rule violations.",
            500, "Error while clearing rule violations", true),

    ERROR_WHILE_GETTING_RULE_VIOLATIONS(990404, "Error while retrieving  rule violations.",
            500, "Error while retrieving  rule violations", true),

    ERROR_WHILE_GETTING_GOVERNANCE_RESULTS(990405, "Error while retrieving  governance results.",
            500, "Error while retrieving  governance results", true),

    ERROR_WHILE_DELETING_GOVERNANCE_RESULTS(990406, "Error while deleting governance results.",
            500, "Error while deleting governance results", true),

    ERROR_WHILE_DELETING_RULE_VIOLATIONS_BY_RESULT_ID(990407, "Error while deleting rule violations by result ID.",
            500, "Error while deleting rule violations by result ID: %s", true),

    ERROR_WHILE_DELETING_EVALUATION_RESULTS_FOR_RULESET(990408, "Error while deleting evaluation results for ruleset.",
            500, "Error while deleting evaluation results for ruleset with ID: %s", true),

    ERROR_WHILE_DELETING_COMPLIANCE_EVALUATION_RESULTS_FOR_POLICY(990409, "Error while deleting " +
            "compliance evaluation results for policy.",
            500, "Error while deleting compliance evaluation results for policy with ID: %s", true),

    ERROR_WHILE_DELETING_RULE_VIOLATIONS_FOR_POLICY(990410, "Error while deleting rule violations for policy.",
            500, "Error while deleting rule violations for policy with ID: %s", true),

    ERROR_WHILE_READING_SPECTRAL_RESULTS(990411, "Error while reading spectral results.",
            500, "Error while reading spectral validation results from the zip", true),

    // Artifact related codes
    ARTIFACT_NOT_FOUND(990500, "Artifact not found.",
            404, "Artifact with id: %s not found in the organization: %s"),

    ERROR_WHILE_DELETING_GOVERNANCE_DATA(990501, "Error while deleting governance data.",
            500, "Error while deleting governance data for artifact: %s", true),

    INVALID_ARTIFACT_TYPE(990502, "Invalid artifact type.",
            400, "Invalid artifact type: %s"),

    UNAUTHORIZED_TO_VIEW_ARTIFACT(990503, "Unauthorized to view artifact.",
            403, "User is unauthorized to view governance details related to" +
            " artifact with ID: %s"),


    // API related codes
    ERROR_WHILE_GETTING_API_INFO(990600, "Error while retrieving  API name/version with ID.",
            500, "Error while retrieving  API name/version with ID: %s", true),

    ERROR_WHILE_GETTING_APIM_PROJECT(990601, "Error while retrieving  APIM project.",
            500, "Error while retrieving  APIM project for artifact: %s in the organization: %s", true),

    ERROR_WHILE_GETTING_API_LIST(990602, "Error while retrieving  API list.",
            500, "Error while retrieving  API list in the organization: %s", true),

    ERROR_WHILE_GETTING_API_TYPE(990603, "Error while retrieving  API type.",
            500, "Error while retrieving  API type for API with ID: %s", true),

    ERROR_WHILE_GETTING_API_TYPE_FROM_PROJECT(990604, "Error while retrieving  API type from project.",
            500, "Error while retrieving  API type from project for API", true),

    ERROR_WHILE_GETTING_LC_STATUS_OF_API(990605, "Error while retrieving  LC status of API.",
            500, "Error while retrieving  LC status of API with ID: %s", true),

    ERROR_WHILE_CHECKING_API_AVAILABILITY(990606, "Error while checking API availability.",
            500, "Error while checking API availability for API with ID: %s", true),

    ERROR_WHILE_CHECKING_API_DEPLOYMENT_STATUS(990607, "Error while checking deployment status.",
            500, "Error while checking deployment status for API with ID: %s", true),

    ERROR_WHILE_GETTING_LABELS_FOR_API(990608, "Error while retrieving  labels for API.",
            500, "Error while retrieving  labels for API with ID: %s", true),

    ERROR_WHILE_GETTING_APIS_FOR_LABEL(990609, "Error while retrieving  APIs for label.",
            500, "Error while retrieving  APIs for label with ID: %s", true),

    API_DEFINITION_NOT_FOUND(990610, "API definition not found.",
            404, "API definition not found in API project"),

    API_DETAILS_NOT_FOUND(990611, "API details not found.",
            404, "API details not found in API project"),

    API_DOCUMENT_DATA_NOT_FOUND(990612, "API document data not found.",
            404, "API document data not found in API project"),

    ERROR_WHILE_EXTRACTING_API_DEFINITION(990613, "Error while extracting API Definition content.",
            500, "Error while extracting API definition content from zip", true),

    ERROR_WHILE_EXTRACTING_API_METADATA(990614, "Error while extracting API details.",
            500, "Error while extracting API details from zip", true),

    ERROR_WHILE_EXTRACTING_DOC_DATA(990615, "Error while extracting API documentation data.",
            500, "Error while extracting API documentation data from zip", true),

    ERROR_WHILE_CHECKING_API_VISIBILITY(990616, "Error while checking API visibility.",
            500, "Error while checking API visibility for API with ID: %s", true),

    ;

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
    APIMGovExceptionCodes(long errorCode, String msg, int httpErrorCode, String errorDescription,
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
    APIMGovExceptionCodes(long errorCode, String msg, int httpErrorCode, String errorDescription) {

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

}
