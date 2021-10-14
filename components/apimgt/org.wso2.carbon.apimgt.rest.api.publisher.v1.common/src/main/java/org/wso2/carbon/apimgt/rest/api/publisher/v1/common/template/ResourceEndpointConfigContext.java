package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import com.google.gson.Gson;
import org.apache.velocity.VelocityContext;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ResourceEndpoint;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;

import java.util.List;

/**
 * Set Resource-Endpoint in context
 */
public class ResourceEndpointConfigContext extends ConfigContextDecorator {
    private List<ResourceEndpoint> resourceEndpoints;
    private API api;
    private JSONObject resourceEndpointConfig;


    public ResourceEndpointConfigContext(ConfigContext context, List<ResourceEndpoint> resourceEndpoints, API api) {
        super(context);
        this.resourceEndpoints = resourceEndpoints;
        this.api = api;
    }

    public void validate() throws APITemplateException, APIManagementException {
        super.validate();
        JSONObject resourceEndpointMap = new JSONObject();

        for (ResourceEndpoint resourceEndpoint : resourceEndpoints) {
            JSONObject resourceEndpointConfig = constructEndpointConfig(resourceEndpoint);
            resourceEndpointMap.put(resourceEndpoint.getId(), resourceEndpointConfig);
        }
        this.resourceEndpointConfig = resourceEndpointMap;
    }

    private JSONObject constructEndpointConfig(ResourceEndpoint resourceEndpoint) {
        JSONObject endpointConfig = new JSONObject();
        JSONObject endpointSecurityConfig = new JSONObject();
        JSONObject resourceEndpointConfig = new JSONObject();

        endpointConfig.put("endpoint_type", resourceEndpoint.getEndpointType().toString().toLowerCase());
        endpointConfig.put("endpointKey", this.api.getUuid() + "--" + resourceEndpoint.getId());

        Gson gson = new Gson();
        if (resourceEndpoint.getSecurityConfig() != null && !resourceEndpoint.getSecurityConfig().isEmpty()) {
            endpointSecurityConfig.put("resource", gson.toJson(resourceEndpoint.getSecurityConfig()));
            endpointConfig.put("endpoint_security", endpointSecurityConfig);
        }

        if (resourceEndpoint.getGeneralConfig() != null && !resourceEndpoint.getGeneralConfig().isEmpty()) {
            resourceEndpointConfig.put("config", gson.toJson(resourceEndpoint.getGeneralConfig()));
            resourceEndpointConfig.put("url", resourceEndpoint.getUrl());
            endpointConfig.put("resource_endpoints", resourceEndpointConfig);
        }

        return endpointConfig;
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();
        context.put("resource_endpoint_config", this.resourceEndpointConfig);

        return context;
    }
}
