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
public enum ExceptionCodes {

    API_ALREADY_EXISTS(900300, "The API already exists.", 400),
    APIMGT_DAO_EXCEPTION(900301, "Something wrong in DAO layer.", 500),
    APIMGT_LIFECYCLE_EXCEPTION(900301, "Life cycle exception occured", 500);

    private final long errorCode;
    private final String errorMessage;
    private final int httpStatusCode;

    ExceptionCodes(long errorCode, String msg, int httpErrorCode) {
        this.errorCode = errorCode;
        this.errorMessage = msg;
        this.httpStatusCode = httpErrorCode;
    }

    public long getErrorCode() {
        return this.errorCode;
    }

    public String getMsg() {
        return this.errorMessage;
    }

    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }
}
