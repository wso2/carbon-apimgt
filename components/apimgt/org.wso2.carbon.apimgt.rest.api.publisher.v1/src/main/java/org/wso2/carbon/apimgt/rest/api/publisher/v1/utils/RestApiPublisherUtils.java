/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.wso2.carbon.apimgt.api.APIComplianceException;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.restapi.publisher.ApisApiServiceImplUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OrganizationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.APIConstants.GOVERNANCE_COMPLIANCE_ERROR_MESSAGE;

public class RestApiPublisherUtils {

    private static final Log log = LogFactory.getLog(RestApiPublisherUtils.class);

    /**
     * Attaches a file to the specified document
     *
     * @param apiId         identifier of the API, the document belongs to
     * @param documentation Documentation object
     * @param inputStream   input Stream containing the file
     * @param fileDetails   file details object as cxf Attachment
     * @param organization  identifier of an organization
     * @throws APIManagementException if unable to add the file
     */
    public static void attachFileToDocument(String apiId, Documentation documentation, InputStream inputStream,
                                            Attachment fileDetails, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String documentId = documentation.getId();
        String randomFolderName = RandomStringUtils.randomAlphanumeric(10);
        String tmpFolder = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator
                + RestApiConstants.DOC_UPLOAD_TMPDIR + File.separator + randomFolderName;
        File docFile = new File(tmpFolder);

        boolean folderCreated = docFile.mkdirs();
        if (!folderCreated) {
            RestApiUtil.handleInternalServerError("Failed to add content to the document " + documentId, log);
        }

        try {
            ContentDisposition contentDisposition = fileDetails.getContentDisposition();
            String filename = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
            if (StringUtils.isBlank(filename)) {
                filename = RestApiConstants.DOC_NAME_DEFAULT + randomFolderName;
                log.warn(
                        "Couldn't find the name of the uploaded file for the document " + documentId + ". Using name '"
                                + filename + "'");
            }

            //APIIdentifier apiIdentifier = APIMappingUtil
            //        .getAPIIdentifierFromUUID(apiId, tenantDomain);

            Path resolvedPath = resolveFilePath(docFile.getAbsolutePath(), filename);

            RestApiUtil.transferFile(inputStream, resolvedPath.getFileName().toString(), resolvedPath.getParent().toString());
            byte[] fileBytes = FileUtils.readFileToByteArray(new File(resolvedPath.toString()));
            String mediaType = detectAndValidateMediaType(fileBytes, filename);
            try (InputStream uploadStream = new ByteArrayInputStream(fileBytes)) {
                PublisherCommonUtils.addDocumentationContentForFile(uploadStream, mediaType, filename, apiProvider,
                        apiId, documentId, organization);
            }
        } catch (FileNotFoundException e) {
            RestApiUtil.handleInternalServerError("Unable to read the file from path ", e, log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error processing file upload for document: " + documentId, e, log);
        } finally {
            FileUtils.deleteQuietly(docFile);
        }
    }

    /**
     * This method validates monetization properties
     *
     * @param monetizationProperties map of monetization properties
     * @return error message if there is an validation error with monetization properties.
     */
    public static String validateMonetizationProperties(Map<String, String> monetizationProperties) {

        if (monetizationProperties != null) {
            for (Map.Entry<String, String> entry : monetizationProperties.entrySet()) {
                String monetizationPropertyKey = entry.getKey().trim();
                String propertyValue = entry.getValue();
                if (monetizationPropertyKey.contains(" ")) {
                    return "Monetization property names should not contain space character. " +
                            "Monetization property '" + monetizationPropertyKey + "' "
                            + "contains space in it.";
                }
                // Maximum allowable characters of registry property name and value is 100 and 1000.
                // Hence we are restricting them to be within 80 and 900.
                if (monetizationPropertyKey.length() > 80) {
                    return "Monetization property name can have maximum of 80 characters. " +
                            "Monetization property '" + monetizationPropertyKey + "' + contains "
                            + monetizationPropertyKey.length() + "characters";
                }
                if (propertyValue.length() > 900) {
                    return "Monetization property value can have maximum of 900 characters. " +
                            "Property '" + monetizationPropertyKey + "' + "
                            + "contains a value with " + propertyValue.length() + "characters";
                }
            }
        }
        return "";
    }

    /**
     * Attaches a file to the specified product document
     *
     * @param productId identifier of the API Product, the document belongs to
     * @param documentation Documentation object
     * @param inputStream input Stream containing the file
     * @param fileDetails file details object as cxf Attachment
     * @param organization organization of the API
     * @throws APIManagementException if unable to add the file
     */
    public static void attachFileToProductDocument(String productId, Documentation documentation, InputStream inputStream,
            Attachment fileDetails, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String documentId = documentation.getId();
        String randomFolderName = RandomStringUtils.randomAlphanumeric(10);
        String tmpFolder = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator
                + RestApiConstants.DOC_UPLOAD_TMPDIR + File.separator + randomFolderName;
        File docFile = new File(tmpFolder);

        boolean folderCreated = docFile.mkdirs();
        if (!folderCreated) {
            RestApiUtil.handleInternalServerError("Failed to add content to the document " + documentId, log);
        }

        try {
            ContentDisposition contentDisposition = fileDetails.getContentDisposition();
            String filename = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
            if (StringUtils.isBlank(filename)) {
                filename = RestApiConstants.DOC_NAME_DEFAULT + randomFolderName;
                log.warn(
                        "Couldn't find the name of the uploaded file for the document " + documentId + ". Using name '"
                                + filename + "'");
            }
            //APIProductIdentifier productIdentifier = APIMappingUtil
            //        .getAPIProductIdentifierFromUUID(productId, tenantDomain);

            Path resolvedPath = resolveFilePath(docFile.getAbsolutePath(), filename);

            RestApiUtil.transferFile(inputStream, resolvedPath.getFileName().toString(), resolvedPath.getParent().toString());
            byte[] fileBytes = FileUtils.readFileToByteArray(new File(resolvedPath.toString()));
            String mediaType = detectAndValidateMediaType(fileBytes, filename);
            try (InputStream uploadStream = new ByteArrayInputStream(fileBytes)) {
                PublisherCommonUtils.addDocumentationContentForFile(uploadStream, mediaType, filename, apiProvider,
                        productId, documentId, organization);
            }
        } catch (FileNotFoundException e) {
            RestApiUtil.handleInternalServerError("Unable to read the file from path ", e, log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error processing file upload for document: " + documentId, e, log);
        } finally {
            FileUtils.deleteQuietly(docFile);
        }
    }

    /**
     * This method will validate the given xml content for the syntactical correctness
     *
     * @param xmlContent string of xml content
     * @return true if the xml content is valid, false otherwise
     * @throws APIManagementException
     */
    public static boolean validateXMLSchema(String xmlContent) throws APIManagementException {
        xmlContent = "<xml>" + xmlContent + "</xml>";
        DocumentBuilderFactory factory = APIUtil.getSecuredDocumentBuilder();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(new InputSource(new StringReader(xmlContent)));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Error occurred while parsing the provided xml content.", e);
            return false;
        }
        return true;
    }

    /**
     * This method is to get the default SOAP API Resource definition. (SOAPAction, SOAP Request)
     * @return String
     * */
    public static String getSOAPOperation() {
        return "{\"/*\":{\"post\":{\"parameters\":[{\"schema\":{\"type\":\"string\"},\"description\":\"SOAP request.\","
            + "\"name\":\"SOAP Request\",\"required\":true,\"in\":\"body\"},"
                + "{\"description\":\"SOAPAction header for soap 1.1\",\"name\":\"SOAPAction\",\"type\":\"string\","
                + "\"required\":false,\"in\":\"header\"}], \"x-throttling-tier\": \"Unlimited\", " +
                "\"responses\":{\"200\":{\"description\":\"OK\"}}," +
                "\"security\":[{\"default\":[]}],\"consumes\":[\"text/xml\",\"application/soap+xml\"]}}}";
    }

    /**
     * This method is used to read input stream of a file and return the string content.
     * @return String
     * @throws IOException
     * */
    public static String readInputStream (InputStream fileInputStream, Attachment fileDetail) throws IOException {

        String content = null;
        if (fileInputStream != null) {
            String fileName = fileDetail.getDataHandler().getName();

            String fileContentType = URLConnection.guessContentTypeFromName(fileName);

            if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
                fileContentType = fileDetail.getContentType().toString();
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(fileInputStream, outputStream);
            byte[] sequenceBytes = outputStream.toByteArray();
            InputStream inSequenceStream = new ByteArrayInputStream(sequenceBytes);
            content = IOUtils.toString(inSequenceStream, StandardCharsets.UTF_8.name());
        }
        return content;
    }

    public static String getContentType(Attachment fileDetail) {
        String fileName = fileDetail.getDataHandler().getName();
        String fileContentType = URLConnection.guessContentTypeFromName(fileName);

        if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
            fileContentType = fileDetail.getContentType().toString();
        }
        return fileContentType;
    }

