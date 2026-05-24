/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;

import java.util.Map;

/**
 * Carries gateway environment validation field keys for environment create/update responses.
 */
public class GatewayEnvironmentValidationException extends APIManagementException {

    private final Map<String, String> errors;

    public GatewayEnvironmentValidationException(String message, ErrorHandler code,
            Map<String, String> errors) {

        super(message, code);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
