/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.importexport;

import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all the constants required for API Import and Export.
 */
public final class ImportExportConstants {

    // System independent file separator for zip files
    public static final char ZIP_FILE_SEPARATOR = '/';
    public static final char WIN_ZIP_FILE_SEPARATOR = '\\';

    //length of the name of the temporary directory
    public static final int TEMP_FILENAME_LENGTH = 5;

    // Location of the API definition file
    public static final String API_FILE_LOCATION = File.separator + "api";

    public static final String MCP_SERVER_FILE_LOCATION = File.separator + "mcp_server";

    // Location of the API definition file
    public static final String API_PRODUCT_FILE_LOCATION = File.separator + "api_product";

    // Location of the definitions such as swagger, graphql schema etc
    public static final String DEFINITIONS_DIRECTORY = "Definitions";

    // Location of the API swagger definition file
    public static final String SWAGGER_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "swagger";

    // Location of the AsyncAPI definition file
    public static final String ASYNCAPI_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "asyncapi";

    // Location of the OpenAPI (AsyncAPI) definition file
    public static final String OPENAPI_FOR_ASYNCAPI_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "swagger";

    // Location of the graphql schema definition file
    public static final String GRAPHQL_SCHEMA_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "schema.graphql";

    // Location of the graphql schema definition file
    public static final String GRAPHQL_COMPLEXITY_INFO_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "graphql-complexity";

    // Location of the wsdl file
    public static final String WSDL_LOCATION = File.separator + "WSDL" + File.separator;

    public static final String JSON_EXTENSION = ".json";
    public static final String YAML_EXTENSION = ".yaml";
    public static final String YML_EXTENSION = ".yml";

    // Image resource
    public static final String IMAGE_RESOURCE = "Image";

    // Custom type
    public static final String CUSTOM_TYPE = "Custom";

    public static final String DOCUMENT_FILE_NAME = File.separator + "document";

    public static final String DOCUMENT_DIRECTORY = "Docs";

    public static final String ENDPOINT_CERTIFICATES_DIRECTORY = "Endpoint-certificates";

    public static final String CLIENT_CERTIFICATES_DIRECTORY = "Client-certificates";

    public static final String ENDPOINTS_CERTIFICATE_FILE = File.separator + "endpoint_certificates";

    public static final String CLIENT_CERTIFICATE_FILE = File.separator + "client_certificates";
    public static final String ENDPOINTS_FILE = "endpoints";

    public static final String APIS_DIRECTORY = "APIs";

    public static final String CERTIFICATE_FILE = "certificate";

    public static final Map<String, String> fileExtensionMapping = new HashMap<>();

    public static final String UPLOAD_API_FILE_NAME = "APIArchive.zip";

    public static final String UPLOAD_POLICY_FILE_NAME = "PolicyArchive.zip";
    public static final String API_YAML_FILE_NAME = "api.yaml";
    public static final String SWAGGER_YAML_FILE_NAME = "Definitions/swagger.yaml";
    public static final String DEPLOYMENT_ENVIRONMENTS_FILE_NAME = "deployment_environments.yaml";

    // Location of the API swagger definition file
    public static final String JSON_SWAGGER_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "swagger.json";

    // Location of the API swagger definition file
    public static final String YAML_SWAGGER_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "swagger.yaml";

    // Location of the AsyncAPI definition file
    public static final String JSON_ASYNCAPI_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "asyncapi.json";

    // Location of the AsyncAPI definition file
    public static final String YAML_ASYNCAPI_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "asyncapi.yaml";

    // Name of the API provider element tag of the api.json file
    public static final String PROVIDER_ELEMENT = "provider";

    // Name of the API name element tag of the api.json file
    public static final String API_NAME_ELEMENT = "name";

    // Name of the API version element tag of the api.json file
    public static final String VERSION_ELEMENT = "version";

    public static final String WSDL_URL = "wsdlUrl";

    // Swagger definition version of the imported API
    public static final String OAS_VERSION_3 = "v3";

