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
public enum ExceptionCodes implements ErrorHandler {

    // API, Application related codes
    API_NAME_ALREADY_EXISTS(900250, "The API name already exists.", 409, "An API with name '%s' already exists"),
    API_CONTEXT_ALREADY_EXISTS(900251, "The API context already exists.", 409, "An API with context '%s' already exists"),
    API_VERSION_ALREADY_EXISTS(900252, "The API version already exists.", 409, "An API with version '%s' already exists for API '%s'"),

    API_PRODUCT_CONTEXT_ALREADY_EXISTS(900275, "The API Product context already exists.", 409, "An API Product with context '%s' already exists"),
    API_PRODUCT_VERSION_ALREADY_EXISTS(900276, "The API Product version already exists.", 409, "An API Product with version '%s' already exists for API Product '%s'"),

    API_CONTEXT_MALFORMED_EXCEPTION(900253, "The API context is malformed.", 400, "'%s'"),
    API_ALREADY_EXISTS(900300, "The API already exists.", 409, "The API already exists"),
    APPLICATION_ALREADY_EXISTS(900301, "The application already exists.", 409, "The application already exists"),
    APIMGT_DAO_EXCEPTION(900302, "Internal server error.", 500, "Error occurred while persisting/retrieving data"),
    APIMGT_LIFECYCLE_EXCEPTION(900303, "Lifecycle exception occurred", 500, "Error occurred while changing " +
            "lifecycle state"),
    TIER_CANNOT_BE_NULL(900304, "The tier cannot be null.", 400, "The tier cannot be null"),
    TIER_NAME_INVALID(900305, "The tier name is invalid.", 400, "The tier name is invalid"),
    APPLICATION_NOT_FOUND(900307, "Application not found", 404, "Application not found"),
    API_NOT_FOUND(900308, "API Not Found", 404, "Requested API with id '%s' not found"),
    APPLICATION_INACTIVE(900309, "Application is not active", 400, "Application is not active"),
    SUBSCRIPTION_NOT_FOUND(900310, "Subscription not found", 404,
            "The requested subscription with ID '%s' was not found."),
    UPDATE_STATE_CHANGE(900311, "API fields have state changes", 400, "Couldn't Update as API have changes can't be done"),
    DOCUMENT_ALREADY_EXISTS(900312, "Document already exists", 409, "Document already exists"),
    COULD_NOT_UPDATE_API(900313, "Error has occurred. Could not update the API", 500, "Error has occurred. Could not "
            + "update the API"),
    DOCUMENT_CONTENT_NOT_FOUND(900314, "Document content not found", 404, "Document content not found"),
    DOCUMENT_NOT_FOUND(900315, "Document not found", 404, "Document not found"),
    DOCUMENT_INVALID_SOURCE_TYPE(900319, "Invalid document source type", 500, "Source type of the document '%s' is invalid"),

