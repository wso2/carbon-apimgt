/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of converting and validating an API for API Platform Gateway format.
 * Used when Synapse (or internal) API is converted to platform api.yaml and sanitized.
 */
public class PlatformGatewayArtifactValidationResult {

    private boolean valid;
    private String convertedYaml;
    private List<String> missingFields;
    private List<String> invalidFields;

    public PlatformGatewayArtifactValidationResult() {
        this.missingFields = new ArrayList<>();
        this.invalidFields = new ArrayList<>();
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getConvertedYaml() {
        return convertedYaml;
    }

    public void setConvertedYaml(String convertedYaml) {
        this.convertedYaml = convertedYaml;
    }

    public List<String> getMissingFields() {
        return missingFields != null ? Collections.unmodifiableList(missingFields) : Collections.emptyList();
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields != null ? new ArrayList<>(missingFields) : new ArrayList<>();
    }

    public void addMissingField(String field) {
        if (this.missingFields == null) {
            this.missingFields = new ArrayList<>();
        }
        this.missingFields.add(field);
    }

    public List<String> getInvalidFields() {
        return invalidFields != null ? Collections.unmodifiableList(invalidFields) : Collections.emptyList();
    }

    public void setInvalidFields(List<String> invalidFields) {
        this.invalidFields = invalidFields != null ? new ArrayList<>(invalidFields) : new ArrayList<>();
    }

    public void addInvalidField(String field) {
        if (this.invalidFields == null) {
            this.invalidFields = new ArrayList<>();
        }
        this.invalidFields.add(field);
    }
}