    // Location of the image
    public static final String IMAGE_FILE_LOCATION = File.separator + IMAGE_RESOURCE + File.separator;

    public static final String CHARSET = "UTF-8";

    public static final String FILE_DOC_TYPE = "FILE";

    // Sequences resource
    public static final String SEQUENCES_RESOURCE = "Sequences";

    // Sequence location post fix
    public static final String SEQUENCE_LOCATION_POSTFIX = "-sequence";

    // Location of the in sequence
    public static final String IN_SEQUENCE_PREFIX = "in";

    // Location of the out sequence
    public static final String OUT_SEQUENCE_PREFIX = "out";

    // Location of the fault sequence
    public static final String FAULT_SEQUENCE_PREFIX = "fault";

    // Location of the in sequence
    public static final String IN_SEQUENCE_LOCATION = File.separator + SEQUENCES_RESOURCE
            + File.separator + IN_SEQUENCE_PREFIX + SEQUENCE_LOCATION_POSTFIX + File.separator;

    // Location of the out sequence
    public static final String OUT_SEQUENCE_LOCATION = File.separator + SEQUENCES_RESOURCE
            + File.separator + OUT_SEQUENCE_PREFIX + SEQUENCE_LOCATION_POSTFIX + File.separator;

    // Location of the fault sequence
    public static final String FAULT_SEQUENCE_LOCATION = File.separator + SEQUENCES_RESOURCE
            + File.separator + FAULT_SEQUENCE_PREFIX + SEQUENCE_LOCATION_POSTFIX + File.separator;

    public static final String CERTIFICATE_CONTENT_JSON_KEY = "certificate";

    public static final String ALIAS_JSON_KEY = "alias";

    public static final String ENDPOINT_JSON_KEY = "endpoint";
    public static final String KEY_TYPE_JSON_KEY = "keyType";

    public static final int REFER_REQUIRE_RE_SUBSCRIPTION_CHECK_ITEM = 1;

    public static final String NODE_TRANSITION = "transition";

    // Since API Products currently don't have versioning support, every API Product will have this version
    public static final String DEFAULT_API_PRODUCT_VERSION = "1.0.0";

    static {
        fileExtensionMapping.put("image/png", "png");
        fileExtensionMapping.put("image/jpeg", "jpeg");
        fileExtensionMapping.put("image/jpg", "jpg");
        fileExtensionMapping.put("image/bmp", "bmp");
        fileExtensionMapping.put("image/gif", "gif");
        // To identify thumbnail icons
        fileExtensionMapping.put("application/json", "json");
    }

    public static final String TYPE_API = "api";

    public static final String TYPE_API_PRODUCT = "api_product";
    public static final String TYPE_MCP_SERVER = "mcp_server";

    public static final String TYPE_APPLICATION = "application";

    public static final String TYPE_DOCUMENTS = "document";

    public static final String TYPE_ENDPOINT_CERTIFICATES = "endpoint_certificates";

    public static final String TYPE_CLIENT_CERTIFICATES = "client_certificates";

    public static final String TYPE_DEPLOYMENT_ENVIRONMENTS = "deployment_environments";
    public static final String DEPLOYMENT_ENVIRONMENT_VERSION = "v4.3.0";

    public static final String TYPE_POLICY_SPECIFICATION = "operation_policy_specification";

    public static final String APIM_VERSION = "v4.6.0";

    public static final String ENDPOINT_CONFIG = "endpointConfig";

    public static final String GRAPHQL_COMPLEXITY = "graphql-complexity";

    public static final String UPLOAD_APPLICATION_FILE_NAME = "ApplicationArchive.zip";

    // Location of the Application YAML file
    public static final String YAML_APPLICATION_FILE_LOCATION = File.separator + "application.yaml";

    // Location of the Application JSON file
    public static final String JSON_APPLICATION_FILE_LOCATION = File.separator + "application.json";

