/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.api.error;

/**
 * This is the custom exception class for Governance.
 */
public class APIMGovernanceException extends Exception {

    private ErrorHandler errorHandler;

    /**
     * Get error handler object.
     *
     * @return ErrorHandler
     */
    public ErrorHandler getErrorHandler() {

        return errorHandler;
    }

    public APIMGovernanceException(String msg) {
        super(msg);
        this.errorHandler = APIMGovExceptionCodes.INTERNAL_SERVER_ERROR;
    }

    public APIMGovernanceException(String msg, Throwable e) {
        super(msg, e);
        this.errorHandler = APIMGovExceptionCodes.INTERNAL_SERVER_ERROR;
    }

    public APIMGovernanceException(Throwable throwable) {
        super(throwable);
        this.errorHandler = APIMGovExceptionCodes.INTERNAL_SERVER_ERROR;
    }

    /**
     * This is a default constructor where you can pass error code to error DTO
     *
     * @param message Error message
     * @param code    Exception code that need to pass to the error DTO
     */
    public APIMGovernanceException(String message, ErrorHandler code) {
        super(message);
        this.errorHandler = code;
    }

    /**
     * This is a default constructor where you can pass error code to error DTO
     *
     * @param code Exception code that need to pass to the error DTO
     */
    public APIMGovernanceException(ErrorHandler code) {
        super(code.getErrorCode() + ":" + code.getErrorMessage() + "::" + code.getErrorDescription());
        this.errorHandler = code;
    }

    /**
     * This is a default constructor where you can pass error code to error DTO
     *
     * @param message Error message
     * @param cause   throwable object.
     * @param code    Exception code that need to pass to the error DTO
     */
    public APIMGovernanceException(String message, Throwable cause, ErrorHandler code) {
        super(message, cause);
        this.errorHandler = code;
    }

    /**
     * This is a default constructor where you can pass error code to error DTO
     *
     * @param code  Exception code that need to pass to the error DTO
     * @param cause throwable object.
     */
    public APIMGovernanceException(ErrorHandler code, Throwable cause, Object... args) {
        super(code.getErrorCode() + ":" + code.getErrorMessage() + "::"
                + String.format(code.getErrorDescription(), args), cause);
        this.errorHandler = new ErrorItem(code.getErrorMessage(),
                String.format(code.getErrorDescription(), args),
                code.getErrorCode(), code.getHttpStatusCode(), code.printStackTrace());
    }

    /**
     * This is a default constructor where you can pass error code to error DTO
     *
     * @param code Exception code that need to pass to the error DTO
     * @param args arguments
     */
    public APIMGovernanceException(ErrorHandler code, Object... args) {

        super(code.getErrorCode() + ":" + code.getErrorMessage() + "::"
                + String.format(code.getErrorDescription(), args));
        this.errorHandler = new ErrorItem(code.getErrorMessage(),
                String.format(code.getErrorDescription(), args),
                code.getErrorCode(), code.getHttpStatusCode(), code.printStackTrace());
    }
}
