package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class BAMMediatorConfigContext extends ConfigContextDecorator {

    public BAMMediatorConfigContext(ConfigContext context) {
        super(context);
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();
        boolean enabledStats = APIUtil.isAnalyticsEnabled();
        if (enabledStats) {
            context.put("statsEnabled", Boolean.TRUE);
        } else {
            context.put("statsEnabled", Boolean.FALSE);
        }

        return context;
    }

}
