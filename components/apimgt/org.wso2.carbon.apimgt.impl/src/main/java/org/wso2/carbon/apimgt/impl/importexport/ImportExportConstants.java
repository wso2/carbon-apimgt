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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all the constants required for API Import and Export.
 */
public final class ImportExportConstants {

    // Location of the API definition file
    public static final String API_FILE_LOCATION = File.separator + "api";

    // Location of the definitions such as swagger, graphql schema etc
    public static final String DEFINITIONS_DIRECTORY = "Definitions";

    // Location of the API swagger definition file
    public static final String SWAGGER_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "swagger";

    // Location of the graphql schema definition file
    public static final String GRAPHQL_SCHEMA_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "schema.graphql";

    // Location of the wsdl file
    public static final String WSDL_LOCATION = File.separator + "WSDL" + File.separator;

    public static final String JSON_EXTENSION = ".json";
    public static final String YAML_EXTENSION = ".yaml";

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

    public static final String APIS_DIRECTORY = "APIs";

    public static final String CERTIFICATE_FILE = "certificate";

    public static final Map<String, String> fileExtensionMapping = new HashMap<>();

    public static final String UPLOAD_FILE_NAME = "APIArchive.zip";

    // Location of the API YAML file
    public static final String YAML_API_FILE_LOCATION = File.separator + "api.yaml";

    // Location of the API JSON file
    public static final String JSON_API_FILE_LOCATION = File.separator + "api.json";

    // Location of the API swagger definition file
    public static final String JSON_SWAGGER_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "swagger.json";

    // Location of the API swagger definition file
    public static final String YAML_SWAGGER_DEFINITION_LOCATION =
            File.separator + DEFINITIONS_DIRECTORY + File.separator + "swagger.yaml";

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

    // Location of the in sequence
    public static final String IN_SEQUENCE_LOCATION = File.separator + SEQUENCES_RESOURCE
            + File.separator + "in-sequence" + File.separator;

    // Location of the out sequence
    public static final String OUT_SEQUENCE_LOCATION = File.separator + SEQUENCES_RESOURCE
            + File.separator + "out-sequence" + File.separator;

    //Location of the fault sequence
    public static final String FAULT_SEQUENCE_LOCATION = File.separator + SEQUENCES_RESOURCE
            + File.separator + "fault-sequence" + File.separator;

    public static final String CERTIFICATE_CONTENT_JSON_KEY = "certificate";

    public static final String ALIAS_JSON_KEY = "alias";

    public static final String ENDPOINT_JSON_KEY = "endpoint";

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

    public static final String TYPE_DOCUMENTS = "document";

    public static final String TYPE_ENDPOINT_CERTIFICATES = "endpoint_certificates";

    public static final String TYPE_CLIENT_CERTIFICATES = "client_certificates";

    public static final String APIM_VERSION = "v4";
}
