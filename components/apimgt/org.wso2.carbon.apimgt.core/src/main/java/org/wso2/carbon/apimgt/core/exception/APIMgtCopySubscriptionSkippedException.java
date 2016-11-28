/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.exception;

/**
 * This class used to throw warn message of skipped list of subscription
 */
public class APIMgtCopySubscriptionSkippedException extends APIManagementException {
    /**
     * @param message Error message
     * @param cause   Error cause
     */
    public APIMgtCopySubscriptionSkippedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public long getErrorCode() {
        return super.getErrorCode();
    }

    @Override
    public String getErrorMsg() {
        return super.getErrorMsg();
    }

    /**
     * @param cause Error cause
     */
    public APIMgtCopySubscriptionSkippedException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message            Error message
     * @param cause              Error cause
     * @param enableSuppression  whether you need enable suppressions
     * @param writableStackTrace Writable error stack trace.
     */
    protected APIMgtCopySubscriptionSkippedException(String message, Throwable cause, boolean enableSuppression,
                                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message Error message
     */
    public APIMgtCopySubscriptionSkippedException(String message) {
        super(message);
    }

    /**
     * This is a default constructure where you can pass error code to error DTO
     *
     * @param message Error message
     * @param code    Exception code that need to pass to the error DTO
     */
    public APIMgtCopySubscriptionSkippedException(String message, ExceptionCodes code) {
        super(message, code);
    }
}
