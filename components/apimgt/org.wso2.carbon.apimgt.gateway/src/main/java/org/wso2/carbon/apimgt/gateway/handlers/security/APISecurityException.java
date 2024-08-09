/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

/**
 * Represents an API security violation or a system error that may have occurred
 * while validating security requirements.
 */
public class APISecurityException extends Exception {
    
    private int errorCode;
    private String description;

    public APISecurityException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public APISecurityException(int errorCode, String message, String description) {
        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    public APISecurityException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }
}
