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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.axiom.om.OMElement;
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
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManager;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ProductAPIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
            RestApiUtil.handleBadRequest("'name' (" + name + ") or 'version' (" + version
                    + ") should not be null.", log);
        }
        String apiRequesterDomain = RestApiUtil.getLoggedInUserTenantDomain();

        // If provider name is not given
        if (StringUtils.isBlank(providerName)) {
            // Retrieve the provider who is in same tenant domain and who owns the same API (by comparing
            // API name and the version)
            providerName = APIUtil.getAPIProviderFromAPINameVersionTenant(name, version, apiRequesterDomain);

            // If there is no provider in current domain, the API cannot be exported
            if (providerName == null) {
                String errorMessage = "Error occurred while exporting. API: " + name + " version: " + version
                        + " not found";
                RestApiUtil.handleResourceNotFoundError(errorMessage, log);
            }
        }

        if (!StringUtils.equals(MultitenantUtils.getTenantDomain(providerName), apiRequesterDomain)) {
            // Not authorized to export requested API
            RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API +
                    " name:" + name + " version:" + version + " provider:" + providerName, log);
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
                                 String userName, ExportFormat exportFormat, Boolean preserveStatus)
            throws APIManagementException {
        int tenantId = 0;
        try {
            // Create temp location for storing API data
            File exportFolder = CommonUtil.createTempDirectory(apiIdentifier);
            String exportAPIBasePath = exportFolder.toString();
            String archivePath = exportAPIBasePath.concat(File.separator + apiIdentifier.getApiName() + "-"
                    + apiIdentifier.getVersion());
            tenantId = APIUtil.getTenantId(userName);
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry(tenantId);

            CommonUtil.createDirectory(archivePath);

            addThumbnailToArchive(archivePath, apiIdentifier, registry);
            addSOAPToRESTMediationToArchive(archivePath, apiIdentifier, registry);
            addAPIDocumentationToArchive(archivePath, apiIdentifier, registry, exportFormat, apiProvider);

            if (StringUtils.isNotEmpty(apiDtoToReturn.getWsdlUrl())) {
                addWSDLtoArchive(archivePath, apiIdentifier, registry);
            } else if (log.isDebugEnabled()) {
                log.debug("No WSDL URL found for API: " + apiIdentifier + ". Skipping WSDL export.");
            }

            addSequencesToArchive(archivePath, APIMappingUtil.fromDTOtoAPI(apiDtoToReturn, userName), registry);

            // Set API status to created if the status is not preserved
            if (!preserveStatus) {
                apiDtoToReturn.setLifeCycleStatus(APIConstants.CREATED);
            }

            addEndpointCertificatesToArchive(archivePath, apiDtoToReturn, tenantId, exportFormat);
            addAPIMetaInformationToArchive(archivePath, apiDtoToReturn, exportFormat, apiProvider, apiIdentifier, userName);

            // Export mTLS authentication related certificates
            if (apiProvider.isClientCertificateBasedAuthenticationConfigured()) {
                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL enabled. Exporting client certificates.");
                }
                ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(APIMappingUtil.fromDTOtoAPI(apiDtoToReturn, userName));
                addClientCertificatesToArchive(archivePath, apiTypeWrapper, tenantId, apiProvider, exportFormat);
            }
            CommonUtil.archiveDirectory(exportAPIBasePath);
            FileUtils.deleteQuietly(new File(exportAPIBasePath));
            return new File(exportAPIBasePath + APIConstants.ZIP_FILE_EXTENSION);
        } catch (RegistryException e) {
            String errorMessage = "Error while getting governance registry for tenant: " + tenantId;
            throw new APIManagementException(errorMessage, e);
        } catch (APIManagementException | APIImportExportException e) {
            RestApiUtil.handleInternalServerError("Error while exporting " + RestApiConstants.RESOURCE_API, e, log);
        }
        return null;
    }

    /**
     * Exports an API Product from API Manager for a given API Product. MMeta information, API Product icon, documentation, client certificates
     * and dependent APIs are exported.
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
                                        Boolean preserveStatus)
            throws APIManagementException {
        int tenantId = 0;
        try {
            // Create temp location for storing API Product data
            File exportFolder = CommonUtil.createTempDirectory(apiProductIdentifier);
            String exportAPIBasePath = exportFolder.toString();
            String archivePath = exportAPIBasePath.concat(File.separator + apiProductIdentifier.getName() + "-"
                    + apiProductIdentifier.getVersion());
            tenantId = APIUtil.getTenantId(userName);
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry(tenantId);
            APIProduct apiProduct = APIMappingUtil.fromDTOtoAPIProduct(apiProductDtoToReturn, userName);

            CommonUtil.createDirectory(archivePath);

            addThumbnailToArchive(archivePath, apiProductIdentifier, registry);
            addAPIDocumentationToArchive(archivePath, apiProductIdentifier, registry, exportFormat, apiProvider);
            addAPIProductMetaInformationToArchive(archivePath, apiProductDtoToReturn, exportFormat, apiProvider,
                    userName);
            addDependentAPIsToArchive(archivePath, apiProductDtoToReturn, exportFormat, apiProvider, userName, preserveStatus);

            // Export mTLS authentication related certificates
            if (apiProvider.isClientCertificateBasedAuthenticationConfigured()) {
                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL enabled. Exporting client certificates.");
                }
                ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(apiProduct);
                addClientCertificatesToArchive(archivePath, apiTypeWrapper, tenantId, apiProvider, exportFormat);
            }
            CommonUtil.archiveDirectory(exportAPIBasePath);
            FileUtils.deleteQuietly(new File(exportAPIBasePath));
            return new File(exportAPIBasePath + APIConstants.ZIP_FILE_EXTENSION);
        } catch (RegistryException e) {
            String errorMessage = "Error while getting governance registry for tenant: " + tenantId;
            throw new APIManagementException(errorMessage, e);
        } catch (APIManagementException | APIImportExportException e) {
            RestApiUtil.handleInternalServerError("Error while exporting " +
                    RestApiConstants.RESOURCE_API_PRODUCT, e, log);
        }
        return null;
    }

    /**
     * Retrieve thumbnail image for the exporting API or API Product and store it in the archive directory.
     *
     * @param identifier ID of the requesting API or API Product
     * @param registry   Current tenant registry
     * @throws APIImportExportException If an error occurs while retrieving image from the registry or
     *                                  storing in the archive directory
     */
    public static void addThumbnailToArchive(String archivePath, Identifier identifier, Registry registry)
            throws APIImportExportException {
        String thumbnailUrl = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR
                + identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getName()
                + RegistryConstants.PATH_SEPARATOR + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR
                + APIConstants.API_ICON_IMAGE;
        String localImagePath = archivePath + File.separator + ImportExportConstants.IMAGE_RESOURCE;
        try {
            if (registry.resourceExists(thumbnailUrl)) {
                Resource icon = registry.get(thumbnailUrl);
                String mediaType = icon.getMediaType();
                String extension = ImportExportConstants.fileExtensionMapping.get(mediaType);
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
     * Retrieve SOAP to REST mediation logic for the exporting API and store it in the archive directory
     *
     * @param archivePath   ID of the requesting API
     * @param apiIdentifier ID of the requesting API
     * @param registry      Current tenant registry
     * @throws APIImportExportException If an error occurs while retrieving image from the registry or
     *                                  storing in the archive directory
     */
    public static void addSOAPToRESTMediationToArchive(String archivePath, APIIdentifier apiIdentifier,
                                                       UserRegistry registry) throws APIImportExportException {
        String soapToRestBaseUrl = "/apimgt/applicationdata/provider" + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getVersion() + RegistryConstants.PATH_SEPARATOR +
                "soap_to_rest";

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (registry.resourceExists(soapToRestBaseUrl)) {
                Collection inFlow = (org.wso2.carbon.registry.api.Collection) registry.get(soapToRestBaseUrl
                        + RegistryConstants.PATH_SEPARATOR + IN);
                Collection outFlow = (org.wso2.carbon.registry.api.Collection) registry.get(soapToRestBaseUrl
                        + RegistryConstants.PATH_SEPARATOR + OUT);

                CommonUtil.createDirectory(archivePath + File.separator + SOAPTOREST + File.separator + IN);
                CommonUtil.createDirectory(archivePath + File.separator + SOAPTOREST + File.separator + OUT);
                if (inFlow != null) {
                    for (String inFlowPath : inFlow.getChildren()) {
                        inputStream = registry.get(inFlowPath).getContentStream();
                        outputStream = new FileOutputStream(archivePath + File.separator + SOAPTOREST
                                + File.separator + IN +
                                inFlowPath.substring(inFlowPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR)));
                        IOUtils.copy(inputStream, outputStream);
                        IOUtils.closeQuietly(inputStream);
                        IOUtils.closeQuietly(outputStream);
                    }
                }
                if (outFlow != null) {
                    for (String outFlowPath : outFlow.getChildren()) {
                        inputStream = registry.get(outFlowPath).getContentStream();
                        outputStream = new FileOutputStream(archivePath + File.separator + SOAPTOREST
                                + File.separator + OUT +
                                outFlowPath.substring(outFlowPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR)));
                        IOUtils.copy(inputStream, outputStream);
                        IOUtils.closeQuietly(inputStream);
                        IOUtils.closeQuietly(outputStream);
                    }
                }
            }
        } catch (IOException e) {
            throw new APIImportExportException("I/O error while writing API SOAP to REST logic to file", e);
        } catch (RegistryException e) {
            throw new APIImportExportException("Error while retrieving SOAP to REST logic", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Retrieve documentation for the exporting API or API Product and store it in the archive directory.
     * FILE, INLINE, MARKDOWN and URL documentations are handled.
     *
     * @param archivePath  File path to the documents to be exported
     * @param identifier   ID of the requesting API or API Product
     * @param registry     Current tenant registry
     * @param exportFormat Format for export
     * @param apiProvider  API Provider
     * @throws APIImportExportException If an error occurs while retrieving documents from the
     *                                  registry or storing in the archive directory
     * @throws APIManagementException   If an error occurs while retrieving document details
     */
    public static void addAPIDocumentationToArchive(String archivePath, Identifier identifier, Registry registry,
                                                    ExportFormat exportFormat, APIProvider apiProvider)
            throws APIImportExportException, APIManagementException {

        List<Documentation> docList = apiProvider.getAllDocumentation(identifier);
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        if (!docList.isEmpty()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String docDirectoryPath = archivePath + File.separator + ImportExportConstants.DOCUMENT_DIRECTORY;
            CommonUtil.createDirectory(docDirectoryPath);
            try {
                for (Documentation doc : docList) {
                    // Retrieving the document again since objects in docList might have missing fields
                    Documentation individualDocument = apiProvider.getDocumentation(doc.getId(), tenantDomain);
                    String sourceType = individualDocument.getSourceType().name();
                    String resourcePath = null;
                    String localFileName = null;
                    String individualDocDirectoryPath = docDirectoryPath + File.separator + individualDocument.getName();
                    CommonUtil.createDirectory(individualDocDirectoryPath);

                    JsonObject documentJsonObject = addTypeAndVersionToFile(
                            ImportExportConstants.TYPE_DOCUMENTS, ImportExportConstants.APIM_VERSION,
                            gson.toJsonTree(DocumentationMappingUtil.fromDocumentationToDTO(individualDocument)));
                    String jsonDocument = gson.toJson(documentJsonObject);
                    writeToYamlOrJson(individualDocDirectoryPath + ImportExportConstants.DOCUMENT_FILE_NAME,
                            exportFormat, jsonDocument);

                    if (Documentation.DocumentSourceType.FILE.toString().equalsIgnoreCase(sourceType)) {
                        localFileName = individualDocument.getFilePath().substring(
                                individualDocument.getFilePath().lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                        resourcePath = APIUtil.getDocumentationFilePath(identifier, localFileName);
                        individualDocument.setFilePath(localFileName);
                    } else if (Documentation.DocumentSourceType.INLINE.toString().equalsIgnoreCase(sourceType)
                            || Documentation.DocumentSourceType.MARKDOWN.toString().equalsIgnoreCase(sourceType)) {
                        // Inline/Markdown content file name would be same as the documentation name
                        localFileName = individualDocument.getName();
                        resourcePath = APIUtil.getAPIOrAPIProductDocPath(identifier) + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
                                + RegistryConstants.PATH_SEPARATOR + localFileName;
                    }

                    if (resourcePath != null) {
                        // Write content for Inline/Markdown/File type documentations only
                        // Check whether resource exists in the registry
                        if (registry.resourceExists(resourcePath)) {
                            Resource docFile = registry.get(resourcePath);
                            try (OutputStream outputStream = new FileOutputStream(individualDocDirectoryPath +
                                    File.separator + localFileName);
                                 InputStream fileInputStream = docFile.getContentStream()) {
                                IOUtils.copy(fileInputStream, outputStream);
                            }
                        } else {
                            // Log error and avoid throwing as we give capability to export document artifact without the
                            // content if does not exists
                            String errorMessage = "Documentation resource for API/API Product: " + identifier.getName()
                                    + " not found in " + resourcePath;
                            log.error(errorMessage);
                        }
                    }
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
        } else if (log.isDebugEnabled()) {
            log.debug("No documentation found for API/API Product: " + identifier + ". Skipping documentation export.");
        }
    }

    /**
     * Retrieve WSDL for the exporting API and store it in the archive directory.
     *
     * @param apiIdentifier ID of the requesting API
     * @param registry      Current tenant registry
     * @throws APIImportExportException If an error occurs while retrieving WSDL from the registry or
     *                                  storing in the archive directory
     */
    public static void addWSDLtoArchive(String archivePath, APIIdentifier apiIdentifier, Registry registry)
            throws APIImportExportException {

        String wsdlPath = APIConstants.API_WSDL_RESOURCE_LOCATION + apiIdentifier.getProviderName() + "--"
                + apiIdentifier.getApiName() + apiIdentifier.getVersion() + APIConstants.WSDL_FILE_EXTENSION;
        try {
            if (registry.resourceExists(wsdlPath)) {
                CommonUtil.createDirectory(archivePath + File.separator + "WSDL");
                Resource wsdl = registry.get(wsdlPath);
                try (InputStream wsdlStream = wsdl.getContentStream();
                     OutputStream outputStream = new FileOutputStream(archivePath + File.separator + "WSDL"
                             + File.separator + apiIdentifier.getApiName() + "-" + apiIdentifier.getVersion()
                             + APIConstants.WSDL_FILE_EXTENSION)) {
                    IOUtils.copy(wsdlStream, outputStream);
                    if (log.isDebugEnabled()) {
                        log.debug("WSDL file: " + wsdlPath + " retrieved successfully");
                    }
                }
            } else if (log.isDebugEnabled()) {
                log.debug("WSDL resource does not exists in path: " + wsdlPath + ". Skipping WSDL export.");
            }
        } catch (IOException e) {
            String errorMessage = "I/O error while writing WSDL: " + wsdlPath + " to file";
            throw new APIImportExportException(errorMessage, e);
        } catch (RegistryException e) {
            String errorMessage = "Error while retrieving WSDL: " + wsdlPath + " to file";
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Retrieve available custom sequences and API specific sequences for API export, and store it in the archive
     * directory.
     *
     * @param api      Exporting API
     * @param registry Current tenant registry
     * @throws APIImportExportException If an error occurs while exporting sequences
     */
    public static void addSequencesToArchive(String archivePath, API api, Registry registry) throws APIImportExportException {

        Map<String, String> sequences = new HashMap<>();
        APIIdentifier apiIdentifier = api.getId();
        String seqArchivePath = archivePath.concat(File.separator + "Sequences");

        if (api.getInSequence() != null) {
            sequences.put(APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN, api.getInSequence());
        }

        if (api.getOutSequence() != null) {
            sequences.put(APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT, api.getOutSequence());
        }

        if (api.getFaultSequence() != null) {
            sequences.put(APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT, api.getFaultSequence());
        }

        if (!sequences.isEmpty()) {
            CommonUtil.createDirectory(seqArchivePath);
            for (Map.Entry<String, String> sequence : sequences.entrySet()) {
                AbstractMap.SimpleEntry<String, OMElement> sequenceDetails;
                String sequenceName = sequence.getValue();
                String direction = sequence.getKey();
                String pathToExportedSequence = seqArchivePath + File.separator + direction + "-sequence" + File.separator;
                if (sequenceName != null) {
                    sequenceDetails = getCustomSequence(sequenceName, direction, registry);
                    if (sequenceDetails == null) {
                        //If sequence doesn't exist in 'apimgt/customsequences/{in/out/fault}' directory check in API
                        //specific registry path
                        sequenceDetails = getAPISpecificSequence(api.getId(), sequenceName, direction, registry);
                        pathToExportedSequence += ImportExportConstants.CUSTOM_TYPE + File.separator;
                    }
                    writeSequenceToFile(pathToExportedSequence, sequenceDetails, apiIdentifier);
                }
            }
        } else if (log.isDebugEnabled()) {
            log.debug("No custom sequences available for API: " + apiIdentifier.getApiName() + StringUtils.SPACE
                    + APIConstants.API_DATA_VERSION + ": " + apiIdentifier.getVersion()
                    + ". Skipping custom sequence export.");
        }
    }

    /**
     * Retrieve custom sequence details from the registry.
     *
     * @param sequenceName Name of the sequence
     * @param type         Sequence type
     * @param registry     Current tenant registry
     * @return Registry resource name of the sequence and its content
     * @throws APIImportExportException If an error occurs while retrieving registry elements
     */
    private static AbstractMap.SimpleEntry<String, OMElement> getCustomSequence(String sequenceName, String type,
                                                                                Registry registry)
            throws APIImportExportException {

        String regPath = null;
        if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN.equals(type)) {
            regPath = APIConstants.API_CUSTOM_INSEQUENCE_LOCATION;
        } else if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT.equals(type)) {
            regPath = APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION;
        } else if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT.equals(type)) {
            regPath = APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION;
        }
        return getSeqDetailsFromRegistry(sequenceName, regPath, registry);
    }

    /**
     * Retrieve API Specific sequence details from the registry.
     *
     * @param sequenceName Name of the sequence
     * @param type         Sequence type
     * @param registry     Current tenant registry
     * @return Registry resource name of the sequence and its content
     * @throws APIImportExportException If an error occurs while retrieving registry elements
     */
    private static AbstractMap.SimpleEntry<String, OMElement> getAPISpecificSequence(APIIdentifier api,
                                                                                     String sequenceName, String type,
                                                                                     Registry registry)
            throws APIImportExportException {

        String regPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + api.getProviderName()
                + RegistryConstants.PATH_SEPARATOR + api.getApiName() + RegistryConstants.PATH_SEPARATOR
                + api.getVersion() + RegistryConstants.PATH_SEPARATOR + type;
        return getSeqDetailsFromRegistry(sequenceName, regPath, registry);
    }

    /**
     * Retrieve sequence details from registry by given registry path.
     *
     * @param sequenceName Sequence Name
     * @param regPath      Registry path
     * @param registry     Registry
     * @return Sequence details as a simple entry
     * @throws APIImportExportException If an error occurs while retrieving sequence details from registry
     */
    private static AbstractMap.SimpleEntry<String, OMElement> getSeqDetailsFromRegistry(String sequenceName,
                                                                                        String regPath, Registry registry)
            throws APIImportExportException {

        AbstractMap.SimpleEntry<String, OMElement> sequenceDetails = null;
        Collection seqCollection;

        try {
            seqCollection = (Collection) registry.get(regPath);
            if (seqCollection != null) {
                String[] childPaths = seqCollection.getChildren();
                for (String childPath : childPaths) {
                    Resource sequence = registry.get(childPath);
                    OMElement seqElement = APIUtil.buildOMElement(sequence.getContentStream());
                    if (sequenceName.equals(seqElement.getAttributeValue(new QName("name")))) {
                        String sequenceFileName = sequenceName + APIConstants.XML_EXTENSION;
                        sequenceDetails = new AbstractMap.SimpleEntry<>(sequenceFileName, seqElement);
                        break;
                    }
                }
            }
        } catch (RegistryException e) {
            String errorMessage = "Error while retrieving sequence: " + sequenceName + " from the path: " + regPath;
            throw new APIImportExportException(errorMessage, e);
        } catch (Exception e) {
            //APIUtil.buildOMElement() throws a generic exception
            String errorMessage = "Error while reading content for sequence: " + sequenceName + " from the registry";
            throw new APIImportExportException(errorMessage, e);
        }
        return sequenceDetails;
    }

    /**
     * Store API Specific or custom sequences in the archive directory.
     *
     * @param sequenceDetails Details of the sequence
     * @param apiIdentifier   ID of the requesting API
     * @throws APIImportExportException If an error occurs while serializing XML stream or storing in
     *                                  archive directory
     */
    private static void writeSequenceToFile(String pathToExportedSequence,
                                            AbstractMap.SimpleEntry<String, OMElement> sequenceDetails,
                                            APIIdentifier apiIdentifier)
            throws APIImportExportException {

        if (sequenceDetails != null) {
            String sequenceFileName = sequenceDetails.getKey();
            OMElement sequenceConfig = sequenceDetails.getValue();
            CommonUtil.createDirectory(pathToExportedSequence);
            String exportedSequenceFile = pathToExportedSequence + sequenceFileName;
            try (OutputStream outputStream = new FileOutputStream(exportedSequenceFile)) {
                sequenceConfig.serialize(outputStream);
                if (log.isDebugEnabled()) {
                    log.debug(sequenceFileName + " of API: " + apiIdentifier.getApiName() + " retrieved successfully");
                }
            } catch (IOException e) {
                String errorMessage = "Unable to find file: " + exportedSequenceFile;
                throw new APIImportExportException(errorMessage, e);
            } catch (XMLStreamException e) {
                String errorMessage = "Error while processing XML stream ";
                throw new APIImportExportException(errorMessage, e);
            }
        } else {
            String errorMessage = "Error while writing sequence of API: " + apiIdentifier.getApiName() + " to file.";
            throw new APIImportExportException(errorMessage);
        }
    }

    /**
     * Retrieve the endpoint certificates and store those in the archive directory.
     *
     * @param apiDto       APIDTO to be exported
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
        String endpointCertsDirectoryPath = archivePath + File.separator
                + ImportExportConstants.ENDPOINT_CERTIFICATES_DIRECTORY;
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
                JsonObject endpointCertificatesJsonObject = addTypeAndVersionToFile(
                        ImportExportConstants.TYPE_ENDPOINT_CERTIFICATES, ImportExportConstants.APIM_VERSION,
                        gson.toJsonTree(endpointCertificatesDetails));
                String certificatesJson = gson.toJson(endpointCertificatesJsonObject);
                writeToYamlOrJson(endpointCertsDirectoryPath +
                        ImportExportConstants.ENDPOINTS_CERTIFICATE_FILE, exportFormat, certificatesJson);
            } else if (log.isDebugEnabled()) {
                log.debug("No endpoint certificates available for API: " + apiDto.getName() + StringUtils.SPACE
                        + APIConstants.API_DATA_VERSION + ": " + apiDto.getVersion() + ". Skipping certificate export.");
            }
        } catch (JSONException e) {
            String errorMsg = "Error in converting Endpoint config to JSON object in API: " + apiDto.getName();
            throw new APIImportExportException(errorMsg, e);
        } catch (IOException e) {
            String errorMessage = "Error while retrieving saving endpoint certificate details for API: "
                    + apiDto.getName() + " as YAML";
            throw new APIImportExportException(errorMessage, e);
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
            String errorMsg = "Error retrieving certificate meta data. For tenantId: " + tenantId + " hostname: "
                    + url;
            throw new APIImportExportException(errorMsg, e);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray certificatesList = new JsonArray();
        certificateMetadataDTOS.forEach(metadataDTO -> {
            ByteArrayInputStream certificate = null;
            try {
                certificate = certificateManager.getCertificateContent(metadataDTO.getAlias());
                certificate.close();
                byte[] certificateContent = IOUtils.toByteArray(certificate);
                String certificateContentEncoded = APIConstants.BEGIN_CERTIFICATE_STRING
                        .concat(new String(Base64.encodeBase64(certificateContent))).concat("\n")
                        .concat(APIConstants.END_CERTIFICATE_STRING);
                CommonUtil.writeFile(certDirectoryPath + File.separator + metadataDTO.getAlias() + ".crt",
                        certificateContentEncoded);
                // Add the file name to the Certificate Metadata
                JsonObject modifiedCertificateMetadata = (JsonObject) gson.toJsonTree(metadataDTO);
                modifiedCertificateMetadata.addProperty(ImportExportConstants.CERTIFICATE_FILE,
                        metadataDTO.getAlias() + ".crt");
                certificatesList.add(modifiedCertificateMetadata);
            } catch (APIManagementException e) {
                log.error("Error retrieving certificate content. For tenantId: " + tenantId + " hostname: "
                        + url + " alias: " + metadataDTO.getAlias(), e);
            } catch (IOException e) {
                log.error("Error while converting certificate content to Byte Array. For tenantId: " + tenantId
                        + " hostname: " + url + " alias: " + metadataDTO.getAlias(), e);
            } catch (APIImportExportException e) {
                log.error("Error while writing the certificate content. For tenantId: " + tenantId + " hostname: "
                        + url + " alias: " + metadataDTO.getAlias(), e);
            } finally {
                if (certificate != null) {
                    IOUtils.closeQuietly(certificate);
                }
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
    private static List<String> getEndpointURLs(JSONObject endpointConfig, String type, String apiName) {
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
                            log.error("Endpoint URL extraction from endpoints JSON object failed in API: "
                                    + apiName, ex);
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
                log.info("Endpoint type: " + type + " not found in API: " + apiName);
            }
        }
        return urls;
    }

    /**
     * Retrieve meta information of the API to export and store those in the archive directory.
     * URL template information are stored in swagger.json definition while rest of the required
     * data are in api.json
     *
     * @param archivePath    Folder path to export meta information
     * @param apiDtoToReturn APIDTO to be exported
     * @param exportFormat   Export format of file
     * @param apiProvider    API Provider
     * @param apiIdentifier  API Identifier
     * @param userName       Username
     * @throws APIImportExportException If an error occurs while exporting meta information
     */
    public static void addAPIMetaInformationToArchive(String archivePath, APIDTO apiDtoToReturn, ExportFormat exportFormat,
                                                      APIProvider apiProvider, APIIdentifier apiIdentifier, String userName)
            throws APIImportExportException {

        CommonUtil.createDirectory(archivePath + File.separator + ImportExportConstants.META_INFO_DIRECTORY);

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // If a web socket API is exported, it does not contain a swagger file.
            // Therefore swagger export is only required for REST, Graphql or SOAP based APIs
            String apiType = apiDtoToReturn.getType().toString();
            if (!APIConstants.APITransportType.WS.toString().equalsIgnoreCase(apiType)) {
                //For Graphql APIs, the graphql schema definition, swagger and the serialized api object are exported.
                if (StringUtils.equals(apiType, APIConstants.APITransportType.GRAPHQL.toString())) {
                    String schemaContent = apiProvider.getGraphqlSchema(apiIdentifier);
                    CommonUtil.writeFile(archivePath + ImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION,
                            schemaContent);
                }
                String formattedSwaggerJson = RestApiPublisherUtils.retrieveSwaggerDefinition(
                        APIMappingUtil.fromDTOtoAPI(apiDtoToReturn, userName), apiProvider);
                writeToYamlOrJson(archivePath + ImportExportConstants.SWAGGER_DEFINITION_LOCATION,
                        exportFormat, formattedSwaggerJson);

                if (log.isDebugEnabled()) {
                    log.debug("Meta information retrieved successfully for API: " + apiDtoToReturn.getName()
                            + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiDtoToReturn.getVersion());
                }
            }
            JsonObject apiJsonObject = addTypeAndVersionToFile(ImportExportConstants.TYPE_API,
                    ImportExportConstants.APIM_VERSION, gson.toJsonTree(apiDtoToReturn));
            String apiInJson = gson.toJson(apiJsonObject);
            writeToYamlOrJson(archivePath +
                    ImportExportConstants.API_FILE_LOCATION, exportFormat, apiInJson);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Swagger definition for API: " + apiDtoToReturn.getName()
                    + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiDtoToReturn.getVersion();
            throw new APIImportExportException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Error while retrieving saving as YAML for API: " + apiDtoToReturn.getName()
                    + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiDtoToReturn.getVersion();
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Retrieve Mutual SSL related certificates and store those in the archive directory
     *
     * @param apiTypeWrapper API or API Product to be exported
     * @param tenantId       Tenant id of the user
     * @param provider       Api Provider
     * @param exportFormat   Export format of file
     * @throws APIImportExportException If an error occurs when writing to file or retrieving certificate metadata
     */
    public static void addClientCertificatesToArchive(String archivePath, ApiTypeWrapper apiTypeWrapper,
                                                      int tenantId, APIProvider provider,
                                                      ExportFormat exportFormat) throws APIImportExportException {
        List<ClientCertificateDTO> certificateMetadataDTOs;
        try {
            if (apiTypeWrapper.isAPIProduct()) {
                certificateMetadataDTOs = provider.searchClientCertificates(tenantId, null, apiTypeWrapper.getApiProduct().getId());
            } else {
                certificateMetadataDTOs = provider.searchClientCertificates(tenantId, null, apiTypeWrapper.getApi().getId());
            }
            if (certificateMetadataDTOs.isEmpty()) {
                return;
            }
            String clientCertsDirectoryPath = archivePath + File.separator
                    + ImportExportConstants.CLIENT_CERTIFICATES_DIRECTORY;
            CommonUtil.createDirectory(clientCertsDirectoryPath);

            JsonArray certificateList = getClientCertificateContentAndMetaData(certificateMetadataDTOs, clientCertsDirectoryPath);

            if (certificateList.size() > 0) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonObject clientCertificatesJsonObject = addTypeAndVersionToFile(
                        ImportExportConstants.TYPE_CLIENT_CERTIFICATES, ImportExportConstants.APIM_VERSION,
                        gson.toJsonTree(certificateList));
                String certificatesJson = gson.toJson(clientCertificatesJsonObject);
                writeToYamlOrJson(clientCertsDirectoryPath + ImportExportConstants.CLIENT_CERTIFICATE_FILE,
                        exportFormat, certificatesJson);
            }
        } catch (IOException e) {
            String errorMessage = "Error while saving as YAML or JSON";
            throw new APIImportExportException(errorMessage, e);
        } catch (APIManagementException e) {
            String errorMsg = "Error retrieving certificate meta data. tenantId [" + tenantId + "] api ["
                    + tenantId + "]";
            throw new APIImportExportException(errorMsg, e);
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
            ByteArrayInputStream certificate = null;
            try {
                String certificateContent = metadataDTO.getCertificate();
                String certificateContentEncoded = APIConstants.BEGIN_CERTIFICATE_STRING
                        .concat(certificateContent).concat("\n")
                        .concat(APIConstants.END_CERTIFICATE_STRING);
                CommonUtil.writeFile(certDirectoryPath + File.separator + metadataDTO.getAlias() + ".crt",
                        certificateContentEncoded);
                // Add the file name to the Certificate Metadata
                metadataDTO.setCertificate(metadataDTO.getAlias() + ".crt");
                JsonObject certificateMetadata = (JsonObject) gson.toJsonTree(metadataDTO);
                certificatesList.add(certificateMetadata);
            } catch (APIImportExportException e) {
                log.error("Error while writing the certificate content. For alias: " + metadataDTO.getAlias(), e);
            } finally {
                if (certificate != null) {
                    IOUtils.closeQuietly(certificate);
                }
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
     * @param userName              Username
     * @throws APIImportExportException If an error occurs while exporting meta information
     */
    public static void addAPIProductMetaInformationToArchive(String archivePath, APIProductDTO apiProductDtoToReturn,
                                                             ExportFormat exportFormat, APIProvider apiProvider,
                                                             String userName)
            throws APIImportExportException {

        CommonUtil.createDirectory(archivePath + File.separator + ImportExportConstants.META_INFO_DIRECTORY);

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            String formattedSwaggerJson = apiProvider.getAPIDefinitionOfAPIProduct(
                    APIMappingUtil.fromDTOtoAPIProduct(apiProductDtoToReturn, userName));
            writeToYamlOrJson(archivePath +
                    ImportExportConstants.SWAGGER_DEFINITION_LOCATION, exportFormat, formattedSwaggerJson);

            if (log.isDebugEnabled()) {
                log.debug("Meta information retrieved successfully for API Product: " + apiProductDtoToReturn.getName());
            }

            JsonObject apiProductJsonObject = addTypeAndVersionToFile(ImportExportConstants.TYPE_API_PRODUCT,
                    ImportExportConstants.APIM_VERSION, gson.toJsonTree(apiProductDtoToReturn));
            String apiProductInJson = gson.toJson(apiProductJsonObject);
            writeToYamlOrJson(archivePath +
                    ImportExportConstants.API_FILE_LOCATION, exportFormat, apiProductInJson);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Swagger definition for API Product: "
                    + apiProductDtoToReturn.getName();
            throw new APIImportExportException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Error while saving as YAML for API Product: " + apiProductDtoToReturn.getName();
            throw new APIImportExportException(errorMessage, e);
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
    public static void addDependentAPIsToArchive(String archivePath, APIProductDTO apiProductDtoToReturn, ExportFormat exportFormat,
                                                 APIProvider provider, String userName, Boolean isStatusPreserved)
            throws APIImportExportException, APIManagementException {
        String apisDirectoryPath = archivePath + File.separator + APIImportExportConstants.APIS_DIRECTORY;
        CommonUtil.createDirectory(apisDirectoryPath);

        List<ProductAPIDTO> apisList = apiProductDtoToReturn.getApis();
        for (ProductAPIDTO productAPIDTO : apisList) {
            String apiProductRequesterDomain = RestApiUtil.getLoggedInUserTenantDomain();
            API api = provider.getAPIbyUUID(productAPIDTO.getApiId(), apiProductRequesterDomain);
            APIDTO apiDtoToReturn = APIMappingUtil.fromAPItoDTO(api);
            File dependentAPI = exportApi(provider, api.getId(), apiDtoToReturn, userName, exportFormat,
                    isStatusPreserved);
            CommonUtil.extractArchive(dependentAPI, apisDirectoryPath);
        }
    }

    /**
     * Add the type and the version to the artifact file when exporting.
     *
     * @param type        Type of the artifact to be exported
     * @param version     API Manager version
     * @param jsonElement JSON element to be added as data
     */
    public static JsonObject addTypeAndVersionToFile(String type, String version, JsonElement jsonElement) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(APIConstants.TYPE, type);
        jsonObject.addProperty(APIConstants.API_DATA_VERSION, version);
        jsonObject.add(APIConstants.DATA, jsonElement);
        return jsonObject;
    }

    /**
     * Write the file content of an API or API related artifact based on the format.
     *
     * @param filePath     Path to the location where the file content should be written
     * @param exportFormat Format to be exported
     * @param fileContent  Content to be written
     */
    public static void writeToYamlOrJson(String filePath, ExportFormat exportFormat, String fileContent)
            throws APIImportExportException, IOException {
        switch (exportFormat) {
            case YAML:
                String fileInYaml = CommonUtil.jsonToYaml(fileContent);
                CommonUtil.writeFile(filePath + ImportExportConstants.YAML_EXTENSION, fileInYaml);
                break;
            case JSON:
                CommonUtil.writeFile(filePath + ImportExportConstants.JSON_EXTENSION, fileContent);
        }
    }
}