    API_EXPORT_ERROR(900316, "API export Error", 500, "Error while exporting the given APIs"),
    API_IMPORT_ERROR(900317, "API import Error", 500, "Error while importing the given APIs"),
    SUBSCRIPTION_STATE_INVALID(900318, "Invalid state change for subscription", 400, "Invalid state change for " +
            "subscription"),
    APIM_DAO_EXCEPTION(900320, "Internal server error.", 500, "Error occurred while retrieving data"),
    GATEWAY_LABELS_CANNOT_BE_NULL(900321, "Gateway labels cannot be null.", 400, "Gateway labels cannot be null"),
    STATUS_CANNOT_BE_NULL(900322, "Status cannot be null.", 400, "Status cannot be null"),
    RATING_NOT_FOUND(900324, "Rating not found", 404, "Couldn't retrieve rating"),
    RATING_VALUE_INVALID(900325, "Rating value invalid", 400, "Provided rating value does not fall in between min max "
            + "values"),
    DOCUMENT_INVALID_VISIBILITY(900326, "Invalid document visibility type", 500, "Visibility type of the document '%s' is invalid"),
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
    API_PRODUCT_USED_RESOURCES(900344,
            "Cannot remove the resource paths because they are used by one or more API Products",
            409, "Cannot update API: %s:%s, due to the resources to remove are used by one or more API Products"),
    API_CATEGORY_INVALID(
            900345, "The API category is invalid.", 400, " The API category is invalid for API: %s"),
    INVALID_ADDITIONAL_PROPERTIES(900346, "Invalid additional properties", 400,
            "Invalid additional properties for API: %s:%s"),
    INVALID_CONTEXT(900346, "Invalid context provided", 400, "Invalid context provided for API: %s:%s"),
    INVALID_ENDPOINT_URL(900346, "Endpoint URL(s) is(are) not valid", 400, "Endpoint URL(s) is(are) not valid"),
    USER_ROLES_CANNOT_BE_NULL(900610, "Access control roles cannot be empty", 400, "Access control roles cannot be empty when visibility is restricted"),
    ORGS_CANNOT_BE_NULL(900610, "Access control organizatoins cannot be empty", 400, "Access control organizations cannot be empty when visibility is restricted"),
    API_REVISION_NOT_FOUND(900347, "API Revision Not Found", 404, "Requested API Revision with id %s not found"),
    EXISTING_API_REVISION_DEPLOYMENT_FOUND(900348, "Can not delete API Revision ", 400, "Couldn't delete API revision since API revision is currently deployed to a gateway. " +
            "You need to undeploy the API Revision from the gateway before attempting deleting API Revision: %s "),
    EXISTING_API_REVISION_FOUND(900349, "Can not create API Revision ", 400, "API revision already exists with id: %s "),
    API_REVISION_UUID_NOT_FOUND(900350, "Can not create API Revision ", 400, "Failed to retrieve revision uuid from revision registry artifact"),
    MAXIMUM_REVISIONS_REACHED(900351, "Can not create API Revision ", 400, "Maximum number of revisions per API has reached." +
            "Need to remove any revision to create a new Revision for API with API UUID: %s"),
    ERROR_CREATING_API_REVISION(900352, "Can not create API Revision ", 400, "Failed to create API revision registry artifacts: %s "),
    ERROR_DELETING_API_REVISION(900353, "Can not delete API Revision ", 400, "Failed to delete API revision registry artifacts: %s "),
    ERROR_RESTORING_API_REVISION(900354, "Can not restore API Revision ", 400, "Failed to restore API revision registry artifacts: %s "),
    DEPLOYMENT_ID_NOT_FOUND(900355, "Deployment Id Not Found", 400, "Deployment Id Not Found"),
    EXISTING_DEPLOYMENT_NOT_FOUND(900356, "Existing Deployment Not Found", 404, "Deployment with %s not found"),
    ORGANIZATION_NOT_FOUND(900357, "Organization Not Found", 400, "Organization is not found in the request"),
    INVALID_ENV_API_PROP_CONFIG(900358, "Invalid environment specific api property config", 400,
            "Environment specific api property config is not valid. %s", false),
    API_OR_API_PRODUCT_NOT_FOUND(900359, "API or API Product Not Found", 404, "Requested API or API Product with id '%s' not found"),
    API_PRODUCT_NOT_FOUND(900360, "API Product Not Found", 404, "Requested API Product with id '%s' not found"),
    SUB_ORGANIZATION_NOT_IDENTIFIED(900361, "User's Organization Not Identified", 403, "User's Organization is not identified"),
    CANNOT_CREATE_API_VERSION(900362, "New API Version cannot be created from a different provider", 409, "Initial provider of an API must be preserved in all versions of that API"),
    ERROR_WHILE_UPDATING_MANDATORY_PROPERTIES(903010, "Error while updating required properties", 400, "Error while updating required properties."),

    //Lifecycle related codes
    API_UPDATE_FORBIDDEN_PER_LC(900380, "Insufficient permission to update the API", 403,
            "Updating the API is restricted as as it is %s."),
    UNSUPPORTED_LIFECYCLE_ACTION(900381, "Unsupported state change action", 400, "Lifecycle state change action %s is not allowed"),
    LIFECYCLE_STATE_INFORMATION_NOT_FOUND(900382, "Lifecycle state information not found", 500,"Lifecycle state change information for %s with %s cannot be found"),

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
    TEMPLATE_EXCEPTION(900501, "Service configuration Error", 500, "Error generate service config"),
    GATEWAY_EXCEPTION(900502, "Gateway publishing Error", 500, "Error occurred while publishing to Gateway"),
    BROKER_EXCEPTION(900503, "Broker Connection Error", 500, "Error occurred while obtaining broker connection"),
    INVALID_GATEWAY_ENVIRONMENT(900504, "Invalid Gateway Environment", 400, "Gateway Environment with name '%s' not found"),
    NO_GATEWAY_ENVIRONMENTS_ADDED(900505, "No Gateway Environments Available", 400, "No gateway environments " +
            "available for the API : %s."),
    GATEWAY_ENVIRONMENT_NOT_FOUND(900506, "Gateway Environment not found", 404,
            "Gateway Environment with %s not found"),
    EXISTING_GATEWAY_ENVIRONMENT_FOUND(900507, "Gateway Environment already exists", 400,
            "A Gateway Environment with %s already exists"),
    READONLY_GATEWAY_ENVIRONMENT(900508, "Gateway Environment is read only", 400,
            "A Gateway Environment with %s is read only"),
    GATEWAY_ENVIRONMENT_DUPLICATE_VHOST_FOUND(900509, "Gateway Environment with duplicate virtual hosts",
            400, "A Gateway Environment cannot exists with duplicate virtual hosts"),
    READONLY_GATEWAY_ENVIRONMENT_NAME(900510, "Names of Gateway Environment cannot be changed",
            400, "Name of the gateway is read only"),
    GATEWAY_ENVIRONMENT_VHOST_NOT_PROVIDED(900511, "Gateway Environment virtual hosts name not provided",
            400, "Gateway Environment VHOST name not provided"),
    INVALID_VHOST(900512, "Invalid virtual host name provided",
            400, "Virtual host with provided vhost name does not exist"),
    FEDERATED_GATEWAY_VALIDATION_FAILED(900513, "API Validation Failed with Federated Gateway",
            400, "API Validation Failed with %s Gateway. %s", false),

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
    INVALID_USER_ROLES(900610, "Invalid user roles found", 400, "Invalid user roles found"),
    IDP_ADDING_FAILED(900611, "Unable to add the identity provider", 400, "Error while adding the identity provider"),
    IDP_RETRIEVAL_FAILED(900612, "Unable to retrieve the identity provider", 400, "Error while retrieving the "
            + "identity provider details"),
    IDP_DELETION_FAILED(900613, "Unable to delete the identity provider", 400, "Error while deleting the "
            + "identity provider"),
    INVALID_IDP_TYPE(900614, "Unsupported identity provider type", 400, "Invalid identity provider type. %s"),
    USERSTORE_INITIALIZATION_FAILED(900615, "Unable to get the user store manager", 500,
            "Error while getting the user store manager from the realm"),


