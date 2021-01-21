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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManager;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ProductAPIDTO;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExportUtils {

    private static final Log log = LogFactory.getLog(ExportUtils.class);
    private static final String IN = "in";
    private static final String OUT = "out";
    private static final String SOAPTOREST = "SoapToRest";

    /**
     * Validate name, version and provider before exporting an API/API Product
     *
     * @param name         API/API Product Name
     * @param version      API/API Product version
     * @param providerName Name of the provider
     * @return Name of the provider
     * @throws APIManagementException If an error occurs while retrieving the provider name from name, version
     *                                and tenant
     */
    public static String validateExportParams(String name, String version, String providerName)
            throws APIManagementException {

        if (name == null || version == null) {
            throw new APIManagementException("'name' (" + name + ") or 'version' (" + version + ") should not be null.",
                    ExceptionCodes.API_NAME_OR_VERSION_NOT_NULL);
        }
        String apiRequesterDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

        // If provider name is not given
        if (StringUtils.isBlank(providerName)) {
            // Retrieve the provider who is in same tenant domain and who owns the same API (by comparing
            // API name and the version)
            providerName = APIUtil.getAPIProviderFromAPINameVersionTenant(name, version, apiRequesterDomain);

            // If there is no provider in current domain, the API cannot be exported
            if (providerName == null) {
                throw new APIMgtResourceNotFoundException(
                        "Error occurred while exporting. API: " + name + " version: " + version + " not found");
            }
        }

        if (!StringUtils.equals(MultitenantUtils.getTenantDomain(providerName), apiRequesterDomain)) {
            throw new APIMgtAuthorizationFailedException(
                    RestApiConstants.RESOURCE_API + " name:" + name + " version:" + version + " provider:"
                            + providerName);
        }
        return providerName;
    }

    /**
     * Exports an API from API Manager for a given API. Meta information, API icon, documentation,
     * WSDL and sequences are exported.
     *
     * @param apiProvider    API Provider
     * @param apiIdentifier  API Identifier
     * @param apiDtoToReturn API DTO
     * @param userName       Username
     * @param exportFormat   Format of output documents. Can be YAML or JSON
     * @param preserveStatus Preserve API status on export
     * @return
     * @throws APIManagementException If an error occurs while getting governance registry
     */
    public static File exportApi(APIProvider apiProvider, APIIdentifier apiIdentifier, APIDTO apiDtoToReturn,
            String userName, ExportFormat exportFormat, boolean preserveStatus, boolean preserveDocs)
            throws APIManagementException, APIImportExportException {

        int tenantId = 0;
        try {
            // Create temp location for storing API data
            File exportFolder = CommonUtil.createTempDirectory(apiIdentifier);
            String exportAPIBasePath = exportFolder.toString();
            String archivePath = exportAPIBasePath
                    .concat(File.separator + apiIdentifier.getApiName() + "-" + apiIdentifier.getVersion());
            tenantId = APIUtil.getTenantId(userName);
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry(tenantId);

            CommonUtil.createDirectory(archivePath);
            if (preserveDocs) {
                addThumbnailToArchive(archivePath, apiIdentifier, apiProvider, APIConstants.API_IDENTIFIER_TYPE);
            }
            addSOAPToRESTMediationToArchive(archivePath, apiIdentifier, registry);
            if (preserveDocs) {
                addDocumentationToArchive(archivePath, apiIdentifier, registry, exportFormat, apiProvider);
            }

            if (StringUtils.isNotEmpty(apiDtoToReturn.getWsdlUrl()) && preserveDocs) {
                addWSDLtoArchive(archivePath, apiIdentifier, registry);
            } else if (log.isDebugEnabled()) {
                log.debug("No WSDL URL found for API: " + apiIdentifier + ". Skipping WSDL export.");
            }

            addSequencesToArchive(archivePath, apiIdentifier, apiDtoToReturn, registry, apiProvider);

            // Set API status to created if the status is not preserved
            if (!preserveStatus) {
                apiDtoToReturn.setLifeCycleStatus(APIConstants.CREATED);
            }

            addEndpointCertificatesToArchive(archivePath, apiDtoToReturn, tenantId, exportFormat);
            addAPIMetaInformationToArchive(archivePath, apiDtoToReturn, exportFormat, apiProvider, apiIdentifier);

            // Export mTLS authentication related certificates
                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL enabled. Exporting client certificates.");
                }
                addClientCertificatesToArchive(archivePath, apiIdentifier, tenantId, apiProvider, exportFormat);
            CommonUtil.archiveDirectory(exportAPIBasePath);
            FileUtils.deleteQuietly(new File(exportAPIBasePath));
            return new File(exportAPIBasePath + APIConstants.ZIP_FILE_EXTENSION);
        } catch (RegistryException e) {
            throw new APIManagementException("Error while getting governance registry for tenant: " + tenantId, e);
        }
    }

    /**
     * Exports an API Product from API Manager for a given API Product. MMeta information, API Product icon,
     * documentation, client certificates and dependent APIs are exported.
     *
     * @param apiProvider           API Provider
     * @param apiProductIdentifier  API Product Identifier
     * @param apiProductDtoToReturn API Product DTO
     * @param userName              Username
     * @param exportFormat          Format of output documents. Can be YAML or JSON
     * @param preserveStatus        Preserve API Product status on export
     * @return
     * @throws APIManagementException If an error occurs while getting governance registry
     */
    public static File exportApiProduct(APIProvider apiProvider, APIProductIdentifier apiProductIdentifier,
                                        APIProductDTO apiProductDtoToReturn, String userName, ExportFormat exportFormat,
                                        Boolean preserveStatus,
                                        boolean preserveDocs, boolean preserveCredentials)
            throws APIManagementException, APIImportExportException {

        int tenantId = 0;
        try {
            // Create temp location for storing API Product data
            File exportFolder = CommonUtil.createTempDirectory(apiProductIdentifier);
            String exportAPIBasePath = exportFolder.toString();
            String archivePath = exportAPIBasePath
                    .concat(File.separator + apiProductIdentifier.getName() + "-" + apiProductIdentifier.getVersion());
            tenantId = APIUtil.getTenantId(userName);
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry(tenantId);

            CommonUtil.createDirectory(archivePath);

            if (preserveDocs) {
                addThumbnailToArchive(archivePath, apiProductIdentifier, apiProvider,
                        APIConstants.API_PRODUCT_IDENTIFIER_TYPE);
                addDocumentationToArchive(archivePath, apiProductIdentifier, registry, exportFormat, apiProvider);

            }
            addAPIProductMetaInformationToArchive(archivePath, apiProductDtoToReturn, exportFormat, apiProvider);
            addDependentAPIsToArchive(archivePath, apiProductDtoToReturn, exportFormat, apiProvider, userName,
                    preserveStatus, preserveDocs, preserveCredentials);

            // Export mTLS authentication related certificates
                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL enabled. Exporting client certificates.");
                }
                addClientCertificatesToArchive(archivePath, apiProductIdentifier, tenantId, apiProvider, exportFormat);

            CommonUtil.archiveDirectory(exportAPIBasePath);
            FileUtils.deleteQuietly(new File(exportAPIBasePath));
            return new File(exportAPIBasePath + APIConstants.ZIP_FILE_EXTENSION);
        } catch (RegistryException e) {
            throw new APIManagementException("Error while getting governance registry for tenant: " + tenantId, e);
        }
    }

    /**
     * Retrieve thumbnail image for the exporting API or API Product and store it in the archive directory.
     *
     * @param archivePath File path to export the thumbnail image
     * @param identifier  ID of the requesting API or API Product
     * @param apiProvider API Provider
     * @param type        Type (whether an API or an API Product
     * @throws APIImportExportException If an error occurs while retrieving image from the registry or
     *                                  storing in the archive directory
     */
    public static void addThumbnailToArchive(String archivePath, Identifier identifier, APIProvider apiProvider,
            String type) throws APIImportExportException, APIManagementException {

        String localImagePath = archivePath + File.separator + ImportExportConstants.IMAGE_RESOURCE;
        try {
            ResourceFile thumbnailResource = StringUtils.equals(type, APIConstants.API_IDENTIFIER_TYPE) ?
                    apiProvider.getIcon((APIIdentifier) identifier) :
                    apiProvider.getProductIcon((APIProductIdentifier) identifier);
            if (thumbnailResource != null) {
                String mediaType = thumbnailResource.getContentType();
                String extension = ImportExportConstants.fileExtensionMapping.get(mediaType);
                if (extension != null) {
                    CommonUtil.createDirectory(localImagePath);
                    try (InputStream imageDataStream = thumbnailResource.getContent();
                            OutputStream outputStream = new FileOutputStream(
                                    localImagePath + File.separator + APIConstants.API_ICON_IMAGE + APIConstants.DOT
                                            + extension)) {
                        IOUtils.copy(imageDataStream, outputStream);
                        if (log.isDebugEnabled()) {
                            log.debug("Thumbnail image retrieved successfully for API/API Product: " + identifier
                                    .getName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + identifier
                                    .getVersion());
                        }
                    }
                } else {
                    //api gets imported without thumbnail
                    log.error("Unsupported media type for icon " + mediaType + ". Skipping thumbnail export.");
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Thumbnail URL does not exists in registry for API/API Product: " + identifier.getName()
                        + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + identifier.getVersion()
                        + ". Skipping thumbnail export.");
            }
        } catch (IOException e) {
            //Exception is ignored by logging due to the reason that Thumbnail is not essential for
            //an API to be recreated.
            log.error("I/O error while writing API/API Product Thumbnail to file", e);
        }
    }

    /**
     * Retrieve SOAP to REST mediation logic for the exporting API and store it in the archive directory
     *
     * @param archivePath   File path to export the SOAPToREST mediation logic
     * @param apiIdentifier ID of the requesting API
     * @param registry      Current tenant registry
     * @throws APIImportExportException If an error occurs while retrieving image from the registry or
     *                                  storing in the archive directory
     */
    public static void addSOAPToRESTMediationToArchive(String archivePath, APIIdentifier apiIdentifier,
            UserRegistry registry) throws APIImportExportException {

        String soapToRestBaseUrl =
                "/apimgt/applicationdata/provider" + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getProviderName()
                        + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getApiName()
                        + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion()
                        + RegistryConstants.PATH_SEPARATOR + SOAPToRESTConstants.SOAP_TO_REST_RESOURCE;
        try {
            if (registry.resourceExists(soapToRestBaseUrl)) {
                Collection inFlow = (org.wso2.carbon.registry.api.Collection) registry
                        .get(soapToRestBaseUrl + RegistryConstants.PATH_SEPARATOR + IN);
                Collection outFlow = (org.wso2.carbon.registry.api.Collection) registry
                        .get(soapToRestBaseUrl + RegistryConstants.PATH_SEPARATOR + OUT);

                CommonUtil.createDirectory(archivePath + File.separator + SOAPTOREST + File.separator + IN);
                CommonUtil.createDirectory(archivePath + File.separator + SOAPTOREST + File.separator + OUT);
                if (inFlow != null) {
                    for (String inFlowPath : inFlow.getChildren()) {
                        try (InputStream inputStream = registry.get(inFlowPath).getContentStream();
                                OutputStream outputStream = new FileOutputStream(
                                        archivePath + File.separator + SOAPTOREST + File.separator + IN + inFlowPath
                                                .substring(
                                                        inFlowPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR)));) {
                            IOUtils.copy(inputStream, outputStream);
                        }
                    }
                }
                if (outFlow != null) {
                    for (String outFlowPath : outFlow.getChildren()) {
                        try (InputStream inputStream = registry.get(outFlowPath).getContentStream();
                                OutputStream outputStream = new FileOutputStream(
                                        archivePath + File.separator + SOAPTOREST + File.separator + OUT + outFlowPath.
                                                substring(outFlowPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR)))) {
                            IOUtils.copy(inputStream, outputStream);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new APIImportExportException("I/O error while writing API SOAP to REST logic to file", e);
        } catch (RegistryException e) {
            throw new APIImportExportException("Error while retrieving SOAP to REST logic", e);
        }
    }

    /**
     * Retrieve documentation for the exporting API or API Product and store it in the archive directory.
     * FILE, INLINE, MARKDOWN and URL documentations are handled.
     *
     * @param archivePath  File path to export the documents
     * @param identifier   ID of the requesting API or API Product
     * @param registry     Current tenant registry
     * @param exportFormat Format for export
     * @param apiProvider  API Provider
     * @throws APIImportExportException If an error occurs while retrieving documents from the
     *                                  registry or storing in the archive directory
     * @throws APIManagementException   If an error occurs while retrieving document details
     */
    public static void addDocumentationToArchive(String archivePath, Identifier identifier, Registry registry,
            ExportFormat exportFormat, APIProvider apiProvider)
            throws APIImportExportException, APIManagementException {

        List<Documentation> docList = apiProvider.getAllDocumentation(identifier);
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (!docList.isEmpty()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String docDirectoryPath = archivePath + File.separator + ImportExportConstants.DOCUMENT_DIRECTORY;
            CommonUtil.createDirectory(docDirectoryPath);
            try {
                for (Documentation doc : docList) {
                    // Retrieving the document again since objects in docList might have missing fields
                    Documentation individualDocument = apiProvider.getDocumentation(identifier.getUUID(), doc.getId(),
                            tenantDomain);
                    String sourceType = individualDocument.getSourceType().name();
                    String resourcePath = null;
                    String localFileName = null;
                    String individualDocDirectoryPath =
                            docDirectoryPath + File.separator + cleanFolderName(individualDocument.getName());
                    CommonUtil.createDirectory(individualDocDirectoryPath);
                    if (Documentation.DocumentSourceType.FILE.toString().equalsIgnoreCase(sourceType)) {
                        localFileName = individualDocument.getFilePath().substring(
                                individualDocument.getFilePath().lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                        resourcePath = APIUtil.getDocumentationFilePath(identifier, localFileName);
                        individualDocument.setFilePath(localFileName);
                    } else if (Documentation.DocumentSourceType.INLINE.toString().equalsIgnoreCase(sourceType)
                            || Documentation.DocumentSourceType.MARKDOWN.toString().equalsIgnoreCase(sourceType)) {
                        // Inline/Markdown content file name would be same as the documentation name
                        localFileName = individualDocument.getName();
                        resourcePath =
                                APIUtil.getAPIOrAPIProductDocPath(identifier) + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
                                        + RegistryConstants.PATH_SEPARATOR + localFileName;
                    }
                    CommonUtil.writeDtoToFile(individualDocDirectoryPath + ImportExportConstants.DOCUMENT_FILE_NAME, exportFormat,
                            ImportExportConstants.TYPE_DOCUMENTS,
                            DocumentationMappingUtil.fromDocumentationToDTO(individualDocument));

                    if (resourcePath != null) {
                        // Write content for Inline/Markdown/File type documentations only
                        // Check whether resource exists in the registry
                        if (registry.resourceExists(resourcePath)) {
                            Resource docFile = registry.get(resourcePath);
                            try (OutputStream outputStream = new FileOutputStream(
                                    individualDocDirectoryPath + File.separator + localFileName);
                                    InputStream fileInputStream = docFile.getContentStream()) {
                                IOUtils.copy(fileInputStream, outputStream);
                            }
                        } else {
                            // Log error and avoid throwing as we give capability to export document artifact without
                            // the content if does not exists
                            log.error("Documentation resource for API/API Product: " + identifier.getName()
                                    + " not found in " + resourcePath);
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Documentation retrieved successfully for API/API Product: " + identifier.getName()
                            + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + identifier.getVersion());
                }
            } catch (IOException e) {
                throw new APIImportExportException(
                        "I/O error while writing documentation to file for API/API Product: " + identifier.getName()
                                + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + identifier.getVersion(),
                        e);
            } catch (RegistryException e) {
                throw new APIImportExportException(
                        "Error while retrieving documentation for API/API Product: " + identifier.getName()
                                + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + identifier.getVersion(),
                        e);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("No documentation found for API/API Product: " + identifier + ". Skipping documentation export.");
        }
    }

    /**
     * Replace the unwanted characters for a folder name with the underscore.
     *
     * @param name Name of the folder
     * @return Folder name which the unwanted characters are replaced
     */
    private static String cleanFolderName(String name) {
        // Replace everything but [a-zA-Z0-9.-]
        return name.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    /**
     * Retrieve WSDL for the exporting API and store it in the archive directory.
     *
     * @param archivePath   File path to export the WSDL
     * @param apiIdentifier ID of the requesting API
     * @param registry      Current tenant registry
     * @throws APIImportExportException If an error occurs while retrieving WSDL from the registry or
     *                                  storing in the archive directory
     */
    public static void addWSDLtoArchive(String archivePath, APIIdentifier apiIdentifier, Registry registry)
            throws APIImportExportException {

        String wsdlPath =
                APIConstants.API_WSDL_RESOURCE_LOCATION + apiIdentifier.getProviderName() + "--" + apiIdentifier
                        .getApiName() + apiIdentifier.getVersion() + APIConstants.WSDL_FILE_EXTENSION;
        try {
            if (registry.resourceExists(wsdlPath)) {
                CommonUtil.createDirectory(archivePath + File.separator + "WSDL");
                Resource wsdl = registry.get(wsdlPath);
                try (InputStream wsdlStream = wsdl.getContentStream();
                        OutputStream outputStream = new FileOutputStream(
                                archivePath + File.separator + "WSDL" + File.separator + apiIdentifier.getApiName()
                                        + "-" + apiIdentifier.getVersion() + APIConstants.WSDL_FILE_EXTENSION)) {
                    IOUtils.copy(wsdlStream, outputStream);
                    if (log.isDebugEnabled()) {
                        log.debug("WSDL file: " + wsdlPath + " retrieved successfully");
                    }
                }
            } else if (log.isDebugEnabled()) {
                log.debug("WSDL resource does not exists in path: " + wsdlPath + ". Skipping WSDL export.");
            }
        } catch (IOException e) {
            throw new APIImportExportException("I/O error while writing WSDL: " + wsdlPath + " to file", e);
        } catch (RegistryException e) {
            throw new APIImportExportException("Error while retrieving WSDL: " + wsdlPath + " to file", e);
        }
    }

    /**
     * Retrieve available custom sequences and API specific sequences for API export, and store it in the archive
     * directory.
     *
     * @param archivePath   File path to export the sequences
     * @param apiIdentifier API Identifier
     * @param apiDto        API DTO
     * @param registry      Current tenant registry
     * @param apiProvider   API Provider
     * @throws APIImportExportException If an error occurs while exporting sequences
     */
    public static void addSequencesToArchive(String archivePath, APIIdentifier apiIdentifier, APIDTO apiDto,
            Registry registry, APIProvider apiProvider)
            throws APIImportExportException, APIManagementException, RegistryException {

        String seqArchivePath = archivePath.concat(File.separator + ImportExportConstants.SEQUENCES_RESOURCE);
        List<MediationPolicyDTO> mediationPolicyDtos = apiDto.getMediationPolicies();

        if (!mediationPolicyDtos.isEmpty()) {
            CommonUtil.createDirectory(seqArchivePath);
            for (MediationPolicyDTO mediationPolicyDto : mediationPolicyDtos) {
                if (mediationPolicyDto.isShared()) {
                    String individualSequenceExportPath =
                            seqArchivePath + File.separator + mediationPolicyDto.getType().toLowerCase()
                                    + ImportExportConstants.SEQUENCE_LOCATION_POSTFIX;
                    if (!CommonUtil.checkFileExistence(individualSequenceExportPath)) {
                        CommonUtil.createDirectory(individualSequenceExportPath);
                    }
                    //Get registry resource correspond to identifier
                    Resource mediationResource = apiProvider
                            .getCustomMediationResourceFromUuid(mediationPolicyDto.getId());
                    writeSequenceToArchive(mediationResource, individualSequenceExportPath, registry,
                            mediationPolicyDto.getName(), apiIdentifier);
                }
            }

            // Getting list of API specific custom mediation policies
            List<Mediation> apiSpecificMediationList = apiProvider.getAllApiSpecificMediationPolicies(apiIdentifier);
            if (!apiSpecificMediationList.isEmpty()) {
                for (Mediation mediation : apiSpecificMediationList) {
                    String individualSequenceExportPath =
                            seqArchivePath + File.separator + mediation.getType().toLowerCase()
                                    + ImportExportConstants.SEQUENCE_LOCATION_POSTFIX + File.separator
                                    + ImportExportConstants.CUSTOM_TYPE;
                    if (!CommonUtil.checkFileExistence(individualSequenceExportPath)) {
                        CommonUtil.createDirectory(individualSequenceExportPath);
                    }
                    // Get registry resource correspond to identifier
                    String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
                    apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
                    Resource mediationResource = apiProvider
                            .getApiSpecificMediationResourceFromUuid(apiIdentifier, mediation.getUuid(),
                                    apiResourcePath);
                    writeSequenceToArchive(mediationResource, individualSequenceExportPath, registry,
                            mediation.getName(), apiIdentifier);
                }
            }
        }
    }

    /**
     * Write the sequence to API archive.
     *
     * @param mediationResource Mediation resource
     * @param individualSequenceExportPath         Path to export the mediation sequence
     * @param registry     Current tenant registry
     * @param mediationName     Name of the mediation policy
     * @param apiIdentifier     API Identifier
     * @throws RegistryException If an error occurs while retrieving registry elements
     * @throws APIManagementException If an error occurs while writing the mediation policy to file
     */
    private static void writeSequenceToArchive(Resource mediationResource, String individualSequenceExportPath,
            Registry registry, String mediationName, APIIdentifier apiIdentifier)
            throws RegistryException, APIManagementException {
        if (mediationResource != null) {
            // Get the registry resource path
            String resourcePath = mediationResource.getPath();
            // Check whether resource exists in the registry
            if (registry.resourceExists(resourcePath)) {
                Resource mediationFile = registry.get(resourcePath);
                try (OutputStream outputStream = new FileOutputStream(
                        individualSequenceExportPath + File.separator + mediationName + APIConstants.DOT
                                + APIConstants.XML_DOC_EXTENSION);
                        InputStream fileInputStream = mediationFile.getContentStream()) {
                    IOUtils.copy(fileInputStream, outputStream);
                } catch (IOException e) {
                    throw new APIManagementException("Error while writing the mediation sequence"+ mediationName+ "to file", e);
                }
            } else {
                throw new APIManagementException(
                        "Resource specified by " + resourcePath + " cannot be found in the registry",
                        ExceptionCodes.RESOURCE_NOT_FOUND);
            }
        }
    }

    /**
     * Retrieve the endpoint certificates and store those in the archive directory.
     *
     * @param archivePath  File path to export the endpoint certificates
     * @param apiDto       API DTO to be exported
     * @param tenantId     Tenant id of the user
     * @param exportFormat Export format of file
     * @throws APIImportExportException If an error occurs while exporting endpoint certificates
     */
    public static void addEndpointCertificatesToArchive(String archivePath, APIDTO apiDto, int tenantId,
            ExportFormat exportFormat) throws APIImportExportException {

        List<String> productionEndpoints;
        List<String> sandboxEndpoints;
        Set<String> uniqueEndpointURLs = new HashSet<>();
        JsonArray endpointCertificatesDetails = new JsonArray();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String endpointConfigString = gson.toJson(apiDto.getEndpointConfig());
        String endpointCertsDirectoryPath =
                archivePath + File.separator + ImportExportConstants.ENDPOINT_CERTIFICATES_DIRECTORY;
        CommonUtil.createDirectory(endpointCertsDirectoryPath);

        if (StringUtils.isEmpty(endpointConfigString)) {
            if (log.isDebugEnabled()) {
                log.debug("Endpoint Details are empty for API: " + apiDto.getName() + StringUtils.SPACE
                        + APIConstants.API_DATA_VERSION + ": " + apiDto.getVersion());
            }
            return;
        }
        try {
            JSONTokener tokener = new JSONTokener(endpointConfigString);
            JSONObject endpointConfig = new JSONObject(tokener);
            productionEndpoints = getEndpointURLs(endpointConfig, APIConstants.API_DATA_PRODUCTION_ENDPOINTS,
                    apiDto.getName());
            sandboxEndpoints = getEndpointURLs(endpointConfig, APIConstants.API_DATA_SANDBOX_ENDPOINTS,
                    apiDto.getName());
            uniqueEndpointURLs.addAll(productionEndpoints); // Remove duplicate and append result
            uniqueEndpointURLs.addAll(sandboxEndpoints);

            for (String url : uniqueEndpointURLs) {
                JsonArray certificateListOfUrl = getEndpointCertificateContentAndMetaData(tenantId, url,
                        endpointCertsDirectoryPath);
                endpointCertificatesDetails.addAll(certificateListOfUrl);
            }
            if (endpointCertificatesDetails.size() > 0) {
                CommonUtil.writeDtoToFile(endpointCertsDirectoryPath + ImportExportConstants.ENDPOINTS_CERTIFICATE_FILE,
                        exportFormat, ImportExportConstants.TYPE_ENDPOINT_CERTIFICATES, endpointCertificatesDetails);
            } else if (log.isDebugEnabled()) {
                log.debug("No endpoint certificates available for API: " + apiDto.getName() + StringUtils.SPACE
                        + APIConstants.API_DATA_VERSION + ": " + apiDto.getVersion()
                        + ". Skipping certificate export.");
            }
        } catch (JSONException e) {
            throw new APIImportExportException(
                    "Error in converting Endpoint config to JSON object in API: " + apiDto.getName(), e);
        } catch (IOException e) {

            throw new APIImportExportException(
                    "Error while retrieving saving endpoint certificate details for API: " + apiDto.getName()
                            + " as YAML", e);
        }
    }

    /**
     * Get Endpoint Certificate MetaData and Certificate detail and build JSON Array.
     *
     * @param tenantId          Tenant id of the user
     * @param url               Url of the endpoint
     * @param certDirectoryPath Directory path to export the certificates
     * @return JSON Array of certificate details
     * @throws APIImportExportException If an error occurs while retrieving endpoint certificate metadata and content
     */
    private static JsonArray getEndpointCertificateContentAndMetaData(int tenantId, String url,
            String certDirectoryPath) throws APIImportExportException {

        List<CertificateMetadataDTO> certificateMetadataDTOS;
        CertificateManager certificateManager = CertificateManagerImpl.getInstance();

        try {
            certificateMetadataDTOS = certificateManager.getCertificates(tenantId, null, url);
        } catch (APIManagementException e) {
            throw new APIImportExportException(
                    "Error retrieving certificate meta data. For tenantId: " + tenantId + " hostname: " + url, e);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray certificatesList = new JsonArray();
        certificateMetadataDTOS.forEach(metadataDTO -> {
            try (ByteArrayInputStream certificate = certificateManager.getCertificateContent(metadataDTO.getAlias())) {
                byte[] certificateContent = IOUtils.toByteArray(certificate);
                String certificateContentEncoded = APIConstants.BEGIN_CERTIFICATE_STRING
                        .concat(new String(Base64.encodeBase64(certificateContent))).concat("\n")
                        .concat(APIConstants.END_CERTIFICATE_STRING);
                CommonUtil.writeFile(certDirectoryPath + File.separator + metadataDTO.getAlias() + ".crt",
                        certificateContentEncoded);
                // Add the file name to the Certificate Metadata
                JsonObject modifiedCertificateMetadata = (JsonObject) gson.toJsonTree(metadataDTO);
                modifiedCertificateMetadata
                        .addProperty(ImportExportConstants.CERTIFICATE_FILE, metadataDTO.getAlias() + ".crt");
                certificatesList.add(modifiedCertificateMetadata);
            } catch (APIManagementException e) {
                log.error("Error retrieving certificate content. For tenantId: " + tenantId + " hostname: " + url
                        + " alias: " + metadataDTO.getAlias(), e);
            } catch (IOException e) {
                log.error("Error while converting certificate content to Byte Array. For tenantId: " + tenantId
                        + " hostname: " + url + " alias: " + metadataDTO.getAlias(), e);
            } catch (APIImportExportException e) {
                log.error("Error while writing the certificate content. For tenantId: " + tenantId + " hostname: " + url
                        + " alias: " + metadataDTO.getAlias(), e);
            }
        });
        return certificatesList;
    }

    /**
     * Get endpoint url list from endpoint config.
     *
     * @param endpointConfig JSON converted endpoint config
     * @param type           End point type - production/sandbox
     * @return List of host names
     */
    private static List<String> getEndpointURLs(JSONObject endpointConfig, String type, String apiName)
            throws APIImportExportException {

        List<String> urls = new ArrayList<>();
        if (endpointConfig != null) {
            try {
                Object item;
                item = endpointConfig.get(type);
                if (item instanceof JSONArray) {
                    JSONArray endpointsJSON = new JSONArray(endpointConfig.getJSONArray(type).toString());
                    for (int i = 0; i < endpointsJSON.length(); i++) {
                        try {
                            String urlValue = endpointsJSON.getJSONObject(i).get(APIConstants.API_DATA_URL).toString();
                            urls.add(urlValue);
                        } catch (JSONException ex) {
                            log.error("Endpoint URL extraction from endpoints JSON object failed in API: " + apiName,
                                    ex);
                        }
                    }
                } else if (item instanceof JSONObject) {
                    JSONObject endpointJSON = new JSONObject(endpointConfig.getJSONObject(type).toString());
                    try {
                        String urlValue = endpointJSON.get(APIConstants.API_DATA_URL).toString();
                        urls.add(urlValue);
                    } catch (JSONException ex) {
                        log.error("Endpoint URL extraction from endpoint JSON object failed in API: " + apiName, ex);
                    }
                }
            } catch (JSONException ex) {
                throw new APIImportExportException("Endpoint type: " + type + " not found in API: " + apiName);
            }
        }
        return urls;
    }

    /**
     * Retrieve meta information of the API to export and store those in the archive directory.
     * URL template information are stored in swagger.json definition while rest of the required
     * data are in api.json
     *
     * @param archivePath    Folder path to export meta information to export
     * @param apiDtoToReturn API DTO to be exported
     * @param exportFormat   Export format of file
     * @param apiProvider    API Provider
     * @param apiIdentifier  API Identifier
     * @throws APIImportExportException If an error occurs while exporting meta information
     */
    public static void addAPIMetaInformationToArchive(String archivePath, APIDTO apiDtoToReturn,
            ExportFormat exportFormat, APIProvider apiProvider, APIIdentifier apiIdentifier)
            throws APIImportExportException {

        CommonUtil.createDirectory(archivePath + File.separator + ImportExportConstants.DEFINITIONS_DIRECTORY);

        try {
            // If a web socket API is exported, it does not contain a swagger file.
            // Therefore swagger export is only required for REST or SOAP based APIs
            String apiType = apiDtoToReturn.getType().toString();
            if (!APIConstants.APITransportType.WS.toString().equalsIgnoreCase(apiType)) {
                // For Graphql APIs, the graphql schema definition should be exported.
                if (StringUtils.equals(apiType, APIConstants.APITransportType.GRAPHQL.toString())) {
                    String schemaContent = apiProvider.getGraphqlSchema(apiIdentifier);
                    CommonUtil.writeFile(archivePath + ImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION,
                            schemaContent);
                }
                // For GraphQL APIs, swagger export is not needed
                if (!APIConstants.APITransportType.GRAPHQL.toString().equalsIgnoreCase(apiType)) {
                    String formattedSwaggerJson = RestApiCommonUtil.retrieveSwaggerDefinition(
                            APIMappingUtil.fromDTOtoAPI(apiDtoToReturn, apiDtoToReturn.getProvider()), apiProvider);
                    CommonUtil.writeToYamlOrJson(archivePath + ImportExportConstants.SWAGGER_DEFINITION_LOCATION, exportFormat,
                            formattedSwaggerJson);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Meta information retrieved successfully for API: " + apiDtoToReturn.getName()
                            + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiDtoToReturn.getVersion());
                }
            }
            CommonUtil.writeDtoToFile(archivePath + ImportExportConstants.API_FILE_LOCATION, exportFormat,
                    ImportExportConstants.TYPE_API, apiDtoToReturn);
        } catch (APIManagementException e) {
            throw new APIImportExportException(
                    "Error while retrieving Swagger definition for API: " + apiDtoToReturn.getName() + StringUtils.SPACE
                            + APIConstants.API_DATA_VERSION + ": " + apiDtoToReturn.getVersion(), e);
        } catch (IOException e) {
            throw new APIImportExportException(
                    "Error while retrieving saving as YAML for API: " + apiDtoToReturn.getName() + StringUtils.SPACE
                            + APIConstants.API_DATA_VERSION + ": " + apiDtoToReturn.getVersion(), e);
        }
    }

    /**
     * Retrieve Mutual SSL related certificates and store those in the archive directory
     *
     * @param archivePath  Folder path to export client certificates
     * @param identifier   Identifier
     * @param tenantId     Tenant id of the user
     * @param provider     Api Provider
     * @param exportFormat Export format of file
     * @throws APIImportExportException If an error occurs when writing to file or retrieving certificate metadata
     */
    public static void addClientCertificatesToArchive(String archivePath, Identifier identifier, int tenantId,
            APIProvider provider, ExportFormat exportFormat) throws APIImportExportException {

        List<ClientCertificateDTO> certificateMetadataDTOs;
        try {
            if (identifier instanceof APIProductIdentifier) {
                certificateMetadataDTOs = provider
                        .searchClientCertificates(tenantId, null, (APIProductIdentifier) identifier);
            } else {
                certificateMetadataDTOs = provider.searchClientCertificates(tenantId, null, (APIIdentifier) identifier);
            }
            if (!certificateMetadataDTOs.isEmpty()) {
                String clientCertsDirectoryPath =
                        archivePath + File.separator + ImportExportConstants.CLIENT_CERTIFICATES_DIRECTORY;
                CommonUtil.createDirectory(clientCertsDirectoryPath);

                JsonArray certificateList = getClientCertificateContentAndMetaData(certificateMetadataDTOs,
                        clientCertsDirectoryPath);

                if (certificateList.size() > 0) {
                    CommonUtil.writeDtoToFile(clientCertsDirectoryPath + ImportExportConstants.CLIENT_CERTIFICATE_FILE,
                            exportFormat, ImportExportConstants.TYPE_CLIENT_CERTIFICATES, certificateList);
                }
            }
        } catch (IOException e) {
            throw new APIImportExportException("Error while saving as YAML or JSON", e);
        } catch (APIManagementException e) {
            throw new APIImportExportException(
                    "Error retrieving certificate meta data. tenantId [" + tenantId + "] api [" + tenantId + "]", e);
        }
    }

    /**
     * Get Client Certificate MetaData and Certificate detail and build JSON list.
     *
     * @param clientCertificateDTOs client certificates list DTOs
     * @param certDirectoryPath     directory path to export the certificates
     * @return list of certificate detail JSON objects
     */
    private static JsonArray getClientCertificateContentAndMetaData(List<ClientCertificateDTO> clientCertificateDTOs,
            String certDirectoryPath) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray certificatesList = new JsonArray();
        clientCertificateDTOs.forEach(metadataDTO -> {
            try {
                String certificateContent = metadataDTO.getCertificate();
                String certificateContentEncoded = APIConstants.BEGIN_CERTIFICATE_STRING.concat(certificateContent)
                        .concat("\n").concat(APIConstants.END_CERTIFICATE_STRING);
                CommonUtil.writeFile(certDirectoryPath + File.separator + metadataDTO.getAlias() + ".crt",
                        certificateContentEncoded);
                // Add the file name to the Certificate Metadata
                metadataDTO.setCertificate(metadataDTO.getAlias() + ".crt");
                JsonObject certificateMetadata = (JsonObject) gson.toJsonTree(metadataDTO);
                certificatesList.add(certificateMetadata);
            } catch (APIImportExportException e) {
                log.error("Error while writing the certificate content. For alias: " + metadataDTO.getAlias(), e);
            }
        });
        return certificatesList;
    }

    /**
     * Retrieve meta information of the API Product to export and store it in the archive directory.
     * URL template information are stored in swagger.json definition while rest of the required
     * data are in api.json
     *
     * @param archivePath           Folder path to export meta information
     * @param apiProductDtoToReturn APIProductDTO to be exported
     * @param exportFormat          Export format of file
     * @param apiProvider           API Provider
     * @throws APIImportExportException If an error occurs while exporting meta information
     */
    public static void addAPIProductMetaInformationToArchive(String archivePath, APIProductDTO apiProductDtoToReturn,
            ExportFormat exportFormat, APIProvider apiProvider) throws APIImportExportException {

        CommonUtil.createDirectory(archivePath + File.separator + ImportExportConstants.DEFINITIONS_DIRECTORY);

        try {
            String formattedSwaggerJson = apiProvider.getAPIDefinitionOfAPIProduct(
                    APIMappingUtil.fromDTOtoAPIProduct(apiProductDtoToReturn, apiProductDtoToReturn.getProvider()));
            CommonUtil.writeToYamlOrJson(archivePath + ImportExportConstants.SWAGGER_DEFINITION_LOCATION, exportFormat,
                    formattedSwaggerJson);

            if (log.isDebugEnabled()) {
                log.debug(
                        "Meta information retrieved successfully for API Product: " + apiProductDtoToReturn.getName());
            }
            CommonUtil.writeDtoToFile(archivePath + ImportExportConstants.API_FILE_LOCATION, exportFormat,
                    ImportExportConstants.TYPE_API_PRODUCT, apiProductDtoToReturn);
        } catch (APIManagementException e) {
            throw new APIImportExportException(
                    "Error while retrieving Swagger definition for API Product: " + apiProductDtoToReturn.getName(), e);
        } catch (IOException e) {
            throw new APIImportExportException(
                    "Error while saving as YAML for API Product: " + apiProductDtoToReturn.getName(), e);
        }
    }

    /**
     * Retrieve dependent APIs by checking the resources of the API Product and store those in the archive directory.
     *
     * @param archivePath           Temp location to save the API artifacts
     * @param apiProductDtoToReturn API Product DTO which the resources should be considered
     * @param userName              User name of the requester
     * @param provider              API Product Provider
     * @param exportFormat          Export format of the API meta data, could be yaml or json
     * @param isStatusPreserved     Whether API status is preserved while export
     * @throws APIImportExportException If an error occurs while creating the directory or extracting the archive
     * @throws APIManagementException   If an error occurs while retrieving API related resources
     */
    public static void addDependentAPIsToArchive(String archivePath, APIProductDTO apiProductDtoToReturn,
                                                 ExportFormat exportFormat, APIProvider provider, String userName,
                                                 Boolean isStatusPreserved, boolean preserveDocs,
                                                 boolean preserveCredentials) throws APIImportExportException, APIManagementException {

        String apisDirectoryPath = archivePath + File.separator + ImportExportConstants.APIS_DIRECTORY;
        CommonUtil.createDirectory(apisDirectoryPath);

        List<ProductAPIDTO> apisList = apiProductDtoToReturn.getApis();
        for (ProductAPIDTO productAPIDTO : apisList) {
            String apiProductRequesterDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            API api = provider.getAPIbyUUID(productAPIDTO.getApiId(), apiProductRequesterDomain);
            APIDTO apiDtoToReturn = APIMappingUtil.fromAPItoDTO(api, preserveCredentials, null);
            File dependentAPI = exportApi(provider, api.getId(), apiDtoToReturn, userName, exportFormat,
                    isStatusPreserved, preserveDocs);
            CommonUtil.extractArchive(dependentAPI, apisDirectoryPath);
        }
    }

}
