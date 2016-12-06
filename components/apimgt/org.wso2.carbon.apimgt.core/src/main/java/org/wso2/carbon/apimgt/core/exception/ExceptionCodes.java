/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.exception;

/**
 * This enum class holds error codes that we need to pass to upper level. For example, to the UI.
 * You have to define your custom error codes here.
 */
public enum ExceptionCodes implements ErrorHandler {

    API_ALREADY_EXISTS(900300, "The API already exists.", 400, " This is error description"),
    APPLICATION_ALREADY_EXISTS(900301, "The application already exists.", 400, " This is error description"),
    APIMGT_DAO_EXCEPTION(900302, "Something wrong in DAO layer.", 500, " This is error description"),
    APIMGT_LIFECYCLE_EXCEPTION(900303, "Life cycle exception occurred", 500, " This is error description"),
    TIER_CANNOT_BE_NULL(900304, "The tier cannot be null.", 400, " This is error description"),
    TIER_NAME_INVALID(900305, "The tier name is invalid.", 400, " This is error description"),
    SWAGGER_PARSE_EXCEPTION(900306, "Error while parsing swagger json", 500, "Error while parsing swagger json"),
    APPLICATION_NOT_FOUND(900307, "Application not found", 400, "Error while parsing swagger json"),
    PARAMETER_NOT_PROVIDED(900308, "Parameter value missing", 400,
            "Some of the mandatory parameter values were missing"),
    API_NOT_FOUND(900309, "API not found", 400, "API could not be found");

    private final long errorCode;
    private final String errorMessage;
    private final int httpStatusCode;
    private final String errorDescription;

    /**
     *
     * @param errorCode This is unique error code that pass to upper level.
     * @param msg The error message that you need to pass along with the error code.
     * @param httpErrorCode This HTTP status code which should return from REST API layer. If you don't want to pass a
     *                      http status code keep it blank.
     * @param errorDescription The error description.
     */
    ExceptionCodes(long errorCode, String msg, int httpErrorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorMessage = msg;
        this.httpStatusCode = httpErrorCode;
        this.errorDescription = errorDescription;
    }

    @Override
    public long getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    @Override
    public String getErrorDescription() {
        return this.errorDescription;
    }

}
