/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ExceptionCodes;

import static org.wso2.carbon.apimgt.api.ExceptionCodes.FEDERATED_GATEWAY_VALIDATION_FAILED;

/**
 *
 * This is the custom exception class for external gateway API validation
 *
 */
public class ExternalGatewayAPIValidationException extends APIManagementException {


    public ExternalGatewayAPIValidationException(String message) {
        super(message, ExceptionCodes.from(FEDERATED_GATEWAY_VALIDATION_FAILED, message));
    }

    public ExternalGatewayAPIValidationException(String message, Throwable e) {
        super(message, e);
    }

    public ExternalGatewayAPIValidationException(String message, ErrorHandler code) {
        super(message, code);
    }

    public ExternalGatewayAPIValidationException(Throwable throwable) {
        super(throwable);
    }
}
