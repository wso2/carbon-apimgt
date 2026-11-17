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
import org.wso2.carbon.apimgt.api.model.SimplifiedEndpoint;

import java.util.List;

public interface APITemplateBuilder {

    String getConfigStringForTemplate(Environment environment) throws APITemplateException;

    String getConfigStringForAIAPI(Environment environment, SimplifiedEndpoint productionEndpoint,
                                   SimplifiedEndpoint sandboxEndpoint) throws APITemplateException;

    String getConfigStringForPrototypeScriptAPI(Environment environment) throws APITemplateException;

    String getConfigStringEndpointConfigTemplate(String endpointType, String endpointUuid, EndpointConfigDTO endpointConfig) throws APITemplateException;

    String getConfigStringForWebSocketEndpointTemplate(String endpointType, String resourceKey, String endpointUrl) throws APITemplateException;

    String getStringForEndpoints(String deploymentStage, List<SimplifiedEndpoint> endpoints,
                                 SimplifiedEndpoint defaultEndpoint) throws APITemplateException;

    /**
     * Renders the endpoints sequence, optionally wiring in the fault sequence with onError. A default, so a builder
     * written against the 3-argument version still works (it ignores the flag).
     *
     * @param faultFailoverEnabled Whether to add the onError attribute.
     */
    default String getStringForEndpoints(String deploymentStage, List<SimplifiedEndpoint> endpoints,
                                         SimplifiedEndpoint defaultEndpoint, boolean faultFailoverEnabled)
            throws APITemplateException {

        return getStringForEndpoints(deploymentStage, endpoints, defaultEndpoint);
    }

    /**
     * Renders the fault sequence the endpoints sequence's onError attribute points to.
     *
     * @param deploymentStage The stage of deployment (PRODUCTION / SANDBOX).
     * @return The rendered fault sequence.
     * @throws APITemplateException If an error occurs during rendering.
     */
    default String getStringForEndpointsFaultSequence(String deploymentStage) throws APITemplateException {

        throw new APITemplateException("Fault failover sequence rendering is not supported.");
    }

}
