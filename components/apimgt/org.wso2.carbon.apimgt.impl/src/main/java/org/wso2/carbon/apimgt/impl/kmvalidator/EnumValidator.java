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

package org.wso2.carbon.apimgt.impl.kmvalidator;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerApplicationConfigValidator;

import java.util.List;
import java.util.Map;

/**
 * Validator implementation for Enum-based constraints.
 */
public class EnumValidator implements KeyManagerApplicationConfigValidator {

    private String errorMessage;

    @Override
    public void validateMetadata(Map<String, Object> constraints) throws APIManagementException {
        Object allowedValuesObj = constraints.get("allowed");

        if (allowedValuesObj == null) {
            throw new APIManagementException("Allowed values list ('allowed') not found for enum constraint.");
        }

        if (!(allowedValuesObj instanceof List)) {
            throw new APIManagementException("Allowed values must be a list.");
        }
    }

    @Override
    public boolean validate(Object inputValue, Map<String, Object> constraints) {
        if (inputValue == null) {
            errorMessage = "Input value is null.";
            return false;
        }

        Object allowedValuesObj = constraints.get("allowed");

        if (!(allowedValuesObj instanceof List)) {
            errorMessage = "Allowed values list not found in constraints.";
            return false;
        }

        List<?> allowedValues = (List<?>) allowedValuesObj;
        if (inputValue instanceof List) {
            List<?> inputList = (List<?>) inputValue;
            for (Object item : inputList) {
                if (item == null) {
                    errorMessage = "Null value found in input list.";
                    return false;
                }
                if (!isValueAllowed(item, allowedValues)) {
                    errorMessage = "Given Values are not among the allowed values.";
                    return false;
                }
            }
            return true;
        } else {
            // in case if the inputValue is a single string , not a list 
            if (!isValueAllowed(inputValue, allowedValues)) {
                errorMessage = "Given Values are not among the allowed values.";
                return false;
            }
            return true;
        }
    }

    /**
     * Helper method to check if a value is in the allowed values list.
     */
    private boolean isValueAllowed(Object value, List<?> allowedValues) {
        String valStr = value.toString();
        for (Object allowed : allowedValues) {
            if (allowed != null && allowed.toString().equals(valStr)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
