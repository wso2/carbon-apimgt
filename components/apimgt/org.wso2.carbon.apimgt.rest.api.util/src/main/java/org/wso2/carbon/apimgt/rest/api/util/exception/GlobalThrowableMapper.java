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
 */

package org.wso2.carbon.apimgt.rest.api.util.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.EOFException;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class GlobalThrowableMapper implements ExceptionMapper<Throwable> {

    private static final Log log = LogFactory.getLog(GlobalThrowableMapper.class);

    private ErrorDTO e500 = new ErrorDTO();

    public GlobalThrowableMapper() {
        e500.setCode((long) 500);
        e500.setMessage("Internal server error");
        e500.setMoreInfo("");
        e500.setDescription("The server encountered an internal error. Please contact administrator.");
    }

    @Override
    public Response toResponse(Throwable e) {

        if (e instanceof ClientErrorException) {
            log.error("Client error", e);
            return ((ClientErrorException) e).getResponse();
        }

        if (e instanceof NotFoundException) {
            log.error("Resource not found", e);
            return ((NotFoundException) e).getResponse();
        }

        if (e instanceof PreconditionFailedException) {
            log.error("Precondition failed", e);
            return ((PreconditionFailedException) e).getResponse();
        }

        if (e instanceof BadRequestException) {
            log.error("Bad request", e);
            return ((BadRequestException) e).getResponse();
        }

        if (e instanceof ConstraintViolationException) {
            log.error("Constraint violation", e);
            return ((ConstraintViolationException) e).getResponse();
        }

        if (e instanceof ForbiddenException) {
            log.error("Resource forbidden", e);
            return ((ForbiddenException) e).getResponse();
        }

        if (e instanceof ConflictException) {
            log.error("Conflict", e);
            return ((ConflictException) e).getResponse();
        }

        if (e instanceof MethodNotAllowedException) {
            log.error("Method not allowed", e);
            return ((MethodNotAllowedException) e).getResponse();
        }

        if (e instanceof InternalServerErrorException) {
            String errorMessage = "The server encountered an internal error : " + e.getMessage();
            log.error(errorMessage, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(e500).build();
        }

        if (e instanceof JsonParseException) {
            String errorMessage = "Malformed request body.";
            log.error(errorMessage, e);
            //noinspection ThrowableResultOfMethodCallIgnored
            return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
        }

        if (e instanceof JsonMappingException) {
            if (e instanceof UnrecognizedPropertyException) {
                UnrecognizedPropertyException unrecognizedPropertyException = (UnrecognizedPropertyException) e;
                String unrecognizedProperty = unrecognizedPropertyException.getPropertyName();
                String errorMessage = "Unrecognized property '" + unrecognizedProperty + "'";
                log.error(errorMessage, e);
                //noinspection ThrowableResultOfMethodCallIgnored
                return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
            } else {
                String errorMessage = "One or more request body parameters contain disallowed values.";
                log.error(errorMessage, e);
                //noinspection ThrowableResultOfMethodCallIgnored
                return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
            }
        }

        if (e instanceof AuthenticationException) {
            ErrorDTO errorDetail = new ErrorDTO();
            errorDetail.setCode((long)401);
            errorDetail.setMoreInfo("");
            errorDetail.setMessage("");
            errorDetail.setDescription(e.getMessage());
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(errorDetail)
                    .build();
        }

        //This occurs when received an empty body in an occasion where the body is mandatory
        if (e instanceof EOFException) {
            String errorMessage = "Request payload cannot be empty.";
            log.error(errorMessage, e);
            //noinspection ThrowableResultOfMethodCallIgnored
            return RestApiUtil.buildBadRequestException(errorMessage).getResponse();
        }

        if (e instanceof APIManagementException) {

            ErrorHandler selectedErrorHandler = null;
            List<Throwable> throwableList = ExceptionUtils.getThrowableList(e);
            for (Throwable t : throwableList) {
                if (t instanceof APIManagementException) {
                    APIManagementException apimException = (APIManagementException) t;
                    ErrorHandler errorHandler = apimException.getErrorHandler();
                    if (errorHandler != null) {
                        if (selectedErrorHandler == null) {
                            selectedErrorHandler = errorHandler;
                        } else {
                            selectedErrorHandler =
                                    errorHandler.getHttpStatusCode() < selectedErrorHandler.getHttpStatusCode()
                                            && errorHandler.getHttpStatusCode() > 0 ?
                                            errorHandler : selectedErrorHandler;
                        }
                    }
                }
            }

            if (selectedErrorHandler != null) {
                // logs the error as the error may be not logged by the origin
                if (selectedErrorHandler.printStackTrace()) {
                    log.error("A defined exception has been captured and mapped to an HTTP response " +
                            "by the global exception mapper ", e);
                } else {
                    // Not to log the stack trace due to error code was mark as not print stacktrace.
                    log.error(e.getMessage());
                    if (log.isDebugEnabled()) {
                        log.debug("A defined exception has been captured and mapped to an HTTP response " +
                                "by the global exception mapper ", e);
                    }
                }

                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(selectedErrorHandler);
                return Response
                        .status(Response.Status.fromStatusCode(selectedErrorHandler.getHttpStatusCode()))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(errorDTO)
                        .build();
            }
        }

        //unknown exception log and return
        log.error("An unknown exception has been captured by the global exception mapper.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(e500)
                .build();
    }
}
