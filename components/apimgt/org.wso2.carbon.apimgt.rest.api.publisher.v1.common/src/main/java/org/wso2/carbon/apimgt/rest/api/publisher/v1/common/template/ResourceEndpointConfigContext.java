package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import org.apache.velocity.VelocityContext;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;

/**
 *
 */
public class ResourceEndpointConfigContext extends ConfigContextDecorator {
    private API api;
    private JSONObject resourceEndpointsConfig;

    public ResourceEndpointConfigContext(ConfigContext context, API api) {
        super(context);
        this.api = api;
    }

    public void validate() throws APITemplateException, APIManagementException {

    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        context.put("environmentType", null);

        return context;
    }
}
