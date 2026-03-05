/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import org.wso2.carbon.apimgt.api.model.AppConfigConstraintType;
import org.wso2.carbon.apimgt.api.model.KeyManagerApplicationConfigValidator;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Map;

public class RangeValidator implements KeyManagerApplicationConfigValidator {

    private String errorMessage;
    private AppConfigConstraintType type;

    public RangeValidator(AppConfigConstraintType type) {

        this.type = type;
    }

    @Override
    public void validateMetadata(Map<String, Object> constraints) throws APIManagementException {

        if (type == AppConfigConstraintType.RANGE || type == AppConfigConstraintType.MIN) {
            if (!constraints.containsKey(APIConstants.KeyManager.CONSTRAINT_FIELD_MIN)) {
                throw new APIManagementException("Minimum value ('min') not found for range constraint.");
            }
            if (!(constraints.get(APIConstants.KeyManager.CONSTRAINT_FIELD_MIN) instanceof Number)) {
                throw new APIManagementException("Minimum value ('min') must be a number.");
            }
        }
        if (type == AppConfigConstraintType.RANGE || type == AppConfigConstraintType.MAX) {
            if (!constraints.containsKey(APIConstants.KeyManager.CONSTRAINT_FIELD_MAX)) {
                throw new APIManagementException("Maximum value ('max') not found for range constraint.");
            }
            if (!(constraints.get(APIConstants.KeyManager.CONSTRAINT_FIELD_MAX) instanceof Number)) {
                throw new APIManagementException("Maximum value ('max') must be a number.");
            }
        }
        if (type == AppConfigConstraintType.RANGE) {
            double min = ((Number) constraints.get(APIConstants.KeyManager.CONSTRAINT_FIELD_MIN)).doubleValue();
            double max = ((Number) constraints.get(APIConstants.KeyManager.CONSTRAINT_FIELD_MAX)).doubleValue();
            if (min > max) {
                throw new APIManagementException("Minimum value cannot be greater than maximum value.");
            }
        }
    }

    @Override
    public boolean validate(Object inputValue, Map<String, Object> constraints) {

        if (!(inputValue instanceof Number)) {
            try {
                inputValue = Double.parseDouble(inputValue.toString());
            } catch (NumberFormatException e) {
                errorMessage = "Input value is not a valid number.";
                return false;
            }
        }
        double val = ((Number) inputValue).doubleValue();

        if (type == AppConfigConstraintType.RANGE || type == AppConfigConstraintType.MIN) {
            if (constraints.containsKey(APIConstants.KeyManager.CONSTRAINT_FIELD_MIN)) {
                double min = ((Number) constraints.get(APIConstants.KeyManager.CONSTRAINT_FIELD_MIN)).doubleValue();
                if (val < min) {
                    errorMessage = "Value is less than minimum";
                    return false;
                }
            }
        }
        if (type == AppConfigConstraintType.RANGE || type == AppConfigConstraintType.MAX) {
            if (constraints.containsKey(APIConstants.KeyManager.CONSTRAINT_FIELD_MAX)) {
                double max = ((Number) constraints.get(APIConstants.KeyManager.CONSTRAINT_FIELD_MAX)).doubleValue();
                if (val > max) {
                    errorMessage = "Value is greater than maximum";
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public String getErrorMessage() {

        return errorMessage;
    }
}
