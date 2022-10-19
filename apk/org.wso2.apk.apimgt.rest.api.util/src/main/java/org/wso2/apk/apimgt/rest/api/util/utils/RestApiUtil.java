/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.apk.apimgt.rest.api.util.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.apk.apimgt.api.APIDefinition;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.apk.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.apk.apimgt.api.ApplicationNameWhiteSpaceValidationException;
import org.wso2.apk.apimgt.api.ApplicationNameWithInvalidCharactersException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.DuplicateAPIException;
import org.wso2.apk.apimgt.api.model.ResourceFile;
import org.wso2.apk.apimgt.api.model.Scope;
import org.wso2.apk.apimgt.api.model.Tier;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.ConfigurationHolder;
import org.wso2.apk.apimgt.impl.definitions.OASParserUtil;
import org.wso2.apk.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.util.dto.ErrorDTO;
import org.wso2.apk.apimgt.rest.api.util.dto.ErrorListItemDTO;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.uri.template.URITemplateException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;

public class RestApiUtil {

    public static final Log log = LogFactory.getLog(RestApiUtil.class);
    private static Dictionary<org.wso2.uri.template.URITemplate, List<String>> uriToHttpMethodsMap;
    private static Dictionary<org.wso2.uri.template.URITemplate, List<String>> ETagSkipListURIToHttpMethodsMap;

