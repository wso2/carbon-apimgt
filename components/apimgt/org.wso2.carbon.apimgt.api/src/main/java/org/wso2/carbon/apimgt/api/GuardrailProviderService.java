/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.dto.GuardrailProviderConfigurationDTO;

import java.util.Map;

/**
 * Interface for Guardrail Provider Services.
 * This interface defines the method for integrating with external guardrail provider services.
 */
public interface GuardrailProviderService {
    /**
     * Initialize the provider with required HTTP client and configuration properties.
     *
     */
    void init(GuardrailProviderConfigurationDTO configurationDTO) throws APIManagementException;

    /**
     * The type identifier for this provider (e.g., "AZURE-CONTENTSAFETY", "AWSBEDROCK-GUARDRAILS", etc).
     *
     * @return A unique string identifier.
     */
    String getType();

    /**
     * Makes a callout to a guardrails service using the provided configuration parameters.
     * This method is used to invoke external logic or validation rules defined by the guardrails layer.
     *
     * @param callOutConfig a map containing the necessary configuration and input parameters for the guardrails call.
     * @return the response returned by the guardrails service as a string.
     * @throws APIManagementException if an error occurs while invoking the guardrails service or processing the response.
     */
    String callOut(Map<String, Object> callOutConfig) throws APIManagementException;
}
