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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.SoapToRestMediationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.SequenceUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constructs API and resource configurations for the ESB/Synapse using a Apache velocity
 * templates.
 */
public class APITemplateBuilderImpl implements APITemplateBuilder {

    public static final String TEMPLATE_TYPE_VELOCITY = "velocity_template";
    public static final String TEMPLATE_WEBSUB_API = "websub_api_template";
    public static final String TEMPLATE_TYPE_PROTOTYPE = "prototype_template";
    public static final String TEMPLATE_DEFAULT_API = "default_api_template";
    public static final String TEMPLATE_DEFAULT_WS_API = "default_ws_api_template";
    private static final Log log = LogFactory.getLog(APITemplateBuilderImpl.class);
    private static final String TEMPLATE_TYPE_ENDPOINT = "endpoint_template";
    private static final String TEMPLATE_TYPE_API_PRODUCT = "api_product_template";
    private List<SoapToRestMediationDto> soapToRestOutMediationDtoList;
    private List<SoapToRestMediationDto> soapToRestInMediationDtoList;
    private API api;
    private APIProduct apiProduct;
    private String velocityLogPath = null;
    private List<HandlerConfig> handlers = new ArrayList<HandlerConfig>();
    private Map<String, APIDTO> associatedAPIMap = new HashMap<>();

    public APITemplateBuilderImpl(API api) {

        this.api = api;
    }

    public APITemplateBuilderImpl(APIProduct apiProduct, Map<String, APIDTO> associatedAPIMap) {
        this.apiProduct = apiProduct;
        this.associatedAPIMap = associatedAPIMap;
    }

    public APITemplateBuilderImpl(API api, List<SoapToRestMediationDto> soapToRestInMediationDtoList,
            List<SoapToRestMediationDto> soapToRestOutMediationDtoList) {
        this(api);
        this.soapToRestInMediationDtoList = soapToRestInMediationDtoList;
        this.soapToRestOutMediationDtoList = soapToRestOutMediationDtoList;
    }

    public APITemplateBuilderImpl(API api, APIProduct apiProduct) {

        this.api = api;
        this.apiProduct = apiProduct;
    }