    public static <T> ErrorDTO getConstraintViolationErrorDTO(Set<ConstraintViolation<T>> violations) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setDescription("Validation Error");
        errorDTO.setMessage("Bad Request");
        errorDTO.setCode(400l);
        errorDTO.setMoreInfo("");
        List<ErrorListItemDTO> errorListItemDTOs = new ArrayList<>();
        for (ConstraintViolation violation : violations) {
            ErrorListItemDTO errorListItemDTO = new ErrorListItemDTO();
            errorListItemDTO.setCode(400 + "_" + violation.getPropertyPath());
            errorListItemDTO.setMessage(violation.getPropertyPath() + ": " + violation.getMessage());
            errorListItemDTOs.add(errorListItemDTO);
        }
        errorDTO.setError(errorListItemDTOs);
        return errorDTO;
    }

    /**
     * Returns a generic errorDTO
     *
     * @param message specifies the error message
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(String message, Long code, String description){
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMoreInfo("");
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        return errorDTO;
    }

    /**
     * Returns a generic errorDTO from an Error Handler
     *
     * @param errorHandler ErrorHandler object containing the error information
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(ErrorHandler errorHandler){
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(errorHandler.getErrorCode());
        errorDTO.setMoreInfo("");
        errorDTO.setMessage(errorHandler.getErrorMessage());
        errorDTO.setDescription(errorHandler.getErrorDescription());
        return errorDTO;
    }

    /**
     * Returns a generic errorDTO from a list of Error Handlers
     *
     * @param errorHandlers A List of error handler objects containing the error information
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(List<ErrorHandler> errorHandlers) {
        ErrorDTO errorDTO = new ErrorDTO();
        for (int i = 0; i < errorHandlers.size(); i++) {
            if (i == 0) {
                ErrorHandler elementAt0 = errorHandlers.get(0);
                errorDTO.setCode(elementAt0.getErrorCode());
                errorDTO.setMoreInfo("");
                errorDTO.setMessage(elementAt0.getErrorMessage());
                errorDTO.setDescription(elementAt0.getErrorDescription());
            } else {
                ErrorListItemDTO errorListItemDTO = new ErrorListItemDTO();
                errorListItemDTO.setCode(errorHandlers.get(i).getErrorCode() + "");
                errorListItemDTO.setMessage(errorHandlers.get(i).getErrorMessage());
                errorListItemDTO.setDescription(errorHandlers.get(i).getErrorDescription());
                errorDTO.getError().add(errorListItemDTO);
            }
        }
        return errorDTO;
    }

    /**
     * Create a JAXRS Response object based on the provided ResourceFile
     *
     * @param fileNameWithoutExtension Filename without the extension. The extension is determined from the method
     * @param resourceFile ResourceFile object
     * @return JAXRS Response object
     */
    public static Response getResponseFromResourceFile(String fileNameWithoutExtension, ResourceFile resourceFile) {
        String contentType;
        String extension;
        if (resourceFile.getContentType().contains(APIConstants.APPLICATION_ZIP)) {
            contentType = APIConstants.APPLICATION_ZIP;
            extension = APIConstants.ZIP_FILE_EXTENSION;
        } else {
            contentType = APIConstants.APPLICATION_WSDL_MEDIA_TYPE;
            extension = APIConstants.WSDL_FILE_EXTENSION;
        }
        String filename = fileNameWithoutExtension + extension;
        return Response.ok(resourceFile.getContent(), contentType).header("Content-Disposition",
                "attachment; filename=\"" + filename + "\"" ).build();
    }

    /**
     * Returns the current logged in consumer's group id
     * @return group id of the current logged in user.
     */
    @SuppressWarnings("unchecked")
    public static String getLoggedInUserGroupId() throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        JSONObject loginInfoJsonObj = new JSONObject();
        try {
            loginInfoJsonObj.put("user", username);
            loginInfoJsonObj.put("isSuperTenant", tenantDomain.equals("carbon.super"));
            String loginInfoString = loginInfoJsonObj.toJSONString();
            String[] groupIdArr = getGroupIds(loginInfoString);
            String groupId = "";
            if (groupIdArr != null) {
                for (int i = 0; i < groupIdArr.length; i++) {
                    if (groupIdArr[i] != null) {
                        if (i == groupIdArr.length - 1) {
                            groupId = groupId + groupIdArr[i];
                        } else {
                            groupId = groupId + groupIdArr[i] + ",";
                        }
                    }
                }
            }
            return groupId;
        } catch (APIManagementException e) {
            String errorMsg = "Unable to get groupIds of user " + username;
            throw  new APIManagementException(errorMsg, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    private static String[] getGroupIds(String loginInfoString) throws APIManagementException {
        String groupingExtractorClass = APIUtil.getRESTApiGroupingExtractorImplementation();
        return APIUtil.getGroupIdsFromExtractor(loginInfoString, groupingExtractorClass);
    }

    /**
     * Returns the requested tenant according to the input x-tenant-header
     *
     * @return requested tenant domain
     */
    public static String getRequestedTenantDomain(String xTenantHeader) {
        if (StringUtils.isEmpty(xTenantHeader)) {
            return RestApiCommonUtil.getLoggedInUserTenantDomain();
        } else {
            return xTenantHeader;
        }
    }

    /**
     * This method uploads a given file to specified location
     *
     * @param uploadedInputStream input stream of the file
     * @param newFileName         name of the file to be created
     * @param storageLocation     destination of the new file
     * @throws APIManagementException if the file transfer fails
     */
    public static void transferFile(InputStream uploadedInputStream, String newFileName, String storageLocation)
            throws APIManagementException {
        FileOutputStream outFileStream = null;

        try {
            outFileStream = new FileOutputStream(new File(storageLocation, newFileName));
            int read;
            byte[] bytes = new byte[1024];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outFileStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            String errorMessage = "Error in transferring files.";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e, ExceptionCodes.INTERNAL_ERROR);
        } finally {
            IOUtils.closeQuietly(outFileStream);
        }
    }

    /**
     * Check if the specified throwable e is due to an authorization failure
     * @param e throwable to check
     * @return true if the specified throwable e is due to an authorization failure, false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToAuthorizationFailure(Throwable e) {
        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause instanceof AuthorizationFailedException
                || rootCause instanceof APIMgtAuthorizationFailedException;
    }


    /**
     * Check if the specified throwable e is happened as the provided meta information related to api is corrupted
     *
     * @param e throwable to check
     * @return true if the specified throwable e is happened as the provided meta information is corrupted, false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToMetaInfoIsCorrupted(Throwable e) {
        return detailedMessageMatches(e, "Error while reading API meta information from path");
    }

    /**
     * Check if the specified throwable e is happened as the provided throttling policy is missing
     *
     * @param e throwable to check
     * @return true if the specified throwable e is happened as the provided throttling policy is missing, false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToProvidedThrottlingPolicyMissing(Throwable e) {
        return detailedMessageMatches(e, "Invalid x-throttling tier") ||
                detailedMessageMatches(e, "Invalid API level throttling tier") ||
                detailedMessageMatches(e, "Invalid Product level throttling tier") ||
                detailedMessageMatches(e, "Invalid Subscription level throttling tier") ||
                detailedMessageMatches(e, "Invalid Application level throttling tier") ;
    }

    /**
     * Check if the specified throwable e is happened as the updated/new resource conflicting with an already existing
     * resource
     *
     * @param e throwable to check
     * @return true if the specified throwable e is happened as the updated/new resource conflicting with an already
     *   existing resource, false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToResourceAlreadyExists(Throwable e) {
        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause instanceof APIMgtResourceAlreadyExistsException || rootCause instanceof DuplicateAPIException;
    }

    /**
     * Check if the specified throwable e is happened as the updated/new application name contains leading or trailing
     * white spaces
     *
     * @param e throwable to check
     * @return true if the specified throwable e is happened as the updated/new application contains leading or trailing
     * whitespace, false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToApplicationNameWhiteSpaceValidation(Throwable e) {
        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause instanceof ApplicationNameWhiteSpaceValidationException;
    }

    /**
     * Check if the specified throwable e is happened as the updated/new application name contains invalid characters
     *
     * @param e throwable to check
     * @return true if the specified throwable e is happened as the updated/new application contains invalid characters
     * false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToApplicationNameWithInvalidCharacters(Throwable e) {
        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause instanceof ApplicationNameWithInvalidCharactersException;
    }

    /**
     * Check if the message of the root cause message of 'e' matches with the specified message
     *
     * @param e throwable to check
     * @param message error message
     * @return true if the message of the root cause of 'e' matches with 'message'
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean rootCauseMessageMatches (Throwable e, String message) {
        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause.getMessage().contains(message);
    }

    /**
     * Check if the message of the detailed message of 'e' matches with the specified message
     *
     * @param e       throwable to check
     * @param message error message
     * @return true if the message of the root cause of 'e' matches with 'message'
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean detailedMessageMatches(Throwable e, String message) {
        return e.getMessage().contains(message);
    }

    /**
     * Attempts to find the actual cause of the throwable 'e'
     *
     * @param e throwable
     * @return the root cause of 'e' if the root cause exists, otherwise returns 'e' itself
     */
    private static Throwable getPossibleErrorCause (Throwable e) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        rootCause = rootCause == null ? e : rootCause;
        return rootCause;
    }

    /**
     * Search the tier in the given collection of Tiers. Returns it if it is included there. Otherwise return null
     *
     * @param tiers    Tier Collection
     * @param tierName Tier to find
     * @return Matched tier with its name
     */
    public static Tier findTier(Collection<Tier> tiers, String tierName) {
        for (Tier tier : tiers) {
            if (tier.getName() != null && tierName != null && tier.getName().equals(tierName)) {
                return tier;
            }
        }
        return null;
    }


    /**
     * Returns the white-listed URIs and associated HTTP methods for REST API by reading api-manager.xml configuration
     *
     * @return A Dictionary with the white-listed URIs and the associated HTTP methods
     * @throws APIManagementException
     */
    private static Dictionary<org.wso2.uri.template.URITemplate, List<String>> getAllowedURIsToMethodsMapFromConfig()
            throws APIManagementException {

        Hashtable<org.wso2.uri.template.URITemplate, List<String>> uriToMethodsMap = new Hashtable<>();

        ConfigurationHolder apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        List<String> uriList = apiManagerConfiguration
                .getProperty(APIConstants.API_RESTAPI_ALLOWED_URI_URI);
        List<String> methodsList = apiManagerConfiguration
                .getProperty(APIConstants.API_RESTAPI_ALLOWED_URI_HTTPMethods);

        if (uriList != null && methodsList != null) {
            if (uriList.size() != methodsList.size()) {
                String errorMsg = "Provided White-listed URIs for REST API are invalid."
                        + " Every 'allowedURI' should include 'URI' and 'HTTPMethods' elements";
                log.error(errorMsg);
                return new Hashtable<>();
            }

            for (int i = 0; i < uriList.size(); i++) {
                String uri = uriList.get(i);
                uri = uri.replace("/{version}", "");
                try {
                    org.wso2.uri.template.URITemplate uriTemplate = new org.wso2.uri.template.URITemplate(uri);
                    String methodsForUri = methodsList.get(i);
                    List<String> methodListForUri = Arrays.asList(methodsForUri.split(","));
                    uriToMethodsMap.put(uriTemplate, methodListForUri);
                } catch (URITemplateException e) {
                    String msg = "Error in parsing uri " + uri + " when retrieving white-listed URIs for REST API";
                    log.error(msg, e);
                    throw new APIManagementException(msg, e);
                }
            }
        }
        return uriToMethodsMap;
    }

    /**
     * Returns the white-listed URIs and associated HTTP methods for REST API. If not already read before, reads
     * api-manager.xml configuration, store the results in a static reference and returns the results.
     * Otherwise returns previously stored the static reference object.
     *
     * @return A Dictionary with the white-listed URIs and the associated HTTP methods
     * @throws APIManagementException
     */
    public static Dictionary<org.wso2.uri.template.URITemplate, List<String>> getAllowedURIsToMethodsMap()
            throws APIManagementException {

        if (uriToHttpMethodsMap == null) {
            uriToHttpMethodsMap = getAllowedURIsToMethodsMapFromConfig();
        }
        return uriToHttpMethodsMap;
    }

    private static String removeLeadingAndTrailing(String base) {
        String result = base;
        if (base.startsWith("\"") || base.endsWith("\"")) {
            result = base.replace("\"", "");
        }
        return result.trim();
    }
    private static Dictionary<org.wso2.uri.template.URITemplate, List<String>> getETagSkipListToMethodsMapFromConfig()
            throws APIManagementException {
        Hashtable<org.wso2.uri.template.URITemplate, List<String>> uriToMethodsMap = new Hashtable<>();
        ConfigurationHolder apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        List<String> uriList = apiManagerConfiguration
                .getProperty(APIConstants.API_RESTAPI_ETAG_SKIP_URI_URI);
        List<String> methodsList = apiManagerConfiguration
                .getProperty(APIConstants.API_RESTAPI_ETAG_SKIP_URI_HTTPMETHOD);

        if (uriList != null && methodsList != null) {
            if (uriList.size() != methodsList.size()) {
                String errorMsg = "Provided ETag skip list URIs for Store REST API are invalid.";
                log.error(errorMsg);
                return new Hashtable<>();
            }

            for (int i = 0; i < uriList.size(); i++) {
                String uri = uriList.get(i);
                try {
                    org.wso2.uri.template.URITemplate uriTemplate = new org.wso2.uri.template.URITemplate(uri);
                    String methodsForUri = methodsList.get(i);
                    List<String> methodListForUri = Arrays.asList(methodsForUri.split(","));
                    uriToMethodsMap.put(uriTemplate, methodListForUri);
                } catch (URITemplateException e) {
                    String msg = "Error in parsing uri " + uri + " when retrieving ETag skip URIs for REST API";
                    log.error(msg, e);
                    throw new APIManagementException(msg, e);
                }
            }
        }
        return uriToMethodsMap;
    }
    public static Dictionary<org.wso2.uri.template.URITemplate, List<String>> getETagSkipListToMethodsMap()
            throws APIManagementException {
        if (ETagSkipListURIToHttpMethodsMap==null){
            ETagSkipListURIToHttpMethodsMap = getETagSkipListToMethodsMapFromConfig();
        }
        return ETagSkipListURIToHttpMethodsMap;
    }

    /**
     * This method is used to get the scope list from the yaml file
     *
     * @return MAP of scope list for all portal
     */
    public static  Map<String, List<String>> getScopesInfoFromAPIYamlDefinitions() throws APIManagementException {

        Map<String, List<String>>   portalScopeList = new HashMap<>();
        String [] fileNameArray = {"/admin-api.yaml", "/publisher-api.yaml", "/devportal-api.yaml", "/service-catalog-api.yaml"};
        for (String fileName : fileNameArray) {
            String definition = null;
            try {
                definition = IOUtils
                        .toString(RestApiUtil.class.getResourceAsStream(fileName), "UTF-8");
            } catch (IOException  e) {
                throw new APIManagementException("Error while reading the swagger definition ,",
                        ExceptionCodes.DEFINITION_EXCEPTION);
            }
            APIDefinition oasParser = OASParserUtil.getOASParser(definition);
            Set<Scope> scopeSet = oasParser.getScopes(definition);
            for (Scope entry : scopeSet) {
                List<String> list = new ArrayList<>();
                list.add(entry.getDescription());
                list.add((fileName.replaceAll("-api.yaml", "").replace("/", "")));
                if (("/service-catalog-api.yaml".equals(fileName))) {
                    if (!entry.getKey().contains("apim:api_view")) {
                        portalScopeList.put(entry.getName(), list);
                    }
                } else {
                    portalScopeList.put(entry.getName(), list);
                }
            }
        }
        return portalScopeList;
    }

    /**
     * To check whether the DevPortal Anonymous Mode is enabled. It can be either enabled globally or tenant vice.
     *
     * @param tenantDomain Tenant domain
     * @return whether devportal anonymous mode is enabled or not
     */
    public static boolean isDevPortalAnonymousEnabled(String tenantDomain) {
        try {
            org.json.simple.JSONObject tenantConfig = APIUtil.getTenantConfig(tenantDomain);
            Object value = tenantConfig.get(APIConstants.API_TENANT_CONF_ENABLE_ANONYMOUS_MODE);
            if (value != null) {
                return Boolean.parseBoolean(value.toString());
            } else {
                return APIUtil.isDevPortalAnonymous();
            }
        } catch (APIManagementException e) {
            log.error("Error while retrieving Anonymous config from registry", e);
        }
        return true;
    }
}
