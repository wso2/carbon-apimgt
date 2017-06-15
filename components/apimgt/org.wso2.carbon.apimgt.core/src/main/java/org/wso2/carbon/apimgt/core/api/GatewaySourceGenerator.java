/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/
package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.template.APIConfigContext;
import org.wso2.carbon.apimgt.core.template.APITemplateException;
import org.wso2.carbon.apimgt.core.template.dto.CompositeAPIEndpointDTO;
import org.wso2.carbon.apimgt.core.template.dto.TemplateBuilderDTO;

import java.util.List;

/**
 * Used to provide abstract layer to generate API type template
 */
public interface GatewaySourceGenerator {
    /**
     * Generate initial service implementation for a API
     *
     * @param apiResources List of api resources.
     * @return service impl as Text
     * @throws APITemplateException throws if an error occurred
     */
    String getConfigStringFromTemplate(List<TemplateBuilderDTO> apiResources) throws APITemplateException;

    /**
     * Generate initial endpoint config
     * @param endpoint List of endpoints.
     * @return endpoint source as Text
     * @throws APITemplateException throws if an error occurred
     */
    String getEndpointConfigStringFromTemplate(Endpoint endpoint) throws APITemplateException;

    /**
     * Used to update or create service implementation using a swagger
     *
     * @param gatewayConfig service impl text
     * @param swagger       swagger text
     * @return updated service impl
     * @throws APITemplateException throws if an error occurred
     */
    String getGatewayConfigFromSwagger(String gatewayConfig, String swagger) throws APITemplateException;

    /**
     * Used to generate swagger from a service implementation
     *
     * @param gatewayConfig service impl text
     * @return generated swagger
     * @throws APITemplateException throws if an error occurred
     */
    String getSwaggerFromGatewayConfig(String gatewayConfig) throws APITemplateException;

    /**
     * Used to set API Config context.
     * @param apiConfigContext  APIConfigContext instance
     */
    void setApiConfigContext(APIConfigContext apiConfigContext);

    /**
     * Generate initial service implementation for a composite API
     *
     * @param apiResources List of api resources.
     * @param compositeApiEndpoints List of api endpoints of subscribed APIs.
     * @return service impl as Text
     * @throws APITemplateException throws if an error occurred
     */
    String getCompositeAPIConfigStringFromTemplate(List<TemplateBuilderDTO> apiResources,
                                                   List<CompositeAPIEndpointDTO> compositeApiEndpoints)
                                                   throws APITemplateException;
}
