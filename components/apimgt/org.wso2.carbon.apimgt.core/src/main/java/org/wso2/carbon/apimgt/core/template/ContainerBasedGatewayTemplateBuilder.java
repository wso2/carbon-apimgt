/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.template;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayConstants;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

/**
 * Generate Container Based Service and Deployment using velocity template
 */
public class ContainerBasedGatewayTemplateBuilder {

    protected String cmsTemplateLocation = APIMgtConstants.RESOURCES + File.separator + APIMgtConstants.TEMPLATES +
            File.separator + ContainerBasedGatewayConstants.CONTAINER_GATEWAY_TEMPLATES + File.separator;
    public static final String CLASS_PATH = "classpath";
    public static final String CLASS_PATH_RESOURCE_LOADER = "classpath.resource.loader.class";

    /**
     * Set velocity context for Gateway Service.
     *
     * @param templateValues VelocityContext template values Map
     * @return Velocity Context Object
     */
    private VelocityContext setVelocityContextValues(Map<String, String> templateValues) {

        VelocityContext context = new VelocityContext();

        for (Map.Entry<String, String> entry : templateValues.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        return context;
    }

    /**
     * Init velocity engine.
     *
     * @return Velocity engine for service template
     */
    private VelocityEngine initVelocityEngine() {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, CLASS_PATH);
        velocityEngine.setProperty(CLASS_PATH_RESOURCE_LOADER, ClasspathResourceLoader.class.getName());
        velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new CommonsLogLogChute());
        velocityEngine.init();
        return velocityEngine;
    }

    /**
     * Generate template for given template values
     *
     * @param templateValues Template values
     * @param template       Template
     * @return template as a String
     */
    public String generateTemplate(Map<String, String> templateValues, String template) {

        StringWriter writer = new StringWriter();
        VelocityEngine velocityengine = initVelocityEngine();
        String templateLocation = cmsTemplateLocation + template;
        Template generateTemplate = velocityengine.getTemplate(templateLocation);
        VelocityContext context = setVelocityContextValues(templateValues);
        generateTemplate.merge(context, writer);
        return writer.toString();
    }

}
