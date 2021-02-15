package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Set if response caching enabled or not
 */
public class ResponseCacheConfigContext extends ConfigContextDecorator {
	
	private API api;
	private APIProduct apiProduct;

	public ResponseCacheConfigContext(ConfigContext context, API api) {
		super(context);
		this.api = api;
	}

    public ResponseCacheConfigContext(ConfigContext context, APIProduct apiProduct) {
        super(context);
        this.apiProduct = apiProduct;
    }
	
	public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        if (api != null) {
            if (APIConstants.ENABLED.equalsIgnoreCase(api.getResponseCache())) {
                context.put("responseCacheEnabled", Boolean.TRUE);
                context.put("responseCacheTimeOut", api.getCacheTimeout());
            } else {
                context.put("responseCacheEnabled", Boolean.FALSE);
            }
        } else if (apiProduct != null) {
            if (APIConstants.ENABLED.equalsIgnoreCase(apiProduct.getResponseCache())) {
                context.put("responseCacheEnabled", Boolean.TRUE);
                context.put("responseCacheTimeOut", apiProduct.getCacheTimeout());
            } else {
                context.put("responseCacheEnabled", Boolean.FALSE);
            }
        } else {
            context.put("responseCacheEnabled", Boolean.FALSE);
        }

        return context;
    }

}
