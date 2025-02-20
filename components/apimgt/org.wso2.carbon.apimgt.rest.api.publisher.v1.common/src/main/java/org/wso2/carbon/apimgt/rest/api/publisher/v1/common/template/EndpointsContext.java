/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.SimplifiedEndpoint;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;

import java.util.List;

/**
 * EndpointsContext is responsible for handling API and APIProduct endpoint configurations.
 * It extends {@link ConfigContextDecorator} to inject endpoint details into the VelocityContext.
 */
public class EndpointsContext extends ConfigContextDecorator {

    private API api;
    private List<SimplifiedEndpoint> endpoints;
    private SimplifiedEndpoint defaultEndpoint;

    /**
     * Constructs an EndpointsContext instance for an API.
     *
     * @param context   The base configuration context
     * @param api       The API associated with the endpoints
     * @param endpoints The list of endpoints
     */
    public EndpointsContext(ConfigContext context, API api, List<SimplifiedEndpoint> endpoints,
                            SimplifiedEndpoint defaultEndpoint) {

        super(context);
        this.api = api;
        this.endpoints = endpoints;
        this.defaultEndpoint = defaultEndpoint;
    }

    /**
     * Validates the configuration context.
     *
     * @throws APITemplateException   If there is an error in the API template processing
     * @throws APIManagementException If there is an API management-related error
     */
    @Override
    public void validate() throws APITemplateException, APIManagementException {

        super.validate();
    }

    /**
     * Populates the Velocity context with endpoint details categorized by deploymentStage.
     *
     * @return VelocityContext containing endpoint configurations
     */
    @Override
    public VelocityContext getContext() {

        VelocityContext context = super.getContext();

        if (this.endpoints != null) {
            context.put("endpoints", endpoints);
        }
        context.put("defaultEndpoint", defaultEndpoint);
        return context;
    }
}