    public static File exportCustomBackendData(String seq, String seqName) throws APIManagementException {
        try {
            // Provided Sequence Name by the user
            String customBackendName = seqName;
            if (!customBackendName.contains(".xml")) {
                customBackendName = seqName + APIConstants.SYNAPSE_POLICY_DEFINITION_EXTENSION_XML;
            }
            CommonUtil.writeFile(customBackendName, seq);
            return new File(customBackendName);
        } catch (APIImportExportException ex) {
            throw new APIManagementException("Error when exporting Custom Backend: " + seqName, ex);
        }
    }

    public static File exportOperationPolicyData(OperationPolicyData policyData, String format)
            throws APIManagementException {

        File exportFolder = null;
        try {
            String sanitizedPolicyName = policyData.getSpecification().getName()
                    .replaceAll(APIConstants.POLICY_FILENAME_INVALID_CHARS_REGEX, "");
            exportFolder = CommonUtil.createTempDirectoryFromName(sanitizedPolicyName + "_" +
                    policyData.getSpecification().getVersion());
            String exportAPIBasePath = exportFolder.toString();
            String archivePath = exportAPIBasePath.concat(File.separator + sanitizedPolicyName);
            CommonUtil.createDirectory(archivePath);
            String policyName = archivePath + File.separator + sanitizedPolicyName;
            if (policyData.getSpecification() != null) {
                if (format.equalsIgnoreCase(ExportFormat.YAML.name())) {
                    CommonUtil.writeDtoToFile(policyName, ExportFormat.YAML,
                            ImportExportConstants.TYPE_POLICY_SPECIFICATION,
                            policyData.getSpecification(), ImportExportConstants.APIM_VERSION);
                } else if (format.equalsIgnoreCase(ExportFormat.JSON.name())) {
                    CommonUtil.writeDtoToFile(policyName, ExportFormat.JSON,
                            ImportExportConstants.TYPE_POLICY_SPECIFICATION,
                            policyData.getSpecification(), ImportExportConstants.APIM_VERSION);
                }
            }
            if (policyData.getSynapsePolicyDefinition() != null) {
                CommonUtil.writeFile(policyName + APIConstants.SYNAPSE_POLICY_DEFINITION_EXTENSION,
                        policyData.getSynapsePolicyDefinition().getContent());
            }
            if (policyData.getCcPolicyDefinition() != null) {
                CommonUtil.writeFile(policyName + APIConstants.CC_POLICY_DEFINITION_EXTENSION,
                        policyData.getCcPolicyDefinition().getContent());
            }

            CommonUtil.archiveDirectory(exportAPIBasePath);
            FileUtils.deleteQuietly(new File(exportAPIBasePath));
            return new File(exportAPIBasePath + APIConstants.ZIP_FILE_EXTENSION);
        } catch (APIImportExportException | IOException e) {
            throw new APIManagementException("Error while exporting operation policy", e);
        }
    }