    // Labels related codes
    LABEL_NAME_ALREADY_EXISTS(900650, "Label Name Already Exists", 409, "Label with name '%s' already exists", false),
    LABEL_NOT_FOUND(900651, "Label Not Found", 404, "Label not found for the given label ID: %s", false),
    LABEL_ADDING_FAILED(900652, "Failed To Create Label", 400, "Error occurred while trying to add label. %s", false),
    LABEL_UPDATE_FAILED(900653, "Failed To Update Label", 400, "Error occurred while trying to update label. %s", false),
    LABEL_CANNOT_DELETE_ASSOCIATED(900654, "Label Deletion Failed", 409, "The label cannot be deleted as it is associated with API(s).", false),
    LABEL_ATTACHMENT_FAILED(900655, "Label Attachment Failed", 400, "Error occurred while attaching label(s) to API. %s", false),
    LABEL_DETACHMENT_FAILED(900656, "Label Detachment Failed", 400, "Error occurred while detaching label(s) from API. %s", false),

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

    //AsyncApi related error codes
    ASYNCAPI_URL_MALFORMED(900756, "AsyncAPI specification retrieval from URL failed", 400, "Exception occurred while retrieving the AsyncAPI Specification from URL"),
    ASYNCAPI_URL_NO_200(900757, "AsyncAPI specification retrieval from URL failed", 400, "Response didn't return a 200 OK status"),

    ERROR_READING_ASYNCAPI_SPECIFICATION(900765, "AsyncAPI specification read error", 500, "Exception occurred while reading the AsyncAPI Specification file"),
    ERROR_RETRIEVE_KM_INFORMATION(900766, "Failed to retrieve key manager information", 500, "Couldn't get the key manager information by name or UUID"),

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
    INVALID_SORT_CRITERIA(900707, "Invalid sort criteria", 400, "Sort criteria contain a non-allowable value"),

    //GraphQL API related codes
    API_NOT_GRAPHQL(900870, "This API is not a GraphQL API", 400, "This API is not a GraphQL API"),
    GRAPHQL_SCHEMA_CANNOT_BE_NULL(900871, "GraphQL Schema cannot be empty or nul", 400,
            "GraphQL Schema cannot be empty or null"),
    UNSUPPORTED_GRAPHQL_FILE_EXTENSION(900872, "Unsupported GraphQL Schema File Extension", 400,
            "Unsupported extension. Only supported extensions are .graphql, .txt and .sdl"),
    INVALID_GRAPHQL_FILE(900873, "GraphQL filename cannot be null or invalid", 400,
            "GraphQL filename cannot be null or invalid"),
    GENERATE_GRAPHQL_SCHEMA_FROM_INTROSPECTION_ERROR(900874, "Error while generating GraphQL schema from introspection",
            400, "Error while generating GraphQL schema from introspection"),
    RETRIEVE_GRAPHQL_SCHEMA_FROM_URL_ERROR(900875, "Error while retrieving GraphQL schema from URL", 400,
            "Error while retrieving GraphQL schema from URL"),

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
    INVALID_PERMISSION(900915, "Invalid Permission", 403, " You are not authorized to access the resource."),
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
    SUBSCRIPTION_POLICY_UPDATE_TYPE_BAD_REQUEST(900974, "Bad Request", 400, "Subscription quota type can not be changed for AI Subscription policies."),
    INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE(903006, "%s", 500, "Server Error Occurred"),

    POLICY_LEVEL_NOT_SUPPORTED(900968, "Throttle Policy level invalid", 400, "Specified Throttle policy level is not "
            + "valid"),

