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

package org.wso2.carbon.apimgt.rest.api.util.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ApplicationNameWhiteSpaceValidationException;
import org.wso2.carbon.apimgt.api.ApplicationNameWithInvalidCharactersException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorListItemDTO;
import org.wso2.carbon.apimgt.rest.api.util.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.util.exception.ConflictException;
import org.wso2.carbon.apimgt.rest.api.util.exception.ForbiddenException;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.util.exception.MethodNotAllowedException;
import org.wso2.carbon.apimgt.rest.api.util.exception.NotFoundException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.uri.template.URITemplateException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;

public class RestApiUtil {

    private static final Log log = LogFactory.getLog(RestApiUtil.class);
    private static Set<URITemplate> storeResourceMappings;
    private static Set<URITemplate> publisherResourceMappings;
    private static Set<URITemplate> adminAPIResourceMappings;
    private static Dictionary<org.wso2.uri.template.URITemplate, List<String>> uriToHttpMethodsMap;
    private static Dictionary<org.wso2.uri.template.URITemplate, List<String>> ETagSkipListURIToHttpMethodsMap;
    public static final ThreadLocal userThreadLocal = new ThreadLocal();

    public static void setThreadLocalRequestedTenant(String user) {
        userThreadLocal.set(user);
    }

    public static void unsetThreadLocalRequestedTenant() {
        userThreadLocal.remove();
    }

    public static String getThreadLocalRequestedTenant() {
        return (String)userThreadLocal.get();
    }

