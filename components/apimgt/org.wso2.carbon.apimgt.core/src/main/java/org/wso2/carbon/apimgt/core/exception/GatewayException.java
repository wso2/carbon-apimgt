package org.wso2.carbon.apimgt.core.exception;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

/**
 * Custom exception for gateway related exceptions
 */
public class GatewayException extends APIManagementException {

    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, ExceptionCodes code) {
        super(message, code);
    }

    public GatewayException(String message, Exception e) {
        super(message, e);
    }

    public GatewayException(String message, Exception e, ExceptionCodes code) {
        super(message, e, code);
    }

}