    THROTTLING_POLICY_NOT_FOUND(903005, "Throttling Policy Not Found", 404,
            "Requested throttling policy with name '%s' and type '%s' not found"),
    INVALID_APPLICATION_ADDITIONAL_PROPERTIES(900970, "Invalid application additional properties", 400,
            "Invalid additional properties. %s"),
    JWT_PARSING_FAILED(900986, "Key Management Error", 500, "Error while parsing JWT. Invalid Jwt."),
    TOKEN_SCOPES_NOT_SET(
            900987, "The token information has not been correctly set internally", 400,
            "The token information has not been correctly set internally"),
    MUTUAL_SSL_NOT_SUPPORTED(
            900988, "Mutual SSL based authentication is not supported in this server", 400,
            "Cannot add client certificates to this server"),
    THROTTLING_POLICY_CANNOT_BE_NULL(900989,
            "Throttling Policy cannot be empty or null", 400, "Throttling Policy cannot be empty or null"),
    ALREADY_ASSIGNED_ADVANCED_POLICY_DELETE_ERROR(900971, "Cannot delete the advanced throttling policy", 403,
            "Cannot delete the advanced policy with the name %s because it is already assigned to an API/Resource"),

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
    BLOCK_CONDITION_RETRIEVE_PARAMS_EXCEPTION(900254, "Block conditions retrieval error", 400,
            "Provided query parameters are not valid"),
    BLOCK_CONDITION_RETRIEVE_FAILED(900255, "Failed to get Block conditions", 500,
            "Failed to retrieve Block conditions from the database"),
    INVALID_BLOCK_CONDITION_VALUES(900256, "Error while retrieving Block Conditions", 500,
            "Invalid format for condition values"),
    SCOPE_ALREADY_ASSIGNED(900988, "Scope already assigned locally by another API", 400,
            "Scope already assigned locally by another API"),

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
    
    // Organization related
    INVALID_ORGANINATION(901301,"Organization Not Found", 404, "Organization Not Found"),
    MISSING_ORGANINATION(901302,"Organization Not Found", 403, "User does not belong to any organization"),
    // Key Manager Related
    INVALID_KEY_MANAGER_TYPE(901400, "Key Manager Type not configured", 400, "Key Manager Type not configured"),
    REQUIRED_KEY_MANAGER_CONFIGURATION_MISSING(901401,"Required Key Manager configuration missing",400,"Missing " +
            "required configuration"),
    KEY_MANAGER_ALREADY_EXIST(901402, "Key Manager Already Exists", 409, "Key Manager Already Exists"),
    KEY_MANAGER_NOT_REGISTERED(901403, "Key Manager not Registered", 400, "Key Manager not Registered"),
    KEY_MANAGER_NOT_FOUND(901411, "Key Manager not Found", 404, "Key Manager not found"),
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
    KEY_MAPPING_ALREADY_EXIST(901409, "Key Mappings already exists", 409, "Key Mappings already exists"),
    TENANT_MISMATCH(901409,"Tenant mismatch", 400, "Tenant mismatch"),
    INVALID_APPLICATION_PROPERTIES(901410, "Invalid additional properties", 400,
            "Invalid additional properties given for application"),

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

    INVALID_API_IDENTIFIER(900851, "Provided API identifier (%s) is invalid", 400,
            "Provided API identifier (%s) is invalid"),
    API_NAME_OR_VERSION_NOT_NULL(900852, "name or version couldn't be null", 400, "name or version couldn't be null"),
    INVALID_CONFIGURATION_ID(900853,"The configuration id validation failed. Should be " +
            "{apiName}#{apiVersion}#{tenantDomain}",400,"The configuration id validation failed. Should be " +
            "{apiName}#{apiVersion}#{tenantDomain}"),
    INVALID_API_NAME(900854, "Invalid API Name",400 ,"Invalid API Name"),
    ALIAS_CANNOT_BE_EMPTY(900855, "The alias cannot be empty", 400, "The alias cannot be empty"),
    KEY_TYPE_CANNOT_BE_EMPTY(900856, "The key type cannot be empty", 400, "The key type cannot be empty"),
    // API import/export related codes
    ERROR_READING_META_DATA(900907, "Error while reading meta information from the definition", 400,
            "Error while reading meta information from the definition"),

    ERROR_READING_CUSTOM_SEQUENCE(900908, "Error while reading Custom Sequence from the API Endpoint Configuration",
            400, "Error while reading Custom Sequence from the API Endpoint Configuration"),
    ERROR_READING_PARAMS_FILE(900908, "Error while reading meta information from the params file", 400,
            "Error while reading meta information from the params file"),
    ERROR_FETCHING_DEFINITION_FILE(900909, "Cannot find the definition file of the project", 400,
            "Cannot find the yaml/json file with the project definition."),
    NO_API_ARTIFACT_FOUND(900910, "No Api artifacts found for given criteria", 404,
            "No Api artifacts found for given criteria"),
    ERROR_UPLOADING_THUMBNAIL(900914,
            "Error while updating thumbnail of API/API Product", 500,
            "Error while updating thumbnail of API/API Product: %s-%s"),
    APICTL_OPENAPI_PARSE_EXCEPTION(
            OPENAPI_PARSE_EXCEPTION.getErrorCode(), OPENAPI_PARSE_EXCEPTION.getErrorMessage(),
            OPENAPI_PARSE_EXCEPTION.getHttpStatusCode(), "%s"),
    GATEWAY_TYPE_NOT_FOUND(900903, "Gateway type not found", 404,
            "Gateway type not found available Gateway types : " + "%s"),