    //Api controller Env Params related constants
    public static final String 
            INTERMEDIATE_PARAMS_FILE_LOCATION = File.separator + "intermediate_params";
    //Env param fields
    public static final String ENDPOINT_TYPE_FIELD = "endpointType";
    public static final String GATEWAY_ENVIRONMENTS_FIELD = "gatewayEnvironments";
    public static final String MUTUAL_SSL_CERTIFICATES_FIELD = "mutualSslCerts";
    public static final String ENDPOINT_CERTIFICATES_FIELD = "certs";
    public static final String ENDPOINT_SECURITY_FIELD = "security";
    public static final String POLICIES_FIELD = "policies";
    public static final String ROUTING_POLICY_FIELD = "endpointRoutingPolicy";
    public static final String ENDPOINTS_FIELD = "endpoints";
    public static final String LOAD_BALANCE_ENDPOINTS_FIELD = "loadBalanceEndpoints";
    public static final String FAILOVER_ENDPOINTS_FIELD = "failoverEndpoints";
    public static final String DEPENDENT_APIS_FIELD = "dependentAPIs";
    public static final String ADDITIONAL_PROPERTIES_FIELD = "additionalProperties";

    //Security config related constants
    public static final String ENDPOINT_NONE_SECURITY_TYPE = "NONE";

    //Default values for Endpoints
    public static final String ENDPOINT_URL = "url";
    public static final String DEFAULT_PRODUCTION_ENDPOINT_URL = "https://localhost";
    public static final String DEFAULT_SANDBOX_ENDPOINT_URL = "https://localhost";
    public static final String DEFAULT_DYNAMIC_ENDPOINT_URL = "default";
    public static final String DEFAULT_ALGORITHM_CLASS = "org.apache.synapse.endpoints.algorithms.RoundRobin";
    public static final String SOAP_ENDPOINT_TYPE_FOR_JSON = "address";

    //Endpoint types
    public static final String HTTP_TYPE_ENDPOINT = "http";
    public static final String REST_TYPE_ENDPOINT = "rest";
    public static final String SOAP_TYPE_ENDPOINT = "soap";
    public static final String DYNAMIC_TYPE_ENDPOINT = "dynamic";
    public static final String AWS_TYPE_ENDPOINT = "aws";
    public static final String AWS_LAMBDA_TYPE_ENDPOINT = "awslambda";
    public static final String LOAD_BALANCE_TYPE_ENDPOINT = "load_balance";
    public static final String FAILOVER_TYPE_ENDPOINT = "failOver";
    public static final String ENDPOINT_TYPE_PROPERTY = "endpoint_type";

    // AWS endpoint related constants
    public static final String AWS_LAMBDA_ENDPOINT_JSON_PROPERTY = "awsLambdaEndpoints";
    public static final String AWS_ACCESS_METHOD_JSON_PROPERTY = "accessMethod";
    public static final String AWS_ACCESS_METHOD_PROPERTY = "access_method";
    public static final String AWS_STORED_ACCESS_METHOD = "stored";
    public static final String AWS_ROLE_SUPPLIED_ACCESS_METHOD = "role-supplied";

    //REST/SOAP endpoint related constants
    public static final String FAILOVER_ROUTING_POLICY = "failover";
    public static final String LOAD_BALANCE_ROUTING_POLICY = "load_balanced";
    public static final String LOAD_BALANCE_ALGORITHM_CLASS_PROPERTY = "algoClassName";
    public static final String LOAD_BALANCE_SESSION_MANAGEMENT_PROPERTY = "sessionManagement";
    public static final String LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY = "sessionTimeOut";
    public static final String PRODUCTION_FAILOVERS_ENDPOINTS_PROPERTY = "production_failovers";
    public static final String SANDBOX_FAILOVERS_ENDPOINTS_PROPERTY = "sandbox_failovers";
    public static final String PRODUCTION_ENDPOINTS_PROPERTY = "production_endpoints";
    public static final String SANDBOX_ENDPOINTS_PROPERTY = "sandbox_endpoints";
    public static final String LOAD_BALANCE_SESSION_MANAGEMENT_TRANSPORT_TYPE = "transport";
    public static final String PRODUCTION_FAILOVERS_ENDPOINTS_JSON_PROPERTY = "productionFailovers";
    public static final String SANDBOX_FAILOVERS_ENDPOINTS_JSON_PROPERTY = "sandboxFailovers";
    public static final String PRODUCTION_ENDPOINTS_JSON_PROPERTY = "production";
    public static final String SANDBOX_ENDPOINTS_JSON_PROPERTY = "sandbox";

