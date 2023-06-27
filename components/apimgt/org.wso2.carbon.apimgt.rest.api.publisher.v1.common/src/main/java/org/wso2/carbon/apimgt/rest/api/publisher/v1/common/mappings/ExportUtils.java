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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManager;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.util.SequenceUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AdvertiseInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLQueryComplexityInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ProductAPIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyListDTO;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.APIConstants.API_DATA_PRODUCTION_ENDPOINTS;
import static org.wso2.carbon.apimgt.impl.APIConstants.API_DATA_SANDBOX_ENDPOINTS;
import static org.wso2.carbon.apimgt.impl.APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE;
import static org.wso2.carbon.apimgt.impl.APIConstants.AsyncApi.ASYNC_DEFAULT_SUBSCRIBER;

/**
 * This class uses for Export API functionality.
 */
public class ExportUtils {

    private static final Log log = LogFactory.getLog(ExportUtils.class);
    private static final String IN = "in";
    private static final String OUT = "out";
    private static final String SOAPTOREST = "SoapToRest";
    private static String migrationEnabled = System.getProperty(APIConstants.MIGRATE);

    /**
     * Validate name, version and provider before exporting an API/API Product.
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
     * @param apiProvider          API Provider
     * @param apiIdentifier        API Identifier
     * @param apiDtoToReturn       API DTO
     * @param userName             Username
     * @param exportFormat         Format of output documents. Can be YAML or JSON
     * @param preserveStatus       Preserve API status on export
     * @param preserveDocs         Preserve documentation on Export.
     * @param originalDevPortalUrl Original DevPortal URL (redirect URL) for the original Store
     *                             (This is used for advertise only APIs).
     * @param organization          Organization
     * @return
     * @throws APIManagementException If an error occurs while getting governance registry
     */
    public static File exportApi(APIProvider apiProvider, APIIdentifier apiIdentifier, APIDTO apiDtoToReturn, API api,
                                 String userName, ExportFormat exportFormat, boolean preserveStatus,
                                 boolean preserveDocs, String originalDevPortalUrl, String organization)
            throws APIManagementException, APIImportExportException {

        int tenantId;
        String currentApiUuid;

        // If explicitly advertise only property has been specified as true, make it true and update the API DTO.
        if (StringUtils.isNotBlank(originalDevPortalUrl)) {
            setAdvertiseOnlySpecificPropertiesToDTO(apiDtoToReturn, originalDevPortalUrl);
        }

        // Resolve whether an API or a corresponding revision
        APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(apiDtoToReturn.getId());
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            currentApiUuid = apiRevision.getApiUUID();
        } else {
            currentApiUuid = apiDtoToReturn.getId();
        }

        // Create temp location for storing API data
        File exportFolder = CommonUtil.createTempDirectory(apiIdentifier);
        String exportAPIBasePath = exportFolder.toString();
        String archivePath = exportAPIBasePath
                .concat(File.separator + apiIdentifier.getApiName() + "-" + apiIdentifier.getVersion());
        tenantId = APIUtil.getTenantId(userName);

        CommonUtil.createDirectory(archivePath);
        if (preserveDocs) {
            addThumbnailToArchive(archivePath, apiIdentifier, apiProvider);
            addDocumentationToArchive(archivePath, apiIdentifier, exportFormat, apiProvider,
                    APIConstants.API_IDENTIFIER_TYPE);
        } else {
            if (StringUtils.equals(apiDtoToReturn.getType().toString().toLowerCase(),
                    APIConstants.API_TYPE_SOAPTOREST.toLowerCase())) {
                addSOAPToRESTMediationToArchive(archivePath, api);
            }
        }

        if (StringUtils
                .equals(apiDtoToReturn.getType().toString().toLowerCase(), APIConstants.API_TYPE_SOAP.toLowerCase())
                && preserveDocs) {
            addWSDLtoArchive(archivePath, apiIdentifier, apiProvider);
        } else if (log.isDebugEnabled()) {
            log.debug("No WSDL URL found for API: " + apiIdentifier + ". Skipping WSDL export.");
        }

        // Set API status to created if the status is not preserved
        if (!preserveStatus) {
            apiDtoToReturn.setLifeCycleStatus(APIConstants.CREATED);
        }
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        addOperationPoliciesToArchive(archivePath, tenantDomain, exportFormat, apiProvider,
                api, currentApiUuid);
        addGatewayEnvironmentsToArchive(archivePath, apiDtoToReturn.getId(), exportFormat, apiProvider);

