package org.wso2.carbon.apimgt.impl.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Set if response caching enabled or not
 */
public class ResponseCacheConfigContext extends ConfigContextDecorator {
	
	private API api;

	public ResponseCacheConfigContext(ConfigContext context, API api) {
		super(context);
		this.api = api;
	}
	
	public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        if (APIConstants.ENABLED.equalsIgnoreCase(api.getResponseCache())) {
            context.put("responseCacheEnabled", Boolean.TRUE);
            context.put("responseCacheTimeOut", api.getCacheTimeout());
        } else {
            context.put("responseCacheEnabled", Boolean.FALSE);
        }

        return context;
    }

}
