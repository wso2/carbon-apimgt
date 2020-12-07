/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.importexport;

import org.wso2.carbon.apimgt.api.ErrorHandler;

/**
 * This is the class to represent APIImportException. This exception is used to indicate the
 * exceptions that might be occurred during API import process.
 */
public class APIImportExportException extends Exception {

    private ErrorHandler errorHandler;

    /**
     * Get error handler object.
     * @return ErrorHandler
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public APIImportExportException(String errorMessage) {
        super(errorMessage);
    }

    public APIImportExportException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * This is a default constructor where you can pass error code to error DTO
     *
     * @param msg  Error message
     * @param code Exception code that need to pass to the error DTO
     */
    public APIImportExportException(String msg, ErrorHandler code) {
        super(msg);
        this.errorHandler = code;
    }

    /**
     * This is a default constructor where you can pass error code to error DTO
     * @param message Error message
     * @param cause throwable object.
     * @param code Exception code that need to pass to the error DTO
     */
    public APIImportExportException(String message, Throwable cause, ErrorHandler code) {
        super(message, cause);
        this.errorHandler = code;
    }
}
