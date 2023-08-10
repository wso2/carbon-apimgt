/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.client;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;

/**
 * This is the custom exception class for exceptions in the Feign Choreo Http Client.
 */
public class ChoreoClientException extends APIManagementException {

    private int statusCode;
    private String reason;

    public ChoreoClientException() {
        super(ExceptionCodes.INTERNAL_ERROR);
    }

    public ChoreoClientException(String message) {

        super(message);
    }

    public ChoreoClientException(String message, Exception e) {

        super(message, e);
    }

    public ChoreoClientException(int statusCode, String reason) {

        super("Received status code: " + statusCode + " Reason: " + reason);
        this.reason = reason;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {

        return statusCode;
    }

    public void setStatusCode(int statusCode) {

        this.statusCode = statusCode;
    }

    public String getReason() {

        return reason;
    }

    public void setReason(String reason) {

        this.reason = reason;
    }
}

