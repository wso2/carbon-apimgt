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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.impl.wsdl.util.SequenceUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.StringWriter;
import java.lang.String;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Constructs API and resource configurations for the ESB/Synapse using a Apache velocity
 * templates.
 */
public class APITemplateBuilderImpl implements APITemplateBuilder {

    private static final Log log = LogFactory.getLog(APITemplateBuilderImpl.class);

    public static final String TEMPLATE_TYPE_VELOCITY = "velocity_template";
    public static final String TEMPLATE_TYPE_PROTOTYPE = "prototype_template";
    public static final String TEMPLATE_DEFAULT_API = "default_api_template";
    private static final String TEMPLATE_TYPE_ENDPOINT = "endpoint_template";
    private static final String TEMPLATE_TYPE_API_PRODUCT = "api_product_template";
    private API api;
    private APIProduct apiProduct;
    private String velocityLogPath = null;
    private List<HandlerConfig> handlers = new ArrayList<HandlerConfig>();

    public APITemplateBuilderImpl(API api) {
        this.api = api;
    }

    public APITemplateBuilderImpl(APIProduct apiProduct) {
        this.apiProduct = apiProduct;
    }

    @Override
    public String getConfigStringForTemplate(Environment environment) throws APITemplateException {
        StringWriter writer = new StringWriter();
        JSONObject originalProperties = null;
        try {
            // build the context for template and apply the necessary decorators
            ConfigContext configcontext = null;

            if (api != null) {
                originalProperties = api.getAdditionalProperties();
                // add new property for entires that has a __display suffix
                JSONObject modifiedProperties = getModifiedProperties(originalProperties);
                api.setAdditionalProperties(modifiedProperties);
                configcontext = createConfigContext(api, environment);
            } else { // API Product scenario
                configcontext = createConfigContext(apiProduct, environment);
            }

            //@todo: this validation might be better to do when the builder is initialized.
            configcontext.validate();

            VelocityContext context = configcontext.getContext();

            context.internalGetKeys();

            /*  first, initialize velocity engine  */
            VelocityEngine velocityengine = new VelocityEngine();
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                        CommonsLogLogChute.class.getName());
                velocityengine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
                velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            }

            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            initVelocityEngine(velocityengine);

            Template t = null;

            if (api != null) {
                t = velocityengine.getTemplate(getTemplatePath());
            } else {
                t = velocityengine.getTemplate(getApiProductTemplatePath());
            }

