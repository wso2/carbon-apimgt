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
package org.wso2.carbon.apimgt.rest.api.common.exception;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;

/**
 * Exception class for OAuth2 security
 */
public class APIMgtSecurityException extends APIManagementException {

    /**
     * Calling super class constructure.
     * @param message Error message
     * @param code Error code
     */
    public APIMgtSecurityException(String message, ExceptionCodes code) {
        super(message, code);
    }

    public APIMgtSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public APIMgtSecurityException(Throwable cause) {
        super(cause);
    }

    public APIMgtSecurityException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public APIMgtSecurityException(String message) {
        super(message);
    }

}
