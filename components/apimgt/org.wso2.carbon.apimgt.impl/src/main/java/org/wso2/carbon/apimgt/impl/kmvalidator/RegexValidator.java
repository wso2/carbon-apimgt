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

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Validator implementation for Regex-based constraints.
 */
public class RegexValidator implements KeyManagerApplicationConfigValidator {

    private String errorMessage;

    @Override
    public void validateMetadata(Map<String, Object> constraints) throws APIManagementException {
        Object regexObj = constraints.get("pattern");
        if (regexObj == null) {
            throw new APIManagementException("Regex pattern not found in constraints.");
        }
        String regex = regexObj.toString();
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new APIManagementException("Invalid regex pattern: " + regex, e);
        }
    }

    @Override
    public boolean validate(Object inputValue, Map<String, Object> constraints) {
        if (inputValue == null) {
            errorMessage = "Input value is null.";
            return false;
        }

        String value = inputValue.toString();
        Object regexObj = constraints.get("pattern");
        if (regexObj == null) {
            errorMessage = "Regex pattern not found in constraints.";
            return false;
        }
        String regex = regexObj.toString();

        if (!Pattern.matches(regex, value)) {
            errorMessage = "Value '" + value + "' does not match pattern '" + regex + "'.";
            return false;
        }

        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