            t.merge(context, writer);
            // Reset the additional properties to the original values
            if (api != null && originalProperties != null) {
                api.setAdditionalProperties(originalProperties);
            }

        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }
        return writer.toString();
    }

    @Override
    public String getConfigStringForPrototypeScriptAPI(Environment environment) throws APITemplateException {
        StringWriter writer = new StringWriter();

        try {
            // build the context for template and apply the necessary decorators

            ConfigContext configcontext = new APIConfigContext(this.api);
            configcontext = new TransportConfigContext(configcontext, api);
            configcontext = new ResourceConfigContext(configcontext, api);
            configcontext = new EndpointBckConfigContext(configcontext, api);
            configcontext = new EndpointConfigContext(configcontext, api);
            configcontext = new SecurityConfigContext(configcontext, api);
            configcontext = new JwtConfigContext(configcontext);
            configcontext = new ResponseCacheConfigContext(configcontext, api);
            configcontext = new BAMMediatorConfigContext(configcontext);
            configcontext = new HandlerConfigContex(configcontext, handlers);
            configcontext = new EnvironmentConfigContext(configcontext, environment);
            configcontext = new TemplateUtilContext(configcontext);

            //@todo: this validation might be better to do when the builder is initialized.
            configcontext.validate();

            VelocityContext context = configcontext.getContext();

            context.internalGetKeys();

            /*  first, initialize velocity engine  */
            VelocityEngine velocityengine = new VelocityEngine();
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                        CommonsLogLogChute.class.getName());
                velocityengine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
                velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            }

            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            initVelocityEngine(velocityengine);

            Template t = velocityengine.getTemplate(this.getPrototypeTemplatePath());

            t.merge(context, writer);

        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }
        return writer.toString();
    }

    @Override
    public String getConfigStringForDefaultAPITemplate(String defaultVersion) throws APITemplateException {
        StringWriter writer = new StringWriter();

        try {
            VelocityEngine velocityengine = new VelocityEngine();
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                        CommonsLogLogChute.class.getName());
                velocityengine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
                velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            }

            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            initVelocityEngine(velocityengine);

            ConfigContext configcontext = new APIConfigContext(this.api);
            configcontext = new TransportConfigContext(configcontext, api);
            configcontext = new ResourceConfigContext(configcontext, api);
            configcontext = new TemplateUtilContext(configcontext);

            VelocityContext context = configcontext.getContext();
            context.put("defaultVersion", defaultVersion);
            String fwdApiContext = this.api.getContext();
            if (fwdApiContext != null && fwdApiContext.charAt(0) == '/') {
                fwdApiContext = fwdApiContext.substring(1);
            }
            context.put("fwdApiContext", fwdApiContext);

            // for default version, we remove the {version} param from the apiContext
            String apiContext = this.api.getContextTemplate();
            if(apiContext.contains("{version}")){
                apiContext = apiContext.replace("/{version}","");
                apiContext = apiContext.replace("{version}","");
            }

            context.put("apiContext", apiContext);

            Template t = velocityengine.getTemplate(this.getDefaultAPITemplatePath());

            t.merge(context, writer);
        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }
        return writer.toString();
    }

    /**
     * Sets the necessary variables to velocity context
     *
     * @param endpointType Type of the endpoint : production or sandbox
     * @return The string of endpoint file content
     * @throws APITemplateException Thrown if an error occurred
     */
    @Override
    public String getConfigStringForEndpointTemplate(String endpointType) throws APITemplateException {
        StringWriter writer = new StringWriter();

        try {
            ConfigContext configcontext = new APIConfigContext(this.api);
            configcontext = new EndpointBckConfigContext(configcontext, api);
            configcontext = new EndpointConfigContext(configcontext, api);
            configcontext = new TemplateUtilContext(configcontext);

            configcontext.validate();

            VelocityContext context = configcontext.getContext();

            context.internalGetKeys();

            VelocityEngine velocityengine = new VelocityEngine();
            if (!"not-defined".equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                        CommonsLogLogChute.class.getName());
                velocityengine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
                velocityengine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            }

            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            initVelocityEngine(velocityengine);

            context.put("type", endpointType);

            Template template = velocityengine.getTemplate(this.getEndpointTemplatePath());

            template.merge(context, writer);

        } catch (Exception e) {
            log.error("Velocity Error");
            throw new APITemplateException("Velocity Error", e);
        }
        return writer.toString();
    }

    private ConfigContext createConfigContext(API api, Environment environment)
            throws UserStoreException, RegistryException {
        ConfigContext configcontext = new APIConfigContext(api);
        configcontext = new TransportConfigContext(configcontext, api);
        configcontext = new ResourceConfigContext(configcontext, api);
        // this should be initialised before endpoint config context.
        configcontext = new EndpointBckConfigContext(configcontext, api);
        configcontext = new EndpointConfigContext(configcontext, api);
        configcontext = new SecurityConfigContext(configcontext, api);
        configcontext = new JwtConfigContext(configcontext);
        configcontext = new ResponseCacheConfigContext(configcontext, api);
        configcontext = new BAMMediatorConfigContext(configcontext);
        configcontext = new HandlerConfigContex(configcontext, handlers);
        configcontext = new EnvironmentConfigContext(configcontext, environment);
        configcontext = new TemplateUtilContext(configcontext);

        if (APIConstants.API_TYPE_SOAPTOREST.equals(api.getType()) || !StringUtils.isEmpty(api.getWsdlUrl())) {
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            String resourceInPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    api.getId().getProviderName() + RegistryConstants.PATH_SEPARATOR + api.getId().getApiName()
                    + RegistryConstants.PATH_SEPARATOR + api.getId().getVersion() + RegistryConstants.PATH_SEPARATOR
                    + SOAPToRESTConstants.SequenceGen.SOAP_TO_REST_IN_RESOURCE;
            String resourceOutPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    api.getId().getProviderName() + RegistryConstants.PATH_SEPARATOR + api.getId().getApiName()
                    + RegistryConstants.PATH_SEPARATOR + api.getId().getVersion() + RegistryConstants.PATH_SEPARATOR
                    + SOAPToRESTConstants.SequenceGen.SOAP_TO_REST_OUT_RESOURCE;
            UserRegistry registry = registryService.getGovernanceSystemRegistry(tenantId);
            configcontext = SequenceUtils.getSequenceTemplateConfigContext(registry, resourceInPath,
                    SOAPToRESTConstants.Template.IN_SEQUENCES, configcontext);
            configcontext = SequenceUtils.getSequenceTemplateConfigContext(registry, resourceOutPath,
                    SOAPToRESTConstants.Template.OUT_SEQUENCES, configcontext);
        }

        return configcontext;
    }

    public ConfigContext createConfigContext(APIProduct apiProduct, Environment environment) {
        StringWriter writer = new StringWriter();

        // build the context for template and apply the necessary decorators
        ConfigContext configcontext = new APIConfigContext(apiProduct);
        configcontext = new TransportConfigContext(configcontext, apiProduct);
        configcontext = new ResourceConfigContext(configcontext, apiProduct);

        configcontext = new ResponseCacheConfigContext(configcontext, apiProduct);
        configcontext = new BAMMediatorConfigContext(configcontext);
        configcontext = new HandlerConfigContex(configcontext, handlers);
        configcontext = new EnvironmentConfigContext(configcontext, environment);
        configcontext = new TemplateUtilContext(configcontext);
        configcontext = new SecurityConfigContext(configcontext, apiProduct);

        return configcontext;
    }

    @Override
    public OMElement getConfigXMLForTemplate(Environment environment) throws APITemplateException {
        try {
            return AXIOMUtil.stringToOM(getConfigStringForTemplate(environment));
        } catch (XMLStreamException e) {
            String msg = "Error converting string to OMElement - String: " + getConfigStringForTemplate(environment);
            log.error(msg, e);
            throw new APITemplateException(msg, e);
        }
    }

    public void addHandler(String handlerName, Map<String, String> properties) {
        addHandlerPriority(handlerName, properties, handlers.size());
    }

    public void addHandlerPriority(String handlerName, Map<String, String> properties, int priority) {
        HandlerConfig handler = new HandlerConfig(handlerName, properties);
        handlers.add(priority, handler);
    }

    public String getTemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator + APITemplateBuilderImpl.TEMPLATE_TYPE_VELOCITY + ".xml";
    }

    public String getPrototypeTemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator + APITemplateBuilderImpl.TEMPLATE_TYPE_PROTOTYPE + ".xml";
    }

    public String getDefaultAPITemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator + APITemplateBuilderImpl.TEMPLATE_DEFAULT_API + ".xml";
    }

    public String getEndpointTemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator + APITemplateBuilderImpl.TEMPLATE_TYPE_ENDPOINT + ".xml";
    }

    public String getApiProductTemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "api_templates" +
                File.separator + APITemplateBuilderImpl.TEMPLATE_TYPE_API_PRODUCT + ".xml";
    }

    public String getVelocityLogger() {
        if (this.velocityLogPath != null) {
            return this.velocityLogPath;
        } else {
            APIManagerConfigurationService config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
            String velocityLogPath = config.getAPIManagerConfiguration().getFirstProperty(APIConstants.VELOCITY_LOGGER);
            if (velocityLogPath != null && velocityLogPath.length() > 1) {
                this.velocityLogPath = velocityLogPath;
            } else {
                this.velocityLogPath = "not-defined";
            }
            return this.velocityLogPath;
        }
    }

    /**
     * Initialize velocity engine
     * @param velocityengine velocity engine object reference
     * @throws APITemplateException in case of an error
     */
    private void initVelocityEngine(VelocityEngine velocityengine) throws APITemplateException {
        Thread thread = Thread.currentThread();
        ClassLoader loader = thread.getContextClassLoader();
        thread.setContextClassLoader(this.getClass().getClassLoader());

        try {
            velocityengine.init();
        } catch (Exception e) {
            String msg = "Error while initiating the Velocity engine";
            log.error(msg, e);
            throw new APITemplateException(msg, e);
        } finally {
            thread.setContextClassLoader(loader);
        }
    }
    
    public JSONObject getModifiedProperties(JSONObject originalProperties) {
        JSONObject modifiedProperties = new JSONObject();
        if (originalProperties.size() > 0) {
            for (Iterator iterator = originalProperties.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                String val = (String) originalProperties.get(key);
                if (key.endsWith("__display")) {
                    modifiedProperties.put(key.replace("__display", ""), val);
                }
                modifiedProperties.put(key, val);
            }
        }
        return modifiedProperties;
    }
}
