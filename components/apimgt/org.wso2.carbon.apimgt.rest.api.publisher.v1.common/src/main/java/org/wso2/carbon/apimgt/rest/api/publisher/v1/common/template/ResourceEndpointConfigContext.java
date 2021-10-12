package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import com.google.gson.Gson;
import org.apache.velocity.VelocityContext;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ResourceEndpoint;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;

/**
 *
 */
public class ResourceEndpointConfigContext extends ConfigContextDecorator {
    private ResourceEndpoint resourceEndpoint;
    private API api;
    private JSONObject resourceEndpointConfig;


    public ResourceEndpointConfigContext(ConfigContext context, ResourceEndpoint resourceEndpoint, API api) {
        super(context);
        this.resourceEndpoint = resourceEndpoint;
        this.api = api;
    }

    public void validate() throws APITemplateException, APIManagementException {
        //construct endpoint config to match with normal api endpoint config
        JSONObject endpointConfig = new JSONObject();
        endpointConfig.put("endpoint_type", resourceEndpoint.getEndpointType().toString().toLowerCase());
        if (!resourceEndpoint.getSecurityConfig().isEmpty()) {
            Gson gson = new Gson();
            endpointConfig.put("endpoint_security",
                    new JSONObject().put("resource", gson.toJson(resourceEndpoint.getSecurityConfig())));
        }
        JSONObject resourceEndpointConfig = new JSONObject();
        resourceEndpointConfig.put("config", resourceEndpoint.getGeneralConfig());
        resourceEndpointConfig.put("url", resourceEndpoint.getUrl());
        resourceEndpointConfig.put("endpointKey", this.api.getUuid() + "--" + resourceEndpoint.getId());
        endpointConfig.put("resource_endpoints", resourceEndpointConfig);

        this.resourceEndpointConfig = endpointConfig;
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        context.put("type", "resource_endpoints");
        context.put("endpoint_config", this.resourceEndpointConfig);

        return context;
    }
}
