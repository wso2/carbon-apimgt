/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Generic exception class for import and export
 */
public class APIMgtEntityImportExportException extends APIManagementException {

    public APIMgtEntityImportExportException(String message, Throwable cause) {
        super(message, cause);
    }

    public APIMgtEntityImportExportException(Throwable cause) {
        super(cause);
    }

    protected APIMgtEntityImportExportException(String message, Throwable cause, boolean enableSuppression,
                                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public APIMgtEntityImportExportException(String message) {
        super(message);
    }

    public APIMgtEntityImportExportException(String message, ErrorHandler code) {
        super(message, code);
    }

    public APIMgtEntityImportExportException(String message, Throwable cause, ErrorHandler code) {
        super(message, cause, code);
    }
}
