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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.netbeans.lib.cvsclient.commandLine.command.log;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RestApiPublisherUtils {

    private static final Log log = LogFactory.getLog(RestApiPublisherUtils.class);

    /**
     * Validate endpoint configurations of {@link APIDTO} for web socket endpoints
     *
     * @param api api model
     * @return validity of the web socket api
     */
    public static boolean isValidWSAPI(APIDTO api) {

        boolean isValid = false;

        if (api.getEndpointConfig() != null) {
            LinkedHashMap endpointConfig = (LinkedHashMap) api.getEndpointConfig();
            String prodEndpointUrl = String
                    .valueOf(((LinkedHashMap) endpointConfig.get("production_endpoints")).get("url"));
            String sandboxEndpointUrl = String
                    .valueOf(((LinkedHashMap) endpointConfig.get("sandbox_endpoints")).get("url"));
            isValid = prodEndpointUrl.startsWith("ws://") || prodEndpointUrl.startsWith("wss://");

            if (isValid) {
                isValid = sandboxEndpointUrl.startsWith("ws://") || sandboxEndpointUrl.startsWith("wss://");
            }
        }

        return isValid;
    }

    /**
     * To validate the roles against user roles and tenant roles.
     *
     * @param inputRoles Input roles.
     * @return relevant error string or empty string.
     * @throws APIManagementException API Management Exception.
     */
    public static String validateUserRoles(List<String> inputRoles) throws APIManagementException {

        String userName = RestApiUtil.getLoggedInUsername();
        String[] tenantRoleList = APIUtil.getRoleNames(userName);
        boolean isMatched = false;
        String[] userRoleList = null;

        if (APIUtil.hasPermission(userName, APIConstants.Permissions.APIM_ADMIN)) {
            isMatched = true;
        } else {
            userRoleList = APIUtil.getListOfRoles(userName);
        }
        if (inputRoles != null && !inputRoles.isEmpty()) {
            if (tenantRoleList != null || userRoleList != null) {
                for (String inputRole : inputRoles) {
                    if (!isMatched && userRoleList != null && APIUtil.compareRoleList(userRoleList, inputRole)) {
                        isMatched = true;
                    }
                    if (tenantRoleList != null && !APIUtil.compareRoleList(tenantRoleList, inputRole)) {
                        return "Invalid user roles found in accessControlRole list";
                    }
                }
                return isMatched ? "" : "This user does not have at least one role specified in API access control.";
            } else {
                return "Invalid user roles found";
            }
        }
        return "";
    }

    /**
     * To validate the additional properties.
     * Validation will be done for the keys of additional properties. Property keys should not contain spaces in it
     * and property keys should not conflict with reserved key words.
     *
     * @param additionalProperties Map<String, String>  properties to validate
     * @return error message if there is an validation error with additional properties.
     */
    public static String validateAdditionalProperties(Map<String, String> additionalProperties) {

        if (additionalProperties != null) {
            for (Map.Entry<String, String> entry : additionalProperties.entrySet()) {
                String propertyKey = entry.getKey().trim();
                String propertyValue = entry.getValue();
                if (propertyKey.contains(" ")) {
                    return "Property names should not contain space character. Property '" + propertyKey + "' "
                            + "contains space in it.";
                }
                if (Arrays.asList(APIConstants.API_SEARCH_PREFIXES).contains(propertyKey.toLowerCase())) {
                    return "Property '" + propertyKey + "' conflicts with the reserved keywords. Reserved keywords "
                            + "are [" + Arrays.toString(APIConstants.API_SEARCH_PREFIXES) + "]";
                }
                // Maximum allowable characters of registry property name and value is 100 and 1000. Hence we are
                // restricting them to be within 80 and 900.
                if (propertyKey.length() > 80) {
                    return "Property name can have maximum of 80 characters. Property '" + propertyKey + "' + contains "
                            + propertyKey.length() + "characters";
                }
                if (propertyValue.length() > 900) {
                    return "Property value can have maximum of 900 characters. Property '" + propertyKey + "' + "
                            + "contains a value with " + propertyValue.length() + "characters";
                }
            }
        }
        return "";
    }
    
    /**
     * To validate the roles against and tenant roles.
     *
     * @param inputRoles Input roles.
     * @return relevant error string or empty string.
     * @throws APIManagementException API Management Exception.
     */
    public static String validateRoles(List<String> inputRoles) throws APIManagementException {
        String userName = RestApiUtil.getLoggedInUsername();
        boolean isMatched = false;
        if (inputRoles != null && !inputRoles.isEmpty()) {
            String roleString = String.join(",", inputRoles);
            isMatched = APIUtil.isRoleNameExist(userName, roleString);
            if (!isMatched) {
                return "Invalid user roles found in visibleRoles list";
            }
        }
        return "";
    }

    /**
     * Attaches a file to the specified document
     *
     * @param apiId identifier of the API, the document belongs to
     * @param documentation Documentation object
     * @param inputStream input Stream containing the file
     * @param fileDetails file details object as cxf Attachment
     * @throws APIManagementException if unable to add the file
     */
    public static void attachFileToDocument(String apiId, Documentation documentation, InputStream inputStream,
                                            Attachment fileDetails) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String documentId = documentation.getId();
        String randomFolderName = RandomStringUtils.randomAlphanumeric(10);
        String tmpFolder = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator
                + RestApiConstants.DOC_UPLOAD_TMPDIR + File.separator + randomFolderName;
        File docFile = new File(tmpFolder);

        boolean folderCreated = docFile.mkdirs();
        if (!folderCreated) {
            RestApiUtil.handleInternalServerError("Failed to add content to the document " + documentId, log);
        }

        InputStream docInputStream = null;
        try {
            ContentDisposition contentDisposition = fileDetails.getContentDisposition();
            String filename = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
            if (StringUtils.isBlank(filename)) {
                filename = RestApiConstants.DOC_NAME_DEFAULT + randomFolderName;
                log.warn(
                        "Couldn't find the name of the uploaded file for the document " + documentId + ". Using name '"
                                + filename + "'");
            }
            APIIdentifier apiIdentifier = APIMappingUtil
                    .getAPIIdentifierFromUUID(apiId, tenantDomain);

            RestApiUtil.transferFile(inputStream, filename, docFile.getAbsolutePath());
            docInputStream = new FileInputStream(docFile.getAbsolutePath() + File.separator + filename);
            String mediaType = fileDetails.getHeader(RestApiConstants.HEADER_CONTENT_TYPE);
            mediaType = mediaType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : mediaType;
            apiProvider.addFileToDocumentation(apiIdentifier, documentation, filename, docInputStream, mediaType);
            apiProvider.updateDocumentation(apiIdentifier, documentation);
            docFile.deleteOnExit();
        } catch (FileNotFoundException e) {
            RestApiUtil.handleInternalServerError("Unable to read the file from path ", e, log);
        } finally {
            IOUtils.closeQuietly(docInputStream);
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
     * @throws APIManagementException if unable to add the file
     */
    public static void attachFileToProductDocument(String productId, Documentation documentation, InputStream inputStream,
            Attachment fileDetails) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String documentId = documentation.getId();
        String randomFolderName = RandomStringUtils.randomAlphanumeric(10);
        String tmpFolder = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator
                + RestApiConstants.DOC_UPLOAD_TMPDIR + File.separator + randomFolderName;
        File docFile = new File(tmpFolder);

        boolean folderCreated = docFile.mkdirs();
        if (!folderCreated) {
            RestApiUtil.handleInternalServerError("Failed to add content to the document " + documentId, log);
        }

        InputStream docInputStream = null;
        try {
            ContentDisposition contentDisposition = fileDetails.getContentDisposition();
            String filename = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
            if (StringUtils.isBlank(filename)) {
                filename = RestApiConstants.DOC_NAME_DEFAULT + randomFolderName;
                log.warn(
                        "Couldn't find the name of the uploaded file for the document " + documentId + ". Using name '"
                                + filename + "'");
            }
            APIProductIdentifier productIdentifier = APIMappingUtil
                    .getAPIProductIdentifierFromUUID(productId, tenantDomain);

            RestApiUtil.transferFile(inputStream, filename, docFile.getAbsolutePath());
            docInputStream = new FileInputStream(docFile.getAbsolutePath() + File.separator + filename);
            String mediaType = fileDetails.getHeader(RestApiConstants.HEADER_CONTENT_TYPE);
            mediaType = mediaType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : mediaType;
            apiProvider.addFileToProductDocumentation(productIdentifier, documentation, filename, docInputStream, mediaType);
            apiProvider.updateDocumentation(productIdentifier, documentation);
            docFile.deleteOnExit();
        } catch (FileNotFoundException e) {
            RestApiUtil.handleInternalServerError("Unable to read the file from path ", e, log);
        } finally {
            IOUtils.closeQuietly(docInputStream);
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
                + "\"required\":false,\"in\":\"header\"}],\"responses\":{\"200\":{\"description\":\"OK\"}}," +
                "\"security\":[{\"default\":[]}],\"consumes\":[\"text/xml\",\"application/soap+xml\"]}}}";
    }

    /**
     * This method retrieves the Swagger Definition for an API to be displayed
     * @param api API
     * @return String
     * */
    public static String retrieveSwaggerDefinition(API api, APIProvider apiProvider)
            throws APIManagementException {
        String apiSwagger = apiProvider.getOpenAPIDefinition(api.getId());
        APIDefinition parser = OASParserUtil.getOASParser(apiSwagger);
        return parser.getOASDefinitionForPublisher(api, apiSwagger);
    }
}
