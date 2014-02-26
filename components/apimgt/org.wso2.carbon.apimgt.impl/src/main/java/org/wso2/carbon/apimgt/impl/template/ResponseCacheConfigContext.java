package org.wso2.carbon.apimgt.impl.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

/**
 * Set if response caching enabled or not
 */
public class ResponseCacheConfigContext extends ConfigContextDecorator{
	
	private API api;

	public ResponseCacheConfigContext(ConfigContext context, API api) {
		super(context);
		this.api = api;
	}
	
	public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        if (APIConstants.API_RESPONSE_CACHE_ENABLED.equalsIgnoreCase(api.getResponseCache())) {
            context.put("responseCacheEnabled", true);
            context.put("responseCacheTimeOut", api.getCacheTimeout());
        } else {
            context.put("responseCacheEnabled", false);
        }

        return context;
    }

}