    SERVICE_IMPORT_FAILED_WITHOUT_OVERWRITE(900910, "Service import is failed" , 412, "Cannot update existing services " +
                                                    "when overwrite is false"),
    MISSING_PROTOCOL_IN_ASYNC_API_DEFINITION(900911, "Missing protocol in Async API Definition", 400,
            "Missing protocol in Async API Definition"),
    UNSUPPORTED_PROTOCOL_SPECIFIED_IN_ASYNC_API_DEFINITION(900912, "Unsupported protocol specified in Async API " +
               "Definition", 400, "Unsupported protocol specified in Async API Definition"),
    API_CREATION_NOT_SUPPORTED_FOR_ASYNC_TYPE_APIS(900915, "API Creation is supported only for WebSocket, WebSub and SSE APIs", 400,
            "API Creation is supported only for WebSocket, WebSub and SSE APIs"),
    LOGGING_API_NOT_FOUND(901400, "Requested Resource Not Found", 404, "Request API Not Found for context: %s"),
    LOGGING_API_INCORRECT_LOG_LEVEL(901401, "Bad Request", 400, "Log level should be either OFF, BASIC, STANDARD or FULL"),
    LOGGING_API_MISSING_DATA(901402, "Missing data", 400, "API context or log level is missing"),
    LOGGING_API_RESOURCE_NOT_FOUND(901403, "Requested Resource Not Found", 404, "Requested API Resource Not Found"),
    LOGGING_API_NOT_FOUND_IN_TENANT(901404, "Requested API Not Found", 404, "Requested API Not Found"),
    CORRELATION_CONFIG_BAD_REQUEST(902020, "Bad Request", 400, "Request body can not have empty elements"),
    CORRELATION_CONFIG_BAD_REQUEST_INVALID_NAME(902021, "Bad Request", 400, "Request body contains invalid correlation component name"),
    //Service Catalog related error codes
    SERVICE_VERSION_NOT_FOUND(901900, "Cannot find the service version", 404, "Cannot find a service that matches the given version"),
    INVALID_ENDPOINT_CREDENTIALS(902000, "Invalid Endpoint Security credentials", 400,
            "Invalid Endpoint Security credentials. %s", false),
    INVALID_TENANT_CONFIG(902001, "Invalid tenant-config found", 400, "Invalid tenant-config found with error %s", false),

    //Operation Policies related error codes
    INVALID_OPERATION_POLICY(902005, "Cannot find the selected api policy", 400,
            "Selected api policy is not found"),
    INVALID_OPERATION_POLICY_SPECIFICATION(902006, "Invalid api policy specification found", 400,
            "Invalid api policy specification. %s", false),

    MISSING_OPERATION_POLICY_PARAMETERS(902007, "Missing required parameters for policy specification", 400,
            "Required parameter(s) %s for policy specification %s are either missing or empty"),
    OPERATION_POLICY_NOT_ALLOWED_IN_THE_APPLIED_FLOW(902008, "API policy is not allowed in the applied flow", 400,
            "%s policy is not allowed in response flow"),
    MISSING_MANDATORY_POLICY_ATTRIBUTES(902009, "Missing mandatory api policy attribute", 400,
            "Required attributes(s) %s for api policy specification %s are either missing or empty"),
    OPERATION_POLICY_NOT_FOUND(902010, "API Policy Not Found", 404,
            "Requested api policy with id '%s' not found"),
    CUSTOM_BACKEND_NOT_FOUND(903250, "Sequence Backend not found",
            404, "Requested Sequence Backend of API '%s' not found"),

    OPERATION_POLICY_ALREADY_EXISTS(903001, "The API Policy already exists.", 409, "An Operation Policy with name '%s' and version '%s' already exists"),

    OPERATION_POLICY_NOT_FOUND_WITH_NAME_AND_VERSION(903004, "API Policy Not Found with given name and version", 404,
            "Requested api policy with name '%s' and version '%s not found"),

    OPERATION_POLICY_NOT_FOUND_WITH_NAME(903007, "API Policy Not Found with given name", 404,
            "Requested api policy with name '%s' not found"),

    OPERATION_POLICY_GATEWAY_ERROR(903008,
            "Either Synapse or Choreo Gateway Definition files or both should be present", 400,
            "API Policy cannot be imported due to the missing Gateway files."),
    ERROR_VALIDATING_API_POLICY(902011, "Error while validating API policies enforced for the API", 400,
            "Error while validating the API policies enforced for the API"),

