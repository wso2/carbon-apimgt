/*
 *
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.util.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ApplicationNameWhiteSpaceValidationException;
import org.wso2.carbon.apimgt.api.ApplicationNameWithInvalidCharactersException;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.util.exception.ConflictException;
import org.wso2.carbon.apimgt.rest.api.util.exception.ForbiddenException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest(
        {LogFactory.class, RestApiCommonUtil.class, CarbonContext.class, Tier.class, APIManagerFactory.class,
                APIProvider.class, RestApiUtil.class, ErrorDTO.class})
public class RestApiUtilTest {

    @Test
    public void testGetLoggedInUsername() {

        System.setProperty(CARBON_HOME, "");
        String defaultUsername = "default@user.com";

        mockStatic(CarbonContext.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        Mockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        Mockito.when(carbonContext.getUsername()).thenReturn(defaultUsername);

        String loggedInUsername = RestApiCommonUtil.getLoggedInUsername();
        Assert.assertEquals(defaultUsername, loggedInUsername);
    }

    @Test
    public void testHandleBadRequest() {

        String errorMessage = "Application name or owner should not be empty or null.";

        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        PowerMockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        Exception exceptionCaught = null;
        try {
            RestApiUtil.handleBadRequest(errorMessage, log);
        } catch (BadRequestException exception) {
            exceptionCaught = exception;
        }
        Assert.assertEquals(errorMessage, exceptionCaught.getMessage());
        Mockito.verify(log).error(errorMessage);
    }

//    TODO : The passed error message will not get displayed in the thrown exception. Implementation in
//     InternalServerErrorException class is not done right to reflect the error message description
//    @Test
//    public void testHandleInternalServerError() {
//        String errorMessage = "Error while updating application owner.";
//        Throwable throwable = new Throwable();
//        Exception exceptionCaught = null;
//
//        Log log = Mockito.mock(Log.class);
//        PowerMockito.mockStatic(LogFactory.class);
//        PowerMockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);
//
//        try {
//            RestApiUtil.handleInternalServerError(errorMessage, throwable, log);
//        } catch (InternalServerErrorException exception) {
//            exceptionCaught = exception;
//        }
//
//        Assert.assertEquals(errorMessage, exceptionCaught.getMessage());
//        Mockito.verify(log).error(errorMessage, throwable);
//    }

    @Test
    public void testIsDueToResourceNotFoundWithAPIMgtResourceNotFoundException() throws Exception {

        APIMgtResourceNotFoundException sampleAPIMgtResourceNotFoundException = new APIMgtResourceNotFoundException(
                "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(sampleAPIMgtResourceNotFoundException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert.assertTrue("Invalid exception has been passed.", RestApiUtil.isDueToResourceNotFound(testThrowable));
    }

    @Test
    public void testIsDueToResourceNotFoundWithResourceNotFoundException() throws Exception {

        ResourceNotFoundException testResourceNotFoundException = new ResourceNotFoundException("New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(testResourceNotFoundException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert.assertTrue("Invalid exception has been passed.", RestApiUtil.isDueToResourceNotFound(testThrowable));
    }

    @Test
    public void testIsDueToResourceNotFoundWithInvalidException() throws Exception {

        APIMgtAuthorizationFailedException testAPIMgtAuthorizationFailedException =
                new APIMgtAuthorizationFailedException(
                        "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(testAPIMgtAuthorizationFailedException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert.assertFalse("Invalid exception has been passed.", RestApiUtil.isDueToResourceNotFound(testThrowable));
    }

    @Test
    public void testisDueToAuthorizationFailureWithAuthorizationFailedException() throws Exception {

        AuthorizationFailedException sampleAuthorizationFailedException = new AuthorizationFailedException(
                "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(sampleAuthorizationFailedException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert.assertTrue("Invalid exception has been passed.", RestApiUtil.isDueToAuthorizationFailure(testThrowable));
    }

    @Test
    public void testisDueToAuthorizationFailureWithAPIMgtAuthorizationFailedException() throws Exception {

        APIMgtAuthorizationFailedException sampleAPIMgtAuthorizationFailedException =
                new APIMgtAuthorizationFailedException(
                        "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(sampleAPIMgtAuthorizationFailedException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert.assertTrue("Invalid exception has been passed.", RestApiUtil.isDueToAuthorizationFailure(testThrowable));
    }

    @Test
    public void testisDueToAuthorizationFailureWithInvalidException() throws Exception {

        ResourceNotFoundException sampleResourceNotFoundException = new ResourceNotFoundException(
                "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(sampleResourceNotFoundException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert
                .assertFalse("Invalid exception has been passed.", RestApiUtil.isDueToAuthorizationFailure(testThrowable));
    }

    @Test
    public void testHandleAuthorizationFailure() {

        String errorDescription = "User is not authorized to access the API";
        APIManagementException apiManagementException = new APIManagementException("API management exception test");

        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        PowerMockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        try {
            RestApiUtil.handleAuthorizationFailure(errorDescription, apiManagementException, log);
        } catch (ForbiddenException exception) {
            Assert.assertEquals(errorDescription, exception.getMessage());
            Mockito.verify(log).error(errorDescription, apiManagementException);
        }
    }

    @Test
    public void testHandleAuthorizationFailureArg() {

        String apiId = "testapiid_4567ui456789";
        String expectedErrormessage = "You don't have permission to access the " + RestApiConstants.RESOURCE_API + " " +
                "with Id " + apiId;
        APIManagementException apiManagementException = new APIManagementException("API management exception test");

        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        PowerMockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        try {
            RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, apiManagementException, log);
        } catch (ForbiddenException exception) {
            Assert.assertEquals(expectedErrormessage, exception.getMessage());
            Mockito.verify(log).error(expectedErrormessage, apiManagementException);
        }
    }

    @Test
    public void testHandleAuthorizationFailureArgWithEmptyID() {

        String apiId = "";
        String expectedErrormessage = "You don't have permission to access the " + RestApiConstants.RESOURCE_API;
        APIManagementException apiManagementException = new APIManagementException("API management exception test");

        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        PowerMockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        try {
            RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, apiManagementException, log);
        } catch (ForbiddenException exception) {
            Assert.assertEquals(expectedErrormessage, exception.getMessage());
        }
        Mockito.verify(log).error(expectedErrormessage, apiManagementException);
    }

    @Test
    public void testbuildForbiddenExceptionWithEmptyID() {

        String apiId = "";
        String expectedErrormessage = "You don't have permission to access the " + RestApiConstants.RESOURCE_API;

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(403l);
        errorDTO.setMoreInfo("");
        errorDTO.setMessage(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT);
        errorDTO.setDescription(expectedErrormessage);

        mockStatic(RestApiUtil.class);
        when(RestApiUtil.getErrorDTO(Mockito.any(), Mockito.any(), Mockito.eq(expectedErrormessage)))
                .thenReturn(errorDTO);
        when(RestApiUtil.buildForbiddenException(RestApiConstants.RESOURCE_API, apiId)).thenCallRealMethod();

        ForbiddenException forbiddenException = RestApiUtil.buildForbiddenException(RestApiConstants.RESOURCE_API,
                apiId);

        Assert.assertEquals(expectedErrormessage, forbiddenException.getMessage());
    }

    @Test
    public void testbuildForbiddenException() {

        String apiId = "TesT_API_ID_45678";
        String expectedErrormessage = "You don't have permission to access the " + RestApiConstants.RESOURCE_API + " " +
                "with Id " + apiId;

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(403l);
        errorDTO.setMoreInfo("");
        errorDTO.setMessage(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT);
        errorDTO.setDescription(expectedErrormessage);
        mockStatic(RestApiUtil.class);
        when(RestApiUtil.getErrorDTO(Mockito.any(), Mockito.any(), Mockito.eq(expectedErrormessage)))
                .thenReturn(errorDTO);
        when(RestApiUtil.buildForbiddenException(RestApiConstants.RESOURCE_API, apiId)).thenCallRealMethod();
        ForbiddenException forbiddenException = RestApiUtil
                .buildForbiddenException(RestApiConstants.RESOURCE_API, apiId);

        Assert.assertEquals(expectedErrormessage, forbiddenException.getMessage());
    }

    @Test
    public void testRootCauseMessageMatches() throws Exception {

        String rootCauseMessage = "Entered start index seems to be greater than the limit count. Please verify your " +
                "parameters";
        ResourceNotFoundException resourceNotFoundException = new ResourceNotFoundException(
                "Resource Not Found Exception");
        Throwable testThrowable = Mockito.mock(Throwable.class);
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(testThrowable)
                .when(RestApiUtil.class, "getPossibleErrorCause", resourceNotFoundException);
        when(testThrowable.getMessage()).thenReturn(rootCauseMessage);

        Assert.assertTrue(RestApiUtil.rootCauseMessageMatches(resourceNotFoundException,
                "index seems to be greater than the limit count"));
    }

    @Test
    public void testRootCauseMessageMatchesNegative() throws Exception {

        String rootCauseMessage = "Entered start index seems to be greater than the limit count. Please verify your " +
                "parameters";
        ResourceNotFoundException resourceNotFoundException = new ResourceNotFoundException(
                "Resource Not Found Exception");
        Throwable testThrowable = Mockito.mock(Throwable.class);
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(testThrowable)
                .when(RestApiUtil.class, "getPossibleErrorCause", resourceNotFoundException);
        when(testThrowable.getMessage()).thenReturn(rootCauseMessage);

        Assert.assertFalse(RestApiUtil.rootCauseMessageMatches(resourceNotFoundException,
                "Caused by exceeded limit count"));
    }

    @Test
    public void testisDueToResourceAlreadyExistsWithAPIMgtResourceAlreadyExistsException() throws Exception {

        APIMgtResourceAlreadyExistsException apiMgtResourceAlreadyExistsException =
                new APIMgtResourceAlreadyExistsException(
                        "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(apiMgtResourceAlreadyExistsException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert
                .assertTrue("Invalid exception has been passed.", RestApiUtil.isDueToResourceAlreadyExists(testThrowable));
    }

    @Test
    public void testisDueToResourceAlreadyExistsWithDuplicateAPIException() throws Exception {

        DuplicateAPIException duplicateAPIException = new DuplicateAPIException(
                "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(duplicateAPIException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert
                .assertTrue("Invalid exception has been passed.", RestApiUtil.isDueToResourceAlreadyExists(testThrowable));
    }

    @Test
    public void testisDueToResourceAlreadyExistsWithInvalidException() throws Exception {

        APIMgtResourceNotFoundException apiMgtResourceNotFoundException = new APIMgtResourceNotFoundException(
                "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(apiMgtResourceNotFoundException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert
                .assertFalse("Invalid exception has been passed.", RestApiUtil.isDueToResourceAlreadyExists(testThrowable));
    }

    @Test
    public void testisDueToApplicationNameWhiteSpaceValidation() throws Exception {

        ApplicationNameWhiteSpaceValidationException applicationNameWhiteSpaceValidationException =
                new ApplicationNameWhiteSpaceValidationException(
                        "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(applicationNameWhiteSpaceValidationException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert.assertTrue("Invalid exception has been passed.",
                RestApiUtil.isDueToApplicationNameWhiteSpaceValidation(testThrowable));
    }

    @Test
    public void testisDueToApplicationNameWhiteSpaceValidationWithInvalidException() throws Exception {

        ApplicationNameWithInvalidCharactersException applicationNameWithInvalidCharactersException =
                new ApplicationNameWithInvalidCharactersException(
                        "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(applicationNameWithInvalidCharactersException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert.assertFalse("Invalid exception has been passed.",
                RestApiUtil.isDueToApplicationNameWhiteSpaceValidation(testThrowable));
    }

    @Test
    public void testisDueToApplicationNameWithInvalidCharacters() throws Exception {

        ApplicationNameWithInvalidCharactersException applicationNameWithInvalidCharactersException =
                new ApplicationNameWithInvalidCharactersException(
                        "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(applicationNameWithInvalidCharactersException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert.assertTrue("Invalid exception has been passed.",
                RestApiUtil.isDueToApplicationNameWithInvalidCharacters(testThrowable));
    }

    @Test
    public void testisDueToApplicationNameWithInvalidCharactersWithInvalidException() throws Exception {

        ApplicationNameWhiteSpaceValidationException applicationNameWhiteSpaceValidationException =
                new ApplicationNameWhiteSpaceValidationException(
                        "New Sample exception");
        Throwable testThrowable = new Throwable();
        PowerMockito.spy(RestApiUtil.class);
        PowerMockito.doReturn(applicationNameWhiteSpaceValidationException)
                .when(RestApiUtil.class, "getPossibleErrorCause", testThrowable);

        Assert.assertFalse("Invalid exception has been passed.",
                RestApiUtil.isDueToApplicationNameWithInvalidCharacters(testThrowable));
    }

    @Test
    public void testHandleResourceAlreadyExistsError() {

        String errorMessage = "Requested new version already exists";
        Throwable throwable = new Throwable();
        Exception exceptionCaught = null;

        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        PowerMockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        try {
            RestApiUtil.handleResourceAlreadyExistsError(errorMessage, throwable, log);
        } catch (ConflictException exception) {
            exceptionCaught = exception;
        }

        Assert.assertEquals(errorMessage, exceptionCaught.getMessage());
        Mockito.verify(log).error(errorMessage, throwable);
    }

    @Test
    public void testHandleResourceNotFoundError() {

        String errorMessage = "Requested new version already exists";
        Throwable throwable = new Throwable();
        Exception exceptionCaught = null;

        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        PowerMockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        try {
            RestApiUtil.handleResourceAlreadyExistsError(errorMessage, throwable, log);
        } catch (ConflictException exception) {
            exceptionCaught = exception;
        }

        Assert.assertEquals(errorMessage, exceptionCaught.getMessage());
        Mockito.verify(log).error(errorMessage, throwable);
    }

    @Test
    public void testGetRequestedTenantDomain() {

        System.setProperty(CarbonBaseConstants.CARBON_HOME, "");
        String tenantDomain = "anotherTenant.com";

        mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(tenantDomain);
        String expectedDomain = RestApiUtil.getRequestedTenantDomain(tenantDomain);

        Assert.assertEquals(tenantDomain, expectedDomain);
    }

    @Test
    public void testGetRequestedTenantDomainWithEmptyTenants() {

        String tenantDomain = "anotherTenant.com";
        mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(tenantDomain);

        String expectedDomain = RestApiUtil.getRequestedTenantDomain("");
        Assert.assertEquals(tenantDomain, expectedDomain);
    }

}
