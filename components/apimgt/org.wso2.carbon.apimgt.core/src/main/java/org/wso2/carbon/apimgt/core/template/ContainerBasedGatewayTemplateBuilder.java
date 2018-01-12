package org.wso2.carbon.apimgt.core.template;

/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayConstants;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

/**
 * Generate Container Based Service and Deployment using velocity template
 */
public class ContainerBasedGatewayTemplateBuilder {

    protected String cmsTemplateLocation =
            "resources" + File.separator + "template" + File.separator + "container_gateway_templates" +
                    File.separator;
    public static final String CLASS_PATH = "classpath";
    public static final String CLASS_PATH_RESOURCE_LOADER = "classpath.resource.loader.class";

    /**
     * Set velocity context for Gateway Service.
     *
     * @param templateValues VelocityContext template values Map
     * @return Velocity Context Object
     */
    public VelocityContext setGatewayServiceContextValues(Map<String, String> templateValues) {

        VelocityContext context = new VelocityContext();
        context.put(ContainerBasedGatewayConstants.SERVICE_NAME,
                templateValues.get(ContainerBasedGatewayConstants.SERVICE_NAME));
        context.put(ContainerBasedGatewayConstants.NAMESPACE,
                templateValues.get(ContainerBasedGatewayConstants.NAMESPACE));
        context.put(ContainerBasedGatewayConstants.GATEWAY_LABEL,
                templateValues.get(ContainerBasedGatewayConstants.GATEWAY_LABEL));
        return context;
    }

    /**
     * Set velocity context  for Gateway Deployment.
     *
     * @param templateValues VelocityContext template values Map
     * @return Velocity Context Object
     */
    public VelocityContext setGatewayDeploymentContextValues(Map<String, String> templateValues) {

        VelocityContext context = new VelocityContext();
        context.put(ContainerBasedGatewayConstants.DEPLOYMENT_NAME,
                templateValues.get(ContainerBasedGatewayConstants.DEPLOYMENT_NAME));
        context.put(ContainerBasedGatewayConstants.NAMESPACE,
                templateValues.get(ContainerBasedGatewayConstants.NAMESPACE));
        context.put(ContainerBasedGatewayConstants.GATEWAY_LABEL,
                templateValues.get(ContainerBasedGatewayConstants.GATEWAY_LABEL));
        context.put(ContainerBasedGatewayConstants.CONTAINER_NAME,
                templateValues.get(ContainerBasedGatewayConstants.CONTAINER_NAME));
        context.put(ContainerBasedGatewayConstants.IMAGE,
                templateValues.get(ContainerBasedGatewayConstants.IMAGE));
        context.put(ContainerBasedGatewayConstants.API_CORE_URL,
                templateValues.get(ContainerBasedGatewayConstants.API_CORE_URL));
        context.put(ContainerBasedGatewayConstants.BROKER_HOST,
                templateValues.get(ContainerBasedGatewayConstants.BROKER_HOST));
        return context;
    }

    /**
     * Init velocity engine.
     *
     * @return Velocity engine for service template
     */
    public VelocityEngine initVelocityEngine() {
        VelocityEngine velocityengine = new VelocityEngine();
        velocityengine.setProperty(RuntimeConstants.RESOURCE_LOADER, CLASS_PATH);
        velocityengine.setProperty(CLASS_PATH_RESOURCE_LOADER, ClasspathResourceLoader.class.getName());
        velocityengine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new CommonsLogLogChute());
        velocityengine.init();
        return velocityengine;
    }

    /**
     * Return Gateway service template for policy level.
     *
     * @param serviceTemplateValues service template values map
     * @return Service Template as a String
     * @throws GatewayException If an error occurred when getting the Service template
     */
    public String getGatewayServiceTemplate(Map<String, String> serviceTemplateValues) throws GatewayException {

        StringWriter writer = new StringWriter();
        VelocityEngine velocityengine = initVelocityEngine();
        Template template = velocityengine.getTemplate(cmsTemplateLocation +
                ContainerBasedGatewayConstants.GATEWAY_SERVICE_TEMPLATE);
        VelocityContext context = setGatewayServiceContextValues(serviceTemplateValues);
        template.merge(context, writer);
        return writer.toString();
    }

    /**
     * Return Gateway deployment template for policy level.
     *
     * @param deploymentTemplateValues service template values map
     * @return Deployment Template as a String
     */
    public String getGatewayDeploymentTemplate(Map<String, String> deploymentTemplateValues) {

        StringWriter writer = new StringWriter();
        VelocityEngine velocityengine = initVelocityEngine();
        Template template = velocityengine.getTemplate(cmsTemplateLocation +
                ContainerBasedGatewayConstants.GATEWAY_DEPLOYMENT_TEMPLATE);
        VelocityContext context = setGatewayDeploymentContextValues(deploymentTemplateValues);
        template.merge(context, writer);
        return writer.toString();
    }
}
