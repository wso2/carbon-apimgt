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

public enum GovernanceExceptionCodes implements ErrorHandler {

    // TODO: Clean up

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
            500, "Error while updating ruleset: %s in the " +
            "organization: %s", true),
    ERROR_WHILE_DELETING_RULESET(200006, "Deleting ruleset failed",
            500, "Error while deleting ruleset with ID: %s in" +
            " the organization: %s", true),
    ERROR_WHILE_DELETING_RULES(200007, "Deleting rules failed",
            500, "Error while deleting rules for the ruleset with ID: %s", true),
    RULESET_NOT_FOUND(200008, "Ruleset not found",
            404, "Ruleset with ID: %s not found"),
    ERROR_WHILE_RETRIEVING_RULESET_CONTENT(200009, "Retrieving ruleset content failed",
            500, "Error while retrieving " +
            "the content of the ruleset with id: %s in the organization: %s", true),
    ERROR_WHILE_RETRIEVING_RULESETS_ASSOCIATED_WITH_POLICY(200010,
            "Error while retrieving rulesets associated with " +
                    "policy", 500, "Error while retrieving " +
            "rulesets associated with policy with id: %s", true),
    ERROR_WHILE_INSERTING_RULES(200011, "Rule insertion failed",
            500, "Rule insertion failed for the ruleset with id:" +
            " '%s'", true),
    INVALID_RULESET_CONTENT(200012, "Ruleset content is invalid",
            400, "Content of ruleset '%s' is invalid"),
    INVALID_RULESET_CONTENT_WITH_INFO(200013, "Invalid Ruleset content",
            400, "Invalid Ruleset content, message: '%s'"),
    ERROR_WHILE_LOADING_DEFAULT_RULESET_CONTENT(200014, "Error while loading default ruleset content",
            500, "Error while loading " +
            "default ruleset provided by WSO2.", true),
    ERROR_WHILE_RETRIEVING_RULESET_BY_NAME(200015, "Retrieving ruleset by name failed",
            500, "Error while retrieving " +
            "ruleset for the organization: %s", true),
    ERROR_FAILED_TO_PARSE_RULESET_CONTENT(200016, "Failed to parse ruleset content",
            500, "Failed to parse ruleset " +
            "content for the ruleset.", true),
    ERROR_RULESET_ASSOCIATED_WITH_POLICIES(200017, "Ruleset is currently " +
            "in use by some governance policies", 409,
            "The ruleset with ID: %s cannot be deleted because it " +
                    "is associated with some governance policies in the " +
                    "organization: %s. Please update the associated governance policies before attempting to delete the ruleset."),
    ERROR_WHILE_RETRIEVING_ASSOCIATED_POLICIES(200018, "Error while retrieving " +
            "associated policies for the ruleset.", 500,
            "Error while retrieving associated policies for the ruleset with ID: %s", true),
    ERROR_WHILE_EXTRACTING_RULE_CONTENT(200019, "Error while extracting rule content",
            500, "Error while extracting " +
            "rule content from the ruleset", true),
    ERROR_WHILE_RETRIEVING_RULES_BY_RULESET_ID(200020, "Error while retrieving rules by ruleset id",
            500, "Error while " +
            "retrieving rules by ruleset id: %s", true),

    // Policy related codes
    ERROR_WHILE_CREATING_POLICY(401001, "Policy creation failed.",
            500, "Error while creating governance policy " +
            "in the organization %s", true),
    POLICY_NOT_FOUND(401002, "Policy not found", 404, "Policy with ID: %s not found"),
    ERROR_WHILE_RETRIEVING_POLICIES(401003, "Retrieving policies failed", 500, "Error while retrieving policies for the organization: %s", true),
    ERROR_WHILE_RETRIEVING_POLICY_BY_ID(401004, "Retrieving policy by id failed", 500, "Error while retrieving policy" +
            " with id: %s ", true),
    ERROR_WHILE_UPDATING_POLICY(401005, "Updating policy failed", 500, ("Error while updating policy with id: %s in " +
            "the organization: %s"), true),
    ERROR_WHILE_DELETING_POLICY(401006, "Deleting policy failed", 500, "Error while deleting policy with ID: %s in " +
            "the organization: %s", true),
    POLICY_ALREADY_EXISTS(401007, "Policy already exists.", 409, "Policy with name: '%s' in the organization: '%s' already exists."),

    ERROR_WHILE_RETRIEVING_POLICY_BY_NAME(401008, "Retrieving policy by name failed", 500, "Error while retrieving " +
            "policy with name: %s for the organization: %s", true),

    ERROR_WHILE_RETRIEVING_POLICIES_BY_LABEL(401009, "Retrieving policies by label failed", 500, "Error while retrieving " +
            "policies by label: %s for the organization: %s", true),

    ERROR_WHILE_RETRIEVING_ACTIONS_BY_POLICY_ID(401010, "Retrieving actions by policy id failed", 500, "Error while " +
            "retrieving actions by policy id: %s", true),

    ERROR_WHILE_RETRIEVING_LABELS_BY_POLICY_ID(401011, "Retrieving labels by policy id failed", 500, "Error while " +
            "retrieving labels by policy id: %s", true),

    // Artifact related codes
    ERROR_WHILE_SAVING_ARTIFACT_INFO(501000, "Error while saving artifact info.", 500, "Error while saving " +
            "artifact info for artifact: %s in the organization: %s"),
    ERROR_WHILE_PROCESSING_GOVERNANCE_EVALUATION_REQUEST(501001, "Error while processing governance evaluation " +
            "request.",
            500,
            "Error while processing governance evaluation request for artifact/api: %s in the organization: %s", true),

    ERROR_WHILE_GETTING_GOVERNANCE_EVALUATION_REQUESTS(501002, "Error while getting governance evaluation requests.",
            500,
            "Error while getting governance evaluation requests"),

    ERROR_WHILE_UPDATING_GOVERNANCE_EVALUATION_REQUEST(501003, "Error while updating governance evaluation request.",
            500, "Error while updating governance evaluation request with ID: %s", true),

    ERROR_WHILE_DELETING_GOVERNANCE_EVALUATION_REQUEST(501004, "Error while deleting governance evaluation request.",
            500, "Error while deleting governance evaluation request with ID: %s"),

    ERROR_WHILE_DELETING_GOVERNANCE_EVALUATION_REQUESTS(501004, "Error while deleting governance evaluation " +
            "requests.",
            500, "Error while deleting governance evaluation requests."),

    ERROR_WHILE_RETRIEVING_ARTIFACT_INFO(501005, "Error while retrieving artifact info.", 500, "Error while " +
            "retrieving artifact info for artifact: %s", true),

    ARTIFACT_INFO_NOT_FOUND(501006, "Artifact info not found.", 404, "Artifact info not found for artifact: %s in the "),

    // Governance Results related codes

    ERROR_WHILE_SAVING_GOVERNANCE_RESULT(601001, "Error while saving governance result.", 500, "Error while saving " +
            "governance result for artifact: %s", true),

    ERROR_WHILE_DELETING_GOVERNANCE_RESULT(601002, "Error while deleting governance result.", 500, "Error while deleting " +
            "governance result with ID: %s", true),
    ERROR_WHILE_SAVING_RULE_VIOLATIONS(601003, "Error while saving rule violations.", 500, "Error while saving rule " +
            "violations", true),

    ERROR_WHILE_CLEARING_RULE_VIOLATIONS(601004, "Error while clearing rule violations.", 500, "Error while clearing " +
            "rule violations", true),

    ERROR_WHILE_GETTING_RULE_VIOLATIONS(601005, "Error while getting rule violations.", 500, "Error while getting " +
            "rule violations", true),

    ERROR_WHILE_GETTING_GOVERNANCE_RESULTS(601006, "Error while getting governance results.", 500, "Error while getting " +
            "governance results", true),

    ERROR_WHILE_DELETING_GOVERNANCE_RESULTS(601007, "Error while deleting governance results.", 500, "Error while " +
            "deleting governance results", true),


    // APIM related codes
    INVALID_APIM_CREDENTIALS(701001, "Invalid credentials", 500, "Invalid APIM admin credentials", true),
    ERROR_WHILE_GETTING_APIM_PROJECT(701002, "Error while getting APIM project.", 500, "Error while getting APIM project for endpoint: %s in the organization: %s", true),
    API_DEFINITION_NOT_FOUND(701003, "API definition not found.", 404, "API definition not found for api: %s in the " +
            "organization: %s"),
    API_DETAILS_NOT_FOUND(701004, "API details not found.", 404, "API details not found for endpoint: %s in the organization: %s"),
    ERROR_WHILE_EXTRACTING_API_DEFINITION(701005, "Error while extracting API Definition content.", 500, "Error " +
            "while extracting API definition content from zip for the api : %s", true),
    ERROR_WHILE_EXTRACTING_API_METADATA(701006, "Error while extracting api details.", 500, "Error while extracting " +
            "api details from zip for the api: %s", true),
    ERROR_WHILE_GETTING_API_LIST(701007, "Error while getting API list.", 500, "Error while getting API list in the " +
            "organization: %s", true),
    ENDPOINT_NOT_FOUND_IN_APIM(701008, "Endpoint not found.", 404, "Endpoint can not be found on APIM",
            true),
    INTERNAL_SERVER_ERROR_FROM_APIM(701009, "Internal server error occurred.", 500,
            "An internal server error occurred while fetching from APIM", true);

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

}
