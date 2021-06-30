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

/**
 * Exception related to jwt generation.
 */
public class JWTGeneratorException extends Exception {
    private ErrorHandler errorHandler;

    /**
     * Get error handler object.
     *
     * @return ErrorHandler
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public JWTGeneratorException(String msg) {
        super(msg);
        this.errorHandler = ExceptionCodes.INTERNAL_ERROR;
    }

    public JWTGeneratorException(String msg, Throwable e) {
        super(msg, e);
        this.errorHandler = ExceptionCodes.INTERNAL_ERROR;
    }

    public JWTGeneratorException(Throwable throwable) {
        super(throwable);
        this.errorHandler = ExceptionCodes.INTERNAL_ERROR;
    }

    /**
     * This is a default constructure where you can pass error code to error DTO.
     *
     * @param message Error message
     * @param code    Exception code that need to pass to the error DTO
     */
    public JWTGeneratorException(String message, ErrorHandler code) {
        super(message);
        this.errorHandler = code;
    }

    /**
     * This is a default constructure where you can pass error code to error DTO.
     *
     * @param code Exception code that need to pass to the error DTO
     */
    public JWTGeneratorException(ErrorHandler code) {
        super(code.getErrorCode() + ":" + code.getErrorMessage() + "::" + code.getErrorDescription());
        this.errorHandler = code;
    }

    /**
     * This is a default constructure where you can pass error code to error DTO.
     *
     * @param message Error message
     * @param cause   throwable object.
     * @param code    Exception code that need to pass to the error DTO
     */
    public JWTGeneratorException(String message, Throwable cause, ErrorHandler code) {
        super(message, cause);
        this.errorHandler = code;
    }
}
