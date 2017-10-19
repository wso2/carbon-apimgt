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
 * Exception class for ETag generation related functionality
 */
public class ETagGenerationException extends APIManagementException {

    /**
     * Calling super class constructure.
     * @param message Error message
     * @param code Error code
     */
    public ETagGenerationException(String message, ExceptionCodes code) {
        super(message, code);
    }

    public ETagGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ETagGenerationException(Throwable cause) {
        super(cause);
    }

    public ETagGenerationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ETagGenerationException(String message) {
        super(message);
    }

}
