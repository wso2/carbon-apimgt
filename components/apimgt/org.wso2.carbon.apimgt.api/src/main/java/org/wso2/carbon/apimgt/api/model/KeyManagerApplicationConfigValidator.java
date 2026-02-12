/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIManagementException;

import java.util.Map;

/**
 * Interface to validate application configuration constraints for Key Managers.
 */
public interface KeyManagerApplicationConfigValidator {

    /**
     * Validates that the constraints themselves are logically sound.
     *
     * @param constraints Map of constraints.
     * @throws APIManagementException If constraints are invalid.
     */
    void validateMetadata(Map<String, Object> constraints) throws APIManagementException;

    /**
     * Validates the input value against the given constraints.
     *
     * @param inputValue  The value provided by the user/application.
     * @param constraints Map of constraints (e.g., min, max, pattern).
     * @return true if valid, false otherwise.
     */
    boolean validate(Object inputValue, Map<String, Object> constraints);

    /**
     * Returns a descriptive error message in case of validation failure.
     *
     * @return Error message string.
     */
    String getErrorMessage();
}
