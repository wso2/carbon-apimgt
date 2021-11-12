package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.ResourceEndpoint;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Set Resource-Endpoint in context
 */
public class ResourceEndpointConfigContext extends ConfigContextDecorator {
    private List<ResourceEndpoint> resourceEndpoints;
    private API api;
    private APIProduct apiProduct;
    private JSONObject resourceEndpointConfig;
    private Map<String, Map<String, EndpointSecurityModel>> resourceEndpointSecurityConfig;

    public ResourceEndpointConfigContext(ConfigContext context, List<ResourceEndpoint> resourceEndpoints, API api) {
        super(context);
        this.resourceEndpoints = resourceEndpoints;
        this.api = api;
    }

    public ResourceEndpointConfigContext(ConfigContext context, List<ResourceEndpoint> resourceEndpoints,
            APIProduct apiProduct) {
        super(context);
        this.resourceEndpoints = resourceEndpoints;
        this.apiProduct = apiProduct;
    }

    public void validate() throws APITemplateException, APIManagementException {
        super.validate();
        JSONObject resourceEndpointMap = new JSONObject();
        Map<String, Map<String, EndpointSecurityModel>> securityConfig = new HashMap<>();

        if (resourceEndpoints != null) {
            for (ResourceEndpoint resourceEndpoint : resourceEndpoints) {
                JSONObject resourceEndpointConfig = constructEndpointConfig(resourceEndpoint);
                resourceEndpointMap.put(resourceEndpoint.getId(), resourceEndpointConfig);

                if (resourceEndpoint.getSecurityConfig() != null) {
                    Map<String, EndpointSecurityModel> endpointSecurityConfig = new HashMap<>();
                    endpointSecurityConfig.put("resource", constructSecurityConfig(resourceEndpoint));
                    securityConfig.put(resourceEndpoint.getId(), endpointSecurityConfig);
                }
            }
        }
        this.resourceEndpointConfig = resourceEndpointMap;
        this.resourceEndpointSecurityConfig = securityConfig;
    }

    private JSONObject constructEndpointConfig(ResourceEndpoint resourceEndpoint) {
        JSONObject endpointConfig = new JSONObject();
        JSONObject resourceEndpointConfig = new JSONObject();

        endpointConfig.put("endpoint_type", resourceEndpoint.getEndpointType().toString().toLowerCase());
        if (this.api != null) {
            endpointConfig.put("endpointKey", this.api.getUuid() + "--" + resourceEndpoint.getId());
        } else {
            endpointConfig.put("endpointKey", this.apiProduct.getUuid() + "--" + resourceEndpoint.getId());
        }

        resourceEndpointConfig.put("url", resourceEndpoint.getUrl());

        Gson gson = new Gson();
        if (resourceEndpoint.getGeneralConfig() != null && !resourceEndpoint.getGeneralConfig().isEmpty()) {
            resourceEndpointConfig.put("config", gson.toJson(resourceEndpoint.getGeneralConfig()));
        }
        endpointConfig.put("resource_endpoints", resourceEndpointConfig);

        return endpointConfig;
    }

    private EndpointSecurityModel constructSecurityConfig(ResourceEndpoint resourceEndpoint)
            throws APITemplateException {
        Gson gson = new Gson();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> securityConfig = resourceEndpoint.getSecurityConfig();
        EndpointSecurityModel endpointSecurityModel = null;
        try {
            String jsonString = mapper.writeValueAsString(securityConfig);
            endpointSecurityModel = gson.fromJson(jsonString, EndpointSecurityModel.class);

            //Add support for BASIC
            if (endpointSecurityModel != null && endpointSecurityModel.isEnabled()) {
                if (StringUtils.isNotBlank(endpointSecurityModel.getUsername()) && StringUtils
                        .isNotBlank(endpointSecurityModel.getPassword())) {
                    endpointSecurityModel.setBase64EncodedPassword(new String(Base64.encodeBase64(
                            endpointSecurityModel.getUsername().concat(":").concat(endpointSecurityModel.getPassword())
                                    .getBytes())));
                }
            }

            if (APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH.equalsIgnoreCase(endpointSecurityModel.getType())) {
                String uniqueIdentifier;
                if (api != null) {
                    uniqueIdentifier = api.getUuid() + "--" + resourceEndpoint.getId();
                } else {
                    uniqueIdentifier = apiProduct.getUuid() + "--" + resourceEndpoint.getId();
                }
                endpointSecurityModel.setUniqueIdentifier(uniqueIdentifier);
            }
        } catch (JsonProcessingException e) {
            this.handleException("Unable to process the endpoint security JSON config");
        }
        return endpointSecurityModel;
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();
        context.put("resource_endpoint_config", this.resourceEndpointConfig);
        context.put("resource_endpoint_security_config", this.resourceEndpointSecurityConfig);

        return context;
    }
}
