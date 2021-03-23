/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.common.gateway.exception;

import java.util.Arrays;

/**
 * This enum class holds error codes that we need to pass to upper level. For example, to the UI.
 * You have to define your custom error codes here.
 */
public enum
ExceptionCodes implements ErrorHandler {

    INTERNAL_ERROR(900967, "General Error", 500, "Server Error Occurred");

    private final long errorCode;
    private final String errorMessage;
    private final int httpStatusCode;
    private final String errorDescription;
    private boolean stackTrace = false;

    /**
     * @param errorCode        This is unique error code that pass to upper level.
     * @param msg              The error message that you need to pass along with the error code.
     * @param httpErrorCode    This HTTP status code which should return from REST API layer. If you don't want to pass
     *                         a http status code keep it blank.
     * @param errorDescription The error description.
     */
    ExceptionCodes(long errorCode, String msg, int httpErrorCode, String errorDescription, boolean stackTrace) {
        this.errorCode = errorCode;
        this.errorMessage = msg;
        this.httpStatusCode = httpErrorCode;
        this.errorDescription = errorDescription;
        this.stackTrace = stackTrace;
    }

    /**
     * @param errorCode        This is unique error code that pass to upper level.
     * @param msg              The error message that you need to pass along with the error code.
     * @param httpErrorCode    This HTTP status code which should return from REST API layer. If you don't want to pass
     *                         a http status code keep it blank.
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

    public boolean printStackTrace() {

        return stackTrace;
    }

    /**
     * Create an ErrorHandler instance with the provided ExceptionCode filled with some dynamic input values.
     *
     * @param errorHandler ErrorHandler or ExceptionCode object
     * @param params       dynamic values to be filled
     * @return ErrorHandler instance with the provided ExceptionCode filled with some dynamic input values
     */
    public static ErrorHandler from(ErrorHandler errorHandler, String... params) {
        String message = errorHandler.getErrorMessage();
        String description = errorHandler.getErrorDescription();

        if (params != null && params.length > 0) {
            int placesToFormatInMessage = message.length() - message.replace("%", "").length();
            int placesToFormatInDescription = description.length() - description.replace("%", "").
                    length();

            String[] part1 = Arrays.copyOfRange(params, 0, placesToFormatInMessage);
            String[] part2 = Arrays.copyOfRange(params, placesToFormatInMessage,
                    placesToFormatInMessage + placesToFormatInDescription);

            if (placesToFormatInMessage > 0) {
                message = String.format(message, part1);
            }
            if (placesToFormatInDescription > 0) {
                description = String.format(description, part2);
            }
        }
        return new ErrorItem(message, description, errorHandler.getErrorCode(), errorHandler.getHttpStatusCode(),
                errorHandler.printStackTrace());
    }
}