    @Override
    public String getConfigStringForTemplate(Environment environment) throws APITemplateException {

        StringWriter writer = new StringWriter();

        try {
            // build the context for template and apply the necessary decorators
            ConfigContext configcontext = null;

            if (api != null) {
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
            APIUtil.initializeVelocityContext(velocityengine);

            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            initVelocityEngine(velocityengine);

            Template t = null;

            if (api != null) {
                t = velocityengine.getTemplate(getTemplatePath());

                if (APIConstants.APITransportType.WS.toString().equals(api.getType())) {
                    context.put("topicMappings", this.api.getWebSocketTopicMappingConfiguration().getMappings());
                } else if (APIConstants.APITransportType.WEBSUB.toString().equals(api.getType())) {
                    String signingAlgorithm = api.getWebsubSubscriptionConfiguration().getSigningAlgorithm();
                    context.put("signingAlgorithm", signingAlgorithm.toLowerCase() + "=");
                    context.put("secret", api.getWebsubSubscriptionConfiguration().getSecret());
                    context.put("hmacSignatureGenerationAlgorithm", "Hmac" + signingAlgorithm);
                    context.put("signatureHeader", api.getWebsubSubscriptionConfiguration().getSignatureHeader());
                    context.put("isSecurityEnabled", !StringUtils.isEmpty(api.getWebsubSubscriptionConfiguration().
                            getSecret()));
                } else if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                    boolean isSubscriptionAvailable = false;
                    if (api.getWebSocketTopicMappingConfiguration() != null) {
                        isSubscriptionAvailable = true;
                        context.put(APIConstants.VELOCITY_API_WEBSOCKET_TOPIC_MAPPINGS,
                                this.api.getWebSocketTopicMappingConfiguration().getMappings());
                    }
                    context.put(APIConstants.VELOCITY_GRAPHQL_API_SUBSCRIPTION_AVAILABLE, isSubscriptionAvailable);
                }
            } else {
                t = velocityengine.getTemplate(getApiProductTemplatePath());
            }

            t.merge(context, writer);

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
            configcontext = new HandlerConfigContex(configcontext, handlers);
            configcontext = new EnvironmentConfigContext(configcontext, environment);
            configcontext = new TemplateUtilContext(configcontext);

            //@todo: this validation might be better to do when the builder is initialized.
            configcontext.validate();

            VelocityContext context = configcontext.getContext();

            context.internalGetKeys();

            /*  first, initialize velocity engine  */
            VelocityEngine velocityengine = new VelocityEngine();
            APIUtil.initializeVelocityContext(velocityengine);

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

    /**
     * Sets the necessary variables to velocity context.
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
            configcontext = new EndpointConfigContext(configcontext, this.apiProduct, api);
            configcontext = new TemplateUtilContext(configcontext);

            configcontext.validate();

            VelocityContext context = configcontext.getContext();

            context.internalGetKeys();

            VelocityEngine velocityengine = new VelocityEngine();
            APIUtil.initializeVelocityContext(velocityengine);

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

    @Override
    public String getConfigStringForWebSocketEndpointTemplate(String endpointType, String resourceKey,
                                                              String endpointUrl)
            throws APITemplateException {

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
            APIUtil.initializeVelocityContext(velocityengine);

            velocityengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
            initVelocityEngine(velocityengine);

            context.put("type", endpointType + "_endpoints");
            context.put("websocketResourceKey", resourceKey);
            context.put("endpointUrl", endpointUrl);

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
        configcontext = new HandlerConfigContex(configcontext, handlers);
        configcontext = new EnvironmentConfigContext(configcontext, environment);
        configcontext = new TemplateUtilContext(configcontext);

        if (APIConstants.API_TYPE_SOAPTOREST.equals(api.getType()) || !StringUtils.isEmpty(api.getWsdlUrl())) {
            configcontext = SequenceUtils.getSequenceTemplateConfigContext(soapToRestInMediationDtoList,
                    SOAPToRESTConstants.Template.IN_SEQUENCES, configcontext);
            configcontext = SequenceUtils.getSequenceTemplateConfigContext(soapToRestOutMediationDtoList,
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
        configcontext = new HandlerConfigContex(configcontext, handlers);
        configcontext = new EnvironmentConfigContext(configcontext, environment);
        configcontext = new TemplateUtilContext(configcontext);
        configcontext = new SecurityConfigContext(configcontext, apiProduct, associatedAPIMap);

        return configcontext;
    }

    public void addHandler(String handlerName, Map<String, String> properties) {

        addHandlerPriority(handlerName, properties, handlers.size());
    }

    public void addHandlerPriority(String handlerName, Map<String, String> properties, int priority) {

        HandlerConfig handler = new HandlerConfig(handlerName, properties);
        handlers.add(priority, handler);
    }

    public String getTemplatePath() {

        if (APIConstants.APITransportType.WEBSUB.toString().equals(this.api.getType())) {
            return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator +
                    APITemplateBuilderImpl.TEMPLATE_WEBSUB_API + ".xml";
        }
        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator +
                APITemplateBuilderImpl.TEMPLATE_TYPE_VELOCITY + ".xml";
    }

    public String getPrototypeTemplatePath() {

        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator +
                APITemplateBuilderImpl.TEMPLATE_TYPE_PROTOTYPE + ".xml";
    }

    public String getDefaultAPITemplatePath() {

        if (APIConstants.APITransportType.WS.toString().equals(this.api.getType())) {
            return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator +
                    APITemplateBuilderImpl.TEMPLATE_DEFAULT_WS_API + ".xml";
        }
        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator +
                APITemplateBuilderImpl.TEMPLATE_DEFAULT_API + ".xml";
    }

    public String getEndpointTemplatePath() {

        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator +
                APITemplateBuilderImpl.TEMPLATE_TYPE_ENDPOINT + ".xml";
    }

    public String getApiProductTemplatePath() {

        return "repository" + File.separator + "resources" + File.separator + "api_templates" +
                File.separator + APITemplateBuilderImpl.TEMPLATE_TYPE_API_PRODUCT + ".xml";
    }

    public String getVelocityLogger() {

        if (this.velocityLogPath != null) {
            return this.velocityLogPath;
        } else {
            APIManagerConfigurationService config =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
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
     * Initialize velocity engine.
     *
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
}