    SUBSCRIPTION_TIER_NOT_ALLOWED(902002, "Subscription Tier is not allowed for user", 403, "Subscription Tier %s is" +
            " not allowed for user %s ", false),
    INVALID_KEY_MANAGER_REQUEST(902003, "Invalid Request sent to Key Manager.", 400, "Invalid Request sent to Key Manager.Error from Backend : %s", false),
    INTERNAL_SERVER_ERROR_FROM_KEY_MANAGER(902004, "Internal Server Error from Key Manager", 500, "Internal Server Error from Key Manager.Error from Backend : %s", true),
    REVISION_ALREADY_DEPLOYED(902005, "Revision deployment state conflicted", 409,
            "Revision deployment request conflicted with the current deployment state of the revision %s. Please try again later", false),
    INVALID_API_ID(902006, "Invalid API ID", 404, "The provided API ID is not found %s", false),
    INVALID_GATEWAY_TYPE(902007, "Invalid Gateway Type", 400, "Invalid Gateway Type. %s", false),
    INVALID_ENDPOINT_CONFIG(902012, "Endpoint config value(s) is(are) not valid", 400, "Endpoint config value(s) is(are) not valid"),
    ARTIFACT_SYNC_HTTP_REQUEST_FAILED(903009, "Error while retrieving from remote endpoint", 500, "Error while executing HTTP request to retrieve from remote endpoint"),
    KEY_MANAGER_RESTRICTED_FOR_USER(902013, "Unauthorized Access to Key Manager", 403, "Key Manager is Restricted for this user"),
    // Admin portal get apis and api provider change related errors
    CHANGE_API_PROVIDER_FAILED(903011, "Error while changing the API provider", 500, "Error while changing the API provider in the registry or DB"),
    GET_SEARCH_APIS_IN_ADMIN_FAILED(903012, "Error while getting the apis", 500, "Error while getting/searching the apis from registry"),
    KEY_MANAGER_DELETE_FAILED(902015, "Key Manager Delete error", 412,"Error while deleting the Key Manager. %s", false),
    KEYS_DELETE_FAILED(902014, "Key Delete error", 412,"Error while deleting Keys. %s", false),

    // AI service invocation related exceptions
    AI_SERVICE_INVALID_RESPONSE(903100, "Invalid response from AI service", 500, "Error while invoking AI service. %s", false),
    AI_SERVICE_INVALID_ACCESS_TOKEN(903101, "Invalid access token provided for AI service", 401, "Invalid access token provided for AI service"),
    AI_SERVICE_QUOTA_EXCEEDED(903102, "Quota exceeded for AI service", 429, "Quota exceeded for AI service"),
    DOCUMENT_NAME_ILLEGAL_CHARACTERS(902016, "Document name cannot contain illegal characters", 400, "Document name contains one or more illegal characters"),

    COMPLIANCE_VIOLATION_ERROR(903300, "Compliance violation error", 400, "Compliance violation error. %s", false),
    // Subscriptions related
    SUBSCRIPTION_ID_NOT_SPECIFIED(902017, "Subscription ID not specified.", 400,
            "Subscription ID not specified."),
    BUSINESS_PLAN_NOT_SPECIFIED(902018, "Business plan not specified.", 400,
            "Business plan not specified."),
    BUSINESS_PLAN_NOT_ALLOWED(902019, "The Business plan is not allowed.", 400,
            "Business plan '%s' is not allowed for the API.", false),
    INVALID_STATE_FOR_BUSINESS_PLAN_CHANGE(902022, "Cannot change the business plan of the subscription.",
            409, "Cannot change the business plan of the subscription with ID '%s' as the " +
            "subscription is in '%s' state.", false),
    NOT_ALLOWED_TIER_FOR_SUBSCRIBER(902023, "Cannot change the business plan of the subscription.",
            403, "Cannot change the business plan of the subscription with ID '%s' as the " +
            "subscriber does not have permission to access the specified business plan.", false),

    HTTP_METHOD_INVALID(903201,
            "Invalid HTTP method provided for API resource", 400,
            "The HTTP method '%s' provided for resource '%s' is invalid", false),

    OPERATION_TYPE_INVALID(903202, "Invalid operation type provided for API operation", 400,
            "The '%s' API operation type '%s' provided for operation '%s' is invalid", false),

    KEYMANAGERS_VALUE_NOT_ARRAY(903203, "KeyManagers value needs to be an array", 400,
            "Value of the KeyManagers config should be an array", false),

    SCOPE_ALREADY_ASSIGNED_FOR_DIFFERENT_API(903204, "Invalid scopes provided for API", 400,
            "Error while adding local scopes for API %s. Scope: %s already assigned locally for a different API.",
            false),

    UNSUPPORTED_TRANSPORT(903205, "Unsupported transport", 400,
            "Unsupported transport '%s' provided for the API", false),

    OAS_DEFINITION_VERSION_NOT_FOUND(903206, "Invalid OAS definition", 400,
            "Could not determine the OAS version as the version element of the definition is not found", false),

    API_NAME_PROVIDER_ORG_EMPTY(903207, "API name, provider or organization cannot be empty", 400,
            "API name, provider or organization cannot be empty. Provided values: name: %s, provider: %s, org: %s",
            false),

