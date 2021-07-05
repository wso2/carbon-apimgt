/*
 *
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

package org.wso2.carbon.apimgt.api;

import java.util.Arrays;

/**
 * This enum class holds error codes that we need to pass to upper level. For example, to the UI.
 * You have to define your custom error codes here.
 */
public enum
ExceptionCodes implements ErrorHandler {

    // API, Application related codes
    API_ALREADY_EXISTS(900300, "The API already exists.", 409, " The API already exists"),
    APPLICATION_ALREADY_EXISTS(900301, "The application already exists.", 409, " The application already exists"),
    APIMGT_DAO_EXCEPTION(900302, "Internal server error.", 500, " Error occurred while persisting/retrieving data"),
    APIMGT_LIFECYCLE_EXCEPTION(900303, "Lifecycle exception occurred", 500, " Error occurred while changing " +
            "lifecycle state"),
    TIER_CANNOT_BE_NULL(900304, "The tier cannot be null.", 400, " The tier cannot be null"),
    TIER_NAME_INVALID(900305, "The tier name is invalid.", 400, " The tier name is invalid"),
    APPLICATION_NOT_FOUND(900307, "Application not found", 404, "Application not found"),
    API_NOT_FOUND(900308, "API Not Found", 404, "Requested API with id %s not found"),
    APPLICATION_INACTIVE(900309, "Application is not active", 400, "Application is not active"),
    SUBSCRIPTION_NOT_FOUND(900310, "Subscription not found", 404, "Couldn't retrieve Subscriptions for API"),
    UPDATE_STATE_CHANGE(900311, "API fields have state changes", 400, "Couldn't Update as API have changes can't be " +
            "done"),
    DOCUMENT_ALREADY_EXISTS(900312, "Document already exists", 409, "Document already exists"),
    COULD_NOT_UPDATE_API(900313, "Error has occurred. Could not update the API", 500, "Error has occurred. Could not "
            + "update the API"),
    DOCUMENT_CONTENT_NOT_FOUND(900314, "Document content not found", 404, "Document content not found"),
    DOCUMENT_NOT_FOUND(900315, "Document not found", 404, "Document not found"),
    API_EXPORT_ERROR(900316, "API export Error", 500, "Error while exporting the given APIs"),
    API_IMPORT_ERROR(900317, "API import Error", 500, "Error while importing the given APIs"),
    SUBSCRIPTION_STATE_INVALID(900318, "Invalid state change for subscription", 400, "Invalid state change for " +
            "subscription"),
    APIM_DAO_EXCEPTION(900320, "Internal server error.", 500, " Error occurred while retrieving data"),
    GATEWAY_LABELS_CANNOT_BE_NULL(900321, "Gateway labels cannot be null.", 400, "Gateway labels cannot be null"),
    STATUS_CANNOT_BE_NULL(900322, "Status cannot be null.", 400, " Status cannot be null"),
    RATING_NOT_FOUND(900324, "Rating not found", 404, "Couldn't retrieve rating"),
    RATING_VALUE_INVALID(900325, "Rating value invalid", 400, "Provided rating value does not fall in between min max "
            + "values"),
    API_TYPE_INVALID(900327, "API Type specified is invalid.", 400, "API Type specified is invalid"),
    COMPOSITE_API_ALREADY_EXISTS(900328, "A Composite API already exists.", 409,
            "A Composite API already exists for this application"),
    API_DEFINITION_NOT_FOUND(900330, "API definition not found", 404, "API definition not found"),
    APPLICATION_KEY_MAPPING_NOT_FOUND(900331, "Application Key mapping not found", 404, "Application Key mapping not " +
            "found"),
    NO_UPDATE_PERMISSIONS(900332, "No permissions to update API.", 403, "No permissions to update API."),
    NO_DELETE_PERMISSIONS(900333, "No permissions to delete API.", 403, "No permissions to delete API."),
    API_ATTRIBUTE_NOT_FOUND(900335, "API attribute not found", 404, "API attribute not found"),
    SUBSCRIPTION_ALREADY_EXISTS(900336, "Subscription already exists", 409, "Subscription already exists"),
    SDK_NOT_GENERATED(900337, "Error while generating SDK", 500, "Error while generating SDK"),
    APPLICATION_EXPORT_ERROR(900338, "Application Export Error", 500, "Error while exporting the given Application"),
    APPLICATION_IMPORT_ERROR(900339, "Application Import Error", 500, "Error while importing the given Application"),
    NO_READ_PERMISSIONS(900340, "No permissions to read API.", 403, "No permissions to read API."),
    API_PRODUCT_DUPLICATE_RESOURCE(900341, "Cannot create API Product with duplicate resource",
            400, "Cannot create API Product with duplicate resource: %s , verb: %s combination"),
    API_PRODUCT_RESOURCE_ENDPOINT_UNDEFINED(900342,
            "Cannot create API Product, due to resources with undefined endpoints in their parent APIs",
            409, "Cannot create API Product %s, due to resources with undefined endpoints in their parent APIs %s"),
    API_PRODUCT_WITH_UNSUPPORTED_LIFECYCLE_API(900343,
            "Cannot create API Product, due to resources parent API being in an unsupported Life Cycle state",
            409, "Cannot create API Product, due to resources parent API being in an unsupported Life Cycle state: %s"),

    // Generic codes
    JSON_PARSE_ERROR(900400, "Json parse error", 500, "JSON parse error"),
    RESOURCE_NOT_FOUND(900401, "Resource not found", 404, "Requested resource not found"),
    RESOURCE_RETRIEVAL_FAILED(900402, "Resource retrieval failed", 400, "Resource retrieval failed"),
    USER_MAPPING_RETRIEVAL_FAILED(900404, "User mapping retrieval failed", 404, "User mapping retrieval failed"),
    MALFORMED_URL(900403, "Malformed URL", 400, "Malformed URL"),

    // Endpoint related codes
    ENDPOINT_NOT_FOUND(900450, "Endpoint Not Found", 404, "Endpoint Not Found"),
    ENDPOINT_ALREADY_EXISTS(900451, "Endpoint already exists", 409, "Endpoint already exists"),
    ENDPOINT_ADD_FAILED(900452, "Endpoint adding failed", 400, "Endpoint adding failed"),
    ENDPOINT_DELETE_FAILED(900453, "Endpoint Delete Failed", 400, "Endpoint Delete Failed"),

    // Service Endpoint Discovery related codes
    ERROR_LOADING_SERVICE_DISCOVERY_IMPL_CLASS(900460, "Error loading service discovery impl class", 500,
            "Error while trying to load a service discovery impl class"),
    ERROR_INITIALIZING_SERVICE_DISCOVERY(900461, "Error initializing service discovery", 500,
            "Error while connecting to the system with services"),
    ERROR_WHILE_TRYING_TO_DISCOVER_SERVICES(900462, "Error while discovering services", 500,
            "Error while trying to discover service endpoints"),

    // Gateway related codes
    API_DEFINITION_MALFORMED(900500, "ApiDefinition not found", 400, "Failed to retrieve API Definition"),
    TEMPLATE_EXCEPTION(900501, "Service configuration Error", 500, " Error generate service config"),
    GATEWAY_EXCEPTION(900502, "Gateway publishing Error", 500, " Error occurred while publishing to Gateway"),
    BROKER_EXCEPTION(900503, "Broker Connection Error", 500, " Error occurred while obtaining broker connection"),
    GATEWAY_ENVIRONMENT_NOT_FOUND(900504, "Invalid Gateway Environment", 400, "Gateway Environment with name '%s' not found"),
    NO_GATEWAY_ENVIRONMENTS_ADDED(900505, "No Gateway Environments Available", 400, "No gateway environments " +
            "available for the API : %s."),

    // Workflow related codes
    WORKFLOW_EXCEPTION(900550, "Workflow error", 500,
            "Error occurred while executing workflow task"),
    WORKFLOW_NOT_FOUND(900551, "Workflow error", 404,
            "Workflow entry cannot be found for the given reference id"),
    WORKFLOW_ALREADY_COMPLETED(900552, "Workflow error", 400,
            "Workflow is already completed"),
    WORKFLOW_PENDING(900553, "Workflow exception", 409,
            "Pending workflow task exists for the seleted API"),
    WORKFLOW_INVALID_WFTYPE(900554, "Workflow error", 500, "Invalid workflow type specified"),
    WORKFLOW_INV_STORE_WFTYPE(900555, "Workflow error", 500, "Invalid workflow type for store workflows"),
    WORKFLOW_STATE_MISSING(900556, "Workflow error", 400, "Workflow status is not defined"),
    WORKFLOW_NO_PENDING_TASK(900557, "Workflow error", 412,
            "Requested resource does not have a pending workflow task"),
    WORKFLOW_REJCECTED(900558, "Workflow error", 403, "Requested action is rejected"),
    INCOMPATIBLE_WORKFLOW_REQUEST_FOR_PUBLISHER(900559, "Incompatible workflow request", 400, "Incompatible workflow " +
            "request received by publisher"),
    INCOMPATIBLE_WORKFLOW_REQUEST_FOR_STORE(900560, "Incompatible workflow request", 400, "Incompatible workflow " +
            "request received by store"),
    WORKFLOW_RETRIEVE_EXCEPTION(900561, "Workflow retrieval error", 400, "Provided parameter is not valid"),

    // Auth related codes
    ROLES_CANNOT_BE_EMPTY(900600, "Role list cannot be empty", 400, "Role list cannot be empty"),
    ROLES_CANNOT_BE_NULL(900601, "Role list cannot be null", 400, "Role list cannot be null"),
    UNSUPPORTED_ROLE(900602, "Non existing roles cannot be added to an API", 400,
            "Non existing roles cannot be added to an API"),
    USER_DOES_NOT_EXIST(900603, "User does not exist in the system", 404, "User does not exist in the system"),
    USER_CREATION_FAILED(900604, "User creation failed", 500, "User creation failed"),
    IDP_INITIALIZATION_FAILED(900605, "Identity Provider initialization failed", 500,
            "Identity provider initialization failed"),
    KEY_MANAGER_INITIALIZATION_FAILED(900606, "Key Manager initialization failed", 500,
            "Key Manager initialization failed",true),
    ROLE_DOES_NOT_EXIST(900607, "Role does not exist in the system", 404, "Role does not exist in the system"),
    MULTIPLE_ROLES_EXIST(900608, "Multiple roles with the same display name exist in the system", 500, "Multiple " +
            "roles with the same display name exist in the system"),
    MULTIPLE_USERS_EXIST(900609, "Multiple users with the same username exist in the system", 500, "Multiple " +
            "users with the same username exist in the system"),


    // Labels related codes
    LABEL_INFORMATION_CANNOT_BE_NULL(900650, "Label information cannot be null", 400, "Label information cannot be " +
            "null"),
    LABEL_EXCEPTION(900651, "Label Error", 500, "Error occurred while retrieving label information"),
    LABEL_NOT_FOUND(900652, "Label Not Found", 404, "Label with specified name cannot be found."),
    LABEL_NOT_FOUND_IN_API(900653, "Label Not Found In API", 404, "Label with specified name"
            + " cannot be found in the API."),
    LABEL_ADDING_FAILED(900654, "Label Error", 500, "Error occurred while trying to add label"),
    LABEL_UPDATE_FAILED(900655, "Label Error", 500, "Error occurred while trying to update label"),
    LABEL_DELETION_FAILED(900656, "Label Error", 500, "Error occurred while trying to delete label"),


    //WSDL related codes
    INVALID_WSDL_URL_EXCEPTION(900675, "Invalid WSDL", 400, "Invalid WSDL URL"),
    CANNOT_PROCESS_WSDL_CONTENT(900676, "Invalid WSDL", 400, "Provided WSDL content cannot be processed"),
    INTERNAL_WSDL_EXCEPTION(900677, "Internal WSDL error", 500, "Internal error while processing WSDL"),
    UNSUPPORTED_WSDL_EXTENSIBILITY_ELEMENT(900678, "Invalid WSDL", 400, "WSDL extensibility element not supported"),
    ERROR_WHILE_INITIALIZING_WSDL_FACTORY(900679, "Internal WSDL error", 500, "Error while initializing WSDL factory"),
    ERROR_WHILE_CREATING_WSDL_ARCHIVE(900680, "Internal WSDL error", 500, "Error while creating WSDL archive"),
    NO_WSDL_FOUND_IN_WSDL_ARCHIVE(900681, "Invalid WSDL Archive", 400, "No valid WSDLs found in the provided WSDL archive"),
    CONTENT_NOT_RECOGNIZED_AS_WSDL(900682, "Invalid WSDL Content", 400, "Provided content is not recognized as a WSDL"),
    URL_NOT_RECOGNIZED_AS_WSDL(900683, "Invalid WSDL URL", 400, "Provided URL is not recognized as a WSDL"),
    NO_WSDL_AVAILABLE_FOR_API(900684, "WSDL Not Found", 404, "No WSDL Available for the API %s:%s"),
    CORRUPTED_STORED_WSDL(900685, "Corrupted Stored WSDL", 500, "The WSDL of the API %s is corrupted."),
    UNSUPPORTED_WSDL_FILE_EXTENSION(900686, "Unsupported WSDL File Extension", 400, "Unsupported extension. Only supported extensions are .wsdl and .zip"),


    //OpenAPI/Swagger related codes [900750 900???)
    MALFORMED_OPENAPI_DEFINITON(900758, "Malformed OpenAPI Definition", 400, "The provided OpenAPI definition is not parsable as a valid JSON or YAML."),
    UNRECOGNIZED_OPENAPI_DEFINITON(900759, "Unrecognized OpenAPI Definition", 400, "The definition is parsable but cannot be identified as an OpenAPI definition."),
    INVALID_SWAGGER_VERSION(900760, "Invalid Swagger Definition", 400, "Unsupported swagger version provided. Please add with swagger version 2.0."),
    INVALID_SWAGGER_PARAMS(900751, "Invalid Swagger Definition", 400, "Swagger contains invalid parameters. Please add valid swagger definition."),
    INVALID_OPENAPI_VERSION(900752, "Invalid OpenAPI Definition", 400, "Unsupported OpenAPI version provided. Please add with OpenAPI version 3.0.0."),
    INVALID_OPENAPI_NO_INFO_PATH(900753, "Invalid OpenAPI Definition", 400, "Required property 'info' or 'paths' are not provided."),
    OPENAPI_PARSE_EXCEPTION(900754, "Error while parsing OpenAPI definition", 400, "Error while parsing OpenAPI definition"),
    OPENAPI_NOT_FOUND(900755, "OpenAPI definition not found", 404, "OpenAPI definition not found"),
    OPENAPI_URL_MALFORMED(900756, "OpenAPI definition retrieval from URL failed", 400, "Exception occurred while retrieving the OpenAPI definition from URL"),
    OPENAPI_URL_NO_200(900757, "OpenAPI definition retrieval from URL failed", 400, "Response didn't return a 200 OK status"),
    INVALID_OAS2_FOUND(900761, "Invalid OpenAPI V2 definition found", 400, "Invalid OpenAPI V2 definition found"),
    INVALID_OAS3_FOUND(900762, "Invalid OpenAPI V3 definition found", 400, "Invalid OpenAPI V3 definition found"),
    NO_RESOURCES_FOUND(900763, "No resources found", 404, "API must have at least one resource defined"),
    ERROR_REMOVING_EXAMPLES(900764, "Internal Error While Processing Swagger Definition", 500, "Couldn't remove one or more examples from the swagger definition"),


    // REST API related codes
    PARAMETER_NOT_PROVIDED(900700, "Parameter value missing", 400,
            "Some of the mandatory parameter values were missing"),
    LOCATION_HEADER_INCORRECT(900701, "Error while obtaining URI for Location header", 500,
            "Error occurred while obtaining URI for Location header"),
    LAST_UPDATED_TIME_RETRIEVAL_ERROR(900702, "Error while retrieving last access time for the resource", 500,
            "Error while retrieving last access time for the resource"),
    INVALID_DATE_TIME_STAMP(900703, "Invalid timestamp value", 400, "Timestamp should be in ISO8601 format"),
    LENGTH_EXCEEDS(900704, "Character length exceeds the allowable limit", 400,
            "One of the provided input character length exceeds the allowable limit."),
    BLANK_PROPERTY_VALUE(900705, "Blank value for required property", 400,
            "%s property value of payload cannot be blank"),
    CONTAIN_SPECIAL_CHARACTERS(900706, "contain invalid characters", 400,
            "%s property value of payload cannot contain invalid characters"),

    //GraphQL API related codes
    API_NOT_GRAPHQL(900800, "This API is not a GraphQL API", 400, "This API is not a GraphQL API"),


    // Oauth related codes
    AUTH_GENERAL_ERROR(900900, "Authorization Error", 403, " Error in authorization"),
    INVALID_CREDENTIALS(900901, "Invalid Credentials", 401, " Invalid username or password"),
    MISSING_CREDENTIALS(900902, "Missing Credentials", 401, " Please provide an active access token to proceed"),
    ACCESS_TOKEN_EXPIRED(900903, "Invalid Credentials", 401, " Access token is expired."),
    ACCESS_TOKEN_INACTIVE(900904, "Access Token Error", 401, " Access token is inactive."),
    USER_NOT_AUTHENTICATED(900905, "User is not Authenticated", 401, " User is not authenticated."),
    ACCESS_TOKEN_INVALID(900906, "Invalid Credentials", 401, " Access token is invalid."),

    INVALID_SCOPE(900910, "Invalid Scope", 403, " You are not authorized to access the resource."),
    INVALID_AUTHORIZATION_HEADER(900911, "Invalid Authorization header", 401,
            " Please provide the Authorization : Bearer <> token to proceed."),
    MALFORMED_AUTHORIZATION_HEADER_OAUTH(900912, "Malformed Authorization Header", 400,
            "Please provide the Authorization : Bearer <> token to proceed."),
    MALFORMED_AUTHORIZATION_HEADER_BASIC(900913, "Malformed Authorization Header", 400,
            "Please provide the Authorization : Basic <> token to proceed."),
    OAUTH2_APP_CREATION_FAILED(900950, "Key Management Error", 500, "Error while creating the consumer application."),
    OAUTH2_APP_ALREADY_EXISTS(900951, "Key Management Error", 409, "OAuth2 application already created."),
    OAUTH2_APP_DELETION_FAILED(900952, "Key Management Error", 500, "Error while deleting the consumer application."),
    OAUTH2_APP_UPDATE_FAILED(900953, "Key Management Error", 500, "Error while updating the consumer application."),
    OAUTH2_APP_RETRIEVAL_FAILED(900954, "Key Management Error", 500, "Error while retrieving the consumer application."
    ),
    APPLICATION_TOKEN_GENERATION_FAILED(900957, "Keymanagement Error", 500, " Error while generating the application" +
            "access token."),
    UNSUPPORTED_THROTTLE_LIMIT_TYPE(900960, "Throttle Policy Error", 400, "Throttle Limit type is not supported"),
    POLICY_NOT_FOUND(900961, "Policy Not found", 404, "Failed to retrieve Policy Definition"),
    OAUTH2_APP_MAP_FAILED(900962, "Key Management Error", 500, "Error while mapping an existing consumer application."),
    TOKEN_INTROSPECTION_FAILED(900963, "Key Management Error", 500, "Error while introspecting the access token."),
    ACCESS_TOKEN_GENERATION_FAILED(900964, "Key Management Error", 500, "Error while generating a new access token."),
    INVALID_TOKEN_REQUEST(900965, "Key Management Error", 400, "Invalid access token request."),
    ACCESS_TOKEN_REVOKE_FAILED(900966, "Key Management Error", 500, "Error while revoking the access token."),
    INTERNAL_ERROR(900967, "General Error", 500, "Server Error Occurred"),
    POLICY_LEVEL_NOT_SUPPORTED(900968, "Throttle Policy level invalid", 400, "Specified Throttle policy level is not "
            + "valid"),
    JWT_PARSING_FAILED(900986, "Key Management Error", 500, "Error while parsing JWT. Invalid Jwt."),
    POLICY_DELETE_ERROR(900988, "Cannot delete the policy", 500, "%s"),

    //Throttle related codes
    THROTTLE_TEMPLATE_EXCEPTION(900969, "Policy Generating Error", 500, " Error while generate policy configuration"),
    ENDPOINT_CONFIG_NOT_FOUND(90070, "Endpoint Config Not found", 404, "Error while retrieving Endpoint " +
            "Configuration"),
    UNSUPPORTED_THROTTLE_CONDITION_TYPE(900975, "Throttle Condition Error", 400, "Throttle Condition type is not "
            + "supported"),
    INVALID_DOCUMENT_CONTENT_DATA(900976, "Invalid document content data provided", 400, "Mismatch between provided " +
            "document content data and Document Source Type given"),
    BLOCK_CONDITION_UNSUPPORTED_API_CONTEXT(900977, "Block Condition Error", 400, "API Context does not exist"),
    BLOCK_CONDITION_UNSUPPORTED_APP_ID_NAME(900978, "Block Condition Error", 400, "Application ID or Name does not " +
            "exist"),
    BLOCK_CONDITION_ALREADY_EXISTS(900979, "The Block Condition exists.", 409, " The Block Condition already exists"),
    SYSTEM_APP_NOT_FOUND(900980, "System Application not found", 409, "System Application not found"),

    SHARED_SCOPE_NOT_FOUND(900981, "Shared Scope not found", 404,
            "Requested Shared Scope ID  %s could not be found"),
    SHARED_SCOPE_ID_NOT_SPECIFIED(900982, "Shared Scope ID not specified", 400,
            "Shared Scope ID not specified"),
    SHARED_SCOPE_NAME_NOT_SPECIFIED(900983, "Shared Scope name not specified", 400,
            "Shared Scope name not specified"),
    SCOPE_ALREADY_REGISTERED(900984, "Scope already exists", 409, "Scope %s already exists"),
    SHARED_SCOPE_ALREADY_ATTACHED(900985, "Shared Scope already attached", 409,
            "Shared Scope %s is already used by one or more APIs"),
    SCOPE_VALIDATION_FAILED(900986, "Scope validation failed", 412, "Scope validation failed"),
    SHARED_SCOPE_DISPLAY_NAME_NOT_SPECIFIED(900987, "Shared Scope display name not specified", 400,
            "Shared Scope display name not specified"),

    //Dedicated container based gateway related Codes
    NO_RESOURCE_LOADED_FROM_DEFINITION(900990, "Container based resource Not Found", 404, "No resource loaded from " +
            "definition provided"),
    LOADED_RESOURCE_DEFINITION_IS_NOT_VALID(900991, "Loaded resource is not valid", 400, "The loaded resource " +
            "definition is not a valid"),
    TEMPLATE_LOAD_EXCEPTION(900992, "Error in loading the template file by client as an InputStream", 500, " Error " +
            "in loading the FileInputStream by client"),
    CONTAINER_GATEWAY_REMOVAL_FAILED(900993, "Cannot complete removing dedicated container based Gateway", 404,
            "Error in deleting the dedicated container based Gateway"),
    ERROR_INITIALIZING_DEDICATED_CONTAINER_BASED_GATEWAY(900994, "Error initializing dedicated container based" +
            " gateway", 500, "Error initializing dedicated container based gateway"),
    DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED(900995, "Error while creating dedicated container based gateway", 500,
            "Error while creating dedicated container based gateway"),
    ERROR_WHILE_UPDATING_DEDICATED_CONTAINER_BASED_GATEWAY(900996, "Error while updating dedicated container based" +
            " gateway", 500, "Error while updating dedicated container based gateway"),
    ERROR_WHILE_RETRIEVING_DEDICATED_CONTAINER_BASED_GATEWAY(900997, "Error while retrieving dedicated container " +
            "based gateway", 500, "Error while retrieving dedicated container based gateway"),
    INVALID_DEDICATED_CONTAINER_BASED_GATEWAY_LABEL(900998, "Invalid gateway label is provided", 400,
            "Invalid gateway label is provided"),
    DEDICATED_GATEWAY_DETAILS_NOT_FOUND(900999, "Dedicated gateway details not found for the API", 404, "Dedicated " +
            "gateway details not found for the API"),

    //Comments related Codes
    NEED_COMMENT_MODERATOR_PERMISSION(901100, "Comment moderator permission needed", 403,
            "This user is not a comment moderator"),
    COULD_NOT_UPDATE_COMMENT(901101, "Error has occurred. Could not update the Comment", 500,
            "Error has occurred. Could not update the Comment"),
    COMMENT_NOT_FOUND(901102, "Comment not found", 404, "Couldn't retrieve comment"),
    COMMENT_LENGTH_EXCEEDED(901103, "Comment length exceeds max limit", 400, "Comment length exceeds allowed maximum "
            + "number of characters"),
    NEED_ADMIN_PERMISSION(901100, "Admin permission needed", 403,
            "This user is not an admin"),

        //External Stores related codes
    EXTERNAL_STORE_ID_NOT_FOUND(901200,"External Store Not Found", 404, "Error while publishing to external stores. " +
            "External Store Not Found"),


    // Tenant related
    INVALID_TENANT(901300,"Tenant Not Found", 400, "Tenant Not Found"),
    // Key Manager Related
    INVALID_KEY_MANAGER_TYPE(901400, "Key Manager Type not configured", 400, "Key Manager Type not configured"),
    REQUIRED_KEY_MANAGER_CONFIGURATION_MISSING(901401,"Required Key Manager configuration missing",400,"Missing " +
            "required configuration"),
    KEY_MANAGER_ALREADY_EXIST(901402, "Key Manager Already Exists", 409, "Key Manager Already Exists"),
    KEY_MANAGER_NOT_FOUND(901403, "Key Manager not found", 400, "Key Manager not found"),
    KEY_MANAGER_NAME_EMPTY(901404,
            "Key Manager name cannot be empty", 400,"Key Manager name cannot be empty"),
    KEY_MANAGER_NOT_SUPPORT_OAUTH_APP_CREATION(901405, "Key Manager doesn't support generating OAuth applications", 400,
            "Key Manager doesn't support generating OAuth applications"),
    KEY_MANAGER_NOT_SUPPORTED_TOKEN_GENERATION(901405, "Key Manager doesn't support token generation", 400,
            "Key Manager doesn't support token generation"),
    KEY_MANAGER_NOT_ENABLED(901406, "Key Manager is not enabled in the system", 400,
            "Key Manager is not enabled in the system"),
    KEY_MANAGER_MISSING_REQUIRED_PROPERTIES_IN_APPLICATION(901407, "Required application properties are missing", 400,
            "Required application properties are missing"),
    APPLICATION_ALREADY_REGISTERED(901408, "Application already Registered", 409, "Application already Registered"),
    KEY_MAPPING_ALREADY_EXISTS(901409, "Key Mappings already exists", 409, "Key Mappings already exists"),

    //Scope related
    SCOPE_NOT_FOUND_FOR_USER(901500, "Scope does not belong to this user", 404, "Scope not found"),
    SCOPE_NOT_FOUND(901501, "Scope Not Found", 404, "Scope does not exist"),
    USER_NOT_FOUND(901502, "User Not Found", 404, "User does not exist"),
    DEFINITION_EXCEPTION(901503, "Internal server error.", 500, " Error occurred while retrieving swagger definition"),

    //Analytics related codes
    ANALYTICS_NOT_ENABLED(901600, "%s not accessible", 404,
            "Analytics should be enabled to access %s"),

    // Password change related
    PASSWORD_CHANGE_DISABLED(901450, "Password change disabled", 400, "Password change operation is disabled in the system"),

    CURRENT_PASSWORD_INCORRECT(901451, "Current password incorrect", 400, "The current password entered is incorrect"),

    PASSWORD_PATTERN_INVALID(901452, "Password pattern invalid", 400, "Password entered is invalid since it doesn't comply with the pattern/policy configured"),

    //Tenant theme related codes
    TENANT_THEME_IMPORT_FAILED(901700, "Failed to import tenant theme of tenant %s", 500,
            "%s"),
    TENANT_THEME_EXPORT_FAILED(901701, "Failed to export tenant theme of tenant %s", 500,
            "%s"),
    TENANT_THEME_IMPORT_NOT_ALLOWED(901702, "Super Tenant not allowed to import tenant theme", 400,
            "Super Tenant %s is not allowed to import a tenant theme"),


    //API mediation policies related
    MEDIATION_POLICY_API_ALREADY_EXISTS(901800, "Mediation Policy Already Exists", 409,
                                            "A mediation policy with the given name is already attached to the API"),

    //mediation policies related common errors
    MEDIATION_POLICY_NAME_TOO_LONG(900850, "Mediation Policy Name Too Long", 400,
                                                "The name of the mediation policy exceeds the max length (%s)"),
    INVALID_ENDPOINT_CREDENTIALS(902000, "Invalid Endpoint Security credentials", 400,
            "Invalid Endpoint Security " + "credentials", false);

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
    ExceptionCodes(long errorCode, String msg, int httpErrorCode, String errorDescription, boolean stackTrace) {
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

    public boolean printStackTrace() {

        return stackTrace;
    }

    /**
     * Create an ErrorHandler instance with the provided ExceptionCode filled with some dynamic input values
     *
     * @param errorHandler ErrorHandler or ExceptionCode object
     * @param params dynamic values to be filled
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
