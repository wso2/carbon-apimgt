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
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the util class which consists of all the functions for exporting API Product.
 */
public class APIAndAPIProductCommonUtil {

    private static final Log log = LogFactory.getLog(APIAndAPIProductCommonUtil.class);

    private APIAndAPIProductCommonUtil() {
    }

    /**
     * Retrieve thumbnail image for the exporting API or API Product and store it in the archive directory.
     *
     * @param identifier                ID of the requesting API or API Product
     * @param registry                  Current tenant registry
     * @throws APIImportExportException If an error occurs while retrieving image from the registry or
     *                                  storing in the archive directory
     */
    public static void exportAPIOrAPIProductThumbnail(String archivePath, Identifier identifier, Registry registry)
            throws APIImportExportException {
        String thumbnailUrl = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR
                + identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getName()
                + RegistryConstants.PATH_SEPARATOR + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_ICON_IMAGE;
        String localImagePath = archivePath + File.separator + APIImportExportConstants.IMAGE_RESOURCE;
        try {
            if (registry.resourceExists(thumbnailUrl)) {
                Resource icon = registry.get(thumbnailUrl);
                String mediaType = icon.getMediaType();
                String extension = APIImportExportConstants.fileExtensionMapping.get(mediaType);
                if (extension != null) {
                    CommonUtil.createDirectory(localImagePath);
                    try (InputStream imageDataStream = icon.getContentStream();
                         OutputStream outputStream = new FileOutputStream(localImagePath + File.separator
                                 + APIConstants.API_ICON_IMAGE + APIConstants.DOT + extension)) {
                        IOUtils.copy(imageDataStream, outputStream);
                        if (log.isDebugEnabled()) {
                            log.debug("Thumbnail image retrieved successfully for API/API Product: " + identifier.getName()
                                    + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                                    + identifier.getVersion());
                        }
                    }
                } else {
                    //api gets imported without thumbnail
                    log.error("Unsupported media type for icon " + mediaType + ". Skipping thumbnail export.");
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Thumbnail URL [" + thumbnailUrl + "] does not exists in registry for API/API Product: "
                        + identifier.getName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                        + identifier.getVersion() + ". Skipping thumbnail export.");
            }
        } catch (RegistryException e) {
            log.error("Error while retrieving API/API Product Thumbnail " + thumbnailUrl, e);
        } catch (IOException e) {
            //Exception is ignored by logging due to the reason that Thumbnail is not essential for
            //an API to be recreated.
            log.error("I/O error while writing API/API Product Thumbnail: " + thumbnailUrl + " to file", e);
        }
    }