    ANONYMOUS_USER_NOT_PERMITTED(903208, "Anonymous user not permitted", 401,
            "Attempt to execute privileged operation as the anonymous user", false),

    GLOBAL_MEDIATION_POLICIES_NOT_FOUND(903209, "Global mediation policies not found", 404,
            "Global mediation policies not found", false),

    ENDPOINT_URL_NOT_PROVIDED(903210, "Endpoint url not provided", 400,
            "Url is not provided for the endpoint type %s in the endpoint config", false),

    OPERATION_POLICY_NAME_VERSION_INVALID(903211, "Invalid operation policy name or version", 400,
            "policyName and/or policyVersion provided for the applied policy %s_%s does not match the policy " +
            "specification identified by the given policyId %s",
            false),

    INVALID_OPERATION_POLICY_PARAMS(903212, "Invalid operation policy parameters", 400,
            "Invalid value provided for the operation policy parameter %s", false),

    INVALID_ENDPOINT_SECURITY_CONFIG(903213, "Invalid endpoint security configuration", 400,
            "Invalid values provided for %s endpoint security configuration", false),

    ENDPOINT_SECURITY_TYPE_NOT_DEFINED(903214, "Endpoint security type not defined", 400,
            "Endpoint security type not defined for the %s endpoint", false),

    ADDITIONAL_PROPERTIES_CANNOT_BE_NULL(903215, "'additionalProperties' is required and should " +
            "not be null", 400,
            "The field 'additionalProperties' is required and should not be null"),

    ADDITIONAL_PROPERTIES_PARSE_ERROR(903216, "Error while parsing 'additionalProperties'", 400,
            "Error while parsing 'additionalProperties'", true),

    ENDPOINT_SECURITY_CRYPTO_EXCEPTION(903217, "Error while encrypting the secret key of API", 500,
            "%s"),

    OPENAPI_RETRIEVAL_ERROR(903218, "Error while retrieving the OAS definition", 500,
            "Error while retrieving the OAS definition for API with UUID %s"),

    ASYNCAPI_RETRIEVAL_ERROR(903219, "Error while retrieving the Async API definition", 500,
            "Error while retrieving the Async API definition for API with UUID %s"),

    ERROR_RETRIEVING_API(903220, "Failed to get API", 500, "Failed to get API with UUID %s"),

    ERROR_CHANGING_REGISTRY_LIFECYCLE_STATE(903221, "Error changing registry lifecycle state", 500,
            "Error changing registry lifecycle state for API/API Product with UUID %s"),

    UN_AUTHORIZED_TO_VIEW_MODIFY_API(903222, "User is not authorized to view or modify the api",
            403, "User %s is not authorized to view or modify the api"),

    FAILED_PUBLISHING_API_NO_ENDPOINT_SELECTED(903223, "Failed to publish service to API store. No endpoint selected",
            400, "Failed to publish service to API store. No endpoint selected for API with UUID %s"),

    FAILED_PUBLISHING_API_NO_TIERS_SELECTED(903224, "Failed to publish service to API store. No Tiers selected",
            400, "Failed to publish service to API store. No Tiers selected for API with UUID %s"),

    THIRD_PARTY_API_REVISION_CREATION_UNSUPPORTED(903225, "Creating API Revisions is not supported " +
            "for third party APIs", 400,"Creating API Revisions is not supported for third party APIs: %s"),

    THIRD_PARTY_API_REVISION_DEPLOYMENT_UNSUPPORTED(903226, "Deploying API Revisions is not supported " +
            "for third party APIs", 400,"Deploying API Revisions is not supported for third party APIs: %s"),

    RETIRED_API_REVISION_DEPLOYMENT_UNSUPPORTED(903227, "Deploying API Revisions is not supported for retired APIs",
            400, "Deploying API Revisions is not supported for retired APIs. ApiId: %s"),

    REVISION_NOT_FOUND_FOR_REVISION_NUMBER(903228, "No revision found", 404,
            "No revision found for revision number %s"),

    ERROR_PROCESSING_DIRECTORY_TO_IMPORT(903229, "Error extracting and processing the directory", 500,
            "Error extracting and processing the directory to be imported", true),

    IMPORT_ERROR_INVALID_GRAPHQL_SCHEMA(903230, "Error occurred while importing the API. Invalid " +
            "GraphQL schema definition found", 400, "Invalid GraphQL schema definition " +
            "found. %s"),

    IMPORT_ERROR_INVALID_ASYNC_API_SCHEMA(903231, "Error occurred while importing the API. " +
            "Invalid AsyncAPI definition found.", 400, "Invalid AsyncAPI definition found. %s"),

    NO_VHOSTS_DEFINED_FOR_ENVIRONMENT(903232, "No VHosts defined for the environment", 400,
            "No VHosts defined for the environment: %s"),

    PROVIDED_GATEWAY_ENVIRONMENT_NOT_FOUND(903233, "Gateway environment not found", 400,
            "Provided gateway environment %s is not found"),

