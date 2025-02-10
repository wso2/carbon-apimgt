/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.rest.api.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.ErrorHandler;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;

import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * This is the custom exception mapper for Governance.
 */
public class APIMGovernanceExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Log log = LogFactory.getLog(APIMGovernanceExceptionMapper.class);

    @Override
    public Response toResponse(Throwable e) {

        if (e instanceof APIMGovernanceException) {

            ErrorHandler selectedErrorHandler = null;
            List<Throwable> throwableList = ExceptionUtils.getThrowableList(e);
            for (Throwable t : throwableList) {
                if (t instanceof APIMGovernanceException) {
                    APIMGovernanceException govException = (APIMGovernanceException) t;
                    ErrorHandler errorHandler = govException.getErrorHandler();
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
                            "by the governance exception mapper ", e);
                } else {
                    // if the request is a bad request, not to log error message or anything
                    if (selectedErrorHandler.getHttpStatusCode() != 400) {
                        // Not to log the stack trace due to error code was mark as not print stacktrace.
                        log.error(e.getMessage());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "A defined exception has been captured and mapped to an HTTP response by the " +
                                        "governance exception mapper ",
                                e);
                    }
                }

                ErrorDTO errorDTO = APIMGovernanceAPIUtil.getErrorDTO(selectedErrorHandler);
                return Response
                        .status(Response.Status.fromStatusCode(selectedErrorHandler.getHttpStatusCode()))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(errorDTO)
                        .build();
            }
        }
        return null;
    }
}