        if (migrationEnabled != null) {
            addRuntimeSequencesToArchive(archivePath, api);
        }
        if (!ImportUtils.isAdvertiseOnlyAPI(apiDtoToReturn)) {
            addEndpointCertificatesToArchive(archivePath, apiDtoToReturn, tenantId, exportFormat);
            // Export mTLS authentication related certificates
            if (log.isDebugEnabled()) {
                log.debug("Mutual SSL enabled. Exporting client certificates.");
            }
            addClientCertificatesToArchive(archivePath, apiIdentifier, tenantId, apiProvider, exportFormat,
                    organization);
        }
        addAPIMetaInformationToArchive(archivePath, apiDtoToReturn, exportFormat, apiProvider, apiIdentifier,
                organization, currentApiUuid);
        CommonUtil.archiveDirectory(exportAPIBasePath);
        FileUtils.deleteQuietly(new File(exportAPIBasePath));
        return new File(exportAPIBasePath + APIConstants.ZIP_FILE_EXTENSION);
    }

    /**
     * Set the properties specific to advertise only APIs
     *
     * @param apiDto               API DTO to export
     * @param originalDevPortalUrl Original DevPortal URL (redirect URL) for the original Store
     *                             (This is used for advertise only APIs).
     */
    private static void setAdvertiseOnlySpecificPropertiesToDTO(APIDTO apiDto, String originalDevPortalUrl) {
        AdvertiseInfoDTO advertiseInfoDTO = new AdvertiseInfoDTO();
        advertiseInfoDTO.setAdvertised(Boolean.TRUE);
        // Change owner to original provider as the provider will be overriding after importing
        advertiseInfoDTO.setApiOwner(apiDto.getProvider());
        advertiseInfoDTO.setOriginalDevPortalUrl(originalDevPortalUrl);
        apiDto.setAdvertiseInfo(advertiseInfoDTO);
        apiDto.setMediationPolicies(null);
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
     * @param organization          Organization Identifier
     * @return
     * @throws APIManagementException If an error occurs while getting governance registry
     */
    public static File exportApiProduct(APIProvider apiProvider, APIProductIdentifier apiProductIdentifier,
            APIProductDTO apiProductDtoToReturn, String userName, ExportFormat exportFormat, Boolean preserveStatus,
            boolean preserveDocs, boolean preserveCredentials, String organization)
            throws APIManagementException, APIImportExportException {

        int tenantId = 0;
        // Create temp location for storing API Product data
        File exportFolder = CommonUtil.createTempDirectory(apiProductIdentifier);
        String exportAPIBasePath = exportFolder.toString();
        String archivePath = exportAPIBasePath
                .concat(File.separator + apiProductIdentifier.getName() + "-" + apiProductIdentifier.getVersion());
        tenantId = APIUtil.getTenantId(userName);

        CommonUtil.createDirectory(archivePath);

        if (preserveDocs) {
            addThumbnailToArchive(archivePath, apiProductIdentifier, apiProvider);
            addDocumentationToArchive(archivePath, apiProductIdentifier, exportFormat, apiProvider,
                    APIConstants.API_PRODUCT_IDENTIFIER_TYPE);
        }
        // Set API Product status to created if the status is not preserved
        if (!preserveStatus) {
            apiProductDtoToReturn.setState(APIConstants.CREATED);
        }
        addGatewayEnvironmentsToArchive(archivePath, apiProductDtoToReturn.getId(), exportFormat, apiProvider);
        addAPIProductMetaInformationToArchive(archivePath, apiProductDtoToReturn, exportFormat,
                apiProvider, organization);
        addDependentAPIsToArchive(archivePath, apiProductDtoToReturn, exportFormat, apiProvider, userName,
                Boolean.TRUE, preserveDocs, preserveCredentials, organization);

        // Export mTLS authentication related certificates
        if (log.isDebugEnabled()) {
            log.debug("Mutual SSL enabled. Exporting client certificates.");
        }
        addClientCertificatesToArchive(archivePath, apiProductIdentifier, tenantId, apiProvider, exportFormat,
                organization);

        CommonUtil.archiveDirectory(exportAPIBasePath);
        FileUtils.deleteQuietly(new File(exportAPIBasePath));
        return new File(exportAPIBasePath + APIConstants.ZIP_FILE_EXTENSION);
    }

    /**
     * Retrieve thumbnail image for the exporting API or API Product and store it in the archive directory.
     *
     * @param archivePath File path to export the thumbnail image
     * @param identifier  ID of the requesting API or API Product
     * @param apiProvider API Provider
     * @throws APIImportExportException If an error occurs while retrieving image from the registry or
     *                                  storing in the archive directory
     */
    public static void addThumbnailToArchive(String archivePath, Identifier identifier, APIProvider apiProvider)
            throws APIImportExportException, APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String localImagePath = archivePath + File.separator + ImportExportConstants.IMAGE_RESOURCE;
        try {
            ResourceFile thumbnailResource = apiProvider.getIcon(identifier.getUUID(), tenantDomain);
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
     * Retrieve SOAP to REST mediation logic for the exporting API and store it in the archive directory.
     *
     * @param archivePath File path to export the SOAPToREST mediation logic
     * @param api         API
     * @throws APIImportExportException If an error occurs while retrieving image from the registry or
     *                                  storing in the archive directory
     */
    public static void addSOAPToRESTMediationToArchive(String archivePath, API api)
            throws APIImportExportException, APIManagementException {

        String sequencePathInArchive = archivePath + File.separator + SOAPTOREST;
        CommonUtil.createDirectory(sequencePathInArchive);

        writeSOAPToRESTSequencesToArchive(api, sequencePathInArchive, IN);
        writeSOAPToRESTSequencesToArchive(api, sequencePathInArchive, OUT);
    }

    /**
     * Retrieve SOAP to REST mediation logic for the exporting API for a particular type (in/out) and store it
     * in the archive directory.
     *
     * @param api                   API
     * @param sequencePathInArchive Path to the SOAP to REST sequences in the archive
     * @param type                  Seqeunce type
     * @throws APIManagementException   If an error occurs while reading/writing SOAP to REST sequences
     * @throws APIImportExportException If an error occurs while creating the directory
     */
    private static void writeSOAPToRESTSequencesToArchive(API api, String sequencePathInArchive, String type)
            throws APIManagementException, APIImportExportException {
        String resourcePolicy = SequenceUtils.getRestToSoapConvertedSequence(api, type);
        ResourcePolicyListDTO resourcePolicyInListDTO = APIMappingUtil.fromResourcePolicyStrToDTO(resourcePolicy);
        String individualSequencePathInArchive = sequencePathInArchive + File.separator + type;
        CommonUtil.createDirectory(individualSequencePathInArchive);
        for (ResourcePolicyInfoDTO resourcePolicyInfoDTO : resourcePolicyInListDTO.getList()) {
            String sequenceContent = resourcePolicyInfoDTO.getContent();
            String sequenceName = resourcePolicyInfoDTO.getResourcePath() + "_" + resourcePolicyInfoDTO.getHttpVerb();
            writeSequenceToArchive(sequenceContent, individualSequencePathInArchive, sequenceName);
        }
    }

    /**
     * Retrieve documentation for the exporting API or API Product and store it in the archive directory.
     * FILE, INLINE, MARKDOWN and URL documentations are handled.
     *
     * @param archivePath  File path to export the documents
     * @param identifier   ID of the requesting API or API Product
     * @param exportFormat Format for export
     * @param apiProvider  API Provider
     * @param type         Type of the Project (whether an API or an API Product)
     * @throws APIImportExportException If an error occurs while retrieving documents from the
     *                                  registry or storing in the archive directory
     * @throws APIManagementException   If an error occurs while retrieving document details
     */
    public static void addDocumentationToArchive(String archivePath, Identifier identifier,
                                                 ExportFormat exportFormat, APIProvider apiProvider, String type)
            throws APIImportExportException, APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        List<Documentation> docList = apiProvider.getAllDocumentation(identifier.getUUID(), tenantDomain);
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
                    InputStream inputStream = null;
                    String localFileName = null;
                    String individualDocDirectoryPath =
                            docDirectoryPath + File.separator + cleanFolderName(individualDocument.getName());
                    CommonUtil.createDirectory(individualDocDirectoryPath);
                    DocumentationContent documentationContent =
                            apiProvider.getDocumentationContent(identifier.getUUID(), doc.getId(), tenantDomain);
                    if (documentationContent != null) {
                        if (Documentation.DocumentSourceType.FILE.toString().equalsIgnoreCase(sourceType)) {
                            localFileName = individualDocument.getFilePath().substring(
                                    individualDocument.getFilePath().lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                            inputStream = documentationContent.getResourceFile().getContent();
                            individualDocument.setFilePath(localFileName);
                        } else if (Documentation.DocumentSourceType.INLINE.toString().equalsIgnoreCase(sourceType)
                                || Documentation.DocumentSourceType.MARKDOWN.toString().equalsIgnoreCase(sourceType)) {
                            // Inline/Markdown content file name would be same as the documentation name
                            localFileName = individualDocument.getName();
                            inputStream = new ByteArrayInputStream(documentationContent.getTextContent().getBytes());
                        }
                    }

                    CommonUtil.writeDtoToFile(individualDocDirectoryPath + ImportExportConstants.DOCUMENT_FILE_NAME,
                            exportFormat,
                            ImportExportConstants.TYPE_DOCUMENTS,
                            DocumentationMappingUtil.fromDocumentationToDTO(individualDocument));

                    if (inputStream != null) {
                        // Write content for Inline/Markdown/File type documentations only
                        // Check whether resource exists in the registry
                        try (OutputStream outputStream = new FileOutputStream(
                                individualDocDirectoryPath + File.separator + localFileName);) {
                            IOUtils.copy(inputStream, outputStream);
                        }
                    } else {
                        // Log error and avoid throwing as we give capability to export document artifact without
                        // the content if does not exists
                        log.error("Documentation resource for API/API Product: " + identifier.getName()
                                + " not found in " + resourcePath);
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
     * @throws APIImportExportException If an error occurs while retrieving WSDL from the registry or
     *                                  storing in the archive directory
     */
    public static void addWSDLtoArchive(String archivePath, APIIdentifier apiIdentifier, APIProvider apiProvider)
            throws APIImportExportException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

        String wsdlPath =
                APIConstants.API_WSDL_RESOURCE_LOCATION + apiIdentifier.getProviderName() + "--" + apiIdentifier
                        .getApiName() + apiIdentifier.getVersion() + APIConstants.WSDL_FILE_EXTENSION;
        try {
            ResourceFile wsdlResource = apiProvider.getWSDL(apiIdentifier.getUUID(), tenantDomain);
            if (wsdlResource != null) {
                CommonUtil.createDirectory(archivePath + File.separator + "WSDL");
                String wsdlResourceExtension = APIConstants.APPLICATION_ZIP.equals(wsdlResource.getContentType()) ?
                        APIConstants.ZIP_FILE_EXTENSION : APIConstants.WSDL_FILE_EXTENSION;
                try (InputStream wsdlStream = wsdlResource.getContent();
                     OutputStream outputStream = new FileOutputStream(
                             archivePath + File.separator + "WSDL" + File.separator + apiIdentifier.getApiName()
                                     + "-" + apiIdentifier.getVersion() + wsdlResourceExtension)) {
                    IOUtils.copy(wsdlStream, outputStream);
                    if (log.isDebugEnabled()) {
                        log.debug("WSDL file: " + wsdlPath + " retrieved successfully");
                    }
                }
            } else if (log.isDebugEnabled()) {
                log.debug("WSDL resource does not exists in path: " + wsdlPath + ". Skipping WSDL export.");
            }
        } catch (IOException | APIManagementException e) {
            throw new APIImportExportException("I/O error while writing WSDL: " + wsdlPath + " to file", e);
        }
    }

    /**
     * Write the sequence to API archive.
     *
     * @param mediation                    Mediation content
     * @param individualSequenceExportPath Path to export the mediation sequence
     * @param mediationName                Name of the mediation policy
     * @throws APIManagementException If an error occurs while writing the mediation policy to file
     */
    private static void writeSequenceToArchive(String mediation, String individualSequenceExportPath,
            String mediationName) throws APIManagementException {
        String mediationFilePath = individualSequenceExportPath + File.separator + mediationName + APIConstants.DOT
                + APIConstants.XML_DOC_EXTENSION;
        if (StringUtils.isNotBlank(mediation) && !CommonUtil.checkFileExistence(mediationFilePath)) {
            try (OutputStream outputStream = new FileOutputStream(mediationFilePath);
                    InputStream fileInputStream = new ByteArrayInputStream(mediation.getBytes())) {
                IOUtils.copy(fileInputStream, outputStream);
            } catch (IOException e) {
                throw new APIManagementException(
                        "Error while writing the mediation sequence" + mediationName + "to file", e);
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
        List<String> productionFailovers;
        List<String> sandboxFailovers;
        Set<String> uniqueEndpointURLs = new HashSet<>();
        JsonArray endpointCertificatesDetails = new JsonArray();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String endpointConfigString = gson.toJson(apiDto.getEndpointConfig());
        String endpointCertsDirectoryPath =
                archivePath + File.separator + ImportExportConstants.ENDPOINT_CERTIFICATES_DIRECTORY;
        CommonUtil.createDirectory(endpointCertsDirectoryPath);

        if (StringUtils.isEmpty(endpointConfigString) || "null".equals(endpointConfigString)) {
            if (log.isDebugEnabled()) {
                log.debug("Endpoint Details are empty for API: " + apiDto.getName() + StringUtils.SPACE
                        + APIConstants.API_DATA_VERSION + ": " + apiDto.getVersion());
            }
            return;
        }
        try {
            JSONTokener tokener = new JSONTokener(endpointConfigString);
            JSONObject endpointConfig = new JSONObject(tokener);
            productionEndpoints = getEndpointURLs(endpointConfig, API_DATA_PRODUCTION_ENDPOINTS,
                    apiDto.getName());
            sandboxEndpoints = getEndpointURLs(endpointConfig, API_DATA_SANDBOX_ENDPOINTS,
                    apiDto.getName());
            productionFailovers = getEndpointURLs(endpointConfig, APIConstants.ENDPOINT_PRODUCTION_FAILOVERS,
                    apiDto.getName());
            sandboxFailovers = getEndpointURLs(endpointConfig, APIConstants.ENDPOINT_SANDBOX_FAILOVERS,
                    apiDto.getName());
            uniqueEndpointURLs.addAll(productionEndpoints); // Remove duplicate and append result
            uniqueEndpointURLs.addAll(sandboxEndpoints);
            uniqueEndpointURLs.addAll(productionFailovers);
            uniqueEndpointURLs.addAll(sandboxFailovers);

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
     * Retrieve the operation policies and store those in the archive directory.
     *
     * @param archivePath    File path to export the endpoint certificates
     * @param exportFormat   Export format of file
     * @param apiProvider    API Provider
     * @param currentApiUuid UUID of the API/ API Product
     * @throws APIManagementException If an error occurs while exporting operation policies
     */
    public static void addOperationPoliciesToArchive(String archivePath, String tenantDomain, ExportFormat exportFormat,
            APIProvider apiProvider, API api, String currentApiUuid) throws APIManagementException {
        try {
            CommonUtil.createDirectory(archivePath + File.separator + ImportExportConstants.POLICIES_DIRECTORY);
            Set<URITemplate> uriTemplates = api.getUriTemplates();
            Set<String> exportedPolicies = new HashSet<>();
            boolean mediationPoliciesLoaded = false;
            for (URITemplate uriTemplate : uriTemplates) {
                List<OperationPolicy> operationPolicies = uriTemplate.getOperationPolicies();
                if (operationPolicies != null && !operationPolicies.isEmpty()) {
                    for (OperationPolicy policy : operationPolicies) {
                        if (!exportedPolicies.contains(policy.getPolicyName() + "_" + policy.getPolicyVersion())) {
                            String policyFileName = APIUtil.getOperationPolicyFileName(policy.getPolicyName(),
                                    policy.getPolicyVersion());
                            if (policy.getPolicyId() != null) {
                                OperationPolicyData policyData =
                                        apiProvider.getAPISpecificOperationPolicyByPolicyId(policy.getPolicyId(),
                                                currentApiUuid, tenantDomain, true);
                                if (policyData != null) {
                                    exportPolicyData(policyFileName, policyData, archivePath, exportFormat);
                                    exportedPolicies.add(policy.getPolicyName() + "_" + policy.getPolicyVersion());
                                }
                            } else {
                                // This path is to handle migrated APIs with mediation policies attached
                                if (APIUtil.isSequenceDefined(api.getInSequence())
                                        || APIUtil.isSequenceDefined(api.getOutSequence())
                                        || APIUtil.isSequenceDefined(api.getFaultSequence())) {
                                    log.info("Mediation policy " + policy.getPolicyName()
                                            + " will be converted to an operation policy");
                                    if (!mediationPoliciesLoaded) {
                                        apiProvider.loadMediationPoliciesToAPI(api, tenantDomain);
                                        mediationPoliciesLoaded = true;
                                    }

                                    OperationPolicyData policyData = APIUtil.getPolicyDataForMediationFlow(api,
                                            policy.getDirection(), tenantDomain);
                                    if (policyData != null) {
                                        exportPolicyData(policyFileName, policyData, archivePath, exportFormat);
                                        exportedPolicies.add(policy.getPolicyName() + "_" + policy.getPolicyVersion());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if ((APIUtil.isSequenceDefined(api.getInSequence())
                    || APIUtil.isSequenceDefined(api.getOutSequence())
                    || APIUtil.isSequenceDefined(api.getFaultSequence())) && migrationEnabled == null) {
                api.setInSequence(null);
                api.setInSequenceMediation(null);
                api.setOutSequence(null);
                api.setOutSequenceMediation(null);
                api.setFaultSequence(null);
                api.setFaultSequenceMediation(null);
            }
        } catch (IOException | APIImportExportException e) {
            throw new APIManagementException("Error while adding operation policy details for API: " + currentApiUuid,
                    e);
        }
    }

    public static void exportPolicyData(String policyFileName, OperationPolicyData policyData, String archivePath,
                                        ExportFormat exportFormat) throws APIImportExportException, IOException {

        String policyName = archivePath + File.separator + ImportExportConstants.POLICIES_DIRECTORY + File.separator +
                policyFileName;
        // Policy specification and definition will have the same name
        if (policyData.getSpecification() != null) {
            CommonUtil.writeDtoToFile(policyName, exportFormat, ImportExportConstants.TYPE_POLICY_SPECIFICATION,
                    policyData.getSpecification());
        }
        if (policyData.getSynapsePolicyDefinition() != null) {
            CommonUtil.writeFile(policyName + APIConstants.SYNAPSE_POLICY_DEFINITION_EXTENSION,
                    policyData.getSynapsePolicyDefinition().getContent());
        }
        if (policyData.getCcPolicyDefinition() != null) {
            CommonUtil.writeFile(policyName + APIConstants.CC_POLICY_DEFINITION_EXTENSION,
                    policyData.getCcPolicyDefinition().getContent());
        }
    }

    /**
     * Retrieve the deployed gateway environments and store those in the archive directory.
     *
     * @param archivePath  File path to export the endpoint certificates
     * @param apiID        UUID of the API/ API Product
     * @param exportFormat Export format of file
     * @param apiProvider  API Provider
     * @throws APIImportExportException If an error occurs while exporting gateway environments
     */
    public static void addGatewayEnvironmentsToArchive(String archivePath, String apiID,
                                                       ExportFormat exportFormat, APIProvider apiProvider)
            throws APIManagementException {

        try {
            List<APIRevisionDeployment> deploymentsList = apiProvider.getAPIRevisionDeploymentList(apiID);
            JsonArray deploymentsArray = new JsonArray();
            for (APIRevisionDeployment deployment : deploymentsList) {
                JsonObject deploymentObject = new JsonObject();
                // Do not set vhost in deployment environment file when export API (or API Project)
                // So when importing the exported API, the default vhost of the new environment is selected.
                deploymentObject.addProperty(ImportExportConstants.DEPLOYMENT_NAME, deployment.getDeployment());
                deploymentObject.addProperty(ImportExportConstants.DISPLAY_ON_DEVPORTAL_OPTION,
                        deployment.isDisplayOnDevportal());
                deploymentsArray.add(deploymentObject);
            }
            if (deploymentsArray.size() > 0) {
                CommonUtil.writeDtoToFile(archivePath + ImportExportConstants.DEPLOYMENT_INFO_LOCATION, exportFormat,
                        ImportExportConstants.TYPE_DEPLOYMENT_ENVIRONMENTS, deploymentsArray);
            }
        } catch (APIImportExportException e) {
            throw new APIManagementException(
                    "Error in converting deployment environment details to JSON object in API: " + apiID, e);
        } catch (IOException e) {
            throw new APIManagementException(
                    "Error while saving deployment environment details for API: " + apiID + " as YAML", e);
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
                                                                      String certDirectoryPath)
            throws APIImportExportException {

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
            try (ByteArrayInputStream certificate = certificateManager.getCertificateContent(tenantId,
                    metadataDTO.getAlias())) {
                byte[] certificateContent = IOUtils.toByteArray(certificate);
                String certificateContentEncoded = APIConstants.BEGIN_CERTIFICATE_STRING.concat(System.lineSeparator())
                        .concat(new String(Base64.encodeBase64(certificateContent))).concat(System.lineSeparator())
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
                if (endpointConfig.has(type)) {
                    Object item = endpointConfig.get(type);
                    if (item instanceof JSONArray) {
                        JSONArray endpointsJSON = new JSONArray(endpointConfig.getJSONArray(type).toString());
                        for (int i = 0; i < endpointsJSON.length(); i++) {
                            try {
                                String urlValue =
                                        endpointsJSON.getJSONObject(i).get(APIConstants.API_DATA_URL).toString();
                                urls.add(urlValue);
                            } catch (JSONException ex) {
                                log.error(
                                        "Endpoint URL extraction from endpoints JSON object failed in API: " + apiName,
                                        ex);
                            }
                        }
                    } else if (item instanceof JSONObject) {
                        JSONObject endpointJSON = new JSONObject(endpointConfig.getJSONObject(type).toString());
                        try {
                            String urlValue = endpointJSON.get(APIConstants.API_DATA_URL).toString();
                            urls.add(urlValue);
                        } catch (JSONException ex) {
                            log.error("Endpoint URL extraction from endpoint JSON object failed in API: " + apiName,
                                    ex);
                        }
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
     * @param organization   Organization Identifier
     * @param currentApiUuid UUID of the API/ API Product
     * @throws APIImportExportException If an error occurs while exporting meta information
     */
    public static void addAPIMetaInformationToArchive(String archivePath, APIDTO apiDtoToReturn,
            ExportFormat exportFormat, APIProvider apiProvider, APIIdentifier apiIdentifier, String organization,
            String currentApiUuid) throws APIImportExportException {
        String apiTenantDomain = null;
        String schemaContent;
        CommonUtil.createDirectory(archivePath + File.separator + ImportExportConstants.DEFINITIONS_DIRECTORY);

        try {
            // If a streaming API is exported, it does not contain a swagger file.
            // Therefore swagger export is only required for REST or SOAP based APIs
            String apiType = apiDtoToReturn.getType().toString();
            API api = APIMappingUtil.fromDTOtoAPI(apiDtoToReturn, apiDtoToReturn.getProvider());
            if (organization != null) {
                api.setOrganization(organization);
            } else {
                apiTenantDomain = getTenantDomain(apiIdentifier);
                api.setOrganization(apiTenantDomain);
            }
            api.setId(apiIdentifier);
            if (!PublisherCommonUtils.isStreamingAPI(apiDtoToReturn)) {
                // For Graphql APIs, the graphql schema definition should be exported.
                if (StringUtils.equals(apiType, APIConstants.APITransportType.GRAPHQL.toString())) {
                    if (organization != null) {
                        schemaContent = apiProvider.getGraphqlSchemaDefinition(currentApiUuid, organization);
                    } else {
                        schemaContent = apiProvider.getGraphqlSchemaDefinition(currentApiUuid, apiTenantDomain);
                    }
                    CommonUtil.writeFile(archivePath + ImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION,
                            schemaContent);
                    GraphqlComplexityInfo graphqlComplexityInfo = apiProvider
                            .getComplexityDetails(currentApiUuid);
                    if (graphqlComplexityInfo.getList().size() != 0) {
                        GraphQLQueryComplexityInfoDTO graphQLQueryComplexityInfoDTO =
                                GraphqlQueryAnalysisMappingUtil.fromGraphqlComplexityInfotoDTO(graphqlComplexityInfo);
                        CommonUtil.writeDtoToFile(archivePath + ImportExportConstants.GRAPHQL_COMPLEXITY_INFO_LOCATION,
                                exportFormat, ImportExportConstants.GRAPHQL_COMPLEXITY, graphQLQueryComplexityInfoDTO);
                    }
                }
                // For GraphQL APIs, swagger export is not needed
                if (!APIConstants.APITransportType.GRAPHQL.toString().equalsIgnoreCase(apiType)) {
                    String formattedSwaggerJson = RestApiCommonUtil.retrieveSwaggerDefinition(currentApiUuid, api,
                            apiProvider);
                    if (formattedSwaggerJson != null) {
                        CommonUtil.writeToYamlOrJson(archivePath + ImportExportConstants.SWAGGER_DEFINITION_LOCATION,
                                exportFormat, formattedSwaggerJson);
                    } else {
                        throw new APIImportExportException("Error while retrieving Swagger definition for API: "
                                + apiDtoToReturn.getName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                                + apiDtoToReturn.getVersion());
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Meta information retrieved successfully for API: " + apiDtoToReturn.getName()
                            + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiDtoToReturn.getVersion());
                }
            } else {
                String asyncApiJson = RestApiCommonUtil.retrieveAsyncAPIDefinition(api, apiProvider);
                // fetching the callback URL from asyncAPI definition.
                JsonParser jsonParser = new JsonParser();
                JsonObject parsedObject = jsonParser.parse(asyncApiJson).getAsJsonObject();
                if (parsedObject.has(ASYNC_DEFAULT_SUBSCRIBER)) {
                    String callBackEndpoint = parsedObject.get(ASYNC_DEFAULT_SUBSCRIBER).getAsString();
                    if (!StringUtils.isEmpty(callBackEndpoint)) {
                        // add openAPI definition to asyncAPI
                        String formattedSwaggerJson = RestApiCommonUtil
                                .generateOpenAPIForAsync(apiDtoToReturn.getName(), apiDtoToReturn.getVersion(),
                                        apiDtoToReturn.getContext(), callBackEndpoint);
                        CommonUtil
                                .writeToYamlOrJson(
                                        archivePath + ImportExportConstants.OPENAPI_FOR_ASYNCAPI_DEFINITION_LOCATION,
                                        exportFormat, formattedSwaggerJson);
                        // Adding endpoint config since adapter validates api.json for endpoint urls.
                        HashMap<String, Object> endpointConfig = new HashMap<>();
                        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "http");
                        endpointConfig.put("failOver", "false");
                        HashMap<String, Object> productionEndpoint = new HashMap<>();
                        productionEndpoint.put("template_not_supported", "false");
                        productionEndpoint.put("url", callBackEndpoint);
                        HashMap<String, Object> sandboxEndpoint = new HashMap<>();
                        sandboxEndpoint.put("template_not_supported", "false");
                        sandboxEndpoint.put("url", callBackEndpoint);
                        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoint);
                        endpointConfig.put(API_DATA_SANDBOX_ENDPOINTS, sandboxEndpoint);
                        apiDtoToReturn.setEndpointConfig(endpointConfig);
                    }
                }
                if (asyncApiJson != null) {
                    CommonUtil.writeToYamlOrJson(archivePath + ImportExportConstants.ASYNCAPI_DEFINITION_LOCATION,
                            exportFormat, asyncApiJson);
                } else {
                    throw new APIImportExportException("Error while retrieving AsyncAPI definition for API: "
                            + apiDtoToReturn.getName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                            + apiDtoToReturn.getVersion());
                }
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement apiObj = gson.toJsonTree(apiDtoToReturn);
            JsonObject apiJson = (JsonObject) apiObj;
            if (organization != null) {
                apiJson.addProperty("organizationId", organization);
            } else {
                apiJson.addProperty("organizationId", apiTenantDomain);
            }

            CommonUtil.writeDtoToFile(archivePath + ImportExportConstants.API_FILE_LOCATION, exportFormat,
                    ImportExportConstants.TYPE_API, apiJson);
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
     * Retrieve Mutual SSL related certificates and store those in the archive directory.
     *
     * @param archivePath  Folder path to export client certificates
     * @param identifier   Identifier
     * @param tenantId     Tenant id of the user
     * @param provider     Api Provider
     * @param exportFormat Export format of file
     * @param organization Organization
     * @throws APIImportExportException If an error occurs when writing to file or retrieving certificate metadata
     */
    public static void addClientCertificatesToArchive(String archivePath, Identifier identifier, int tenantId,
            APIProvider provider, ExportFormat exportFormat, String organization)
            throws APIImportExportException {

        List<ClientCertificateDTO> certificateMetadataDTOs;
        try {
            if (identifier instanceof APIProductIdentifier) {
                certificateMetadataDTOs = provider
                        .searchClientCertificates(tenantId, null, (APIProductIdentifier) identifier, organization);
            } else {
                certificateMetadataDTOs = provider
                        .searchClientCertificates(tenantId, null, (APIIdentifier) identifier, organization);
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
                String certificateContentEncoded = APIConstants.BEGIN_CERTIFICATE_STRING.concat(System.lineSeparator())
                        .concat(certificateContent)
                        .concat(System.lineSeparator()).concat(APIConstants.END_CERTIFICATE_STRING);
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
                                                             ExportFormat exportFormat, APIProvider apiProvider,
                                                             String organization)
            throws APIImportExportException {

        CommonUtil.createDirectory(archivePath + File.separator + ImportExportConstants.DEFINITIONS_DIRECTORY);

        try {
            String formattedSwaggerJson = apiProvider.getOpenAPIDefinition(apiProductDtoToReturn.getId(), organization);
            if (formattedSwaggerJson != null) {
                CommonUtil.writeToYamlOrJson(archivePath + ImportExportConstants.SWAGGER_DEFINITION_LOCATION,
                        exportFormat, formattedSwaggerJson);
            } else {
                throw new APIImportExportException("Error while retrieving Swagger definition for API Product: "
                        + apiProductDtoToReturn.getName());
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "Meta information retrieved successfully for API Product: " + apiProductDtoToReturn.getName());
            }
            CommonUtil.writeDtoToFile(archivePath + ImportExportConstants.API_PRODUCT_FILE_LOCATION, exportFormat,
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
     * @param organization          Organization
     * @throws APIImportExportException If an error occurs while creating the directory or extracting the archive
     * @throws APIManagementException   If an error occurs while retrieving API related resources
     */
    public static void addDependentAPIsToArchive(String archivePath, APIProductDTO apiProductDtoToReturn,
                                                 ExportFormat exportFormat, APIProvider provider, String userName,
                                                 Boolean isStatusPreserved, boolean preserveDocs,
                                                 boolean preserveCredentials, String organization)
            throws APIImportExportException, APIManagementException {

        String apisDirectoryPath = archivePath + File.separator + ImportExportConstants.APIS_DIRECTORY;
        CommonUtil.createDirectory(apisDirectoryPath);

        List<ProductAPIDTO> apisList = apiProductDtoToReturn.getApis();
        for (ProductAPIDTO productAPIDTO : apisList) {
            String apiProductRequesterDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            API api = provider.getAPIbyUUID(productAPIDTO.getApiId(), apiProductRequesterDomain);
            APIDTO apiDtoToReturn = APIMappingUtil.fromAPItoDTO(api, preserveCredentials, null);
            File dependentAPI = exportApi(provider, api.getId(), apiDtoToReturn, api, userName, exportFormat,
                    isStatusPreserved, preserveDocs, StringUtils.EMPTY, organization);
            CommonUtil.extractArchive(dependentAPI, apisDirectoryPath);
        }
    }

    protected static String getTenantDomain(Identifier identifier) {

        return MultitenantUtils.getTenantDomain(
                APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
    }

    /**
     * Retrieve available custom sequences and API specific sequences for API export, and store it in the archive
     * directory.
     *
     * @param archivePath File path to export the sequences
     * @throws APIImportExportException If an error occurs while exporting sequences
     */
    public static void addRuntimeSequencesToArchive(String archivePath, API api)
            throws APIImportExportException, APIManagementException {

        String seqArchivePath = archivePath.concat(File.separator + ImportExportConstants.SEQUENCES_RESOURCE);
        Mediation inSequenceMediation = api.getInSequenceMediation();
        Mediation outSequenceMediation = api.getOutSequenceMediation();
        Mediation faultSequenceMediation = api.getFaultSequenceMediation();
        if (inSequenceMediation != null || outSequenceMediation != null || faultSequenceMediation != null) {
            CommonUtil.createDirectory(seqArchivePath);
            if (inSequenceMediation != null) {
                String individualSequenceExportPath;
                if (inSequenceMediation.isGlobal()) {
                    individualSequenceExportPath =
                            seqArchivePath + File.separator + ImportExportConstants.IN_SEQUENCE_PREFIX +
                                    ImportExportConstants.SEQUENCE_LOCATION_POSTFIX;
                } else {
                    individualSequenceExportPath =
                            seqArchivePath + File.separator + ImportExportConstants.IN_SEQUENCE_PREFIX
                                    + ImportExportConstants.SEQUENCE_LOCATION_POSTFIX + File.separator
                                    + ImportExportConstants.CUSTOM_TYPE;
                }
                if (!CommonUtil.checkFileExistence(individualSequenceExportPath)) {
                    CommonUtil.createDirectory(individualSequenceExportPath);
                }
                writeSequenceToArchive(inSequenceMediation.getConfig(), individualSequenceExportPath,
                        inSequenceMediation.getName());
            }
            if (outSequenceMediation != null) {
                String individualSequenceExportPath;
                if (outSequenceMediation.isGlobal()) {
                    individualSequenceExportPath =
                            seqArchivePath + File.separator + ImportExportConstants.OUT_SEQUENCE_PREFIX +
                                    ImportExportConstants.SEQUENCE_LOCATION_POSTFIX;
                } else {
                    individualSequenceExportPath =
                            seqArchivePath + File.separator + ImportExportConstants.OUT_SEQUENCE_PREFIX
                                    + ImportExportConstants.SEQUENCE_LOCATION_POSTFIX + File.separator
                                    + ImportExportConstants.CUSTOM_TYPE;
                }
                if (!CommonUtil.checkFileExistence(individualSequenceExportPath)) {
                    CommonUtil.createDirectory(individualSequenceExportPath);
                }
                writeSequenceToArchive(outSequenceMediation.getConfig(), individualSequenceExportPath,
                        outSequenceMediation.getName());
            }
            if (faultSequenceMediation != null) {
                String individualSequenceExportPath;
                if (faultSequenceMediation.isGlobal()) {
                    individualSequenceExportPath =
                            seqArchivePath + File.separator + ImportExportConstants.FAULT_SEQUENCE_PREFIX +
                                    ImportExportConstants.SEQUENCE_LOCATION_POSTFIX;
                } else {
                    individualSequenceExportPath =
                            seqArchivePath + File.separator + ImportExportConstants.FAULT_SEQUENCE_PREFIX
                                    + ImportExportConstants.SEQUENCE_LOCATION_POSTFIX + File.separator
                                    + ImportExportConstants.CUSTOM_TYPE;

                }
                if (!CommonUtil.checkFileExistence(individualSequenceExportPath)) {
                    CommonUtil.createDirectory(individualSequenceExportPath);
                }
                writeSequenceToArchive(faultSequenceMediation.getConfig(), individualSequenceExportPath,
                        faultSequenceMediation.getName());
            }
        }
    }
}