    /**
     * Retrieve documentation for the exporting API or API Product and store it in the archive directory.
     * FILE, INLINE, MARKDOWN and URL documentations are handled.
     *
     * @param identifier    ID of the requesting API or API Product
     * @param registry      Current tenant registry
     * @param docList       Documentation list of the exporting API or API Product
     * @param exportFormat  Format for export
     * @throws APIImportExportException If an error occurs while retrieving documents from the
     *                                  registry or storing in the archive directory
     */
    public static void exportAPIOrAPIProductDocumentation(String archivePath, List<Documentation> docList,
                                               Identifier identifier, Registry registry, ExportFormat exportFormat)
            throws APIImportExportException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String docDirectoryPath = File.separator + APIImportExportConstants.DOCUMENT_DIRECTORY;
        CommonUtil.createDirectory(archivePath + docDirectoryPath);
        try {
            for (Documentation doc : docList) {
                String sourceType = doc.getSourceType().name();
                String resourcePath = null;
                String localFilePath;
                String localFileName = null;
                String localDocDirectoryPath = docDirectoryPath;
                if (Documentation.DocumentSourceType.FILE.toString().equalsIgnoreCase(sourceType)) {
                    localFileName = doc.getFilePath().substring(
                            doc.getFilePath().lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                    resourcePath = APIUtil.getDocumentationFilePath(identifier, localFileName);
                    localDocDirectoryPath += File.separator + APIImportExportConstants.FILE_DOCUMENT_DIRECTORY;
                    doc.setFilePath(localFileName);
                } else if (Documentation.DocumentSourceType.INLINE.toString().equalsIgnoreCase(sourceType)
                        || Documentation.DocumentSourceType.MARKDOWN.toString().equalsIgnoreCase(sourceType)) {
                    //Inline/Markdown content file name would be same as the documentation name
                    //Markdown content files will also be stored in InlineContents directory
                    localFileName = doc.getName();
                    resourcePath = APIUtil.getAPIOrAPIProductDocPath(identifier) + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
                            + RegistryConstants.PATH_SEPARATOR + localFileName;
                    localDocDirectoryPath += File.separator + APIImportExportConstants.INLINE_DOCUMENT_DIRECTORY;
                }

                if (resourcePath != null) {
                    //Write content separately for Inline/Markdown/File type documentations only
                    //check whether resource exists in the registry
                    if (registry.resourceExists(resourcePath)) {
                        CommonUtil.createDirectory(archivePath + localDocDirectoryPath);
                        localFilePath = localDocDirectoryPath + File.separator + localFileName;
                        Resource docFile = registry.get(resourcePath);
                        try (OutputStream outputStream = new FileOutputStream(archivePath + localFilePath);
                             InputStream fileInputStream = docFile.getContentStream()) {
                            IOUtils.copy(fileInputStream, outputStream);
                        }
                    } else {
                        //Log error and avoid throwing as we give capability to export document artifact without the
                        //content if does not exists
                        String errorMessage = "Documentation resource for API/API Product: " + identifier.getName()
                                + " not found in " + resourcePath;
                        log.error(errorMessage);
                    }
                }
            }

            String json = gson.toJson(docList);
            switch (exportFormat) {
                case JSON:
                    CommonUtil.writeFile(archivePath + APIImportExportConstants.JSON_DOCUMENT_FILE_LOCATION, json);
                    break;
                case YAML:
                    String yaml = CommonUtil.jsonToYaml(json);
                    CommonUtil.writeFile(archivePath + APIImportExportConstants.YAML_DOCUMENT_FILE_LOCATION, yaml);
                    break;
            }

            if (log.isDebugEnabled()) {
                log.debug("Documentation retrieved successfully for API/API Product: " + identifier.getName()
                        + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + identifier.getVersion());
            }
        } catch (IOException e) {
            String errorMessage = "I/O error while writing documentation to file for API/API Product: "
                    + identifier.getName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                    + identifier.getVersion();
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (RegistryException e) {
            String errorMessage = "Error while retrieving documentation for API/API Product: " + identifier.getName()
                    + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + identifier.getVersion();
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Export Mutual SSL related certificates
     *
     * @param apiTypeWrapper    API or API Product to be exported
     * @param tenantId          Tenant id of the user
     * @param provider          Api Provider
     * @param exportFormat      Export format of file
     * @throws APIImportExportException
     */

    public static void exportClientCertificates(String archivePath, ApiTypeWrapper apiTypeWrapper, int tenantId, APIProvider provider,
                                                 ExportFormat exportFormat) throws APIImportExportException {

        List<ClientCertificateDTO> certificateMetadataDTOS;
        try {
            if (!apiTypeWrapper.isAPIProduct()) {
                certificateMetadataDTOS = provider.searchClientCertificates(tenantId, null, apiTypeWrapper.getApi().getId());
            } else {
                certificateMetadataDTOS = provider.searchClientCertificates(tenantId, null, apiTypeWrapper.getApiProduct().getId());
            }
            if (!certificateMetadataDTOS.isEmpty()) {
                CommonUtil.createDirectory(archivePath + File.separator + APIImportExportConstants.META_INFO_DIRECTORY);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String element = gson.toJson(certificateMetadataDTOS,
                        new TypeToken<ArrayList<ClientCertificateDTO>>() {
                        }.getType());

                switch (exportFormat) {
                    case YAML:
                        String yaml = CommonUtil.jsonToYaml(element);
                        CommonUtil.writeFile(archivePath + APIImportExportConstants.YAML_CLIENT_CERTIFICATE_FILE,
                                yaml);
                        break;
                    case JSON:
                        CommonUtil.writeFile(archivePath + APIImportExportConstants.JSON_CLIENT_CERTIFICATE_FILE,
                                element);
                }
            }
        } catch (IOException e) {
            String errorMessage = "Error while retrieving saving as YAML";
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (APIManagementException e) {
            String errorMsg = "Error retrieving certificate meta data. tenantId [" + tenantId + "] api ["
                    + tenantId + "]";
            throw new APIImportExportException(errorMsg, e);
        }
    }
}
