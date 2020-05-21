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

package org.wso2.carbon.apimgt.impl.importexport.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.axiom.om.OMElement;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIMRegistryServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManager;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.CertificateDetail;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportManager;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This is the util class which consists of all the functions for exporting API.
 */
public class APIExportUtil {

    private static final Log log = LogFactory.getLog(APIExportUtil.class);
    private static final String IN = "in";
    private static final String OUT = "out";
    private static final String SOAPTOREST = "SoapToRest";

    private APIExportUtil() {
    }

    /**
     * This method retrieves all meta information and registry resources required for an API to
     * recreate.
     *
     * @param archiveBasePath   temp location to save the API artifacts
     * @param apiToReturn       Exporting API
     * @param userName          User name of the requester
     * @param provider          API Provider
     * @param exportFormat      Export format of the API meta data, could be yaml or json
     * @param isStatusPreserved Whether API status is preserved while export
     * @throws APIImportExportException If an error occurs while retrieving API related resources
     */
    public static void retrieveApiToExport(String archiveBasePath, API apiToReturn, APIProvider provider, String userName,
                                           boolean isStatusPreserved, ExportFormat exportFormat)
            throws APIImportExportException {

        UserRegistry registry;
        APIIdentifier apiIDToReturn = apiToReturn.getId();
        String archivePath = archiveBasePath.concat(File.separator + apiIDToReturn.getApiName() + "-"
                + apiIDToReturn.getVersion());
        int tenantId = APIUtil.getTenantId(userName);

        try {
            registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
            //directory creation
            CommonUtil.createDirectory(archivePath);

            //export thumbnail
            exportAPIThumbnail(archivePath, apiIDToReturn, registry);
            exportSOAPToRESTMediation(archivePath, apiIDToReturn, registry);

            //export documents
            List<Documentation> docList = provider.getAllDocumentation(apiIDToReturn, userName);
            if (!docList.isEmpty()) {
                exportAPIDocumentation(archivePath, docList, apiIDToReturn, registry, exportFormat);
            } else if (log.isDebugEnabled()) {
                log.debug("No documentation found for API: " + apiIDToReturn + ". Skipping API documentation export.");
            }

            //export wsdl
            if (StringUtils.isNotEmpty(apiToReturn.getWsdlUrl())) {
                exportWSDL(archivePath, apiIDToReturn, registry);
            } else if (log.isDebugEnabled()) {
                log.debug("No WSDL URL found for API: " + apiIDToReturn + ". Skipping WSDL export.");
            }

            //export sequences
            exportSequences(archivePath, apiToReturn, registry);

            //set API status to created if status is not preserved
            if (!isStatusPreserved) {
                apiToReturn.setStatus(APIConstants.CREATED);
            }

            //export certificates
            exportEndpointCertificates(archivePath, apiToReturn, tenantId, exportFormat);

            //export meta information
            exportAPIMetaInformation(archivePath, apiToReturn, registry, exportFormat, provider);
            
            //export mTLS authentication related certificates
            if(provider.isClientCertificateBasedAuthenticationConfigured()) {
                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL enabled. Exporting client certificates.");
                }
                ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(apiToReturn);
                APIAndAPIProductCommonUtil.exportClientCertificates(archivePath, apiTypeWrapper, tenantId, provider, exportFormat);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Unable to retrieve API Documentation for API: " + apiIDToReturn.getApiName()
                    + StringUtils.SPACE + APIConstants.API_DATA_VERSION + " : " + apiIDToReturn.getVersion();
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (RegistryException e) {
            String errorMessage = "Error while getting governance registry for tenant: " + tenantId;
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Retrieve thumbnail image for the exporting API and store it in the archive directory.
     *
     * @param apiIdentifier ID of the requesting API
     * @param registry      Current tenant registry
     * @throws APIImportExportException If an error occurs while retrieving image from the registry or
     *                                  storing in the archive directory
     */
    private static void exportAPIThumbnail(String archivePath, APIIdentifier apiIdentifier, Registry registry)
            throws APIImportExportException {
        APIAndAPIProductCommonUtil.exportAPIOrAPIProductThumbnail(archivePath, apiIdentifier, registry);
    }


    /**
     * Retrieve SOAP to REST mediation logic for the exporting API and store it in the archive directory
     *
     * @param apiIdentifier ID of the requesting API
     * @param registry      Current tenant registry
     * @throws APIImportExportException If an error occurs while retrieving image from the registry or
     *                            storing in the archive directory
     */
    private static void exportSOAPToRESTMediation(String archivePath, APIIdentifier apiIdentifier, Registry registry)
            throws APIImportExportException {
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
            log.error("I/O error while  writing API SOAP to REST logic to file", e);
            throw new APIImportExportException("I/O error while writing API SOAP to REST logic to file", e);
        } catch (RegistryException e) {
            log.error("Error while retrieving API SOAP to REST logic ", e);
            throw new APIImportExportException("Error while retrieving SOAP to REST logic", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Retrieve documentation for the exporting API and store it in the archive directory.
     * FILE, INLINE, MARKDOWN and URL documentations are handled.
     *
     * @param apiIdentifier ID of the requesting API
     * @param registry      Current tenant registry
     * @param docList       documentation list of the exporting API
     * @param exportFormat  Format for export
     * @throws APIImportExportException If an error occurs while retrieving documents from the
     *                                  registry or storing in the archive directory
     */
    private static void exportAPIDocumentation(String archivePath, List<Documentation> docList,
                                               APIIdentifier apiIdentifier, Registry registry, ExportFormat exportFormat)
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
                    resourcePath = APIUtil.getDocumentationFilePath(apiIdentifier, localFileName);
                    localDocDirectoryPath += File.separator + APIImportExportConstants.FILE_DOCUMENT_DIRECTORY;
                    doc.setFilePath(localFileName);
                } else if (Documentation.DocumentSourceType.INLINE.toString().equalsIgnoreCase(sourceType)
                        || Documentation.DocumentSourceType.MARKDOWN.toString().equalsIgnoreCase(sourceType)) {
                    //Inline/Markdown content file name would be same as the documentation name
                    //Markdown content files will also be stored in InlineContents directory
                    localFileName = doc.getName();
                    resourcePath = APIUtil.getAPIDocPath(apiIdentifier) + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
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
                        String errorMessage = "Documentation resource for API: " + apiIdentifier.getApiName()
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
                log.debug("API Documentation retrieved successfully for API: " + apiIdentifier.getApiName()
                        + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiIdentifier.getVersion());
            }
        } catch (IOException e) {
            String errorMessage = "I/O error while writing API documentation to file for API: "
                    + apiIdentifier.getApiName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                    + apiIdentifier.getVersion();
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (RegistryException e) {
            String errorMessage = "Error while retrieving documentation for API: " + apiIdentifier.getApiName()
                    + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiIdentifier.getVersion();
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
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
    private static void exportWSDL(String archivePath, APIIdentifier apiIdentifier, Registry registry)
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
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (RegistryException e) {
            String errorMessage = "Error while retrieving WSDL: " + wsdlPath + " to file";
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Retrieve available custom sequences and API specific sequences for API export.
     *
     * @param api      exporting API
     * @param registry current tenant registry
     * @throws APIImportExportException If an error occurs while exporting sequences
     */
    private static void exportSequences(String archivePath, API api, Registry registry) throws APIImportExportException {

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
                        pathToExportedSequence += APIImportExportConstants.CUSTOM_TYPE + File.separator;
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
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (Exception e) {
            //APIUtil.buildOMElement() throws a generic exception
            String errorMessage = "Error while reading content for sequence: " + sequenceName + " from the registry";
            log.error(errorMessage, e);
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
                log.error(errorMessage, e);
                throw new APIImportExportException(errorMessage, e);
            } catch (XMLStreamException e) {
                String errorMessage = "Error while processing XML stream ";
                log.error(errorMessage, e);
                throw new APIImportExportException(errorMessage, e);
            }
        } else {
            String errorMessage = "Error while writing sequence of API: " + apiIdentifier.getApiName() + " to file.";
            log.error(errorMessage);
            throw new APIImportExportException(errorMessage);
        }
    }

    /**
     * Retrieve meta information of the API to export.
     * URL template information are stored in swagger.json definition while rest of the required
     * data are in api.json
     *
     * @param apiToReturn  API to be exported
     * @param registry     Current tenant registry
     * @param exportFormat Export format of file
     * @param apiProvider  API Provider
     * @throws APIImportExportException If an error occurs while exporting meta information
     */
    private static void exportAPIMetaInformation(String archivePath, API apiToReturn, Registry registry,
                                              ExportFormat exportFormat, APIProvider apiProvider)
            throws APIImportExportException, APIManagementException {

        CommonUtil.createDirectory(archivePath + File.separator + APIImportExportConstants.META_INFO_DIRECTORY);
        //Remove unnecessary data from exported Api
        cleanApiDataToExport(apiToReturn);

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            //If a web socket API is exported, it does not contain a swagger file.
            //Therefore swagger export is only required for REST, Graphql or SOAP based APIs
            if (!APIConstants.APITransportType.WS.toString().equalsIgnoreCase(apiToReturn.getType())) {
                //For Graphql APIs, the graphql schema definition, swagger and the serialized api object are exported.
                //For Graphql APIs, the URI templates and scopes are not cleared from the API object. Because we cannot
                //get graphql operation info from the swagger.
                if (StringUtils.equals(apiToReturn.getType(), APIConstants.APITransportType.GRAPHQL.toString())) {
                    String schemaContent = apiProvider.getGraphqlSchema(apiToReturn.getId());
                    CommonUtil.writeFile(archivePath + APIImportExportConstants.GRAPHQL_SCHEMA_DEFINITION_LOCATION,
                            schemaContent);
                    //Set the id of the scopes in API object to 0, as scope creation fails with a non existing id
                    Set<URITemplate> uriTemplates = apiToReturn.getUriTemplates();
                    for (URITemplate uriTemplate : uriTemplates) {
                        if (uriTemplate.getScope() != null) {
                            uriTemplate.getScope().setId("0");
                        }
                    }
                    if (apiToReturn.getScopes() != null) {
                        for (Scope scope : apiToReturn.getScopes()) {
                            scope.setId("0");
                        }
                    }
                } else {
                    //Swagger.json contains complete details about scopes. Therefore scope details and uri templates
                    //are removed from api.json.
                    apiToReturn.setScopes(new LinkedHashSet<>());
                    apiToReturn.setUriTemplates(new LinkedHashSet<>());
                }
                String swaggerDefinition = OASParserUtil.getAPIDefinition(apiToReturn.getId(), registry);
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
                    log.debug("Meta information retrieved successfully for API: " + apiToReturn.getId().getApiName()
                            + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiToReturn.getId().getVersion());
                }
            }

            String apiInJson = gson.toJson(apiToReturn);
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
            String errorMessage = "Error while retrieving Swagger definition for API: "
                    + apiToReturn.getId().getApiName() + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": "
                    + apiToReturn.getId().getVersion();
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Error while retrieving saving as YAML for API: " + apiToReturn.getId().getApiName()
                    + StringUtils.SPACE + APIConstants.API_DATA_VERSION + ": " + apiToReturn.getId().getVersion();
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Clean api by removing unnecessary details.
     *
     * @param api API to be exported
     */
    private static void cleanApiDataToExport(API api) throws APIManagementException {
        // Thumbnail will be set according to the importing environment. Therefore current URL is removed
        api.setThumbnailUrl(null);
        // WSDL file path will be set according to the importing environment. Therefore current path is removed
        api.setWsdlUrl(null);
        // If Secure Endpoint is enabled and "ExposeEndpointPassword" is 'false' in tenant-conf.json in registry,
        // secure endpoint password is removed, as it causes security issues. Need to add it manually when importing.
        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
        if (api.isEndpointSecured() && api.getEndpointUTPassword() != null && !isExposeEndpointPasswordEnabled(
                tenantDomain)) {
            api.setEndpointUTPassword(StringUtils.EMPTY);
        }
    }

    /**
     * Export endpoint certificates.
     *
     * @param api          API to be exported
     * @param tenantId     tenant id of the user
     * @param exportFormat Export format of file
     * @throws APIImportExportException If an error occurs while exporting endpoint certificates
     */
    private static void exportEndpointCertificates(String archivePath, API api, int tenantId, ExportFormat exportFormat)
            throws APIImportExportException {

        JSONObject endpointConfig;
        List<String> productionEndpoints;
        List<String> sandboxEndpoints;
        Set<String> uniqueEndpointURLs = new HashSet<>();
        List<CertificateDetail> endpointCertificatesDetails = new ArrayList<>();
        String endpointConfigString = api.getEndpointConfig();

        if (StringUtils.isEmpty(endpointConfigString)) {
            if(log.isDebugEnabled()) {
                log.debug("Endpoint Details are empty for API: " + api.getId().getApiName() + StringUtils.SPACE
                        + APIConstants.API_DATA_VERSION + ": " + api.getId().getVersion());
            }
            return;
        }
        try {
            JSONTokener tokener = new JSONTokener(endpointConfigString);
            endpointConfig = new JSONObject(tokener);
            productionEndpoints = getEndpointURLs(endpointConfig, APIConstants.API_DATA_PRODUCTION_ENDPOINTS,
                    api.getId().getApiName());
            sandboxEndpoints = getEndpointURLs(endpointConfig, APIConstants.API_DATA_SANDBOX_ENDPOINTS,
                    api.getId().getApiName());
            uniqueEndpointURLs.addAll(productionEndpoints); // Remove duplicate and append result
            uniqueEndpointURLs.addAll(sandboxEndpoints);
            for (String url : uniqueEndpointURLs) {
                List<CertificateDetail> list = getCertificateContentAndMetaData(tenantId, url);
                endpointCertificatesDetails.addAll(list);
            }
            if (!endpointCertificatesDetails.isEmpty()) {
                CommonUtil.createDirectory(archivePath + File.separator + APIImportExportConstants.META_INFO_DIRECTORY);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String element = gson.toJson(endpointCertificatesDetails,
                        new TypeToken<ArrayList<CertificateDetail>>() {
                        }.getType());

                switch (exportFormat) {
                    case YAML:
                        String yaml = CommonUtil.jsonToYaml(element);
                        CommonUtil.writeFile(archivePath + APIImportExportConstants.YAML_ENDPOINTS_CERTIFICATE_FILE,
                                yaml);
                        break;
                    case JSON:
                        CommonUtil.writeFile(archivePath + APIImportExportConstants.JSON_ENDPOINTS_CERTIFICATE_FILE,
                                element);
                }
            } else if (log.isDebugEnabled()) {
                log.debug("No endpoint certificates available for API: " + api.getId().getApiName() + StringUtils.SPACE
                        + APIConstants.API_DATA_VERSION + ": " + api.getId().getVersion() + ". Skipping certificate export.");
            }
        } catch (JSONException e) {
            String errorMsg = "Error in converting Endpoint config to JSON object in API: " + api.getId().getApiName();
            throw new APIImportExportException(errorMsg, e);
        } catch (IOException e) {
            String errorMessage = "Error while retrieving saving endpoint certificate details for API: "
                    + api.getId().getApiName() + " as YAML";
            log.error(errorMessage, e);
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Get endpoint url list from endpoint config.
     *
     * @param endpointConfig JSON converted endpoint config
     * @param type           end point type - production/sandbox
     * @return list of hostnames
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
     * This method used to check whether the config for exposing endpoint security password when getting API is enabled
     * or not in tenant-conf.json in registry.
     *
     * @return boolean as config enabled or not
     * @throws APIManagementException
     */
    private static boolean isExposeEndpointPasswordEnabled(String tenantDomainName)
            throws APIManagementException {
        org.json.simple.JSONObject apiTenantConfig;
        try {
            APIMRegistryServiceImpl apimRegistryService = new APIMRegistryServiceImpl();
            String content = apimRegistryService.getConfigRegistryResourceContent(tenantDomainName,
                    APIConstants.API_TENANT_CONF_LOCATION);
            if (content != null) {
                JSONParser parser = new JSONParser();
                apiTenantConfig = (org.json.simple.JSONObject) parser.parse(content);
                if (apiTenantConfig != null) {
                    Object value = apiTenantConfig.get(APIConstants.API_TENANT_CONF_EXPOSE_ENDPOINT_PASSWORD);
                    if (value != null) {
                        return Boolean.parseBoolean(value.toString());
                    }
                }
            }
        } catch (UserStoreException e) {
            String msg = "UserStoreException thrown when getting API tenant config from registry while reading " +
                    "ExposeEndpointPassword config";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            String msg = "RegistryException thrown when getting API tenant config from registry while reading " +
                    "ExposeEndpointPassword config";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "ParseException thrown when parsing API tenant config from registry while reading " +
                    "ExposeEndpointPassword config";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return false;
    }


    /**
     * Get Certificate MetaData and Certificate detail and build JSON list.
     *
     * @param tenantId tenant id of the user
     * @param url      url of the endpoint
     * @return list of certificate detail JSON objects
     * @throws APIImportExportException If an error occurs while retrieving endpoint certificate metadata and content
     */
    private static List<CertificateDetail> getCertificateContentAndMetaData(int tenantId, String url)
            throws APIImportExportException {

        List<CertificateDetail> certificateDetails = new ArrayList<>();
        List<CertificateMetadataDTO> certificateMetadataDTOS;
        CertificateManager certificateManager = CertificateManagerImpl.getInstance();

        try {
            certificateMetadataDTOS = certificateManager.getCertificates(tenantId, null, url);
        } catch (APIManagementException e) {
            String errorMsg = "Error retrieving certificate meta data. For tenantId: " + tenantId + " hostname: "
                    + url;
            throw new APIImportExportException(errorMsg, e);
        }

        certificateMetadataDTOS.forEach(metadataDTO -> {
            ByteArrayInputStream certificate = null;
            try {
                certificate = certificateManager.getCertificateContent(metadataDTO.getAlias());
                certificate.close();
                byte[] certificateContent = IOUtils.toByteArray(certificate);
                String encodedCertificate = new String(Base64.encodeBase64(certificateContent));
                CertificateDetail certificateDetail = new CertificateDetail();
                certificateDetail.setHostName(url);
                certificateDetail.setAlias(metadataDTO.getAlias());
                certificateDetail.setCertificate(encodedCertificate);
                certificateDetails.add(certificateDetail);
            } catch (APIManagementException e) {
                log.error("Error retrieving certificate content. For tenantId: " + tenantId + " hostname: "
                        + url + " alias: " + metadataDTO.getAlias(), e);
            } catch (IOException e) {
                log.error("Error while converting certificate content to Byte Array. For tenantId: " + tenantId
                        + " hostname: " + url + " alias: " + metadataDTO.getAlias(), e);
            } finally {
                if (certificate != null) {
                    IOUtils.closeQuietly(certificate);
                }
            }
        });
        return certificateDetails;
    }

    /**
     * Exports an API from API Manager for a given API ID. Meta information, API icon, documentation, WSDL
     * and sequences are exported. This service generates a zipped archive which contains all the above mentioned
     * resources for a given API.
     *
     * @param apiIdentifier
     * @param apiProvider
     * @param preserveStatus Preserve API status on export
     * @return Zipped file containing exported API
     */

    public static File exportApi(APIProvider apiProvider, APIIdentifier apiIdentifier, String userName,
                                 ExportFormat exportFormat, Boolean preserveStatus)
            throws APIImportExportException, APIManagementException {
        API api;
        APIImportExportManager apiImportExportManager;
        boolean isStatusPreserved = preserveStatus == null || preserveStatus;
        api = apiProvider.getAPI(apiIdentifier);
        ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(api);
        apiImportExportManager = new APIImportExportManager(apiProvider, userName);
        return apiImportExportManager.exportAPIOrAPIProductArchive(apiTypeWrapper, isStatusPreserved, exportFormat);
    }
}