    UNSUPPORTED_AND_ALLOWED_LIFECYCLE_ACTIONS(903234, "Unsupported state change action", 400,
            "Lifecycle state change action %s is not allowed for this API. Allowed actions are %s"),

    NO_CORRESPONDING_RESOURCE_FOUND_IN_API(903235, "No corresponding resource found in API", 400,
            "API with id %s does not have a resource %s with http method %s"),

    ERROR_PARSING_MONETIZATION_PROPERTIES(903237, "Error when parsing monetization properties",
            400, "Error when parsing monetization properties"),

    API_NAME_CANNOT_BE_NULL(903238, "API name is required", 400,
            "API name is required and cannot be null"),

    API_NAME_ILLEGAL_CHARACTERS(903239, "API name contains illegal characters", 400,
            "API name %s contains one or more illegal characters from (%s)"),

    API_VERSION_CANNOT_BE_NULL(903240, "API version is required", 400,
            "API version is required and cannot be null"),

    API_VERSION_ILLEGAL_CHARACTERS(903241, "API version contains illegal characters", 400,
            "API version %s contains one or more illegal characters from (%s)"),

    UNSUPPORTED_CONTEXT(903242, "Unsupported context", 400,
            "Unsupported context %s"),

    ERROR_PARSING_ENDPOINT_CONFIG(903243, "Error when parsing endpoint configuration",
            400, "Error when parsing endpoint configuration"),

    NOT_IN_OPEN_API_FORMAT(903244, "Not in Open API format",
            400, "The API definition is not in Open API format"),

    PARAMETER_NOT_PROVIDED_FOR_DOCUMENTATION(903245, "Parameter value missing", 400,
            "Some of the mandatory parameter values were missing. %s"),

    INVALID_API_RESOURCES_FOR_API_PRODUCT(903246, "Cannot find API resources for some API Product " +
            "resources.", 404, "Some of the resources in the API Product are not found as API resources. %s"),

    INVALID_ADDITIONAL_PROPERTIES_WITH_ERROR(903247, "Invalid additional properties", 400,
            "Invalid additional properties for API: %s:%s Error: %s"),

    TIER_NAME_INVALID_WITH_TIER_INFO(903248, "The tier name is invalid.", 400,
            "The tier name(s) %s are invalid"),

    LENGTH_EXCEEDS_ERROR(903249, "Character length exceeds the allowable limit", 400, "%s"),

    ROLE_OF_SCOPE_DOES_NOT_EXIST(903250, "Role does not exist", 404,
            "Role %s does not exist"),

    OPERATION_OR_RESOURCE_TYPE_OR_METHOD_NOT_DEFINED(902031,
            "Operation type/http method is not specified for the operation/resource", 400,
            "Operation type/http method is not specified for the operation/resource: %s", false),

    FAILED_TO_RETRIEVE_WORKFLOW_BY_EXTERNAL_REFERENCE_ID(902033, "Failed to rettrieve workflow request by the " +
            "external workflow reference", 500,
            "Failed to retrieve workflow request by the external workflow reference"),
    FAILED_TO_RETRIEVE_WORKFLOWS(902034, "Error while retrieving workflow requests.", 500,
            "Error while retrieving workflow requests."),
    WORKFLOW_PAYLOAD_MISSING(902035, "Payload is missing", 400,
            "Payload is missing in the workflow request"),
    WORKFLOW_STATUS_NOT_DEFINED(902036, "Workflow status not defined", 400,
            "Workflow status is not defined"),
    RESOURCE_URI_TEMPLATE_NOT_DEFINED(902032, "Resource URI template value not defined", 400,
            "Resource URI template value (target) not defined", false),
    API_ENDPOINT_NOT_FOUND(902040, "Cannot find the required API Endpoint details.", 404,
            "Requested API endpoint with id '%s' not found."),
    ERROR_UPDATING_API_ENDPOINT_API(902041, "Error has occurred. Cannot update an API endpoint.", 500,
            "Error when updating the API Endpoint."),
    ERROR_INSERTING_API_ENDPOINT_API(902042, "Error has occurred. Fail to add an API endpoint to API.", 500,
            "Error has occurred while inserting an API endpoint."),
    ERROR_MISSING_ENDPOINT_CONFIG_OF_API_ENDPOINT_API(902043, "Missing mandatory API endpoint's endpoint config", 500,
            "Required attributes %s for an API endpoint config specification %s are either missing or empty"),
    ERROR_READING_API_ENDPOINTS_FILE(902044, "Error while reading API Endpoints from the endpoints file",
            400, "Error while reading API Endpoints from the endpoints file"),
    ERROR_ADDING_API_ENDPOINT(902045, "Error while adding API Endpoint to the API", 500,
            "Error while adding API Endpoint with ID: %s to the API"),
    ERROR_ADDING_API_ENDPOINTS(902046, "Error while adding API Endpoints to the API", 500,
            "Error while adding API Endpoints to the API");
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
