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

package org.wso2.carbon.apimgt.impl.importexport.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportManager;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is the util class which consists of all the functions for exporting API Product.
 */
public class APIProductExportUtil {

    private static final Log log = LogFactory.getLog(APIProductExportUtil.class);

    private APIProductExportUtil() {
    }

    /**
     * This method retrieves all meta information and registry resources required for an API Product to
     * recreate.
     *
     * @param archiveBasePath       temp location to save the API Product artifacts
     * @param apiProductToReturn    Exporting API Product
     * @param userName              User name of the requester
     * @param provider              API Product Provider
     * @param exportFormat          Export format of the API Product meta data, could be yaml or json
     * @param isStatusPreserved     Whether API Product status is preserved while export
     * @throws APIImportExportException If an error occurs while retrieving API Product related resources
     */
    public static void retrieveApiProductToExport(String archiveBasePath, APIProduct apiProductToReturn, APIProvider provider, String userName,
                                           boolean isStatusPreserved, ExportFormat exportFormat)
            throws APIImportExportException {

        UserRegistry registry;
        APIProductIdentifier apiProductIdentifier = apiProductToReturn.getId();
        String archivePath = archiveBasePath.concat(File.separator + apiProductIdentifier.getName() + "-"
                + apiProductIdentifier.getVersion());
        int tenantId = APIUtil.getTenantId(userName);

        try {
            registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
            // Directory creation
            CommonUtil.createDirectory(archivePath);

            // Export thumbnail
            exportAPIProductThumbnail(archivePath, apiProductIdentifier, registry);

            // Export documents
            List<Documentation> docList = provider.getAllDocumentation(apiProductIdentifier);
            if (!docList.isEmpty()) {
                exportAPIProductDocumentation(archivePath, docList, apiProductIdentifier, registry, exportFormat);
            } else if (log.isDebugEnabled()) {
                log.debug("No documentation found for API Product: " + apiProductIdentifier + ". Skipping API Product documentation export.");
            }

            // Export meta information
            exportAPIProductMetaInformation(archivePath, apiProductToReturn, registry, exportFormat, provider);

            // Export dependent APIs
            exportDependentAPIs(archivePath, apiProductToReturn, exportFormat, provider, userName, isStatusPreserved);

            // Export mTLS authentication related certificates
            if(provider.isClientCertificateBasedAuthenticationConfigured()) {
                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL enabled. Exporting client certificates.");
                }
                ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(apiProductToReturn);
                APIAndAPIProductCommonUtil.exportClientCertificates(archivePath, apiTypeWrapper, tenantId, provider, exportFormat);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Unable to retrieve artifacts for API Product: " + apiProductIdentifier.getName()
                    + StringUtils.SPACE + APIConstants.API_DATA_VERSION + " : " + apiProductIdentifier.getVersion();
            throw new APIImportExportException(errorMessage, e);
        } catch (RegistryException e) {
            String errorMessage = "Error while getting governance registry for tenant: " + tenantId;
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Export dependent APIs by checking the resources of the API Product.
     *
     * @param archivePath               Temp location to save the API artifacts
     * @param apiProductToReturn        API Product which the resources should be considered
     * @param userName                  User name of the requester
     * @param provider                  API Product Provider
     * @param exportFormat              Export format of the API meta data, could be yaml or json
     * @param isStatusPreserved         Whether API status is preserved while export
     * @throws APIImportExportException If an error occurs while retrieving API related resources
     */
    private static void exportDependentAPIs(String archivePath, APIProduct apiProductToReturn, ExportFormat exportFormat,
                                            APIProvider provider, String userName, Boolean isStatusPreserved) throws APIImportExportException, APIManagementException {
        String apisDirectoryPath = archivePath + File.separator + APIImportExportConstants.APIS_DIRECTORY;
        CommonUtil.createDirectory(apisDirectoryPath);

        List<APIProductResource> apiProductResources = apiProductToReturn.getProductResources();
        for (APIProductResource apiProductResource : apiProductResources) {
            APIIdentifier apiIdentifier = apiProductResource.getApiIdentifier();
            API api = provider.getAPI(apiIdentifier);
            APIExportUtil.retrieveApiToExport(apisDirectoryPath, api, provider, userName, isStatusPreserved, exportFormat);
        }
    }

    /**
     * Retrieve thumbnail image for the exporting API Product and store it in the archive directory.
     *
     * @param apiProductIdentifier  ID of the requesting API Product
     * @param registry              Current tenant registry
     * @throws APIImportExportException If an error occurs while retrieving image from the registry or
     *                                  storing in the archive directory
     */
    private static void exportAPIProductThumbnail(String archivePath, APIProductIdentifier apiProductIdentifier, Registry registry)
            throws APIImportExportException {
        APIAndAPIProductCommonUtil.exportAPIOrAPIProductThumbnail(archivePath, apiProductIdentifier, registry);
    }

    /**
     * Retrieve documentation for the exporting API Product and store it in the archive directory.
     * FILE, INLINE, MARKDOWN and URL documentations are handled.
     *
     * @param apiProductIdentifier  ID of the requesting API Product
     * @param registry              Current tenant registry
     * @param docList               Documentation list of the exporting API Product
     * @param exportFormat          Format for export
     * @throws APIImportExportException If an error occurs while retrieving documents from the
     *                                  registry or storing in the archive directory
     */
    private static void exportAPIProductDocumentation(String archivePath, List<Documentation> docList,
                                               APIProductIdentifier apiProductIdentifier, Registry registry, ExportFormat exportFormat)
            throws APIImportExportException {
        APIAndAPIProductCommonUtil.exportAPIOrAPIProductDocumentation(archivePath, docList, apiProductIdentifier, registry, exportFormat);
    }

    /**
     * Retrieve meta information of the API Product to export.
     * URL template information are stored in swagger.json definition while rest of the required
     * data are in api.json
     *
     * @param apiProductToReturn    API Product to be exported
     * @param registry              Current tenant registry
     * @param exportFormat          Export format of file
     * @param apiProvider           API Product Provider
     * @throws APIImportExportException If an error occurs while exporting meta information
     * @throws APIManagementException If an error occurs while removing unnecessary data from exported API Product
     *                                or while retrieving Swagger definition for API Product
     */
    private static void exportAPIProductMetaInformation(String archivePath, APIProduct apiProductToReturn, Registry registry,
                                              ExportFormat exportFormat, APIProvider apiProvider)
            throws APIImportExportException, APIManagementException {

        CommonUtil.createDirectory(archivePath + File.separator + APIImportExportConstants.META_INFO_DIRECTORY);
        // Remove unnecessary data from exported API Product
        cleanApiProductDataToExport(apiProductToReturn);
        // Get only the subscription tier names of the API, rather than retrieving the whole object array
        ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(apiProductToReturn);
        Set<String> availableSubscriptionTierNames = APIAndAPIProductCommonUtil.getAvailableTierNames(apiTypeWrapper);

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Swagger.json contains complete details about scopes. Therefore scope details and uri templates
            // are removed from api.json.
            apiProductToReturn.setScopes(new LinkedHashSet<>());

            String swaggerDefinition = OASParserUtil.getAPIDefinition(apiProductToReturn.getId(), registry);
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(swaggerDefinition).getAsJsonObject();
            String formattedSwaggerJson = gson.toJson(json);
            switch (exportFormat) {
                case YAML:
                    String swaggerInYaml = CommonUtil.jsonToYaml(formattedSwaggerJson);
                    CommonUtil.writeFile(archivePath + APIImportExportConstants.YAML_SWAGGER_DEFINITION_LOCATION,
                            swaggerInYaml);
                    break;
                case JSON:
                    CommonUtil.writeFile(archivePath + APIImportExportConstants.JSON_SWAGGER_DEFINITION_LOCATION,
                            formattedSwaggerJson);
            }

            if (log.isDebugEnabled()) {
                log.debug("Meta information retrieved successfully for API Product: " + apiProductToReturn.getId().getName()
                        + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiProductToReturn.getId().getVersion());
            }


            String apiInJson = gson.toJson(apiProductToReturn);
            JSONParser jsonParser = new JSONParser();
            org.json.simple.JSONObject apiJsonObject = (org.json.simple.JSONObject) jsonParser.parse(apiInJson);
            apiJsonObject.remove(APIConstants.SUBSCRIPTION_TIERS);
            apiJsonObject.put(APIConstants.SUBSCRIPTION_TIERS, availableSubscriptionTierNames);
            apiInJson = gson.toJson(apiJsonObject);
            switch (exportFormat) {
                case JSON:
                    CommonUtil.writeFile(archivePath + APIImportExportConstants.JSON_API_FILE_LOCATION, apiInJson);
                    break;
                case YAML:
                    String apiInYaml = CommonUtil.jsonToYaml(apiInJson);
                    CommonUtil.writeFile(archivePath + APIImportExportConstants.YAML_API_FILE_LOCATION, apiInYaml);
                    break;
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Swagger definition for API Product: "
                    + apiProductToReturn.getId().getName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                    + apiProductToReturn.getId().getVersion();
            throw new APIImportExportException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Error while retrieving saving as YAML for API Product: " + apiProductToReturn.getId().getName()
                    + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiProductToReturn.getId().getVersion();
            throw new APIImportExportException(errorMessage, e);
        } catch (ParseException e) {
            String msg = "ParseException thrown when parsing API Product config";
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Clean API Product by removing unnecessary details.
     *
     * @param apiProduct API Product to be exported
     */
    private static void cleanApiProductDataToExport(APIProduct apiProduct) {
        // Thumbnail will be set according to the importing environment. Therefore current URL is removed
        apiProduct.setThumbnailUrl(null);
    }

    /**
     * Exports an API Product from API Manager for a given API Product ID. Meta information, API icon, documentation, client certificates
     * and dependent APIs are exported. This service generates a zipped archive which contains all the above mentioned
     * resources for a given API Product.
     *
     * @param apiProvider           API provider
     * @param apiProductIdentifier  ID of the requesting API Product
     * @param userName              User name of the user
     * @param preserveStatus        Preserve API status on export
     * @return Zipped file containing exported API Product
     * @throws APIImportExportException If an error occurs while exporting the API Product and creating the archive
     * @throws APIManagementException If an error occurs while retrieving the API Product
     */
    public static File exportApiProduct(APIProvider apiProvider, APIProductIdentifier apiProductIdentifier, String userName,
                                 ExportFormat exportFormat, Boolean preserveStatus)
            throws APIImportExportException, APIManagementException {
        APIProduct apiProduct;
        APIImportExportManager apiImportExportManager;
        boolean isStatusPreserved = preserveStatus == null || preserveStatus;
        apiProduct = apiProvider.getAPIProduct(apiProductIdentifier);
        ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(apiProduct);
        apiImportExportManager = new APIImportExportManager(apiProvider, userName);
        return apiImportExportManager.exportAPIOrAPIProductArchive(apiTypeWrapper, isStatusPreserved, exportFormat);
    }
}
