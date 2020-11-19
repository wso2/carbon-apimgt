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

    //location of the api definition file
    public static final String API_FILE_LOCATION = File.separator + "Meta-information" + File.separator + "api";

    //location of the api swagger definition file
    public static final String SWAGGER_DEFINITION_LOCATION = File.separator
            + APIImportExportConstants.META_INFO_DIRECTORY + File.separator + "swagger";
    //location of the graphql schema definition file
    public static final String GRAPHQL_SCHEMA_DEFINITION_LOCATION = File.separator
            + APIImportExportConstants.META_INFO_DIRECTORY + File.separator + "schema.graphql";
    public static final String JSON_EXTENSION = ".json";
    public static final String YAML_EXTENSION = ".yaml";

    //Image resource
    public static final String IMAGE_RESOURCE = "Image";
    //Custom type
    public static final String CUSTOM_TYPE = "Custom";
    public static final String DOCUMENT_FILE_NAME = File.separator + "document";

    public static final String DOCUMENT_DIRECTORY = "Docs";

    public static final String META_INFO_DIRECTORY = "Meta-information";

    public static final String ENDPOINT_CERTIFICATES_DIRECTORY = "Endpoint-certificates";

    public static final String CLIENT_CERTIFICATES_DIRECTORY = "Client-certificates";

    public static final String ENDPOINTS_CERTIFICATE_FILE = File.separator + "endpoint_certificates";

    public static final String CLIENT_CERTIFICATE_FILE = File.separator + "client_certificates";

    public static final String CERTIFICATE_FILE = "certificate";

    public static final Map<String, String> fileExtensionMapping = new HashMap<>();

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

    public static final String TYPE_DOCUMENTS = "document";

    public static final String TYPE_ENDPOINT_CERTIFICATES = "endpoint_certificates";

    public static final String TYPE_CLIENT_CERTIFICATES = "client_certificates";

    public static final String APIM_VERSION = "v4";
}
