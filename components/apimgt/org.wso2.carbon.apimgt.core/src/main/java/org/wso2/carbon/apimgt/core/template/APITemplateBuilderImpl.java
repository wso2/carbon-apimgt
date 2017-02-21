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
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.APIMConfigurations;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.template.dto.TemplateBuilderDTO;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

/**
 * Generate API config template
 */
public class APITemplateBuilderImpl implements APITemplateBuilder {
    private static final Logger log = LoggerFactory.getLogger(APITemplateBuilderImpl.class);
    private API api;
    private String packageName;

    public APITemplateBuilderImpl(API api) {
        this();
        this.api = api;
    }

    public APITemplateBuilderImpl() {
        APIMConfigurations config = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        packageName = config.getGatewayPackageName();
    }

    @Override
    public String getConfigStringFromTemplate(List<TemplateBuilderDTO> apiResources) throws APITemplateException {
        StringWriter writer = new StringWriter();

        try {
            // build the context for template and apply the necessary decorators
            ConfigContext configcontext = new APIConfigContext(this.api, packageName);
            configcontext = new ResourceConfigContext(configcontext, this.api, apiResources);
            VelocityContext context = configcontext.getContext();
            VelocityEngine velocityengine = new VelocityEngine();
            velocityengine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            velocityengine.init();
            Template template = velocityengine.getTemplate("resources" + File.separator + "template.xml");
            template.merge(context, writer);
        } catch (ResourceNotFoundException e) {
            log.error("Template " + "resources" + File.separator + "template.xml not Found", e);
            throw new APITemplateException("Template " + "resources" + File.separator + "template.xml not Found",
                    ExceptionCodes.TEMPLATE_EXCEPTION);
        } catch (ParseErrorException e) {
            log.error("Syntax error in " + "resources" + File.separator + "template.xml", e);
            throw new APITemplateException("Syntax error in " + "resources" + File.separator + "template.xml",
                    ExceptionCodes.TEMPLATE_EXCEPTION);
        }
        return writer.toString();
    }

    @Override
    public String getGatewayConfigFromSwagger(String gatewayConfig, String swagger) throws APITemplateException {
        //TODO implement logic
        return "to be implement";
    }

    @Override
    public String getSwaggerFromGatewayConfig(String gatewayConfig) throws APITemplateException {
        //TODO implement logic
        return "to be implement";
    }

    @Override
    public String getEndpointConfigStringFromTemplate(List<Endpoint> endpoints) throws APITemplateException {
        StringWriter writer = new StringWriter();

        try {
            // build the context for template and apply the necessary decorators
            ConfigContext configcontext = new EndpointContext(endpoints, packageName);
            VelocityContext context = configcontext.getContext();
            VelocityEngine velocityengine = new VelocityEngine();
            velocityengine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            velocityengine.init();
            Template template = velocityengine.getTemplate("resources" + File.separator + "endpoint.xml");
            template.merge(context, writer);
        } catch (ResourceNotFoundException e) {
            log.error("Template " + "resources" + File.separator + "template.xml not Found", e);
            throw new APITemplateException("Template " + "resources" + File.separator + "endpoint.xml not Found",
                    ExceptionCodes.TEMPLATE_EXCEPTION);
        } catch (ParseErrorException e) {
            log.error("Syntax error in " + "resources" + File.separator + "template.xml", e);
            throw new APITemplateException("Syntax error in " + "resources" + File.separator + "endpoint.xml",
                    ExceptionCodes.TEMPLATE_EXCEPTION);
        }
        return writer.toString();
    }
}
