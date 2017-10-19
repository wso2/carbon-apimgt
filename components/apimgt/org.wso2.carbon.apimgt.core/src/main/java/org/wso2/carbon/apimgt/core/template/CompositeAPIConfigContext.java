/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.template;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.template.dto.CompositeAPIEndpointDTO;
import org.wso2.carbon.apimgt.core.template.dto.TemplateBuilderDTO;

import java.util.List;

/**
 * Composite api context to generate the ballerina template
 */
public class CompositeAPIConfigContext extends ConfigContext {

    private ConfigContext configContext;
    private static final Logger log = LoggerFactory.getLogger(CompositeAPIConfigContext.class);
    private List<TemplateBuilderDTO> apiResources;
    private List<CompositeAPIEndpointDTO> compositeApiEndpoints;

    public CompositeAPIConfigContext(ConfigContext context, List<TemplateBuilderDTO> apiResources,
                                     List<CompositeAPIEndpointDTO> compositeApiEndpoints) {
        this.configContext = context;
        this.apiResources = apiResources;
        this.compositeApiEndpoints = compositeApiEndpoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws APITemplateException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VelocityContext getContext() {
        VelocityContext context = configContext.getContext();
        context.put("StringUtils", StringUtils.class);
        context.put("apiResources", this.apiResources);
        context.put("compositeApiEndpoints", this.compositeApiEndpoints);
        return context;
    }
}