    /**
     * This method will detect the Media Type of the given input stream
     *
     * @param inputStream stream containing the data of which the Media Type has to be detected
     * @return Media Type of the given input stream
     */
    public static String detectMediaType(InputStream inputStream) {
        try {
            TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
            Metadata metadata = new Metadata();
            return tikaConfig.getDetector().detect(TikaInputStream.get(inputStream), metadata).toString();
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Unable to read the input stream", e, log);
        }
        return null;
    }

    /**
     * Detects the MIME type of a file based on its byte content and validates whether the file extension matches the
     * detected MIME type.
     *
     * @param fileBytes the byte content of the file to validate
     * @param filename  the name of the file, used to extract the extension for validation
     * @return the detected MIME type as a string if the extension matches the MIME type
     * @throws APIManagementException if the fileBytes or filename is null, or if the MIME type detection or validation fails
     */
    public static String detectAndValidateMediaType(byte[] fileBytes, String filename) throws APIManagementException {
        if (fileBytes == null || filename == null) {
            throw new APIManagementException(ExceptionCodes.INVALID_MEDIA_TYPE_VALIDATION);
        }

        String detectedMimeType;
        try (InputStream mimeDetectStream = new ByteArrayInputStream(fileBytes)) {
            Tika tika = new Tika();
            detectedMimeType = tika.detect(mimeDetectStream, filename);
        } catch (Exception e) {
            throw new APIManagementException("Error detecting media type", e,
                    ExceptionCodes.INVALID_MEDIA_TYPE_VALIDATION);
        }

        int lastDot = filename.lastIndexOf('.');
        String fileExtension = (lastDot == -1) ? "" : filename.substring(lastDot).toLowerCase();

        boolean extensionMatches;
        MimeType mimeType;
        try {
            mimeType = MimeTypes.getDefaultMimeTypes().forName(detectedMimeType);
        } catch (MimeTypeException e) {
            throw new APIManagementException("Error resolving expected extension", e,
                    ExceptionCodes.from(ExceptionCodes.INVALID_MEDIA_TYPE_VALIDATION, fileExtension, detectedMimeType));
        }
        Set<String> validExtensions = new HashSet<>(mimeType.getExtensions());
        extensionMatches = validExtensions.stream().anyMatch(ext -> ext.equalsIgnoreCase(fileExtension));

        if (!extensionMatches) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INVALID_MEDIA_TYPE_VALIDATION, fileExtension, detectedMimeType));
        }

        return detectedMimeType;
    }

    /**
     * This method will validate the given input stream for the allowed Media Types
     *
     * @param fileInputStream stream containing the thumbnail data of which the content has to be validated
     * @return input stream containing the thumbnail data
     * @throws APIManagementException if error occurs while validating the thumbnail content
     */
    public static InputStream validateThumbnailContent(InputStream fileInputStream) throws APIManagementException {
        ByteArrayOutputStream outputStream = null;
        ByteArrayInputStream inputStream;
        try {
            // Convert the InputStream to a bytes array to be able to re-use it.
            outputStream = new ByteArrayOutputStream();
            IOUtils.copy(fileInputStream, outputStream);
            byte[] inputStreamBytes = outputStream.toByteArray();

            // Detect Media Type and validate.
            inputStream = new ByteArrayInputStream(inputStreamBytes);
            if (inputStream.available() > 0) {
                log.debug("Validating thumbnail content");
                String fileMediaType = RestApiPublisherUtils.detectMediaType(inputStream);
                if (log.isDebugEnabled()) {
                    log.debug("Detected Media Type during thumbnail content validation : " + fileMediaType);
                }
                if (StringUtils.isBlank(fileMediaType) || !RestApiConstants.ALLOWED_THUMBNAIL_MEDIA_TYPES
                        .contains(fileMediaType.toLowerCase())) {
                    RestApiUtil.handleBadRequest(
                            "Media Type of provided thumbnail is not supported. Supported Media Types are image/jpeg, "
                                    + "image/png, image/gif and image/svg+xml", log);
                }

                // Convert svg images to png. This is done to prevent scripts within svg images from executing.
                if (RestApiConstants.SVG_MEDIA_TYPE.equals(fileMediaType)) {
                    log.debug("Converting svg image to png format");
                    outputStream = new ByteArrayOutputStream();
                    TranscoderInput input_svg_image = new TranscoderInput(inputStream);
                    TranscoderOutput output_png_image = new TranscoderOutput(outputStream);
                    PNGTranscoder my_converter = new PNGTranscoder();
                    my_converter.transcode(input_svg_image, output_png_image);
                    inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                }
            }
        } catch (TranscoderException | IOException e) {
            throw new APIManagementException("Error while validating thumbnail content", e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return inputStream;
    }

    /**
     * This method will retrieve the Media Type of the processed input stream
     *
     * @param inputStream stream data which has been processed
     * @param fileDetail object containing meta data of the file
     * @return Media Type of the processed input stream
     */
    public static String getMediaType(InputStream inputStream, Attachment fileDetail) throws IOException {
        String fileMediaType;
        if (inputStream.available() > 0) {
            // Since the inputStream contains data, this will be a thumbnail uploading scenario.
            fileMediaType = RestApiPublisherUtils.detectMediaType(inputStream);
        } else {
            // Since the inputStream does not have data, this will be a thumbnail removing scenario.
            // Apache Tika would detect the media type of the empty stream as application/octet-stream. Hence,
            // retrieving the media type from the fileDetail object.
            fileMediaType = fileDetail.getContentType().toString();
        }
        if (log.isDebugEnabled()) {
            log.debug("Media Type of thumbnail to be uploaded : " + fileMediaType);
        }
        if (StringUtils.isBlank(fileMediaType)) {
            RestApiUtil.handleBadRequest(
                    "Media Type of provided thumbnail is not supported. Supported Media Types are image/jpeg, "
                            + "image/png, image/gif and image/svg+xml", log);
        }
        return fileMediaType;
    }

    /**
     * Resolves an untrusted user-specified path against the base directory.
     * Paths that try to escape the base directory are rejected.
     * @param baseDirPathString the absolute path of the base directory that all
     *                     user-specified paths should be within
     * @param userPathString  the untrusted path provided by the user
     * @return Resolved Path
     * @throws APIManagementException if resolution fails.
     */
    private static Path resolveFilePath(final String baseDirPathString,
                                        final String userPathString) throws APIManagementException {
        Path baseDirPath = Paths.get(baseDirPathString);
        Path userPath = Paths.get(userPathString);
        if (!baseDirPath.isAbsolute()) {
            throw new APIManagementException("Invalid base path provided." +
                    " Base path must be absolute. Base Path: " + baseDirPath);
        }

        if (userPath.isAbsolute()){
            throw new APIManagementException("Invalid user path provided." +
                    " User path should not be absolute. User Path: " + userPath);
        }

        /*
         * Combines the absolute base directory path and the user-specified relative path.
         * Then, normalizes the path to handle any ".." elements in the userPath.
         * For example, if the baseDirPath is "/foo/bar/baz" and userPath is "../attack",
         * the resulting resolvedPath will be "/foo/bar/attack".
         */
        final Path resolvedPath = baseDirPath.resolve(userPath).normalize();

        /*
         * Verifies that the resolved path is still within the expected base directory.
         * If the resolved path does not start with the base directory path,
         * it indicates an attempt to escape the intended directory structure.
         */
        if (!resolvedPath.startsWith(baseDirPath.normalize())) {
            throw new APIManagementException("Error resolving path. The user path attempts" +
                    " to escape the base directory.");
        }

        return resolvedPath;
    }

    /**
     * Fetches subscription policies for the relevant organization ID.
     * @param apiInfo           APIDTO object
     * @param organizationID    Organziation ID
     * @return                  List of subscription policies
     */
    public static List<String> getSubscriptionPoliciesForOrganization(APIDTO apiInfo, String organizationID) {

        if (organizationID == null) {
            return apiInfo.getPolicies();
        }
        List<String> policies = new ArrayList<>();
        List<OrganizationPoliciesDTO> organizationPoliciesDTOs = apiInfo.getOrganizationPolicies();
        if (organizationPoliciesDTOs != null && !organizationPoliciesDTOs.isEmpty()) {
            for (OrganizationPoliciesDTO organizationPoliciesDTO : organizationPoliciesDTOs) {
                if (StringUtils.equals(organizationID, organizationPoliciesDTO.getOrganizationID())) {
                    policies = organizationPoliciesDTO.getPolicies();
                    break;
                }
            }
        }
        return policies;
    }

    public static APIDTO importOpenAPIDefinition(InputStream definition, String definitionUrl, String inlineDefinition,
                                           APIDTO apiDTOFromProperties, Attachment fileDetail, ServiceEntry service,
                                           String organization) throws APIManagementException {
        // Validate and retrieve the OpenAPI definition
        Map validationResponseMap = null;
        boolean isServiceAPI = false;

        if (service != null) {
            isServiceAPI = true;
        }
        try {
            validationResponseMap = validateOpenAPIDefinition(definitionUrl, definition, fileDetail, inlineDefinition,
                    true, isServiceAPI);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while validating API Definition", e, log);
        }

        OpenAPIDefinitionValidationResponseDTO validationResponseDTO =
                (OpenAPIDefinitionValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
        APIDefinitionValidationResponse validationResponse =
                (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (!validationResponseDTO.isIsValid()) {
            ErrorDTO errorDTO = APIMappingUtil.getErrorDTOFromErrorListItems(validationResponseDTO.getErrors());
            throw RestApiUtil.buildBadRequestException(errorDTO);
        }

        // Only HTTP or WEBHOOK type APIs should be allowed
        if (!(APIDTO.TypeEnum.HTTP.equals(apiDTOFromProperties.getType())
                || APIDTO.TypeEnum.WEBHOOK.equals(apiDTOFromProperties.getType())
                || APIDTO.TypeEnum.MCP.equals(apiDTOFromProperties.getType()))) {
            throw RestApiUtil.buildBadRequestException(
                    "The API's type is not supported when importing an OpenAPI definition");
        }
        // Import the API and Definition
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // Add description from definition if it is not defined by user
        if (validationResponseDTO.getInfo().getDescription() != null
                && apiDTOFromProperties.getDescription() == null) {
            apiDTOFromProperties.setDescription(validationResponse.getInfo().getDescription());
        }
        if (isServiceAPI) {
            apiDTOFromProperties.setType(PublisherCommonUtils.getAPIType(service.getDefinitionType(), null));
        }
        API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(apiDTOFromProperties, apiProvider,
                RestApiCommonUtil.getLoggedInUsername(), organization);
        boolean syncOperations = !apiDTOFromProperties.getOperations().isEmpty();
        Map<String, String> complianceResult = PublisherCommonUtils.checkGovernanceComplianceSync(apiToAdd.getUuid(),
                APIMGovernableState.API_CREATE, ArtifactType.API, organization, null, null);
        if (!complianceResult.isEmpty()
                && complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY) != null
                && !Boolean.parseBoolean(complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY))) {
            throw new APIComplianceException(complianceResult.get(GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
        }
        API addedAPI = ApisApiServiceImplUtils.importAPIDefinition(apiToAdd, apiProvider, organization,
                service, validationResponse, isServiceAPI, syncOperations);
        PublisherCommonUtils.checkGovernanceComplianceAsync(addedAPI.getUuid(), APIMGovernableState.API_CREATE,
                ArtifactType.API, organization);
        return APIMappingUtil.fromAPItoDTO(addedAPI);
    }

    /**
     * Validate the provided OpenAPI definition (via file or url) and return a Map with the validation response
     * information.
     *
     * @param url             OpenAPI definition url
     * @param fileInputStream file as input stream
     * @param apiDefinition   Swagger API definition String
     * @param returnContent   whether to return the content of the definition in the response DTO
     * @return Map with the validation response information. A value with key 'dto' will have the response DTO
     * of type OpenAPIDefinitionValidationResponseDTO for the REST API. A value with key 'model' will have the
     * validation response of type APIDefinitionValidationResponse coming from the impl level.
     */
    public static Map validateOpenAPIDefinition(String url, InputStream fileInputStream, Attachment fileDetail,
                                                String apiDefinition, Boolean returnContent, Boolean isServiceAPI) throws APIManagementException {
        //validate inputs
        handleInvalidParams(fileInputStream, fileDetail, url, apiDefinition, isServiceAPI);
        String fileName = null;

        OpenAPIDefinitionValidationResponseDTO responseDTO;
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (fileDetail != null) {
            fileName = fileDetail.getContentDisposition().getFilename();
        }
        validationResponse = ApisApiServiceImplUtils.validateOpenAPIDefinition(url, fileInputStream, apiDefinition, fileName, returnContent);
        responseDTO = APIMappingUtil.getOpenAPIDefinitionValidationResponseFromModel(validationResponse,
                returnContent);

        Map response = new HashMap();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);
        return response;
    }

    /**
     * Validate API import definition/validate definition parameters
     *
     * @param fileInputStream file content stream
     * @param url             URL of the definition
     * @param apiDefinition   Swagger API definition String
     */
    public static void handleInvalidParams(InputStream fileInputStream, Attachment fileDetail, String url,
                                           String apiDefinition, Boolean isServiceAPI) {

        String msg = "";
        boolean isFileSpecified = (fileInputStream != null && fileDetail != null &&
                fileDetail.getContentDisposition() != null && fileDetail.getContentDisposition().getFilename() != null)
                || (fileInputStream != null && isServiceAPI);
        if (url == null && !isFileSpecified && apiDefinition == null) {
            msg = "One out of 'file' or 'url' or 'inline definition' should be specified";
        }

        boolean isMultipleSpecificationGiven = (isFileSpecified && url != null) || (isFileSpecified &&
                apiDefinition != null) || (apiDefinition != null && url != null);
        if (isMultipleSpecificationGiven) {
            msg = "Only one of 'file', 'url', and 'inline definition' should be specified";
        }

        if (StringUtils.isNotBlank(msg)) {
            RestApiUtil.handleBadRequest(msg, log);
        }
    }
}
