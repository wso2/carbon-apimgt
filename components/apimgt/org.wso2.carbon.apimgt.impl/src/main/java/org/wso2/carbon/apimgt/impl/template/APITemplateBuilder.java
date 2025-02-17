/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.template;

import org.wso2.carbon.apimgt.api.dto.EndpointConfigDTO;
import org.wso2.carbon.apimgt.api.dto.EndpointDTO;
import org.wso2.carbon.apimgt.api.model.Environment;

import java.util.List;

public interface APITemplateBuilder {

    String getConfigStringForTemplate(Environment environment) throws APITemplateException;

    /**
     * Generates a configuration string for AI API based on the given environment.
     *
     * @param environment     The deployment environment for which the configuration string is generated.
     * @param endpointDTOList List of Endpoint
     * @return A string representing the AI API configuration.
     * @throws APITemplateException If an error occurs while processing the Velocity template.
     */
    String getConfigStringForAIAPI(Environment environment, List<EndpointDTO> endpointDTOList) throws APITemplateException;

    String getConfigStringForPrototypeScriptAPI(Environment environment) throws APITemplateException;

    String getConfigStringForEndpointTemplate(String endpointType, String endpointUuid, EndpointConfigDTO endpointConfig) throws APITemplateException;

    String getConfigStringForWebSocketEndpointTemplate(String endpointType, String resourceKey, String endpointUrl) throws APITemplateException;

}
