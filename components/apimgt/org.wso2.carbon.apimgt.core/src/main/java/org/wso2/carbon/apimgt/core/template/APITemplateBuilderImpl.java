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
package org.wso2.carbon.apimgt.core.template;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.template.dto.TemplateBuilderDTO;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

/**
 * Generate API config template
 */
public class APITemplateBuilderImpl implements APITemplateBuilder {
    private API api;
    private List<TemplateBuilderDTO> apiResources;

    public APITemplateBuilderImpl(API.APIBuilder apiBuilder, List<TemplateBuilderDTO> apiResources) {
        this.api = apiBuilder.build();
        this.apiResources = apiResources;
    }

    @Override
    public String getConfigStringForTemplate() throws APITemplateException {
        StringWriter writer = new StringWriter();

        try {
            // build the context for template and apply the necessary decorators
            ConfigContext configcontext = new APIConfigContext(this.api);
            configcontext = new ResourceConfigContext(configcontext, this.api, this.apiResources);
            VelocityContext context = configcontext.getContext();
            VelocityEngine velocityengine = new VelocityEngine();
            velocityengine.init();
            Template t = velocityengine.getTemplate("resources" + File.separator + "template.xml");
            t.merge(context, writer);
        } catch (Exception e) {
            //        log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);

        }
        return writer.toString();
    }

    @Override
    public String getConfigStringForPrototypeScriptAPI() throws APITemplateException {
        return null;
    }

    @Override
    public String getConfigStringForDefaultAPITemplate() throws APITemplateException {
        return null;
    }
}