    public static APIProvider getLoggedInUserProvider() throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIProvider(getLoggedInUsername());
    }

    public static APIProvider getProvider(String username) throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIProvider(username);
    }

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
     * Check whether the specified apiId is of type UUID
     *
     * @param apiId api identifier
     * @return true if apiId is of type UUID, false otherwise
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isUUID(String apiId) {
        try {
            UUID.fromString(apiId);
            return true;
        } catch (IllegalArgumentException e) {
            if (log.isDebugEnabled()) {
                log.debug(apiId + " is not a valid UUID");
            }
            return false;
        }

    }

    /**
     * Url validator, Allow any url with https and http.
     * Allow any url without fully qualified domain
     *
     * @param url Url as string
     * @return boolean type stating validated or not
     */
    public static boolean isURL(String url) {

        Pattern pattern = Pattern.compile("^(http|https)://(.)+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();

    }

    public static APIConsumer getConsumer(String subscriberName) throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
    }

    /** Returns an APIConsumer which is corresponding to the current logged in user taken from the carbon context
     *
     * @return an APIConsumer which is corresponding to the current logged in user
     * @throws APIManagementException
     */
    public static APIConsumer getLoggedInUserConsumer() throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer(getLoggedInUsername());
    }

    public static String getLoggedInUsername() {
        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    public static String getLoggedInUserTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
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
    public static String getLoggedInUserGroupId() {
        String username = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        JSONObject loginInfoJsonObj = new JSONObject();
        try {
            loginInfoJsonObj.put("user", username);
            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                loginInfoJsonObj.put("isSuperTenant", true);
            } else {
                loginInfoJsonObj.put("isSuperTenant", false);
            }
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
            handleInternalServerError(errorMsg, e, log);
            return null;
        }
    }

    private static String[] getGroupIds(String loginInfoString) throws APIManagementException {
        String groupingExtractorClass = APIUtil.getRESTApiGroupingExtractorImplementation();
        return APIUtil.getGroupIdsFromExtractor(loginInfoString, groupingExtractorClass);
    }

    /**
     * Check if the user's tenant and the API's tenant is equal. If it is not this will throw an
     * APIMgtAuthorizationFailedException
     *
     * @param apiIdentifier API Identifier
     * @throws APIMgtAuthorizationFailedException
     */
    public static void validateUserTenantWithAPIIdentifier(APIIdentifier apiIdentifier)
            throws APIMgtAuthorizationFailedException {
        String username = RestApiUtil.getLoggedInUsername();
        String providerName = APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName());
        String providerTenantDomain = MultitenantUtils.getTenantDomain(providerName);
        String loggedInUserTenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        if (!providerTenantDomain.equals(loggedInUserTenantDomain)) {
            String errorMsg = "User " + username + " is not allowed to access " + apiIdentifier.toString()
                    + " as it belongs to a different tenant : " + providerTenantDomain;
            throw new APIMgtAuthorizationFailedException(errorMsg);
        }
    }

    /**
     * Returns the requested tenant according to the input x-tenant-header
     *
     * @return requested tenant domain
     */
    public static String getRequestedTenantDomain(String xTenantHeader) {
        if (StringUtils.isEmpty(xTenantHeader)) {
            return getLoggedInUserTenantDomain();
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
            throw new APIManagementException(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(outFileStream);
        }
    }

    /**
     * Returns date in RFC3339 format.
     * Example: 2008-11-13T12:23:30-08:00
     *
     * @param date Date object
     * @return date string in RFC3339 format.
     */
    public static String getRFC3339Date(Date date) {
        DateTimeFormatter jodaDateTimeFormatter = ISODateTimeFormat.dateTime();
        DateTime dateTime = new DateTime(date);
        return jodaDateTimeFormatter.print(dateTime);
    }

    /**
     * Returns a new InternalServerErrorException
     *
     * @return a new InternalServerErrorException with default details as a response DTO
     */
    public static InternalServerErrorException buildInternalServerErrorException() {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT, 500l,
                RestApiConstants.STATUS_INTERNAL_SERVER_ERROR_DESCRIPTION_DEFAULT);
        return new InternalServerErrorException(errorDTO);
    }

    /**
     * Returns a new InternalServerErrorException
     *
     * @param errorDescription Error Description
     * @return a new InternalServerErrorException with default details as a response DTO
     */
    public static InternalServerErrorException buildInternalServerErrorException(String errorDescription) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT, 500l,
                errorDescription);
        return new InternalServerErrorException(errorDTO);
    }

    /**
     * Returns a new NotFoundException
     *
     * @param resource Resource type
     * @param id identifier of the resource
     * @return a new NotFoundException with the specified details as a response DTO
     */
    public static NotFoundException buildNotFoundException(String resource, String id) {
        String description;
        if (!StringUtils.isEmpty(id)) {
            description = "Requested " + resource + " with Id '" + id + "' not found";
        } else {
            description = "Requested " + resource + " not found";
        }
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_NOT_FOUND_MESSAGE_DEFAULT, 404l, description);
        return new NotFoundException(errorDTO);
    }

    /**
     * Returns a new NotFoundException
     *
     * @param resource resource type
     * @param id       identifier of the resource
     * @param tenant   tenant for which the resource has been searched
     * @return a new NotFoundException with the specified details
     */
    public static NotFoundException buildNotFoundException(String resource, String id, String tenant) {
        String description;
        if (!StringUtils.isEmpty(id)) {
            description = "Requested " + resource + " with Id '" + id + "' not found in tenant " + tenant;
        } else {
            description = "Requested " + resource + " not found in tenant " + tenant;
        }
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_NOT_FOUND_MESSAGE_DEFAULT, 404l, description);
        return new NotFoundException(errorDTO);
    }

    /**
     * Returns a new NotFoundException
     *
     * @param description description of the error
     * @return a new NotFoundException with the specified details as a response DTO
     */
    public static NotFoundException buildNotFoundException(String description) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_NOT_FOUND_MESSAGE_DEFAULT, 404l, description);
        return new NotFoundException(errorDTO);
    }

    /**
     * Returns a new ForbiddenException
     *
     * @param resource Resource type
     * @param id identifier of the resource
     * @return a new ForbiddenException with the specified details as a response DTO
     */
    public static ForbiddenException buildForbiddenException(String resource, String id) {
        String description;
        if (!StringUtils.isEmpty(id)) {
            description = "You don't have permission to access the " + resource + " with Id " + id;
        } else {
            description = "You don't have permission to access the " + resource;
        }
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT, 403l, description);
        return new ForbiddenException(errorDTO);
    }

    /**
     * Returns a new ForbiddenException
     *
     * @param description description of the failure
     * @return a new ForbiddenException with the specified details as a response DTO
     */
    public static ForbiddenException buildForbiddenException(String description) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT, 403l, description);
        return new ForbiddenException(errorDTO);
    }

    /**
     * Returns a new BadRequestException
     *
     * @param description description of the exception
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(String description) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400l, description);
        return new BadRequestException(errorDTO);
    }

    /**
     * Returns a new BadRequestException
     *
     * @param description description of the exception
     * @param code error code
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(String description, Long code) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, code, description);
        return new BadRequestException(errorDTO);
    }

    /**
     * Returns a new BadRequestException
     *
     * @param description description of the exception
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(String description, Throwable e) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400l, description);
        return new BadRequestException(description, e, errorDTO);
    }

    /**
     * Returns a new BadRequestException
     *
     * @param errorDTO ErrorDTO object containing the error information
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(ErrorDTO errorDTO) {
        return new BadRequestException(errorDTO);
    }


    /**
     * Returns a new BadRequestException
     *
     * @param errorHandler ErrorHandler object containing the error information
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(ErrorHandler errorHandler) {
        ErrorDTO errorDTO = getErrorDTO(errorHandler.getErrorMessage(), errorHandler.getErrorCode(),
                errorHandler.getErrorDescription());
        return new BadRequestException(errorDTO);
    }

    /**
     * Returns a new BadRequestException from a list of Error Handlers
     *
     * @param errorHandlers A List of ErrorHandler object containing the error information
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(List<ErrorHandler> errorHandlers) {
        ErrorDTO errorDTO = getErrorDTO(errorHandlers);
        return new BadRequestException(errorDTO);
    }

    /**
     * Returns a new MethodNotAllowedException
     *
     * @param method http method
     * @param resource resource which the method is not allowed
     * @return a new MethodNotAllowedException consists of the error message
     */
    public static MethodNotAllowedException buildMethodNotAllowedException(String method, String resource) {
        String description = "Method " + method + " is not supported for " + resource;
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_METHOD_NOT_ALLOWED_MESSAGE_DEFAULT, 405l, description);
        return new MethodNotAllowedException(errorDTO);
    }

    /**
     * Returns a new ConflictException
     *
     * @param message summary of the error
     * @param description description of the exception
     * @return a new ConflictException with the specified details as a response DTO
     */
    public static ConflictException buildConflictException(String message, String description) {
        ErrorDTO errorDTO = getErrorDTO(message, 409l, description);
        return new ConflictException(errorDTO);
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
     * Check if the specified throwable e is happened as the required resource cannot be found
     * @param e throwable to check
     * @return true if the specified throwable e is happened as the required resource cannot be found, false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToResourceNotFound(Throwable e) {
        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause instanceof APIMgtResourceNotFoundException || rootCause instanceof ResourceNotFoundException;
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
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param msg error message
     * @param log Log instance
     * @throws BadRequestException
     */
    public static void handleBadRequest(String msg, Log log) throws BadRequestException {
        BadRequestException badRequestException = buildBadRequestException(msg);
        log.error(msg);
        throw badRequestException;
    }

    /**
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param msg error message
     * @param log Log instance
     * @param code error code
     * @throws BadRequestException
     */
    public static void handleBadRequest(String msg, Long code, Log log) throws BadRequestException {
        BadRequestException badRequestException = buildBadRequestException(msg, code);
        log.error(msg);
        throw badRequestException;
    }

    /**
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param msg error message
     * @param e throwable to log
     * @param log Log instance
     * @throws BadRequestException
     */
    public static void handleBadRequest(String msg, Throwable e, Log log) throws BadRequestException {
        BadRequestException badRequestException = buildBadRequestException(msg);
        log.error(msg, e);
        throw badRequestException;
    }

    /**
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param errorHandler ErrorHandler object containing the error information
     * @param log Log instance
     * @throws BadRequestException
     */
    public static void handleBadRequest(ErrorHandler errorHandler, Log log) throws BadRequestException {
        BadRequestException badRequestException = buildBadRequestException(errorHandler);
        log.error(errorHandler.getErrorMessage());
        throw badRequestException;
    }

    /**
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param errorHandlers A List of error handler objects containing the error information
     * @param log Log instance
     * @throws BadRequestException
     */
    public static void handleBadRequest(List<ErrorHandler> errorHandlers, Log log) throws BadRequestException {
        BadRequestException badRequestException = buildBadRequestException(errorHandlers);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < errorHandlers.size(); i++) {
            ErrorHandler handler = errorHandlers.get(i);
            builder.append(handler.getErrorMessage());
            if (StringUtils.isNotBlank(handler.getErrorDescription())) {
                builder.append(":");
                builder.append(handler.getErrorDescription());
            }

            if (i < errorHandlers.size() - 1) {
                builder.append(", ");
            }
        }
        log.error(builder.toString());
        throw badRequestException;
    }

    /**
     * Logs the error, builds a ForbiddenException with specified details and throws it
     *
     * @param resource Resource type
     * @param id id of resource
     * @param t Throwable
     * @param log Log instance
     * @throws ForbiddenException
     */
    public static void handleAuthorizationFailure(String resource, String id, Throwable t, Log log)
            throws ForbiddenException {
        ForbiddenException forbiddenException = buildForbiddenException(resource, id);
        log.error(forbiddenException.getMessage(), t);
        throw forbiddenException;
    }

    /**
     * Logs the error, builds a ForbiddenException with specified details and throws it
     *
     * @param resource requested resource
     * @param id id of resource
     * @param log Log instance
     * @throws ForbiddenException
     */
    public static void handleAuthorizationFailure(String resource, String id, Log log)
            throws ForbiddenException {
        ForbiddenException forbiddenException = buildForbiddenException(resource, id);
        log.error(forbiddenException.getMessage());
        throw forbiddenException;
    }

    /**
     * Logs the error, builds a ForbiddenException with specified details and throws it
     *
     * @param description description of the error
     * @param t Throwable instance
     * @param log Log instance
     * @throws ForbiddenException
     */
    public static void handleAuthorizationFailure(String description, Throwable t, Log log)
            throws ForbiddenException {
        ForbiddenException forbiddenException = buildForbiddenException(description);
        log.error(description, t);
        throw forbiddenException;
    }

    /**
     * Logs the error, builds a ForbiddenException with specified details and throws it
     *
     * @param description description of the error
     * @param log Log instance
     * @throws ForbiddenException
     */
    public static void handleAuthorizationFailure(String description, Log log)
            throws ForbiddenException {
        ForbiddenException forbiddenException = buildForbiddenException(description);
        log.error(description);
        throw forbiddenException;
    }
    /**
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param resource requested resource
     * @param id       id of resource
     * @param log      Log instance
     * @param tenant   tenant where the resource should be searched
     * @throws NotFoundException if the resource does not exist in the given tenant
     */
    public static void handleResourceNotFoundInTenantError(String resource, String id, Log log, String tenant)
            throws NotFoundException {
        NotFoundException notFoundException = buildNotFoundException(resource, id, tenant);
        log.error(notFoundException.getMessage());
        throw notFoundException;
    }

    /**
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param resource requested resource
     * @param id id of resource
     * @param t Throwable instance
     * @param log Log instance
     * @throws NotFoundException
     */
    public static void handleResourceNotFoundError(String resource, String id, Throwable t, Log log)
            throws NotFoundException {
        NotFoundException notFoundException = buildNotFoundException(resource, id);
        log.error(notFoundException.getMessage(), t);
        throw notFoundException;
    }

    /**
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param resource requested resource
     * @param id id of resource
     * @param log Log instance
     * @throws NotFoundException
     */
    public static void handleResourceNotFoundError(String resource, String id, Log log)
            throws NotFoundException {
        NotFoundException notFoundException = buildNotFoundException(resource, id);
        log.error(notFoundException.getMessage());
        throw notFoundException;
    }

    /**
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param description description of the error
     * @param t Throwable instance
     * @param log Log instance
     * @throws NotFoundException
     */
    public static void handleResourceNotFoundError(String description, Throwable t, Log log)
            throws NotFoundException {
        NotFoundException notFoundException = buildNotFoundException(description);
        log.error(description, t);
        throw notFoundException;
    }

    /**
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param description description of the error
     * @param log Log instance
     * @throws NotFoundException
     */
    public static void handleResourceNotFoundError(String description, Log log)
            throws NotFoundException {
        NotFoundException notFoundException = buildNotFoundException(description);
        log.error(description);
        throw notFoundException;
    }

    /**
     * Logs the error, builds a ConflictException with specified details and throws it
     *
     * @param description description of the error
     * @param log Log instance
     * @throws ConflictException
     */
    public static void handleResourceAlreadyExistsError(String description, Log log)
            throws ConflictException {
        ConflictException conflictException = buildConflictException(
                RestApiConstants.STATUS_CONFLICT_MESSAGE_RESOURCE_ALREADY_EXISTS, description);
        log.error(description);
        throw conflictException;
    }

    /**
     * Logs the error, builds a ConflictException with specified details and throws it
     *
     * @param description description of the error
     * @param log Log instance
     * @throws ConflictException
     */
    public static void handleConflict(String description, Log log)
            throws ConflictException {
        ConflictException conflictException = buildConflictException(RestApiConstants.STATUS_CONFLICT_MESSAGE_DEFAULT,
                description);
        log.error(description);
        throw conflictException;
    }

    /**
     * Logs the error, builds a ConflictException with specified details and throws it
     *
     * @param description description of the error
     * @param t Throwable instance
     * @param log Log instance
     * @throws ConflictException
     */
    public static void handleResourceAlreadyExistsError(String description, Throwable t, Log log)
            throws ConflictException {
        ConflictException conflictException = buildConflictException(
                RestApiConstants.STATUS_CONFLICT_MESSAGE_RESOURCE_ALREADY_EXISTS, description);
        log.error(description, t);
        throw conflictException;
    }

    /**
     * Logs the error, builds a MethodNotAllowedException with specified details and throws it
     *
     * @param method http method
     * @param resource requested resource
     * @param log Log instance
     * @throws MethodNotAllowedException
     */
    public static void handleMethodNotAllowedError(String method, String resource, Log log)
            throws MethodNotAllowedException {
        MethodNotAllowedException methodNotAllowedException = buildMethodNotAllowedException(method, resource);
        log.error(methodNotAllowedException.getMessage());
        throw methodNotAllowedException;
    }

    /**
     * Logs the error, builds a internalServerErrorException with specified details and throws it
     *
     * @param msg error message
     * @param t Throwable instance
     * @param log Log instance
     * @throws InternalServerErrorException
     */
    public static void handleInternalServerError(String msg, Throwable t, Log log)
            throws InternalServerErrorException {
        InternalServerErrorException internalServerErrorException = buildInternalServerErrorException(msg);
        log.error(msg, t);
        throw internalServerErrorException;
    }

    /**
     * Logs the error, builds a internalServerErrorException with specified details and throws it
     *
     * @param msg error message
     * @param log Log instance
     * @throws InternalServerErrorException
     */
    public static void handleInternalServerError(String msg, Log log)
            throws InternalServerErrorException {
        InternalServerErrorException internalServerErrorException = buildInternalServerErrorException();
        log.error(msg);
        throw internalServerErrorException;
    }

    /**
     * Check whether the HTTP method is allowed for given resources
     *
     * @param method HTTP method
     * @param resource requested resource
     * @throws MethodNotAllowedException if the method is not supported
     */
    public static void checkAllowedMethodForResource(String method, String resource) throws MethodNotAllowedException {
        if (RestApiConstants.RESOURCE_PATH_TIERS_APPLICATION.equals(resource)
                || RestApiConstants.RESOURCE_PATH_TIERS_RESOURCE.equals(resource)) {
            if (!"GET".equals(method)) {
                RestApiUtil.handleMethodNotAllowedError(method, resource, log);
            }
        }
    }

    /**
     * Returns the next/previous offset/limit parameters properly when current offset, limit and size parameters are specified
     *
     * @param offset current starting index
     * @param limit current max records
     * @param size maximum index possible
     * @return the next/previous offset/limit parameters as a hash-map
     */
    public static Map<String, Integer> getPaginationParams(Integer offset, Integer limit, Integer size) {
        Map<String, Integer> result = new HashMap<>();
        if (offset >= size || offset < 0)
            return result;

        int start = offset;
        int end = offset + limit - 1;

        int nextStart = end + 1;
        if (nextStart < size) {
            result.put(RestApiConstants.PAGINATION_NEXT_OFFSET, nextStart);
            result.put(RestApiConstants.PAGINATION_NEXT_LIMIT, limit);
        }

        int previousEnd = start - 1;
        int previousStart = previousEnd - limit + 1;

        if (previousEnd >= 0) {
            if (previousStart < 0) {
                result.put(RestApiConstants.PAGINATION_PREVIOUS_OFFSET, 0);
                result.put(RestApiConstants.PAGINATION_PREVIOUS_LIMIT, limit);
            } else {
                result.put(RestApiConstants.PAGINATION_PREVIOUS_OFFSET, previousStart);
                result.put(RestApiConstants.PAGINATION_PREVIOUS_LIMIT, limit);
            }
        }
        return result;
    }

    /** Returns the paginated url for APIs API
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param query search query value
     * @return constructed paginated url
     */
    public static String getAPIPaginatedURL(Integer offset, Integer limit, String query) {
        String paginatedURL = RestApiConstants.APIS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.QUERY_PARAM, query);
        return paginatedURL;
    }

    /** Returns the paginated url for Applications API
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param groupId groupId of the Application
     * @return constructed paginated url
     */
    public static String getApplicationPaginatedURL(Integer offset, Integer limit, String groupId) {
        groupId = groupId == null ? "" : groupId;
        String paginatedURL = RestApiConstants.APPLICATIONS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.GROUPID_PARAM, groupId);
        return paginatedURL;
    }

    /** Returns the paginated url for admin  /Applications API
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getApplicationPaginatedURL(Integer offset, Integer limit) {
        String paginatedURL = RestApiConstants.APPLICATIONS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /** Returns the paginated url for subscriptions for a particular API identifier
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param apiId API Identifier
     * @param groupId groupId of the Application
     * @return constructed paginated url
     */
    public static String getSubscriptionPaginatedURLForAPIId(Integer offset, Integer limit, String apiId,
            String groupId) {
        groupId = groupId == null ? "" : groupId;
        String paginatedURL = RestApiConstants.SUBSCRIPTIONS_GET_PAGINATION_URL_APIID;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APIID_PARAM, apiId);
        paginatedURL = paginatedURL.replace(RestApiConstants.GROUPID_PARAM, groupId);
        return paginatedURL;
    }

    /** Returns the paginated url for subscriptions for a particular application
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param applicationId application id
     * @return constructed paginated url
     */
    public static String getSubscriptionPaginatedURLForApplicationId(Integer offset, Integer limit,
            String applicationId) {
        String paginatedURL = RestApiConstants.SUBSCRIPTIONS_GET_PAGINATION_URL_APPLICATIONID;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APPLICATIONID_PARAM, applicationId);
        return paginatedURL;
    }

    /** Returns the paginated url for documentations
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getDocumentationPaginatedURL(Integer offset, Integer limit, String apiId) {
        String paginatedURL = RestApiConstants.DOCUMENTS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APIID_PARAM, apiId);
        return paginatedURL;
    }

    /** Returns the paginated url for API ratings
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getRatingPaginatedURL(Integer offset, Integer limit, String apiId) {
        String paginatedURL = RestApiConstants.RATINGS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APIID_PARAM, apiId);
        return paginatedURL;
    }

    /** Returns the paginated url for tiers
     *
     * @param tierLevel tier level (api/application or resource)
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getTiersPaginatedURL(String tierLevel, Integer offset, Integer limit) {
        String paginatedURL = RestApiConstants.TIERS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.TIER_LEVEL_PARAM, tierLevel);
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /** Returns the paginated url for tags
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getTagsPaginatedURL(Integer offset, Integer limit) {
        String paginatedURL = RestApiConstants.TAGS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /** Returns the paginated url for tags
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getResourcePathPaginatedURL(Integer offset, Integer limit) {
        String paginatedURL = RestApiConstants.RESOURCE_PATH_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        return paginatedURL;
    }

    /** Returns the paginated url for APIProducts API
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param query search query value
     * @return constructed paginated url
     */
    public static String getAPIProductPaginatedURL(Integer offset, Integer limit, String query) {
        String paginatedURL = RestApiConstants.APIS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.QUERY_PARAM, query);
        return paginatedURL;
    }

    /** Returns the paginated url for product documentations
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @return constructed paginated url
     */
    public static String getProductDocumentationPaginatedURL(Integer offset, Integer limit, String apiId) {
        String paginatedURL = RestApiConstants.PRODUCT_DOCUMENTS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.APIID_PARAM, apiId);
        return paginatedURL;
    }

    /**
     * Checks whether the list of tiers are valid given the all valid tiers
     *
     * @param allTiers All defined tiers
     * @param currentTiers tiers to check if they are a subset of defined tiers
     * @return null if there are no invalid tiers or returns the set of invalid tiers if there are any
     */
    public static List<String> getInvalidTierNames(Set<Tier> allTiers, List<String> currentTiers) {
        List<String> invalidTiers = new ArrayList<>();
        for (String tierName : currentTiers) {
            boolean isTierValid = false;
            for (Tier definedTier : allTiers) {
                if (tierName.equals(definedTier.getName())) {
                    isTierValid = true;
                    break;
                }
            }
            if (!isTierValid) {
                invalidTiers.add(tierName);
            }
        }
        return invalidTiers;
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
     * Following 3 methods are temporary added to rest API Util
     * Ideally they should move to DCR, RR and Introspection API implementation
     *
     * @param api
     * @param swagger
     * @return
     */
    public static boolean registerResource(API api, String swagger) {

        APIDefinition oasParser;
        try {
            oasParser = OASParserUtil.getOASParser(swagger);
        } catch (APIManagementException e) {
            log.error("Error occurred while parsing swagger definition");
            return false;
        }


        Set<URITemplate> uriTemplates = null;
        try {
            uriTemplates = oasParser.getURITemplates(swagger);
        } catch (APIManagementException e) {
            log.error("Error while parsing swagger content to get URI Templates", e);
        }
        api.setUriTemplates(uriTemplates);
        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
        Map registeredResource = null;
        try {
            registeredResource = keyManager.getResourceByApiId(api.getId().toString());
        } catch (APIManagementException e) {
            log.error("Error while getting registered resources for API: " + api.getId().toString(), e);
        }
        //Add new resource if not exist
        if (registeredResource == null) {
            boolean isNewResourceRegistered = false;
            try {
                isNewResourceRegistered = keyManager.registerNewResource(api, null);
            } catch (APIManagementException e) {
                log.error("Error while registering new resource for API: " + api.getId().toString(), e);
            }
            if (!isNewResourceRegistered) {
                log.error("New resource not registered for API: " + api.getId());
            }
        }
        //update existing resource
        else {
            try {
                keyManager.updateRegisteredResource(api, registeredResource);
            } catch (APIManagementException e) {
                log.error("Error while updating resource", e);
            }
        }
        return true;
    }

    public static OAuthApplicationInfo registerOAuthApplication(OAuthAppRequest appRequest) {
        //Create Oauth Application - Dynamic client registration service
        AMDefaultKeyManagerImpl impl = new AMDefaultKeyManagerImpl();
        OAuthApplicationInfo returnedAPP = null;
        try {
            returnedAPP = impl.createApplication(appRequest);
        } catch (APIManagementException e) {
            log.error("Cannot create OAuth application from provided information, for APP name: " +
                    appRequest.getOAuthApplicationInfo().getClientName(), e);
        }
        return returnedAPP;
    }

    public static OAuthApplicationInfo retrieveOAuthApplication(String consumerKey) {
        //Create Oauth Application - Dynamic client registration service
        AMDefaultKeyManagerImpl impl = new AMDefaultKeyManagerImpl();
        OAuthApplicationInfo returnedAPP = null;
        try {
            returnedAPP = impl.retrieveApplication(consumerKey);
        } catch (APIManagementException e) {
            log.error("Error while retrieving OAuth application information for Consumer Key: " + consumerKey, e);
        }
        return returnedAPP;
    }

    /**
     * This is static method to return URI Templates map of API Store REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return URITemplate set associated with API Manager Store REST API
     */
    public static Set<URITemplate> getStoreAppResourceMapping(String version) {

        API api = new API(new APIIdentifier(RestApiConstants.REST_API_PROVIDER,
                RestApiConstants.REST_API_STORE_CONTEXT,
                RestApiConstants.REST_API_STORE_VERSION_0));

        if (storeResourceMappings != null) {
            return storeResourceMappings;
        } else {
            try {
                String definition;
                if (RestApiConstants.REST_API_STORE_VERSION_0.equals(version)) {
                    definition = IOUtils
                            .toString(RestApiUtil.class.getResourceAsStream("/store-api.json"), "UTF-8");
                } else {
                    definition = IOUtils
                            .toString(RestApiUtil.class.getResourceAsStream("/store-api.yaml"), "UTF-8");
                }
                APIDefinition oasParser = OASParserUtil.getOASParser(definition);
                //Get URL templates from swagger content w created
                storeResourceMappings = oasParser.getURITemplates(definition);
            } catch (APIManagementException e) {
                log.error("Error while reading resource mappings for API: " + api.getId().getApiName(), e);
            } catch (IOException e) {
                log.error("Error while reading the swagger definition for API: " + api.getId().getApiName(), e);
            }
            return storeResourceMappings;
        }
    }


    /**
     * This is static method to return URI Templates map of API Publisher REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return URITemplate set associated with API Manager publisher REST API
     */
    public static Set<URITemplate> getPublisherAppResourceMapping(String version) {
        API api = new API(new APIIdentifier(RestApiConstants.REST_API_PROVIDER, RestApiConstants.REST_API_STORE_CONTEXT,
                RestApiConstants.REST_API_STORE_VERSION_0));

        if (publisherResourceMappings != null) {
            return publisherResourceMappings;
        } else {
            try {
                String definition;
                if (RestApiConstants.REST_API_PUBLISHER_VERSION_0.equals(version)) {
                    definition = IOUtils
                            .toString(RestApiUtil.class.getResourceAsStream("/publisher-api.json"), "UTF-8");
                } else {
                    definition = IOUtils
                            .toString(RestApiUtil.class.getResourceAsStream("/publisher-api.yaml"), "UTF-8");
                }
                APIDefinition oasParser = OASParserUtil.getOASParser(definition);
                //Get URL templates from swagger content we created
                publisherResourceMappings = oasParser.getURITemplates(definition);
            } catch (APIManagementException e) {
                log.error("Error while reading resource mappings for API: " + api.getId().getApiName(), e);
            } catch (IOException e) {
                log.error("Error while reading the swagger definition for API: " + api.getId().getApiName(), e);
            }
            return publisherResourceMappings;
        }
    }

    /**
     * This is static method to return URI Templates map of API Admin REST API.
     * This content need to load only one time and keep it in memory as content will not change
     * during runtime.
     *
     * @return URITemplate set associated with API Manager Admin REST API
     */
    public static Set<URITemplate> getAdminAPIAppResourceMapping() {

        API api = new API(new APIIdentifier(RestApiConstants.REST_API_PROVIDER, RestApiConstants.REST_API_ADMIN_CONTEXT,
                RestApiConstants.REST_API_ADMIN_VERSION));

        if (adminAPIResourceMappings != null) {
            return adminAPIResourceMappings;
        } else {
            try {
                String definition = IOUtils
                        .toString(RestApiUtil.class.getResourceAsStream("/admin-api.json"), "UTF-8");
                APIDefinition oasParser = OASParserUtil.getOASParser(definition);
                //Get URL templates from swagger content we created
                adminAPIResourceMappings = oasParser.getURITemplates(definition);
            } catch (APIManagementException e) {
                log.error("Error while reading resource mappings for API: " + api.getId().getApiName(), e);
            } catch (IOException e) {
                log.error("Error while reading the swagger definition for API: " + api.getId().getApiName(), e);
            }
            return adminAPIResourceMappings;
        }
    }

    /**
     * Returns the white-listed URIs and associated HTTP methods for REST API by reading api-manager.xml configuration
     *
     * @return A Dictionary with the white-listed URIs and the associated HTTP methods
     * @throws APIManagementException
     */
    private static Dictionary<org.wso2.uri.template.URITemplate, List<String>> getWhiteListedURIsToMethodsMapFromConfig()
            throws APIManagementException {
        Hashtable<org.wso2.uri.template.URITemplate, List<String>> uriToMethodsMap = new Hashtable<>();
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        List<String> uriList = apiManagerConfiguration
                .getProperty(APIConstants.API_RESTAPI_WHITELISTED_URI_URI);
        List<String> methodsList = apiManagerConfiguration
                .getProperty(APIConstants.API_RESTAPI_WHITELISTED_URI_HTTPMethods);

        if (uriList != null && methodsList != null) {
            if (uriList.size() != methodsList.size()) {
                String errorMsg = "Provided White-listed URIs for REST API are invalid."
                        + " Every 'WhiteListedURI' should include 'URI' and 'HTTPMethods' elements";
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
    public static Dictionary<org.wso2.uri.template.URITemplate, List<String>> getWhiteListedURIsToMethodsMap()
            throws APIManagementException {

        if (uriToHttpMethodsMap == null) {
            uriToHttpMethodsMap = getWhiteListedURIsToMethodsMapFromConfig();
        }
        return uriToHttpMethodsMap;
    }

    /**
     * @param message        CXF message to be extract auth header
     * @param pattern        Pattern to extract access token
     * @param authHeaderName transport header name which contains authentication information
     * @return access token string according to provided pattern name and auth header name
     */
    public static String extractOAuthAccessTokenFromMessage(Message message, Pattern pattern, String authHeaderName) {
        String authHeader = null;
        ArrayList authHeaders = (ArrayList) ((TreeMap) (message.get(Message.PROTOCOL_HEADERS))).get(authHeaderName);
        if (authHeaders == null)
            return null;

        String headerString = authHeaders.get(0).toString();
        Matcher matcher = pattern.matcher(headerString);
        if (matcher.find()) {
            authHeader = headerString.substring(matcher.end());
        }
        return authHeader;
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
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
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
    public static Boolean checkETagSkipList(String path, String httpMethod) {
        //Check if the accessing URI is ETag skipped
        try {
            Dictionary<org.wso2.uri.template.URITemplate, List<String>> eTagSkipListToMethodsMap = RestApiUtil.getETagSkipListToMethodsMap();
            Enumeration<org.wso2.uri.template.URITemplate> uriTemplateSet = eTagSkipListToMethodsMap.keys();

            while (uriTemplateSet.hasMoreElements()) {
                org.wso2.uri.template.URITemplate uriTemplate = uriTemplateSet.nextElement();
                if (uriTemplate.matches(path, new HashMap<String, String>())) {
                    List<String> ETagDisableHttpVerbs = eTagSkipListToMethodsMap.get(uriTemplate);
                    return ETagDisableHttpVerbs.contains(httpMethod);
                }
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Unable to resolve ETag skip list in api-manager.xml", e, log);
        }
        return false;
    }

    /**
     * Handle if any cross tenant access permission violations detected. Cross tenant resources (apis/apps) can be
     * retrieved only by super tenant admin user, only while a migration process(2.6.0 to 3.0.0). APIM server has to be
     * started with the system property 'migrationMode=true' if a migration related exports are to be done.
     *
     * @param targetTenantDomain Tenant domain of which resources are requested
     * @param username           Logged in user name
     * @throws ForbiddenException
     */
    public static void handleMigrationSpecificPermissionViolations(String targetTenantDomain, String username) throws
            ForbiddenException {
        boolean isCrossTenantAccess = !targetTenantDomain.equals(MultitenantUtils.getTenantDomain(username));
        if (!isCrossTenantAccess) {
            return;
        }
        String superAdminRole = null;
        try {
            superAdminRole = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getRealmConfiguration().getAdminRoleName();
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error in getting super admin role name", e, log);
        }

        //check whether logged in user is a super tenant user
        String superTenantDomain = null;
        try {
            superTenantDomain = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getSuperTenantDomain();
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error in getting the super tenant domain", e, log);
        }
        boolean isSuperTenantUser = RestApiUtil.getLoggedInUserTenantDomain().equals(superTenantDomain);
        if (!isSuperTenantUser) {
            String errorMsg = "Cross Tenant resource access is not allowed for this request. User " + username +
                    " is not allowed to access resources in " + targetTenantDomain + " as the requester is not a super " +
                    "tenant user";
            log.error(errorMsg);
            ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT, 403l, errorMsg);
            throw new ForbiddenException(errorDTO);
        }

        //check whether the user has super tenant admin role
        boolean isSuperAdminRoleNameExist = false;
        try {
            isSuperAdminRoleNameExist = APIUtil.isUserInRole(username, superAdminRole);
        } catch (UserStoreException | APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error in checking whether the user has admin role", e, log);
        }

        if (!isSuperAdminRoleNameExist) {
            String errorMsg = "Cross Tenant resource access is not allowed for this request. User " + username +
                    " is not allowed to access resources in " + targetTenantDomain + " as the requester is not a " +
                    "super tenant admin";
            log.error(errorMsg);
            ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT, 403l, errorMsg);
            throw new ForbiddenException(errorDTO);
        }
    }

    /**
     * Check if user calls an anonymous REST API.
     *
     * @param inMessage cxf message
     * @return true if user calls anonymous API, false otherwise.
     */
    public static boolean checkIfAnonymousAPI(Message inMessage) {
        return (inMessage.get(RestApiConstants.AUTHENTICATION_REQUIRED) != null &&
                !((Boolean) inMessage.get(RestApiConstants.AUTHENTICATION_REQUIRED)));
    }

    /**
     * This method is used to get the URI template set for the relevant REST API using the given base path.
     *
     * @param basePath Base path of the REST API
     * @return Set of URI templates for the REST API
     */
    public static Set<URITemplate> getURITemplatesForBasePath(String basePath) {
        Set<URITemplate> uriTemplates = new HashSet<>();
        //get URI templates using the base path in the request
        if (basePath.contains(RestApiConstants.REST_API_PUBLISHER_CONTEXT_FULL_0)) {
            uriTemplates = RestApiUtil.getPublisherAppResourceMapping(RestApiConstants.REST_API_PUBLISHER_VERSION_0);
        } else if (basePath.contains(RestApiConstants.REST_API_PUBLISHER_CONTEXT_FULL_1)) {
            uriTemplates = RestApiUtil.getPublisherAppResourceMapping(RestApiConstants.REST_API_PUBLISHER_VERSION_1);
        } else if (basePath.contains(RestApiConstants.REST_API_STORE_CONTEXT_FULL_0)) {
            uriTemplates = RestApiUtil.getStoreAppResourceMapping(RestApiConstants.REST_API_STORE_VERSION_0);
        } else if (basePath.contains(RestApiConstants.REST_API_STORE_CONTEXT_FULL_1)) {
            uriTemplates = RestApiUtil.getStoreAppResourceMapping(RestApiConstants.REST_API_STORE_VERSION_1);
        } else if (basePath.contains(RestApiConstants.REST_API_ADMIN_CONTEXT)) {
            uriTemplates = RestApiUtil.getAdminAPIAppResourceMapping();
        }
        return uriTemplates;
    }
}
