/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public final class APIImportExportConstants {

    //System independent file separator for zip files
    public static final char ZIP_FILE_SEPARATOR = '/';
    //length of the name of the temporary directory
    public static final int TEMP_FILENAME_LENGTH = 5;
    //name of the uploaded zip file
    public static final String UPLOAD_FILE_NAME = "APIArchive.zip";
    //location of the api YAML file
    public static final String YAML_API_FILE_LOCATION = File.separator + "Meta-information" + File.separator +
            "api.yaml";
    //location of the api YAML file
    public static final String JSON_API_FILE_LOCATION = File.separator + "Meta-information" + File.separator +
            "api.json";
    //name of the id element tag of the api.json file
    public static final String ID_ELEMENT = "id";
    //name of the api provider element tag of the api.json file
    public static final String PROVIDER_ELEMENT = "providerName";
    //name of the api name element tag of the api.json file
    public static final String API_NAME_ELEMENT = "apiName";
    //name of the api version element tag of the api.json file
    public static final String VERSION_ELEMENT = "version";

    //name of the api product name element tag of the api.json file
    public static final String API_PRODUCT_NAME_ELEMENT = "apiProductName";

    //location of the api swagger definition file
    public static final String JSON_SWAGGER_DEFINITION_LOCATION = File.separator
            + APIImportExportConstants.META_INFO_DIRECTORY + File.separator + "swagger.json";
    //location of the api swagger definition file
    public static final String YAML_SWAGGER_DEFINITION_LOCATION = File.separator
            + APIImportExportConstants.META_INFO_DIRECTORY + File.separator + "swagger.yaml";
    //location of the graphql schema definition file
    public static final String GRAPHQL_SCHEMA_DEFINITION_LOCATION = File.separator
            + APIImportExportConstants.META_INFO_DIRECTORY + File.separator + "schema.graphql";

    public static final int REFER_REQUIRE_RE_SUBSCRIPTION_CHECK_ITEM = 1;

    //Image resource
    public static final String IMAGE_RESOURCE = "Image";
    //Sequences resource
    public static final String SEQUENCES_RESOURCE = "Sequences";
    //Custom type
    public static final String CUSTOM_TYPE = "Custom";
    //location of the image
    public static final String IMAGE_FILE_LOCATION = File.separator + IMAGE_RESOURCE + File.separator;
    //location of the documents JSON file
    public static final String JSON_DOCUMENT_FILE_LOCATION = File.separator + APIImportExportConstants.DOCUMENT_DIRECTORY
            + File.separator + "docs.json";
    //location of the documents YAML file
    public static final String YAML_DOCUMENT_FILE_LOCATION = File.separator + APIImportExportConstants.DOCUMENT_DIRECTORY
            + File.separator + "docs.yaml";
    //name of the physical file type
    public static final String FILE_DOC_TYPE = "FILE";
    //location of the in sequence
    public static final String IN_SEQUENCE_LOCATION = File.separator + APIImportExportConstants.SEQUENCES_RESOURCE
            + File.separator + "in-sequence" + File.separator;
    //location of the out sequence
    public static final String OUT_SEQUENCE_LOCATION = File.separator + APIImportExportConstants.SEQUENCES_RESOURCE
            + File.separator + "out-sequence" + File.separator;
    //location of the fault sequence
    public static final String FAULT_SEQUENCE_LOCATION = File.separator + APIImportExportConstants.SEQUENCES_RESOURCE
            + File.separator + "fault-sequence" + File.separator;
    //location of the wsdl file
    public static final String WSDL_LOCATION = File.separator + "WSDL" + File.separator;

    public static final String WSDL_URL = "wsdlUrl";

    public static final String DOCUMENT_DIRECTORY = "Docs";

    public static final String INLINE_DOCUMENT_DIRECTORY = "InlineContents";

    public static final String FILE_DOCUMENT_DIRECTORY = "FileContents";

    public static final String CHARSET = "UTF-8";

    public static final String META_INFO_DIRECTORY = "Meta-information";

    public static final String YAML_ENDPOINTS_CERTIFICATE_FILE = File.separator
            + APIImportExportConstants.META_INFO_DIRECTORY + File.separator + "endpoint_certificates.yaml";

    public static final String JSON_ENDPOINTS_CERTIFICATE_FILE = File.separator
            + APIImportExportConstants.META_INFO_DIRECTORY + File.separator + "endpoint_certificates.json";
    
    public static final String YAML_CLIENT_CERTIFICATE_FILE = File.separator
            + APIImportExportConstants.META_INFO_DIRECTORY + File.separator + "client_certificates.yaml";

    public static final String JSON_CLIENT_CERTIFICATE_FILE = File.separator
            + APIImportExportConstants.META_INFO_DIRECTORY + File.separator + "client_certificates.json";

    public static final String APIS_DIRECTORY = "APIs";
    
    public static final String HOSTNAME_JSON_KEY = "hostName";

    public static final String ALIAS_JSON_KEY = "alias";

    public static final String CERTIFICATE_CONTENT_JSON_KEY = "certificate";

    public static final String NODE_TRANSITION = "transition";

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
}
