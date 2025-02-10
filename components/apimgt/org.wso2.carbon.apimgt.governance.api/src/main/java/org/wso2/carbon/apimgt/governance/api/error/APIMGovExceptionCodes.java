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
    INTERNAL_SERVER_ERROR(100000, "Internal server error.",
            500, "Internal server error occurred while processing the request.", true),
    ORGANIZATION_NOT_FOUND(100001, "Organization Not Found",
            400, "Organization is not found in the request"),

    // Ruleset related codes
    RULESET_ALREADY_EXIST(200001, "Ruleset already exists",
            409, "Ruleset with name: '%s' in the organization: '%s' already exists."),
    RULESET_CREATION_FAILED(200002, "Ruleset creation failed",
            500, "Ruleset creation failed with name: '%s' in the " +
            "organization: '%s'.", true),
    ERROR_WHILE_RETRIEVING_RULESETS(200003, "Retrieving rulesets failed",
            500, "Error while retrieving rulesets for " +
            "the organization: %s", true),
    ERROR_WHILE_RETRIEVING_RULESET_BY_ID(200004, "Retrieving ruleset by id failed",
            500, "Error while retrieving ruleset with the provided ID", true),
    ERROR_WHILE_UPDATING_RULESET(200005, "Updating ruleset failed",
            500, "Error while updating ruleset: %s", true),
    ERROR_WHILE_DELETING_RULESET(200006, "Deleting ruleset failed",
            500, "Error while deleting ruleset with ID: %s ", true),
    RULESET_NOT_FOUND(200008, "Ruleset not found",
            404, "Ruleset with ID: %s not found"),
    ERROR_WHILE_RETRIEVING_RULESET_CONTENT(200009, "Retrieving ruleset content failed",
            500, "Error while retrieving " +
            "the content of the ruleset with id: %s", true),
    ERROR_WHILE_RETRIEVING_RULESETS_ASSOCIATED_WITH_POLICY(200010,
            "Error while retrieving rulesets associated with " +
                    "policy", 500, "Error while retrieving " +
            "rulesets associated with policy with id: %s", true),
    ERROR_WHILE_INSERTING_RULES(200011, "Rule insertion failed",
            500, "Rule insertion failed for the ruleset with id:" +
            " '%s'", true),
    INVALID_RULESET_CONTENT(200012, "Ruleset content is invalid",
            400, "Content of ruleset '%s' is invalid"),
    ERROR_WHILE_LOADING_DEFAULT_RULESET_CONTENT(200013, "Error while loading default ruleset content",
            500, "Error while loading " +
            "default ruleset provided by WSO2.", true),
    ERROR_WHILE_RETRIEVING_RULESET_BY_NAME(200014, "Retrieving ruleset by name failed",
            500, "Error while retrieving " +
            "ruleset for the organization: %s", true),
    ERROR_FAILED_TO_PARSE_RULESET_CONTENT(200015, "Failed to parse ruleset content",
            500, "Failed to parse ruleset " +
            "content for the ruleset.", true),
    ERROR_RULESET_ASSOCIATED_WITH_POLICIES(200016, "Ruleset is currently " +
            "in use by some governance policies", 409,
            "The ruleset with ID: %s cannot be deleted because it " +
                    "is associated with some governance policies. " +
                    "Please update the associated governance policies before " +
                    "attempting to delete the ruleset."),
    ERROR_WHILE_RETRIEVING_ASSOCIATED_POLICIES(200017, "Error while retrieving " +
            "associated policies for the ruleset.", 500,
            "Error while retrieving associated policies for the ruleset with ID: %s", true),
    ERROR_WHILE_EXTRACTING_RULE_CONTENT(200018, "Error while extracting rule content",
            500, "Error while extracting " +
            "rule content from the ruleset", true),
    ERROR_WHILE_RETRIEVING_RULES_BY_RULESET_ID(200019, "Error while retrieving rules by ruleset id",
            500, "Error while " +
            "retrieving rules by ruleset id: %s", true),
    ERROR_WHILE_SEARCHING_RULESETS(200020, "Error while searching rulesets",
            500, "Error while searching rulesets for the organization: %s", true),

    INVALID_RULESET_CONTENT_DETAILED(200021, "Ruleset content is invalid",
            400, "Content of ruleset %s is invalid: %s"),

    // Policy related codes
    ERROR_WHILE_CREATING_POLICY(300001, "Policy creation failed.",
            500, "Error while creating governance policy " +
            "in the organization %s", true),
    POLICY_NOT_FOUND(300002, "Policy not found",
            404, "Policy with ID: %s not found"),
    ERROR_WHILE_RETRIEVING_POLICIES(300003, "Retrieving policies failed",
            500, "Error while retrieving policies for the organization: %s", true),
    ERROR_WHILE_RETRIEVING_POLICY_BY_ID(300004, "Retrieving policy by id failed",
            500, "Error while retrieving policy" +
            " with id: %s ", true),
    ERROR_WHILE_UPDATING_POLICY(300005, "Updating policy failed",
            500, ("Error while updating policy with id: %s "), true),
    ERROR_WHILE_DELETING_POLICY(300006, "Deleting policy failed",
            500, "Error while deleting policy with ID: %s", true),
    POLICY_ALREADY_EXISTS(300007, "Policy already exists.",
            409, "Policy with name: '%s' in the organization: '%s' already exists."),
    ERROR_WHILE_RETRIEVING_POLICY_BY_NAME(300008, "Retrieving policy by name failed",
            500, "Error while retrieving " +
            "policy with name: %s for the organization: %s", true),
    ERROR_WHILE_RETRIEVING_ACTIONS_BY_POLICY_ID(300010, "Retrieving actions by policy id failed",
            500, "Error while " +
            "retrieving actions by policy id: %s", true),
    ERROR_WHILE_RETRIEVING_LABELS_BY_POLICY_ID(300011, "Retrieving labels by policy id failed",
            500, "Error while retrieving labels by policy id: %s", true),
    ERROR_WHILE_ASSIGNING_ACTION_TO_POLICY(300012, "Error while assigning action to policy",
            400, "Invalid action provided for the policy: %s"),
    ERROR_WHILE_SEARCHING_POLICIES(300013, "Error while searching policies",
            500, "Error while searching policies for the organization: %s", true),
    ERROR_WHILE_DELETING_LABEL_POLICY_MAPPINGS(300014, "Error while deleting label policy mappings",
            500, "Error while deleting label policy mappings for " +
            "label with ID: %s", true),

    // Request related codes
    ERROR_WHILE_ADDING_NEW_GOV_EVAL_REQUEST(400001, "Error while adding new governance evaluation request.",
            500, "Error while adding new governance evaluation request for artifact: %s", true),
    ERROR_WHILE_GETTING_GOV_EVAL_REQUEST_FOR_ARTIFACT(400002, "Error while getting governance evaluation request.",
            500, "Error while getting governance evaluation request for artifact: %s", true),
    ERROR_WHILE_GETTING_GOV_EVAL_REQUESTS(400003, "Error while getting governance evaluation requests.",
            500, "Error while getting governance evaluation requests"),
    ERROR_WHILE_UPDATING_GOV_EVAL_REQUEST(400004, "Error while updating governance evaluation request.",
            500, "Error while updating governance evaluation request with ID: %s", true),
    ERROR_WHILE_DELETING_GOVERNANCE_EVAL_REQUEST(400005, "Error while deleting governance evaluation request.",
            500, "Error while deleting governance evaluation request with ID: %s"),
    ERROR_WHILE_DELETING_GOVERNANCE_EVAL_REQUESTS(400006, "Error while deleting governance evaluation requests.",
            500, "Error while deleting governance evaluation requests."),
    ERROR_WHILE_CHANGING_PROCESSING_REQ_TO_PENDING(400007, "Error while changing processing requests to pending.",
            500, "Error while changing processing requests to pending", true),
    ERROR_WHILE_GETTING_COMPLIANCE_PENDING_ARTIFACTS(400008, "Error while getting compliance pending artifacts.",
            500, "Error while getting compliance pending artifacts", true),

    // Result related codes
    ERROR_WHILE_SAVING_GOVERNANCE_RESULT(500001, "Error while saving governance result.",
            500, "Error while saving governance result for artifact: %s", true),
    ERROR_WHILE_DELETING_GOVERNANCE_RESULT(500002, "Error while deleting governance result.",
            500, "Error while deleting governance result with ID: %s", true),
    ERROR_WHILE_SAVING_RULE_VIOLATIONS(500003, "Error while saving rule violations.",
            500, "Error while saving rule violations", true),
    ERROR_WHILE_CLEARING_RULE_VIOLATIONS(500004, "Error while clearing rule violations.",
            500, "Error while clearing rule violations", true),
    ERROR_WHILE_GETTING_RULE_VIOLATIONS(500005, "Error while getting rule violations.",
            500, "Error while getting rule violations", true),
    ERROR_WHILE_GETTING_GOVERNANCE_RESULTS(500006, "Error while getting governance results.",
            500, "Error while getting governance results", true),
    ERROR_WHILE_DELETING_GOVERNANCE_RESULTS(500007, "Error while deleting governance results.",
            500, "Error while deleting governance results", true),
    ERROR_WHILE_DELETING_RULE_VIOLATIONS_BY_RESULT_ID(500008, "Error while deleting rule " +
            "violations by result ID.",
            500, "Error while deleting rule violations by result ID: %s", true),
    ERROR_WHILE_DELETING_EVALUATION_RESULTS_FOR_RULESET(500009, "Error while deleting evaluation " +
            "results for ruleset.",
            500, "Error while deleting evaluation results for ruleset with ID: %s",
            true),
    ERROR_WHILE_DELETING_COMPLIANCE_EVALUATION_RESULTS_FOR_POLICY(500010, "Error while deleting compliance " +
            "evaluation results for policy.",
            500, "Error while deleting compliance evaluation results for policy with ID: %s",
            true),
    ERROR_WHILE_DELETING_RULE_VIOLATIONS_FOR_POLICY(500011, "Error while deleting rule " +
            "violations for policy.",
            500, "Error while deleting rule violations for policy with ID: %s", true),

    // Artifact related codes
    ERROR_WHILE_SAVING_ARTIFACT_INFO(600001, "Error while saving artifact info.",
            500, "Error while saving artifact info for artifact: %s in the organization: %s"),
    ERROR_WHILE_RETRIEVING_ARTIFACT_INFO(600002, "Error while retrieving artifact info.",
            500, "Error while retrieving artifact info for artifact: %s", true),
    ARTIFACT_INFO_NOT_FOUND(600003, "Artifact info not found.",
            404, "Artifact info not found for artifact: %s in the organization: %s"),
    ARTIFACT_NOT_FOUND(600004, "Artifact not found.", 404,
            "Artifact with id: %s not found in the organization: %s"),
    ERROR_WHILE_DELETING_GOVERNANCE_DATA(600005, "Error while deleting governance data.",
            500, "Error while deleting governance data for artifact: %s", true),
    ARTIFACT_NOT_FOUND_WITH_NAME_AND_VERSION(600006, "Artifact not found with name and version.",
            404, "Artifact not found with name: %s and version: %s in the organization: %s"),

    ERROR_FAILED_TO_READ_ARTIFACT_PROJECT(600007, "Error while reading artifact project.",
            500, "Error while reading artifact project from given file path. ", true),

    INVALID_ARTIFACT_TYPE(600008, "Invalid artifact type.",
            400, "Invalid artifact type: %s"),

    // APIM related codes

    ERROR_WHILE_GETTING_API_INFO(700001, "Error while getting API name/version with ID.",
            500, "Error while getting API name/version with ID: %s", true),
    ERROR_WHILE_GETTING_APIM_PROJECT(700002, "Error while getting APIM project.",
            500, "Error while getting APIM project for artifact: %s in the organization: %s",
            true),
    API_DEFINITION_NOT_FOUND(700003, "API definition not found.",
            404, "API definition not found in API project"),
    API_DETAILS_NOT_FOUND(700004, "API details not found.",
            404, "API details not found in API project"),
    ERROR_WHILE_EXTRACTING_API_DEFINITION(700005, "Error while extracting API Definition content.",
            500, "Error while extracting API definition content from zip", true),
    ERROR_WHILE_EXTRACTING_API_METADATA(700006, "Error while extracting API details.",
            500, "Error while extracting API details from zip", true),
    ERROR_WHILE_GETTING_API_LIST(700007, "Error while getting API list.",
            500, "Error while getting API list in the organization: %s", true),
    ERROR_WHILE_GETTING_LC_STATUS_OF_API(700008, "Error while getting LC status of API.",
            500, "Error while getting LC status of API with ID: %s", true),

    ERROR_WHILE_GETTING_LABELS_FOR_API(700009, "Error while getting labels for API.",
            500, "Error while getting labels for API with ID: %s", true),

    ERROR_WHILE_GETTING_APIS_FOR_LABEL(700010, "Error while getting APIs for label.",
            500, "Error while getting APIs for label with ID: %s", true),

    ERROR_WHILE_GETTING_API_UUID_WITH_NAME_VERSION(700011, "Error while getting API UUID " +
            "with name and version.",
            500, "Error while getting API UUID with name: %s and version: %s", true),

    ERROR_WHILE_GETTING_API_TYPE(700012, "Error while getting API type.",
            500, "Error while getting API type for API with ID: %s", true),

    ERROR_WHILE_EXTRACTING_DOC_DATA(700013, "Error while extracting API documentation data.",
            500, "Error while extracting API documentation data from zip", true),

    API_DOCUMENT_DATA_NOT_FOUND(700014, "API document data not found.",
            404, "API document data not found in API project"),
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