    //Certificate related constants
    public static final String MUTUAL_SSL_ENABLED = "mutualssl";
    public static final String CERTIFICATE_PATH_PROPERTY = "path";
    public static final String CERTIFICATE_HOST_NAME_PROPERTY = "hostName";
    public static final String CERTIFICATE_TIER_NAME_PROPERTY = "tierName";
    public static final String CERTIFICATE_DIRECTORY = File.separator + "certificates";
    public static final String ENDPOINT_CERTIFICATES_DIRECTORY_PATH = File.separator + "Endpoint-certificates";
    public static final String CLIENT_CERTIFICATES_DIRECTORY_PATH = File.separator + "Client-certificates";
    public static final String ENDPOINT_CERTIFICATES_META_DATA_FILE_PATH =
            ENDPOINT_CERTIFICATES_DIRECTORY_PATH + File.separator + "endpoint_certificates";
    public static final String PRODUCTION_CLIENT_CERTIFICATES_META_DATA_FILE_PATH =
            CLIENT_CERTIFICATES_DIRECTORY_PATH + File.separator + APIConstants.API_KEY_TYPE_PRODUCTION
                    + File.separator + "client_certificates";
    public static final String SANDBOX_CLIENT_CERTIFICATES_META_DATA_FILE_PATH =
            CLIENT_CERTIFICATES_DIRECTORY_PATH + File.separator + APIConstants.API_KEY_TYPE_SANDBOX
                    + File.separator + "client_certificates";

    //Deployment directory related constants
    public static final String DEPLOYMENT_DIRECTORY_NAME= "Deployment";
    public static final String DEPLOYMENT_DIRECTORY= File.separator + DEPLOYMENT_DIRECTORY_NAME;
    public static final String SOURCE_ZIP_DIRECTORY_NAME = "SourceArchive.zip";

    // Location of the deployments file
    public static final String DEPLOYMENT_INFO_LOCATION = File.separator + "deployment_environments";
    public static final String DEPLOYMENT_ENVIRONMENTS = "deploymentEnvironments";
    public static final String DEPLOYMENT_NAME = "deploymentEnvironment";
    public static final String DEPLOYMENT_VHOST = "deploymentVhost";
    public static final String DISPLAY_ON_DEVPORTAL_OPTION = "displayOnDevportal";

    public static final String POLICIES_DIRECTORY = "Policies";
    public static final String CUSTOM_BACKEND_DIRECTORY = "Sequence-Backend";
    public static final String SWAGGER_X_WSO2_APICTL_INIT = "x-wso2-apictl-init";

    public static final String EXPORT_POLICY_TYPE_YAML = "YAML";
    public static final String EXPORT_POLICY_TYPE_JSON = "JSON";

    public static final String POLICY_NAME = "name";

    public static final String POLICY_TYPE_API = "api";
    public static final String POLICY_TYPE_COMMON = "common";

    // API Endpoints related constants
    public static final String API_ENDPOINTS_TYPE = "endpoints";
    public static final String API_ENDPOINTS_FILE_LOCATION = File.separator + "endpoints";

    public static final String BACKENDS_TYPE = "backends";
    public static final String BACKENDS_FILE_LOCATION = File.separator + "backends";

    public static final String API_NAME_DELIMITER = "-";
    public static final String INITIATED_FROM_GATEWAY_CONSTANT = "initiatedFromGateway";
}
