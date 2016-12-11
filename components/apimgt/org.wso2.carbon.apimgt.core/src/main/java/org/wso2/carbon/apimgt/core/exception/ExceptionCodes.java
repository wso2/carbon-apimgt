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
    APIMGT_DAO_EXCEPTION(900302, "Internal server error.", 500, " This is error description"),
    APIMGT_LIFECYCLE_EXCEPTION(900303, "Life cycle exception occurred", 500, " This is error description"),
    TIER_CANNOT_BE_NULL(900304, "The tier cannot be null.", 400, " This is error description"),
    TIER_NAME_INVALID(900305, "The tier name is invalid.", 400, " This is error description"),
    SWAGGER_PARSE_EXCEPTION(900306, "Error while parsing swagger json", 500, "Error while parsing swagger json"),
    APPLICATION_NOT_FOUND(900307, "Application not found", 400, "Error while parsing swagger json"),
    PARAMETER_NOT_PROVIDED(900308, "Parameter value missing", 400,
            "Some of the mandatory parameter values were missing"),
    API_NOT_FOUND(900309, "API not found", 400, "API could not be found"),
    SUBSCRIPTION_NOT_FOUND(900310, "Subscription not found", 400, "Couldn't retrieve Subscriptions for API"),
    UPDATE_STATE_CHANGE(900311, "API fields have state changes", 400, "Couldn't Update as API have changes can't be " +
            "done"),
    DOCUMENT_ALREADY_EXISTS(900312, "Document already exists", 400, "Document Already Exists"),
    COULD_NOT_UPDATE_API(900313, "Error has occurred. Could not update the API", 500, "Error has occurred. Could not "
            + "update the API"),
    AUTH_GENERAL_ERROR(900900, "Authorization Error", 403, " Error in authorization"),
    ACCESS_TOKEN_INACTIVE(900304, "Invalid Credentials", 401, " Access token is inactive."),
    ACCESS_TOKEN_EXPIRED(900903, "Invalid Credentials", 401, " Access token is expired."),
    MISSING_CREDENTIALS(900902, "Missing Credentials", 401, " Please provide an active access token to proceed"),
    INVALID_SCOPE(900910, "Invalid Scope", 401, " You are not authorized to access the resource."),
    INVALID_AUTHORIZATION_HEADER(900911, "Invalid Authorization header", 401,
            " Please provide the Authorization : Bearer <> token to proceed.");

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
