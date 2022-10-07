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
    API_EMPTY_PASSWORD_FOR_SECURED_ENDPOINT(900253, "Empty password is given for endpointSecurity when creating API: %s",
            400, "An empty password is given for endpointSecurity when creating API: %s"),
    SEARCH_API_EXCEPTION(900254, "Internal server error.", 500, "Error occurred while searching APIs"),

    API_PRODUCT_CONTEXT_ALREADY_EXISTS(900275, "The API Product context already exists.", 409, "An API Product with context '%s' already exists"),

    API_ALREADY_EXISTS(900300, "The API already exists.", 409, "The API already exists"),
    API_NOT_FOUND(900301, "API Not Found", 404, "Requested API with id '%s' not found"),

    APIMGT_DAO_EXCEPTION(900302, "Internal server error.", 500, "Error occurred while persisting/retrieving data"),
    APIMGT_LIFECYCLE_EXCEPTION(900303, "Lifecycle exception occurred", 500, "Error occurred while changing " +
            "lifecycle state"),
    TIER_CANNOT_BE_NULL(900304, "The tier cannot be null.", 400, "The tier cannot be null"),
    TIER_NAME_INVALID(900305, "The tier name is invalid.", 400, "The tier name is invalid"),
    APPLICATION_NOT_FOUND(900306, "Application not found", 404, "Application not found"),
    APPLICATION_ALREADY_EXISTS(900307, "The application already exists.", 409, "The application already exists"),
    APPLICATION_DELETE_FAILED(900308, "Error has occurred. Could not delete the application", 500, "Error has occurred. Could not delete the application '%s'"),

    SUBSCRIPTION_NOT_ALLOWED(900309, "Subscription is not allowed for %s", 403, "%s"),
    SUBSCRIPTION_NOT_FOUND(900310, "Subscription not found", 404, "Couldn't retrieve Subscriptions for API"),
    UPDATE_STATE_CHANGE(900311, "API fields have state changes", 400, "Couldn't Update as API have changes can't be done"),
    DOCUMENT_ALREADY_EXISTS(900312, "Document already exists", 409, "Document already exists"),
    COULD_NOT_UPDATE_API(900313, "Error has occurred. Could not update the API", 500, "Error has occurred. Could not "
            + "update the API"),
    DOCUMENT_CONTENT_NOT_FOUND(900314, "Document content not found", 404, "Document content not found"),
    DOCUMENT_NOT_FOUND(900315, "Document not found", 404, "Document not found"),
    DOCUMENT_INVALID_SOURCE_TYPE(900319, "Invalid document source type", 500, "Source type of the document '%s' is invalid"),
    UNSUPPORTED_DOC_EXTENSION(900367, "Document file type not supported", 400, "Unsupported extension type of document file"),

    API_EXPORT_ERROR(900316, "API export Error", 500, "Error while exporting the given APIs"),
    API_IMPORT_ERROR(900317, "API import Error", 500, "Error while importing the given APIs"),
    API_PRODUCT_IMPORT_ERROR(900318, "API product import Error", 500,
            "Error while importing the given API Products"),
    SUBSCRIPTION_STATE_INVALID(900320, "Invalid state for subscription", 400, "Invalid state for " +
            "subscription"),
    API_RETRIEVE_EXCEPTION(900319, "Internal server error.", 500, "Error occurred while retrieving %s"),
    APPLICATION_RETRIEVE_EXCEPTION(900320, "Internal server error.", 500, "Error occurred while retrieving %s"),
    SUBSCRIPTION_RETRIEVE_EXCEPTION(900321, "Internal server error.", 500, "Error occurred while retrieving %s"),

    GATEWAY_LABELS_CANNOT_BE_NULL(900322, "Gateway labels cannot be null.", 400, "Gateway labels cannot be null"),
    ERROR_RETRIEVING_RATING(900323, "Cannot retrieve rating", 500, "Error while retrieving ratings for API '%s'"),
    RATING_NOT_FOUND(900324, "Rating not found", 404, "Couldn't retrieve rating for API '%s'"),
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
    API_DELETE_API_PRODUCT_USED_RESOURCES(900344, API_PRODUCT_USED_RESOURCES.getErrorMessage(), 409,
            "Cannot delete API since the resources to remove are used by one or more API Products"),
    API_CATEGORY_INVALID(
            900345, "The API category is invalid.", 400, " The API category is invalid for API: %s:%s"),
    INVALID_ADDITIONAL_PROPERTIES(900346, "Invalid additional properties", 400,
            "Invalid additional properties for API: %s:%s"),
    INVALID_CONTEXT(900346, "Invalid context provided", 400, "Invalid context provided for API: %s:%s"),
    INVALID_ENDPOINT_URL(900346, "Endpoint URL(s) is(are) not valid", 400, "Endpoint URL(s) is(are) not valid"),
    USER_ROLES_CANNOT_BE_NULL(900610, "User roles cannot be found", 400, "User roles cannot be found"),

    ERROR_RETRIEVING_REVISION_FOR_UUID(900410, "Failed to get revision details for revision UUID: %s",
            500, "Failed to get revision details"),

    ERROR_RETRIEVING_REVISION_DEPLOYMENT_MAPPING(900411,
            "Failed to get API Revision deployment mapping details for %s: %s", 500,
            "Failed to get API Revision deployment mapping details"),

    ERROR_UPDATING_REVISION_DEPLOYMENT_MAPPING(900412,
            "Failed to update Deployment Mapping entry for API UUID: ", 500,
            "Failed to update Deployment Mapping entry"),

    METHOD_NOT_ALLOWED(900420, "Not allowed", 405, "Method not allowed"),
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
    ERROR_RETRIEVING_CATEGORY(900362, "Cannot retrieve categories", 500, "Error while retrieving categories for organization '%s'"),
    PERSISTENCE_ERROR(900363, "Error occurred in registry transaction", 500, "'%s'"),
    NO_VIEW_UPDATE_PERMISSIONS(900365, "Insufficient permission to view or update the API", 403, "Insufficient permission to view or update the API"),
    API_DELETE_FAILED_SUBSCRIPTIONS(900366, "Failed to delete the API", 409, "Cannot remove the API as active subscriptions exist"),
    CATEGORY_NAME_CONTAINS_SPECIAL_CHARS(900368, "Name field contains special characters", 400, "API Category name contains special characters"),
    CATEGORY_NAME_TOO_LONG(900369, "API Category name is too long", 400, "API Category name exceeds 255 characters"),
    CATEGORY_ALREADY_EXISTS(900370, "API Category name already exists", 409, "Category with name '%s' already exists"),
    CATEGORY_NOT_FOUND(900371, "Category not found", 404, "No API Category with the given category ID '%s' exists"),
    CATEGORY_USED(900372, "Category has usages", 409, "Category is attached to one or more APIs"),
    ERROR_CHANGING_APP_OWNER(900373, "Failed to change the application owner", 500, "Error while changing the application owner"),

    USER_SUBSCRIPTION_EXISTS_CHECK_FAILED(900374, "Failed to check if user subscribed to API", 500, "Failed to check if user '%s' with an application '%s' is subscribed to API %s"),

    USER_ACCESSIBLE_APPLICATION_CHECK_FAILED(900375, "Failed to check if user subscribed to API", 500, "Error occurred while checking whether the application '%s' is accessible to user '%s'" ),
    API_TAGS_NOT_FOUND(900376, "API Tags Not Found", 404, "API Tags not found for organization '%s'"),

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
    MALFORMED_URL(900403, "Malformed URL", 400, "Invalid or Malformed URL"),
    URI_PARSE_ERROR(900405, "Error constructing the URI", 500, "'%s'"),
    INVALID_OPERATION_TYPE(900406, "Unsupported '%s' operation", 400, "The '%s' operation type '%s' is invalid"),
    VERB_NOT_FOUND(900407, "Missing '%s' type", 400, "Missing '%s type in URI templates"),
    YAML_PARSE_ERROR(900408, "Yaml parse error", 500, "Yaml parse error"),
    RESOURCE_NOT_FOUND_WITH_TYPE_AND_ID(900409, "Requested %s with Id %s not found", 404,
            "Requested %s with Id %s not found"),
    AUTHORIZATION_ERROR(900409, "Forbidden", 403, "You don't have permission to access the '%s' with Id '%s'"),
    FORBIDDEN_ERROR(900409, "Forbidden", 403, "You don't have permission to access this resource"),
    RESOURCE_NOT_FOUND_WITH_DESC(900401, "Resource not found", 404, "Requested '%s' with Id '%s' not found"),
    UNAUTHORIZED(900410, "Unauthorized", 401, "User is unauthorized"),

    // Endpoint related codes
    ENDPOINT_NOT_FOUND(900450, "Endpoint Not Found", 404, "Endpoint Not Found"),
    ENDPOINT_ALREADY_EXISTS(900451, "Endpoint already exists", 409, "Endpoint already exists"),
    ENDPOINT_ADD_FAILED(900452, "Endpoint adding failed", 400, "Endpoint adding failed"),
    ENDPOINT_DELETE_FAILED(900453, "Endpoint Delete Failed", 400, "Endpoint Delete Failed"),
    ENDPOINT_CRYPTO_ERROR(900454, "Error while encrypting/decrypting endpoint secrets", 500, "'%s'"),
    ENDPOINT_CONFIG_PARSE_FAILED(900455, "Endpoint config parsing failed", 500, "Error occurred while parsing endpoint config json"),


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

    FAILED_GET_ENVIRONMENT_LIST_OF_VHOST(900512, "Failed to get gateway environments list of VHost",
            500, "Failed to get gateway environments list of VHost"),
    FAILED_GET_ENVIRONMENT_LIST_OF_TENANT_DOMAIN(900513, "Failed to get Environments in tenant domain",
            500, "Failed to get Environments in tenant domain: %s"),
    FAILED_GET_ENVIRONMENT_FOR_TENANT_DOMAIN(900514, "Failed to get Environment in tenant domain: %s",
            500, "Failed to get Environment in tenant domain: %s"),
    TENANT_NOT_FOUND(900515, "Tenant does not exist", 404,
            "Tenant does not exist"),
    INVALID_TENANT_STATE(900516, "Invalid tenant state", 400,
            "Invalid tenant state provided"),
    FAILED_GET_ENVIRONMENT_SPECIFIC_PROPERTIES(900517,
            "Error occurred when getting environment specific api properties", 500,
            "Error occurred when getting environment specific api properties"),
    VHOST_FOR_ENVIRONMENT_NOT_FOUND(900518, "VHosts not found for the environment: %s", 404,
            "VHosts not found for the environment: %s"),

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
    ANON_USER_ACTION(900616, "Operation not permitted", 401, "Attempt to execute privileged operation as the anonymous user"),
    ROLE_ID_EMPTY(900617, "Role Id cannot be empty", 400,
            "Role Id cannot be empty"),

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
    API_NOT_SOAPTOREST(900687, "Provided API is not a SOAP to REST converted API", 400, "Provided API is not a SOAP to REST converted API"),
    ERROR_ADDING_WSDL_TO_API(900687, "Error while adding WSDL to API: %s", 500,
            "Error while saving WSDL"),


    //OpenAPI/Swagger related codes [900750 900???)
    MALFORMED_OPENAPI_DEFINITON(900758, "Malformed OpenAPI Definition", 400, "The provided OpenAPI definition is not parsable as a valid JSON or YAML."),
    UNRECOGNIZED_OPENAPI_DEFINITON(900759, "Unrecognized OpenAPI Definition", 400, "The definition is parsable but cannot be identified as an OpenAPI definition."),
    INVALID_SWAGGER_VERSION(900760, "Invalid Swagger Definition", 400, "Unsupported swagger version provided. Please add with swagger version 2.0."),
    INVALID_SWAGGER_PARAMS(900751, "Invalid Swagger Definition", 400, "Swagger contains invalid parameters. Please add valid swagger definition."),
    INVALID_OPENAPI_VERSION(900752, "Invalid OpenAPI Definition", 400, "Unsupported OpenAPI version provided. Please add with OpenAPI version 3.0.0."),
    INVALID_OPENAPI_NO_INFO_PATH(900753, "Invalid OpenAPI Definition", 400, "Required property 'info' or 'paths' are not provided."),
    OPENAPI_PARSE_EXCEPTION(900754, "Error while parsing OpenAPI definition", 400, "Error while parsing OpenAPI definition"),
    OPENAPI_PARSE_EXCEPTION_WITH_CUSTOM_MESSAGE(OPENAPI_PARSE_EXCEPTION.getErrorCode(), OPENAPI_PARSE_EXCEPTION.getErrorMessage(), OPENAPI_PARSE_EXCEPTION.getHttpStatusCode(), "'%s'"),
    OPENAPI_NOT_FOUND(900755, "OpenAPI definition not found", 404, "OpenAPI definition not found"),
    OPENAPI_URL_MALFORMED(900756, "OpenAPI definition retrieval from URL failed", 400, "Exception occurred while retrieving the OpenAPI definition from URL"),
    OPENAPI_URL_NO_200(900757, "OpenAPI definition retrieval from URL failed", 400, "Response didn't return a 200 OK status"),
    INVALID_OAS2_FOUND(900761, "Invalid OpenAPI V2 definition found", 400, "Invalid OpenAPI V2 definition found"),
    INVALID_OAS3_FOUND(900762, "Invalid OpenAPI V3 definition found", 400, "Invalid OpenAPI V3 definition found"),
    NO_RESOURCES_FOUND(900763, "No resources found", 404, "API must have at least one resource defined"),
    ERROR_REMOVING_EXAMPLES(900764, "Internal Error While Processing Swagger Definition", 500, "Couldn't remove one or more examples from the swagger definition"),
    MOCK_HTTP_METHOD_MISSING(900765, "Could not find HTTP methods", 400, "Cannot find the HTTP method for the API Resource Mediation Policy"),
    SWAGGER_ARCHIVE_MISSING(900766, "Could not find an archive in the given ZIP file", 500, "Could not find an archive in the given ZIP file"),

    //AsyncApi related error codes
    ASYNCAPI_URL_MALFORMED(900756, "AsyncAPI specification retrieval from URL failed", 400, "Exception occurred while retrieving the AsyncAPI Specification from URL"),
    ASYNCAPI_URL_NO_200(900757, "AsyncAPI specification retrieval from URL failed", 400, "Response didn't return a 200 OK status"),

    ERROR_READING_ASYNCAPI_SPECIFICATION(900765, "AsyncAPI specification read error", 500, "Exception occurred while reading the AsyncAPI Specification file"),

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
    INVALID_PARAMETERS_PROVIDED(900708, "Invalid parameter(s) provided", 400, "Bad Request. Mandatory parameters are invalid/missing"),

    INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE(900708, "'%s'", 400, "Bad Request. Parameters are invalid/missing"),

    //GraphQL API related codes
    API_NOT_GRAPHQL(900800, "This API is not a GraphQL API", 400, "This API is not a GraphQL API"),
    GRAPHQL_SCHEMA_CANNOT_BE_NULL(900801, "GraphQL Schema cannot be empty or nul", 400,
            "GraphQL Schema cannot be empty or null"),
    UNSUPPORTED_GRAPHQL_FILE_EXTENSION(900802, "Unsupported GraphQL Schema File Extension", 400,
            "Unsupported extension. Only supported extensions are .graphql, .txt and .sdl"),

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
    INVALID_PERMISSION(900915, "Invalid Permission", 403, " You are not authorized to access the '%s'."),
    OPENID_CONFIG(900916, "Missing OpenID configurations", 500, "Error in fetching Open ID configuration"),
    OAUTH2_APP_CREATION_FAILED(900950, "Key Management Error", 500, "Error while creating the consumer application."),
    OAUTH2_APP_ALREADY_EXISTS(900951, "Key Management Error", 409, "OAuth2 application already created."),
    OAUTH2_APP_DELETION_FAILED(900952, "Key Management Error", 500, "Error while deleting the consumer application."),
    OAUTH2_APP_UPDATE_FAILED(900953, "Key Management Error", 500, "Error while updating the consumer application."),
    OAUTH2_APP_RETRIEVAL_FAILED(900954, "Key Management Error", 500, "Error while retrieving the consumer application."
    ),
    APPLICATION_TOKEN_GENERATION_FAILED(900957, "Keymanagement Error", 500, " Error while generating the application" +
            "access token."),
    APPLICATION_CONSUMER_KEY_NOT_FOUND(900958, "Keymanagement Error", 403, "Requested consumer key with application '%s' not found"),
    UNSUPPORTED_THROTTLE_LIMIT_TYPE(900960, "Throttle Policy Error", 400, "Throttle Limit type is not supported"),
    POLICY_NOT_FOUND(900961, "Policy Not found", 404, "Failed to retrieve Policy Definition"),
    OAUTH2_APP_MAP_FAILED(900962, "Key Management Error", 500, "Error while mapping an existing consumer application."),
    TOKEN_INTROSPECTION_FAILED(900963, "Key Management Error", 500, "Error while introspecting the access token."),
    ACCESS_TOKEN_GENERATION_FAILED(900964, "Key Management Error", 500, "Error while generating a new access token."),
    INVALID_TOKEN_REQUEST(900965, "Key Management Error", 400, "Invalid access token request."),
    ACCESS_TOKEN_REVOKE_FAILED(900966, "Key Management Error", 500, "Error while revoking the '%s'."),

    INTERNAL_ERROR(900967, "General Error", 500, "Server Error Occurred"),
    INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE(903006, "%s", 500, "Server Error Occurred"),
    INTERNAL_ERROR_WITH_SPECIFIC_DESC(903007, "Internal Server Error", 500, "'%s'"),

    POLICY_LEVEL_NOT_SUPPORTED(900968, "Throttle Policy level invalid", 400, "Specified Throttle policy level is not "
            + "valid"),
    POLICY_LEVEL_EMPTY(900973, "Policy Level can not be empty", 400,
            "Throttle policy level can not be empty"),
    POLICY_LEVEL_NOT_FOUND(900974, "Policy Level %s not found", 404,
            "Throttle policy level %s not found"),
    UNSUPPORTED_POLICY_TYPE(901001, "Policy type error", 400, "Unsupported policy type"),
    UNSUPPORTED_TIER_TYPE(901002, "Policy tier error", 400, "Unsupported policy tier"),
    INVALID_THROTTLE_TIER(901003, "Invalid throttle tier", 400, "Invalid x-throttling tier"),
    THROTTLE_TIER_NOT_FOUND(901004, "Throttle policy tier not found", 404, "Policy tier not found"),

    THROTTLING_POLICY_NOT_FOUND(903005, "Throttling Policy Not Found", 404,
            "Requested throttling policy with name '%s' and type '%s' not found"),
    INVALID_APPLICATION_ADDITIONAL_PROPERTIES(900970, "Invalid application additional properties", 400,
            "Invalid additional properties. %s"),
    ERROR_RETRIEVE_APPLICATION_DETAILS(900972, "Error while obtaining details of the Application : %s",
            500, "Error while obtaining details of the Application : %s"),
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
    ADVANCED_POLICY_EXISTS(902900, "Advanced policy already exists", 409, "Advanced Policy with name '%s' already exists"),
    APPLICATION_POLICY_EXISTS(902901, "Application policy already exists", 409, "Application Policy with name '%s' already exists"),
    SUBSCRIPTION_POLICY_EXISTS(902902, "Subscription policy already exists", 409, "Subscription Policy with name '%s' already exists"),
    GLOBAL_POLICY_EXISTS(902903, "Policy already exists", 409, "Policy already exists"),
    ADVANCED_POLICY_ADD_FAILED(902904, "Error while adding an Advanced level policy: '%s'", 500, "'%s'"),
    ADVANCED_POLICY_GET_FAILED(902905, "Error while retrieving Advanced level policy : '%s'", 500, "'%s'"),
    ADVANCED_POLICY_UPDATE_FAILED(902906, "Error while updating Advanced level policy : '%s'", 500, "'%s'"),
    SUBSCRIPTION_POLICY_GET_ALL_FAILED(902907, "Error while retrieving Subscription level policies", 500, "Server Error Occurred"),
    SUBSCRIPTION_POLICY_ADD_FAILED(902908, "Error while adding Subscription level policies", 500, "Server Error Occurred"),
    SUBSCRIPTION_POLICY_GET_FAILED(902909, "Error while retrieving Subscription level policy : '%s'", 500, "Server Error Occurred"),
    BAD_POLICY_OBJECT(902010, "Policy object doesn't contain mandatory parameters", 500, "Policy object doesn't contain mandatory parameters."),
    SUBSCRIPTION_POLICY_UPDATE_FAILED(902911, "Error while updating Subscription level policy : '%s'", 500, "Server Error Occurred"),
    CUSTOM_RULE_EXISTS(902914, "Custom rule already exists", 409, "Custom rule with name %s already exists"),
    INVALID_IP_ADDRESS_FORMAT(902915, "Invalid IP address format", 400, "Invalid IP address format"),
    BLOCK_CONDITION_ALREADY_EXISTS(902916, "Block condition already exists", 409, "A block condition with type: %s, value: %s already exists"),
    ALREADY_ASSIGNED_APP_POLICY_DELETE_ERROR(902912, "Cannot delete the application throttling policy", 409, "Policy %s is already attached to an Application"),
    ALREADY_ASSIGNED_SUB_POLICY_DELETE_ERROR(902913, "Cannot delete the subscription throttling policy", 409, "Policy %s already has subscriptions"),

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
    SCOPE_ALREADY_ASSIGNED(900988, "Scope already assigned locally by another API", 400,
            "Scope already assigned locally by another API"),
    TOKEN_VALIDATION_FAILED(900989, "Validation failed for the given token", 400, "Validation failed for the given token"),
    ERROR_CHECKING_SCOPE_NAME(901000, "Error while checking scope name", 500,
            "Error occurred while checking scope name %s"),
    ERROR_CREATING_URI_FOR_SHARED_SCOPE(901004, "Error while creating shared scope URI", 500,
            "Error while creating URI for shared scope: %s"),
    FAILED_RETRIEVE_SHARED_SCOPE(901005, "Error while retrieving shared scope", 500,
            "Error while retrieving shared scope"),
    FAILED_CHECKING_SCOPE_KEY_AVAILABILITY(901006, "Failed to check scope key availability for: %s" ,
            500, "Error while checking scope key availability"),

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
    COMMENT_NOT_FOUND(901102, "Comment not found", 404, "Failed to retrieve comment"),
    COMMENT_LENGTH_EXCEEDED(901103, "Comment length exceeds max limit", 400, "Comment length exceeds allowed maximum "
            + "number of characters"),
    COMMENT_NO_PERMISSION(901104, "Insufficient permission", 403, "User '%s' doesn't have permission to access the comment with id '%s'"),
    COMMENT_CANNOT_RETRIEVE(901105, "Failed to get '%s'", 500, "Failed to get '%s"),

    COMMENT_CANNOT_DELETE(901106, "Failed to delete the Comment", 500, "Failed to delete the Comment of '%s'"),

    NEED_ADMIN_PERMISSION(901100, "Admin permission needed", 403,
            "This user is not an admin"),

        //External Stores related codes
    EXTERNAL_STORE_ID_NOT_FOUND(901200,"External Store Not Found", 404, "Error while publishing to external stores. " +
            "External Store Not Found"),
    EXTERNAL_STORE_CLASS_NOT_FOUND(901201,
            ExceptionConstants.EXTERNAL_STORE_ERROR_MSG, 404,
            "One or more classes defined in APIConstants.EXTERNAL_API_STORE_CLASS_NAME cannot be found"),
    EXTERNAL_STORE_CLASS_NOT_LOADED(901202,
            ExceptionConstants.EXTERNAL_STORE_ERROR_MSG, 500,
            "One or more classes defined in APIConstants.EXTERNAL_API_STORE_CLASS_NAME cannot be loaded"),
    EXTERNAL_STORE_CLASS_NOT_ACCESSIBLE(901203,
            ExceptionConstants.EXTERNAL_STORE_ERROR_MSG, 500,
            "One or more classes defined in APIConstants.EXTERNAL_API_STORE_CLASS_NAME cannot be accessed"),
    ERROR_RETRIEVE_EXTERNAL_STORE_CONFIG(901204, "External Store Config Retrieve Error", 500,
            "Error while retrieving External Stores Configuration from registry"),
    MALFORMED_XML_IN_EXTERNAL_STORE_CONFIG(901205, "Malformed XML in External Stores Configuration",
            500, "Malformed XML found in the External Stores Configuration resource"),

    // Tenant related
    INVALID_TENANT(901300,"Tenant Not Found", 404, "Tenant Not Found"),
    CONFIG_NOT_FOUND(901301, "Config not found", 404, "Config not found in tenant-config"),
    ERROR_GETTING_CUSTOM_URLS(901302, "Failed to get custom url info", 500, "Error while retrieving custom url info for tenant : '%s'"),
    // Key Manager Related
    INVALID_KEY_MANAGERS_VALUE(901350, "Key Managers value need to be an array", 400,
            "Invalid Key Managers value"),
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
    TENANT_MISMATCH(901410,"Tenant mismatch", 400, "Tenant mismatch"),
    INVALID_APPLICATION_PROPERTIES(901411, "Invalid additional properties", 400,
            "Invalid additional properties given for application"),
    DECRYPT_CONFIG_ERROR(901412, "Error while decrypting key manager configuration", 500, "Unable to decrypt the value"),

    //Scope related
    SCOPE_NOT_FOUND_FOR_USER(901500, "Scope does not belong to this user", 404, "Scope not found"),
    SCOPE_NOT_FOUND(901501, "Scope Not Found", 404, "Scope does not exist"),
    USER_NOT_FOUND(901502, "User Not Found", 404, "User does not exist"),
    DEFINITION_EXCEPTION(901503, "Internal server error.", 500, " Error occurred while retrieving swagger definition"),

    //Analytics related codes
    ANALYTICS_NOT_ENABLED(901600, "%s not accessible", 404,
            "Analytics should be enabled to access %s"),
    UNSUPPORTED_ALERT_TYPE(901601, "Unsupported alert type", 400, "Unsupported alert type: '%s' is provided"),
    MALFORMED_SP_URL(901602, "Malformed URL", 500, "Error while parsing the stream processor url"),
    ERROR_INVOKING_SP_REST_API(901603, "Error while invoking steam processor REST API", 500, "'%s'"),
    ALREADY_SUBSCRIBED_FOR_BOT_ALERTS(901604, "Subscription already exists", 409, "Email: '%s' has already been subscribed for bot detection alerts"),
    BOT_DETECTION_SUBSCRIPTION_NOT_FOUND(901605, "Subscription does not exist", 404, "Bot detection alert subscription with uuid: '%s' uuid does not exist"),

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
    TENANT_THEME_NOT_FOUND(901703, "Tenant theme does not exist", 404, "Tenant theme for tenant: '%s' does not exist"),

    INVALID_API_IDENTIFIER(900851, "Provided API identifier (%s) is invalid", 400,
            "Provided API identifier (%s) is invalid"),
    API_NAME_OR_VERSION_NOT_NULL(900852, "name or version couldn't be null", 400, "name or version couldn't be null"),
    INVALID_CONFIGURATION_ID(900853,"The configuration id validation failed. Should be " +
            "{apiName}#{apiVersion}#{tenantDomain}",400,"The configuration id validation failed. Should be " +
            "{apiName}#{apiVersion}#{tenantDomain}"),
    INVALID_API_NAME(900854, "Invalid API Name",400 ,"Invalid API Name"),
    ALIAS_CANNOT_BE_EMPTY(900855, "The alias cannot be empty", 400, "The alias cannot be empty"),

    // API import/export related codes
    ERROR_READING_META_DATA(900907, "Error while reading meta information from the definition", 400,
            "Error while reading meta information from the definition"),
    ERROR_READING_PARAMS_FILE(900908, "Error while reading meta information from the params file", 400,
            "%s"),
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

    //Service Catalog related error codes
    SERVICE_VERSION_NOT_FOUND(901900, "Cannot find the service version", 404, "Cannot find a service that matches the given version"),
    ERROR_RETRIEVE_SERVICE_INFORMATION(901901, "Error while getting service information",
            500, "Error while executing SQL for getting service information"),

    INVALID_ENDPOINT_CREDENTIALS(902000, "Invalid Endpoint Security credentials", 400,
            "Invalid Endpoint Security credentials. %s", false),
    INVALID_TENANT_CONFIG(902001, "Invalid tenant-config found", 400, "Invalid tenant-config found with error %s", false),

    //Operation Policies related error codes
    INVALID_OPERATION_POLICY(902005, "%s. Cannot find the selected operation policy", 400,
            "Selected operation policy is not found"),
    INVALID_OPERATION_POLICY_SPECIFICATION(902006, "Invalid operation policy specification found", 400,
            "Invalid operation policy specification. %s", false),

    INVALID_OPERATION_POLICY_PARAMETERS(902007, "Missing required parameters for operation policy specification", 400,
            "Required parameter(s) %s for operation policy specification %s are either missing or empty"),
    OPERATION_POLICY_NOT_ALLOWED_IN_THE_APPLIED_FLOW(902008, "Operation policy is not allowed in the applied flow", 400,
            "%s policy is not allowed in response flow"),
    MISSING_MANDATORY_POLICY_ATTRIBUTES(902009, "Missing mandatory operation policy attribute", 400,
            "Required attributes(s) %s for operation policy specification %s are either missing or empty"),
    OPERATION_POLICY_NOT_FOUND(902010, "Operation Policy Not Found", 404,
            "Requested operation policy with id '%s' not found"),
    OPERATION_POLICY_SPEC_MISMATCH(902011, "Applied policy does not match specification", 400, "Applied policy for URI template does not match specification"),

    OPERATION_POLICY_ALREADY_EXISTS(903001, "The Operation Policy already exists.", 409, "An Operation Policy with name '%s' and version '%s' already exists"),

    OPERATION_POLICY_NOT_FOUND_WITH_NAME_AND_VERSION(903004, "Operation Policy Not Found with given name and version", 404,
            "Requested operation policy with name '%s' and version '%s' not found"),

    OPERATION_POLICY_GATEWAY_ERROR(903008,
            "Either Synapse or Choreo Gateway Definition files or both should be present", 400,
            "Operation Policy cannot be imported due to the missing Gateway files."),
    OPERATION_POLICY_USAGE_EXISTS(903009, "Operation policy usages exist", 500, "Policy usages exist for policy ID '%s'"),

    SUBSCRIPTION_TIER_NOT_ALLOWED(902002, "Subscription Tier is not allowed for user", 403, "Subscription Tier %s is" +
            " not allowed for user %s ", false),
    INVALID_KEY_MANAGER_REQUEST(902003, "Invalid Request sent to Key Manager.", 400, "Invalid Request sent to Key Manager.Error from Backend : %s", false),
    INTERNAL_SERVER_ERROR_FROM_KEY_MANAGER(902004, "Internal Server Error from Key Manager", 500, "Internal Server Error from Key Manager.Error from Backend : %s", true),
    REVISION_ALREADY_DEPLOYED(902005, "Revision deployment state conflicted", 409,
            "Revision deployment request conflicted with the current deployment state of the revision %s. Please try again later", false),
    INVALID_API_ID(902006, "Invalid API ID", 404,
            "The provided API ID is not found %s", false),
    GATEWAY_DOMAIN_MAPPING_RETRIEVE_ERROR(902100, "Error retrieving gateway domain mappings from registry",
            500, "Error while retrieving gateway domain mappings from registry"),
    INVALID_GATEWAY_DOMAIN_MAPPING_JSON(902101, "Invalid JSON in gateway tenant domain mappings",
            500, "Invalid JSON found in the gateway tenant domain mappings"),
    MALFORMED_GATEWAY_DOMAIN_MAPPING_JSON(902102, "Malformed JSON in gateway tenant domain mappings",
            500, "Malformed JSON found in the gateway tenant domain mappings"),
    ERROR_PARSING_TENANT_CONFIG_JSON(902103, "Error occurred while converting to json",
            500, "Error occurred while converting tenantConfig to json"),
    FAILED_RETRIEVE_CONFIGURATION_FOR_ORGANIZATION(902150, "Failed to retrieve configuration",
            500, "Failed to retrieve %s Configuration for org: %s"),
    INVALID_QUERY(902200, "Failed to retrieve configuration",
            500, "Failed to retrieve %s Configuration for org: %s"),
    ERROR_RETRIEVING_CLAIM_VALUES(902300, "Error while retrieving claim values from user store",
            500, "Error while retrieving claim values from user store"),
    FAILED_FIND_API_USAGE(902350, "Failed to find API Usage for : %s", 500,
            "Failed to find API Usage for : %s"),
    BAD_REQUEST_SUBSCRIPTION_ID(902351, "Invalid Subscription ID", 400,
            "Invalid Subscription ID"),
    FAILED_GET_SUBSCRIPTION_POLICY(902352, "Failed to get subscription policy: %s", 500,
            "Failed to retrieve subscription policy: %s data"),
    FAILED_GET_API_POLICY(902353, "Failed to get API policy: %s", 500,
            "Failed to retrieve API policy: %s data"),
    FAILED_GET_APPLICATION_POLICY(902354, "Failed to get application policy: %s", 500,
            "Failed to retrieve application policy: %s data"),

    READ_ONLY_ENVIRONMENT_NOT_FOUND(902400, "Configured read only environment not found: %s",
            404, "Configured read only environment not found: %s"),

    // monetization related codes

    INVALID_API_STATE_MONETIZATION(904300, "Invalid API state", 400, "Invalid API state to configure monetization"),
    MONETIZATION_STATE_CHANGE_FAILED(904301, "Could not change the monetization state", 500, "Monetization state change to '%s' failed"),

    MONETIZATION_IMPLEMENTATION_LOADING_FAILED(904302, "Could not load the monetization implementation", 500, "Failed to load the monetization implementation"),

    // audit related codes

    AUDIT_SEND_FAILED(904200, "Error sending audit data", 500, "Sending audit data failed. Response code: '%s'"),
    AUDIT_RETRIEVE_FAILED(904201, "Error retrieving audit data", 500, "Error retrieving audit data"),

    // transport related codes
    UNSUPPORTED_TRANSPORT_TYPE(904100, "Unsupported transport type", 400, "Transport type '%s' is not supported"),

    // certificate related error codes

    CERT_NOT_FOUND(904001, "Could not find the certificate", 404, "'Cannot find the certificate with alias '%s' in the truststore'"),
    CERT_BAD_REQUEST(904002, "Bad Request", 400, "'%s"),
    GET_CERT_CONTENT(904003, "Error getting the certificate content", 500, "'%s'"),
    RETRIEVE_CERT(904004, "Could not retrieve the certificate", 500, "'%s"),
    DELETE_CERT(904005, "Could not delete the certificate", 500, "Error while deleting the certificate for alias '%s'"),
    GET_CERT_INFO(904006, "Could not get the certificate information", 500, "'%s"),
    UPDATE_CERT(904007, "Could not update the certificate", 500, "'%s'"),
    ENCODE_CERT(904008, "Error occurred while encoding the certificate", 500, "'%s"),
    INTERNAL_SERVER_CERT(904009, "Internal server error", 500, "'%s'"),
    EXPIRED_CERT(904010, "Certificate expired", 400, "'%s'"),
    CERT_ALREADY_EXIST(904011, "Certificate alias already exists", 409, "The alias '%s' already exists in the truststore"),
    DECODE_CERT(904012, "Error occurred while decoding the certificate", 500, "'%s'"),

    INVALID_KEY_TYPE(904013, "Bad Request", 400, "Invalid keyType. KeyType should be either PRODUCTION or SANDBOX"),

    ERROR_DELETING_APPLICATION_REGISTRATION(904014, "Can not delete application registration", 400, "Failed to delete Application registration of : '%s'"),

    ERROR_DELETING_APPLICATION_KEY_MAPPING(904015, "Can not delete application key mapping", 500, "Failed to delete Application key mapping  of : '%s'"),

    ERROR_RETRIEVE_APPLICATION_KEYS(904016, "Failed to retrieve application keys", 500, "Failed to retrieve application keys for '%s' ");

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
